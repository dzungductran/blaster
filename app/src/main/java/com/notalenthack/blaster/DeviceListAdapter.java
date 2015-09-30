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
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class DeviceListAdapter extends BaseAdapter {

	private ArrayList<EdisonDevice> mDevices;
    private HashMap<String, EdisonDevice> mMap;
	private LayoutInflater mInflater;

	public DeviceListAdapter(Activity par) {
		super();
		mDevices  = new ArrayList<EdisonDevice>();
        mMap = new HashMap<String, EdisonDevice>();
		mInflater = par.getLayoutInflater();
	}
	
	public void addDevice(BluetoothDevice device) {
		if(!mMap.containsKey(device.getAddress())) {
            EdisonDevice edisonDevice = new EdisonDevice(device);
            mMap.put(device.getAddress(), edisonDevice);
			mDevices.add(edisonDevice);
		}
	}

    public void addDevices(Set<BluetoothDevice> devices) {
        for (BluetoothDevice bluetoothDevice : devices) {
            addDevice(bluetoothDevice);
        }

    }
	
	public EdisonDevice getDevice(int index) {
		return mDevices.get(index);
	}

    public void clearList() {
        mDevices.clear();
        mMap.clear();
    }

    public void setIPAddress(int pos, String ipAddr) {
        EdisonDevice device = mDevices.get(pos);
        device.setIPAddress(ipAddr);
    }

    public void updateStatus(int pos, EdisonDevice.Status status) {
        EdisonDevice device = mDevices.get(pos);
        device.setStatus(status);
    }

    @Override
	public int getCount() {
		return mDevices.size();
	}

	@Override
	public Object getItem(int position) {
		return getDevice(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// get already available view or create new if necessary
		FieldReferences fields;
        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.activity_scanning_item, null);
        	fields = new FieldReferences();
        	fields.deviceAddress = (TextView)convertView.findViewById(R.id.deviceAddress);
        	fields.deviceName    = (TextView)convertView.findViewById(R.id.deviceName);
            fields.deviceStatus  = (TextView)convertView.findViewById(R.id.deviceStatus);
            fields.deviceIP      = (TextView)convertView.findViewById(R.id.deviceIP);

            convertView.setTag(fields);
        } else {
            fields = (FieldReferences) convertView.getTag();
        }			
		
        // set proper values into the view
        EdisonDevice device = mDevices.get(position);
        String name = device.getName();
        String address = device.getAddress();

        if(name == null || name.length() <= 0) name = "Unknown Device";
        
        fields.deviceName.setText(name);
        fields.deviceAddress.setText(address);

        if (device.getStatus() == EdisonDevice.Status.PAIRED) {
            fields.deviceStatus.setText(R.string.paired);
        } else if (device.getStatus() == EdisonDevice.Status.PAIRING) {
            fields.deviceStatus.setText(R.string.pairing);
        } else if (device.getStatus() == EdisonDevice.Status.NONE) {
            fields.deviceStatus.setText(R.string.not_pair);
        } else if (device.getStatus() == EdisonDevice.Status.CONNECTED) {
            fields.deviceStatus.setText(R.string.connected);
        }

        fields.deviceIP.setText(device.getIPAddress());
        if (device.isIPSet()) {
            if (device.getIPAddress().equals(EdisonDevice.DEFAULT_IP))
                fields.deviceIP.setTextColor(Color.RED);
            else
                fields.deviceIP.setTextColor(Color.GREEN);
        }

		return convertView;
	}
	
	private class FieldReferences {
		TextView deviceName;
		TextView deviceAddress;
        TextView deviceStatus;
        TextView deviceIP;
	}
}
