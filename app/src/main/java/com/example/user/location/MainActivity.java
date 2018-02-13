package com.example.user.location;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import com.firebase.ui.auth.AuthUI;

public class MainActivity extends AppCompatActivity
{
    Button btnLogin;
    Button btnCreateAcc;

    private final static int LOGIN_PERMISSION=1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  MultiDex.install(getApplicationContext());

        btnLogin=(Button)findViewById(R.id.btnSignIn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAllowNewEmailAccounts(true).build(),LOGIN_PERMISSION);
            }}

        );
        btnCreateAcc=(Button)findViewById(R.id.btnCreateAcc);
        btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(), RegisterUser.class);
                startActivity(i);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==LOGIN_PERMISSION){
            startNewActivity(resultCode,data);
        }
    }
    private void startNewActivity(int resultCode, Intent data){
        if(resultCode==RESULT_OK){
             Intent i=new Intent(MainActivity.this,ListOnline.class);
             startActivity(i);
             finish();
        }
        else{
            Toast.makeText(this,"Login failed",Toast.LENGTH_SHORT).show();
        }
    }


}

