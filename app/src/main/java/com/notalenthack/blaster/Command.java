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

    private String mCommandLine;
    private String mName;
    private Status mStatus;
    private int mCpuUsage;

    public Command(String name, String cmdLine, Status status, int cpu) {
        mName = name;
        mCommandLine = cmdLine;
        mStatus = status;
        mCpuUsage = cpu;
    }

    public Command(String name, String cmdLine) {
        mName = name;
        mCommandLine = cmdLine;
        mStatus = Status.NOT_RUNNING;
        mCpuUsage = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mName);
        parcel.writeString(mCommandLine);
        parcel.writeInt(mStatus.ordinal());
        parcel.writeInt(mCpuUsage);
    }

    public static final Creator<Command> CREATOR = new ClassLoaderCreator<Command>() {
        @Override
        public Command createFromParcel(Parcel parcel, ClassLoader classLoader) {
            String name = parcel.readString();
            String cmd = parcel.readString();
            Status status = Status.values()[parcel.readInt()];
            int cpu = parcel.readInt();
            return new Command(name, cmd, status, cpu);
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

    public String getCommandLine() {
        return mCommandLine;
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

    public void setCpuUsage(int cpu) {
        mCpuUsage = cpu;
    }
}
