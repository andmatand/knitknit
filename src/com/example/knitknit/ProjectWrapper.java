/*
 * Copyright 2012 Andrew Anderson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     1. Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 * 
 *     2. Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.knitknit;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import java.util.Timer;
import java.util.TimerTask;

public class ProjectWrapper extends RelativeLayout {
    private static final String TAG = "knitknit-CountingLandWrapper";

    //private Context mContext;
    private Project mProject;
    protected MotionEvent mTouchDown = null;
    protected MotionEvent mTouchUp = null;
    private Timer mTouchTimer;
    private boolean mCounterIsHighlighted = false;

    public ProjectWrapper(Context context) {
        super(context);

        this.setClickable(true);
    }

    public ProjectWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setClickable(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.w(TAG, "intercepted down event");
                mTouchDown = MotionEvent.obtain(event);
                Log.w(TAG, "Touch y: " + mTouchDown.getY());
                Log.w(TAG, "Raw y: " + mTouchDown.getRawY());
                mCounterIsHighlighted = false;

                // Set a timer callback to check if the touch is still being
                // held down
                mTouchTimer = new Timer();
                mTouchTimer.scheduleAtFixedRate(
                        new TimerTask() {
                            public void run() {
                                checkTouch();
                            }
                        }, 250, 250);

                // Intercept the event
                return true;
        }

        // Otherwise let the event pass through to the children
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Exit if we haven't had a touch down event from onInterceptTouchEvent
        if (mTouchDown == null) return true;

        Log.w(TAG, "in onTouchEvent");

        switch(event.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.w(TAG, "got up event");

                // If a counter is not currently highlighted
                if (!mCounterIsHighlighted) {
                    // Increase all counters
                    mProject.increase();
                } else {
                    // Redraw all counters
                    //mProject.refreshCounters();
                }

                // Reset the touchDown event
                mTouchDown = null;

                // Cancel the timer
                mTouchTimer.cancel();

                return false;
        }

        return true;
    }

    public void checkTouch() {
        Log.w(TAG, "in checkTouch()");
        if (mTouchDown != null) {
            Log.w(TAG, "pushing counter...");

            if (mCounterIsHighlighted == false) {
                mProject.highlightCounter(mTouchDown.getRawY());
                mCounterIsHighlighted = true;
            } else {
                mProject.longClickCounter(mTouchDown.getRawY());
                mTouchDown = null;

                Log.w(TAG, "canceling timer");
                mTouchTimer.cancel();
            }
        } else {
            // The touch is no longer being held down; stop the timer
            Log.w(TAG, "canceling timer");
            mTouchTimer.cancel();

            // Un-highlight all counters
            //mProject.refreshCounters();
        }
    }

    public void setProject(Project project) {
        mProject = project;
    }
}
