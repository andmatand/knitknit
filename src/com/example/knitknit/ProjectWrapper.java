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

public class ProjectWrapper extends RelativeLayout {
    private static final String TAG = "knitknit-ProjectWrapper";

    private boolean mDoneWithTouch;
    private Project mProject;
    private boolean mRespondToTouch;

    public ProjectWrapper(Context context) {
        super(context);
        init();
    }

    public ProjectWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setClickable(true);
        mRespondToTouch = true;
    }

    public void setRespondToTouch(boolean tf) {
        mRespondToTouch = tf;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.w(TAG, "pointer count: " + event.getPointerCount() +
                   ", event action: " + event.getActionMasked());

        if (!mRespondToTouch) {
            return false;
        }

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDoneWithTouch = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (!mDoneWithTouch) {
                    if (event.getPointerCount() == 2) {
                        mProject.decrease();

                        // Ignore the touch event that will occur when the other finger comes up
                        mDoneWithTouch = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mDoneWithTouch) {
                    mProject.increase();
                }
                break;
        }

        // Otherwise let the event pass through to the children
        return false;
    }

    public void setDoneWithTouch(boolean tf) {
        mDoneWithTouch = tf;
    }

    public void setProject(Project project) {
        mProject = project;
    }
}
