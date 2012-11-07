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

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
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
    private CountingLand mActivity;

    public Project(long id, String name, long totalRows, String dateCreated, String dateOpened,
                   Context context) {
        mId = id;
        mName = name;
        mTotalRows = totalRows;
        mDateCreated = dateCreated;
        mDateOpened = dateOpened;

        mCounters = new ArrayList<Counter>();
        mDeletedCounters = new ArrayList<Counter>();

        // Inflate a copy of the project layout
        LayoutInflater inflater;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWrapper = (ProjectWrapper) inflater.inflate(R.layout.project, null, false);

        // Give the ProjectWrapper object a reference to us as its project
        mWrapper.setProject(this);

        // Find the counter wrapper view
        mCounterWrapper = (LinearLayout) mWrapper.findViewById(R.id.project_counterwrapper);
    }

    public void addCounter(Counter counter) {
        // Add the counter object to our list of counters
        mCounters.add(counter);

        // Attach the new counter's view to our counter wrapper view
        counter.refreshViews();
        mCounterWrapper.addView(counter.getWrapper());

        counter.setProject(this);
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
        // Adds (or subtracts, depending on counter setting) to all counters
        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();
            counter.increase();
        }

        mTotalRows++;
        refreshTotal();
    }

    private void refreshTotal() {
    }

    public void refreshViews() {
        refreshTotal();

        sizeCounters();

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

    private void sizeCounters() {
        // Set the maximum height of the counters based on the available height
        // divided by the number of counters
        int counterHeight = (int) (mWrapper.getHeight() / mCounters.size());
        Log.w(TAG, "wrapper height: " + mWrapper.getHeight());
        Log.w(TAG, "counterHeight: " + counterHeight);

        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();

            counter.getWrapper().getLayoutParams().height = counterHeight;
        }
    }

    public void startActionModeForCounter(Counter counter) {
        mActivity.startActionModeForCounter(counter);
    }
}
