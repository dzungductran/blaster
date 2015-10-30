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

/**
 * Constants to use in app
 */
public class Constants {
    // Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE_CMD = 1;
    public static final int MESSAGE_READ_CMD = 2;
    public static final int MESSAGE_WRITE_CMD = 3;
    public static final int MESSAGE_DEVICE_NAME_CMD = 4;
    public static final int MESSAGE_TOAST_CMD = 5;
    public static final int MESSAGE_BROWSE_DONE_CMD = 6;
    public static final int MESSAGE_DOWNLOAD_PROGRESS = 7;
    public static final int MESSAGE_DEVICE_SERIAL_CMD = 8;

    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_DEVICE_STATE = "device_state";
    public static final String KEY_COMMAND_STATE = "command_state";
    public static final String KEY_FILE_ENTRIES = "file_entries";
    public static final String KEY_FILE_NAME = "file_name";
    public static final String KEY_FOLDER_NAME = "folder_name";
    public static final String KEY_FILE_SIZE = "file_size";
    public static final String KEY_JSON_STR      = "json_str";

    public static final String KEY_PERCENT        = "percent";
    public static final String KEY_TOAST          = "toast";
    public static final String KEY_COMMAND_TYPE   = "command_type";
    public static final String KEY_COMMAND        = "command";
    public static final String KEY_CAPTURE_OUTPUT = "capture_output";
    public static final String KEY_IDENTIFIER     = "identifier";
    public static final String KEY_FREQUENCY      = "frequency";
    public static final String KEY_CPU_CORES      = "cpu_cores";
    public static final String KEY_CPU_FAMILY     = "cpu_family";
    public static final String KEY_STEPPING       = "stepping";
    public static final String KEY_MODEL          = "model";
    public static final String KEY_CACHE_SIZE     = "cache_size";
    public static final String KEY_MODEL_NAME     = "model_name";
    public static final String KEY_VENDOR_ID      = "vendor_id";

    // serial command
    // commands between Client and Server. Server is on Edison side and
    public static final int SERIAL_CMD_START    = 1;
    public static final int SERIAL_CMD_STATUS   = 2;
    public static final int SERIAL_CMD_ERROR    = 3;
    public static final int SERIAL_CMD_KILL     = 4;
    public static final int SERIAL_CMD_STOP     = 5;
    public static final int SERIAL_CMD_CPU_INFO = 6;

    public static final int SERIAL_CMD_CLOSE        = 0xFF;

    public static final String SERIAL_TYPE_STDOUT = "o";
    public static final String SERIAL_TYPE_STDERR = "e";
    public static final String SERIAL_TYPE_STDOUT_ERR = "r";
}
