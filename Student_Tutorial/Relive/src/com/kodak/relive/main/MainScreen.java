package com.kodak.relive.main;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView.ScaleType;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.kodak.relive.common.ThumbCallingActivity;
import com.kodak.gallery.camera.BitmapManager;
import com.kodak.gallery.camera.GallerySettings;
import com.kodak.gallery.camera.ImageManager;
import com.kodak.gallery.camera.MenuHelper;
import com.kodak.gallery.camera.Util;
import com.kodak.gallery.camera.ViewImage;
import com.kodak.gallery.camera.gallery.IImage;
import com.kodak.gallery.camera.gallery.IImageList;
import com.kodak.relive.R;
import com.kodak.relive.thumbnail.Thumbnail;
import com.kodak.relive.viewbydate.ViewByDate;
/**
 * The GalleryPicker activity.
 */
public class MainScreen extends Activity {
    private static final String TAG = "GalleryPicker";
    private static final String SDCARD_DCIM = "/sdcard/DCIM";
    private static final String SDCARD_LOST_DIR = "/sdcard/LOST.DIR";    
    
    Handler mHandler = new Handler();  // handler for the main thread
    Thread mWorkerThread;
    BroadcastReceiver mReceiver;
    ContentObserver mDbObserver;
    GridView mGridView;
    boolean mScanning;
    boolean mUnmounted;

	static HashMap<String, String> mRecentImageMap;
    static HashMap<String, String> mDateImageMap;
    static ContentResolver mCr;
    static int noOfPages; 
    static int noOfItems;
    static Vector<String> foldersVec;
    private int mCurrentShowingPage;
    private int currentVecIndex = 1;
    private View mNoImagesView;
    private long recentImage_dateTaken = 0;
	private int id = 0;
	private static final String people = "People";
	private static final String location = "Locations";
	private static final String keyword = "Keywords";
	private static final String album = "Albums";
	private static final String folder = "Folders";
	private GestureDetector mGestureDetector;
	private ViewFlipper viewFlipper;
	private Button PagingBar[];

