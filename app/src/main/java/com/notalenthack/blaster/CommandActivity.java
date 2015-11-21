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
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.notalenthack.blaster.dialog.EditCommandDialog;
import com.notalenthack.blaster.dialog.LaunchCommandDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

/**
 * Class that displays all the commands
 */
public class CommandActivity extends Activity implements EditCommandDialog.CommandCallback, View.OnClickListener  {
    private static final String TAG = "CommandActivity";
    private static final boolean D = false;

    private EdisonDevice mDevice;

    // Broadcast receiver for receiving intents
    private BroadcastReceiver mReceiver;

    // fields in the layout
    private TextView mModelName;
    private TextView mCores;
    private TextView mCacheSize;
    private TextView mConnectStatus;
    private ImageView mDeviceStatus;
    private ImageView mBatteryStatus;
    private ListView mCmdListView;
    private Button mBtnExecAll;
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
        mBtnExecAll = (Button)findViewById(R.id.btnExecAll);

        if (mDevice != null) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);

            mSerialService = new BluetoothSerialService(this, mHandlerBT);
            mSerialService.connect(mDevice.getBluetoothDevice());

            // Execute all the commands
            mBtnExecAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // do a quick status update on the commands
                    List<Command> commands = mListAdapter.getCommands();
                    // this list should be in the same order as in the ListBox
                    for (Command cmd : commands) {
                        if (!cmd.getCommandStart().equalsIgnoreCase(Command.OBEX_FTP_START)) {
                            String outType = Constants.SERIAL_TYPE_STDERR;
                            if (cmd.getDisplayOutput()) {
                                outType = Constants.SERIAL_TYPE_STDOUT_ERR; // capture stdout+stderr
                            }
                            mSerialService.sendCommand(Constants.SERIAL_CMD_START,
                                    cmd.getCommandStart(), outType);
                        }
                    }
                }
            });

            setupCommandList();

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

        // do a quick status update on the commands
        List<Command> commands = mListAdapter.getCommands();
        // this list should be in the same order as in the ListBox
        int i = 0;
        for (Command cmd : commands) {
            mSerialService.sendStatusCommand(cmd.getCommandStat(), i, true);
            i++;
        }
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
                                String state = jsonObject.getString(Constants.KEY_PROCESS_STATE);
                                boolean quick = jsonObject.getInt(Constants.KEY_QUICK_STATUS) == 1 ? true : false;
                                if (!quick) {
                                    mListAdapter.updateCpuUsage(id, percent);
                                }
                                mListAdapter.updateStatus(id, getStatusFromState(state));
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

        setupFilter();

        startStatusUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancelStatusUpdate();

        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;

        if (mSerialService != null)
            mSerialService.stop();
    }

    public void send(byte[] out) {

        out = Utils.handleEndOfLineChars(out);

        if (out.length > 0) {
            mSerialService.write(out);
        }
    }

    /* Mapping Linux state to our Enum status
     *  http://man7.org/linux/man-pages/man5/proc.5.html
    *
    *  R  Running
    *  S  Sleeping in an interruptible wait
    *  D  Waiting in uninterruptible disk sleep
    *  Z  Zombie
    *  T  Stopped (on a signal) or (before Linux 2.6.33) trace stopped
    *  t  Tracing stop (Linux 2.6.33 onward)
    *  X  Dead (from Linux 2.6.0 onward)
    */
    private Command.Status getStatusFromState(String state) {
        // only care about R, S, Z
        byte[] s = state.getBytes();
        switch (s[0]) {
            case 'R': return Command.Status.RUNNING;
            case 'S': return Command.Status.SLEEPING;
            case 'Z': return Command.Status.ZOMBIE;
            default: return Command.Status.NOT_RUNNING;
        }
    }

    private void editCommand(Command command) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(EditCommandDialog.TAG_EDIT_COMMAND_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = EditCommandDialog.newInstance(command, this);
        newFragment.show(ft, EditCommandDialog.TAG_EDIT_COMMAND_DIALOG);
    }

    private void launchCommand() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(LaunchCommandDialog.TAG_LAUNCH_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = LaunchCommandDialog.newInstance();
        newFragment.show(ft, LaunchCommandDialog.TAG_LAUNCH_DIALOG);
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
    public void newCommand(Command command, boolean bNew) {
        if (D) Log.d(TAG, "command is done " + command.getName());
        if (bNew) {
            mListAdapter.addCommand(1, command);
        }
        mListAdapter.notifyDataSetChanged();
        saveCommands();
    }

    private void handlePlayCommand(int position) {
        Command command = mListAdapter.getCommand(position);
        if (command != null) {
            if (D) Log.d(TAG, "command " + command.getCommandStart());
            if (command.getCommandStart().equalsIgnoreCase(Command.OBEX_FTP_START)) {
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
                            command.getCommandStart(), outType);
                } else if (status == Command.Status.ZOMBIE) {
                    mSerialService.sendCommand(Constants.SERIAL_CMD_KILL,
                            command.getCommandStart(), outType);
                } else if (status == Command.Status.RUNNING || status == Command.Status.SLEEPING) {
                    mSerialService.sendCommand(Constants.SERIAL_CMD_START,
                            command.getCommandStop(), outType);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Unknown command state: " + status, Toast.LENGTH_SHORT).show();
                    return;
                }
                mSerialService.sendStatusCommand(command.getCommandStat(), position, true);
            }
        }
    }

    // Setup receiver to get message from alarm
    private void setupFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_REFRESH_STATUS);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Got intent to clean up
                if (intent.getAction().equals(Constants.ACTION_REFRESH_STATUS)) {
                    if (D) Log.d(TAG, "onReceive intent " + intent.toString());
                    List<Command> commands = mListAdapter.getCommands();
                    // this list should be in the same order as in the ListBox
                    int i=0;
                    for (Command cmd : commands) {
                        if (cmd.getDisplayStatus()) {
                            mSerialService.sendStatusCommand(cmd.getCommandStat(), i, false);
                        }
                        i++;
                    }
                }
            }
        };
        registerReceiver(mReceiver, filter);
    }

    private void startStatusUpdate() {
        // Setup expiration if we never get a message from the service
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_REFRESH_STATUS);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set repeating updating of status, will need to cancel if activity is gone
        Calendar cal = Calendar.getInstance();
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                Constants.UPATE_STATUS_PERIOD * 1000, pi);
    }

    private void cancelStatusUpdate() {
        // Setup expiration if we never get a message from the service
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_REFRESH_STATUS);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(pi);
    }

    private void saveCommands() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(mDevice.getAddress(), mListAdapter.getCommandsAsJSONArray().toString());
        editor.commit();
    }

    private void restoreCommands() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the stored command
        String jsonArrStr = preferences.getString(mDevice.getAddress(), getDefaultCommands().toString());
        try {
            JSONArray jsonArray = new JSONArray(jsonArrStr);
            int len = jsonArray.length();
            for(int i = 0; i < len; ++i) {
                JSONObject json = jsonArray.getJSONObject(i);
                Command command = new Command(json);
                command.setStatus(Command.Status.NOT_RUNNING);
                command.setCpuUsage(0);
                mListAdapter.addCommand(command);
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
            cmd = new Command("Download files", R.drawable.ic_sample_3, Command.OBEX_FTP_START, Command.OBEX_FTP_STOP, Command.OBEX_FTP_STAT, false, true, true);
            jsonArray.put(cmd.toJSON());
            cmd = new Command("Video recording", R.drawable.ic_sample_10, "/bin/ls", "/usr/bin/video stop", "", false, false, false);
            jsonArray.put(cmd.toJSON());
            cmd = new Command("Record GPS data", R.drawable.ic_sample_8, Command.LSM9DS0_START, Command.LSM9DS0_STOP, Command.LSM9DS0_STAT, false, false, false);
            jsonArray.put(cmd.toJSON());
            cmd = new Command("Launch Rocket", R.drawable.ic_launcher, Command.LAUNCHER_START, Command.LAUNCHER_STOP, Command.LAUNCHER_STAT, false, false, false);
            jsonArray.put(cmd.toJSON());
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON object " + ex.toString());
            return null;
        }

        return jsonArray;
    }
}