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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.notalenthack.blaster.dialog.EditCommandDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that displays all the commands
 */
public class CommandActivity extends Activity implements EditCommandDialog.CommandCallback {
    private static final String TAG = "CommandActivity";
    private static final boolean D = true;

    public enum Direction { RIGHT, LEFT };

    private EdisonDevice mDevice;

    // fields in the layout
    private TextView mModelName;
    private TextView mVendorId;
    private TextView mCpuSpeed;
    private TextView mCacheSize;
    private TextView mConnectStatus;
    private ImageView mDeviceStatus;
    private ImageView mBatteryStatus;
    private ListView mCmdListView;
    private Activity mThisActivity;

    private CommandListAdapter mListAdapter;

    private static BluetoothSerialService mSerialService = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThisActivity = this;

        final Intent launchingIntent = getIntent();

        // Retrieve the Query Type and Position from the intent or bundle
        if (savedInstanceState != null) {
            mDevice = savedInstanceState.getParcelable(Constants.DEVICE_STATE);
        } else if (launchingIntent != null) {
            mDevice = launchingIntent.getParcelableExtra(Constants.DEVICE_STATE);
        }

        setContentView(R.layout.commands);
        // get the fields
        mModelName = (TextView) findViewById(R.id.modelName);
        mVendorId = (TextView) findViewById(R.id.vendorId);
        mCpuSpeed = (TextView) findViewById(R.id.cpuSpeed);
        mCacheSize = (TextView) findViewById(R.id.cacheSize);
        mConnectStatus = (TextView) findViewById(R.id.status);
        mDeviceStatus = (ImageView) findViewById(R.id.deviceStatusIcon);
        mBatteryStatus = (ImageView) findViewById(R.id.batteryStatus);
        mCmdListView = (ListView) findViewById(R.id.listView);;

