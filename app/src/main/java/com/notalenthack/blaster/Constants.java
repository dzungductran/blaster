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

    public static final String TOAST = "toast";
    public static final String DEVICE_NAME = "device_name";

    // serial command
    public static byte SERIAL_CMD_EXECUTE    = 0x1;
    public static byte SERIAL_CMD_STATUS     = 0x2;
    public static byte SERIAL_CMD_ERROR      = 0x3;
    public static byte SERIAL_CMD_CLOSE      = 0xF;
}
