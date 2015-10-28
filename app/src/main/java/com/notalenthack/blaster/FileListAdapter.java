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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for loading files
 */
public class FileListAdapter extends BaseAdapter
{
    private List<FileEntry> items = new ArrayList<FileEntry>();
    private Map<String, FileEntry> mapItems = new HashMap<String, FileEntry>();
    private LayoutInflater inflater;

    public FileListAdapter(Activity par)
    {
        inflater = par.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i)
    {
        return items.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        FieldReferences fields;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.file_item, viewGroup, false);
            fields = new FieldReferences();
            fields.picture = (ImageView)convertView.findViewById(R.id.picture);
            fields.name = (TextView)convertView.findViewById(R.id.text);
            fields.progressBar = (ProgressBar)convertView.findViewById(R.id.myProgress);

            convertView.setTag(fields);
        } else {
            fields = (FieldReferences) convertView.getTag();
        }

        FileEntry entry = (FileEntry)getItem(position);

        fields.picture.setImageResource(entry.drawableId);;
        fields.name.setText(entry.name);
        if (!entry.bFolder) {
            fields.progressBar.setProgress(entry.downloadProgress);
        } else {
            fields.progressBar.setProgress(0);
        }

        return convertView;
    }

    public FileEntry getItem(String pathName) { return mapItems.get(pathName); }

    private class FieldReferences {
        ImageView picture;
        TextView name;
        ProgressBar progressBar;
    }

    public void addFileEntries(ArrayList<FileEntry> entries) {
        items.clear();  // remove old entries
        for (FileEntry entry : entries) {
            items.add(entry);
            mapItems.put(entry.path + File.separator + entry.name, entry);
        }
    }
}
