package com.example.shan.securityapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shan.securityapp.misc.CustomSnackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddLocationActivity extends AppCompatActivity {

    private EditText etLocation;
    private EditText etName;

    private Toolbar toolbar;

    static boolean isOnCreateCalled = false;

    private Button btnSubmit;

    private RelativeLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        isOnCreateCalled = true;

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        parent = (RelativeLayout) findViewById(R.id.parent);

        etName = (EditText) findViewById(R.id.et_name);

        btnSubmit = (Button) findViewById(R.id.btn_submit);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                onBackPressed();
            }
        });
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_back_white));

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etName.getText().toString() == null || etName.getText().toString().isEmpty()) {
                    CustomSnackbar.createSnackbarRed("Please enter name of location", parent, AddLocationActivity.this);
                } else {
                    startActivity(new Intent(AddLocationActivity.this, BarcodeCaptureActivity.class));

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isOnCreateCalled) {
            isOnCreateCalled = false;
        } else {
            addLocation();
        }
    }

    private void addLocation() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        long currentTime = System.currentTimeMillis();

        SharedPreferences  pref = getSharedPreferences("pref", 0);

        Location location = new Location();

        location.setLatitude(""+pref.getLong("latitude", 0));
        location.setLongitude(""+pref.getLong("longitude", 0));
        location.setBarcodeValue(pref.getString("barcodeValue", "0"));
        location.setName(etName.getText().toString());

        myRef.child("Location")
                .child(String.valueOf(currentTime))
                .setValue(location);
        Toast.makeText(AddLocationActivity.this, "Location added successfully", Toast.LENGTH_SHORT);
        finish();
    }
}
