package com.example.knitknit;

import java.util.ArrayList;
import java.util.Date;

public class Project {
    private long mId;
    private String mName;
    private long mTotalRows;
    private String mDateCreated;
    private String mDateOpened;
    private ArrayList<Counter> mCounters;

    public Project(long id, String name, long totalRows, String dateCreated, String dateOpened,
                   ArrayList<Counter> counters) {
        mId = id;
        mName = name;
        mTotalRows = totalRows;
        mDateCreated = dateCreated;
        mDateOpened = dateOpened;
        mCounters = counters;
    }

    public String getName() {
        return mName;
    }

    public ArrayList<Counter> getCounters() {
        return mCounters;
    }

}
