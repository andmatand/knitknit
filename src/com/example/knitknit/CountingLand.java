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
import android.view.MenuItem;

public class CountingLand extends Activity {
    private static final String TAG = "knitknit-CountingLand";

    private DataWrangler mDataWrangler;
    private Project mProject;

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

        // Set the action bar title to the name of the project
        getActionBar().setTitle(mProject.getName());

        Log.w(TAG, "this project has " + mProject.getCounters().size() + " counters");
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
            default:
                return false;
        }
    }
}
