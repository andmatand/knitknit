package com.example.knitknit;

import java.util.Date;

public class Project {
    private long mID;
    private String mName;
    private long mTotalRows;
    private String mDateCreated;
    private String mDateAccessed;

    public Project(long id, String name, long totalRows, String dateCreated, String dateAccessed) {
        mID = id;
        mName = name;
        mTotalRows = totalRows;
        mDateCreated = dateCreated;
        mDateAccessed = dateAccessed;
    }
}
