package com.kodak.gallery.camera;

import android.content.Context;
import android.util.AttributeSet;
import java.util.HashMap;

//import com.android.kcamera.CustomImageBlockManager.ImageBlock;
import com.kodak.gallery.camera.gallery.IImage;
import com.kodak.gallery.camera.gallery.IImageList;
import com.kodak.relive.R;
import static com.kodak.gallery.camera.Util.Assert;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Scroller;
public class GridViewL extends View
{
	@SuppressWarnings("unused")
    private static final String TAG = "CustomGridView";
    private static final float MAX_FLING_VELOCITY = 2500;
    private static final int WIDTH = 79;
    private static final int HEIGHT = 79;
    private static final int CELL_SPACING = 1;
    private static final int LEFT_EDGE_PADDING = 0;

    public static interface Listener {
        public void onImageClicked(int index);
        public void onImageTapped(int index);
        public void onLayoutComplete(boolean changed);

        /**
         * Invoked when the <code>MyGridView</code> scrolls.
         *
         * @param scrollPosition the position of the scroller in the range
         *         [0, 1], when 0 means on the top and 1 means on the buttom
         */
        public void onScroll(float scrollPosition);
    }

    public static interface DrawAdapter {
        public void drawImage(Canvas canvas, IImage image,
                Bitmap b, int xPos, int yPos, int w, int h);
        public void drawDecoration(Canvas canvas, IImage image,
                int xPos, int yPos, int w, int h);
        public boolean needsDecoration();
        public void setThumbPosition(int progress);
    }

    public static final int INDEX_NONE = -1;

    // There are two cell size we will use. It can be set by setSizeChoice().
    // The mLeftEdgePadding fields is filled in onLayout(). See the comments
    // in onLayout() for details.
    static class LayoutSpec {
        LayoutSpec(int w, int h, int intercellSpacing, int leftEdgePadding,
                DisplayMetrics metrics) {
//            mCellWidth = dpToPx(w, metrics);
//            mCellHeight = dpToPx(h, metrics);
//        	  mCellSpacing = dpToPx(intercellSpacing, metrics);
//            mLeftEdgePadding = dpToPx(leftEdgePadding, metrics);
        	  mCellWidth = w;
	          mCellHeight = h;	   
	          mCellSpacing = intercellSpacing;
              mLeftEdgePadding = leftEdgePadding;
        }
        int mCellWidth, mCellHeight;
        int mCellSpacing;
        int mLeftEdgePadding;
    }

    private LayoutSpec [] mCellSizeChoices;

    private void initCellSize() {
    	Log.e("initCellSize", "GridL");
        Activity a = (Activity) getContext();
        DisplayMetrics metrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mCellSizeChoices = new LayoutSpec[] {
            new LayoutSpec(WIDTH, HEIGHT, CELL_SPACING, LEFT_EDGE_PADDING, metrics),
            new LayoutSpec(WIDTH, HEIGHT, CELL_SPACING, LEFT_EDGE_PADDING, metrics),
        };
    }

    // Converts dp to pixel.
    private static int dpToPx(int dp, DisplayMetrics metrics) {
        return (int) (metrics.density * dp);
    }

    // These are set in init().
    private final Handler mHandler = new Handler();
    private GestureDetector mGestureDetector;
    private ImageBlockManagerL mImageBlockManager;

    // These are set in set*() functions.
    private ImageLoader mLoader;
    private Listener mListener = null;
    private DrawAdapter mDrawAdapter = null;
    private IImageList mAllImages = ImageManager.makeEmptyImageList();
    private int mSizeChoice = 1;  // default is big cell size

    // These are set in onLayout().
    private LayoutSpec mSpec;
    private int mRows;
    private int mMaxScrollX; 
    
    // We can handle events only if onLayout() is completed.
    private boolean mLayoutComplete = false;

    // Selection state
    private int mCurrentSelection = INDEX_NONE;
    private int mCurrentPressState = 0;
    private static final int TAPPING_FLAG = 1;
    private static final int CLICKING_FLAG = 2;

