package com.example.shan.securityapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shan.securityapp.misc.CustomProgressDialog;
import com.example.shan.securityapp.misc.CustomSnackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity {

    private AppCompatButton btnSignIn;

    private AppCompatEditText etUsername;
    private AppCompatEditText etPassword;

    private TextView tvForgotPswd;

    private RelativeLayout parent;

    private String strUsername;
    private String strPassword;

    private SharedPreferences pref;

    final private String TAG="LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        pref = getSharedPreferences("pref",MODE_PRIVATE);

        parent = (RelativeLayout) findViewById(R.id.parent);

        btnSignIn = (AppCompatButton) findViewById(R.id.btn_login);

        etUsername = (AppCompatEditText) findViewById(R.id.et_username);
        etPassword = (AppCompatEditText) findViewById(R.id.et_password);

        tvForgotPswd = (TextView) findViewById(R.id.tv_forgot_pswd);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strUsername=etUsername.getText().toString();
                strPassword=etPassword.getText().toString();
                if(strUsername.isEmpty())
                {
                    CustomSnackbar.createSnackbarRed("Please enter username",parent,LoginActivity.this);

                }else if(strPassword.isEmpty())
                {
                    CustomSnackbar.createSnackbarRed("Please enter password",parent,LoginActivity.this);
                }else {
                    validate();
                }
            }
        });

    }

    private void validate()
    {
        final ProgressDialog pd = CustomProgressDialog.ctor(this);
        pd.show();
        Log.e(TAG, "valid");
        String base64 = "";
        try {
            byte[] data = strUsername.getBytes("UTF-8");
            base64  = Base64.encodeToString(data, Base64.NO_WRAP);
            Log.e(TAG,base64);//

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String username=new String(base64);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference parentRef = database.getReference();
        final DatabaseReference usernameRef=parentRef.child("friends").child(username);
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pd.dismiss();
                if(dataSnapshot.exists())
                {
                    Log.e(TAG, dataSnapshot.toString());
                    User user=dataSnapshot.getValue(User.class);

                    if(user.getPassword().contentEquals(strPassword)) {
                        SharedPreferences.Editor prefsEditor = pref.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        prefsEditor.putString("user", json);
                        prefsEditor.putBoolean("userloggedin",true);
                        prefsEditor.commit();

                        Intent intent = new Intent(LoginActivity.this, Main2Activity.class);
                        startActivity(intent);

                    }else{
                        CustomSnackbar.createSnackbarRed("Invalid password",parent,LoginActivity.this);
                    }
                }
                else
                {
                    CustomSnackbar.createSnackbarRed("User doesn't exist",parent,LoginActivity.this);
                    Log.e(TAG,"Failed");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                pd.dismiss();

            }
        });
    }
}
