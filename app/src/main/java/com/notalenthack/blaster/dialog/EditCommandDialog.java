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
package com.notalenthack.blaster.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;

import com.notalenthack.blaster.Command;
import com.notalenthack.blaster.ImageAdapter;
import com.notalenthack.blaster.R;

/**
 * Edit command dialog
 */
public class EditCommandDialog extends DialogFragment implements IconPickerDialog.IconSelectedCallback {
    private static final String TAG = "EditCommandDialog";
    private static final boolean D = true;

    // TAGs for dialogs
    public static final String TAG_ICON_PICKER_DIALOG = "EditCommandDialog";

    private Activity mActivity;

    public interface CommandCallback {
        public void newCommand(Command command);
    }

    private CommandCallback mCallback = null;


    private void showIconPicker() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(IconPickerDialog.TAG_ICON_PICKER_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = IconPickerDialog.newInstance(this);
        newFragment.show(ft, IconPickerDialog.TAG_ICON_PICKER_DIALOG);
    }

    // Callback when Icon is selected.
    public void iconSelected(long resId) {
        if (D) Log.d(TAG, "Icon with resId selected " + resId);
    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static EditCommandDialog newInstance(CommandCallback callback) {
        EditCommandDialog f = new EditCommandDialog();
        f.setCallback(callback);

        // Save Arguments
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    public EditCommandDialog() {
        super();
    }

    // set the callback before doing anything else
    public void setCallback(CommandCallback callback) {
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
        View view = inflater.inflate(R.layout.command_detail_dlg, container, false);

        getDialog().setTitle(getString(R.string.command_details));

        final EditText nameText=(EditText)view.findViewById(R.id.name);
        final EditText startText=(EditText)view.findViewById(R.id.startCommand);
        final EditText stopText=(EditText)view.findViewById(R.id.stopCommand);

        final ImageButton iconButton=(ImageButton)view.findViewById(R.id.commandButton);
        iconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIconPicker();
            }
        });

        final CheckBox outputCheck = (CheckBox)view.findViewById(R.id.captureOutput);
        outputCheck.setChecked(false);  // default to false
        outputCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (outputCheck.isChecked()) {
                }
            }
        });

        final CheckBox statusCheck = (CheckBox)view.findViewById(R.id.displayStatus);
        statusCheck.setChecked(false);  // default to false
        statusCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statusCheck.isChecked()) {
                }
            }
        });

        final Button btnCancel=(Button)view.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (D) Log.d(TAG, "Cancel out of edit details...");
                dismiss();
            }
        });

        // Okay
        final Button okay=(Button)view.findViewById(R.id.okay);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (D) Log.d(TAG, "Okay out of edit details...");
                dismiss();
            }
        });

        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setTitle(R.string.command_details);

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