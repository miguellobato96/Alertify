package com.example.alertify;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;

public class Home extends AppCompatActivity {

    private View sidebarLayout;
    private View backgroundOverlay;
    private ImageButton closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
    }

