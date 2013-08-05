package com.example.knitknit;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

public class ProjectWrapper extends RelativeLayout implements OnTouchListener {
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
        //this.setClickable(true);
        setOnTouchListener(this);
        mRespondToTouch = true;
    }

    public void setRespondToTouch(boolean tf) {
        mRespondToTouch = tf;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.w(TAG, "in onInterceptTouchEvent()");

        if (!mRespondToTouch) {
            // Consume the event
            return true;
        }

        return onTouch(null, event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.w(TAG, "in onTouch()");
        Log.w(TAG, "pointer count: " + event.getPointerCount() +
                   ", event action masked: " + event.getActionMasked());

        if (!mRespondToTouch) {
            // Consume the event
            return true;
        }

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.w(TAG, "down");
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

        // If the touch was directly on this view
        if (v != null) {
            // Consume the event
            return true;
        } else {
            // Let the event pass on to the children
            return false;
        }
    }

    public void setDoneWithTouch(boolean tf) {
        mDoneWithTouch = tf;
    }

    public void setProject(Project project) {
        mProject = project;
    }
}