    // These are cached derived information.
    private int mCount;  // Cache mImageList.getCount();
    private int mColumns;// Cache (mCount + mRows - 1) / mRows
    private int mBlockHeight; // Cache mSpec.mCellSpacing + mSpec.mCellHeight
    private int mBlockWidth;

    private boolean mRunning = false;
    private Scroller mScroller = null;
	    
	public GridViewL(Context context, AttributeSet attrs) {
		super(context, attrs);
        init(context);
	}
	
	private void init(Context context) {   
		Log.e("init", "GridL");
        setHorizontalScrollBarEnabled(true);
        mGestureDetector = new GestureDetector(context,
                new MyGestureDetector());
        setFocusableInTouchMode(true);
        initCellSize();
    }

    private final Runnable mRedrawCallback = new Runnable() {
        public void run() {
            invalidate();
        }
    };
//private int mScrollY;//added by sachi

	public void setLoader(ImageLoader loader) {
		Log.e("setLoader", "GridL");
	Assert(mRunning == false);
	mLoader = loader;
	}
	
	public void setListener(Listener listener) {
		Log.e("setListener", "GridL");
	Assert(mRunning == false);
	mListener = listener;
	}
	
	public void setDrawAdapter(DrawAdapter adapter) {
		Log.e("setDrawAdapter", "GridL");
	Assert(mRunning == false);
	mDrawAdapter = adapter;
	}
	
	public void setImageList(IImageList list) {
		Log.e("setImageList", "GridL");
	Assert(mRunning == false);
	mAllImages = list;
	mCount = mAllImages.getCount();
	}
	
	public void setSizeChoice(int choice) {
		Log.e("setSizeChoice", "GridL");
	Assert(mRunning == false);
	if (mSizeChoice == choice) return;
	mSizeChoice = choice;
	}
	
	 @Override
    public void onLayout(boolean changed, int left, int top,
                         int right, int bottom)
	{
		 Log.e("onLayout", "GridL");
        super.onLayout(changed, left, top, right, bottom);

        if (!mRunning) {
            return;
        }

        mSpec = mCellSizeChoices[mSizeChoice];
        int width = right - left;
        int height = bottom - top;

        // The width is divided into following parts:
        //
        // LeftEdgePadding CellWidth (CellSpacing CellWidth)* RightEdgePadding
        //
        // We determine number of cells (columns) first, then the left and right
        // padding are derived. We make left and right paddings the same size.
        //
        // The height is divided into following parts:
        //
        // CellSpacing (CellHeight CellSpacing)+

        mRows = 1 + (height - mSpec.mCellHeight)
        			/ (mSpec.mCellHeight + mSpec.mCellSpacing);
        
        mSpec.mLeftEdgePadding = 0;
        mColumns = (mCount + mRows - 1) / mRows;
        mBlockWidth = mSpec.mCellSpacing + mSpec.mCellWidth;
       
        mMaxScrollX = mSpec.mCellSpacing + (mColumns * mBlockWidth)
	                - (width);

        // Put mScrollY in the valid range. This matters if mMaxScrollY is
        // changed. For example, orientation changed from portrait to landscape.
       //Arti this variable is protected in the View.java in the Framework.
        //mScrollY = Math.max(0, Math.min(mMaxScrollY, getScrollY()));
        

        generateOutlineBitmap();

        if (mImageBlockManager != null) {
            mImageBlockManager.recycle();
        }

        mImageBlockManager = new ImageBlockManagerL(mHandler, mRedrawCallback,
                mAllImages, mLoader, mDrawAdapter, mSpec, mRows, height,
                mOutline[OUTLINE_EMPTY]);
        
        mListener.onLayoutComplete(changed);

        moveDataWindow();

        mLayoutComplete = true;
    }
 
	 @Override
	    protected int computeHorizontalScrollRange() {
		 Log.e("onLayout", "GridL");
	        return mMaxScrollX + getWidth();
	    }
	 // We cache the three outlines from NinePatch to Bitmap to speed up
	    // drawing. The cache must be updated if the cell size is changed.
	    public static final int OUTLINE_EMPTY = 0;
	    public static final int OUTLINE_PRESSED = 1;
	    public static final int OUTLINE_SELECTED = 2;

