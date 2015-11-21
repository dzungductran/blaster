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

/**
 * Helper functions
 */
public class Utils {
    private static int mIncomingEoL_0D = 0x0D;
    private static int mIncomingEoL_0A = 0x0A;
    private static int mOutgoingEoL_0D = 0x0D;
    private static int mOutgoingEoL_0A = 0x0A;

    private static byte[] endOfLineChars( int outgoingEoL ) {
        byte[] out;

        if ( outgoingEoL == 0x0D0A ) {
            out = new byte[2];
            out[0] = 0x0D;
            out[1] = 0x0A;
        }
        else {
            if ( outgoingEoL == 0x00 ) {
                out = new byte[0];
            }
            else {
                out = new byte[1];
                out[0] = (byte)outgoingEoL;
            }
        }

        return out;
    }

    public static byte[] handleEndOfLineChars(byte[] out) {

        if ( out.length == 1 ) {

            if ( out[0] == 0x0D ) {
                out = endOfLineChars( mOutgoingEoL_0D );
            }
            else {
                if ( out[0] == 0x0A ) {
                    out = endOfLineChars( mOutgoingEoL_0A );
                }
            }
        }
        return out;
    }

    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a)
    {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    /**
     * This String util method removes single or double quotes
     * from a string if its quoted.
     * for input string = "mystr1" output will be = mystr1
     * for input string = 'mystr2' output will be = mystr2
     *
     * @param s value to be unquoted.
     * @return value unquoted, null if input is null.
     *
     */
    public static String unquote(String s) {

        if (s != null
                && ((s.startsWith("\"") && s.endsWith("\""))
                || (s.startsWith("'") && s.endsWith("'")))) {

            s = s.substring(1, s.length() - 1);
        }
        return s;
    }
}
