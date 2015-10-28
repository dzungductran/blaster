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
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;

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

    private String mCurFolder = "";

    private Menu menu;

    private View previousSelectedView = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent launchingIntent = getIntent();

        // Retrieve the Query Type and Position from the intent or bundle
        if (savedInstanceState != null) {
            mDevice = savedInstanceState.getParcelable(Constants.KEY_DEVICE_STATE);
        } else if (launchingIntent != null) {
            mDevice = launchingIntent.getParcelableExtra(Constants.KEY_DEVICE_STATE);
        }

        setContentView(R.layout.list_files);

        mGridView = (GridView)findViewById(R.id.gridview);
        mListAdapter = new FileListAdapter(this);
        mGridView.setAdapter(mListAdapter);

        // What to do when an item is select\
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (D) Log.d(TAG, "position " + position);
                FileEntry entry = (FileEntry) mListAdapter.getItem(position);

                // Folder so we need to browse into it
                if (entry.bFolder) {
                    if (mCurFolder.isEmpty())
                        mCurFolder = entry.name;
                    else
                        mCurFolder += File.separator + entry.name;

                    setTitle(mCurFolder);

                    mObexClient.browseFolder(entry.name);
                } else {
                    if (previousSelectedView != null) {
                        previousSelectedView.setSelected(false);
                        previousSelectedView.setBackgroundColor(Color.WHITE);
                    }
                    view.setSelected(true);
                    // Set the current selected item background color
                    view.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
                    previousSelectedView = view;
                    if (entry.downloadProgress == 100) {
                        String pathName = entry.path + File.separator + entry.name;
                        String type = URLConnection.guessContentTypeFromName(pathName);
                        if (type == null) {
                            Toast.makeText(getApplicationContext(),
                                    "No viewer for file: " + pathName, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + pathName), type);
                        startActivity(intent);
                    } else {
                        mObexClient.downloadFile(entry.path, entry.name, entry.size);
                    }
                    //setShareIntent("/url_here");
                }
            }
        });


        if (mDevice != null) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);

            mObexClient = new BluetoothObexClient(this, mHandlerBT);

            mObexClient.connect(mDevice.getBluetoothDevice());

            // Browse the top most folder first
            mObexClient.browseFolder(mCurFolder);
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
                            msg.getData().getString(Constants.KEY_TOAST), Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_BROWSE_DONE_CMD:
                    if (D) Log.i(TAG, "Done browsing get file list");
                    ArrayList<FileEntry> entries = msg.getData().getParcelableArrayList(Constants.KEY_FILE_ENTRIES);
                    //Go the list of files and see if they exist on disk
                    File root = Environment.getExternalStorageDirectory();
                    for (FileEntry entry : entries) {
                        if (!entry.bFolder) {
                            entry.path = root.getAbsolutePath();
                            if (!mCurFolder.isEmpty()) {
                                entry.path += File.separator + mCurFolder;
                            }
                            String pathName = entry.path + File.separator + entry.name;
                            File f = new File(pathName);
                            if (f.exists() && f.length() == entry.size) {
                                entry.downloadProgress = 100;
                            }
                        }
                    }
                    // Add entry to the list
                    mListAdapter.addFileEntries(entries);
                    mListAdapter.notifyDataSetChanged();
                    break;

                case Constants.MESSAGE_DOWNLOAD_PROGRESS:
                    String folder = msg.getData().getString(Constants.KEY_FOLDER_NAME);
                    String file = msg.getData().getString(Constants.KEY_FILE_NAME);
                    int percent = msg.getData().getInt(Constants.KEY_PERCENT);
                    FileEntry entry = mListAdapter.getItem(folder + File.separator + file);
                    if (entry != null) {
                        entry.downloadProgress = percent;
                        mListAdapter.notifyDataSetChanged();
                    }
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mDevice != null) {
            savedInstanceState.putParcelable(Constants.KEY_DEVICE_STATE, mDevice);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDevice = savedInstanceState.getParcelable(Constants.KEY_DEVICE_STATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_file, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        this.menu = menu;

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
            if (mCurFolder.isEmpty()) {
                mObexClient.shutdown(); // top folder, back go previous activity
                finish();
            } else {
                String title;
                int idx = mCurFolder.lastIndexOf(File.separator);
                if (idx != -1) {
                    mCurFolder = mCurFolder.substring(0, mCurFolder.lastIndexOf(File.separator));
                    title = mCurFolder;
                } else {
                    // must be backing up to top dir
                    mCurFolder = "";
                    title = getResources().getString(R.string.browse_files);
                }
                setTitle(title);
                mObexClient.browseFolder(".."); // go up one folder
            }
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