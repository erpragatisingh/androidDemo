package com.kodak.relive.viewbydate;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;
import android.widget.Scroller;

public class HorizontalScrollViewEx extends HorizontalScrollView {

	private Context mContext;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private VelocityTracker mVelocityTracker = null;
	private static final String tag = "Scroll";
	float xPos, xPosfinal;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		this.setVisibility(0);
		if (mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(ev);

		// on touch down event get the current X position.
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			xPos = ev.getX();

		} else if (ev.getAction() == MotionEvent.ACTION_UP) {
			// on touch up event calculate the current X position.
			xPosfinal = ev.getX();

			mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			int initialVelocity = (int) mVelocityTracker.getXVelocity();
			if ((Math.abs(initialVelocity) > mMinimumVelocity)
					&& super.getChildCount() > 0) {
				if (xPos < xPosfinal) {
		    		this.pageScroll(this.FOCUS_LEFT);

				} else {
					this.pageScroll(this.FOCUS_RIGHT);

				}
			}
		} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			Log.d(tag, "Action Move Event");
		}
		return true;
	}

	@Override
	public boolean pageScroll(int direction) {
		// TODO Auto-generated method stub
		return super.pageScroll(direction);
	}

	public HorizontalScrollViewEx(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	public HorizontalScrollViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	public HorizontalScrollViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

}