	    public Bitmap mOutline[] = new Bitmap[3];
	    
	 private void generateOutlineBitmap() {
		 Log.e("onLayout", "GridL");
	        int w = mSpec.mCellWidth;
	        int h = mSpec.mCellHeight;

	        for (int i = 0; i < mOutline.length; i++) {
	        	if ( i == 0 ){ //deepak
	        		mOutline[i] = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
	        	}
	        	else {
	        		mOutline[i] = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	        	}
	        }

	        Drawable cellOutline;
	        cellOutline = GridViewL.this.getResources()
	                .getDrawable(android.R.drawable.gallery_thumb);
	        cellOutline.setBounds(0, 0, w, h);
	        Canvas canvas = new Canvas();

	        canvas.setBitmap(mOutline[OUTLINE_EMPTY]);
	        cellOutline.setState(EMPTY_STATE_SET);
	        cellOutline.draw(canvas);

	        canvas.setBitmap(mOutline[OUTLINE_PRESSED]);
	        cellOutline.setState(
	                PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET);
	        cellOutline.draw(canvas);
	        canvas.setBitmap(mOutline[OUTLINE_SELECTED]);
	        cellOutline.setState(ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET);
	        cellOutline.draw(canvas);
	    }
	 
	 private void moveDataWindow()
	 {
		 Log.e("onLayout", "GridL");
	     // Calculate visible region according to scroll position.
		 int startCol = (getScrollX() - mSpec.mCellSpacing) / mBlockWidth;
		 int endCol = (getScrollX() + getWidth() - mSpec.mCellSpacing - 1) / mBlockWidth + 1;
        // Limit startRow and endRow to the valid range.
        // Make sure we handle the mRows == 0 case right.
		 startCol = Math.max(Math.min(startCol, mColumns - 1), 0);
		 endCol = Math.max(Math.min(endCol, mColumns), 0);
		 mImageBlockManager.setVisibleColumns(startCol, endCol);//TODO_SMITA
		 
	    }
	 
	// In MyGestureDetector we have to check canHandleEvent() because
	    // GestureDetector could queue events and fire them later. At that time
	    // stop() may have already been called and we can't handle the events.
	    private class MyGestureDetector extends SimpleOnGestureListener {
	        private AudioManager mAudioManager;

	        @Override
	        public boolean onDown(MotionEvent e) {
	        	Log.e("onDown", "MyGestureDetector");
	            if (!canHandleEvent()) return false;
	            if (mScroller != null && !mScroller.isFinished()) {
	                mScroller.forceFinished(true);
	                return false;
	            }
	            int index = computeSelectedIndex(e.getX(), e.getY());
	            if (index >= 0 && index < mCount) {
	                setSelectedIndex(index, false);
	            } else {
	                setSelectedIndex(INDEX_NONE, false);
	            }
	            return true;
	        }
	        
	        @Override
	        public boolean onFling(MotionEvent e1, MotionEvent e2,
	                float velocityX, float velocityY)
	        {
	        	Log.e("onFling", "MyGestureDetector");
	            if (!canHandleEvent()) return false;
	            if (velocityX > MAX_FLING_VELOCITY)
	            {
	                velocityX = MAX_FLING_VELOCITY;
	            } 
	            else if (velocityX < -MAX_FLING_VELOCITY)
	            {
	                velocityX = -MAX_FLING_VELOCITY;
	            }

	            setSelectedIndex(INDEX_NONE, false);
	            mScroller = new Scroller(getContext());
	            mScroller.fling(getScrollX(), 0, -(int) velocityX, 0, 0, mMaxScrollX,
	            				0, 0);
	            computeScroll();

	            return true;
	        }

	        @Override
	        public void onLongPress(MotionEvent e) {
	        	Log.e("onLongPress", "MyGestureDetector");
	            if (!canHandleEvent()) return;
	            performLongClick();
	        }

