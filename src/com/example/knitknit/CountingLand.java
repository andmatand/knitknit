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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.ViewGroup;

public class CountingLand extends Activity {
    private static final String TAG = "knitknit-CountingLand";

    private DataWrangler mDataWrangler;
    private Project mProject;
    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Get projectID from savedInstanceState
        Long projectID = null;
        projectID = (savedInstanceState == null ?
                     null :
                     (Long) savedInstanceState.getSerializable(DataWrangler.PROJECT_KEY_ID));

        // If we still don't have projectID, get it from intent extras
        if (projectID == null) {
            Bundle extras = getIntent().getExtras();
            projectID = (extras != null ?
                         extras.getLong(DataWrangler.PROJECT_KEY_ID) :
                         null);
        }

        if (projectID == null) {
            Log.w(TAG, "projectID is null");
        }
        else{ 
            Log.w(TAG, "projectID: " + projectID);
        }

        // Load the project
        mDataWrangler = new DataWrangler(this);
        mDataWrangler.open();
        mProject = mDataWrangler.retrieveProject(projectID);
        mProject.setActivity(this);

        // Update the dateOpened on the project
        mDataWrangler.touchProject(mProject);

        // DEBUG
        Log.w(TAG, "this project has " + mProject.getCounters().size() + " counters");

        // Set the action bar title to the name of the project
        getActionBar().setTitle(mProject.getName());

        // Attach the project's view to our view
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        mProject.inflate(root, getResources().getConfiguration().orientation);
        root.addView(mProject.getWrapper());
    }


    // Lifecycle Management Methods
    //@Override
    //protected void onDestroy() {
    //    super.onDestroy();

    //    mDataWrangler.close();
    //}

    @Override
    protected void onPause() {
        super.onPause();

        mDataWrangler.saveProject(mProject);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.w(TAG, "in onWindowFocusChanged()");
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mProject.refreshViews();
        }
    }


    // Options Menu (Action Bar) Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.countingland, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ProjectList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.new_counter:
                // Add a new counter to the database
                long id = mDataWrangler.createCounter(mProject.getId());

                // Retrieve a counter object for the new counter
                Counter counter = mDataWrangler.retrieveCounter(id);

                // Add the counter object to the project
                mProject.addCounter(counter);
                mProject.refreshViews();
                return true;
            default:
                return false;
        }
    }


    // Other Methods
    public Project getProject() {
        return mProject;
    }

    public static boolean getZeroMode() {
        return false;
    }

    public void startActionModeForCounter(Counter counter) {
        if (mActionMode == null) {
            // Tell the ProjectWrapper to ignore touch events while we are in action mode
            ((ProjectWrapper) findViewById(R.id.project_wrapper)).setRespondToTouch(false);

            mActionMode = startActionMode(mActionModeCallback);
            mActionMode.setTag(counter);

            // Highlight thte selected counter
            counter.getWrapper().setSelected(true);
            counter.refreshViews();
        }
    }


    // Contextual Action Mode Callbacks
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.counter_context, menu);

            // If there is less than 2 counters
            if (mProject.getCounters().size() < 2) {
                // Hide the "delete" menu item
                MenuItem deleteItem = menu.findItem(R.id.delete_counter);
                deleteItem.setVisible(false);
            }

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Counter counter = (Counter) mode.getTag();

            switch (item.getItemId()) {
                case R.id.decrease_counter:
                    counter.decrease();
                    return true;
                case R.id.delete_counter:
                    mProject.deleteCounter(counter);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.increase_counter:
                    counter.increase();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;

            // Un-highlight the selected counter
            Counter counter = (Counter) mode.getTag();
            counter.getWrapper().setSelected(false);
            mProject.refreshViews();

            // Tell the ProjectWrapper to stop ignoring touch events
            ((ProjectWrapper) findViewById(R.id.project_wrapper)).setRespondToTouch(true);
        }
    };
}
