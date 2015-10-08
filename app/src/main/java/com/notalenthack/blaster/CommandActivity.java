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
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Class that displays all the commands
 */
public class CommandActivity extends Activity {
    private static final String TAG = "CommandActivity";
    private static final boolean D = true;

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

    private BluetoothSerialService mSerialService = null;

    private BluetoothObexClient mObexClient = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Intent launchingIntent = getIntent();

        // Retrieve the Query Type and Position from the intent or bundle
        if (savedInstanceState != null) {
            mDevice = savedInstanceState.getParcelable(Constants.DEVICE_STATE);
        } else if (launchingIntent != null) {
            mDevice = launchingIntent.getParcelableExtra(Constants.DEVICE_STATE);
        }

        setContentView(R.layout.commands);
        // get the fields
        mModelName = (TextView)findViewById(R.id.modelName);
        mVendorId = (TextView)findViewById(R.id.vendorId);
        mCpuSpeed = (TextView)findViewById(R.id.cpuSpeed);
        mCacheSize = (TextView)findViewById(R.id.cacheSize);
        mConnectStatus = (TextView)findViewById(R.id.status);
        mDeviceStatus = (ImageView)findViewById(R.id.deviceStatusIcon);
        mBatteryStatus = (ImageView)findViewById(R.id.batteryStatus);
        mCmdListView = (ListView)findViewById(R.id.listView);;

        if (mDevice != null) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);

            mSerialService = new BluetoothSerialService(this, mHandlerBT);

            mObexClient = new BluetoothObexClient(this, mHandlerBT);

            mSerialService.connect(mDevice.getBluetoothDevice());
            mObexClient.connect(mDevice.getBluetoothDevice());

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
        /*            mSerialService.connect(mDevice.getBluetoothDevice());
                    mObexClient.connect(mDevice.getBluetoothDevice());
                    mObexClient.browseFolder(""); */
                case Constants.MESSAGE_STATE_CHANGE_CMD:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
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
                        case BluetoothSerialService.STATE_CANTCONNECT:
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
    protected void onStart() {
        super.onStart();
        mSerialService.connect(mDevice.getBluetoothDevice());
        mObexClient.connect(mDevice.getBluetoothDevice());
        mObexClient.browseFolder("");
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

        if ( out.length > 0 ) {
            mSerialService.write( out );
        }
    }

    // Select the icon
    private void selectIcon() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.icon_selection);
        dialog.setTitle(getString(R.string.select_icon));
        int width = getResources().getDisplayMetrics().widthPixels;
        int columns = (width / ImageAdapter.ICON_SIZE) - 1;

        GridView gridview = (GridView)dialog.findViewById(R.id.gridview);
        gridview.setNumColumns(columns);
        gridview.setColumnWidth(ImageAdapter.ICON_SIZE);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (D) Log.i(TAG, "Selection = " + position + " id " + id);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void editCommand(Command command) {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.command_detail_dlg);
        dialog.setTitle(getString(R.string.command_details));

        final EditText nameText=(EditText)dialog.findViewById(R.id.name);
        final EditText startText=(EditText)dialog.findViewById(R.id.startCommand);
        final EditText stopText=(EditText)dialog.findViewById(R.id.stopCommand);

        final ImageButton iconButton=(ImageButton)dialog.findViewById(R.id.commandButton);
        iconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectIcon();
            }
        });

        final CheckBox outputCheck = (CheckBox)dialog.findViewById(R.id.captureOutput);
        outputCheck.setChecked(false);  // default to false
        outputCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (outputCheck.isChecked()) {
                }
            }
        });

        final CheckBox statusCheck = (CheckBox)dialog.findViewById(R.id.displayStatus);
        statusCheck.setChecked(false);  // default to false
        statusCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statusCheck.isChecked()) {
                }
            }
        });

        final Button btnCancel=(Button)dialog.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (D) Log.d(TAG, "Cancel out of edit details...");
                dialog.dismiss();
            }
        });

        // Okay
        final Button okay=(Button)dialog.findViewById(R.id.okay);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (D) Log.d(TAG, "Okay out of edit details...");
            }
        });
        dialog.show();
    }
}
