package com.example.alertify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.alertify.database.ContactDatabaseHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class Home extends AppCompatActivity implements OnMapReadyCallback {

    private static final int SMS_PERMISSION_CODE = 100;

    // Sidebar layout and buttons
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

    // Slider elements for SOS functionality
    private FrameLayout sliderButton;
    private TextView sliderInstruction;
    private boolean isSliderActive = false;

    private boolean isHomeSelected = true; // Flag to track if the "Home" button is selected

    private ArrayList<Contact> pinnedContacts = new ArrayList<>(); // List of pinned contacts

    // Placeholders for pinned contacts in the UI
    private ArrayList<TextView> placeholders;

    // Database helper for retrieving contacts
    private ContactDatabaseHelper contactDatabaseHelper;

    // Google Map variables
    private MapView mapView;
    private GoogleMap googleMap;

    // Location client for accessing the user's location
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Request permissions (combined for location and SMS)
        checkAndRequestPermissions();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize sidebar buttons
        btnHome = findViewById(R.id.btn_home);
        btnSosContacts = findViewById(R.id.btn_sos_contacts);
        btnSafetyTips = findViewById(R.id.btn_safety_tips);
        btnAboutUs = findViewById(R.id.btn_about_us);
        btnTermsConditions = findViewById(R.id.btn_terms_conditions);
        btnLogOut = findViewById(R.id.btn_logout);

        // Initialize slider elements for SOS activation
        sliderButton = findViewById(R.id.sliderButton);
        sliderInstruction = findViewById(R.id.sliderInstruction);

        sliderButton.setOnTouchListener(this::handleSliderMovement);

        // Initialize placeholders for pinned contacts in the UI
        placeholders = new ArrayList<>();
        placeholders.add(findViewById(R.id.contact_text_1));
        placeholders.add(findViewById(R.id.contact_text_2));
        placeholders.add(findViewById(R.id.contact_text_3));
        placeholders.add(findViewById(R.id.contact_text_4));

        // Initialize database helper for contacts
        contactDatabaseHelper = new ContactDatabaseHelper(this);

        // Load pinned contacts from the database
        loadPinnedContacts();

        // Initialize sidebar layout and elements
        sidebarLayout = findViewById(R.id.sidebar_layout);
        backgroundOverlay = findViewById(R.id.background_overlay);
        ImageButton openSettingsButton = findViewById(R.id.open_settings_button);

        // Set listeners for sidebar buttons
        openSettingsButton.setOnClickListener(v -> openSidebar());
        backgroundOverlay.setOnClickListener(v -> closeSidebar());
        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> closeSidebar());

        // Set actions for sidebar menu buttons
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
            startActivity(new Intent(Home.this, SosContacts.class));
        });

        btnSafetyTips.setOnClickListener(v -> {
            setSelectedButton(btnSafetyTips);
            isHomeSelected = false;
            // TODO: Navigate to Safety Tips page
        });

        btnAboutUs.setOnClickListener(v -> {
            setSelectedButton(btnAboutUs);
            isHomeSelected = false;
            // TODO: Navigate to About Us page
        });

        btnTermsConditions.setOnClickListener(v -> {
            setSelectedButton(btnTermsConditions);
            isHomeSelected = false;
            // TODO: Navigate to Terms & Conditions page
        });

        btnLogOut.setOnClickListener(v -> showLogoutDialog());
    }


    // Checks if location permissions are granted and requests them if not
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Requests perms
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Update map
            setupMap();
        }
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Configure the map if permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupMap();
        }
    }

    // Sets up the Google Map with the user's location
    private void setupMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                googleMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload pinned contacts when Home is resumed
        loadPinnedContacts();

        // Always select the Home button
        setSelectedButton(btnHome);
        isHomeSelected = true;

        // Resume the map view
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Pause the map view
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Destroy the map view
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the map view state
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    // Loads pinned contacts from the database and updates the UI
    private void loadPinnedContacts() {
        SQLiteDatabase database = contactDatabaseHelper.getReadableDatabase();

        // Query pinned contacts, ordered by pinned_order
        Cursor cursor = database.query(
                "contacts",
                new String[]{"name", "number"}, // Include the phone number in the query
                "isPinned = ?",
                new String[]{"1"},
                null,
                null,
                "pinned_order ASC"
        );

        // Clear the list of pinned contacts
        pinnedContacts.clear();

        // Reset placeholders to "N/A"
        for (TextView placeholder : placeholders) {
            placeholder.setText("N/A");
            placeholder.setOnClickListener(null); // Clear any existing click listeners
        }

        // Populate placeholders with contact names and set click listeners
        int index = 0;
        while (cursor.moveToNext()) {
            String contactName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String contactNumber = cursor.getString(cursor.getColumnIndexOrThrow("number"));

            // Add the contact to the pinned contacts list
            pinnedContacts.add(new Contact(contactName, contactNumber));

            // Update placeholders up to the maximum limit
            if (index < placeholders.size()) {
                TextView placeholder = placeholders.get(index);
                placeholder.setText(contactName);

                // Set click listener for the placeholder with feedback
                placeholder.setOnClickListener(v -> {
                    // Visual feedback: Change the placeholder's background color temporarily
                    placeholder.setBackgroundResource(R.color.orange); // Change the entire background of the placeholder

                    // Reset the background color after a short delay
                    new Handler().postDelayed(() -> {
                        placeholder.setBackgroundResource(R.color.light_purple); // Reset to the default color
                    }, 200); // 200ms delay for visual effect

                    // Add vibration feedback
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)); // 100ms vibration
                    }

                    // Send the safe message
                    sendSafeMessage(contactName, contactNumber);
                });

                index++;
            }
        }


        cursor.close();
    }


    // Sends a "safe message" to a specific contact
    private void sendSafeMessage(String contactName, String contactNumber) {
        // Check for SMS permission before sending the message
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission not granted. Cannot send the message.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "I don't feel safe, please keep an eye on me!";
            smsManager.sendTextMessage(contactNumber, null, message, null, null);

            Toast.makeText(this, "Message sent to " + contactName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send message to " + contactName, Toast.LENGTH_SHORT).show();
        }
    }


    // Opens the sidebar with an animation
    private void openSidebar() {
        sidebarLayout.setVisibility(View.VISIBLE);
        backgroundOverlay.setVisibility(View.VISIBLE);
        sidebarLayout.setTranslationX(sidebarLayout.getWidth());
        sidebarLayout.animate().translationX(0).setDuration(300).start();
    }

    // Closes the sidebar with an animation
    private void closeSidebar() {
        sidebarLayout.animate()
                .translationX(sidebarLayout.getWidth())
                .setDuration(300)
                .withEndAction(() -> {
                    sidebarLayout.setVisibility(View.GONE);
                    backgroundOverlay.setVisibility(View.GONE);
                }).start();
    }

    // Updates the styles of the selected sidebar button
    private void setSelectedButton(Button selectedButton) {
        resetButtonStyles();

        // Apply selected style
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_purple));
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.orange));

        isHomeSelected = selectedButton == btnHome;
    }

    // Resets the styles of all sidebar buttons to default
    private void resetButtonStyles() {
        Button[] buttons = {btnHome, btnSosContacts, btnSafetyTips, btnAboutUs, btnTermsConditions};
        for (Button button : buttons) {
            button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            button.setTextColor(ContextCompat.getColor(this, R.color.dark_grey));
        }
    }

    // Shows a logout confirmation dialog
    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);

        AlertDialog customDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (customDialog.getWindow() != null) {
            customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        customDialog.show();

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        // Confirm logout
        btnYes.setOnClickListener(v -> {
            performLogout();
            customDialog.dismiss();
        });

        // Cancel logout
        btnNo.setOnClickListener(v -> customDialog.dismiss());
    }

    // Logs the user out and redirects to the login screen
    private void performLogout() {
        // Reset the logged-in state in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AlertifyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false); // Set login state to false
        editor.apply(); // Apply changes

        // Redirect to the LogIn screen
        Intent intent = new Intent(Home.this, LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Close all the other activities and opens the new
        startActivity(intent);
        finish(); // Finish the current activity ( Home )
    }

    // Handles slider movement for SOS activation
    private boolean handleSliderMovement(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX(); //get actual position of the slider
                float parentStart = sliderInstruction.getX(); //Define start of container
                float parentEnd = parentStart + sliderInstruction.getWidth(); //Define end of container
                float maxTranslation = sliderInstruction.getWidth() - sliderButton.getWidth(); //calculates the max movement of the button.

                if (x >= parentStart && x <= parentEnd) {
                    float translationX = x - parentStart - sliderButton.getWidth() / 2; //Calculate the new horizontal position by the touch.
                    sliderButton.setTranslationX(Math.min(Math.max(translationX, 0), maxTranslation)); // Works as guarantee that the button will not leave the container
                }
                return true;
            case MotionEvent.ACTION_UP: //Detects the button final's touch
                float finalPosition = sliderButton.getTranslationX() + sliderButton.getWidth(); //Calculate's the button position + his width.
                if (finalPosition >= sliderInstruction.getWidth() * 0.85) {            //confirms if the button is at least 85% before the container ends
                    lockSliderAtEnd();                                                  // if that happens the slider is blocked at the end
                } else {
                    sliderButton.animate().translationX(0).setDuration(200).start(); //if not the button will go back to the initial position
                }
                return true;
        }
        return false;
    }

    // Locks the slider at the end position and starts the countdown
    private void lockSliderAtEnd() {
        sliderButton.setTranslationX(sliderInstruction.getWidth() - sliderButton.getWidth());  //block the slider at the end
        sliderInstruction.setText("Sending SOS in 5...");                               //update's the slider text
        isSliderActive = true;                                                          //activates the slider

        // Start countdown
        startCountdown(5); // 5 seconds countdown
    }

    // Start a countdown with the ability to cancel
    private void startCountdown(int seconds) {
        Handler handler = new Handler();
        Runnable countdownRunnable = new Runnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    sliderInstruction.setText("Sending SOS in " + timeLeft + "...");
                    timeLeft--;
                    handler.postDelayed(this, 1000); // Continue countdown every second
                } else {
                    // Send SOS when countdown finishes
                    sendSosMessage();
                    resetSliderWithDelay(2000); // Reset slider after 2 seconds
                }
            }
        };

        // Add cancel button functionality
        addCancelButton(handler, countdownRunnable, seconds);

        // Start the countdown
        handler.post(countdownRunnable);
    }

    // Add a cancel button to stop the countdown
    private void addCancelButton(Handler handler, Runnable countdownRunnable, int seconds) {
        Button cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
        cancelButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        cancelButton.setPadding(16, 8, 16, 8);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.bottomMargin = 100;
        cancelButton.setLayoutParams(params);

        // Add the button to the layout
        FrameLayout rootLayout = findViewById(android.R.id.content);
        rootLayout.addView(cancelButton);

        cancelButton.setOnClickListener(v -> {
            // Cancel the countdown
            handler.removeCallbacks(countdownRunnable);
            sliderInstruction.setText("Cancelled");
            resetSliderWithDelay(1000); // Reset slider after 1 second
            rootLayout.removeView(cancelButton); // Remove the button
        });

        // Automatically remove the cancel button after the countdown finishes
        new Handler().postDelayed(() -> {
            if (rootLayout.indexOfChild(cancelButton) != -1) {
                rootLayout.removeView(cancelButton); // Remove the button if it still exists
            }
        }, (seconds + 2) * 1000L); // Buffer to ensure button removal after the countdown
    }

    // Reset the slider to its initial position with a delay
    private void resetSliderWithDelay(int delay) {
        new Handler().postDelayed(() -> {
            sliderButton.animate().translationX(0).setDuration(200).start();
            sliderInstruction.setText("Slide to Send SOS");
            isSliderActive = false;
        }, delay);
    }

    // Sends an SOS message to pinned contacts
    private void sendSosMessage() {
        // Check for SMS permission before sending messages
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {                                         //if not the method is canceled
            Toast.makeText(this, "SMS permission not granted. Cannot send SOS.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Execute a query to find the contacts where column isPinned = 1.
        SQLiteDatabase database = contactDatabaseHelper.getReadableDatabase();
        Cursor cursor = database.query(
                "contacts",
                new String[]{"name", "number"},
                "isPinned = ?",
                new String[]{"1"},
                null, null, "pinned_order ASC"
        );

        SmsManager smsManager = SmsManager.getDefault();                        //uses the SMS manager to send the message
        boolean success = false;

        while (cursor.moveToNext()) {                                           // method used to search the results of the query
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));

            // Validate if the number is valid before attempting to send a message
            if (number == null || number.isEmpty()) {
                Toast.makeText(this, "Contact " + name + " has an invalid number.", Toast.LENGTH_SHORT).show();
                continue;
            }
            // Try catch to send the SOS message
            try {
                smsManager.sendTextMessage(number, null, "SOS! I need help.", null, null);
                success = true;
            } catch (Exception e) {             // Captures every error that may occur during try
                e.printStackTrace(); // Prints to the logcat so it is easier to diagnose the error
            }
        }
        cursor.close();

        if (success) {
            Toast.makeText(this, "SOS sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No SOS messages were successfully sent.", Toast.LENGTH_SHORT).show();
        }
    }

    // Checks and requests SMS permissions if not already granted
    private void checkAndRequestPermissions() {
        // List of permissions to request
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS
        };

        // Check if any of the permissions are not granted
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // Request all necessary permissions
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 1);
        }
    }

    //Verifies if all permissions were conceded
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // iterates all the results and checks if the location and sms permissions were given
        if (requestCode == 1) {
            boolean locationGranted = false;
            boolean smsGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    locationGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                if (permissions[i].equals(Manifest.permission.SEND_SMS)) {
                    smsGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }

            // Handle location permission
            if (locationGranted) {
                setupMap();
            } else {
                Toast.makeText(this, "Location permission is required for maps.", Toast.LENGTH_SHORT).show();
            }

            // Handle SMS permission
            if (!smsGranted) {
                Toast.makeText(this, "SMS permission is required to send alerts.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // Contact class to store information about contacts
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
