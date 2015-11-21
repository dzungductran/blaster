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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * File entry
 */
public class FileEntry implements Parcelable {
    public String name;
    public String path;
    public String perm;
    public long size;
    public long accessTime;
    public boolean bFolder;
    public int drawableId;
    public int downloadProgress;

    public FileEntry(String name, String path, String perm, long size, long accessTime, int drawableId, boolean bFolder, int downloadProgress) {
        this.name = name;
        this.path = path;
        this.perm = perm;
        this.size = size;
        this.accessTime = accessTime;
        this.drawableId = drawableId;
        this.bFolder = bFolder; // Is folder or file
        this.downloadProgress = downloadProgress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.name);
        parcel.writeString(this.path);
        parcel.writeString(this.perm);
        parcel.writeLong(this.size);
        parcel.writeLong(this.accessTime);
        parcel.writeInt(this.drawableId);
        parcel.writeInt(this.downloadProgress);
        parcel.writeByte((byte) (this.bFolder ? 1 : 0));
    }

    public static final Creator<FileEntry> CREATOR = new ClassLoaderCreator<FileEntry>() {
        @Override
        public FileEntry createFromParcel(Parcel parcel, ClassLoader classLoader) {
            String name = parcel.readString();
            String path = parcel.readString();
            String perm = parcel.readString();
            long size = parcel.readLong();
            long accessTime = parcel.readLong();
            int drawableId = parcel.readInt();
            int downProg = parcel.readInt();
            boolean bFolder = parcel.readByte() != 0;
            return new FileEntry(name, path, perm, size, accessTime, drawableId, bFolder, downProg);
        }

        @Override
        public FileEntry createFromParcel(Parcel parcel) {
            return createFromParcel(parcel, ClassLoader.getSystemClassLoader());
        }

        @Override
        public FileEntry[] newArray(int i) {
            return new FileEntry[i];
        }
    };

}