	        @Override
	        public boolean onScroll(MotionEvent e1, MotionEvent e2,
	                                float distanceX, float distanceY) {
	        	Log.e("onScroll", "MyGestureDetector");
	            if (!canHandleEvent()) return false;
	            setSelectedIndex(INDEX_NONE, false);
	            scrollBy((int) distanceX, 0);
	            invalidate();
	            return true;
	        }
	        
	        @Override
	        public boolean onSingleTapConfirmed(MotionEvent e) {
	        	Log.e("onSingleTapConfirmed", "MyGestureDetector");
	            if (!canHandleEvent()) return false;
	            int index = computeSelectedIndex(e.getX(), e.getY());
	            if (index >= 0 && index < mCount) {
	                // Play click sound.
	                if (mAudioManager == null) {
	                    mAudioManager = (AudioManager) getContext()
	                            .getSystemService(Context.AUDIO_SERVICE);
	                }
	                mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);

	                mListener.onImageTapped(index);
	                return true;
	            }
	            return false;
	        }
	    }

	    public int getCurrentSelection() {
	    	Log.e("getCurrentSelection", "MyGestureDetector");
	        return mCurrentSelection;
	    }

	    public void invalidateImage(int index) {
	    	Log.e("invalidateImage", "MyGestureDetector");
	        if (index != INDEX_NONE) {
	            mImageBlockManager.invalidateImage(index);
	        }
	    }
	    
	    /**
	     *
	     * @param index <code>INDEX_NONE</code> (-1) means remove selection.
	     */
	    public void setSelectedIndex(int index, boolean center) {
	    	Log.e("setSelectedIndex", "MyGestureDetector");
	        // A selection box will be shown for the image that being selected,
	        // (by finger or by the dpad center key). The selection box can be drawn
	        // in two colors. One color (yellow) is used when the the image is
	        // still being tapped or clicked (the finger is still on the touch
	        // screen or the dpad center key is not released). Another color
	        // (orange) is used after the finger leaves touch screen or the dpad
	        // center key is released.

	        if (mCurrentSelection == index) {
	            return;
	        }
	        // This happens when the last picture is deleted.
	        mCurrentSelection = Math.min(index, mCount - 1);

	        if (mCurrentSelection != INDEX_NONE) {
	            ensureVisible(mCurrentSelection, center);
	        }
	        invalidate();
	    }

	    public void scrollToImage(int index) {
	    	Log.e("scrollToImage", "MyGestureDetector");
	        Rect r = getRectForPosition(index);
	        scrollTo(r.left, 0);
	    }
	    
	    public void scrollToVisible(int index) {
	    	Log.e("scrollToVisible", "MyGestureDetector");
	        Rect r = getRectForPosition(index);
	        
	        int left = getScrollX();
	        int right = getScrollX() + getWidth();
	        if (r.right > right)
	        {
	        	scrollTo (r.right - getWidth(), 0);
	        }
	        else if (r.left < left)
	        {
	        	scrollTo(r.left, 0);
	        }
	    }

	    private void ensureVisible(int pos, boolean center)
	    {
	    	Log.e("ensureVisible", "MyGestureDetector");
	        Rect r = getRectForPosition(pos);
	        int col = pos / mRows;
	        int NumColInRect = getWidth() / mBlockWidth;
	        int left = getScrollX();
	        int right = left + getWidth();
	        if (center)
	        {
	        	 if (col > NumColInRect/2)
			       {
			    	   mScroller = new Scroller(getContext());
				       mScroller.startScroll(getScrollX(), getScrollY(),
				        		   (mBlockWidth * (col - (NumColInRect/2)) - getScrollX()), 0, 200);
	    	           
				       
			       }
	        }else {
	        
	        if (r.right > right)
	        {
	        	 mScroller = new Scroller(getContext());
		         mScroller.startScroll(getScrollX(), getScrollY(),
		        		   r.right - getWidth() - getScrollX(), 0, 200);
		                   
			         
	        }
	        else if (r.left < left) {
	            mScroller = new Scroller(getContext());
	            mScroller.startScroll(getScrollX(), getScrollY(),
	            				r.left - getScrollX(), 0 , 200);
		        }
	        }
	        computeScroll();
	    }
	    public void start()
	    {
	    	Log.e("start", "MyGestureDetector");
	        // These must be set before start().
	        Assert(mLoader != null);
	        Assert(mListener != null);
	        Assert(mDrawAdapter != null);
	        mRunning = true;
	        requestLayout();
	    }
	    
	 // If the the underlying data is changed, for example,
	    // an image is deleted, or the size choice is changed,
	    // The following sequence is needed:
	    //
	    // mGvs.stop();
	    // mGvs.set...(...);
	    // mGvs.set...(...);
	    // mGvs.start();
	    public void stop()
	    {
	    	Log.e("stop", "MyGestureDetector");
	        // Remove the long press callback from the queue if we are going to
	        // stop.
	        mHandler.removeCallbacks(mLongPressCallback);
	        mScroller = null;
	        if (mImageBlockManager != null) {
	            mImageBlockManager.recycle();
	            mImageBlockManager = null;
	        }
	        mRunning = false;
	        mCurrentSelection = INDEX_NONE;
	    }
	    
	    @Override
	    public void onDraw(Canvas canvas)
	    {
	    	Log.e("onDraw", "MyGestureDetector");
	        super.onDraw(canvas);
	        if (!canHandleEvent()) return;
	        mImageBlockManager.doDraw(canvas, getWidth(), getHeight(), getScrollX());
	        paintDecoration(canvas);
	        paintSelection(canvas);
	        moveDataWindow();
	    }
	    @Override
	    public void computeScroll()
	    {
	    	Log.e("computeScroll", "MyGestureDetector");
	        if (mScroller != null) {
	            boolean more = mScroller.computeScrollOffset();
	            scrollTo(mScroller.getCurrX(),0);
	            if (more) {
	                invalidate();  // So we draw again
	            } else {
	                mScroller = null;
	            }
	        } else {
	            super.computeScroll();
	        }
	    }
	    // Return the rectange for the thumbnail in the given position.
	    Rect getRectForPosition(int pos)
	    {
	    	Log.e("getRectForPosition", "MyGestureDetector");
	        int col = pos / mRows;
	    	int row = pos - (col * mRows);
	    	
	    	//following need to be changed
	        int left = mSpec.mLeftEdgePadding
	                + (col * (mSpec.mCellWidth + mSpec.mCellSpacing));
	        int top = row * mBlockHeight;

	        return new Rect(left, top,
	                left + mSpec.mCellWidth + mSpec.mCellSpacing,
	                top + mSpec.mCellHeight + mSpec.mCellSpacing);
	    }
	    
	 // Inverse of getRectForPosition: from screen coordinate to image position.
	    int computeSelectedIndex(float xFloat, float yFloat) {
	    	Log.e("computeSelectedIndex", "MyGestureDetector");
	        int x = (int) xFloat;
	        int y = (int) yFloat;

	        int spacing = mSpec.mCellSpacing;
	        int leftSpacing = mSpec.mLeftEdgePadding;

	        int row = Math.min(mRows - 1, (y - spacing) / 
	        		            (mSpec.mCellHeight + spacing));
	        int col = (getScrollX() + x - spacing - leftSpacing) / 
	                            (mSpec.mCellWidth + spacing);
	        return (col * mRows) + row;	    	
	    }
	    
	    @Override
	    public boolean onTouchEvent(MotionEvent ev) {
	    	Log.e("onTouchEvent", "MyGestureDetector");
	        if (!canHandleEvent()) {
	            return false;
	        }
	        switch (ev.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                mCurrentPressState |= TAPPING_FLAG;
	                invalidate();
	                break;
	            case MotionEvent.ACTION_UP:
	                mCurrentPressState &= ~TAPPING_FLAG;
	                invalidate();
	                break;
	        }
	        mGestureDetector.onTouchEvent(ev);
	        // Consume all events
	        return true;
	    }

	    @Override
	    public void scrollBy(int x, int y) {
	    	Log.e("scrollBy", "MyGestureDetector");
	        scrollTo(getScrollX() + x, getScrollY() + y);
	    }

	    public void scrollTo(float scrollPosition) {
	    	Log.e("scrollTo given position", "MyGestureDetector");
	    	scrollTo(Math.round(scrollPosition * mMaxScrollX), 0 );
	    }
	    
	    @Override
	    public void scrollTo(int x, int y)
	    {
	    	Log.e("scrollTo given coordinate", "MyGestureDetector");
	    	mDrawAdapter.setThumbPosition((x*100)/mMaxScrollX);
	    	x = Math.max(Math.min(mMaxScrollX, x), 0);
	        if (mSpec != null) {
	            mListener.onScroll((float) getScrollX() / mMaxScrollX);
	        }
	        super.scrollTo(x, y);
	    }

	    private boolean canHandleEvent() {
	    	Log.e("canHandleEvent", "MyGestureDetector");
	        return mRunning && mLayoutComplete;
	    }

	    private final Runnable mLongPressCallback = new Runnable() {	    	
	        public void run() {
	        	Log.e("Runnable", "MyGestureDetector");
	            mCurrentPressState &= ~CLICKING_FLAG;
	            showContextMenu();
	        }
	    };