	private void cleanup() {
		id = 0;
		noOfItems = 0;
		noOfPages = 0;
		foldersVec = null;
		mCurrentShowingPage = 0;
		currentVecIndex = 1;
		mRecentImageMap = null;
	    mDateImageMap = null;
	    recentImage_dateTaken = 0;
	    PagingBar = null;
	    viewFlipper = null;
	    mGestureDetector = null;
	}
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // start Prashant
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		cleanup();
		mCr = getContentResolver();
        mRecentImageMap = GetRecentImageOrVideoPath(getContentResolver());
        mDateImageMap = GetDateImageOrVideoPath(getContentResolver());
        foldersVec = checkFolders();
        Log.i("foldersVec:: ", "foldersVec:: "+foldersVec);
        Log.i("mRecentImageMap", "mRecentImageMap:: "+mRecentImageMap.size());
        Log.i("mDateImageMap", "mDateImageMap:: "+mDateImageMap.size());
        noOfItems = mRecentImageMap.size() + mDateImageMap.size() + foldersVec.size();
        noOfPages = noOfItems / 3;
        if ((noOfItems % 3) > 0) {
        	noOfPages = noOfPages + 1;
        }
        Log.i("noOfPages", "noOfPages:: "+noOfPages);
        View view = null;
        
        
        if (noOfPages == 0) {
        	showNoImagesView();
        } else if (noOfPages == 1) {
        	setContentView(R.layout.main_single_page);
        	view = findViewById(R.id.rootViewWithoutPaging);
        	GridView gridView1 = (GridView) findViewById(R.id.GridView01);
        	ImageView recentImage = (ImageView) findViewById(R.id.recentImageView);
        	ImageView dateImage = (ImageView) findViewById(R.id.dateImageView);
        	changeFirstPageView(recentImage, dateImage, gridView1);
        
        } else if (noOfPages > 1 && noOfPages <= 5) {
        	//TODO: to handle noOfPages > 5
        	setContentView(R.layout.main_paging_bar);
        	view = findViewById(R.id.rootViewWithPaging);
        	ImageView recentView = (ImageView) findViewById(R.id.recentImageView);
        	ImageView dateView = (ImageView) findViewById(R.id.dateImageView);
        	GridView grid1 = (GridView) findViewById(R.id.GridView01);
        	
        	changeFirstPageView(recentView, dateView, grid1);
        	
        	viewFlipper = (ViewFlipper) findViewById(R.id.PageBarViewFlipper);
			viewFlipper.setAnimateFirstView(true);
			
			for (int i = 1; i < noOfPages; i++) {
				viewFlipper.addView(createPage());
			}
			manipulatePagingBar();
			if (view != null) {
//	        	setupOnTouchListeners(view);
	        }
		}
        // end Prashant
        
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				onReceiveMediaBroadcast(intent);
			}
		};

        mDbObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
            	Log.d("MainScreen", "inside ContentObserver->onChange()");
            	rebake(false, ImageManager.isMediaScannerScanning(
                        getContentResolver()));
            }
        };

        ImageManager.ensureOSXCompatibleFolder();
        Log.i("view::  ", "view::  "+view);
    }
    
    private void manipulatePagingBar() {
    	PagingBar = new Button[5];
    	PagingBar[0] = (Button) findViewById(R.id.page1);
		PagingBar[1] = (Button) findViewById(R.id.page2);
		PagingBar[2] = (Button) findViewById(R.id.page3);
		PagingBar[3] = (Button) findViewById(R.id.page4);
		PagingBar[4] = (Button) findViewById(R.id.page5);

		switch (noOfPages) {
		case 1:
			PagingBar[0].setVisibility(Button.GONE);
			PagingBar[1].setVisibility(Button.GONE);
			PagingBar[2].setVisibility(Button.GONE);
			PagingBar[3].setVisibility(Button.GONE);
			PagingBar[4].setVisibility(Button.GONE);
			break;
		case 2:
			PagingBar[2].setVisibility(Button.GONE);
			PagingBar[3].setVisibility(Button.GONE);
			PagingBar[4].setVisibility(Button.GONE);
			break;
		case 3:
			PagingBar[3].setVisibility(Button.GONE);
			PagingBar[4].setVisibility(Button.GONE);
			break;
		case 4:
			PagingBar[4].setVisibility(Button.GONE);
			break;
		case 5:
			break;
		}
		
		PagingBar[0].setBackgroundColor(Color.WHITE);			
		for (int i = 0; i < noOfPages; i++) {
			final int idx = i;
			PagingBar[i].setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.setFocusable(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						showNextPage(idx);
					}
						return true;
				}
			});
		}

	}
    
    
    public void showNextPage(int index) {
    	if (mCurrentShowingPage < index) {
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_in));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_out));
			PagingBar[index].setBackgroundColor(Color.WHITE);
			PagingBar[mCurrentShowingPage].setBackgroundColor(Color.GRAY);
			mCurrentShowingPage = index;
			viewFlipper.setDisplayedChild(index);
		} else if (mCurrentShowingPage > index) {
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_in));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_out));
			PagingBar[index].setBackgroundColor(Color.WHITE);
			PagingBar[mCurrentShowingPage].setBackgroundColor(Color.GRAY);
			mCurrentShowingPage = index;
			viewFlipper.setDisplayedChild(index);
		} else {
			viewFlipper.clearAnimation();
		}
	}
    
    // Show "no images" icon and text. Load resources on demand.
    private void showNoImagesView() {
    	setContentView(R.layout.gallerypicker_no_images);
    	RelativeLayout noImage = (RelativeLayout)findViewById(R.id.no_images);
    	noImage.setVisibility(View.VISIBLE);
    }
    
    private void changeFirstPageView(ImageView recent, ImageView date, GridView grid) {

		if (mRecentImageMap != null && mRecentImageMap.size() > 0) {
			
			recent.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//Done temporarily for testing
//					Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
//			        
//			        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//			        intent.putExtra("activity", com.kodak.relive.common.ThumbCallingActivity.Folder);
//			        intent.setClass(MainScreen.this, Thumbnail.class);
//			        startActivity(intent);					
				}
			});
			TextView recentTextView = (TextView) findViewById(R.id.recentTextView); 
			recentTextView.setVisibility(View.VISIBLE);
			String imageid = (String)  mRecentImageMap.keySet().iterator().next();
			String ImgVid =  mRecentImageMap.get(imageid);
	    	if((ImgVid.compareTo("I") == 0)) {
	    		recent.setImageBitmap(MediaStore.Images.Thumbnails
	    				.getThumbnail(MainScreen.mCr, Integer.parseInt(imageid),
	    						MediaStore.Images.Thumbnails.MINI_KIND, null));
	    	} else {
	    		recent.setImageBitmap(MediaStore.Video.Thumbnails.
	    				getThumbnail(MainScreen.mCr, Integer.parseInt(imageid),
	    						MediaStore.Video.Thumbnails.MICRO_KIND, null));
	    	}
		} else {
			TextView recentTextView = (TextView) findViewById(R.id.recentTextView); 
			recentTextView.setVisibility(View.GONE);
		}
		if (mDateImageMap != null && mDateImageMap.size() > 0) {
			date.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					launchViewByDateActivity();
				}
			});
			TextView dateTextView = (TextView) findViewById(R.id.dateTextView); 
			dateTextView.setVisibility(View.VISIBLE);
			String imageid = (String)  mDateImageMap.keySet().iterator().next();
			String ImgVid =  mDateImageMap.get(imageid);
			if((ImgVid.compareTo("I") == 0)) {
	    		date.setImageBitmap(MediaStore.Images.Thumbnails.getThumbnail
	    				(MainScreen.mCr, Integer.parseInt(imageid),
	    						MediaStore.Images.Thumbnails.MINI_KIND, null));
	    	} else {
	    		date.setImageBitmap(MediaStore.Video.Thumbnails.
	    				getThumbnail(MainScreen.mCr, Integer.parseInt(imageid),
	    						MediaStore.Video.Thumbnails.MICRO_KIND, null));
	    	}
	    	TextView yearTextView = (TextView) findViewById(R.id.yearTextView);
	    	yearTextView.setText(getYear());
		} else {
			TextView dateTextView = (TextView) findViewById(R.id.dateTextView); 
			dateTextView.setVisibility(View.GONE);
		}
		if (foldersVec != null && foldersVec.size() > 0) {
    		String folderName = foldersVec.elementAt(0);
    		grid.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					return true;
				}
			});
    		if (folderName.equals(folder)) {
    			Cursor cursor = getFolderCursor();
        		if (cursor != null && cursor.getCount() > 0) {
        			ImageView imageView = (ImageView) findViewById(R.id.locationImageView);
        			imageView.setVisibility(View.GONE);
        			TextView textView = (TextView) findViewById(R.id.gridTextView);
        			textView.setVisibility(View.VISIBLE);
            		textView.setText(folder);
            		grid.setVisibility(View.VISIBLE);
            		cursor.moveToFirst();
            		grid.setAdapter(new ImageAdapter(getApplicationContext(), cursor));
        		} else {
        			TextView textView = (TextView) findViewById(R.id.gridTextView);
            		textView.setVisibility(View.GONE);
            		grid.setVisibility(View.GONE);
        		}
        	} else if (folderName.equals(location)) {
        		RelativeLayout layout = (RelativeLayout) findViewById(R.id.RelativeLayout01);
        		TextView textView = (TextView) findViewById(R.id.gridTextView);
        		textView.setVisibility(View.GONE);
        		textView = new TextView(this);
        		textView.setText(location);
        		grid.setVisibility(View.GONE);
        		ImageView image = (ImageView) findViewById(R.id.locationImageView);
        		image.setVisibility(View.VISIBLE);
        		image.setImageResource(R.drawable.main_locations);
        		RelativeLayout.LayoutParams textViewLayoutParam = new RelativeLayout.
        		LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        		textViewLayoutParam.addRule(RelativeLayout.BELOW, image.getId());
        		TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        		textViewLayoutParam.addRule(RelativeLayout.RIGHT_OF, dateTextView.getId());
        		textView.setPadding(40, 10, 0, 0);
        		textView.setTextSize(20);
        		layout.addView(textView, textViewLayoutParam);
        		
        	} else {
        		ImageView imageView = (ImageView) findViewById(R.id.locationImageView);
        		imageView.setVisibility(View.GONE);
    			String filePath = Environment.getExternalStorageDirectory().toString() + "/" + folderName;
            	File file = new File(filePath);
    			if (file != null && file.isDirectory()) {
    				String bucketId = getBucketId(filePath);
    				String where = "bucket_id = '"+bucketId+"'" ;
    		    	Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"_id"},
    		    					where, null," datetaken DESC limit 4");
    				cursor.moveToFirst();
    				TextView textView = (TextView) findViewById(R.id.gridTextView);
    	    		if (cursor.getCount() > 0) {
    					grid.setVisibility(View.VISIBLE);
    					ImageView image = (ImageView) findViewById(R.id.locationImageView);
    	        		image.setVisibility(View.GONE);
        				textView.setVisibility(View.VISIBLE);
        	    		textView.setText(folderName);
        				grid.setAdapter(new ImageAdapter(getApplicationContext(), cursor));
    				} else {
    		    		textView.setVisibility(View.GONE);
    		    		grid.setVisibility(View.GONE);
    				}
    			} else {
        			TextView textView = (TextView) findViewById(R.id.gridTextView);
            		textView.setVisibility(View.GONE);
            		grid.setVisibility(View.GONE);
            		ImageView image = (ImageView) findViewById(R.id.locationImageView);
	        		image.setVisibility(View.GONE);
        		}
        	} 
		} else {
			TextView textView = (TextView) findViewById(R.id.gridTextView);
    		textView.setVisibility(View.GONE);
    		grid.setVisibility(View.GONE);
    		ImageView image = (ImageView) findViewById(R.id.locationImageView);
    		image.setVisibility(View.GONE);
		}
    }
    
    private String getYear() {
    	String year = "";
    	if (recentImage_dateTaken > 0) {
    		Calendar cal = Calendar.getInstance();
    		cal.setTime(new Date(recentImage_dateTaken));
    		int yy = cal.get(Calendar.YEAR);
    		year = String.valueOf(yy);
    	}
    	return year;
    }
    
    
 // Add dynamic
	public RelativeLayout createPage() {
		RelativeLayout relativeLayout = new RelativeLayout(getApplicationContext());
		relativeLayout.setId(id++);
		
		LayoutParams relativeLayoutParameter = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		relativeLayout.setLayoutParams(relativeLayoutParameter);
		Vector<View> viewVec = new Vector<View>();
		Vector<TextView> textViewVec = new Vector<TextView>();
		
		for (int j = 0; currentVecIndex < foldersVec.size() && j < 3; j++) {
			String folderName = foldersVec.get(currentVecIndex);
			File file = null;
			String filePath = null;
			if (!folderName.equals(folder)) {
				filePath = Environment.getExternalStorageDirectory().toString() + "/" + folderName;
				file = new File(filePath);
			}
			if (folderName.equals(folder) ||(file != null && file.isDirectory())) {
				if (folderName.equals(location)) {
					relativeLayout = getImageViewLayout(textViewVec, viewVec, relativeLayout, filePath);
				} else {
					relativeLayout = getGridViewLayout(textViewVec, viewVec, relativeLayout, filePath);
				}
			} 
			currentVecIndex++;
		}
				
		return relativeLayout;
	}
	
	private RelativeLayout getTextViewLayout( Vector<TextView> textViewVec, Vector<View> viewVec,
			RelativeLayout relativeLayout, String folderName) {
		try {
		TextView textView = new TextView(this);
		textView.setId(id++);
		textView.setText(folderName);
		textView.setVisibility(View.VISIBLE);
		RelativeLayout.LayoutParams textLayoutParam = new RelativeLayout.LayoutParams(142, RelativeLayout.LayoutParams.WRAP_CONTENT);
		textLayoutParam.addRule(RelativeLayout.BELOW, viewVec.lastElement().getId());
		if (textViewVec.size()> 0) {
			textLayoutParam.addRule(RelativeLayout.RIGHT_OF, textViewVec.lastElement().getId());
		}
		Log.i("textViewVec:: ", "textViewVec size:: "+textViewVec.size());
		if (textViewVec.size() == 0) {
			textView.setPadding(14, 10, 0, 0);
		} else if (textViewVec.size() == 1) {
			textView.setPadding(27, 10, 0, 0);
		} else {
			textView.setPadding(40, 10, 0, 0);
		}
		textView.setTextSize(20);
		textViewVec.add(textView);
		relativeLayout.addView(textView, textLayoutParam);
		} catch (Exception e) {
			Log.i("Exception::  ", "Exception 1 :: "+e);
		}
		return relativeLayout;
	}
	
	private RelativeLayout getGridViewLayout(Vector<TextView> textViewVec,Vector<View> viewVec,
			RelativeLayout relativeLayout, String filePath) {
		try {
		GridView grid1 = new GridView(this);
		grid1.setSelector(R.drawable.viewbydate_selector);
		grid1.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		RelativeLayout.LayoutParams gridLayoutParam = new RelativeLayout.LayoutParams(155, 142);
		
		if (viewVec.size() > 0) {
			gridLayoutParam.addRule(RelativeLayout.RIGHT_OF, viewVec.lastElement().getId());
		}
		grid1.setId(id++);
		grid1.setVisibility(GridView.VISIBLE);
		grid1.setPadding(14, 0, 0, 0);
		grid1.setNumColumns(2);
		grid1.setVisibility(View.VISIBLE);
		Cursor cursor = null;
		if (filePath != null) {
			String bucketId = getBucketId(filePath);
			String where = "bucket_id = '"+bucketId+"'" ;
			cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"_id"},
					where, null, " datetaken DESC limit 4");
		} else {
			cursor = getFolderCursor();
			Log.i("Folder cursor::  ", "cursor length:: "+cursor.getCount());
		}
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			relativeLayout.addView(grid1,gridLayoutParam);
			grid1.setAdapter(new ImageAdapter(getApplicationContext(), cursor));
			viewVec.add(grid1);
			String folderName = "";
			if (filePath != null) {
				folderName = filePath.substring(filePath.lastIndexOf('/')+1);
			} else {
				folderName = folder;
			}
			relativeLayout = getTextViewLayout(textViewVec, viewVec, relativeLayout, folderName);
		}
		} catch (Exception e) {
			Log.i("Exception::  ", "Exception 2 :: "+e);
		}
		return relativeLayout;
	}
	
	private RelativeLayout getImageViewLayout(Vector<TextView> textViewVec, Vector<View> viewVec,
			RelativeLayout relativeLayout, String filePath) {
		try {
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(R.drawable.main_locations);
			imageView.setId(id++);
			imageView.setVisibility(GridView.VISIBLE);
			imageView.setPadding(14, 0, 0, 0);
			RelativeLayout.LayoutParams imageViewLayoutParam = new RelativeLayout.LayoutParams(
					142, 142);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			int count = relativeLayout.getChildCount();
			if (count == 0) {
				relativeLayout.setPadding(14, 0, 0, 0);
			}
			if (viewVec.size() > 0) {
				imageViewLayoutParam.addRule(RelativeLayout.RIGHT_OF, viewVec
						.lastElement().getId());
			}
			viewVec.add(imageView);
			relativeLayout.addView(imageView, imageViewLayoutParam);
			String folderName = filePath
					.substring(filePath.lastIndexOf('/') + 1);
			relativeLayout = getTextViewLayout(textViewVec, viewVec,
					relativeLayout, folderName);

		} catch (Exception e) {
			Log.i("Exception::  ", "Exception 3:: " + e);
		}
		return relativeLayout;
	}
	
	private boolean isFolderDataPresent() {
		boolean isImage = false;
		String[] projection = new String[] {
				"DISTINCT(" + MediaStore.Images.ImageColumns.BUCKET_ID + ")"
				};
		String where = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + 
			" IN (select " + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + 
			" from images where " + MediaStore.Images.ImageColumns.DATA + 
			" not like ('" + SDCARD_DCIM + "%' and '" + SDCARD_LOST_DIR + "%'))";
		Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, where, null, null);
        if (cursor != null) {
        	if (cursor.getCount() > 0) {
        		isImage = true;
        	}
        	cursor.close();
        }
		return isImage;
	}
	
    	private String bucketId() {
		try {
	        int bucketId = 0;
	        String[] projection = new String[] {
					"DISTINCT(" + MediaStore.Images.ImageColumns.BUCKET_ID + ")"
					};
			String where = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + 
				" IN (select " + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + 
				" from images where " + MediaStore.Images.ImageColumns.DATA + 
				" not like ('" + SDCARD_DCIM + "%' and '" + SDCARD_LOST_DIR + "%'))";
	        Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, where, null, null);
	        StringBuffer sb = new StringBuffer();
	        if (cursor != null) {
		        while (cursor.moveToNext() && cursor.getPosition() < 4) {
		              bucketId = cursor.getInt(0);
		              sb = individualFolderRecentImage(bucketId,sb);
		        }
		        cursor.close();
	        }
	        Log.i("sb.toString():: ", ""+sb.toString().substring(0, sb.toString().length()-1));
	        return sb.toString().substring(0, sb.toString().length()-1);
		} catch (Exception e) {
			Log.e("bucketid", "exception is " + e);
			return "";
		}
  }

  private StringBuffer individualFolderRecentImage(int bucketId,StringBuffer sb) {
        String imageId = "";
        String[] projection = new String[] {
        		MediaStore.Images.ImageColumns._ID, 
        		MediaStore.Images.ImageColumns.DATE_TAKEN
        		};
        
        String where = MediaStore.Images.ImageColumns.BUCKET_ID + "='" + bucketId + "'";
        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC LIMIT 1";
        Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, where, null, orderBy);
        if (cursor != null) {
        	if (cursor.getCount() > 0) {
	            cursor.moveToFirst();
	            imageId = cursor.getString(0);
	            sb.append(imageId );
	            sb.append(",");
        	}
            cursor.close();
        }
        return sb;
  }

  private Cursor getFolderCursor() {
	  String sb = bucketId();
	  String[] projection = new String[] {MediaStore.Images.ImageColumns._ID};
      String where = MediaStore.Images.ImageColumns._ID + " in ("+sb.toString()+")";
      Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection,
                  where, null, null);
      cursor.moveToFirst() ;
      //Log.d("Cursor Count",""+cursor.getCount());
      return cursor;
  }
    
	private void launchViewByDateActivity() {
		Intent intent = new Intent(this, ViewByDate.class);
		startActivity(intent);
	}
	
    private Vector<String> checkFolders() {
		Vector<String> vec = new Vector<String>();
		File file = new File(Environment.getExternalStorageDirectory().toString());
//		Log.i("path::  ", ""+file.getAbsolutePath());
		if (file.canRead()) {
			File[] child = file.listFiles();
			if (child != null) {
				for (int i = 0; i < child.length; i++) {
					if (child[i].isDirectory()) {
						String name = child[i].getName();
						if (name.equalsIgnoreCase(people)) {
							vec = AddFolder(vec, child[i]);
						} else if (name.equalsIgnoreCase(location)) {
							vec = AddFolder(vec, child[i]);
						} else if (name.equalsIgnoreCase(keyword)) {
							vec = AddFolder(vec, child[i]);
						} else if (name.equalsIgnoreCase(album)) {
							vec = AddFolder(vec, child[i]);
						} 
					}
				}
			}
		}
		if (isFolderDataPresent()) {
			vec.add(folder);
		}
		Vector<String> sotredVec = null;
		if (vec.size() > 0) {
			sotredVec = new Vector<String>();
			if (vec.contains(people)) {
				sotredVec.add(people);
			}
			if (vec.contains(location)) {
				sotredVec.add(location);
			}
			if (vec.contains(keyword)) {
				sotredVec.add(keyword);
			}
			if (vec.contains(album)) {
				sotredVec.add(album);
			}
			if (vec.contains(folder)) {
				sotredVec.add(folder);
			}
		}
//		Log.i("Vector::  ", ""+ sotredVec);
		if (sotredVec != null) {
			return sotredVec;
		} else {
			return vec;
		}
	}
    
    private Vector<String> AddFolder(Vector<String> p_foderVec, File p_file) {
    	try {
    	String t_fileParentPath = p_file.getAbsolutePath();
    	String[] projection = new String[]{MediaStore.Images.ImageColumns._ID};
    	String selection = MediaStore.Images.ImageColumns.DATA + " like '%" + t_fileParentPath + "%'";
    	Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
    	Log.i("cursor::  ", "cursor count :: "+cursor.getCount());
    	String bucket_id = getBucketId(p_file.getAbsolutePath());
    	if (cursor.getCount() > 0 && bucket_id != null) {
    		p_foderVec.add(p_file.getName());
			return p_foderVec;
    	}
    	} catch (Error e) {
//			Log.i("My Error::  ", "Error:: "+e);
		}
    	return p_foderVec;
	}

    
    private String getBucketId(String filePath) {
		String path = filePath;
    	int j=path.lastIndexOf('/');
    	String bucketName = path.substring(j+1, path.length());
    	//Log.d("bucketNamefromFile", ""+bucketName);
    	Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
     	String where = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " = '"+bucketName+"'" ;
    	String[] projection = { MediaStore.Images.ImageColumns.BUCKET_ID };
 		final Cursor c = getContentResolver().query(uri, projection, where, null, null);
 		startManagingCursor(c);
 		final int columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_ID);
 		String bucketId = null;
 		if (c != null && c.getCount() > 0) {
	 		c.moveToFirst();
	 		bucketId = c.getString(columnIndex);
 		}
    	return bucketId;
     }


    public HashMap<String, String> GetRecentImageOrVideoPath(ContentResolver cResolver) {
		HashMap< String, String> map = new HashMap<String, String>();
		long videoDateTaken = 0;
		long imageDateTaken = 0;
		String imageid = "";
		String videoid = "";
		Cursor tempImgCursor;
		Cursor tempVidCursor;

		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN};
        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC LIMIT 1";
		try {
			tempImgCursor = cResolver.query(uri, projection, null, null, orderBy);
			tempImgCursor.moveToFirst();
			int idColumnIdx = tempImgCursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);
			int dateTakenColIdx = tempImgCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
			imageDateTaken = tempImgCursor.getLong(dateTakenColIdx);
			imageid = tempImgCursor.getString(idColumnIdx);

			// Now check whether a Video was more recent than the Image
			Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			String[] Videoprojection = { MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATE_TAKEN};
			tempVidCursor = cResolver.query(videoUri, Videoprojection, null,
					null, orderBy);
			if (tempVidCursor.getCount() != 0) {
				tempVidCursor.moveToFirst();
				int videoIdColumnIdx = tempVidCursor
						.getColumnIndex(MediaStore.Video.VideoColumns._ID);
				int videoDateTakenColIdx = tempVidCursor
						.getColumnIndex(MediaStore.Video.VideoColumns.DATE_TAKEN);
				videoDateTaken = tempVidCursor.getLong(videoDateTakenColIdx);
				videoid = tempVidCursor.getString(videoIdColumnIdx);
			}
			if (videoDateTaken > 0 && imageDateTaken > 0) {
				if ( videoDateTaken <= imageDateTaken) // most recent item is a image
				{
					recentImage_dateTaken = imageDateTaken;
					map.put(imageid, "I");
				} else {
					recentImage_dateTaken = videoDateTaken;
					map.put(videoid, "V");
				}
			} else if (videoDateTaken > 0 || imageDateTaken > 0){
				if (imageDateTaken > 0) // most recent item is a image
				{
					recentImage_dateTaken = imageDateTaken;
					map.put(imageid, "I");
				} else {
					recentImage_dateTaken = videoDateTaken;
					map.put(videoid, "V");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.i("onCreateOptionsMenu(Menu menu)::", "onCreateOptionsMenu(Menu menu)::");
        MenuItem item = menu.add(Menu.NONE, 0, 0, "Capture") .setIcon(android.R.drawable.ic_menu_preferences);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				//TODO
				return false;
			}
		});
        item.setIcon(R.drawable.frame_overlay_gallery_camera);
        
        item = menu.add(Menu.NONE, 0, 0, R.string.camerasettings) .setIcon(android.R.drawable.ic_menu_preferences);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				//TODO
                return true;
				
			}
		});
        item.setIcon(android.R.drawable.ic_menu_preferences);
        return super.onCreateOptionsMenu(menu);
    }


    public HashMap<String, String> GetDateImageOrVideoPath(ContentResolver cResolver) {
    	HashMap< String, String> map = new HashMap<String, String>();
    	if (recentImage_dateTaken <= 0) {
    		return map;
    	} else {
    		Calendar cal = Calendar.getInstance();
    		cal.setTime(new Date(recentImage_dateTaken));
    		int year = cal.get(Calendar.YEAR);
    		cal.set(Calendar.YEAR, year);
        	cal.set(Calendar.MONTH, 0);
        	cal.set(Calendar.DATE, 1);
        	cal.set(Calendar.HOUR_OF_DAY, 0);
        	cal.set(Calendar.MINUTE, 0);
        	cal.set(Calendar.SECOND, 0);
        	long timeMiles = cal.getTimeInMillis();

        	long videoDateTaken = 0;
    		long imageDateTaken = 0;
    		String imageid = "";
    		String videoid = "";
    		Cursor tempImgCursor;
    		Cursor tempVidCursor;

    		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    		String[] projection = { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN};
    		String whereClause = MediaStore.Images.ImageColumns.DATE_TAKEN + " > " + timeMiles;
    		String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC LIMIT 1";
    		try {
    			tempImgCursor = cResolver.query(uri, projection, whereClause, null, orderBy);
    			tempImgCursor.moveToFirst();
    			int idColumnIdx = tempImgCursor
    					.getColumnIndex(MediaStore.Images.ImageColumns._ID);
    			int dateTakenColIdx = tempImgCursor
    					.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
    			imageDateTaken = tempImgCursor.getLong(dateTakenColIdx);
    			imageid = tempImgCursor.getString(idColumnIdx);

    			// Now check whether a Video was more recent than the Image
    			Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    			String[] Videoprojection = { MediaStore.Video.VideoColumns._ID,  MediaStore.Video.VideoColumns.DATE_TAKEN };
    			tempVidCursor = cResolver.query(videoUri, Videoprojection, whereClause,null, orderBy);
    			if (tempVidCursor.getCount() != 0) {
    				tempVidCursor.moveToFirst();
    				int videoIdColumnIdx = tempVidCursor
    						.getColumnIndex(MediaStore.Video.VideoColumns._ID);
    				int videoDateTakenColIdx = tempVidCursor
    						.getColumnIndex(MediaStore.Video.VideoColumns.DATE_TAKEN);
    				videoDateTaken = tempVidCursor.getLong(videoDateTakenColIdx);
    				videoid = tempVidCursor.getString(videoIdColumnIdx);
    			}
    			if (videoDateTaken > 0 && imageDateTaken > 0) {
    				if (videoDateTaken <= imageDateTaken) // most recent item is a image
    				{
    					map.put(videoid, "V");
    				} else {
    					map.put(imageid, "I");
    				}
    			}	else if (videoDateTaken > 0 || imageDateTaken > 0) {
    				if (videoDateTaken > 0) // most recent item is a image
    				{
    					map.put(videoid, "V");
    				} else {
    					map.put(imageid, "I");
    				}
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

    	}
    			return map;
	}
    
    private void setupOnTouchListeners(View rootView) {
        mGestureDetector = new GestureDetector(this, new MainScreenGestureListener());
         OnTouchListener rootListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                // We do not use the return value of
                // mGestureDetector.onTouchEvent because we will not receive
                // the "up" event if we return false for the "down" event.
                return true;
            }
        };
        
        rootView.setOnTouchListener(rootListener);
    }
    
    public class MainScreenGestureListener extends SimpleOnGestureListener{
    	private int swipe_Max_Distance = 350;
    	
    	@Override
    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    		final float xDistance = Math.abs(e1.getX() - e2.getX());
    		final float yDistance = Math.abs(e1.getY() - e2.getY());

    		if (xDistance > this.swipe_Max_Distance || yDistance > this.swipe_Max_Distance) {
    			return false;
    		}
    		velocityX = Math.abs(velocityX);
    		velocityY = Math.abs(velocityY);
    		boolean result = false;
    		int childCount = viewFlipper.getChildCount();
    		if (e1.getX() > e2.getX()) {
    			if (mCurrentShowingPage < (childCount -1)) {
    				showNextPage(mCurrentShowingPage + 1);
    			}
    		} else {
    			if (mCurrentShowingPage > 0) {
    				showNextPage(mCurrentShowingPage -1);
    			}
    		}
    		return result;
    	}
    }
    
    Dialog mMediaScanningDialog;

    // Display a dialog if the storage is being scanned now.
    public void updateScanningDialog(boolean scanning) {
    	
/*    	Log.d("MainScreen", "inside updateScanningDialog()");
        boolean prevScanning = (mMediaScanningDialog != null);
        if (prevScanning) {
            mMediaScanningDialog.cancel();
            mMediaScanningDialog = null;
        }
*/         
    }

    

    private void hideNoImagesView() {
    	if (mNoImagesView != null) {
            mNoImagesView.setVisibility(View.GONE);
        }
    }

    // The storage status is changed, restart the worker or show "no images".
    private void rebake(boolean unmounted, boolean scanning) {
//    	Log.d("MainScreen", "inside rebake()");
        if (unmounted == mUnmounted && scanning == mScanning) return;
        abortWorker();
        mUnmounted = unmounted;
        mScanning = scanning;
        updateScanningDialog(mScanning);
        if (mUnmounted) {
            showNoImagesView();
        } else {
            hideNoImagesView();
            startWorker();
        }
    }

    // This is called when we receive media-related broadcast.
    private void onReceiveMediaBroadcast(Intent intent) {
//    	Log.d("MainScreen", "inside onReceiveMediaBroadcast()");
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            // SD card available
            // TODO put up a "please wait" message
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

    private void launchFolderGallery(int position) {
//    	Log.d("MainScreen", "inside launchFolderGallery()");
//        mAdapter.mItems.get(position).launch(this);
    }

    private void onCreateGalleryPickerContextMenu(ContextMenu menu,
            final ContextMenuInfo menuInfo) {
    	// "View"
        menu.add(R.string.view)
                .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                            return onViewClicked(menuInfo);
                    }
                });
    }

    // This is called when the user clicks "View" from the context menu.
    private boolean onViewClicked(ContextMenuInfo menuInfo) {
//    	Log.d("MainScreen", "inside onViewClicked()");
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        launchFolderGallery(info.position);
        return true;
    }

    @Override
    public void onStop() {
//    	Log.d("MainScreen", "inside onStop()");
        super.onStop();
        abortWorker();
        unregisterReceiver(mReceiver);
        getContentResolver().unregisterContentObserver(mDbObserver);
        // free up some ram
        unloadDrawable();
    }

    @Override
    public void onStart() {
//    	Log.d("MainScreen", "inside onStart()");
        super.onStart();

         // install an intent filter to receive SD card related events.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");

        registerReceiver(mReceiver, intentFilter);

        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true, mDbObserver);
       // Assume the storage is mounted and not scanning.
        mUnmounted = false;
        mScanning = false;
        startWorker();
    }

    // This is used to stop the worker thread.
    volatile boolean mAbort = false;

    // Create the worker thread.
    private void startWorker() {
//    	Log.d("MainScreen", "inside startWorker()");
        mAbort = false;
        mWorkerThread = new Thread("MainScreen Worker") {
            @Override
            public void run() {
                workerRun();
            }
        };
        BitmapManager.instance().allowThreadDecoding(mWorkerThread);
        mWorkerThread.start();
    }

    private void abortWorker() {
//    	Log.d("MainScreen", "inside abortWorker()::  "+mWorkerThread);
        if (mWorkerThread != null) {
            BitmapManager.instance().cancelThreadDecoding(mWorkerThread);
            MediaStore.Images.Thumbnails.cancelThumbnailRequest(getContentResolver(), -1);
            mAbort = true;
            try {
                mWorkerThread.join();
            } catch (InterruptedException ex) {
                Log.e(TAG, "join interrupted");
            }
            mWorkerThread = null;
            // Remove all runnables in mHandler.
            // (We assume that the "what" field in the messages are 0
            // for runnables).
            
            mHandler.removeMessages(0);
//            mAdapter.clear();
//            mAdapter.updateDisplay();
            clearImageLists();
        }
    }

    // This is run in the worker thread.
     private void workerRun() {
//    	Log.d("MainScreen", "inside workerRun()");
        // We collect items from checkImageList() and checkBucketIds() and
        // put them in allItems. Later we give allItems to checkThumbBitmap()
        // and generated thumbnail bitmaps for each item. We do this instead of
        // generating thumbnail bitmaps in checkImageList() and checkBucketIds()
        // because we want to show all the folders first, then update them with
        // the thumb bitmaps. (Generating thumbnail bitmaps takes some time.)
        ArrayList<Item> allItems = new ArrayList<Item>();
        try {
        	checkScanning();
        	if (mAbort) {
        		return;
        	}
        } catch(Exception e) {
        		Log.d("checksac","caughtcaught");
        }

        checkImageList(allItems);
        if (mAbort) return;

//        checkBucketIds(allItems);
//        if (mAbort) return;
        
//        checkThumbBitmap(allItems);
//        
//        if (mAbort) return;

        checkLowStorage();
    }

	private void checkBucketPictureVideo(String bucketPath) {
//		Log.d("gp", "inside checkBucketFiles()");
		String bucketId = getBucketId(bucketPath);
		IImageList allImages = ImageManager.makeImageList(getContentResolver(),
				ImageManager.DataLocation.ALL, ImageManager.INCLUDE_IMAGES
						| ImageManager.INCLUDE_VIDEOS,
				ImageManager.SORT_DESCENDING, bucketId);

		for (int k = 0; k < allImages.getCount(); k++) {
			IImage img = allImages.getImageAt(k);
			Bitmap bitmap = img.miniThumbBitmap();
			Item item = new Item(bitmap, img.fullSizeImageUri(),
					Item.TYPE_CHILD_IMAGES_VIDEOS);

			final Item finalItem = item;
			mHandler.post(new Runnable() {
				public void run() {
					updateItem(finalItem);
				}
			});
		}
		// mHandler.post(new Runnable() {
		// public void run() {
		// checkBucketIdsFinished();
		// }
		// });

	}
     
    // This is run in the worker thread.
    private void checkScanning() {
//    	Log.d("MainScreen", "inside checkScanning()");
        ContentResolver cr = getContentResolver();
        ContentResolver cr2 = getApplicationContext().getContentResolver();
        ContentResolver cr3 = getBaseContext().getContentResolver();
        final boolean scanning =
                ImageManager.isMediaScannerScanning(cr);
        mHandler.post(new Runnable() {
                    public void run() {
                        checkScanningFinished(scanning);
                    }
                });
    }

    // This is run in the main thread.
    private void checkScanningFinished(boolean scanning) {
//    	Log.d("MainScreen", "inside checkScanningFinished()");
        updateScanningDialog(scanning);
    }

    // This is run in the worker thread.
    private void checkImageList(ArrayList<Item> allItems) {
//    	Log.d("MainScreen", "inside checkImageList()");
    	checkFolders();
        int length = IMAGE_LIST_DATA.length;
        IImageList[] lists = new IImageList[length];
        for (int i = 0; i < length; i++) {
            ImageListData data = IMAGE_LIST_DATA[i];
            lists[i] = createImageList(data.mInclude, data.mBucketId, getContentResolver());
            if (mAbort) return;
            Item item = null;

            if (lists[i].isEmpty()) continue;

            // i >= 3 means we are looking at All Images/All Videos.
            // lists[i-3] is the corresponding Camera Images/Camera Videos.
            // We want to add the "All" list only if it's different from
            // the "Camera" list.
            if (i >= 3 && lists[i].getCount() == lists[i - 3].getCount()) {
                continue;
            }

            item = new Item(data.mType,
                            data.mBucketId,
                            getResources().getString(data.mStringId),
                            lists[i]);

            allItems.add(item);

            final Item finalItem = item;
            mHandler.post(new Runnable() {
                        public void run() {
//                            updateItem(finalItem);
                        }
                    });
        }
    }

    // This is run in the main thread.
    private void updateItem(Item item) {
//    	Log.d("MainScreen", "inside updateItem()");
        // Hide NoImageView if we are going to add the first item
//        if (mAdapter.getCount() == 0) {
//            hideNoImagesView();
//        }
//        mAdapter.addItem(item);
//        mAdapter.updateDisplay();
    }

    private static final String CAMERA_BUCKET = ImageManager.CAMERA_IMAGE_BUCKET_ID;
    
	
    // This is run in the worker thread.
    private void checkBucketIds(ArrayList<Item> allItems) {
//    	Log.d("MainScreen", "inside checkBucketIds()");
        final IImageList allImages;
        if (!mScanning && !mUnmounted) {
            allImages = ImageManager.makeImageList(
                    getContentResolver(),
                    ImageManager.DataLocation.ALL,
                    ImageManager.INCLUDE_IMAGES | ImageManager.INCLUDE_VIDEOS,
                    ImageManager.SORT_DESCENDING,
                    null);
        } else {
            allImages = ImageManager.makeEmptyImageList();
        }

        if (mAbort) {
            allImages.close();
            return;
        }

        HashMap<String, String> hashMap = allImages.getBucketIds();
        
        allImages.close();
        if (mAbort) return;

        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String key = entry.getKey();
//            Log.i("Entry::  ", ""+entry);
            if (key == null) {
                continue;
            }
            if (!key.equals(CAMERA_BUCKET)) {
                IImageList list = createImageList(
                        ImageManager.INCLUDE_IMAGES
                        | ImageManager.INCLUDE_VIDEOS, key,
                        getContentResolver());
                if (mAbort) return;

                Item item = new Item(Item.TYPE_NORMAL_FOLDERS, key,
                        entry.getValue(), list);

                allItems.add(item);
               
                final Item finalItem = item;
                mHandler.post(new Runnable() {
                            public void run() {
                                updateItem(finalItem);
                            }
                        });
            }
        }

        mHandler.post(new Runnable() {
                    public void run() {
//                        checkBucketIdsFinished();
                    }
                });
    }

    // This is run in the main thread.
    private void checkBucketIdsFinished() {
//    	Log.d("MainScreen", "inside checkBucketIdsFinished()");

        // If we just have one folder, open it.
        // If we have zero folder, show the "no images" icon.
//        if (!mScanning) {
//            int numItems = mAdapter.mItems.size();
//            if (numItems == 0) {
//                showNoImagesView();
//            } else if (numItems == 1) {
////                mAdapter.mItems.get(0).launch(this);
////                finish();
//                return;
//            }
//        }
    }

    private static final int THUMB_SIZE = 142;
    // This is run in the worker thread.
    private void checkThumbBitmap(ArrayList<Item> allItems) {
//    	Log.d("MainScreen", "inside checkThumbBitmap()");
        for (Item item : allItems) {
            final Bitmap b = makeMiniThumbBitmap(THUMB_SIZE, THUMB_SIZE, item.mImageList);
            if (mAbort) {
                if (b != null) b.recycle();
                return;
            }

            final Item finalItem = item;
            mHandler.post(new Runnable() {
                        public void run() {
                            updateThumbBitmap(finalItem, b);
                        }
                    });
        }
    }

    // This is run in the main thread.
    private void updateThumbBitmap(Item item, Bitmap b) {
//    	Log.d("MainScreen", "inside updateThumbBitmap()");
        item.setThumbBitmap(b);
//        mAdapter.updateDisplay();
    }

    private static final long LOW_STORAGE_THRESHOLD = 1024 * 1024 * 2;

    // This is run in the worker thread.
    private void checkLowStorage() {
//    	Log.d("MainScreen", "inside checkLowStorage()");
        // Check available space only if we are writable
        if (ImageManager.hasStorage()) {
            String storageDirectory = Environment
                    .getExternalStorageDirectory().toString();
            StatFs stat = new StatFs(storageDirectory);
            long remaining = (long) stat.getAvailableBlocks()
                    * (long) stat.getBlockSize();
            if (remaining < LOW_STORAGE_THRESHOLD) {
                mHandler.post(new Runnable() {
                    public void run() {
                        checkLowStorageFinished();
                    }
                });
            }
        }
    }

    // This is run in the main thread.
    // This is called only if the storage is low.
    private void checkLowStorageFinished() {
//    	Log.d("MainScreen", "inside checkLowStorageFinished()");
        Toast.makeText(MainScreen.this, R.string.not_enough_space, 5000)
                .show();
    }

    // IMAGE_LIST_DATA stores the parameters for the four image lists
    // we are interested in. The order of the IMAGE_LIST_DATA array is
    // significant (See the implementation of GalleryPickerAdapter.init).
    private static final class ImageListData {
        ImageListData(int type, int include, String bucketId, int stringId) {
            mType = type;
            mInclude = include;
            mBucketId = bucketId;
            mStringId = stringId;
        }
        int mType;
        int mInclude;
        String mBucketId;
        int mStringId;
    }

    private static final ImageListData[] IMAGE_LIST_DATA = {
			new ImageListData(Item.TYPE_ALL_IMAGES, ImageManager.INCLUDE_IMAGES
					| ImageManager.INCLUDE_VIDEOS, null, R.string.main_recent),
			// All Images
			new ImageListData(Item.TYPE_ALL_IMAGES, ImageManager.INCLUDE_IMAGES
					| ImageManager.INCLUDE_VIDEOS, null, R.string.main_date),

					
			// Camera Medias
			new ImageListData(Item.TYPE_CAMERA_MEDIAS,
					ImageManager.INCLUDE_IMAGES,
					ImageManager.CAMERA_IMAGE_BUCKET_ID, R.string.main_people),

			new ImageListData(Item.TYPE_CAMERA_IMAGES,
					ImageManager.INCLUDE_IMAGES | ImageManager.INCLUDE_VIDEOS,
					null, R.string.main_location),

			new ImageListData(Item.TYPE_CAMERA_IMAGES,
					ImageManager.INCLUDE_IMAGES | ImageManager.INCLUDE_VIDEOS,
					null, R.string.main_keywords),
			new ImageListData(Item.TYPE_CAMERA_IMAGES,
					ImageManager.INCLUDE_IMAGES | ImageManager.INCLUDE_VIDEOS,
					null, R.string.main_albums),
			new ImageListData(Item.TYPE_CAMERA_IMAGES,
					ImageManager.INCLUDE_IMAGES | ImageManager.INCLUDE_VIDEOS,
					null, R.string.main_folders),
					
    };

    
    
    // These drawables are loaded on-demand.
    Drawable mFrameGalleryMask;
    Drawable mCellOutline;
    Drawable mVideoOverlay;

    private void loadDrawableIfNeeded() {
//    	Log.d("MainScreen", "inside loadDrawableIfNeeded()");
        if (mFrameGalleryMask != null) return;  // already loaded
        Resources r = getResources();
        mFrameGalleryMask = r.getDrawable(
                R.drawable.frame_gallery_preview_album_mask);
        mCellOutline = r.getDrawable(android.R.drawable.gallery_thumb);
        mVideoOverlay = r.getDrawable(R.drawable.ic_gallery_video_overlay);
    }

    private void unloadDrawable() {
//    	Log.d("MainScreen", "inside unloadDrawable()");
        mFrameGalleryMask = null;
        mCellOutline = null;
        mVideoOverlay = null;
    }

    private static void placeImage(Bitmap image, Canvas c, Paint paint,
            int imageWidth, int widthPadding, int imageHeight,
            int heightPadding, int offsetX, int offsetY,
            int pos) {
        int row = pos / 2;
        int col = pos - (row * 2);

        int xPos = (col * (imageWidth + widthPadding)) - offsetX;
        int yPos = (row * (imageHeight + heightPadding)) - offsetY;
//        Log.d("MainScreen", "inside placeImage()");
        c.drawBitmap(image, xPos, yPos, paint);
    }

    // This is run in worker thread.
    private Bitmap makeMiniThumbBitmap(int width, int height, IImageList images) {
//    	Log.d("MainScreen", "inside makeMiniThumbBitmap()");
        int count = images.getCount();
        // We draw three different version of the folder image depending on the
        // number of images in the folder.
        //    For a single image, that image draws over the whole folder.
        //    For two or three images, we draw the two most recent photos.
        //    For four or more images, we draw four photos.
        final int padding = 4;
        int imageWidth = width;
        int imageHeight = height;
        int offsetWidth = 0;
        int offsetHeight = 0;

        imageWidth = (imageWidth - padding) / 2;  // 2 here because we show two
                                                  // images
        imageHeight = (imageHeight - padding) / 2;  // per row and column

        final Paint p = new Paint();
        final Bitmap b = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(b);
        final Matrix m = new Matrix();

        // draw the whole canvas as transparent
        p.setColor(0x00000000);
        c.drawPaint(p);

        // load the drawables
        loadDrawableIfNeeded();

        // draw the mask normally
        p.setColor(0xFFFFFFFF);
        mFrameGalleryMask.setBounds(0, 0, width, height);
        mFrameGalleryMask.draw(c);

        Paint pdpaint = new Paint();
        pdpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        pdpaint.setStyle(Paint.Style.FILL);
        c.drawRect(0, 0, width, height, pdpaint);

        for (int i = 0; i < 4; i++) {
            if (mAbort) {
                return null;
            }

            Bitmap temp = null;
            IImage image = i < count ? images.getImageAt(i) : null;

            if (image != null) {
                temp = image.miniThumbBitmap();
            }

            if (temp != null) {
                if (ImageManager.isVideo(image)) {
                    Bitmap newMap = temp.copy(temp.getConfig(), true);
                    Canvas overlayCanvas = new Canvas(newMap);
                    int overlayWidth = mVideoOverlay.getIntrinsicWidth();
                    int overlayHeight = mVideoOverlay.getIntrinsicHeight();
                    int left = (newMap.getWidth() - overlayWidth) / 2;
                    int top = (newMap.getHeight() - overlayHeight) / 2;
                    Rect newBounds = new Rect(left, top, left + overlayWidth,
                            top + overlayHeight);
                    mVideoOverlay.setBounds(newBounds);
                    mVideoOverlay.draw(overlayCanvas);
                    temp.recycle();
                    temp = newMap;
                }

                temp = Util.transform(m, temp, imageWidth,
                        imageHeight, true, Util.RECYCLE_INPUT);
            }

            Bitmap thumb = Bitmap.createBitmap(imageWidth, imageHeight,
                                               Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(thumb);
            if (temp != null) {
                tempCanvas.drawBitmap(temp, new Matrix(), new Paint());
            }
//            mCellOutline.setBounds(0, 0, imageWidth, imageHeight);
//            mCellOutline.draw(tempCanvas);

            placeImage(thumb, c, pdpaint, imageWidth, padding, imageHeight,
                       padding, offsetWidth, offsetHeight, i);

            thumb.recycle();

            if (temp != null) {
                temp.recycle();
            }
        }

        return b;
    }

   
    // image lists created by createImageList() are collected in mAllLists.
    // They will be closed in clearImageList, so they don't hold open files
    // on SD card. We will be killed if we don't close files when the SD card
    // is unmounted.
    ArrayList<IImageList> mAllLists = new ArrayList<IImageList>();

    private IImageList createImageList(int mediaTypes, String bucketId,
            ContentResolver cr) {
//    	Log.d("MainScreen", "inside createImageList()");
        IImageList list = ImageManager.makeImageList(
                cr,
                ImageManager.DataLocation.ALL,
                mediaTypes,
                ImageManager.SORT_DESCENDING,
                bucketId);
        mAllLists.add(list);
        return list;
    }

    private void clearImageLists() {
//    	Log.d("MainScreen", "inside clearImageLists()");
        for (IImageList list : mAllLists) {
            list.close();
        }
        mAllLists.clear();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	finish();
        	return true;
        }
    	return false;
    }

}

