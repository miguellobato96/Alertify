package com.example.alertify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class StaffLogIn extends AppCompatActivity {

    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_login);

        btnLogin = findViewById(R.id.btnLogin);

        // No validation needed for staff login -> Test purposes
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(StaffLogIn.this, StaffHome.class);
            startActivity(intent);
        });
    }
}
