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
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class wraps an Edison device
 */
public class Command implements Parcelable {
    public enum Status { RUNNING, SLEEPING, ZOMBIE, NOT_RUNNING };

    private String mCommandStart;
    private String mCommandStop;
    private String mName;
    private Status mStatus;
    private int mCpuUsage;
    private boolean mDisplayOutput;
    private boolean mDisplayStatus;
    private int mCommandResId;

    public Command() {
       this("", R.drawable.unknown_item, "", "", Status.NOT_RUNNING, 0, false, false);
    }

    public Command(String name, int resId, String cmdStart, String cmdStop,
                   Status status, int cpu, boolean bOutput, boolean bStatus) {
        mName = name;
        mCommandResId = resId;
        mCommandStart = cmdStart;
        mCommandStop = cmdStop;
        mStatus = status;
        mCpuUsage = cpu;
        mDisplayOutput = bOutput;
        mDisplayStatus = bStatus;
    }

    public Command(String name, String cmdStart, String cmdStop) {
        this(name, R.drawable.unknown_item, cmdStart, cmdStop, Status.NOT_RUNNING, 0, false, false);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mName);
        parcel.writeString(mCommandStart);
        parcel.writeString(mCommandStop);
        parcel.writeInt(mCommandResId);
        parcel.writeInt(mStatus.ordinal());
        parcel.writeInt(mCpuUsage);
        parcel.writeByte((byte) (mDisplayOutput ? 1 : 0));
        parcel.writeByte((byte)(mDisplayStatus ? 1 : 0));
    }

    public static final Creator<Command> CREATOR = new ClassLoaderCreator<Command>() {
        @Override
        public Command createFromParcel(Parcel parcel, ClassLoader classLoader) {
            String name = parcel.readString();
            String cmdStart = parcel.readString();
            String cmdStop = parcel.readString();
            int resId = parcel.readInt();
            Status status = Status.values()[parcel.readInt()];
            int cpu = parcel.readInt();
            boolean bOutput = parcel.readByte() != 0;
            boolean bStatus = parcel.readByte() != 0;
            return new Command(name, resId, cmdStart, cmdStop, status, cpu, bOutput, bStatus);
        }

        @Override
        public Command createFromParcel(Parcel parcel) {
            return createFromParcel(parcel, ClassLoader.getSystemClassLoader());
        }

        @Override
        public Command[] newArray(int i) {
            return new Command[i];
        }
    };

    public String getName() {
        return mName;
    }

    public String getCommandStart() {
        return mCommandStart;
    }

    public String getCommandStop() {
        return mCommandStop;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public int getCpuUsage() {
        return mCpuUsage;
    }

    public void setName(String name) { mName = name; }

    public void setStartCommand(String startCmd) { mCommandStart = startCmd; }

    public void setStopCommand(String stopCmd) { mCommandStop = stopCmd; }

    public void setCpuUsage(int cpu) {
        mCpuUsage = cpu;
    }

    public void setDisplayOutput(boolean b) { mDisplayOutput = b; }

    public boolean getDisplayOutput() { return mDisplayOutput; }

    public void setDisplayStatus(boolean b) { mDisplayStatus = b; }

    public boolean getDisplayStatus() { return mDisplayStatus; }

    public void setResourceId(int resId) { mCommandResId = resId; }

    public int getResourceId() { return mCommandResId; }
}
