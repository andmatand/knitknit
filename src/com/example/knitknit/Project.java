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
    private ArrayList<Counter> mCounters;
    private Context mContext;

    // Views
    private ProjectWrapper mWrapper;
    private LinearLayout mCounterWrapper;

    // UI-Related
    private Counter mSelectedCounter;

    public Project(long id, String name, long totalRows, String dateCreated, String dateOpened,
                   ArrayList<Counter> counters, Context context) {
        mId = id;
        mName = name;
        mTotalRows = totalRows;
        mDateCreated = dateCreated;
        mDateOpened = dateOpened;
        mCounters = counters;
        mContext = context;

        // Inflate a copy of the project layout
        LayoutInflater inflater;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWrapper = (ProjectWrapper) inflater.inflate(R.layout.project, null, false);

        // Give the ProjectWrapper object a reference to us as its project
        mWrapper.setProject(this);

        // Find the counter wrapper view
        mCounterWrapper = (LinearLayout) mWrapper.findViewById(R.id.project_counterwrapper);

        attachCounters();
    }

    public void addCounter(Counter counter) {
        // Add the counter object to our list of counters
        mCounters.add(counter);

        // Attach the new counter's view to our counter wrapper view
        counter.refreshViews();
        mCounterWrapper.addView(counter.getWrapper());

        sizeCounters();
    }

    public void attachCounters() {
        // Attach all of our counters to our counter wrapper view
        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();

            mCounterWrapper.addView(counter.getWrapper());
        }
    }

    private Counter findCounterByYPosition(float y) {
        // Loop through each counter
        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();

            // Get the counter's y position
            int couterY = counter.getY();

            // If y overlaps with this counter
            if (y >= (float) counter.getY() &&
                y <= (float) counter.getY() + (counter.getHeight() - 1)) {
                Log.w(TAG, "Counter value: " + counter.getValue());
                Log.w(TAG, "Counter y1: " + counter.getY());
                Log.w(TAG, "Counter y2: " + (counter.getY() + counter.getHeight()));
                Log.w(TAG, "Touch y: " + y);
                return counter;
            }
        }

        return null;
    }

    public ArrayList<Counter> getCounters() {
        return mCounters;
    }

    public String getDateCreated() {
        return mDateCreated;
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

    public void highlightCounter(float y) {
        Counter counter = findCounterByYPosition(y);

        if (counter != null) counter.highlight();
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

    public void longClickCounter(float y) {
        Counter counter = findCounterByYPosition(y);

        if (counter != null) {
            mSelectedCounter = counter;
            counter.longClick();
        }
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

    public void setDateOpened(String dateOpened) {
        mDateOpened = dateOpened;
    }

    private void sizeCounters() {
        // Set the maximum height of the counters based on the available height
        // divided by the number of counters
        int counterHeight = (int) (mWrapper.getHeight() / mCounters.size());
        Log.w(TAG, "wrapper height: " + mWrapper.getHeight());
        Log.w(TAG, "counterHeight: " + counterHeight);

        // Set the right padding
        //int rightPadding;
        //rightPadding = 0;
        //if (mCounters.size() == 1 || !mPrefs.getBoolean(Settings.PREF_SHOWNUMREPEATS, false)) {
        //    rightPadding = 0;
        //} else {
            //rightPadding = mWrapper.getWidth() - (int) (counterSize * 2.3);
        //}
        //mCounterWrapper.setPadding(0, 0, rightPadding, 0);
        //Log.w(TAG, "set padding to " + rightPadding);

        for (Iterator it = mCounters.iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();

            counter.getWrapper().getLayoutParams().height = counterHeight;

            // If there are multiple counters
            //if (mCounters.size() > 1) {
            //    counter.setSingleMode(false);

            //    // Set showing numRepeats based on prference
            //    c.setShowNumRepeats(mPrefs.getBoolean(Settings.PREF_SHOWNUMREPEATS, false));
            //} else {
            //    c.setSingleMode(true);
            //}
        }
    }
}
