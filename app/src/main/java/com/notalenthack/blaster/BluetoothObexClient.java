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
/*
 * Client for FTP
*/
package com.notalenthack.blaster;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.ObexTransport;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

/**
 * The Obex client class runs its own message handler thread,
 * to avoid executing long operations
 * hence all call-backs (and thereby transmission of data) is executed
 * from this thread.
 */
public class BluetoothObexClient {
    private static final String TAG = "BluetoothMnsObexClient";
    private static final boolean D = false;
    private ObexTransport mTransport;
    private Context mContext;
    public Handler mHandler = null;
    private volatile boolean mWaitingForRemote;
    private static final String TYPE_EVENT = "x-bt/MAP-event-report";
    private ClientSession mClientSession;
    private boolean mConnected = false;
    private BluetoothDevice mDevice = null;
    private Handler mCallback = null;
    private String mCurrentFolder = null;

    // Used by the
    public static final int MSG_BROWSE_FOLDER = 1;
    public static final int MSG_DOWNLOAD_FILE = 2;

    public static final UUID BluetoothUuid_Obex =
            UUID.fromString("00001106-0000-1000-8000-00805f9b34fb");
    public BluetoothObexClient(Context context, Handler callback) {
        HandlerThread thread = new HandlerThread("BluetoothMnsObexClient");
        thread.start();
        Looper looper = thread.getLooper();
        mHandler = new ObexClientHandler(looper);
        mContext = context;
        mCallback = callback;
    }

    public Handler getMessageHandler() {
        return mHandler;
    }

