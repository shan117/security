package com.example.shan.securityapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.shan.securityapp.misc.Helper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main2Activity extends AppCompatActivity {

    private Button btnStart;

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvCompany;
    private TextView tvSupervisior;

    private SharedPreferences pref;

    static boolean isOnCreateCalled = false;

    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private User user;

    private Scan scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        pref = getSharedPreferences("pref", 0);

        Gson gson = new Gson();
        String json = pref.getString("user", "");
        user = gson.fromJson(json, User.class);

        btnStart = (Button) findViewById(R.id.btn_start);

        tvName = (TextView) findViewById(R.id.tv_name);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        tvCompany = (TextView) findViewById(R.id.tv_company);
        tvSupervisior = (TextView) findViewById(R.id.tv_supervisior);

        tvName.setText(user.getName());
        tvEmail.setText(user.getEmail());
        tvCompany.setText(user.getCompany());
        tvSupervisior.setText(Helper.base64ToString(user.getSuperUser()));


        // adminBtn =(Button) findViewById(R.id.button3);

//        imgView =(ImageView)findViewById(R.id.imageView6);

       /* adminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this,ListActivity.class));
            }
        });*/

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this, FaceTrackerActivity.class));
            }
        });

        isOnCreateCalled = true;

        int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
    }


    private void requestCameraPermission() {
        Log.w("", "Camera permission is not granted. Requesting permission");
        final String[] permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                , android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isOnCreateCalled) {
            isOnCreateCalled = false;
        } else {
            if (pref.getBoolean("uploadAllowed", false)) {
                final int count = pref.getInt("imageCount", 0);
                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                long currentTime = System.currentTimeMillis();

                Uri file = Uri.fromFile(new File("/storage/emulated/0/Android/data/com.example.shan.securityapp/files/MyFileStorage/pic5.jpg"));
                StorageReference riversRef = mStorageRef.child("images/faceImage" + currentTime + ".jpg");

                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                scan = new Scan();
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putInt("imageCount", count + 1);
                                editor.commit();

                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                Date date = new Date();
                                Log.e("date", (formatter.format(date)).toString());

                                scan.setImagePath(downloadUrl.toString());
                                scan.setTime(""+(formatter.format(date)).toString());
                                scan.setLatitude(""+pref.getLong("latitude", 0));
                                scan.setLongitude(""+pref.getLong("longitude", 0));
                                scan.setBarcodeValue(pref.getString("barcodeValue", "0"));

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference();
                                long currentTime = System.currentTimeMillis();
                                myRef.child("companies")
                                        .child(user.getCompany())
                                        .child("superAdmin")
                                        .child(user.getSuperAdmin())
                                        .child("superUser")
                                        .child(user.getSuperUser())
                                        .child("user")
                                        .child(user.getUser())
                                        .child("supervisor")
                                        .child(Helper.stringToBase64(user.getEmail()))
                                        .child(String.valueOf(currentTime))
                                        .setValue(scan);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                            }
                        });
            }
        }
    }
}
