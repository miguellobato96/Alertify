package com.example.alertify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the splash screen layout
        setContentView(R.layout.activity_splash);

        // Add a delay of 3 seconds before redirecting to the Login screen
        new Handler().postDelayed(() -> {
            // Navigate to the Login Activity
            Intent intent = new Intent(SplashActivity.this, LogIn.class);
            startActivity(intent);

            // Finish SplashActivity to remove it from the back stack
            finish();
        }, 3000); // 3000 milliseconds = 3 seconds
    }
}