//	    @Override
//	    public boolean onKeyDown(int keyCode, KeyEvent event) {
//	        if (!canHandleEvent()) return false;
//	        int sel = mCurrentSelection;
//	        if (sel != INDEX_NONE) {
//	            switch (keyCode) {
//	                case KeyEvent.KEYCODE_DPAD_RIGHT:
//	                    if (sel != mCount - 1 && (sel % mColumns < mColumns - 1)) {
//	                        sel += 1;
//	                    }
//	                    Log.d("in the onKeyDown", "right");
//	                    break;
//	                case KeyEvent.KEYCODE_DPAD_LEFT:
//	                    if (sel > 0 && (sel % mColumns != 0)) {
//	                        sel -= 1;
//	                    }
//	                    Log.d("in the onKeyDown", "left");
//	                    break;
//	                case KeyEvent.KEYCODE_DPAD_UP:
////	                    if (sel >= mColumns) {
////	                        sel -= mColumns;
////	                    }	                	
//	                	Log.d("in the onKeyDown", "up");
//	                    break;
//	                case KeyEvent.KEYCODE_DPAD_DOWN:
//	                    sel = Math.min(mCount - 1, sel + mColumns);
//	                    Log.d("in the onKeyDown", "down");
//	                    break;
//	                case KeyEvent.KEYCODE_DPAD_CENTER:
//	                    if (event.getRepeatCount() == 0) {
//	                        mCurrentPressState |= CLICKING_FLAG;
//	                        mHandler.postDelayed(mLongPressCallback,
//	                                ViewConfiguration.getLongPressTimeout());
//	                    }
//	                    Log.d("in the onKeyDown", "center");
//	                    break;
//	                default:
//	                	Log.d("in the onKeyDown", "default");
//	                    return super.onKeyDown(keyCode, event);
//	            }
//	        } else {
//	            switch (keyCode) {
//	                case KeyEvent.KEYCODE_DPAD_RIGHT:
//	                case KeyEvent.KEYCODE_DPAD_LEFT:
//	                case KeyEvent.KEYCODE_DPAD_UP:
//	                case KeyEvent.KEYCODE_DPAD_DOWN:
//	                		int startCol = (getScrollX() - mSpec.mCellSpacing) / mBlockWidth;
//	                        int topPos = startCol * mRows;
//	                        Rect r = getRectForPosition(topPos);
//	                        if (r.left < getScrollX())
//	                        {
//	                        	topPos += mRows;
//	                        }
//	                        topPos = Math.min(mCount - 1, topPos);
//	                	    sel = topPos;
//	                	    Log.d("in the onKeyDown"+topPos, "all"+sel+startCol);
//	                    break;
//	                default:
//	                    return super.onKeyDown(keyCode, event);
//	            }
//	        }
//	        setSelectedIndex(sel);
//	        return true;
//	    }
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	    	Log.e("onKeyDown", "MyGestureDetector");
	        if (!canHandleEvent()) return false;
	        int sel = mCurrentSelection;
	        int cur = mCurrentSelection;	        
	        if (sel != INDEX_NONE) {
	            switch (keyCode) {
	                case KeyEvent.KEYCODE_DPAD_DOWN: // deepak KeyEvent.KEYCODE_DPAD_RIGHT:
//	                    if (sel != mCount - 1 && (sel % mColumns < mColumns - 1)) {
//	                        sel += 1;
//	                    }
	                	if  ((sel < mCount) && (sel % mRows < mRows - 1)) {
	                		sel++;
	                	}
	                    Log.e("", "CustomGridView.onKeyDown DOWN mCount="+mCount+",cur="+cur+",sel="+sel);
	                    break;
	                case KeyEvent.KEYCODE_DPAD_UP:  //deepak KeyEvent.KEYCODE_DPAD_LEFT:
//	                    if (sel > 0 && (sel % mColumns != 0)) {
//	                        sel -= 1;
//	                    }
	                	if (sel > 0 && (sel % mRows != 0)){
	                		sel--;
	                	}
	                    Log.e("", "CustomGridView.onKeyDown UP cur="+cur+",sel="+sel);
	                    break;
	                case KeyEvent.KEYCODE_DPAD_LEFT: //deepak KeyEvent.KEYCODE_DPAD_UP:
	                    if (sel >= mRows) {
	                        sel -= mRows;
	                    }	                	
	                	Log.e("", "CustomGridView.onKeyDown LEFT cur="+cur+",sel="+sel);
	                    break;
	                case KeyEvent.KEYCODE_DPAD_RIGHT: //deepak KeyEvent.KEYCODE_DPAD_DOWN:
	                    if ((sel + mRows) < mCount) {
	                    	sel += mRows;
	                    }
	                    //if (selMath.min(mCount - 1, sel + mRows);
	                    Log.e("", "CustomGridView.onKeyDown RIGHT cur="+cur+",sel="+sel);
	                    break;
	                case KeyEvent.KEYCODE_DPAD_CENTER:
	                    if (event.getRepeatCount() == 0) {
	                        mCurrentPressState |= CLICKING_FLAG;
	                        mHandler.postDelayed(mLongPressCallback,
	                                ViewConfiguration.getLongPressTimeout());
	                    }
	                    Log.e("", "GridViewL.onKeyDown CENTER cur="+cur+",sel="+sel);
	                    break;
	                default:
	                	Log.d("in the onKeyDown", "default");
	                    return super.onKeyDown(keyCode, event);
	            }
	        } else {
	            switch (keyCode) {
	                case KeyEvent.KEYCODE_DPAD_RIGHT:
	                case KeyEvent.KEYCODE_DPAD_LEFT:
	                case KeyEvent.KEYCODE_DPAD_UP:
	                case KeyEvent.KEYCODE_DPAD_DOWN:
	                	    int x = getScrollX();
	                		int startCol = (getScrollX() - mSpec.mCellSpacing) / mBlockWidth;
	                        int topPos = startCol * mRows;
	                        Rect r = getRectForPosition(topPos);
	                        if (r.left < getScrollX())
	                        {
	                        	topPos += mRows;
	                        }
	                        topPos = Math.min(mCount - 1, topPos);
	                	    sel = topPos;
	                	    //Log.e("", "CustomGridView.onKeyDown getScrollX="+x+",startCol="+startCol+",topPos="+topPos+",r.left="+r.left+",mRows="+mRows+",mCount="+mCount+",cur="+cur+",sel="+sel);
	                	    //Log.d("in the onKeyDown"+topPos, "all"+sel+startCol);
	                    break;
	                default:
	                    return super.onKeyDown(keyCode, event);
	            }
	        }
	        setSelectedIndex(sel, false);
	        
	        return true;
	    }

	    @Override
	    public boolean onKeyUp(int keyCode, KeyEvent event) {
	    	Log.e("onKeyUp", "MyGestureDetector");
	        if (!canHandleEvent()) return false;

	        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
	            mCurrentPressState &= ~CLICKING_FLAG;
	            invalidate();

	            // The keyUp doesn't get called when the longpress menu comes up. We
	            // only get here when the user lets go of the center key before the
	            // longpress menu comes up.
	            mHandler.removeCallbacks(mLongPressCallback);

	            // open the photo
	            mListener.onImageClicked(mCurrentSelection);
	            return true;
	        }
	        return super.onKeyUp(keyCode, event);
	    }
	    
	    private void paintDecoration(Canvas canvas) {
	    	Log.e("paintDecoration", "MyGestureDetector");
	        if (!mDrawAdapter.needsDecoration()) return;

	        // Calculate visible region according to scroll position.
	        
	        int startCol = (getScrollX() - mSpec.mCellSpacing) / mBlockWidth;
	        int endCol = (getScrollX() + getWidth() - mSpec.mCellSpacing - 1)
	        				/ mBlockWidth + 1;
	        // Limit startRow and endRow to the valid range.
	        // Make sure we handle the mRows == 0 case right.

	        startCol = Math.max(Math.min(startCol, mColumns - 1), 0);
	        endCol = Math.max(Math.min(endCol, mColumns), 0);
	        
	        int startIndex = startCol * mRows;
	        int endIndex = Math.min(endCol * mRows, mCount);
	        
	        
	        int xPos = mSpec.mLeftEdgePadding + startCol * mBlockWidth;
	        int yPos = mSpec.mCellSpacing;
	        int off = 0;
	        for (int i = startIndex; i < endIndex; i++) {
	            IImage image = mAllImages.getImageAt(i);

	            mDrawAdapter.drawDecoration(canvas, image, xPos, yPos,
	                    mSpec.mCellWidth, mSpec.mCellHeight);

	            // Calculate next position
	            off += 1;
	            if (off == mRows) {
	                xPos += mBlockWidth;//mSpec.mLeftEdgePadding;
	                yPos = mSpec.mCellSpacing;// + startRow * mBlockHeight;
	                off = 0;
	            } else {
	                yPos += mSpec.mCellHeight + mSpec.mCellSpacing;
	            }
	        }
	    }
