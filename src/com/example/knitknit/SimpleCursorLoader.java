package com.example.knitknit;

import android.content.Context;
//import android.support.v4.content.AsyncTaskLoader;
import android.content.AsyncTaskLoader;
import android.database.Cursor;
import android.util.Log;

public class SimpleCursorLoader extends AsyncTaskLoader<Cursor> {
    private static final String TAG = "knitknit-SimpleCursorLoader";

    private DataWrangler mDataWrangler;
    private Cursor mCursor;

    //public SimpleCursorLoader(Context context, DataWrangler dataWrangler) {
    //    super(context);

    //    mDataWrangler = dataWrangler;//new DataWrangler(context);
    //}
    public SimpleCursorLoader(Context context) {
        super(context);

        mDataWrangler = new DataWrangler(context);
    }

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
        // If we currently have a result available
        if (mCursor != null) {
            // Immediately deliver it
            deliverResult(mCursor);
        }


        // If the data has changed since the last time it was loaded, or the
        // data is not currently available
        if (takeContentChanged() || mCursor == null) {
            // Start a load
            forceLoad();
        } 

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

        if (!cursor.isClosed()) {
            cursor.close();
        }
        mDataWrangler.close();
    }
}
