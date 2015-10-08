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

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class DeviceListActivity extends ListActivity {

    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final String KEY_EDISON_ONLY = "EDISON_ONLY";

    private MenuItem mScanProgressItem;
    private MenuItem mScanMenuItem;
    private MenuItem mEdisonMenuItem;

    private BluetoothAdapter mBluetoothAdapter;
    private DeviceListAdapter mDevicesListAdapter;
    private int mCurrentPosition;
    private boolean mScanning = false;
    private boolean mEdisonOnly = true;

    private ProgressIndicator mIndicator = null;

    private void initialization() {
	    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    if (mBluetoothAdapter == null) {
	        finish();
	        return;
	    }

        mEdisonOnly = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_EDISON_ONLY, true);

        // Register the BroadcastReceiver
	    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    registerReceiver(mReceiver, filter);

	    // Register for broadcasts when discovery has finished
	    filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	    this.registerReceiver(mReceiver, filter);

	    filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
	    this.registerReceiver(mReceiver, filter);
    }

    // The on-click listener for all devices in the ListViews
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Cancel discovery because it's costly and we're about to connect
        stopScanning();

        mCurrentPosition = position;

        EdisonDevice device = mDevicesListAdapter.getDevice(position);
        if (device != null
           && (device.getStatus() == EdisonDevice.Status.PAIRED
             || device.getStatus() == EdisonDevice.Status.CONNECTED)) {
           if (D) Log.d(TAG, "device " + device.getName() + " bonded");

            Intent launchingIntent = new Intent(this, CommandActivity.class);
            launchingIntent.putExtra(Constants.DEVICE_STATE, device);

            if (D) Log.d(TAG, "Launch command screen: " + device.getName());

            startActivity(launchingIntent);

        } else {
            pairDevice(device.getBluetoothDevice());
        }
    }

    private void stopScanning() {
        mScanning = false;
        if (mScanMenuItem != null)
            mScanMenuItem.setTitle(R.string.start_scan);

        if (mScanProgressItem != null)
            mScanProgressItem.setActionView(null);

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }
    /**
     * Start device discover with the BluetoothAdapter
     */
    private void startScanning() {
        mScanning = true;
        mDevicesListAdapter.clearList();
        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            if (mEdisonOnly) {
                String name = device != null ? device.getName() : "";
                if (name != null && name.startsWith(getString(R.string.edison)))
                    mDevicesListAdapter.addDevice(device);
            } else {
                mDevicesListAdapter.addDevice(device);
            }
        }
        mCurrentPosition = -1;
        if (mScanProgressItem != null) {
            mScanProgressItem.setActionView(R.layout.actionbar_indeterminate_progress);
        }
        if (mScanMenuItem != null) {
            mScanMenuItem.setTitle(R.string.stop_scan);
        }

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
	    mBluetoothAdapter.startDiscovery();
    }

    private void pairDevice(BluetoothDevice device) {
	    try {
	        if (D)
		    Log.d(TAG, "Start Pairing...");

	        Method m = device.getClass()
		        .getMethod("createBond", (Class[]) null);
	        m.invoke(device, (Object[]) null);
	    } catch (Exception e) {
	        Log.e(TAG, e.getMessage());
	    }
    }

    private void unpairDevice(BluetoothDevice device) {
	    try {
	        Method m = device.getClass()
		        .getMethod("removeBond", (Class[]) null);
	        m.invoke(device, (Object[]) null);
	    } catch (Exception e) {
	        Log.e(TAG, e.getMessage());
	    }
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mEdisonOnly) {
                    String name = device != null ? device.getName() : "";
                    if (name != null && name.startsWith(getString(R.string.edison)))
                        mDevicesListAdapter.addDevice(device);
                } else {
                    mDevicesListAdapter.addDevice(device);
                }
                mDevicesListAdapter.notifyDataSetChanged();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mScanning = false;
                if (mScanMenuItem != null)
                    mScanMenuItem.setTitle(R.string.start_scan);

                if (mScanProgressItem != null)
                    mScanProgressItem.setActionView(null);
            }
            // When the device bond state changed.
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            int prevBondState = intent.getIntExtra(
                BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
            int bondState = intent.getIntExtra(
                BluetoothDevice.EXTRA_BOND_STATE, -1);

            if (prevBondState == BluetoothDevice.BOND_BONDED
                && bondState == BluetoothDevice.BOND_NONE) {
                BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mCurrentPosition != -1
                    && mCurrentPosition < mDevicesListAdapter.getCount()) {
                    BluetoothDevice device1 = mDevicesListAdapter.getDevice(mCurrentPosition).getBluetoothDevice();
                    if (device.getAddress().compareTo(device1.getAddress()) == 0) {
                        mDevicesListAdapter.updateStatus(mCurrentPosition, EdisonDevice.Status.NONE);
                        mDevicesListAdapter.notifyDataSetChanged();
                    }
                }
            } else if (prevBondState == BluetoothDevice.BOND_BONDING
                && bondState == BluetoothDevice.BOND_BONDED) {
                BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mCurrentPosition != -1
                    && mCurrentPosition < mDevicesListAdapter.getCount()) {
                    BluetoothDevice device1 = mDevicesListAdapter.getDevice(mCurrentPosition).getBluetoothDevice();
                    if (device.getAddress().compareTo(device1.getAddress()) == 0) {
                        mDevicesListAdapter.updateStatus(mCurrentPosition, EdisonDevice.Status.PAIRED);
                        mDevicesListAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        initialization();


        mDevicesListAdapter = new DeviceListAdapter(this);
        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            if (mEdisonOnly) {
                String name = device != null ? device.getName() : "";
                if (name != null && name.startsWith(getString(R.string.edison)))
                    mDevicesListAdapter.addDevice(device);
            } else {
                mDevicesListAdapter.addDevice(device);
            }
        }

        setListAdapter(mDevicesListAdapter);
        startScanning();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

	    if (!mBluetoothAdapter.isEnabled()) {
	        Intent enableIntent = new Intent(
		        BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        startActivityForResult(enableIntent, DeviceListActivity.REQUEST_ENABLE_BT);
	        // Otherwise, setup the chat session
	    }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_scan || id == R.id.scanning_indicator) {
            if (mScanning) {
                stopScanning();
            } else {
                startScanning();
            }

            invalidateOptionsMenu();
        } else if (id == R.id.action_edison_only) {
            mEdisonOnly = !mEdisonMenuItem.isChecked();   // toggle
            mEdisonMenuItem.setChecked(mEdisonOnly);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(KEY_EDISON_ONLY, mEdisonOnly);
            if (mScanning)
                stopScanning();
            startScanning();
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device_list, menu);
        mScanProgressItem = menu.findItem(R.id.scanning_indicator);
        mScanMenuItem = menu.findItem(R.id.action_scan);
        mEdisonMenuItem = menu.findItem(R.id.action_edison_only);

        // check for BLE
        mEdisonMenuItem.setChecked(mEdisonOnly);

        if (mScanning) {
            mScanMenuItem.setTitle(R.string.stop_scan);
            mScanProgressItem.setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            mScanMenuItem.setTitle(R.string.start_scan);
            mScanProgressItem.setActionView(null);
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
	    case DeviceListActivity.REQUEST_ENABLE_BT:
	        // When the request to enable Bluetooth returns
	        if (resultCode == Activity.RESULT_OK) {
		    // Bluetooth is now enabled, so set up a chat session
		        startScanning();
	        } else {
		        // User did not enable Bluetooth or an error occured
		        if (D)
		        Log.d(TAG, "BT not enabled");
		        Toast.makeText(this, R.string.bt_not_enabled_leaving,
			    Toast.LENGTH_SHORT).show();
		        finish();
	        }
	    }
	    super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
	    super.onDestroy();

	    // Make sure we're not doing discovery anymore
	    if (mBluetoothAdapter != null) {
	        mBluetoothAdapter.cancelDiscovery();
	    }

	    // Unregister broadcast listeners
	    this.unregisterReceiver(mReceiver);
    }
}
