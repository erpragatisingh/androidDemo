/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kodak.relive.thumbnail;


import java.util.ArrayList;
import java.util.HashSet;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.kodak.gallery.camera.CropImage;
import com.kodak.gallery.camera.DeleteImage;
import com.kodak.gallery.camera.GridViewL;
import com.kodak.gallery.camera.ImageLoader;
import com.kodak.gallery.camera.ImageManager;
import com.kodak.gallery.camera.MenuHelper;
import com.kodak.gallery.camera.ViewImage;
import com.kodak.gallery.camera.gallery.IImage;
import com.kodak.gallery.camera.gallery.IImageList;
import com.kodak.gallery.camera.gallery.ImageList;
import com.kodak.gallery.camera.gallery.VideoList;
import com.kodak.gallery.camera.gallery.VideoObject;
import com.kodak.relive.common.ThumbCallingActivity;
import com.kodak.relive.common.ThumbScrollBar.ThumbScroller;
import com.kodak.relive.main.MainScreen;

public class Thumbnail extends Activity implements
	com.kodak.gallery.camera.GridViewL.Listener,
	com.kodak.gallery.camera.GridViewL.DrawAdapter,
	com.kodak.relive.common.ThumbScrollBar.ThumbScroller.OnThumbChangeListener {
	
	public final static String EXTRA_SIZE_LIMIT = "android.intent.extra.sizeLimit";
	private static final String STATE_SCROLL_POSITION = "scroll_position";
    private static final String STATE_SELECTED_INDEX = "first_index";

    private static final String TAG = "Thumbnail";
    private static final float INVALID_POSITION = -1f;
    private ImageManager.ImageListParam mParam;
    private IImageList mAllImages;
    private int mInclusion;
    boolean mSortAscending = true;
    private View mNoImagesView;
    public static final int CROP_MSG = 2;
    public static final int IMAGE_INDEX_MSG = 3;

    private Dialog mMediaScanningDialog;
    private MenuItem mSlideShowItem;
    private SharedPreferences mPrefs;
    private long mVideoSizeLimit = Long.MAX_VALUE;
    private View mFooterOrganizeView;

    private BroadcastReceiver mReceiver = null;

    private final Handler mHandler = new Handler();
    private boolean mLayoutComplete;
    private boolean mPausing = true;
    private ImageLoader mLoader;
    private GridViewL mCgv = null;
    Uri mCropResultUri;

    // The index of the first picture in CustomGridView.
    private int mSelectedIndex = GridViewL.INDEX_NONE;
    private float mScrollPosition = INVALID_POSITION;
    private boolean mConfigurationChanged = false;
    private boolean mIsCenterIndex = false;
    private HashSet<IImage> mMultiSelected = null;
    ImageButton mMainBtn, mShareBtn, mDeleteBtn;
    ImageButton mCancelBtn, mNextBtn;
    TextView mShareTxtView, mDelTxtView;

	private ThumbScroller mSeekBar; 
	private Bitmap BitmapOrg , BitmapThumb;
	private BitmapDrawable bmp, bmpT;
	private int curr=0;
	private boolean setThumb = false;
    @Override
    public void onCreate(Bundle icicle) {
    	Log.e("onCreate", "ImageGallery");
        super.onCreate(icicle);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.thumbnail);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        						WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mNoImagesView = findViewById(R.id.no_images);       
       	mCgv = (GridViewL) findViewById(R.id.gridL);
        mCgv.setListener(this);
       
        mMainBtn 		= (ImageButton)findViewById(R.id.mainBtn);
        mShareBtn 		= (ImageButton)findViewById(R.id.shareBtn);
        mDeleteBtn 		= (ImageButton)findViewById(R.id.deleteBtn);
        mCancelBtn 		= (ImageButton)findViewById(R.id.cancelBtn);
		mNextBtn 		= (ImageButton)findViewById(R.id.nextBtn);
		mShareTxtView 	= (TextView)findViewById(R.id.shareText);
		mDelTxtView 	= (TextView)findViewById(R.id.delText);
        init_button_click_handler();
       
        if (isPickIntent()) {
            mVideoSizeLimit = getIntent().getLongExtra(
                    EXTRA_SIZE_LIMIT, Long.MAX_VALUE);
        } else {
            mVideoSizeLimit = Long.MAX_VALUE;
        }
        registerForContextMenu(mCgv);
        mCgv.setOnCreateContextMenuListener(new CreateContextMenuListener());
		setupInclusion();
        mLoader = new ImageLoader(getContentResolver(), mHandler);
        
		BitmapOrg = BitmapFactory.decodeResource (getResources(),
					R.drawable.scroll_bar1);        
		bmp = new BitmapDrawable(BitmapOrg); 
		BitmapThumb = BitmapFactory.decodeResource (getResources(),
					R.drawable.thumb2);        
		bmpT = new BitmapDrawable(BitmapThumb);
  	    mSeekBar = (ThumbScroller)findViewById(R.id.seek);
  	    mSeekBar.setProgressDrawable(bmp);
   	    mSeekBar.setThumbDrawable(bmpT);
        mSeekBar.setOnThumbScrollerChangeListener(this);        
    }
    
    private void init_button_click_handler()
    {    	
    	View.OnClickListener handler = new View.OnClickListener(){
    		public void onClick(View v){
    			switch (v.getId()){
    				case R.id.mainBtn:
    					Intent mainIntent = new Intent();
    					mainIntent.setClass(Thumbnail.this, MainScreen.class);
    			        startActivity(mainIntent);
    					finish();
    					break;
    				case R.id.deleteBtn:
    					mMainBtn.setVisibility(View.GONE);
    			        mShareBtn.setVisibility(View.GONE);
    			        mDeleteBtn.setVisibility(View.GONE);
    			        mShareTxtView.setVisibility(View.GONE);
    			        mCancelBtn.setVisibility(View.VISIBLE);
    					mNextBtn.setVisibility(View.VISIBLE);    					
    					mDelTxtView.setVisibility(View.VISIBLE);
    					openMultiSelectMode();
    					break;
    				case R.id.shareBtn:
    					mMainBtn.setVisibility(View.GONE);
    			        mShareBtn.setVisibility(View.GONE);
    			        mDeleteBtn.setVisibility(View.GONE);
    			        mDelTxtView.setVisibility(View.GONE);
    			        mCancelBtn.setVisibility(View.VISIBLE);
    					mNextBtn.setVisibility(View.VISIBLE);
    					mShareTxtView.setVisibility(View.VISIBLE);
    					openMultiSelectMode();
    					break;
    				case R.id.cancelBtn:
    					mCancelBtn.setVisibility(View.GONE);
    					mNextBtn.setVisibility(View.GONE);
    					mShareTxtView.setVisibility(View.GONE);
    					mDelTxtView.setVisibility(View.GONE);
    					mMainBtn.setVisibility(View.VISIBLE);
    			        mShareBtn.setVisibility(View.VISIBLE);
    			        mDeleteBtn.setVisibility(View.VISIBLE);    			        
    			        closeMultiSelectMode();
    			        break;
    				case R.id.nextBtn:
    					//TODO
    					break;
    			}
    		}
    	};
    	mMainBtn.setOnClickListener(handler);
    	mShareBtn.setOnClickListener(handler);
    	mDeleteBtn.setOnClickListener(handler);
    	mCancelBtn.setOnClickListener(handler);
    	mNextBtn.setOnClickListener(handler);
    }
    

    private MenuItem addSlideShowMenu(Menu menu) {
    	Log.e("addSlideShowMenu", "ImageGallery");
        return menu.add(Menu.NONE, Menu.NONE, 1,
                R.string.slide_show)
                .setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //return onSlideShowClicked();
                    	//HCL RELEASE2
                    	return true;
                    }
                }).setIcon(android.R.drawable.ic_menu_slideshow);
    }

    public boolean onSlideShowClicked() {
    	Log.e("onSlideShowClicked", "ImageGallery");
        if (!canHandleEvent()) {
            return false;
        }
        IImage img = getCurrentImage();
        if (img == null) {
            img = mAllImages.getImageAt(0);
            if (img == null) {
                return true;
            }
        }
        Uri targetUri = img.fullSizeImageUri();
        Uri thisUri = getIntent().getData();
        if (thisUri != null) {
            String bucket = thisUri.getQueryParameter("bucketId");
            if (bucket != null) {
                targetUri = targetUri.buildUpon()
                        .appendQueryParameter("bucketId", bucket)
                        .build();
            }
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, targetUri);
        intent.putExtra("slideshow", true);
        startActivity(intent);
        return true;
    }

    private final Runnable mDeletePhotoRunnable = new Runnable() {
		public void run() {
			Log.e("Runnable", "ImageGallery");
			if (!canHandleEvent())
				return;

			IImage currentImage = getCurrentImage();

			// The selection will be cleared when mGvs.stop() is called, so
			// we need to call getCurrentImage() before mGvs.stop().
			mCgv.stop();
			if (currentImage != null)
			{
				mAllImages.removeImage(currentImage);
			}
			mCgv.setImageList(mAllImages);
			mCgv.start();
			
			if (mAllImages.isEmpty())
			{
				mMainBtn.setVisibility(View.GONE);
		        mShareBtn.setVisibility(View.GONE);
		        mDeleteBtn.setVisibility(View.GONE);
			}
			mNoImagesView.setVisibility(mAllImages.isEmpty() ? View.VISIBLE
					: View.GONE);
		}
	};

    private Uri getCurrentImageUri() {
    	Log.e("getCurrentImageUri", "ImageGallery");
        IImage image = getCurrentImage();
        if (image != null) {
            return image.fullSizeImageUri();
        } else {
            return null;
        }
    }

    private IImage getCurrentImage() {
    	Log.e("getCurrentImage", "ImageGallery");
    	int currentSelection;
       	currentSelection = mCgv.getCurrentSelection();
        
        if (currentSelection < 0
                || currentSelection >= mAllImages.getCount()) {
            return null;
        } else {
            return mAllImages.getImageAt(currentSelection);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	Log.e("onConfigurationChanged", "ImageGallery");
        super.onConfigurationChanged(newConfig);
        mConfigurationChanged = true;
    }

    boolean canHandleEvent() {
    	Log.e("canHandleEvent", "ImageGallery");
        // Don't process event in pause state.
        return (!mPausing) && (mLayoutComplete);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.e("onKeyDown", "ImageGallery");
        if (!canHandleEvent()) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                IImage image = getCurrentImage();
                if (image != null) {
                    MenuHelper.deleteImage(
                            this, mDeletePhotoRunnable, getCurrentImage());
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isPickIntent() {
    	Log.e("isPickIntent", "ImageGallery");
        String action = getIntent().getAction();
        return (Intent.ACTION_PICK.equals(action)
                || Intent.ACTION_GET_CONTENT.equals(action));
    }

    private void launchCropperOrFinish(IImage img) {
    	Log.e("launchCropperOrFinish", "ImageGallery");
        Bundle myExtras = getIntent().getExtras();

        long size = MenuHelper.getImageFileSize(img);
        if (size < 0) {
            // Return if the image file is not available.
            return;
        }

        if (size > mVideoSizeLimit) {
            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.file_info_title)
                    .setMessage(R.string.video_exceed_mms_limit)
                    .setNeutralButton(R.string.details_ok, buttonListener)
                    .show();
            return;
        }

        String cropValue = myExtras != null ? myExtras.getString("crop") : null;
        if (cropValue != null) {
            Bundle newExtras = new Bundle();
            if (cropValue.equals("circle")) {
                newExtras.putString("circleCrop", "true");
            }

            Intent cropIntent = new Intent();
            cropIntent.setData(img.fullSizeImageUri());
            cropIntent.setClass(this, CropImage.class);
            cropIntent.putExtras(newExtras);

            /* pass through any extras that were passed in */
            cropIntent.putExtras(myExtras);
            startActivityForResult(cropIntent, CROP_MSG);
        } else {
            Intent result = new Intent(null, img.fullSizeImageUri());
            if (myExtras != null && myExtras.getBoolean("return-data")) {
                // The size of a transaction should be below 100K.
                Bitmap bitmap = img.fullSizeBitmap(
                        IImage.UNCONSTRAINED, 100 * 1024);
                if (bitmap != null) {
                    result.putExtra("data", bitmap);
                }
            }
            setResult(RESULT_OK, result);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
    	Log.e("onActivityResult"+requestCode, "ImageGallery");
        switch (requestCode) {
            case MenuHelper.RESULT_COMMON_MENU_CROP: {
                if (resultCode == RESULT_OK) {

                    // The CropImage activity passes back the Uri of the cropped
                    // image as the Action rather than the Data.
                    // We store this URI so we can move the selection box to it
                    // later.
                    mCropResultUri = Uri.parse(data.getAction());
                }
                break;
            }
            case CROP_MSG: {
                if (resultCode == RESULT_OK) {
                    setResult(resultCode, data);
                    finish();
                }
                break;
            }
            case IMAGE_INDEX_MSG: {
                if (resultCode == RESULT_OK) {
                    int index = data.getIntExtra("Image Index", 0);
                    mSelectedIndex = index;
                    mIsCenterIndex = true;
                }
                break;
            }
        }
    }

    @Override
    public void onPause() {
    	Log.e("onPause", "ImageGallery");
        super.onPause();
        mPausing = true;

        mLoader.stop();        
        mCgv.stop(); 

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        // Now that we've paused the threads that are using the cursor it is
        // safe to close it.
        mAllImages.close();
        mAllImages = null;
    }

    private void rebake(boolean unmounted, boolean scanning) {
    	Log.e("rebake", "ImageGallery");
    	mCgv.stop();
    	if (mAllImages != null) {
            mAllImages.close();
            mAllImages = null;
        }

        if (mMediaScanningDialog != null) {
            mMediaScanningDialog.cancel();
            mMediaScanningDialog = null;
        }

        if (scanning) {
            mMediaScanningDialog = ProgressDialog.show(
                    this,
                    null,
                    getResources().getString(R.string.wait),
                    true,
                    true);
        }

        mParam = allImages(!unmounted && !scanning);
        mAllImages = ImageManager.makeImageList(getContentResolver(), mParam);
      
		mCgv.setImageList(mAllImages);
		mCgv.setDrawAdapter(this);
		mCgv.setLoader(mLoader);
		mCgv.start();
		
		if (mAllImages.getCount() == 0)
		{
			mMainBtn.setVisibility(View.GONE);
	        mShareBtn.setVisibility(View.GONE);
	        mDeleteBtn.setVisibility(View.GONE);
		}
        mNoImagesView.setVisibility(mAllImages.getCount() > 0
                ? View.GONE
                : View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
    	Log.e("onSaveInstanceState", "ImageGallery");
        super.onSaveInstanceState(state);
        state.putFloat(STATE_SCROLL_POSITION, mScrollPosition);
        state.putInt(STATE_SELECTED_INDEX, mSelectedIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
    	Log.e("onRestoreInstanceState", "ImageGallery");
        super.onRestoreInstanceState(state);
        mScrollPosition = state.getFloat(
                STATE_SCROLL_POSITION, INVALID_POSITION);
        mSelectedIndex = state.getInt(STATE_SELECTED_INDEX, 0);
    }

    @Override
    public void onResume() {
    	Log.e("onResume", "ImageGallery");
        super.onResume();

     
		mCgv.setSizeChoice(Integer.parseInt(mPrefs.getString(
					"pref_gallery_size_key", "1")));
		mCgv.requestFocus();
		
        String sortOrder = mPrefs.getString("pref_gallery_sort_key", null);
        if (sortOrder != null) {
            mSortAscending = sortOrder.equals("ascending");
        }

        mPausing = false;

        // install an intent filter to receive SD card related events.
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                    // SD card available
                    // TODO put up a "please wait" message
                    // TODO also listen for the media scanner finished message
                } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    // SD card unavailable
                    rebake(true, false);
                } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                    rebake(false, true);
                } else if (action.equals(
                        Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                    rebake(false, false);
                } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                    rebake(true, false);
                }
            }
        };
        registerReceiver(mReceiver, intentFilter);
        rebake(false, ImageManager.isMediaScannerScanning(
                getContentResolver()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.e("onCreateOptionsMenu", "ImageGallery");
        if (isPickIntent()) {
            String type = getIntent().resolveType(this);
            if (type != null) {
                if (isImageType(type)) {
                    MenuHelper.addCapturePictureMenuItems(menu, this);
                } else if (isVideoType(type)) {
                    MenuHelper.addCaptureVideoMenuItems(menu, this);
                }
            }
        } else {
            //MenuHelper.addCaptureMenuItems(menu, this);
        	MenuHelper.captureMenuItem(menu, this);
            if ((mInclusion & ImageManager.INCLUDE_IMAGES) != 0) {
                mSlideShowItem = addSlideShowMenu(menu);
            }

            MenuItem item = menu.add(Menu.NONE, Menu.NONE,
                    5, R.string.camerasettings);
            item.setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
//                    Intent preferences = new Intent();
//                    preferences.setClass(ImageGallery.this,
//                            GallerySettings.class);
//                    startActivity(preferences);
                	//HCL Release 2
                    return true;
                }
            });
            item.setAlphabeticShortcut('p');
            item.setIcon(android.R.drawable.ic_menu_preferences);

            menu.add(Menu.NONE, Menu.NONE, 2,
			"Main").setOnMenuItemClickListener(
			new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					//TODO: Will call Main screen
					finish();//TODO
					return true;
				}
			}).setAlphabeticShortcut('p');
            
            MenuItem shareMenuItem = menu.add(Menu.NONE, Menu.NONE, 3,
    				R.string.multiselect_share).setOnMenuItemClickListener(
    				new MenuItem.OnMenuItemClickListener() {
    					public boolean onMenuItemClick(MenuItem item) {
    						//HCL Arti 					
    						return true;
    					}
    				}).setAlphabeticShortcut('p').setIcon(
    				android.R.drawable.ic_menu_share);
            MenuItem deleteMenuItem = menu.add(Menu.NONE, Menu.NONE, 4,
    				R.string.multiselect_delete).setOnMenuItemClickListener(
    				new MenuItem.OnMenuItemClickListener() {
    					public boolean onMenuItemClick(MenuItem item) {
    						//HCL Release2
//    						if (isInMultiSelectMode()) {
//    							mCancelBtn.setVisibility(View.GONE);
//    	    					mNextBtn.setVisibility(View.GONE);
//    	    					mShareTxtView.setVisibility(View.GONE);
//    	    					mDelTxtView.setVisibility(View.GONE);
//    	    					mMainBtn.setVisibility(View.VISIBLE);
//    	    			        mShareBtn.setVisibility(View.VISIBLE);
//    	    			        mDeleteBtn.setVisibility(View.VISIBLE);    			        
//    	    			        closeMultiSelectMode();
//    						}else {
//    							mMainBtn.setVisibility(View.GONE);
//            			        mShareBtn.setVisibility(View.GONE);
//            			        mDeleteBtn.setVisibility(View.GONE);
//            			    	mShareTxtView.setVisibility(View.GONE);
//            			        mDelTxtView.setVisibility(View.VISIBLE);
//            			        mCancelBtn.setVisibility(View.VISIBLE);
//            					mNextBtn.setVisibility(View.VISIBLE);
//            					openMultiSelectMode();
//    						}    			
    						return true;
    					}
    				}).setAlphabeticShortcut('p').setIcon(
    				android.R.drawable.ic_menu_delete);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Log.e("onPrepareOptionsMenu", "ImageGallery");
        if (!canHandleEvent()) return false;
        if ((mInclusion & ImageManager.INCLUDE_IMAGES) != 0) {
            boolean videoSelected = isVideoSelected();
            // TODO: Only enable slide show if there is at least one image in
            // the folder.
            if (mSlideShowItem != null) {
                mSlideShowItem.setEnabled(!videoSelected);
            }
        }

        return true;
    }

    private boolean isVideoSelected() {
    	Log.e("isVideoSelected", "ImageGallery");
        IImage image = getCurrentImage();
        return (image != null) && ImageManager.isVideo(image);
    }

    private boolean isImageType(String type) {
    	Log.e("isImageType", "ImageGallery");
        return type.equals("vnd.android.cursor.dir/image")
                || type.equals("image/*");
    }

    private boolean isVideoType(String type) {
    	Log.e("isVideoType", "ImageGallery");
        return type.equals("vnd.android.cursor.dir/video")
                || type.equals("video/*");
    }

    // According to the intent, setup what we include (image/video) in the
    // gallery and the title of the gallery.
    private void setupInclusion() {
    	Log.e("setupInclusion", "ImageGallery");
        mInclusion = ImageManager.INCLUDE_IMAGES | ImageManager.INCLUDE_VIDEOS;

        Intent intent = getIntent();
        if (intent != null) {
            String type = intent.resolveType(this);
            //TextView leftText = (TextView) findViewById(R.id.left_text);
            if (type != null) {
                if (isImageType(type)) 
                {
                    mInclusion = ImageManager.INCLUDE_IMAGES;
                }
                if (isVideoType(type))
                {
                    mInclusion = ImageManager.INCLUDE_VIDEOS;
                }
            }
            Bundle extras = intent.getExtras();

            if (extras != null) {
                mInclusion = (ImageManager.INCLUDE_IMAGES
                        | ImageManager.INCLUDE_VIDEOS)
                        & extras.getInt("mediaTypes", mInclusion);
            }

            if (extras != null && extras.getBoolean("pick-drm")) {
                Log.d(TAG, "pick-drm is true");
                mInclusion = ImageManager.INCLUDE_DRM_IMAGES;
            }
        }
    }

    // Returns the image list parameter which contains the subset of image/video
    // we want.
    private ImageManager.ImageListParam allImages(boolean storageAvailable) {
    	Log.e("allImages", "ImageGallery");
    	ImageManager.ImageListParam list = null;
        if (!storageAvailable) {
            list = ImageManager.getEmptyImageListParam();
        }
        else
        {
        	
        	int activity = getIntent().getIntExtra("activity", 0);
        	Log.e("allImages"+activity, "ImageGallery");
        	switch(activity)
        	{
        		case ThumbCallingActivity.ViewByDate:
	        	{
	        		//To be given by calling activity
	                  long t_from = 1000000000000L;
	                  long t_to = 1200000000000L;
	                  String imgClause =
	                      "(" + MediaStore.Images.Media.DATE_TAKEN + " BETWEEN " + t_from + " AND " + t_to + ")";
	                  String vidClause =
	                      "(" + MediaStore.Video.Media.DATE_TAKEN + " BETWEEN " + t_from + " AND " + t_to + ")";
	                  ImageList.setWhereClause(imgClause);
	                  VideoList.setWhereClause(vidClause);
	                  
	                  Log.e(imgClause, "ImageGallery");
	                  Uri uri = getIntent().getData();
		                 list  = ImageManager.getImageListParam(
		                         ImageManager.DataLocation.EXTERNAL,
		                         mInclusion,
		                         mSortAscending
		                         ? ImageManager.SORT_ASCENDING
		                         : ImageManager.SORT_DESCENDING,
		                         null);
//	                  String selectionVid = MediaStore.Video.VideoColumns.DATE_TAKEN + 
//						" BETWEEN " + t_from + " AND " + t_to;
	        		
	        		//TODO
	        	}break;
        		case ThumbCallingActivity.Folder:
        		case ThumbCallingActivity.Album:
	        	{
	        		 Uri uri = getIntent().getData();
	                 list  = ImageManager.getImageListParam(
	                         ImageManager.DataLocation.EXTERNAL,
	                         mInclusion,
	                         mSortAscending
	                         ? ImageManager.SORT_ASCENDING
	                         : ImageManager.SORT_DESCENDING,
	                         (uri != null)
	                         ? uri.getQueryParameter("bucketId")
	                         : null);
	            }break;
        	}
        	
        }
        return list;
    }

    private void toggleMultiSelected(IImage image) {
    	Log.e("toggleMultiSelected", "ImageGallery");
        int original = mMultiSelected.size();
        if (!mMultiSelected.add(image)) {
            mMultiSelected.remove(image);
        }
		mCgv.invalidate();
		
        //smita
//        if (original == 0) showFooter();
//        if (mMultiSelected.size() == 0) hideFooter();
    }

    public void onImageClicked(int index) {
    	Log.e("onImageClicked", "ImageGallery");
        if (index < 0 || index >= mAllImages.getCount()) {
            return;
        }
        mSelectedIndex = index;
       	mCgv.setSelectedIndex(index, mIsCenterIndex);
    	//mCgv.setSelectedIndex(index);
       
        IImage image = mAllImages.getImageAt(index);

        if (isInMultiSelectMode()) {
            toggleMultiSelected(image);
            return;
        }

        if (isPickIntent()) {
            launchCropperOrFinish(image);
        } else {
            Intent intent;
            if (image instanceof VideoObject) {
                intent = new Intent(
                        Intent.ACTION_VIEW, image.fullSizeImageUri());
                intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                intent = new Intent(this, ViewImage.class);
                intent.putExtra(ViewImage.KEY_IMAGE_LIST, mParam);
                intent.setData(image.fullSizeImageUri());
            }
            startActivityForResult(intent, IMAGE_INDEX_MSG);
        }
    }

    public void onImageTapped(int index) {
    	Log.e("onImageTapped", "ImageGallery");
        // In the multiselect mode, once the finger finishes tapping, we hide
        // the selection box by setting the selected index to none. However, if
        // we use the dpad center key, we will keep the selected index in order
        // to show the the selection box. We do this because we have the
        // multiselect marker on the images to indicate which of them are
        // selected, so we don't need the selection box, but in the dpad case
        // we still need the selection box to show as a "cursor".

        if (isInMultiSelectMode())
        {
        	mCgv.setSelectedIndex(GridViewL.INDEX_NONE, mIsCenterIndex);
            toggleMultiSelected(mAllImages.getImageAt(index));
        } else {
            onImageClicked(index);
        }
    }

    private class CreateContextMenuListener implements
            View.OnCreateContextMenuListener {
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenu.ContextMenuInfo menuInfo) {
        	Log.e("CreateContextMenuListener", "CreateContextMenuListener");
            if (!canHandleEvent()) return;

            IImage image = getCurrentImage();

            if (image == null) {
                return;
            }

            boolean isImage = ImageManager.isImage(image);
            if (isImage) {
                menu.add(R.string.view)
                        .setOnMenuItemClickListener(
                        new MenuItem.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                if (!canHandleEvent()) return false;
                                //HCL Release2
                                //onImageClicked(mCgv.getCurrentSelection());//should be enabled after Release2 
                                return true;
                            }
                        });
            }
            int sideButtons = 0;
            if (image instanceof VideoObject) {
            	sideButtons =  MenuHelper.INCLUDE_SHOWMAP_MENU | MenuHelper.INCLUDE_ROTATE_MENU |
            				   MenuHelper.INCLUDE_SET_MENU ;
            }
            menu.setHeaderTitle(isImage
                    ? R.string.context_menu_header
                    : R.string.video_context_menu_header);
            if ((mInclusion & (ImageManager.INCLUDE_IMAGES
                    | ImageManager.INCLUDE_VIDEOS)) != 0) {
                MenuHelper.MenuItemsResult r = MenuHelper.addImageMenuItems(
                        menu,
                        MenuHelper.INCLUDE_ALL & ~sideButtons,
                        Thumbnail.this,
                        mHandler,
                        mDeletePhotoRunnable,
                        new MenuHelper.MenuInvoker() {
                            public void run(MenuHelper.MenuCallback cb) {
                                if (!canHandleEvent()) {
                                    return;
                                }
                                cb.run(getCurrentImageUri(), getCurrentImage());
                               	mCgv.invalidateImage(mCgv.getCurrentSelection());                  	
                            }
                        });

                if (r != null) {
                    r.gettingReadyToOpen(menu, image);
                }

                if (isImage) {
                    MenuHelper.enableShowOnMapMenuItem(
                            menu, MenuHelper.hasLatLngData(image));
                    //addSlideShowMenu(menu);//smita
                }
            }
        }
    }

    public void onLayoutComplete(boolean changed) {
    	Log.e("onLayoutComplete", "ImageGallery");
		mLayoutComplete = true;
		if (mCropResultUri != null) {
			IImage image = mAllImages.getImageForUri(mCropResultUri);
			mCropResultUri = null;
			if (image != null) {
				mSelectedIndex = mAllImages.getImageIndex(image);
			}
		}
		 
		mCgv.setSelectedIndex(mSelectedIndex, mIsCenterIndex);
			mIsCenterIndex = false;
			if (mScrollPosition == INVALID_POSITION) {
				if (mSortAscending) {
					mCgv.setSelectedIndex(mAllImages.getCount(), mIsCenterIndex);
					mCgv.scrollToImage((mAllImages.getCount()));
				} else {
					mCgv.scrollToImage(0);
				}
			} else if (mConfigurationChanged) {
				mConfigurationChanged = false;
				mCgv.scrollTo(mScrollPosition);
				if (mCgv.getCurrentSelection() != GridViewL.INDEX_NONE) {
					mCgv.scrollToVisible(mSelectedIndex);
				}
			} else {
				mCgv.scrollTo(mScrollPosition);
			}		
	}

    public void onScroll(float scrollPosition) {
    	Log.e("onScroll", "ImageGallery");
        mScrollPosition = scrollPosition;
    }

    private Drawable mVideoOverlay;
    private Drawable mVideoMmsErrorOverlay;
    private Drawable mMultiSelectTrue;
    private Drawable mMultiSelectFalse;

    // mSrcRect and mDstRect are only used in drawImage, but we put them as
    // instance variables to reduce the memory allocation overhead because
    // drawImage() is called a lot.
    private final Rect mSrcRect = new Rect();
    private final Rect mDstRect = new Rect();

    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    public void drawImage(Canvas canvas, IImage image,
            Bitmap b, int xPos, int yPos, int w, int h) {
    	Log.e("drawImage", "ImageGallery");
        if (b != null) {
            // if the image is close to the target size then crop,
            // otherwise scale both the bitmap and the view should be
            // square but I suppose that could change in the future.

            int bw = b.getWidth();
            int bh = b.getHeight();

            int deltaW = bw - w;
            int deltaH = bh - h;

            if (deltaW >= 0 && deltaW < 10 &&
                deltaH >= 0 && deltaH < 10) {
                int halfDeltaW = deltaW / 2;
                int halfDeltaH = deltaH / 2;
                mSrcRect.set(0 + halfDeltaW, 0 + halfDeltaH,
                        bw - halfDeltaW, bh - halfDeltaH);
                mDstRect.set(xPos, yPos, xPos + w, yPos + h);
                canvas.drawBitmap(b, mSrcRect, mDstRect, null);
            } else {
                mSrcRect.set(0, 0, bw, bh);
                mDstRect.set(xPos, yPos, xPos + w, yPos + h);
                canvas.drawBitmap(b, mSrcRect, mDstRect, mPaint);
            }
        } else {
            // If the thumbnail cannot be drawn, put up an error icon
            // instead
            Bitmap error = getErrorBitmap(image);
            int width = error.getWidth();
            int height = error.getHeight();
            mSrcRect.set(0, 0, width, height);
            int left = (w - width) / 2 + xPos;
            int top = (w - height) / 2 + yPos;
            mDstRect.set(left, top, left + width, top + height);
            canvas.drawBitmap(error, mSrcRect, mDstRect, null);
        }

        if (ImageManager.isVideo(image)) {
            Drawable overlay = null;
            long size = MenuHelper.getImageFileSize(image);
            if (size >= 0 && size <= mVideoSizeLimit) {
                if (mVideoOverlay == null) {
                    mVideoOverlay = getResources().getDrawable(
                            R.drawable.ic_gallery_video_overlay);
                }
                overlay = mVideoOverlay;
            } else {
                if (mVideoMmsErrorOverlay == null) {
                    mVideoMmsErrorOverlay = getResources().getDrawable(
                            R.drawable.ic_error_mms_video_overlay);
                }
                overlay = mVideoMmsErrorOverlay;
                Paint paint = new Paint();
                paint.setARGB(0x80, 0x00, 0x00, 0x00);
                canvas.drawRect(xPos, yPos, xPos + w, yPos + h, paint);
            }
            int width = overlay.getIntrinsicWidth();
            int height = overlay.getIntrinsicHeight();
            int left = xPos;//(w - width) / 2 + xPos;
            int top = yPos;//(h - height) / 2 + yPos;
            mSrcRect.set(left, top, left + width, top + height);
            overlay.setBounds(mSrcRect);
            overlay.draw(canvas);
        }
    }

    public boolean needsDecoration() {
    	Log.e("needsDecoration", "ImageGallery");
        return (mMultiSelected != null);
    }

    public void drawDecoration(Canvas canvas, IImage image,
            int xPos, int yPos, int w, int h) {
    	Log.e("drawDecoration", "ImageGallery");
        if (mMultiSelected != null) {
            initializeMultiSelectDrawables();

            Drawable checkBox = mMultiSelected.contains(image)
                    ? mMultiSelectTrue
                    : mMultiSelectFalse;
            int width = checkBox.getIntrinsicWidth();
            int height = checkBox.getIntrinsicHeight();
            int left = xPos + w - width - 5;
            int top = yPos + 5;
            mSrcRect.set(left, top, left + width, top + height);
            checkBox.setBounds(mSrcRect);
            checkBox.draw(canvas);
        }
    }

    private void initializeMultiSelectDrawables() {
    	Log.e("initializeMultiSelectDrawables", "ImageGallery");
        if (mMultiSelectTrue == null) {
            mMultiSelectTrue = getResources()
                    .getDrawable(R.drawable.btn_check_buttonless_on);
        }
        if (mMultiSelectFalse == null) {
            mMultiSelectFalse = getResources()
                    .getDrawable(R.drawable.btn_check_buttonless_off);
        }
    }

    private Bitmap mMissingImageThumbnailBitmap;
    private Bitmap mMissingVideoThumbnailBitmap;

    // Create this bitmap lazily, and only once for all the ImageBlocks to
    // use
    public Bitmap getErrorBitmap(IImage image) {
    	Log.e("getErrorBitmap", "ImageGallery");
        if (ImageManager.isImage(image)) {
            if (mMissingImageThumbnailBitmap == null) {
                mMissingImageThumbnailBitmap = BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }
            return mMissingImageThumbnailBitmap;
        } else {
            if (mMissingVideoThumbnailBitmap == null) {
                mMissingVideoThumbnailBitmap = BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_missing_thumbnail_video);
            }
            return mMissingVideoThumbnailBitmap;
        }
    }

    private Animation mFooterAppear;
    private Animation mFooterDisappear;

    private void showFooter() {
    	Log.e("showFooter", "ImageGallery");
        mFooterOrganizeView.setVisibility(View.VISIBLE);
        if (mFooterAppear == null) {
            mFooterAppear = AnimationUtils.loadAnimation(
                    this, R.anim.footer_appear);
        }
        mFooterOrganizeView.startAnimation(mFooterAppear);
    }

    private void hideFooter() {
    	Log.e("hideFooter", "ImageGallery");
        if (mFooterOrganizeView.getVisibility() != View.GONE) {
            mFooterOrganizeView.setVisibility(View.GONE);
            if (mFooterDisappear == null) {
                mFooterDisappear = AnimationUtils.loadAnimation(
                        this, R.anim.footer_disappear);
            }
            mFooterOrganizeView.startAnimation(mFooterDisappear);
        }
    }

    private String getShareMultipleMimeType() {
    	Log.e("getShareMultipleMimeType", "ImageGallery");
        final int FLAG_IMAGE = 1, FLAG_VIDEO = 2;
        int flag = 0;
        for (IImage image : mMultiSelected) {
            flag |= ImageManager.isImage(image) ? FLAG_IMAGE : FLAG_VIDEO;
        }
        return flag == FLAG_IMAGE
                ? "image/*"
                : flag == FLAG_VIDEO ? "video/*" : "*/*";
    }

    private void onShareMultipleClicked() {
    	Log.e("onShareMultipleClicked", "ImageGallery");
        if (mMultiSelected.size() > 1) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);

            String mimeType = getShareMultipleMimeType();
            intent.setType(mimeType);
            ArrayList<Parcelable> list = new ArrayList<Parcelable>();
            for (IImage image : mMultiSelected) {
                list.add(image.fullSizeImageUri());
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, list);
            try {
                startActivity(Intent.createChooser(
                        intent, getText(R.string.send_media_files)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, R.string.no_way_to_share,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (mMultiSelected.size() == 1) {
            IImage image = mMultiSelected.iterator().next();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            String mimeType = image.getMimeType();
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, image.fullSizeImageUri());
            boolean isImage = ImageManager.isImage(image);
            try {
                startActivity(Intent.createChooser(intent, getText(
                        isImage ? R.string.sendImage : R.string.sendVideo)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, isImage
                        ? R.string.no_way_to_share_image
                        : R.string.no_way_to_share_video,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onDeleteMultipleClicked() {
    	Log.e("onDeleteMultipleClicked", "ImageGallery");
        Runnable action = new Runnable() {
            public void run() {
                ArrayList<Uri> uriList = new ArrayList<Uri>();
                for (IImage image : mMultiSelected) {
                    uriList.add(image.fullSizeImageUri());
                }
                closeMultiSelectMode();
                Intent intent = new Intent(Thumbnail.this,
                        DeleteImage.class);
                intent.putExtra("delete-uris", uriList);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.e(TAG, "Delete images fail", ex);
                }
            }
        };
        MenuHelper.deleteMultiple(this, action);
    }

    private boolean isInMultiSelectMode() {
    	Log.e("isInMultiSelectMode", "ImageGallery");
        return mMultiSelected != null;
    }

    private void closeMultiSelectMode() {
    	Log.e("closeMultiSelectMode", "ImageGallery");
        if (mMultiSelected == null) return;
        mMultiSelected = null;
        mCgv.invalidate();   
        mCgv.scrollTo(mScrollPosition);
        //hideFooter();//smita
    }

    private void openMultiSelectMode() {
    	Log.e("openMultiSelectMode", "ImageGallery");
        if (mMultiSelected != null) return;
        mMultiSelected = new HashSet<IImage>();
       	mCgv.invalidate();
       	mCgv.scrollTo(mScrollPosition);
    }


	public void onProgressChanged(ThumbScroller scroller, int progress) {
		// TODO Auto-generated method stub
		if (setThumb) {
			setThumb = false;
			return;
		}
		Log.e("onProgressChanged","onProgressChanged + progress=" + progress);
		//mToast.makeText(getApplicationContext(), "Aug,2010",Toast.LENGTH_SHORT).show();
			mCgv.trackThumbScroll(progress-curr);
			curr = progress;
	}


	public void onStartTrackingTouch(ThumbScroller scroller) {
		// TODO Auto-generated method stub
		Log.e("onStartTrackingTouch","ImageGallery");
	}


	public void onStopTrackingTouch(ThumbScroller scroller) {
		// TODO Auto-generated method stub
		Log.e("onStopTrackingTouch","ImageGallery");	
	}

	public void setThumbPosition(int progress) {
		// TODO Auto-generated method stub
		Log.e("setThumbPosition","progress="+progress);
		setThumb = true;
        mSeekBar.setProgress(progress);
	}

}
