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
/*            R.drawable.ic_blast,
            R.drawable.ic_youtube,
            R.drawable.ic_blast2,
            R.drawable.unknown_item,
            R.drawable.ic_blaster3,
            R.drawable.ic_compass,
            R.drawable.ic_download,
            R.drawable.ic_edison,
            R.drawable.ic_globe,
            R.drawable.ic_gps,
            R.drawable.ic_gps2,
            R.drawable.ic_rocket,
            R.drawable.ic_video    */
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