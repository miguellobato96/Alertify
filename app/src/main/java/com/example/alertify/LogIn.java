package com.example.alertify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LogIn extends AppCompatActivity {

    private EditText etEmail, etPassword; // Input fields for email and password
    private Button btnLogin; // Log In button
    private TextView tvSignUp; // Sign Up navigation text
    private DatabaseHelper dbHelper; // Helper for database operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Link UI components using their respective IDs
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        // Set click listener for the Log In button
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim(); // Get the entered email
            String password = etPassword.getText().toString().trim(); // Get the entered password

            // Validate that no fields are left empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LogIn.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return; // Stop execution if validation fails
            }

            // Check user credentials in the database
            boolean isValid = dbHelper.checkUser(email, password);

            if (isValid) {
                // If valid, display success message and navigate to Home screen
                Toast.makeText(LogIn.this, "Login successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LogIn.this, Home.class);
                startActivity(intent);
                finish(); // Close the Log In activity to prevent going back to it
            } else {
                // If invalid, show an error message
                Toast.makeText(LogIn.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to the Sign Up page when "Sign Up" text is clicked
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LogIn.this, SignUp.class);
            startActivity(intent); // Open the Sign Up activity
        });
    }
}
