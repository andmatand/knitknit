package com.example.knitknit;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
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

    private Project mProject;

    // Views
    private LinearLayout mWrapper;
    private TextView mRepeatsView;
    private TextView mValueView;

    // UI-Related
    private Context mContext;
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

        mContext = context;

        // Get the resources (for setting text colors)
        mResources = mContext.getResources();
    }

    private void addToValue(int amount) {
        mValue += amount;
        if (mPatternEnabled) {
            if (mValue > mPatternLength - 1) {
                if (mCountUp) {
                    mNumRepeats++;
                } else {
                    mNumRepeats--;
                }
                mValue = 0;
            } else if (mValue < 0) {
                if (!mCountUp) {
                    mNumRepeats++;
                } else {
                    mNumRepeats--;
                }
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

    public void increase() {
        // Add or subtract 1, depending on countUp setting
        if (mCountUp) {
            addToValue(1);
        } else {
            addToValue(-1);
        }

        refreshViews();
    }

    public void inflate(ViewGroup root) {
        // Inflate a new copy of the counter layout
        LayoutInflater inflater;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWrapper = (LinearLayout) inflater.inflate(R.layout.counter, root, false);
        mValueView = (TextView) mWrapper.getChildAt(0);
        mRepeatsView = (TextView) mWrapper.getChildAt(1);

        int orientation = mResources.getConfiguration().orientation;

        // Adjust the layout based on the screen orientation
        LinearLayout.LayoutParams params;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        } else {
            params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        params.gravity = Gravity.CENTER;
        params.weight = 1;
        mWrapper.setOrientation(0); // Horizontal
        mWrapper.setLayoutParams(params);

        mWrapper.setOnLongClickListener(this);
    }

    public void refreshViews() {
        // DEBUG
        //mPatternEnabled = true;

        if (mPatternEnabled) {
            mRepeatsView.setVisibility(View.VISIBLE);
            //mValueView.setGravity(Gravity.RIGHT);
        } else {
            mRepeatsView.setVisibility(View.GONE);
            //mValueView.setGravity(Gravity.CENTER);
        }

        resizeText();

        // Update the TextViews with the counter's current values
        mValueView.setText(String.valueOf(getDisplayValue()));
        mValueView.requestLayout();
        mValueView.append("\uFEFF"); // Prevent dumb ICS bug with TextView resizing
        mRepeatsView.setText(String.valueOf(mNumRepeats));

        // Set the color of the value view
        mValueView.setTextColor(mResources.getColor(mWrapper.isSelected() ?
                                                    R.color.counter_selected :
                                                    R.color.counter));
    }

    private void resizeText() {
        int numCounters = mProject.getCounters().size();
        int length = String.valueOf(getDisplayValue()).length();

        float dp = 225 / numCounters;

        if (length > 2 && numCounters == 1) {
            dp -= dp / length;
        }

        mValueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dp);
        mRepeatsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dp / 4);

        // Get the screen's density scale
        final float scale = mResources.getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        int paddingTop = (int) ((dp / 1.5) * scale + 0.5f);

        // Set the paddingTop of the repeats view
        mRepeatsView.setPadding(0, paddingTop, 0, 0);
    }

    public void setCountUp(boolean countUp) {
        this.mCountUp = countUp;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setPatternEnabled(boolean enabled) {
        this.mPatternEnabled = enabled;
    }

    public void setPatternLength(long length) {
        this.mPatternLength = length;
    }

    public void setProject(Project project) {
        mProject = project;
    }

    // OnLongClickListener Callbacks
    public boolean onLongClick(View v) {
        ProjectWrapper projectWrapper = (ProjectWrapper) v.getParent().getParent();
        projectWrapper.setDoneWithTouch(true);

        mProject.startActionModeForCounter(this);

        return true;
    }
}
