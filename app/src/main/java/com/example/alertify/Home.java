package com.example.alertify;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MotionEvent;
import java.util.ArrayList;

import android.Manifest;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.util.ArrayList;
public class Home extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

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

    private FrameLayout sliderButton;
    private TextView sliderInstruction;
    private boolean isSliderActive = false;

    private boolean isHomeSelected = true; // Flag to track if Home is selected

    private ArrayList<Contact> pinnedContacts = new ArrayList<>();

    private ArrayList<TextView> placeholders;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Check and request SMS permissions
        checkAndRequestPermissions();

        // Initialize sidebar buttons
        btnHome = findViewById(R.id.btn_home);
        btnSosContacts = findViewById(R.id.btn_sos_contacts);
        btnSafetyTips = findViewById(R.id.btn_safety_tips);
        btnAboutUs = findViewById(R.id.btn_about_us);
        btnTermsConditions = findViewById(R.id.btn_terms_conditions);
        btnLogOut = findViewById(R.id.btn_logout);


        // Initialize slider elements
        sliderButton = findViewById(R.id.sliderButton);
        sliderInstruction = findViewById(R.id.sliderInstruction);

        sliderButton.setOnTouchListener(this::handleSliderMovement);



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
                new String[]{"name", "number"}, // Inclui o número na consulta

                new String[]{"name"},

                "isPinned = ?",
                new String[]{"1"},
                null,
                null,
                "pinned_order ASC"
        );


        // Processar contatos "pinned"
        pinnedContacts.clear(); // Limpa a lista que armazena contatos "pinned"


        // Clear placeholders and reset to "N/A"
        for (TextView placeholder : placeholders) {
            placeholder.setText("N/A");
        }

        // Populate placeholders and pinned contacts list
        int index = 0;
        while (cursor.moveToNext()) {
            String contactName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String contactNumber = cursor.getString(cursor.getColumnIndexOrThrow("number")); // Recupera o número

            // Adiciona o contato à lista de "pinned contacts"
            pinnedContacts.add(new Contact(contactName, contactNumber));

            // Atualiza os placeholders até o limite máximo
            if (index < placeholders.size()) {
                placeholders.get(index).setText(contactName);
                index++;
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

    private boolean handleSliderMovement(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX();
                float parentStart = sliderInstruction.getX();
                float parentEnd = parentStart + sliderInstruction.getWidth();
                float maxTranslation = sliderInstruction.getWidth() - sliderButton.getWidth();

                if (x >= parentStart && x <= parentEnd) {
                    float translationX = x - parentStart - sliderButton.getWidth() / 2;
                    sliderButton.setTranslationX(Math.min(Math.max(translationX, 0), maxTranslation));
                }
                return true;
            case MotionEvent.ACTION_UP:
                float finalPosition = sliderButton.getTranslationX() + sliderButton.getWidth();
                if (finalPosition >= sliderInstruction.getWidth() * 0.85) {
                    lockSliderAtEnd();
                } else {
                    sliderButton.animate().translationX(0).setDuration(200).start();
                }
                return true;
        }
        return false;
    }

    private void lockSliderAtEnd() {
        sliderButton.setTranslationX(sliderInstruction.getWidth() - sliderButton.getWidth());
        sliderInstruction.setText("Activated");
        isSliderActive = true;

        // Send SOS message
        sendSosMessage();
    }
    private void sendSosMessage() {
        // Verificar permissão antes de enviar mensagens
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission not granted. Cannot send SOS.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                "contacts",
                new String[]{"name", "number"},
                "isPinned = ?",
                new String[]{"1"},
                null, null, "pinned_order ASC"
        );

        SmsManager smsManager = SmsManager.getDefault();
        boolean success = false;

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));

            // Validar se o número é válido antes de tentar enviar a mensagem
            if (number == null || number.isEmpty()) {
                Toast.makeText(this, "Contact " + name + " has an invalid number.", Toast.LENGTH_SHORT).show();
                continue;
            }

            try {
                smsManager.sendTextMessage(number, null, "SOS! I need help.", null, null);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();

        if (success) {
            Toast.makeText(this, "SOS sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No SOS messages were successfully sent.", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public class Contact {
        private String name;
        private String number;

        public Contact(String name, String number) {
            this.name = name;
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }
    }


}


}
