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
 * XML parser for entries from OBEX FTP folder browsing
*/
package com.notalenthack.blaster;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Parser for XML returns from OBEX FTP folder
 */
public class ObexDirParser {
    private static final String TAG = ObexDirParser.class.getName();
    private static final String ns = null;

    public ArrayList<FileEntry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readListing(parser);
        } finally {
            in.close();
        }
    }

    private ArrayList<FileEntry> readListing(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<FileEntry> entries = new ArrayList<FileEntry>();

        parser.require(XmlPullParser.START_TAG, ns, "folder-listing");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("folder") || name.equals("file")) {
                entries.add(readEntry(parser, name));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private FileEntry readEntry(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
        String name = "", perm = "", sizeStr = "", accessStr = "";
        long size = 0, accessTime = 0;
        boolean bFolder = false;
        parser.require(XmlPullParser.START_TAG, ns, tag);

        name = parser.getAttributeValue(null, "name");
        perm = parser.getAttributeValue(null, "user-perm");
        sizeStr = parser.getAttributeValue(null, "size");
        accessStr = parser.getAttributeValue(null, "accessed");
        size = (sizeStr == null) ? 0 : Long.parseLong(sizeStr);
        bFolder = tag.equalsIgnoreCase("folder") ? true : false;

        SimpleDateFormat format = new SimpleDateFormat(
                "yyyyMMdd'T'HHmmss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = format.parse(accessStr);
            accessTime = date.getTime();
        } catch (ParseException ex) {
            Log.e(TAG, "Parse error " + ex.getLocalizedMessage());
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
        }

        int resId;
        if (bFolder) {
            resId = R.drawable.ic_folder;
        } else {
            String lowcase = name.toLowerCase();
            if (lowcase.endsWith(".jpg") || lowcase.endsWith(".png")) {
                resId = R.drawable.ic_picture;
            } else if (lowcase.endsWith(".mp4") || lowcase.endsWith(".ogv")
                    || (lowcase.endsWith(".m4v")) || lowcase.endsWith(".h264")
                    || (lowcase.endsWith(".mpg")) || lowcase.endsWith(".webm")) {
                resId = R.drawable.ic_movies;
            } else {
                resId = R.drawable.ic_file;
            }
        }

        return new FileEntry(name, "", perm, size, accessTime, resId, bFolder, 0);
    }
}
