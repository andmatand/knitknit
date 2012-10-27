package com.example.knitknit;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

public class ProjectList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "knitknit-ProjectList";

    public DataWrangler mDataWrangler;
    private SimpleCursorAdapter mAdapter;
    private ProjectCursorLoader mLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataWrangler = new DataWrangler(this);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {DataWrangler.PROJECT_KEY_NAME};
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                                           null, fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }


    // Loader Callbacks
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        mLoader = new ProjectCursorLoader(this, mDataWrangler);

        return mLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }


    // Options Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.projectlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_project:
                showNameDialog(-1, null);
                //mDataWrangler.addTestProject();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // @projectID
    //	-1 we are creating a project
    //	Otherwise we are renaming the project with this projectID
    private void showNameDialog(final long projectID, String currentName) {
        // Instantiate a view of projectlist_namedialog.xml
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.projectlist_namedialog, null, false);

        // Find the name EditText field
        final EditText nameField = (EditText) view.findViewById(R.id.projectlist_namedialog_name);

        // If we are renaming, put the current name in the box
        if (currentName != null) {
            nameField.append(currentName);
        }

        DialogInterface.OnClickListener listener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (projectID == -1) {
                        mDataWrangler.createProject(nameField.getText().toString());
                        // Manually tell the ProjectCursorLoader that the content changed
                        mLoader.onContentChanged();
                    } else {
                        //mDatabaseHelper.updateProject(projectID, name.getText().toString(),
                        //                              null, null, null);
                    }
                    return;
                }
            };


        // Create an AlertDialog and set its properties
        AlertDialog.Builder dialog = new AlertDialog.Builder(this); 
        dialog.setCancelable(true);
        dialog.setTitle((projectID == -1 ?
                         R.string.projectlist_namedialog_title_create :
                         R.string.projectlist_namedialog_title_rename));
        dialog.setPositiveButton((projectID == -1 ?
                                  R.string.projectlist_namedialog_create :
                                  R.string.projectlist_namedialog_rename),
                                 listener);

        // Fill the dialog with the view
        dialog.setView(view);
        dialog.create();

        // Show the dialog
        dialog.show();
    }
}
