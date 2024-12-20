package com.example.alertify;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    private View sidebarLayout;
    private View backgroundOverlay;
    private ImageButton closeButton;

    // Sidebar buttons
    private TextView btnHome;
    private TextView btnSosContacts;
    private TextView btnSafetyTips;
    private TextView btnAboutUs;
    private TextView btnTermsConditions;

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

        // Set listeners for sidebar buttons
        btnHome.setOnClickListener(v -> setSelectedButton(btnHome));
        btnSosContacts.setOnClickListener(v -> setSelectedButton(btnSosContacts));
        btnSafetyTips.setOnClickListener(v -> setSelectedButton(btnSafetyTips));
        btnAboutUs.setOnClickListener(v -> setSelectedButton(btnAboutUs));
        btnTermsConditions.setOnClickListener(v -> setSelectedButton(btnTermsConditions));

        // Default selection
        setSelectedButton(btnHome);

        // Set the layout for the Home activity
        setContentView(R.layout.activity_home);

        // Settings Button
        ImageButton openSettingsButton = findViewById(R.id.open_settings_button);

        // Sidebar and dark background
        sidebarLayout = findViewById(R.id.sidebar_layout);
        backgroundOverlay = findViewById(R.id.background_overlay);

        // Settings -> SOS button
        Button sosContactsButton = findViewById(R.id.btn_sos_contacts);

        // Btn open sidebar
        openSettingsButton.setOnClickListener(v -> openSidebar());

        // Closing sidebar when background clicked
        backgroundOverlay.setOnClickListener(v -> closeSidebar());
        // Close sidebar when button clicked
        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> closeSidebar());

        // Navigate to SosContacts page
        sosContactsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, SosContacts.class);
            startActivity(intent);
        });

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

    private void setSelectedButton(TextView selectedButton) {
        // Reset all buttons to default style
        resetButtonStyles();

        // Apply selected style
        selectedButton.setBackgroundColor(getResources().getColor(R.color.purple));
        selectedButton.setTextColor(getResources().getColor(R.color.orange));
    }

    private void resetButtonStyles() {
        // Reset each button to default style
        TextView[] buttons = {btnHome, btnSosContacts, btnSafetyTips, btnAboutUs, btnTermsConditions};
        for (TextView button : buttons) {
            button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            button.setTextColor(getResources().getColor(R.color.dark_grey));
        }
    }
}
