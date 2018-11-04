package com.example.shan.securityapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shan.securityapp.misc.CustomProgressDialog;
import com.example.shan.securityapp.misc.CustomSnackbar;
import com.example.shan.securityapp.misc.Helper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main2Activity extends CustomActivity {

    private ImageView ivLogout;
    private ImageView ivScan;
    private ImageView ivAddLocation;

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvCompany;
    private TextView tvSupervisior;

    private RelativeLayout parent;

    private SharedPreferences pref;

    private String strLocationName;

    boolean isSourceValid = false;

    static boolean isOnCreateCalled = false;

    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private User user;

    private Scan scan;

    int j = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        pref = getSharedPreferences("pref", 0);

        Gson gson = new Gson();
        String json = pref.getString("user", "");
        user = gson.fromJson(json, User.class);

        ivAddLocation = (ImageView) findViewById(R.id.iv_add_location);
        ivScan = (ImageView) findViewById(R.id.iv_scan);
        ivLogout = (ImageView) findViewById(R.id.iv_logout);

        parent = (RelativeLayout) findViewById(R.id.parent);

        tvName = (TextView) findViewById(R.id.tv_name);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        tvCompany = (TextView) findViewById(R.id.tv_company);
        tvSupervisior = (TextView) findViewById(R.id.tv_supervisior);

        tvName.setText(user.getName());
        tvEmail.setText(user.getEmail());
        tvCompany.setText(user.getCompany());
        tvSupervisior.setText(Helper.base64ToString(user.getSuperUser()));

        ivScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this, FaceTrackerActivity.class));
            }
        });
        ivAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this, AddLocationActivity.class));
            }
        });
        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("userloggedin", false);
                editor.commit();
                startActivity(new Intent(Main2Activity.this, LoginActivity.class));
                finish();
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

    public void checkLocation(long lat, long lng) {
        j = 0;
        isSourceValid = false;
        final double sourceLatitude = Double.longBitsToDouble(lat);
        final double sourceLongitude = Double.longBitsToDouble(lng);

        Log.e("Main2Activity", "source " + sourceLatitude + " , " + sourceLongitude);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference().child("companies").child(user.getCompany()).child("Location");


        final ProgressDialog pd = CustomProgressDialog.ctor(Main2Activity.this);
        pd.show();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final long count = dataSnapshot.getChildrenCount();
                try {
                    myRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            j++;

                            Location location = dataSnapshot.getValue(Location.class);
                            Double destLatitude = Double.longBitsToDouble(Long.parseLong(location.getLatitude()));
                            Double destLongitude = Double.longBitsToDouble(Long.parseLong(location.getLongitude()));
                            Log.e("Main2Activity", "destination " + destLatitude + " , " + destLongitude);

                            android.location.Location startPoint = new android.location.Location("locationA");
                            startPoint.setLatitude(sourceLatitude);
                            startPoint.setLongitude(sourceLongitude);

                            android.location.Location endPoint = new android.location.Location("locationA");
                            endPoint.setLatitude(destLatitude);
                            endPoint.setLongitude(destLongitude);

                            double distance = startPoint.distanceTo(endPoint);
                            if (distance < 250.0 && location.getBarcodeValue().contentEquals(pref.getString("barcodeValue", "0"))) {
                                isSourceValid = true;
                                Log.e("Main2Activity", "valid distance");
                                strLocationName = location.getName();
                                addData();
                            }

                            if (count == j) {
                                CustomSnackbar.createSnackbarRed("No nearby location found", parent, Main2Activity.this);
                            }

                            Log.e("Main2Activity", "distance " + distance);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                } catch (Exception e) {
                    Log.e("ListActivity", " error :" + e.getMessage());
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addData() {
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
                        scan.setTime("" + (formatter.format(date)).toString());
                        scan.setLatitude("" + pref.getLong("latitude", 0));
                        scan.setLongitude("" + pref.getLong("longitude", 0));
                        scan.setLocationName("" + strLocationName);
                        scan.setBarcodeValue(pref.getString("barcodeValue", "0"));
                        scan.setUser(user.getUser());
                        scan.setSuperAdmin(user.getSuperAdmin());
                        scan.setSuperUser(user.getSuperUser());
                        scan.setSupervisor(Helper.stringToBase64(user.getEmail()));
                        scan.setSupervisorName(user.getName());

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference();
                        long currentTime = System.currentTimeMillis();
                        myRef.child("companies").child(user.getCompany()).child("data")
                                .child(String.valueOf(currentTime))
                                .setValue(scan);
                        Toast.makeText(Main2Activity.this, "Scanned successfully", Toast.LENGTH_SHORT);
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
    @Override
    protected void onResume() {
        super.onResume();

        if (isOnCreateCalled) {
            isOnCreateCalled = false;
        } else {
            if (pref.getBoolean("uploadAllowed", false)) {

                checkLocation(pref.getLong("latitude", 0), pref.getLong("longitude", 0));

            }
        }
    }

    @Override
    public void onBackPressed() {
    }

}
