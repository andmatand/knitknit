package com.example.knitknit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class DataWrangler {
    private static final String TAG = "knitknit-DataWrangler";

    // Database Constants
    private static final String DATABASE_NAME = "knitknit.db";
    private static final int DATABASE_VERSION = 9;

    private static final String PROJECT_TABLE = "project";
    public static final String PROJECT_KEY_ID = "_id";
    public static final String PROJECT_KEY_NAME = "name";
    private static final String PROJECT_KEY_DATECREATED = "dateCreated";
    private static final String PROJECT_KEY_DATEOPENED = "dateOpened";
    private static final String PROJECT_KEY_TOTALROWS = "totalRows";

    private static final String COUNTER_TABLE = "counter";
    private static final String COUNTER_KEY_ID = "_id";
    private static final String COUNTER_KEY_PROJECTID = "project_id";
    private static final String COUNTER_KEY_NAME = "name";
    private static final String COUNTER_KEY_VALUE = "value";
    private static final String COUNTER_KEY_COUNTUP = "countUp";
    private static final String COUNTER_KEY_PATTERNENABLED = "patternEnabled";
    private static final String COUNTER_KEY_PATTERNLENGTH = "patternLength";
    private static final String COUNTER_KEY_NUMREPEATS = "numRepeats";

    // Member Variables
    private Context mContext;
    private DatabaseHelper mOpenHelper;
    private SQLiteDatabase mDatabase;

    // DatabaseHelper Class
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create the project table
            db.execSQL("create table " + PROJECT_TABLE + "(" +
                       PROJECT_KEY_ID + " integer primary key autoincrement, " +
                       PROJECT_KEY_NAME + " tinytext not null, " +
                       PROJECT_KEY_DATECREATED + " datetime not null," +
                       PROJECT_KEY_DATEOPENED + " datetime not null, " +
                       PROJECT_KEY_TOTALROWS + " integer not null default 0);");

            // Create the counter table
            db.execSQL("create table " + COUNTER_TABLE + "(" +
                       COUNTER_KEY_ID + " integer primary key autoincrement, " +
                       COUNTER_KEY_PROJECTID + " integer not null, " +
                       COUNTER_KEY_NAME + " tinytext null, " +
                       COUNTER_KEY_VALUE + " integer not null default 0, " +
                       COUNTER_KEY_COUNTUP + " bool not null default 1, " +
                       COUNTER_KEY_PATTERNENABLED + " bool not null default 0, " +
                       COUNTER_KEY_PATTERNLENGTH + " integer not null default 10, " +
                       COUNTER_KEY_NUMREPEATS + " integer not null default 0);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from " + "version " + oldVersion + " to " + newVersion +
                       ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + PROJECT_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + COUNTER_TABLE);

            onCreate(db);
        }
    }


    // Core DataWrangler Methods
    public DataWrangler(Context context) {
        mOpenHelper = new DatabaseHelper(context);
        mContext = context;
    }

    public DataWrangler open() throws SQLException {
        // Open the notes database.  If it cannot be opened, try to create a
        // new instance of the database.  If it cannot be created, throw an
        // exception to signal the failure
        mDatabase = mOpenHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mOpenHelper.close();
    }


    // Project Methods
    public Cursor getProjectCursor() {
        // Returns a Cursor over the list of all projects in the database
        return mDatabase.query(PROJECT_TABLE,
                               new String[] {PROJECT_KEY_ID, PROJECT_KEY_NAME},
                               null, null, null, null, PROJECT_KEY_DATEOPENED + " desc");
    }

    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

        return dateFormat.format(new Date());
    }

    public boolean createProject(String name) {
        String currentDate = getDate();

        ContentValues projectValues = new ContentValues();
        projectValues.put(PROJECT_KEY_NAME, name);
        projectValues.put(PROJECT_KEY_DATECREATED, currentDate);
        projectValues.put(PROJECT_KEY_DATEOPENED, currentDate);

        long projectId = mDatabase.insert(PROJECT_TABLE, null, projectValues);

        // If there was a database error
        if (projectId == -1) {
            return false;
        } else {
            // Add one counter to the project to begin with
            if (createCounter(projectId) != -1) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    public Project retrieveProject(long projectId) throws SQLException {
        String[] columns = {PROJECT_KEY_ID,
                            PROJECT_KEY_NAME,
                            PROJECT_KEY_TOTALROWS,
                            PROJECT_KEY_DATECREATED,
                            PROJECT_KEY_DATEOPENED};

        String where = PROJECT_KEY_ID + "=" + projectId;

        Cursor projectCursor = mDatabase.query(true, PROJECT_TABLE, columns, where,
                                               null, null, null, null, null);
        if (projectCursor != null) {
            projectCursor.moveToFirst();
        }

        // Create a Project object with all the info we got
        Project project = new Project(
            projectCursor.getLong(projectCursor.getColumnIndex(PROJECT_KEY_ID)),
            projectCursor.getString(projectCursor.getColumnIndex(PROJECT_KEY_NAME)),
            projectCursor.getLong(projectCursor.getColumnIndex(PROJECT_KEY_TOTALROWS)),
            projectCursor.getString(projectCursor.getColumnIndex(PROJECT_KEY_DATECREATED)),
            projectCursor.getString(projectCursor.getColumnIndex(PROJECT_KEY_DATEOPENED)),
            mContext);

        // Get a cursor over all the counters in this project
        Cursor counterCursor = getCounterCursor(projectId);

        // Iterate through the counter rows
        do {
            // Add a new Counter object to the Project object
            long id = counterCursor.getLong(counterCursor.getColumnIndex(COUNTER_KEY_ID));
            project.addCounter(retrieveCounter(id));
        } while (counterCursor.moveToNext());
        
        // Close the cursors
        counterCursor.close();
        projectCursor.close();

        return project;
    }

    public boolean updateProject(long projectId, String name, Long totalRows, String dateCreated,
                                 String dateOpened) {
        ContentValues values = new ContentValues();
        if (name != null) values.put(PROJECT_KEY_NAME, name);
        if (totalRows != null) values.put(PROJECT_KEY_TOTALROWS, (long) totalRows);
        if (dateCreated != null) values.put(PROJECT_KEY_DATECREATED, dateCreated);
        if (dateOpened != null) values.put(PROJECT_KEY_DATEOPENED, dateOpened);

        return mDatabase.update(PROJECT_TABLE, values, PROJECT_KEY_ID + "=" + projectId, null) > 0;
    }

    public boolean deleteProject(long projectId) {
        // Get a cursor over the list of counters in this project
        Cursor cursor = getCounterCursor(projectId);

        // Delete each counter
        if (cursor != null) {
            do {
                if (!deleteCounter(cursor.getLong(cursor.getColumnIndexOrThrow(COUNTER_KEY_ID)))) {
                    return false;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Delete the project
        return mDatabase.delete(PROJECT_TABLE, PROJECT_KEY_ID + "=" + projectId, null) > 0;
    }

    public void saveProject(Project project) {
        updateProject(project.getId(), project.getName(), project.getTotalRows(),
                      project.getDateCreated(), project.getDateOpened());

        for (Iterator it = project.getCounters().iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();

            updateCounter(counter.getId(), counter.getName(), counter.getValue(),
                          counter.getCountUp(), counter.getPatternEnabled(),
                          counter.getPatternLength(), counter.getNumRepeats());
        }

        for (Iterator it = project.getDeletedCounters().iterator(); it.hasNext(); ) {
            Counter counter = (Counter) it.next();

            if (deleteCounter(counter.getId())) {
                it.remove();
            }
        }
    }

    public void touchProject(Project project) {
        project.setDateOpened(getDate());

        // Set the dateOpened on the project to the current time
        boolean result = updateProject(project.getId(), null, null, null, project.getDateOpened());

        Log.w(TAG, "touchProject result: " + result);
    }


    // Counter Methods
    public Cursor getCounterCursor(long projectId) throws SQLException {
        // Returns a Cursor over the all counters with the specified projectId
        String[] columns = {COUNTER_KEY_ID,
                            COUNTER_KEY_NAME,
                            COUNTER_KEY_VALUE,
                            COUNTER_KEY_COUNTUP,
                            COUNTER_KEY_PATTERNENABLED,
                            COUNTER_KEY_PATTERNLENGTH,
                            COUNTER_KEY_NUMREPEATS};

        Cursor cursor = mDatabase.query(true, COUNTER_TABLE, columns, 
                                        COUNTER_KEY_PROJECTID + "=" + projectId,
                                        null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        if (cursor.getCount() == 0) {
            Log.w(TAG, "counterCursor was empty");
        }

        return cursor;
    }

    public long createCounter(long projectId) throws SQLException {
        ContentValues counterValues = new ContentValues();
        counterValues.put(COUNTER_KEY_PROJECTID, projectId);

        return mDatabase.insert(COUNTER_TABLE, null, counterValues);
    }

    public Counter retrieveCounter(long counterId) throws SQLException {
        String[] columns = {COUNTER_KEY_ID,
                            COUNTER_KEY_PROJECTID,
                            COUNTER_KEY_NAME,
                            COUNTER_KEY_VALUE,
                            COUNTER_KEY_COUNTUP,
                            COUNTER_KEY_PATTERNENABLED,
                            COUNTER_KEY_PATTERNLENGTH,
                            COUNTER_KEY_NUMREPEATS};

        String where = COUNTER_KEY_ID + "=" + counterId;

        Cursor cursor = mDatabase.query(true, COUNTER_TABLE, columns, where,
                                               null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        // Create a Counter object from the info we got
        Counter counter;
        counter = new Counter(cursor.getLong(cursor.getColumnIndex(COUNTER_KEY_ID)),
                              cursor.getLong(cursor.getColumnIndex(COUNTER_KEY_PROJECTID)),
                              cursor.getString(cursor.getColumnIndex(COUNTER_KEY_NAME)),
                              cursor.getLong(cursor.getColumnIndex(COUNTER_KEY_VALUE)),
                              cursor.getInt(cursor.getColumnIndex(COUNTER_KEY_COUNTUP)) > 0,
                              cursor.getInt(cursor.getColumnIndex(COUNTER_KEY_PATTERNENABLED)) > 0,
                              cursor.getLong(cursor.getColumnIndex(COUNTER_KEY_PATTERNLENGTH)),
                              cursor.getLong(cursor.getColumnIndex(COUNTER_KEY_NUMREPEATS)),
                              mContext);

        return counter;
    }

    public boolean updateCounter(long counterId, String name, long value, boolean countUp,
                                 boolean patternEnabled, long patternLength, long numRepeats) {
        ContentValues values = new ContentValues();
        values.put(COUNTER_KEY_NAME, name);
        values.put(COUNTER_KEY_VALUE, value);
        values.put(COUNTER_KEY_COUNTUP, countUp);
        values.put(COUNTER_KEY_PATTERNENABLED, patternEnabled);
        values.put(COUNTER_KEY_PATTERNLENGTH, patternLength);
        values.put(COUNTER_KEY_NUMREPEATS, numRepeats);

        return mDatabase.update(COUNTER_TABLE, values, COUNTER_KEY_ID + "=" + counterId, null) > 0;
    }

    public boolean deleteCounter(long counterId) {
        return mDatabase.delete(COUNTER_TABLE, COUNTER_KEY_ID + "=" + counterId, null) > 0;
    }
}
