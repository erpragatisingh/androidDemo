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

package com.kodak.relive.common.ThumbScrollBar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kodak.relive.R;

/**
 * A AbsThumbScroller class which draws the scroll track and Dragable thumb. 
 * The user can touch the thumb and drag left or right to set the current 
 * progress level or can tap on scroll track to move thumb to that position.
 * The view also changes accordingly. 
 * If view is scrolled using fling or gesture event then thumb is also reset accordingly. 
 */

public abstract class AbsThumbScroller extends View {

	private static final int MAX_LEVEL = 10000;
	private static final int ANIMATION_RESOLUTION = 200;

	int mMinWidth; //minimum track width
	int mMaxWidth; //maximum track width
	int mMinHeight; //Minimum track height
	int mMaxHeight; //Maximum track height
	private int mProgress; //Current progress position
	private int mMax; // Maximum percent of progress

	private int mThumbOffset; //Thumb offset position
	public RefreshProgressRunnable mRefreshProgressRunnable;

	private Transformation mTransformation;
	private AlphaAnimation mAnimation;
	private Drawable mProgressDrawable; //Progress Bar drawable 
	private Drawable mThumbDrawable; //Thumb drawable


	private long mUiThreadId; // used to store thread id
	private long mLastDrawTime;

	private Bitmap mProgressBitmap;
	private BitmapDrawable mProgressBitmapDrawable;
	private Bitmap mThumbBitamp;
	private BitmapDrawable mThumbBitampDrawable;

	public AbsThumbScroller(Context context) {
		this(context, null);
	}

	public AbsThumbScroller(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AbsThumbScroller(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	// Initialize the progress bar parameters.	
		initThumbScroller();

		// set Thumb Drawable.
		mThumbBitamp = BitmapFactory.decodeResource(getResources(),
				R.drawable.thumb);
		mThumbBitampDrawable = new BitmapDrawable(mThumbBitamp);
		if (mThumbBitampDrawable != null) {
			setThumbDrawable(mThumbBitampDrawable);
		}		
		
		// Set progress Track Drawable.
		mProgressBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.track);
		mProgressBitmapDrawable = new BitmapDrawable(mProgressBitmap); 		
		if (mProgressBitmapDrawable != null) {
			setProgressDrawable(mProgressBitmapDrawable);
		}		
		
		mUiThreadId = Thread.currentThread().getId();
	}

	/**
	 * Get current progress drawable.
	 */	
	public Drawable getProgressDrawable() {
		return mProgressDrawable;
	}

	 /**
     * Provide functionality to set current progress drawable.
     * @param thumbOffset The offset amount in pixels.
     */	
	public void setProgressDrawable(Drawable d) {
		mProgressDrawable = d;
		postInvalidate();
	}
	
	/**
     * Set maximum scroll limit
     * @param max maximum limit
     */	
	public synchronized void setMax(int max) {
		if (max < 0) {
			max = 0;
		}
		if (max != mMax) {
			mMax = max;
			postInvalidate();

			if (mProgress > max) {
				mProgress = max;
				refreshProgress(mProgress);
			}
		}
	}
	/**
	 * Initialize scrolling parameters
	 */	
	private void initThumbScroller() {
		mMax = 100;
		mProgress = 0;
		mMinWidth = 10;
		mMaxWidth = 10;
		mMinHeight = 5;
		mMaxHeight = 20;
	}
	
    /**
     * Sets the thumb that will be drawn at the end of the progress meter within the ProgressBar.
     * If the thumb is a valid drawable (i.e. not null), half its width will be
     * used as the new thumb offset (@see #setThumbOffset(int)).
     * @param thumb Drawable representing the thumb
     */
	
	public void setThumbDrawable(Drawable thumb) {
		mThumbOffset = (int) thumb.getIntrinsicWidth() / 2;
		mThumbDrawable = thumb;
		invalidate();
	}
	
    /**
     * Get current thumb offset position.   
     */
	public int getThumbOffset() {
		return mThumbOffset;
	}

	/**
     * Get Minimum height of Track.   
     */	
	public int getTrackMinHeight() {
		return mMinHeight;
	}
	
	/**
     * Sets the track minimum height
     * @param val The minimum height value.
     */	
	public void setTrackMinHeight(int val) {
		mMinHeight = val;
	}

	/**
     * Get Maximum height value of track.   
     */		
	public int getTrackMaxHeight() {
		return mMaxHeight;
	}

	/**
     * Sets the track maximum height
     * @param val The maximum height value.
     */	
	public void setTrackMaxHeight(int val) {
		mMaxHeight = val;
	}

	/**
     * Get Minimum width value of track.  
     */		
	public int getTrackMinWidth() {
		return mMinWidth;
	}

	/**
     * Sets the track minimum width
     * @param val The minimum width value.
     */	
	public void setTrackMinWidth(int val) {
		mMinWidth = val;
	}