    private final class ObexClientHandler extends Handler {
        private ObexClientHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BROWSE_FOLDER:
                    String folder = msg.getData().getString(Constants.KEY_FOLDER_NAME);
                    handleBrowse(folder);
                    break;
                case MSG_DOWNLOAD_FILE:
                    folder = msg.getData().getString(Constants.KEY_FOLDER_NAME);
                    String file = msg.getData().getString(Constants.KEY_FILE_NAME);
                    long expectedSize = msg.getData().getLong(Constants.KEY_FILE_SIZE);
                    handleDownload(folder, file, expectedSize);
                default:
                    break;
            }
        }
    }
    
    public boolean isConnected() {
        return mConnected;
    }
    /**
     * Disconnect the connection to MNS server.
     * Call this when the MAS client requests a de-registration on events.
     */
    public void disconnect() {
        try {
            if (mClientSession != null) {
                mClientSession.disconnect(null);
                if (D) Log.d(TAG, "OBEX session disconnected");
            }
        } catch (IOException e) {
            Log.w(TAG, "OBEX session disconnect error " + e.getMessage());
        }
        try {
            if (mClientSession != null) {
                if (D) Log.d(TAG, "OBEX session close mClientSession");
                mClientSession.close();
                mClientSession = null;
                if (D) Log.d(TAG, "OBEX session closed");
            }
        } catch (IOException e) {
            Log.w(TAG, "OBEX session close error:" + e.getMessage());
        }
        if (mTransport != null) {
            try {
                if (D) Log.d(TAG, "Close Obex Transport");
                mTransport.close();
                mTransport = null;
                mConnected = false;
                if (D) Log.d(TAG, "Obex Transport Closed");
            } catch (IOException e) {
                Log.e(TAG, "mTransport.close error: " + e.getMessage());
                sendMessageToast("mTransport.close error: " + e.getMessage());
            }
        }
    }

    /**
     * Shutdown the service.
     */
    public void shutdown() {
        /* should shutdown handler thread first to make sure
         * we don't process message when disconnet
         */
        if (mHandler != null) {
            // Shut down the thread
            mHandler.removeCallbacksAndMessages(null);
            Looper looper = mHandler.getLooper();
            if (looper != null) {
                looper.quit();
            }
            mHandler = null;
        }
        /* Disconnect if connected */
        disconnect();
    }

    private HeaderSet hsConnect = null;

    public void connect(BluetoothDevice device) {
        // Already connecting or connected...
        if (mDevice != null && mDevice.getAddress().equals(device.getAddress())
                && mConnected) {
            return;
        }

        Log.d(TAG, "Connecting: connect 2");
        mDevice = device;
        BluetoothSocket btSocket = null;
        try {
            btSocket = device.createRfcommSocketToServiceRecord( BluetoothUuid_Obex );
            btSocket.connect();
        } catch (IOException e) {
            Log.e(TAG, "BtSocket Connect error " + e.getMessage(), e);
            sendMessageToast("BtSocket Connect error: " + e.getMessage());
            return;
        }
        mTransport = new BluetoothRfcommTransport(btSocket);
        try {
            mClientSession = new ClientSession(mTransport);
            mConnected = true;
        } catch (IOException e1) {
            Log.e(TAG, "OBEX session create error " + e1.getMessage());
            sendMessageToast("OBEX session create error: " + e1.getMessage());
        }
        if (mConnected && mClientSession != null) {
            mConnected = false;
            HeaderSet hs = new HeaderSet();
            //Target Header must be set to the FTP UUID:
            //F9EC7BC4-953C-11D2-984E-525400DC9E09.
            byte[] FTPUiid = {(byte)0xF9,(byte)0xEC,(byte)0x7B,(byte)0xC4,(byte)0x95,(byte)0x3C,(byte)0x11,(byte)0xD2,
                    (byte)0x98,(byte)0x4E,(byte)0x52,(byte)0x54,(byte)0x00,(byte)0xDC,(byte)0x9E,(byte)0x09};
            hs.setHeader(HeaderSet.TARGET, FTPUiid);
            synchronized (this) {
                mWaitingForRemote = true;
            }
            try {
                hsConnect = mClientSession.connect(hs);
                if (D) Log.d(TAG, "OBEX session created");
                mConnected = true;
            } catch (IOException e) {
                Log.e(TAG, "OBEX session connect error " + e.getMessage());
                sendMessageToast("OBEX session connect error: " + e.getMessage());
            }
        }
        synchronized (this) {
            mWaitingForRemote = false;
        }
    }

    private boolean handleDownload(String folder, String file, long expectedSize) {
        ClientSession clientSession = mClientSession;
        if ((!mConnected) || (clientSession == null) || (mCurrentFolder == null)) {
            Log.w(TAG, "sendEvent after disconnect:" + mConnected);
            return false;
        }
        try {
            //Go the desired folder
            // Create file on disk
            File root = Environment.getExternalStorageDirectory();
            String path = root.getAbsolutePath();
            if (!folder.isEmpty()) {
                path += File.separator + folder;
                File dir = new File(path);
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        sendMessageToast("Can't create directory: " + path);
                        return false;
                    }
                }
            }
            path += File.separator + file;
            File f = new File(path);
            if (f.exists() && f.length() == expectedSize) {
                sendMessageToast("File " + path + " already existed");
                sendDownloadStatus(folder, file, 100.0);
                return true;
            }
            f.createNewFile();

            HeaderSet header = new HeaderSet();
            header.setHeader(HeaderSet.NAME, ""); //open the folder
            HeaderSet result = clientSession.setPath(header, false, false);//if the third option is set to true
            if (result.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                Log.e(TAG, "Bad setPath " + result.getResponseCode());
                sendMessageToast("Bad setPath " + result.getResponseCode());
            }
            //the folder if not exist it is created
            //Retreive the file
            header.setHeader(HeaderSet.NAME, file);
            Operation op = clientSession.get(header);
            InputStream is = op.openInputStream();

            FileOutputStream fos = new FileOutputStream (f);
            long total = 0;
            long chunkSize = expectedSize / 10;
            long chunk = chunkSize;
            byte b[] = new byte[1000];
            int len;
            while (is.available() > 0 && (len = is.read(b)) > 0) {
                fos.write (b, 0, len);
                total += len;
                if (total >= chunk) {
                    double r = ((double)total/expectedSize) * 100;
                    sendDownloadStatus(folder, file, r);
                    chunk += chunkSize;
                }
            }
            fos.close();
            is.close();
            op.close();
            Log.i(TAG, "File stored in: " + f.getAbsolutePath());
            return true;
        }catch(Exception e){
            Log.e(TAG, "Exception " + e.toString());
            sendMessageToast("Exception " + e.toString());
            return false;
        }
    }

    // default directory where OBEX put stuff is: /home/root/.cache/obexd
    // Probably need to make sure directory permission allow for create/change directory
    private boolean handleBrowse(String folder) {
        ClientSession clientSession = mClientSession;
        boolean upLevel = false;
        if (folder.equals("..")) {
            upLevel = true;
            folder = "";
        }
        mCurrentFolder = folder;
        if ((!mConnected) || (clientSession == null) || (mCurrentFolder == null)) {
            Log.w(TAG, "sendEvent after disconnect:" + mConnected);
            return false;
        }
        try {
            //Go the desired folder
            HeaderSet header = new HeaderSet();
            header.setHeader(HeaderSet.NAME, mCurrentFolder);
            HeaderSet result = clientSession.setPath(header, upLevel, false);//if the third option is set to true
            if (result.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                Log.e(TAG, "Bad setPath " + result.getResponseCode());
                sendMessageToast("Bad setPath " + result.getResponseCode());
            }
            HeaderSet header2 = new HeaderSet();
            header2.setHeader(HeaderSet.TYPE, "x-obex/folder-listing");
            Operation op = clientSession.get(header2);
            ObexDirParser parser = new ObexDirParser();
            ArrayList<FileEntry> list = parser.parse(op.openInputStream());
            /*
            BufferedReader br = new BufferedReader(new InputStreamReader(op.openInputStream()));
            String line;
            String xmlString = "";
            while ((line = br.readLine()) != null) {
                xmlString = xmlString + line + "\n";
            }
            Log.i(TAG, "Obex XML String:\n" + xmlString);
            br.close();
            */
            op.close();
            sendFileList(list);
            return true;
        }catch(Exception e){
            Log.e(TAG, "Exception " + e.toString());
            sendMessageToast("Exception " + e.toString());
            return false;
        }
    }

    public void browseFolder(String folder) {
        Message msg = new Message();
        msg.what = MSG_BROWSE_FOLDER;
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_FOLDER_NAME, folder);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void downloadFile(String folder, String file, long expectedSize) {
        Message msg = new Message();
        msg.what = MSG_DOWNLOAD_FILE;
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_FILE_NAME, file);
        bundle.putString(Constants.KEY_FOLDER_NAME, folder);
        bundle.putLong(Constants.KEY_FILE_SIZE, expectedSize);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void sendFileList(ArrayList<FileEntry> entries) {
        Message msg = mCallback.obtainMessage(Constants.MESSAGE_BROWSE_DONE_CMD);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.KEY_FILE_ENTRIES, entries);
        msg.setData(bundle);
        mCallback.sendMessage(msg);
    }

    private void sendDownloadStatus(String folder, String file, double percent) {
        // Send a failure message back to the Activity
        Message msg = mCallback.obtainMessage(Constants.MESSAGE_DOWNLOAD_PROGRESS);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_FOLDER_NAME, folder);
        bundle.putString(Constants.KEY_FILE_NAME, file);
        bundle.putInt(Constants.KEY_PERCENT, (int)percent);
        msg.setData(bundle);
        mCallback.sendMessage(msg);
    }

    private void sendMessageToast(String msgStr) {
        // Send a failure message back to the Activity
        Message msg = mCallback.obtainMessage(Constants.MESSAGE_TOAST_CMD);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_TOAST, msgStr );
        msg.setData(bundle);
        mCallback.sendMessage(msg);
    }
}