// Item is the underlying data for GalleryPickerAdapter.
// It is passed from the activity to the adapter.
class Item {
    public static final int TYPE_NONE = -1;
    public static final int TYPE_ALL_IMAGES = 0;
    public static final int TYPE_ALL_VIDEOS = 1;
    public static final int TYPE_CAMERA_IMAGES = 2;
    public static final int TYPE_CAMERA_VIDEOS = 3;
    public static final int TYPE_CAMERA_MEDIAS = 4;
    public static final int TYPE_NORMAL_FOLDERS = 5;
    
 // change start by HCL
    public static final int TYPE_CHILD_FOLDERS = 6;
    public static final int TYPE_CHILD_IMAGES_VIDEOS = 7;
   //change end by HCL  

    public final int mType;
    public final String mBucketId;
    public final String mName;
    public final IImageList mImageList;
    public final int mCount;
    public final Uri mFirstImageUri;  // could be null if the list is empty

    // The thumbnail bitmap is set by setThumbBitmap() later because we want
    // to let the user sees the folder icon as soon as possible (and possibly
    // select them), then present more detailed information when we have it.
    public Bitmap mThumbBitmap;  // the thumbnail bitmap for the image list

    public Item(int type, String bucketId, String name, IImageList list) {
//    	Log.d("MainScreen", "inside Item class-Item()");
        mType = type;
        mBucketId = bucketId;
        mName = name;
        mImageList = list;
        mCount = list.getCount();
        if (mCount > 0) {
            mFirstImageUri = list.getImageAt(0).fullSizeImageUri();
        } else {
            mFirstImageUri = null;
        }
    }
    