//	    private void paintSelection(Canvas canvas) {
//	        if (mCurrentSelection == INDEX_NONE) return;
//
//	        int col = mCurrentSelection / mRows;
//	        int row = mCurrentSelection - (col * mRows);
//
//	        int spacing = mSpec.mCellSpacing;
//	        int leftSpacing = mSpec.mLeftEdgePadding;
//	        int xPos = leftSpacing + (col * (mSpec.mCellWidth + spacing));
//	        int yTop = spacing + (row * mBlockHeight);
//
//	        int type = OUTLINE_SELECTED;
//	        if (mCurrentPressState != 0) {
//	            type = OUTLINE_PRESSED;
//	        }
//	        canvas.drawBitmap(mOutline[type], xPos, yTop, null);
//	    }
	    
	    private void paintSelection(Canvas canvas) {
	    	Log.e("paintSelection", "MyGestureDetector");
	    	
	        if (mCurrentSelection == INDEX_NONE) return;

	        int row = mCurrentSelection % mRows;             //deepak
	        int col = mCurrentSelection / mRows;  //deepak
	        int spacing = mSpec.mCellSpacing;
	        int leftSpacing = mSpec.mLeftEdgePadding;
	        int xPos = leftSpacing + (col * (mSpec.mCellWidth + spacing));
	        int yTop = spacing + (row * mBlockWidth);
	        //Log.e("","GridViewSpecial.#paintSelection row="+row+",col"+col+",spacing="+spacing+",leftspacing="+leftSpacing+",cellwidth="+mSpec.mCellWidth+",blockheight="+mBlockHeight+",xPos="+xPos+",yTop="+yTop);
	        int type = OUTLINE_SELECTED;
	        if (mCurrentPressState != 0) {
	            type = OUTLINE_PRESSED;
	        }
	        canvas.drawBitmap(mOutline[type], xPos, yTop, null);
	    }
	    
	    public void trackThumbScroll(int deltaX) {
	    	
	    	Log.e("trackThumbScroll","deltaX="+deltaX);
	    	//setSelectedIndex(progress*mCount/100);
	    	scrollBy(deltaX*mMaxScrollX/100,0);
	    	
	    }
	    
}

