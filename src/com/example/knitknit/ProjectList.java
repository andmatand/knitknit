package com.example.knitknit;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ProjectList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "knitknit-ProjectList";

    public DataWrangler mDataWrangler;
    private SimpleCursorAdapter mAdapter;
    private ProjectCursorLoader mLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 

        // Create a wrapper layout in which to center a progress bar
        LinearLayout progressBarWrapper = new LinearLayout(this);
        progressBarWrapper.setGravity(Gravity.CENTER);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                                     LayoutParams.WRAP_CONTENT));

        // Put the progress bar in the wrapper
        progressBarWrapper.addView(progressBar);

        // Add the progress bar to the root of the layout
        getListView().setEmptyView(progressBarWrapper);
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBarWrapper);

        // Create a DataWrangler which will be used here and also passed to other classes
        mDataWrangler = new DataWrangler(this);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {DataWrangler.PROJECT_KEY_NAME};
        int[] toViews = {R.id.projectlist_item_name}; // The TextView in projectlist_item

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this, R.layout.projectlist_item,
                                           null, fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);

        // Enable multiple selection of list items
        ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                                  boolean checked) {
                int numSelected = getListView().getCheckedItemCount();

                mode.setTitle(numSelected + " selected");

                if (numSelected > 1) {
                    // Hide the rename button
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                long[] checkedItemIds = getListView().getCheckedItemIds();

                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.delete_project:
                        for (int i = 0; i < checkedItemIds.length; i++) {
                            // Delete the project from the database
                            Log.w(TAG, "deleting project with id " + checkedItemIds[i]);
                            if (mDataWrangler.deleteProject(checkedItemIds[i])) {
                                Log.w(TAG, "deleted successfully");
                            }
                            else {
                                Log.w(TAG, "delete failed");
                            }
                            
                            // Refresh the list
                            mLoader.onContentChanged();
                        }

                        mode.finish();
                        return true;
                    case R.id.rename_project:
                        SparseBooleanArray items = getListView().getCheckedItemPositions();
                        for (int i = 0; i < items.size(); i++) {
                            if (items.valueAt(i)) {
                                TextView tv = (TextView) getListView().getChildAt(items.keyAt(i));
                                showNameDialog(checkedItemIds[0], (String) tv.getText());
                                break;
                            }
                        }

                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.projectlist_context, menu);

                // Give a checkmark to all the list items
                for (int i = 0; i < getListView().getChildCount(); i++) {
                    int[] attrs = {android.R.attr.listChoiceIndicatorMultiple};
                    TypedArray ta = getTheme().obtainStyledAttributes(attrs);
                    Drawable checkmark = ta.getDrawable(0);
                    checkmark.mutate();

                    TextView tv = (TextView) getListView().getChildAt(i);
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmark, null);
                }

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Remove checkmarks from all the list items
                for (int i = 0; i < getListView().getChildCount(); i++) {
                    TextView tv = (TextView) getListView().getChildAt(i);
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
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


    // Options Menu (Action Bar)
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
    private void showNameDialog(final long projectId, String currentName) {
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
                    if (projectId == -1) {
                        mDataWrangler.createProject(nameField.getText().toString());
                    } else {
                        mDataWrangler.updateProject(projectId, nameField.getText().toString(),
                                                    null, null, null);
                    }

                    // Manually tell the ProjectCursorLoader that the content changed
                    mLoader.onContentChanged();
                    return;
                }
            };

        // Create an AlertDialog and set its properties
        AlertDialog.Builder dialog = new AlertDialog.Builder(this); 
        dialog.setCancelable(true);
        dialog.setTitle((projectId == -1 ?
                         R.string.projectlist_namedialog_title_create :
                         R.string.projectlist_namedialog_title_rename));
        dialog.setPositiveButton((projectId == -1 ?
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
