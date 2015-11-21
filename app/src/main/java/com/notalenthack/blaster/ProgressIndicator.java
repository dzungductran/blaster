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

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/*
 * Progress dialog mostly from this: http://stackoverflow.com/questions/3225889/
 *   how-to-center-progress-indicator-in-progressdialog-easily-when-no-title-text-pa/3226233#3226233
 */
public class ProgressIndicator extends Dialog {
    static OnCancelListener mListener;

    public static ProgressIndicator show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false, null);
    }

    public static ProgressIndicator show(Context context, CharSequence title,
                                        CharSequence message, boolean cancelable) {
        return show(context, title, message, cancelable, null);
    }

    public static ProgressIndicator show(Context context, CharSequence title, CharSequence message,
                                        boolean cancelable, OnCancelListener cancelListener) {
        mListener = cancelListener;
        ProgressIndicator dialog = new ProgressIndicator(context);
        dialog.setCancelable(false);
        //dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(null);

        /* The next line will add the ProgressBar to the dialog. */
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        TextView windowTitle = new TextView(context);
        windowTitle.setText(title);

        View v = new ProgressBar(context);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mListener.onCancel(null);
                return true;
            }
        });
        linearLayout.addView(v);
        linearLayout.addView(windowTitle);
        dialog.addContentView(linearLayout,
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();

        return dialog;
    }

    public ProgressIndicator(Context context) {
        super(context, R.style.NewDialog);
    }
}
