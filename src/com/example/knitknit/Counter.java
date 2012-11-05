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
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Counter implements OnLongClickListener {
    private static final String TAG = "knitknit-Counter";

    // Database Fields
    private long mId;
    private long mProjectId;
    private String mName;
    private long mValue;
    private boolean mCountUp;
    private boolean mPatternEnabled;
    private long mPatternLength;
    private long mNumRepeats;

    // Views
    private LinearLayout mWrapper;
    private TextView mRepeatsView;
    private FittedTextView mValueView;

    // UI-Related
    private boolean mPressed = false;
    private Resources mResources;

    // Methods
    public Counter(long id, long projectId, String name, long value, boolean countUp,
                   boolean patternEnabled, long patternLength, long numRepeats, Context context) {
        mId = id;
        mProjectId = projectId;
        mName = name;
        mValue = value;
        mCountUp = countUp;
        mPatternEnabled = patternEnabled;
        mPatternLength = patternLength;
        mNumRepeats = numRepeats;

        // Inflate a new copy of the counter layout
        LayoutInflater inflater;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWrapper = (LinearLayout) inflater.inflate(R.layout.counter, null, false);
        mValueView = (FittedTextView) mWrapper.getChildAt(0);
        mRepeatsView = (TextView) mWrapper.getChildAt(1);

        // Get the resources (for setting text colors)
        mResources = context.getResources();

        mWrapper.setOnLongClickListener(this);
    }

    private void addToValue(int amount) {
        mValue += amount;
        if (mPatternEnabled) {
            if (mValue > mPatternLength - 1) {
                mNumRepeats++;
                mValue = 0;
            }
            else if (mValue < 0) {
                mNumRepeats++;
                mValue = mPatternLength - 1;
            }
        }

        Log.w(TAG, "value: " + mValue);
    }

    public void decrease() {
        // Subtract or add 1, depending on countUp setting
        if (mCountUp) {
            addToValue(-1);
        } else {
            addToValue(1);
        }

        refreshViews();
    }

    public int getHeight() {
        return mWrapper.getHeight();
    }

    public LinearLayout getWrapper() {
        return mWrapper;
    }

    public int getContentWidth() {
        int width;
        width = mValueView.getWidth();

        if (mRepeatsView.getVisibility() == View.VISIBLE) {
            width += mRepeatsView.getWidth();
        }

        return width;
    }

    public boolean getCountUp() {
        return mCountUp;
    }

    private long getDisplayValue() {
        return (CountingLand.getZeroMode() ? mValue : mValue + 1);
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public long getNumRepeats() {
        return mNumRepeats;
    }

    public boolean getPatternEnabled() {
        return mPatternEnabled;
    }

    public long getPatternLength() {
        return mPatternLength;
    }

    public long getValue() {
        return mValue;
    }

    public int getY() {
        // Create an array of two integers
        int[] xy = new int[2];

        // Fill the array the the x and y coordinates
        mWrapper.getLocationOnScreen(xy);

        // Return the y coordinate
        return xy[1];
    }

    public void highlight() {
        // Highlight the counter to show user that a longClick is imminent

        // Use the post method so it runs in the main UI thread intead
        // of the timer thread
        mWrapper.post(new Runnable() {
            public void run() {
                // Set the color
                mValueView.setTextColor(mResources.getColor(R.color.counter_pressed));
            }
        });

        mPressed = true;
    }

    public void increase() {
        // Add or subtract 1, depending on countUp setting
        if (mCountUp) {
            addToValue(1);
        } else {
            addToValue(-1);
        }

        refreshViews();
    }

    public void refreshViews() {
        // Use the post method so it runs in the main UI thread intead
        // of the timer thread
        mWrapper.post(new Runnable() {
            public void run() {
                if (mPatternEnabled) {
                    mRepeatsView.setVisibility(View.VISIBLE);
                    mValueView.setGravity(Gravity.RIGHT);
                }
                else {
                    mRepeatsView.setVisibility(View.GONE);
                    mValueView.setGravity(Gravity.CENTER);
                }

                // Update the TextViews with the counter's current values
                mValueView.setText(String.valueOf(getDisplayValue()));
                mRepeatsView.setText(String.valueOf(mNumRepeats));

                // Set the color
                mValueView.setTextColor(mResources.getColor(R.color.counter));
            }
        });
    }


    // OnLongClickListener Callbacks
    public boolean onLongClick(View v) {
        highlight();
        ((ProjectWrapper) v.getParent().getParent()).setDoneWithTouch(true);

        return true;
    }
}
