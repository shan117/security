package com.example.shan.securityapp;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {


    Button startBtn;
    Button adminBtn;
    ImageView imgView;
     RecyclerView recyclerView;

    SharedPreferences counterPref;
    static boolean isOnCreateCalled=false;

    private static final int RC_HANDLE_CAMERA_PERM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        startBtn =(Button)findViewById(R.id.button2);

       // adminBtn =(Button) findViewById(R.id.button3);

//        imgView =(ImageView)findViewById(R.id.imageView6);

       /* adminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this,ListActivity.class));
            }
        });*/

        counterPref=getSharedPreferences("counterPref",0);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this,FaceTrackerActivity.class));
            }
        });

    isOnCreateCalled=true;

        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date= new Date();
        (formatter.format(date)).toString();
        Log.e("date1",(formatter.format(date)).toString());


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

        Map map=new HashMap<>();
        SharedPreferences pref= getSharedPreferences("dataPref",MODE_PRIVATE);
//        SharedPreferences.Editor editor=pref.edit();
//        editor.putString("ab","ab");
//        editor.putString("cd","cd");
//       editor.commit();
//        editor.apply();


//        SharedPreferences pref2=getSharedPreferences("dataPref2",0);

//        String x=pref.getString("ab","no");
//        String y =pref.getString("cd","no");

      /*  map=pref.getAll();
//        pref.get

        String imagePath= pref.getString("ImagePath","None");

        Bitmap imageBitmap= BitmapFactory.decodeFile(imagePath);
        Bitmap imageBitmap1= BitmapFactory.decodeFile(imagePath);

//        imgView.setImageBitmap(imageBitmap);

        // Write a message to the database
        if(!map.isEmpty()){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();

            long currentTime= System.currentTimeMillis();

            myRef.child("data").child( String.valueOf(currentTime)).setValue(pref.getAll());
//            myRef.child("data").child( String.valueOf(currentTime)).child("imageBitmap").setValue(imageBitmap);


        }
        SharedPreferences.Editor editor= pref.edit();
        editor.clear();
        editor.commit();*/





//        /storage/emulated/0/Android/data/com.example.shan.securityapp/files/MyFileStorage/pic5.jpg

        if(isOnCreateCalled){
            isOnCreateCalled=false;
        }
        else{

            SharedPreferences pref1= getSharedPreferences("dataPref",MODE_PRIVATE);
            if(pref1.getBoolean("uploadAllowed",false)){
                StorageReference mStorageRef;
                mStorageRef = FirebaseStorage.getInstance().getReference();

                SharedPreferences sPref=getSharedPreferences("counterPref",0);
                final int count=sPref.getInt("imageCount",0);

                long currentTime= System.currentTimeMillis();

                Uri file = Uri.fromFile(new File("/storage/emulated/0/Android/data/com.example.shan.securityapp/files/MyFileStorage/pic5.jpg"));
                StorageReference riversRef = mStorageRef.child("images/faceImage"+currentTime+".jpg");

                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                downloadUrl = taskSnapshot.getDownloadUrl();
                                SharedPreferences pref=getSharedPreferences("counterPref",0);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putInt("imageCount",count+1);
                                editor.commit();

                                SharedPreferences pref1= getSharedPreferences("dataPref",MODE_PRIVATE);
                                SharedPreferences.Editor edit= pref1.edit();
                                SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                Date date= new Date();
                                (formatter.format(date)).toString();
                                Log.e("date",(formatter.format(date)).toString());
                                edit.putString("imagePath",downloadUrl.toString());
                                edit.putString("time",(formatter.format(date)).toString());
                                edit.commit();



                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference();

                                long currentTime= System.currentTimeMillis();

                                myRef.child("data").child( String.valueOf(currentTime)).setValue(pref1.getAll());
//                            myRef.child("data").child( String.valueOf(currentTime)).child("imageUrl").setValue(downloadUrl);

                                SharedPreferences.Editor edit1= pref1.edit();
                                edit1.clear();
                                edit1.commit();



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
