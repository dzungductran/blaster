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
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class wraps an Edison device
 */
public class Command implements Parcelable {
    private static final String TAG = Command.class.getCanonicalName();

    private static final String KEY_CMD_START="cmdStart";
    private static final String KEY_CMD_STOP="cmdStop";
    private static final String KEY_NAME="name";
    private static final String KEY_STATUS="status";
    private static final String KEY_CPU_USAGE="cpuUsage";
    private static final String KEY_DISPLAY_OUTPUT="displayOutput";
    private static final String KEY_DISPLAY_STATUS="displayStatus";
    private static final String KEY_SYSTEM_CMD="systemCommand";
    private static final String KEY_RES_ID="resId";

    public enum Status { RUNNING, SLEEPING, ZOMBIE, NOT_RUNNING };

    private String mCommandStart;
    private String mCommandStop;
    private String mName;
    private Status mStatus;
    private boolean mSystemCmd;    // System command, can't delete or edit
    private int mCpuUsage;
    private boolean mDisplayOutput;
    private boolean mDisplayStatus;
    private int mCommandResId;

    public Command() {
       this("", R.drawable.unknown_item, "", "", Status.NOT_RUNNING, 0, false, false, false);
    }

    public Command(String name, int resId, String cmdStart, String cmdStop,
                   Status status, int cpu, boolean bOutput, boolean bStatus, boolean bSystem) {
        mName = name;
        mCommandResId = resId;
        mCommandStart = cmdStart;
        mCommandStop = cmdStop;
        mStatus = status;
        mCpuUsage = cpu;
        mDisplayOutput = bOutput;
        mDisplayStatus = bStatus;
        mSystemCmd = bSystem;
    }

    public Command(String name, int resId, String cmdStart, String cmdStop, boolean bOutput,
                   boolean bStatus, boolean bSystem) {
        this(name, resId, cmdStart, cmdStop, Status.NOT_RUNNING, 0, bOutput, bStatus, bSystem);
    }

    public Command(String name, String cmdStart, String cmdStop) {
        this(name, R.drawable.unknown_item, cmdStart, cmdStop, Status.NOT_RUNNING, 0, false, false, false);
    }

    public Command(JSONObject json) {
        try {
            mCommandStart = json.getString(KEY_CMD_START);
            mCommandStop = json.getString(KEY_CMD_STOP);
            mName = json.getString(KEY_NAME);
            mStatus = Status.values()[json.getInt(KEY_STATUS)];
            mCpuUsage = json.getInt(KEY_CPU_USAGE);
            mDisplayOutput = json.getBoolean(KEY_DISPLAY_OUTPUT);
            mDisplayStatus = json.getBoolean(KEY_DISPLAY_STATUS);
            mSystemCmd = json.getBoolean(KEY_SYSTEM_CMD);
            mCommandResId = json.getInt(KEY_RES_ID);
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON object for Command");
        }
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
        parcel.writeByte((byte) (mDisplayStatus ? 1 : 0));
        parcel.writeByte((byte) (mSystemCmd ? 1 : 0));
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
            boolean bSystem = parcel.readByte() != 0;
            return new Command(name, resId, cmdStart, cmdStop, status, cpu, bOutput, bStatus, bSystem);
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

    public boolean isSystemCommand() { return mSystemCmd; }

    // Quote into json key/value pair
    private String quoteToKeyValue(String key, String value) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"");
        buffer.append(key);
        buffer.append("\":\"");
        buffer.append(value);
        buffer.append("\"");
        return buffer.toString();
    }

    // Quote into json key/value pair
    private String quoteToKeyValue(String key, boolean value) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"");
        buffer.append(key);
        buffer.append("\":");
        buffer.append(value);
        return buffer.toString();
    }

    // Quote into json key/value pair
    private String quoteToKeyValue(String key, int value) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"");
        buffer.append(key);
        buffer.append("\":");
        buffer.append(value);
        return buffer.toString();
    }

    // Return the string representation of this object
    public JSONObject toJSON() throws JSONException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append(quoteToKeyValue(KEY_CMD_START, mCommandStart));
        buffer.append(",");
        buffer.append(quoteToKeyValue(KEY_CMD_STOP, mCommandStop));
        buffer.append(",");
        buffer.append(quoteToKeyValue(KEY_NAME, mName));
        buffer.append(",");
        buffer.append(quoteToKeyValue(KEY_STATUS, mStatus.ordinal()));
        buffer.append(",");
        buffer.append(quoteToKeyValue(KEY_CPU_USAGE, mCpuUsage));
        buffer.append(",");
        buffer.append(quoteToKeyValue(KEY_DISPLAY_OUTPUT, mDisplayOutput));
        buffer.append(",");
        buffer.append(quoteToKeyValue(KEY_DISPLAY_STATUS, mDisplayStatus));
        buffer.append(",");
        buffer.append(quoteToKeyValue(KEY_SYSTEM_CMD, mSystemCmd));
        buffer.append(",");
        buffer.append(quoteToKeyValue(KEY_RES_ID, mCommandResId));
        buffer.append("}");

        return new JSONObject(buffer.toString());
    }
}
