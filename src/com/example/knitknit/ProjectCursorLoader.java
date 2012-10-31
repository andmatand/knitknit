package com.example.knitknit;

import android.content.Context;
//import android.support.v4.content.AsyncTaskLoader;
import android.content.AsyncTaskLoader;
import android.database.Cursor;
import android.util.Log;

public class ProjectCursorLoader extends AsyncTaskLoader<Cursor> {
    private static final String TAG = "knitknit-ProjectCursorLoader";

    private DataWrangler mDataWrangler;
    private Cursor mCursor;
    private int mLastDatabaseChangeNumber;

    public ProjectCursorLoader(Context context, DataWrangler dataWrangler) {
        super(context);

        mDataWrangler = dataWrangler;
    }
    //public ProjectCursorLoader(Context context) {
    //    super(context);

    //    mDataWrangler = new DataWrangler(context);
    //}

    public Cursor loadInBackground() {
        mDataWrangler.open();
        Cursor cursor = mDataWrangler.getProjectCursor();

        return cursor;
    }

    // Runs on the UI thread
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We don't
            // need the result.
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        Cursor oldCursor = mCursor;
        mCursor = cursor;

        // Start monitoring for changes
        //registerDataSetObserver();

        // If the Loader is currently started
        if (isStarted()) {
            // Immediately deliver the result
            super.deliverResult(cursor);
        }

        // If there is an old cursor
        if (oldCursor != null) {
            // Release its resources
            onReleaseResources(oldCursor);
        }
    }

    @Override
    protected void onStartLoading() {
        Log.w(TAG, "in onStartLoading()");

        // If we currently have a result available
        if (mCursor != null) {
            // Immediately deliver it
            deliverResult(mCursor);
        }

        //boolean databaseChanged = !(mLastDatabaseChangeNumber == mDataWrangler.mChangeNumber);
        //mLastDatabaseChangeNumber = mDataWrangler.mChangeNumber;

        // If the data has changed since the last time it was loaded, or the
        // data is not currently available
        //if (databaseChanged || takeContentChanged() || mCursor == null) {
        if (takeContentChanged() || mCursor == null) {
            // Start a load
            forceLoad();
        } 

    }

    @Override
    public void onContentChanged() {
        Log.w(TAG, "in onContentChanged()");
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        super.onCanceled(cursor);
        
        onReleaseResources(cursor);
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // If there is cursor and it is not closed
        if (mCursor != null) {
            onReleaseResources(mCursor);
        }
        mCursor = null;
    }

    protected void onReleaseResources(Cursor cursor) {
        Log.w(TAG, "in onReleaseResources()");

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