	/**
     * Get Maximum width value of track. 
     */		
	public int getTrackMaxWidth() {
		return mMaxWidth;
	}

	/**
     * Sets the track maximum width
     * @param val The maximum width value.
     */	
	public void setTrackMaxWidth(int val) {
		mMaxWidth = val;
	}

    /**
     * Sets the thumb offset that allows the thumb to extend out of the range of
     * the track.
     * @param thumbOffset The offset amount in pixels.
     */	
	public void setThumbOffset(int thumbOffset) {
		mThumbOffset = thumbOffset;
		invalidate();
	}
	
	/**
     * Returm current progress position. 
     */	
	public synchronized int getProgress() {
		return mProgress;
	}
	
	/**
     * Returm maximum progress percentage. 
     */
	public synchronized int getMax() {
		return mMax;
	}
	
	/**
     * Sets current progress percentage
     * @param progress current progress percentage.
     */	
	public synchronized void setProgress(int progress) {
		if (progress < 0) {
			progress = 0;
		}
		if (progress > mMax) {
			progress = mMax;
		}
		if (progress != mProgress) {
			mProgress = progress;
			refreshProgress(mProgress);
		}
	}
	

	@Override
	public void postInvalidate() {
		super.postInvalidate();
	}
	
	/**
     * Track the touch event
     * @param event current touch event.
     */	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setPressed(true);
			onStartTrackingTouch();
			trackTouchEvent(event);
			break;

		case MotionEvent.ACTION_MOVE:
			trackTouchEvent(event);
			attemptClaimDrag();
			break;

		case MotionEvent.ACTION_UP:
			trackTouchEvent(event);
			onStopTrackingTouch();
			setPressed(false);
			// ProgressBar doesn't know to repaint the thumb drawable
			// in its inactive state when the touch stops (because the
			// value has not apparently changed)
			invalidate();
			break;