        if (mDevice != null) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);

            mSerialService = new BluetoothSerialService(this, mHandlerBT);

            // setup swipe listener
            mCmdListView.setOnTouchListener(new OnSwipeTouchListener(this, mCmdListView) {
                @Override
                public void onSwipeRight(int pos) {
                    Command command = mListAdapter.getCommand(pos);
                    if (!command.isSystemCommand()) {
                        showDeleteButton(pos, Direction.RIGHT);
                    }
                    if (D) Toast.makeText(mThisActivity, "right", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSwipeLeft(int pos) {
                    Command command = mListAdapter.getCommand(pos);
                    if (!command.isSystemCommand()) {
                        showDeleteButton(pos, Direction.LEFT);
                    }
                    if (D) Toast.makeText(mThisActivity, "left", Toast.LENGTH_SHORT).show();
                }
            });

            setupCommandList();

            mSerialService.connect(mDevice.getBluetoothDevice());

        } else {
            Log.e(TAG, "Bluetooth device is not initialized");
            finish();
        }
    }

    private boolean showDeleteButton(int pos, Direction dir) {
        View child = mCmdListView.getChildAt(pos - mCmdListView.getFirstVisiblePosition());
        if (child != null) {

            View slideIn = child.findViewById(R.id.slideIn);
            if (slideIn != null) {
                if (slideIn.getVisibility() == View.INVISIBLE) {
                    if (dir == Direction.LEFT) {
                        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
                        slideIn.startAnimation(anim);
                        slideIn.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (dir == Direction.RIGHT) {
                        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
                        slideIn.startAnimation(anim);
                        slideIn.setVisibility(View.INVISIBLE);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void setupCommandList() {
        mListAdapter = new CommandListAdapter(this);
        // read from storage and initialze the adapter.
        restoreCommands();

        mCmdListView.setAdapter(mListAdapter);
        mCmdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Command command = mListAdapter.getCommand(position);
                if (command != null) {
                    if (D) Log.d(TAG, "command " + command.getCommandStart());
                    mSerialService.sendCommand(Constants.SERIAL_CMD_EXECUTE, command.getCommandStart());
                }
            }
        });
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandlerBT = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE_CMD:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothSerialService.STATE_CONNECTING:
                            mDeviceStatus.setImageResource(R.drawable.ic_bluetooth_paired);
                            mConnectStatus.setText(R.string.connecting);
                            break;

                        case BluetoothSerialService.STATE_CONNECTED:
                            mDeviceStatus.setImageResource(R.drawable.ic_bluetooth_connected);
                            mConnectStatus.setText(R.string.connected);
                            break;

                        case BluetoothSerialService.STATE_NONE:
                        //case BluetoothSerialService.STATE_CANTCONNECT:
                            mDeviceStatus.setImageResource(R.drawable.ic_bluetooth_disabled);
                            mConnectStatus.setText(R.string.none);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE_CMD:
                    // data is in
                    if (D) {
                        byte[] writeBuf = (byte[]) msg.obj;
                        Log.d(TAG, "write data: " + writeBuf);
                    }
                    break;

                case Constants.MESSAGE_READ_CMD:
                    if (D) {
                        byte[] readBuf = (byte[]) msg.obj;
                        Log.d(TAG, "read data: " + readBuf);
                    }
                    break;

                case Constants.MESSAGE_DEVICE_NAME_CMD:
                    // save the connected device's name
                    String deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_connected_to) + " "
                            + deviceName, Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_TOAST_CMD:
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
        menuInflater.inflate(R.menu.add_command, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mSerialService.sendCommand(Constants.SERIAL_CMD_CLOSE, "");
            mSerialService.stop();
            finish();
        } else if (item.getItemId() == R.id.menu_add_command) {
            Command cmd = new Command();
            editCommand(cmd);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public synchronized void onResume() {
        super.onResume();

        if (mSerialService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
                // Start the Bluetooth  services
                mSerialService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSerialService != null)
            mSerialService.stop();
    }

    public void send(byte[] out) {

        out = Utils.handleEndOfLineChars(out);

        if (out.length > 0) {
            mSerialService.write(out);
        }
    }

    private void editCommand(Command command) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(EditCommandDialog.TAG_ICON_PICKER_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = EditCommandDialog.newInstance(command, this);
        newFragment.show(ft, EditCommandDialog.TAG_ICON_PICKER_DIALOG);
    }

    // Callback when a command is created
    public void newCommand(Command command) {
        if (D) Log.d(TAG, "command is done " + command.getName());
        mListAdapter.addCommand(command);
        mListAdapter.notifyDataSetChanged();
        saveCommands();
    }

    private void saveCommands() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(mDevice.getAddress(), mListAdapter.getCommands());
        editor.commit();
    }

    private void restoreCommands() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the feeds
        Set<String> commandStrs = preferences.getStringSet(mDevice.getAddress(), getDefaultCommands());
        for (String cmdStr : commandStrs) {
            try {
                mListAdapter.addCommand(new Command(new JSONObject(cmdStr)));
            } catch (JSONException ex) {
                Log.e(TAG, "Bad JSON " + cmdStr);
            }
        }
    }

    // default set of commands
    private Set<String> getDefaultCommands() {
        Set<String> defCmds = new HashSet<String>();
        Command cmd;

        try {
            cmd = new Command("Download files", R.drawable.ic_sample_3, "Serial OBEX FTP", "", false, false, true);
            defCmds.add(cmd.toJSON().toString());
            cmd = new Command("Launch Rocket", R.drawable.ic_launcher, "/usr/bin/launch", "", false, false, false);
            defCmds.add(cmd.toJSON().toString());
            cmd = new Command("Video recording", R.drawable.ic_sample_10, "/usr/bin/video start", "/usr/bin/video stop", false, false, false);
            defCmds.add(cmd.toJSON().toString());
            cmd = new Command("Record GPS data", R.drawable.ic_sample_8, "/usr/bin/gps start", "/usr/bin/gps stop", false, false, false);
            defCmds.add(cmd.toJSON().toString());
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON object " + ex.toString());
        }

        return defCmds;
    }

    // Touch listener to detect swipe
    private class OnSwipeTouchListener implements View.OnTouchListener {

        ListView list;
        private GestureDetector gestureDetector;
        private Context context;

        public OnSwipeTouchListener(Context ctx, ListView list) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
            context = ctx;
            this.list = list;
        }

        public OnSwipeTouchListener() {
            super();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        public void onSwipeRight(int pos) {

        }

        public void onSwipeLeft(int pos) {

        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            private int getPostion(MotionEvent e1) {
                return list.pointToPosition((int) e1.getX(), (int) e1.getY());
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null)
                    return false;
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    int pos = getPostion(e1);
                    if (pos < 0)
                        return false;
                    if (distanceX > 0)
                        onSwipeRight(getPostion(e1));
                    else
                        onSwipeLeft(getPostion(e1));
                    return true;
                }
                return false;
            }

        }
    }

}