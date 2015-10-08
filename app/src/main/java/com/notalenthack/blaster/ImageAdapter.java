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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

    public static int ICON_SIZE = 150;

    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        SquareImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new SquareImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (SquareImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.unknown_item,
            R.drawable.ic_sample_2,
            R.drawable.ic_sample_3,
            R.drawable.ic_sample_4,
            R.drawable.ic_sample_5,
            R.drawable.ic_sample_6,
            R.drawable.ic_sample_7,
            R.drawable.ic_sample_8,
            R.drawable.ic_sample_9,
            R.drawable.ic_sample_10,
            R.drawable.ic_sample_11,
            R.drawable.ic_sample_12,
            R.drawable.ic_sample_13,
            R.drawable.ic_sample_14,
            R.drawable.ic_sample_15,
            R.drawable.ic_sample_16,
            R.drawable.ic_sample_17,
            R.drawable.ic_sample_18,
            R.drawable.ic_sample_19,
            R.drawable.ic_sample_20,
            R.drawable.ic_sample_21,
            R.drawable.ic_sample_22,
            R.drawable.ic_sample_23,
            R.drawable.ic_sample_24
    };

    private class SquareImageView extends ImageView
    {
        public SquareImageView(Context context)
        {
            super(context);
        }

        public SquareImageView(Context context, AttributeSet attrs)
        {
            super(context, attrs);
        }

        public SquareImageView(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(ICON_SIZE, ICON_SIZE); //Snap to width
        }
    }
}