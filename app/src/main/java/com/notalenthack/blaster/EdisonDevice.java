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

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dtran on 6/30/14.
 */
public class EdisonDevice {
    public enum Status { PAIRED, PAIRING, CONNECTED, NONE };

    public static String DEFAULT_IP = "0.0.0.0";

    private BluetoothDevice mDevice;
    private Status mStatus;
    private String mIPAddress;
    private boolean mIPSet;

    public EdisonDevice(BluetoothDevice device, Status status, String ip) {
        mDevice = device;
        mStatus = status;
        mIPAddress = ip;
        mIPSet = false;
    }

    public EdisonDevice(BluetoothDevice device) {
        mDevice = device;
        if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            mStatus = Status.PAIRED;
        else if (device.getBondState() == BluetoothDevice.BOND_BONDING)
            mStatus = Status.PAIRING;
        else
            mStatus = Status.NONE;
        mIPAddress = DEFAULT_IP;     // initial ip address
    }

    public BluetoothDevice getBluetoothDevice() { return mDevice; }

    public String getIPAddress() {
        return mIPAddress;
    }

    public String getName() {
        return mDevice.getName();
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public boolean isIPSet() { return mIPSet; }

    public void setIPAddress(String ipAddress) {
        mIPAddress = ipAddress;
        mIPSet = true;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        if (status == Status.NONE) {
            if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
                mStatus = Status.PAIRED;
            else if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING)
                mStatus = Status.PAIRING;
            else if (mDevice.getBondState() == BluetoothDevice.BOND_NONE)
                mStatus = Status.NONE;
        } else {
            mStatus = status;
        }
    }
}
