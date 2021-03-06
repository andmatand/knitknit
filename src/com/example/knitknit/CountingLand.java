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
    public static Counter selectedCounter = null;

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

            // If the selected counter has a name
            if (counter.getName() != null) {
                // Set the action bar title to the name of the counter
                mActionMode.setTitle(counter.getName());
            }

            // Highlight thte selected counter
            counter.getWrapper().setSelected(true);
            counter.refreshViews();

            selectedCounter = counter;
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

                    // Close the Contextual Action Bar
                    mode.finish();
                    return true;
                case R.id.increase_counter:
                    counter.increase();
                    return true;
                case R.id.edit_counter:
                    // Start the CounterEditor activity
                    Intent intent = new Intent(CountingLand.this, CounterEditor.class);
                    startActivity(intent);

                    // Close the Contextual Action Bar
                    mode.finish();
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
