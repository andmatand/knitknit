package com.example.knitknit;

public class Counter {
    private long mId;
    private long mProjectId;
    private String mName;
    private long mValue;
    private boolean mCountUp;
    private boolean mPatternEnabled;
    private long mPatternLength;
    private long mNumRepeats;

    public Counter(long id, long projectId, String name, long value, boolean countUp,
                   boolean patternEnabled, long patternLength, long numRepeats) {
        mId = id;
        mProjectId = projectId;
        mName = name;
        mValue = value;
        mCountUp = countUp;
        mPatternEnabled = patternEnabled;
        mPatternLength = patternLength;
        mNumRepeats = numRepeats;
    }
}
