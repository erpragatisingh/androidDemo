/*
 * Copyright (C) 2006 The Android Open Source Project
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
import android.util.AttributeSet;

/**
 * ThumbScroller is derived from AbsThumbScroller class which has actual implementation of
 * Dragable Thumb. This class provide an interface to track the user event on user actions.
 */
public class ThumbScroller extends AbsThumbScroller {

    /**
     * A callback that notifies clients when the progress level has been
     * changed. This includes changes that were initiated by the user through a
     * touch gesture or arrow key/trackball as well as changes that were initiated
     * programmatically.
     */
    public interface OnThumbChangeListener {
        
        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         * 
         * @param scroller The ThumbScroller whose progress has changed
         * @param progress The current progress level. This will be in the range 0..max 
         * (The default value for max is 100.)        
         */
        void onProgressChanged(ThumbScroller scroller, int progress);
    
        /**
         * Notification that the user has started a touch gesture. Clients may want to use this
         * to disable advancing the Scroller. 
         * @param scroller The ThumbScroller in which the touch gesture began
         */
        void onStartTrackingTouch(ThumbScroller scroller);
        
        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this
         * to re-enable advancing the scroller. 
         * @param scroller The ThumbScroller in which the touch gesture began
         */
        void onStopTrackingTouch(ThumbScroller scroller);
    }

    private OnThumbChangeListener mOnThumbScrollerChangeListener;
    
    public ThumbScroller(Context context) {
        this(context, null);
    }
    
    public ThumbScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbScroller(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    void onProgressRefresh(float scale) {
        super.onProgressRefresh(scale);

        if (mOnThumbScrollerChangeListener != null) {
            mOnThumbScrollerChangeListener.onProgressChanged(this, getProgress());
        }
    }

    /**
     * Sets a listener to receive notifications of changes to the Scroller's progress level. Also
     * provides notifications of when the user starts and stops a touch gesture within the Scroller.
     * 
     * @param l The ThumbScroller notification listener 
     */
    public void setOnThumbScrollerChangeListener(OnThumbChangeListener l) {
        mOnThumbScrollerChangeListener = l;
    }
    
    @Override
    void onStartTrackingTouch() {
        if (mOnThumbScrollerChangeListener != null) {
            mOnThumbScrollerChangeListener.onStartTrackingTouch(this);
        }
    }
    
    @Override
    void onStopTrackingTouch() {
        if (mOnThumbScrollerChangeListener != null) {
            mOnThumbScrollerChangeListener.onStopTrackingTouch(this);
        }
    }
    
}