		case MotionEvent.ACTION_CANCEL:
			onStopTrackingTouch();
			setPressed(false);
			invalidate(); // see above explanation
			break;
		}
		return true;
	}
	
	/**
     * Calls when progress position has been changed. It sets Thumb position accordingly.
     * @param scale progress percentage.
     */		
	void onProgressRefresh(float scale) {
		Drawable thumb = mThumbDrawable;
		if (thumb != null) {
			setThumbPos(getWidth(), thumb, scale, Integer.MIN_VALUE);
			/*
			 * Since we draw translated, the drawable's bounds that it signals
			 * for invalidation won't be the actual bounds we want invalidated,
			 * so just invalidate this whole view.
			 */
			invalidate();
		}
	}	
	
	/**
	 * This is called when the user has started touching this widget.
	 */
	void onStartTrackingTouch() {
	}

	/**
	 * This is called when the user either releases his touch or the touch is
	 * canceled.
	 */
	void onStopTrackingTouch() {
	}

	/**
	 * Called when the user changes the Scroller's progress 
	 */
	void onKeyChange() {
	}
	
	/**
     * Verify that if given drawable is authentic drawable
     * @param who Drawable passed.
     */
	protected boolean verifyDrawable(Drawable who) {
		return who == mThumbDrawable || who == mProgressDrawable
				|| super.verifyDrawable(who);
	}

	
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Drawable d = mProgressDrawable;
		if (d != null) {
			// Translate canvas so a indeterminate circular progress bar with
			// padding rotates properly in its animation
			canvas.save();
			canvas.translate(getPaddingLeft(), getPaddingTop());
			long time = getDrawingTime();
			if (mAnimation != null) {
				mAnimation.getTransformation(time, mTransformation);
				float scale = mTransformation.getAlpha();
				try {
					d.setLevel((int) (scale * MAX_LEVEL));
				} finally {
				}
				if (SystemClock.uptimeMillis() - mLastDrawTime >= ANIMATION_RESOLUTION) {
					mLastDrawTime = SystemClock.uptimeMillis();
					postInvalidateDelayed(ANIMATION_RESOLUTION);
				}
			}
			d.draw(canvas);
		}
		if (mThumbDrawable != null) {
			canvas.save();
			// Translate the padding. For the x, we need to allow the thumb to
			// draw in its extra space
			canvas.translate(getPaddingLeft() - mThumbOffset, getPaddingTop());
			mThumbDrawable.draw(canvas);
			canvas.restore();
		}
	}
	
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec) {
		Drawable d = this.getProgressDrawable();
		// Drawable d = mThumb;

		int thumbHeight = mThumbDrawable == null ? 0 : mThumbDrawable
				.getIntrinsicHeight();
		int dw = 0;
		int dh = 0;
		if (d != null) {
			dw = Math.max(getWidth(), Math.min(getWidth(), d
					.getIntrinsicWidth()));
			dh = Math.max(getHeight(), Math.min(getHeight(), d
					.getIntrinsicHeight()));
			dh = Math.max(thumbHeight, dh);
		}
		dw += getPaddingLeft() + getPaddingRight();
		dh += getPaddingTop() + getPaddingBottom();

		setMeasuredDimension(resolveSize(dw, widthMeasureSpec), resolveSize(dh,
				heightMeasureSpec));
	}


	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Drawable d = getProgressDrawable();
		Drawable thumb = mThumbDrawable;
		int thumbHeight = thumb == null ? 0 : thumb.getIntrinsicHeight();
		// The max height does not incorporate padding, whereas the height
		// parameter does
		int trackHeight = Math.min(getHeight(), h - getPaddingTop()
				- getPaddingBottom());

		int max = getMax();
		float scale = max > 0 ? (float) getProgress() / (float) max : 0;

		if (thumbHeight > trackHeight) {
			if (thumb != null) {
				setThumbPos(w, thumb, scale, 0);
			}
			int gapForCenteringTrack = (thumbHeight - trackHeight) / 2;
			if (d != null) {
				// Canvas will be translated by the padding, so 0,0 is where we
				// start drawing
				d.setBounds(0, gapForCenteringTrack, w - getPaddingRight()
						- getPaddingLeft(), h - getPaddingBottom()
						- gapForCenteringTrack - getPaddingTop());
			}
		} else {
			if (d != null) {
				// Canvas will be translated by the padding, so 0,0 is where we
				// start drawing
				d.setBounds(0, 0, w - getPaddingRight() - getPaddingLeft(), h
						- getPaddingBottom() - getPaddingTop());
			}
			int gap = (trackHeight - thumbHeight) / 2;
			if (thumb != null) {
				setThumbPos(w, thumb, scale, gap);
			}
		}
	}

	/**
     * used to set thumb postion
     * @param w Width of scroll track.
     * @param thumb Thumb Drawable.
     * @param scale Scalling parameter for progress.
     * @param gap gap between thumb and track height.
     */
	private void setThumbPos(int w, Drawable thumb, float scale, int gap) {
		int available = w - getPaddingLeft() - getPaddingRight();
		int thumbWidth = thumb.getIntrinsicWidth();
		int thumbHeight = thumb.getIntrinsicHeight();
		available -= thumbWidth;

		// The extra space for the thumb to move on the track
		available += mThumbOffset * 2;

		int thumbPos = (int) (scale * available);

		int topBound, bottomBound;
		if (gap == Integer.MIN_VALUE) {
			Rect oldBounds = thumb.getBounds();
			topBound = oldBounds.top;
			bottomBound = oldBounds.bottom;
		} else {
			topBound = gap;
			bottomBound = gap + thumbHeight;
		}

		// Canvas will be translated, so 0,0 is where we start drawing
		thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound);
	}

	/**
     * Track the touch event and set current progress position accordingly
     * @param event touch event 
     */	
	private void trackTouchEvent(MotionEvent event) {
		final int width = getWidth();
		final int available = width - getPaddingLeft() - getPaddingRight();
		int x = (int) event.getX();
		float scale;
		float progress = 0;
		if (x < getPaddingLeft()) {
			scale = 0.0f;
		} else if (x > width - getPaddingRight()) {
			scale = 1.0f;
		} else {
			scale = (float) (x - getPaddingLeft()) / (float) available;
		}
		
		final int max = getMax();
		progress += scale * max;
		setProgress((int) progress);
	}

	/**
	 * Tries to claim the user's drag motion, and requests disallowing any
	 * ancestors from stealing events in the drag.
	 */
	private void attemptClaimDrag() {
		if (getParent() != null) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
	}

	
	private class RefreshProgressRunnable implements Runnable {

		private int mProgress;

		RefreshProgressRunnable(int progress) {
			mProgress = progress;
		}

		public void run() {
			doRefreshProgress(mProgress);
			// Put ourselves back in the cache when we are done
			mRefreshProgressRunnable = this;
		}

		public void setup(int progress) {
			mProgress = progress;
		}

	}

	/**
     * Refresh the current progress position
     * @param progress progress percentage
     */	
	private synchronized void doRefreshProgress(int progress) {
		float scale = mMax > 0 ? (float) progress / (float) mMax : 0;
		final Drawable d = mProgressDrawable;
		if (d != null) {
			final int level = (int) (scale * MAX_LEVEL);
			mProgressDrawable.setLevel(level);
		} else {
			invalidate();
		}
		onProgressRefresh(scale);
	}

	/**
     * Refresh the current progress position
     * @param progress progress percentage
     */		
	private synchronized void refreshProgress(int progress) {
		if (mUiThreadId == Thread.currentThread().getId()) {
			doRefreshProgress(progress);
		} else {
			RefreshProgressRunnable r;
			if (mRefreshProgressRunnable != null) {
				// Use cached RefreshProgressRunnable if available
				r = mRefreshProgressRunnable;
				// Uncache it
				mRefreshProgressRunnable = null;
				r.setup(progress);
			} else {
				// Make a new one
				r = new RefreshProgressRunnable(progress);
			}
			post(r);
		}
	}	



}
