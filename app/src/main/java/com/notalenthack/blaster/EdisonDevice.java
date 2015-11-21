/*
Copyright © 2015-2016 Dzung Tran (dzungductran@yahoo.com)

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
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class wraps an Edison device
 */
public class EdisonDevice implements Parcelable {
    public enum Status { PAIRED, PAIRING, CONNECTED, NONE };

    private BluetoothDevice mDevice;
    private Status mStatus;

    public EdisonDevice(BluetoothDevice device, Status status) {
        mDevice = device;
        mStatus = status;
    }

    public EdisonDevice(BluetoothDevice device) {
        mDevice = device;
        if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            mStatus = Status.PAIRED;
        else if (device.getBondState() == BluetoothDevice.BOND_BONDING)
            mStatus = Status.PAIRING;
        else
            mStatus = Status.NONE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mStatus.ordinal());
        mDevice.writeToParcel(parcel, flags);
    }

    public static final Creator<EdisonDevice> CREATOR = new ClassLoaderCreator<EdisonDevice>() {
        @Override
        public EdisonDevice createFromParcel(Parcel parcel, ClassLoader classLoader) {
            Status status = Status.values()[parcel.readInt()];
            BluetoothDevice device = BluetoothDevice.CREATOR.createFromParcel(parcel);
            return new EdisonDevice(device, status);
        }

        @Override
        public EdisonDevice createFromParcel(Parcel parcel) {
            return createFromParcel(parcel, ClassLoader.getSystemClassLoader());
        }

        @Override
        public EdisonDevice[] newArray(int i) {
            return new EdisonDevice[i];
        }
    };

    public BluetoothDevice getBluetoothDevice() { return mDevice; }

    public String getName() {
        return mDevice.getName();
    }

    public String getAddress() {
        return mDevice.getAddress();
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
