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
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

public class FittedTextView extends TextView {
    private static final String TAG = "knitknit-FittedTextView";

    private float maxTextSizePx;

    public FittedTextView(Context context) {
        super(context);
        init();
    }

    public FittedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FittedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        maxTextSizePx = getTextSize(); 
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != oldw || h != oldh) {
            refitText(getText().toString(), w, h);
        }
    }

    @Override
    protected void onTextChanged (CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        // IF the length changed
        if (lengthAfter != lengthBefore) {
            refitText(text.toString(), getWidth(), getHeight());
        }
    }

    private void refitText(String text, int w, int h) {
        Log.w(TAG, "in refitText()");

        Log.w(TAG, "padding left: " + getPaddingLeft());
        w = w - getPaddingLeft() - getPaddingRight();
        w = w - (w / 5);
        h = h - getPaddingTop() - getPaddingBottom();
        h = h - (h / 4);

        if (w <= 0 || h <= 0) return;

        Log.w(TAG, "available width: " + w);
        Log.w(TAG, "available height: " + h);

        TextPaint textPaintClone = new TextPaint();
        textPaintClone.set(getPaint());

        float testSize = maxTextSizePx;
        textPaintClone.setTextSize(testSize);

        int loops = 0;
        Rect testBounds = new Rect();
        String testString = "";
        for (int i = 0; i < text.length(); i++) {
            testString += "0";
        }
        while (true) {
            loops++;

            textPaintClone.getTextBounds(testString, 0, testString.length(), testBounds);
            Log.w(TAG, "test width: " + testBounds.width());
            Log.w(TAG, "test height: " + testBounds.height());

            if (testBounds.width() > w || testBounds.height() > h) {
                testSize -= 1;
            }
            else {
                break;
            }

            textPaintClone.setTextSize(testSize);
        }
        Log.w(TAG, "loops: " + loops);

        setTextSize(TypedValue.COMPLEX_UNIT_PX, testSize);
    }
}
