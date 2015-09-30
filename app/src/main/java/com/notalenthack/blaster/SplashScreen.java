package com.notalenthack.blaster;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

public class SplashScreen extends Activity {
    /**
     * The thread to process splash screen events
     */
    private Thread mSplashThread;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Splash screen view
        setContentView(R.layout.splashscreen);

        // Start animating the image
        final ImageView splashImageView = (ImageView) findViewById(R.id.SplashImageView);
        splashImageView.setBackgroundResource(R.drawable.flag);
        final AnimationDrawable frameAnimation = (AnimationDrawable)splashImageView.getBackground();
        splashImageView.post(new Runnable(){
            @Override
            public void run() {
                frameAnimation.start();
            }
        });

        final SplashScreen sPlashScreen = this;

        // The thread to wait for splash screen events
        mSplashThread =  new Thread(){
            @Override
            public void run(){
                try {
                    synchronized(this){
                        // Wait given period of time or exit on touch
                        wait(3000);

                        // Run next activity
                        Intent intent = new Intent();
                        intent.setClass(sPlashScreen, DeviceListActivity.class);
                        startActivity(intent);
                    }
                }
                catch(InterruptedException ex){
                }
                finally {
                    finish();
                }
            }
        };

        mSplashThread.start();
    }

    /**
     * Processes splash screen touch events
     */
    @Override
    public boolean onTouchEvent(MotionEvent evt)
    {
        if(evt.getAction() == MotionEvent.ACTION_DOWN)
        {
            synchronized(mSplashThread){
                mSplashThread.notifyAll();
            }
        }
        return true;
    }
}
