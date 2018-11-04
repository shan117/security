package com.example.shan.securityapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shan.securityapp.misc.ConnectionDetector;

import java.util.ArrayList;
import java.util.List;

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

        if(isMyAppLauncherDefault()) {
            reload();
        }else{
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Please select Security as a default launcher and after that select always to continue.");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            resetPreferredLauncherAndOpenChooser(LauncherActivity.this);
                            if(isMyAppLauncherDefault()){
                                reload();
                            }else{
                                finish();
                            }
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, FakeLauncherActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

    }

    private void reload() {
        cd = new ConnectionDetector(getApplicationContext());
        isConnected = cd.isConnectingToInternet(this);


        if (isConnected) {

            SharedPreferences prefs = getSharedPreferences("pref",MODE_PRIVATE);
            final boolean isLoggedIn = prefs.getBoolean("userloggedin", false);

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
                            Intent mainactivity2 = new Intent(LauncherActivity.this, Main2Activity.class);
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

    private boolean isMyAppLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) getPackageManager();

        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

}
