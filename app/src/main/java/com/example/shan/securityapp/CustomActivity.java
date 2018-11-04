package com.example.shan.securityapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CustomActivity extends AppCompatActivity {


    // To keep track of activity's window focus
    private boolean nPanelPulled;

    // To keep track of activity's foreground/background status
    boolean isPaused;


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            // Close every kind of system dialog

            nPanelPulled=true;
            if(nPanelPulled)
            {
                Timer timer=new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!isPaused) {
                                    Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                                    sendBroadcast(closeDialog);
                                }
                                nPanelPulled=false;
                            }
                        });

                    }
                },100,100);
            }
        }
        isPaused=false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isPaused = true;
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);

    }


    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }
}
