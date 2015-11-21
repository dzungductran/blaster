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
package com.notalenthack.blaster.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.notalenthack.blaster.ImageAdapter;
import com.notalenthack.blaster.R;

/**
 * Icon picker dialog
 */
public class IconPickerDialog extends DialogFragment {
    private static final String TAG = "IconPickerDialog";
    private static final boolean D = true;

    // TAGs for dialogs
    public static final String TAG_ICON_PICKER_DIALOG = "IconPickerDialog";

    private Activity mActivity;

    public interface IconSelectedCallback {
        public void iconSelected(long resId);
    }

    private IconSelectedCallback mCallback = null;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static IconPickerDialog newInstance(IconSelectedCallback callback) {
        IconPickerDialog f = new IconPickerDialog();
        f.setCallback(callback);

        // Save Arguments
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    public IconPickerDialog() {
        super();
    }

    // set the callback before doing anything else
    public void setCallback(IconSelectedCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (D) Log.d(TAG, "Showing Dialog");
        View view = inflater.inflate(R.layout.icon_selection, container, false);

        int width = getResources().getDisplayMetrics().widthPixels;
        int columns = (width / ImageAdapter.ICON_SIZE) - 1;

        GridView gridview = (GridView)view.findViewById(R.id.gridview);
        gridview.setNumColumns(columns);
        gridview.setColumnWidth(ImageAdapter.ICON_SIZE);
        gridview.setAdapter(new ImageAdapter(mActivity));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (D) Log.i(TAG, "Selection = " + position + " id " + id);
                if (mCallback != null) {
                    mCallback.iconSelected(id);
                }
                dismiss();
            }
        });

        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setTitle(R.string.select_icon);

        return view;
    }

    /* Init the font manager
     *
     */
    @Override
    public void onAttach(Activity activity) {
        mActivity = activity;
        super.onAttach(activity);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}