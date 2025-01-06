package com.example.alertify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify.database.UserDatabaseHelper;

public class SignUp extends AppCompatActivity {

    // UI components
    private EditText etFullName, etEmail, etPassword, etRepeatPassword; // Input fields
    private Button btnSignUp; // Sign Up button
    private TextView tvLogin; // Link to navigate to Login screen
    private UserDatabaseHelper dbHelper; // Database helper for database operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up); // Link to the Sign Up layout

        // Initialize UserDatabaseHelper
        dbHelper = new UserDatabaseHelper(this);

        // Link UI components using their IDs
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        // Set click listener for the Sign Up button
        btnSignUp.setOnClickListener(v -> {
            // Retrieve input from fields
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String repeatPassword = etRepeatPassword.getText().toString().trim();

            // Validate that no fields are left empty
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(SignUp.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return; // Stop execution if validation fails
            }

            // Check if passwords match
            if (!password.equals(repeatPassword)) {
                Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert user data into the database
            boolean isInserted = dbHelper.addUser(fullName, email, password);

            if (isInserted) {
                // Registration success
                Toast.makeText(SignUp.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUp.this, LogIn.class)); // Navigate to Login page
                finish(); // Close Sign Up activity
            } else {
                // Registration failure (e.g., email already exists)
                Toast.makeText(SignUp.this, "Registration failed. Email may already exist.", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to Login page when "Log In" text is clicked
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUp.this, LogIn.class)); // Navigate to Login page
            finish(); // Close Sign Up activity
        });
    }
}
