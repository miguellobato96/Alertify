package com.example.alertify;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private View sidebarLayout;
    private View backgroundOverlay;
    private ImageButton closeButton;

    // Sidebar buttons
    private Button btnHome;
    private Button btnSosContacts;
    private Button btnSafetyTips;
    private Button btnAboutUs;
    private Button btnTermsConditions;
    private Button btnLogOut;

    private boolean isHomeSelected = true; // Flag to track if Home is selected

    private ArrayList<TextView> placeholders;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize sidebar buttons
        btnHome = findViewById(R.id.btn_home);
        btnSosContacts = findViewById(R.id.btn_sos_contacts);
        btnSafetyTips = findViewById(R.id.btn_safety_tips);
        btnAboutUs = findViewById(R.id.btn_about_us);
        btnTermsConditions = findViewById(R.id.btn_terms_conditions);
        btnLogOut = findViewById(R.id.btn_logout);

        // Initialize placeholders
        placeholders = new ArrayList<>();
        placeholders.add(findViewById(R.id.contact_text_1));
        placeholders.add(findViewById(R.id.contact_text_2));
        placeholders.add(findViewById(R.id.contact_text_3));
        placeholders.add(findViewById(R.id.contact_text_4));

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Load pinned contacts
        loadPinnedContacts();

        // Set listeners for sidebar buttons
        btnHome.setOnClickListener(v -> {
            if (isHomeSelected) {
                closeSidebar();
            } else {
                setSelectedButton(btnHome);
                isHomeSelected = true;
            }
        });

        btnSosContacts.setOnClickListener(v -> {
            setSelectedButton(btnSosContacts);
            isHomeSelected = false;
            Intent intent = new Intent(Home.this, SosContacts.class);
            startActivity(intent);
        });

        btnSafetyTips.setOnClickListener(v -> {
            setSelectedButton(btnSafetyTips);
            isHomeSelected = false;
            // Navigate to Safety Tips page
        });

        btnAboutUs.setOnClickListener(v -> {
            setSelectedButton(btnAboutUs);
            isHomeSelected = false;
            // Navigate to About Us page
        });

        btnTermsConditions.setOnClickListener(v -> {
            setSelectedButton(btnTermsConditions);
            isHomeSelected = false;
            // Navigate to Terms & Conditions page
        });

        // Set listener for Logout
        btnLogOut.setOnClickListener(v -> {
            showLogoutDialog(); // dialog confirmation to logout
        });

        // Sidebar elements
        sidebarLayout = findViewById(R.id.sidebar_layout);
        backgroundOverlay = findViewById(R.id.background_overlay);
        ImageButton openSettingsButton = findViewById(R.id.open_settings_button);

        openSettingsButton.setOnClickListener(v -> openSidebar());
        backgroundOverlay.setOnClickListener(v -> closeSidebar());
        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> closeSidebar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always reload pinned contacts when Home is resumed
        loadPinnedContacts();

        // Always select the Home button
        setSelectedButton(btnHome);
        isHomeSelected = true;
    }

    private void loadPinnedContacts() {
        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Query for pinned contacts, ordered by pinned_order
        Cursor cursor = database.query(
                "contacts",
                new String[]{"name"},
                "isPinned = ?",
                new String[]{"1"},
                null,
                null,
                "pinned_order ASC"
        );

        // Clear placeholders and reset to "N/A"
        for (TextView placeholder : placeholders) {
            placeholder.setText("N/A");
        }

        // Populate placeholders with pinned contacts
        int index = 0;
        while (cursor.moveToNext() && index < placeholders.size()) {
            String contactName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            placeholders.get(index).setText(contactName);
            index++;
        }

        cursor.close();
    }

    private void openSidebar() {
        sidebarLayout.setVisibility(View.VISIBLE);
        backgroundOverlay.setVisibility(View.VISIBLE);
        sidebarLayout.setTranslationX(sidebarLayout.getWidth());
        sidebarLayout.animate().translationX(0).setDuration(300).start();
    }

    private void closeSidebar() {
        sidebarLayout.animate()
                .translationX(sidebarLayout.getWidth())
                .setDuration(300)
                .withEndAction(() -> {
                    sidebarLayout.setVisibility(View.GONE);
                    backgroundOverlay.setVisibility(View.GONE);
                }).start();
    }

    private void setSelectedButton(Button selectedButton) {
        // Reset all buttons to default style
        resetButtonStyles();

        // Apply selected style to the clicked button
        selectedButton.setBackgroundColor(getResources().getColor(R.color.purple));
        selectedButton.setTextColor(getResources().getColor(R.color.orange));

        // Update Home button selection flag
        isHomeSelected = selectedButton == btnHome;
    }

    private void resetButtonStyles() {
        // Reset each button to default style
        Button[] buttons = {btnHome, btnSosContacts, btnSafetyTips, btnAboutUs, btnTermsConditions};
        for (Button button : buttons) {
            button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            button.setTextColor(getResources().getColor(R.color.dark_grey));
        }
    }

    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);

        AlertDialog customDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false) // the user can't cancel clicking outside
                .create();
        if (customDialog.getWindow() != null) {
            customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        customDialog.show();
        // Initializing layout Buttons
        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            performLogout(); // calls the logout method
            customDialog.dismiss(); // Close the Dialog
        });

        btnNo.setOnClickListener(v -> customDialog.dismiss()); // Closes the dialog
    }

    private void performLogout() {
        // Redirect to Login screen
        Intent intent = new Intent(Home.this, LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close the current activity
    }
}