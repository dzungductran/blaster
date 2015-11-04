/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.notalenthack.blaster;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothSerialService {
    // Debugging
    private static final String TAG = "BluetoothReadService";
    private static final boolean D = false;


    private static final UUID SerialPortServiceClass_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    private BluetoothDevice mDevice = null;
    private boolean mAllowInsecureConnections;

    private Context mContext;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothSerialService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mContext = context;
        mAllowInsecureConnections = false;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE_CMD, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Already connecting or connected...
        if (mDevice != null && mDevice.getAddress().equals(device.getAddress())
                && getState() == STATE_CONNECTED) {
            // reset state to Connected so we get a message in the UI to
            // bring up our UI for set IP address
            setState(STATE_CONNECTED);
            return;
        }

        mDevice = device;

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME_CMD);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);

        stop(); // stop all threads

        // Send a failure message back to the Activity
        sendMessageToast(mContext.getString(R.string.toast_unable_to_connect));
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_NONE);

        stop(); // stop all threads

        // Send a failure message back to the Activity
        sendMessageToast(mContext.getString(R.string.toast_connection_lost));
    }

    private void sendMessageToast(String msgStr) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST_CMD);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_TOAST, msgStr);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void sendMessageBackToUI(String str) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_SERIAL_CMD);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_JSON_STR, str);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if ( mAllowInsecureConnections ) {
                    tmp = device.createInsecureRfcommSocketToServiceRecord( SerialPortServiceClass_UUID );
                }
                else {
                    tmp = device.createRfcommSocketToServiceRecord( SerialPortServiceClass_UUID );
                }
            } catch (Exception e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                Log.e(TAG, "BtSocket Connect error " + e.getMessage(), e);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                //BluetoothSerialService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothSerialService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    byte[] in = ByteBuffer.allocate(bytes).put(buffer, 0, bytes).array();
                    debugInputData(in);
                    String s = new String(in);
                    // Share the sent message back to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ_CMD, s.length(), -1, s)
                            .sendToTarget();
                    sendMessageBackToUI(s);
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /* debug the data coming over from the device */
        private void debugInputData(byte[] input) {
            if (D) {
                String inputStr = new String(input);
                Log.d(TAG, "read data: " + inputStr);
                    sendMessageToast(inputStr);
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mmOutStream.flush();

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE_CMD, buffer.length, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                sendMessageToast("Exception during write" + e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
                sendMessageToast("close() of connect socket failed" + e);
            }
        }
    }

    public void setAllowInsecureConnections( boolean allowInsecureConnections ) {
        mAllowInsecureConnections = allowInsecureConnections;
    }

    public boolean getAllowInsecureConnections() {
        return mAllowInsecureConnections;
    }

    /*
     * Format a JSON to send over the wire
     */
    public void sendCommand(int cmd, String cmdStr, String type) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.KEY_COMMAND_TYPE, cmd);
            jsonObject.put(Constants.KEY_COMMAND, cmdStr);
            jsonObject.put(Constants.KEY_CAPTURE_OUTPUT, type);
            sendJSON(jsonObject);
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON " + ex.getMessage());
            sendMessageToast("Bad JSON " + ex.getMessage());
            return;
        }
    }

    /*
     * Format a JSON to send over the wire
    */
    public void sendCommand(int cmd, String cmdStr) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.KEY_COMMAND_TYPE, cmd);
            jsonObject.put(Constants.KEY_COMMAND, cmdStr);
            sendJSON(jsonObject);
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON " + ex.getMessage());
            sendMessageToast("Bad JSON " + ex.getMessage());
            return;
        }
    }

    /*
    * Format a JSON to send over the wire
    */
    public void sendCommand(int cmd, String cmdStr, int identifier) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.KEY_COMMAND_TYPE, cmd);
            jsonObject.put(Constants.KEY_COMMAND, cmdStr);
            jsonObject.put(Constants.KEY_IDENTIFIER, identifier);
            sendJSON(jsonObject);
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON " + ex.getMessage());
            sendMessageToast("Bad JSON " + ex.getMessage());
            return;
        }
    }

    /*
    * Format a JSON to send over the wire
    */
    public void sendStatusCommand(String cmdStr, int identifier, boolean quick) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.KEY_COMMAND_TYPE, Constants.SERIAL_CMD_STATUS);
            jsonObject.put(Constants.KEY_COMMAND, cmdStr);
            jsonObject.put(Constants.KEY_IDENTIFIER, identifier);
            jsonObject.put(Constants.KEY_QUICK_STATUS, quick ? 1 : 0);
            sendJSON(jsonObject);
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON " + ex.getMessage());
            sendMessageToast("Bad JSON " + ex.getMessage());
            return;
        }
    }

    /*
     * Format a JSON to send over the wire
    */
    public void sendCommand(int cmd) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.KEY_COMMAND_TYPE, cmd);
            sendJSON(jsonObject);
        } catch (JSONException ex) {
            Log.e(TAG, "Bad JSON " + ex.getMessage());
            sendMessageToast("Bad JSON " + ex.getMessage());
            return;
        }
    }

    // send the JSON over BT
    private void sendJSON(JSONObject jsonObject) {
        int len = jsonObject.toString().length();
        byte[] out = ByteBuffer.allocate(len)
                .put(jsonObject.toString().getBytes()).array();  // cmdStr
        if (D) {
            String str = new String(out);
            Log.d(TAG, "Send JSON: " + str);
        }
        write( out );
    }

    public void send(byte[] out) {

        out = Utils.handleEndOfLineChars(out);

        if ( out.length > 0 ) {
            write( out );
        }
    }
}
