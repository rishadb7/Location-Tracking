package com.example.user.location;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class RegisterUser extends AppCompatActivity {
    TextView txtview1;
    TextView txtview2;
    TextView txtview3;
    Button btn3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        btn3=(Button)findViewById(R.id.btnRegister);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "You are registered!" , Toast.LENGTH_SHORT ).show();
            }
        });

    }
}
