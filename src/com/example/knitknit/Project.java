package com.example.knitknit;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Project {
    private static final String TAG = "knitknit-Project";

    // Database Fields
    private long mId;
    private String mName;
    private long mTotalRows;
    private String mDateCreated;
    private String mDateOpened;

    // Child Objects
    private ArrayList<Counter> mCounters;
    private ArrayList<Counter> mDeletedCounters;

    // Views
    private ProjectWrapper mWrapper;
    private LinearLayout mCounterWrapper;

    // UI-Related
    private Context mContext;
    private CountingLand mActivity;

    public Project(long id, String name, long totalRows, String dateCreated, String dateOpened,
                   Context context) {
        mId = id;
        mName = name;
        mTotalRows = totalRows;
        mDateCreated = dateCreated;
        mDateOpened = dateOpened;

        mContext = context;

        mCounters = new ArrayList<Counter>();
        mDeletedCounters = new ArrayList<Counter>();

    }

    public void addCounter(Counter counter) {
        // Add the counter object to our list of counters
        mCounters.add(counter);

        // Attach the new counter's view to our counter wrapper view
        attachCounter(counter);

        counter.setProject(this);
    }

    private void attachCounter(Counter counter) {
        if (mCounterWrapper != null) {
            counter.inflate(mCounterWrapper);
            mCounterWrapper.addView(counter.getWrapper());
        }
    }

    private void attachCounters() {
        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();
            attachCounter(counter);
        }
    }

    public void decrease() {
        // Subtracts (or adds, depending on counter setting) from all counters
        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();
            counter.decrease();
        }

        mTotalRows--;
        refreshTotal();
    }

    public void deleteCounter(Counter counter) {
        // Remove the counter's view
        mCounterWrapper.removeView(counter.getWrapper());

        // Add the counter to our list of counters to delete on our next save
        mDeletedCounters.add(counter);

        // Remove the counter from our list of counters
        mCounters.remove(counter);

        refreshViews();
    }

    public ArrayList<Counter> getCounters() {
        return mCounters;
    }

    public LinearLayout getCounterWrapper() {
        return mCounterWrapper;
    }

    public String getDateCreated() {
        return mDateCreated;
    }

    public ArrayList<Counter> getDeletedCounters() {
        return mDeletedCounters;
    }

    public String getDateOpened() {
        return mDateOpened;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public long getTotalRows() {
        return mTotalRows;
    }

    public ProjectWrapper getWrapper() {
        return mWrapper;
    }

    public void increase() {
        Log.w(TAG, "in increase()");
        // Adds (or subtracts, depending on counter setting) to all counters
        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();
            counter.increase();
        }

        mTotalRows++;
        refreshTotal();
    }

    public void inflate(ViewGroup root, int orientation) {
        // Inflate a copy of the project layout
        LayoutInflater inflater;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //mWrapper = (ProjectWrapper) inflater.inflate(R.layout.project, root, false);
        mWrapper = (ProjectWrapper) inflater.inflate(R.layout.project, null, false);

        // Give the ProjectWrapper object a reference to us as its project
        mWrapper.setProject(this);

        // Find the counter wrapper view
        mCounterWrapper = (LinearLayout) mWrapper.findViewById(R.id.project_counterwrapper);

        // Set the orientation of the counter wrapper based on the screen's orientation
        mCounterWrapper.setOrientation(orientation == Configuration.ORIENTATION_LANDSCAPE ?
                                       0 : 1);

        attachCounters();
    }

    private void refreshTotal() {
    }

    public void refreshViews() {
        refreshTotal();

        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();

            counter.refreshViews();
        }
    }

    public void setActivity(CountingLand activity) {
        mActivity = activity;
    }

    public void setDateOpened(String dateOpened) {
        mDateOpened = dateOpened;
    }

    public void startActionModeForCounter(Counter counter) {
        mActivity.startActionModeForCounter(counter);
    }
}
