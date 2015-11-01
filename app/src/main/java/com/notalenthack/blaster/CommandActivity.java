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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.notalenthack.blaster.dialog.EditCommandDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that displays all the commands
 */
public class CommandActivity extends Activity implements EditCommandDialog.CommandCallback, View.OnClickListener  {
    private static final String TAG = "CommandActivity";
    private static final boolean D = true;

    private static final String OBEX_FTP = "Serial OBEX FTP";

    private EdisonDevice mDevice;

    // fields in the layout
    private TextView mModelName;
    private TextView mCores;
    private TextView mCacheSize;
    private TextView mConnectStatus;
    private ImageView mDeviceStatus;
    private ImageView mBatteryStatus;
    private ListView mCmdListView;
    private Activity mThisActivity;

    private View mPrevSlideOut = null;

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
            mDevice = savedInstanceState.getParcelable(Constants.KEY_DEVICE_STATE);
        } else if (launchingIntent != null) {
            mDevice = launchingIntent.getParcelableExtra(Constants.KEY_DEVICE_STATE);
        }

        setContentView(R.layout.commands);
        // get the fields
        mModelName = (TextView) findViewById(R.id.modelName);
        mCores = (TextView) findViewById(R.id.cores);
        mCacheSize = (TextView) findViewById(R.id.cacheSize);
        mConnectStatus = (TextView) findViewById(R.id.status);
        mDeviceStatus = (ImageView) findViewById(R.id.deviceStatusIcon);
        mBatteryStatus = (ImageView) findViewById(R.id.batteryStatus);
        mCmdListView = (ListView) findViewById(R.id.listView);;

        if (mDevice != null) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);

            mSerialService = new BluetoothSerialService(this, mHandlerBT);

            setupCommandList();

            mSerialService.connect(mDevice.getBluetoothDevice());

        } else {
            Log.e(TAG, "Bluetooth device is not initialized");
            finish();
        }
    }

    private void setupCommandList() {
        mListAdapter = new CommandListAdapter(this);
        // read from storage and initialze the adapter.
        restoreCommands();

        mCmdListView.setAdapter(mListAdapter);
        mCmdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePlayCommand(position);
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
                            // connected, so ask for CPU Info
                            mSerialService.sendCommand(Constants.SERIAL_CMD_CPU_INFO);
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
                        Log.d(TAG, "read data: " + msg.obj);
                    }
                    break;

                // command coming back from device
                case Constants.MESSAGE_DEVICE_SERIAL_CMD:
                    String jsonStr = msg.getData().getString(Constants.KEY_JSON_STR);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStr);
                        int cmd = jsonObject.getInt(Constants.KEY_COMMAND_TYPE);
                        switch (cmd) {
                            case Constants.SERIAL_CMD_ERROR:
                                String errStr = jsonObject.getString(Constants.KEY_TOAST);
                                Toast.makeText(getApplicationContext(), errStr, Toast.LENGTH_SHORT).show();
                                break;

                            case Constants.SERIAL_CMD_CLOSE:
                                finish(); // assuming that onDestroy is called to clean up
                                break;

                            case Constants.SERIAL_CMD_STATUS:
                                int percent = jsonObject.getInt(Constants.KEY_PERCENT);
                                int id = jsonObject.getInt(Constants.KEY_IDENTIFIER);
                                mListAdapter.updateCpuUsage(id, percent);
                                mListAdapter.notifyDataSetChanged();
                                break;

                            case Constants.SERIAL_CMD_CPU_INFO:
                                String modelName = jsonObject.getString(Constants.KEY_MODEL_NAME);
                                int cores = jsonObject.getInt(Constants.KEY_CPU_CORES);
                                String cacheSize = jsonObject.getString(Constants.KEY_CACHE_SIZE);
                                mModelName.setText(modelName);
                                mCores.setText(cores + " cores");
                                mCacheSize.setText(cacheSize + " cache");
                                break;

                        }
                    } catch (JSONException ex) {
                        Log.e(TAG, "Invalid JSON " + ex.getMessage());
                        Toast.makeText(getApplicationContext(),
                                "Invalid JSON commadn from device " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;

                case Constants.MESSAGE_DEVICE_NAME_CMD:
                    // save the connected device's name
                    String deviceName = msg.getData().getString(Constants.KEY_DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_connected_to) + " "
                            + deviceName, Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_TOAST_CMD:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(Constants.KEY_TOAST), Toast.LENGTH_LONG).show();
                    break;
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
        menuInflater.inflate(R.menu.add_command, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mSerialService.sendCommand(Constants.SERIAL_CMD_CLOSE);
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


    @Override
    public void onClick(View v) {
        Integer position = (Integer)  v.getTag();
        if (v.getId() == R.id.btnEditCommand) {
            final Command cmd = mListAdapter.getCommand(position);
            Log.d(TAG, "Edit button click for position " + position);
            //Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(this, v);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.edit_delete, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.edit) {
                        editCommand(cmd);
                    } else if (item.getItemId() == R.id.delete) {
                        mListAdapter.deleteCommand(cmd);
                    } else {
                        return false;
                    }
                    saveCommands(); // update commands in pref for presistent
                    mListAdapter.notifyDataSetChanged();
                    return true;
                }
            });

            // show the popup
            popup.show();

        } else if (v.getId() == R.id.btnCommandAction) {
            Log.d(TAG, "Play button click for position " + position);
            handlePlayCommand(position);
        }
    }

    // Callback when a command is created
    public void newCommand(Command command) {
        if (D) Log.d(TAG, "command is done " + command.getName());
        mListAdapter.addCommand(command);
        mListAdapter.notifyDataSetChanged();
        saveCommands();
    }

    private void handlePlayCommand(int position) {
        Command command = mListAdapter.getCommand(position);
        if (command != null) {
            if (D) Log.d(TAG, "command " + command.getCommandStart());
            if (command.getCommandStart().equalsIgnoreCase(OBEX_FTP)) {
                Intent launchingIntent = new Intent(mThisActivity, FileListActivity.class);
                launchingIntent.putExtra(Constants.KEY_DEVICE_STATE, mDevice);

                if (D) Log.d(TAG, "Launch file list screen: " + mDevice.getName());

                startActivity(launchingIntent);
            } else {
                Command.Status status = command.getStatus();
                String outType = Constants.SERIAL_TYPE_STDERR;
                if (command.getDisplayOutput()) {
                    outType = Constants.SERIAL_TYPE_STDOUT_ERR; // capture stdout+stderr
                }
                if (status == Command.Status.NOT_RUNNING) {
                    mSerialService.sendCommand(Constants.SERIAL_CMD_START,
                            outType, command.getCommandStart());
                    command.setStatus(Command.Status.RUNNING);
                } else if (status == Command.Status.ZOMBIE) {
                    mSerialService.sendCommand(Constants.SERIAL_CMD_KILL,
                            outType, command.getCommandStart());
                    command.setStatus(Command.Status.NOT_RUNNING);
                } else if (status == Command.Status.RUNNING) {
                    mSerialService.sendCommand(Constants.SERIAL_CMD_START,
                            outType, command.getCommandStop());
                    command.setStatus(Command.Status.NOT_RUNNING);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Unknown command state: " + status, Toast.LENGTH_SHORT).show();
                    return;
                }
                mListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void saveCommands() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(mDevice.getAddress(), mListAdapter.getCommands());
        editor.commit();
    }

    private void restoreCommands() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the feeds
        String jsonArrStr = preferences.getString(mDevice.getAddress(), getDefaultCommands().toString());
        try {
            JSONArray jsonArray = new JSONArray(jsonArrStr);
            int len = jsonArray.length();
            for(int i = 0; i < len; ++i) {
                JSONObject json = jsonArray.getJSONObject(i);
                mListAdapter.addCommand(new Command(json));
            }
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON " + ex.getMessage());
        }
    }

    // default set of commands
    private JSONArray getDefaultCommands() {
        Command cmd;
        JSONArray jsonArray = new JSONArray();

        try {
            cmd = new Command("Download files", R.drawable.ic_sample_3, OBEX_FTP, OBEX_FTP, false, false, true);
            jsonArray.put(cmd.toJSON());
            cmd = new Command("Launch Rocket", R.drawable.ic_launcher, "/bin/ls /abc", "", false, false, false);
            jsonArray.put(cmd.toJSON());
            cmd = new Command("Video recording", R.drawable.ic_sample_10, "/bin/ls", "/usr/bin/video stop", false, false, false);
            jsonArray.put(cmd.toJSON());
            cmd = new Command("Record GPS data", R.drawable.ic_sample_8, "/bin/ls", "/usr/bin/gps stop", false, false, false);
            jsonArray.put(cmd.toJSON());
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON object " + ex.toString());
            return null;
        }

        return jsonArray;
    }
}