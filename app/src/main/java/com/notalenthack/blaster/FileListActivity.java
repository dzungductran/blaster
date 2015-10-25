/*
Copyright © 2015 NoTalentHack, LLC

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.notalenthack.blaster;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.notalenthack.blaster.dialog.EditCommandDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that displays all the files
 */
public class FileListActivity extends Activity {
    private static final String TAG = "FileListActivity";
    private static final boolean D = true;

    private EdisonDevice mDevice;

    // fields in the layout
    private GridView mGridView;
    private FileListAdapter mListAdapter;

    private ShareActionProvider mShareActionProvider;

    private static BluetoothObexClient mObexClient = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent launchingIntent = getIntent();

        // Retrieve the Query Type and Position from the intent or bundle
        if (savedInstanceState != null) {
            mDevice = savedInstanceState.getParcelable(Constants.DEVICE_STATE);
        } else if (launchingIntent != null) {
            mDevice = launchingIntent.getParcelableExtra(Constants.DEVICE_STATE);
        }

        setContentView(R.layout.list_files);

        mGridView = (GridView)findViewById(R.id.gridview);
        mListAdapter = new FileListAdapter(this);
        mGridView.setAdapter(mListAdapter);

        // What to do when an item is select
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setShareIntent("/url_here");
            }
        });

        if (mDevice != null) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);

            mObexClient = new BluetoothObexClient(this, mHandlerBT);

            mObexClient.connect(mDevice.getBluetoothDevice());

            mObexClient.browseFolder("");
        } else {
            Log.e(TAG, "Bluetooth device is not initialized");
            finish();
        }
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandlerBT = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_TOAST_CMD:
                    if (D) Log.i(TAG, "MESSAGE_TOAST_CMD: " + msg.arg1);
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(Constants.TOAST), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mDevice != null) {
            savedInstanceState.putParcelable(Constants.DEVICE_STATE, mDevice);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDevice = savedInstanceState.getParcelable(Constants.DEVICE_STATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_file, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    // Call to update the share intent. You may only need to set the share intent once during the creation of your
    // menus, or you may want to set it and then update it as the UI changes. For example, when you view photos
    // full screen in the Gallery app, the sharing intent changes as you flip between photos.
    private void setShareIntent(String url) {
        if (mShareActionProvider != null) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "");
            i.putExtra(Intent.EXTRA_TEXT, url);
            //startActivity(Intent.createChooser(i, "Share URL"));
            mShareActionProvider.setShareIntent(i);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mObexClient.shutdown();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (mObexClient != null && mDevice != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (!mObexClient.isConnected()) {
                // Start the Bluetooth  services
                mObexClient.connect(mDevice.getBluetoothDevice());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mObexClient != null)
            mObexClient.shutdown();
    }
}