    public Item(Bitmap mThumbBitmap, Uri uri, int type) {
		mType = type;
        mBucketId = "";
        mName = "";
        mImageList = null;
        mCount = 1;
		this.mThumbBitmap = mThumbBitmap;
		this.mFirstImageUri = uri;
		
	}

    public void setThumbBitmap(Bitmap thumbBitmap) {
//    	Log.d("MainScreen", "inside Item class-setThumbBitmap()");
        mThumbBitmap = thumbBitmap;
    }

    public boolean needsBucketId() {
//    	Log.d("MainScreen", "inside Item class-needsBucketId()");
        return mType >= TYPE_CAMERA_IMAGES;
    }

    public void launch(Activity activity) {
//    	Log.d("MainScreen", "inside Item class-launch()");
        Uri uri = Images.Media.INTERNAL_CONTENT_URI;
        if (needsBucketId()) {
            uri = uri.buildUpon()
                    .appendQueryParameter("bucketId", mBucketId).build();
        }
        Log.i("uri :: ", ""+uri.getPath());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra("windowTitle", mName);
        intent.putExtra("mediaTypes", getIncludeMediaTypes());
        activity.startActivity(intent);
    }

    public int getIncludeMediaTypes() {
//    	Log.d("MainScreen", "inside Item class-getIncludeMediaTypes()");
        return convertItemTypeToIncludedMediaType(mType);
    }

