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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.notalenthack.blaster.Command;
import com.notalenthack.blaster.Constants;
import com.notalenthack.blaster.R;

import javax.obex.PasswordAuthentication;

/**
 * Edit command dialog
 */
public class EditCommandDialog extends DialogFragment implements IconPickerDialog.IconSelectedCallback {
    private static final String TAG = "EditCommandDialog";
    private static final boolean D = true;

    // TAGs for dialogs
    public static final String TAG_EDIT_COMMAND_DIALOG = "EditCommandDialog";

    private Activity mActivity;
    private EditText mNameText;
    private EditText mStartText;
    private EditText mStopText;
    private EditText mStatText;
    private ImageButton mIconButton;
    private RadioButton mRKillTerm;
    private RadioButton mRKillStop;

    public interface CommandCallback {
        public void newCommand(Command command, boolean bNew);
    }

    private CommandCallback mCallback = null;
    private Command mCommand = null;
    private boolean bNewCmd;

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
        mCommand.setResourceId((int) resId);
        mIconButton.setImageResource(mCommand.getResourceId());
    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static EditCommandDialog newInstance(Command command, CommandCallback callback) {
        EditCommandDialog f = new EditCommandDialog();
        f.setCallback(callback);
        f.setCommand(command);
        return f;
    }

    public EditCommandDialog() {
        super();
    }

    // set the callback before doing anything else
    public void setCallback(CommandCallback callback) {
        mCallback = callback;
    }
    // set the command
    public void setCommand(Command command) {
        if (command == null) {
            mCommand = new Command();
            bNewCmd = true;
        } else {
            mCommand = command;
            if (command.getName().isEmpty() && command.getCommandStart().isEmpty() &&
                    command.getCommandStop().isEmpty() && command.getResourceId() == R.drawable.unknown_item) {
                bNewCmd = true;
            } else {
                bNewCmd = false;
            }
        }
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

        mStopText=(EditText)view.findViewById(R.id.stopCommand);

        mRKillStop = (RadioButton)view.findViewById(R.id.rkillstop);
        mRKillTerm = (RadioButton)view.findViewById(R.id.rkillterm);
        mStopText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    mRKillStop.setEnabled(true);
                    mRKillTerm.setEnabled(true);
                } else {
                    mRKillStop.setChecked(false);
                    mRKillTerm.setChecked(false);
                    mRKillStop.setEnabled(false);
                    mRKillTerm.setEnabled(false);
                }
            }
        });

        mNameText=(EditText)view.findViewById(R.id.name);
        mNameText.setText(mCommand.getName());
        mStartText=(EditText)view.findViewById(R.id.startCommand);
        mStartText.setText(mCommand.getCommandStart());

        if (mCommand.getCommandStop().isEmpty()) {
            updateRadioButtons();
        } else {
            mStopText.setText(mCommand.getCommandStop());
        }

        mStatText=(EditText)view.findViewById(R.id.statCommand);
        mStatText.setText(mCommand.getCommandStat());

        mIconButton=(ImageButton)view.findViewById(R.id.commandButton);
        mIconButton.setImageResource(mCommand.getResourceId());
        mIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIconPicker();
            }
        });

        final CheckBox outputCheck = (CheckBox)view.findViewById(R.id.captureOutput);
        outputCheck.setChecked(mCommand.getDisplayOutput());  // default to false
        outputCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommand.setDisplayOutput(outputCheck.isChecked());
            }
        });

        final CheckBox statusCheck = (CheckBox)view.findViewById(R.id.displayStatus);
        statusCheck.setChecked(mCommand.getDisplayStatus());  // default to false
        statusCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommand.setDisplayStatus(statusCheck.isChecked());
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
                if (mCallback != null) {
                    mCommand.setName(mNameText.getText().toString());
                    mCommand.setStartCommand(mStartText.getText().toString());
                    mCommand.setStopCommand(mStopText.getText().toString());
                    mCommand.setStatCommand(mStatText.getText().toString());
                    mCallback.newCommand(mCommand, bNewCmd);
                }
                dismiss();
            }
        });

        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setTitle(R.string.command_details);

        return view;
    }

    private void updateRadioButtons() {
        if (mCommand.getKillMethod() == Command.KillMethod.STOP) {
            mRKillStop.setChecked(true);
        } else if (mCommand.getKillMethod() == Command.KillMethod.TERMINATE) {
            mRKillTerm.setChecked(true);
        }
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