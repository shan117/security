package com.example.shan.securityapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shan.securityapp.misc.ConnectionDetector;

public class LauncherActivity extends AppCompatActivity {

    Boolean isConnected = false;
    ConnectionDetector cd;

    RelativeLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launcher);

        parent = (RelativeLayout) findViewById(R.id.parent);
        reload();
    }

    private void reload() {
        cd = new ConnectionDetector(getApplicationContext());
        isConnected = cd.isConnectingToInternet(this);


        if (isConnected) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            final boolean isLoggedIn = true;//prefs.getBoolean(Constant.IS_LOGGED_IN_KEY, false);

            Thread timer = new Thread() {
                public void run() {
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.getStackTrace();
                    } finally {

                        if (!isLoggedIn) {

                            Intent mainactivity1 = new Intent(LauncherActivity.this, LoginActivity.class);
                            startActivity(mainactivity1);
                        } else {
                            Intent mainactivity2 = new Intent(LauncherActivity.this, MainActivity.class);
                            startActivity(mainactivity2);
                        }

                    }
                }

            };
            timer.start();
        } else {
            final Snackbar snackbar = Snackbar
                    .make(parent, "Please check internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reload();
                        }
                    });

            snackbar.setActionTextColor(Color.RED);

            View sbView = snackbar.getView();
            sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }
    }


    protected void onPause() {
        super.onPause();
        finish();
    }

}
