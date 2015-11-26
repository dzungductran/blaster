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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.notalenthack.blaster.R;

import org.w3c.dom.Text;

import java.util.Random;

/**
 * Edit command dialog
 */
public class LaunchCommandDialog extends DialogFragment implements Button.OnClickListener {
    private static final String TAG = "LaunchCommandDialog";
    private static final boolean D = true;

    // TAGs for dialogs
    public static final String TAG_LAUNCH_DIALOG = "LaunchCommandDialog";

    private Activity mActivity;

    private View mView;

    private EditText mEditPin1;
    private EditText mEditPin2;
    private EditText mEditPin3;
    private EditText mEditPin4;
    private EditText mCurEditPin;

    // buttons
    private Button mBtnOne;
    private Button mBtnTwo;
    private Button mBtnThree;
    private Button mBtnFour;
    private Button mBtnFive;
    private Button mBtnSix;
    private Button mBtnSeven;
    private Button mBtnEight;
    private Button mBtnNine;
    private Button mBtnZero;
    private ImageButton mBtnBack;
    private ImageButton mBtnLaunch;

    // Codes
    private TextView mTxtCode1;
    private TextView mTxtCode2;
    private TextView mTxtCode3;
    private TextView mTxtCode4;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static LaunchCommandDialog newInstance() {
        LaunchCommandDialog f = new LaunchCommandDialog();
        return f;
    }

    public LaunchCommandDialog() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (D) Log.d(TAG, "Showing Dialog");
        mView = inflater.inflate(R.layout.pin_input, container, false);
        getDialog().setTitle(getString(R.string.launch_codes));

        setupLaunchCodes();

        setupEditFields();
        mCurEditPin = mEditPin1;

        setupButtons();

        setNoKeyBoard();

        return mView;
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

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.number0:
                mCurEditPin.setText("0");
                break;
            case R.id.number1:
                mCurEditPin.setText("1");
                break;
            case R.id.number2:
                mCurEditPin.setText("2");
                break;
            case R.id.number3:
                mCurEditPin.setText("3");
                break;
            case R.id.number4:
                mCurEditPin.setText("4");
                break;
            case R.id.number5:
                mCurEditPin.setText("5");
                break;
            case R.id.number6:
                mCurEditPin.setText("6");
                break;
            case R.id.number7:
                mCurEditPin.setText("7");
                break;
            case R.id.number8:
                mCurEditPin.setText("8");
                break;
            case R.id.number9:
                mCurEditPin.setText("9");
                break;
            case R.id.delete:
                mCurEditPin.setText("");
                break;
        }
    }

    // Setup buttons
    private void setupButtons() {
        // buttons
        mBtnZero = (Button)mView.findViewById(R.id.number0);
        mBtnOne = (Button)mView.findViewById(R.id.number1);
        mBtnTwo = (Button)mView.findViewById(R.id.number2);
        mBtnThree = (Button)mView.findViewById(R.id.number3);
        mBtnFour = (Button)mView.findViewById(R.id.number4);
        mBtnFive = (Button)mView.findViewById(R.id.number5);
        mBtnSix = (Button)mView.findViewById(R.id.number6);
        mBtnSeven = (Button)mView.findViewById(R.id.number7);
        mBtnEight = (Button)mView.findViewById(R.id.number8);
        mBtnNine = (Button)mView.findViewById(R.id.number9);
        mBtnBack = (ImageButton)mView.findViewById(R.id.delete);
        mBtnLaunch = (ImageButton)mView.findViewById(R.id.launch);

        mBtnZero.setOnClickListener(this);
        mBtnOne.setOnClickListener(this);
        mBtnTwo.setOnClickListener(this);
        mBtnThree.setOnClickListener(this);
        mBtnFour.setOnClickListener(this);
        mBtnFive.setOnClickListener(this);
        mBtnSix.setOnClickListener(this);
        mBtnSeven.setOnClickListener(this);
        mBtnEight.setOnClickListener(this);
        mBtnNine.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
    }

    // Setup launch codes
    private void setupLaunchCodes() {
        Random rn = new Random();
        int num1 = rn.nextInt(10);
        int num2 = rn.nextInt(10);
        int num3 = rn.nextInt(10);
        int num4 = rn.nextInt(10);

        mTxtCode1 = (TextView)mView.findViewById(R.id.code1);
        mTxtCode2 = (TextView)mView.findViewById(R.id.code2);
        mTxtCode3 = (TextView)mView.findViewById(R.id.code3);
        mTxtCode4 = (TextView)mView.findViewById(R.id.code4);

        mTxtCode1.setText(String.valueOf(num1));
        mTxtCode2.setText(String.valueOf(num2));
        mTxtCode3.setText(String.valueOf(num3));
        mTxtCode4.setText(String.valueOf(num4));
    }

    // Set handle of text
    private void setupEditFields() {
        mEditPin1 = (EditText)mView.findViewById(R.id.entry1);
        mEditPin2 = (EditText)mView.findViewById(R.id.entry2);
        mEditPin3 = (EditText)mView.findViewById(R.id.entry3);
        mEditPin4 = (EditText)mView.findViewById(R.id.entry4);

        mEditPin1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    mCurEditPin = mEditPin2;
                }
                mCurEditPin.setSelection(mCurEditPin.getText().length());
                mCurEditPin.requestFocus();
            }
        });

        mEditPin2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    mCurEditPin = mEditPin3;
                } else {
                    mCurEditPin = mEditPin1;
                }
                mCurEditPin.setSelection(mCurEditPin.getText().length());
                mCurEditPin.requestFocus();
            }
        });

        mEditPin3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    mCurEditPin = mEditPin4;
                } else {
                    mCurEditPin = mEditPin2;
                }
                mCurEditPin.setSelection(mCurEditPin.getText().length());
                mCurEditPin.requestFocus();
            }
        });

        mEditPin4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    if (mTxtCode1.getText().toString().equals(mEditPin1.getText().toString()) &&
                            mTxtCode2.getText().toString().equals(mEditPin2.getText().toString()) &&
                            mTxtCode3.getText().toString().equals(mEditPin3.getText().toString()) &&
                            mTxtCode4.getText().toString().equals(mEditPin4.getText().toString())) {
                        mBtnLaunch.setImageResource(R.drawable.ic_launcher);
                    } else {
                        mBtnLaunch.setImageResource(R.drawable.ic_no_launch);
                    }
                } else {
                    mBtnLaunch.setImageResource(R.drawable.ic_no_launch);
                    mCurEditPin = mEditPin3;
                }
                mCurEditPin.setSelection(mCurEditPin.getText().length());
                mCurEditPin.requestFocus();

            }
        });
    }

    // Hide keyboard
    private void setNoKeyBoard() {
        mEditPin1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });
        mEditPin2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });
        mEditPin3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });
        mEditPin4.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });}
}