    public static int convertItemTypeToIncludedMediaType(int itemType) {
//    	Log.d("MainScreen", "inside Item class-convertItemTypeToIncludedMediaType()");
        switch (itemType) {
        case TYPE_ALL_IMAGES:
        case TYPE_CAMERA_IMAGES:
            return ImageManager.INCLUDE_IMAGES;
        case TYPE_ALL_VIDEOS:
        case TYPE_CAMERA_VIDEOS:
            return ImageManager.INCLUDE_VIDEOS;
        case TYPE_NORMAL_FOLDERS:
        case TYPE_CAMERA_MEDIAS:
        default:
            return ImageManager.INCLUDE_IMAGES
                    | ImageManager.INCLUDE_VIDEOS;
        }
    }

    public int getOverlay() {
//    	Log.d("MainScreen", "inside Item class-getOverlay()");
        switch (mType) {
            case TYPE_ALL_IMAGES:
            case TYPE_CAMERA_IMAGES:
                return R.drawable.frame_overlay_gallery_camera;
            case TYPE_ALL_VIDEOS:
            case TYPE_CAMERA_VIDEOS:
            case TYPE_CAMERA_MEDIAS:
                return R.drawable.frame_overlay_gallery_video;
            case TYPE_NORMAL_FOLDERS:
            default:
                return R.drawable.frame_overlay_gallery_folder;
        }
    }
}

