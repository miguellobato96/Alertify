package com.example.alertify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class StaffLogIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_login);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Set click listener for back button
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(StaffLogIn.this, LogIn.class);
            startActivity(intent);
        });

        // No validation needed for staff login -> Test purposes
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(StaffLogIn.this, StaffHome.class);
            startActivity(intent);
        });
    }
}
