package com.example.alertify;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.alertify.database.ContactDatabaseHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import java.util.ArrayList;

public class StaffHome extends AppCompatActivity {

    private FrameLayout sliderButton;
    private View expandedView1;
    private ImageButton toggleButton1;

    private ArrayList<View> placeholders;
    private ContactDatabaseHelper contactDatabaseHelper;
    private GoogleMap googleMap;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        // Find views
        View placeholder1 = findViewById(R.id.contact_placeholderStaff_1);
        expandedView1 = findViewById(R.id.contact_placeholderStaffOpen_1);
        toggleButton1 = findViewById(R.id.open_button);

        // Initially hide the expanded section
        expandedView1.setVisibility(View.GONE);

        // Allow only the button to trigger expansion
        toggleButton1.setOnClickListener(v -> toggleContactDetails(expandedView1, toggleButton1));

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize slider elements
        sliderButton = findViewById(R.id.sliderButton);

        // Initialize placeholders for pinned contacts
        placeholders = new ArrayList<>();
        placeholders.add(placeholder1);
        placeholders.add(expandedView1);

        // Initialize database helper for contacts
        contactDatabaseHelper = new ContactDatabaseHelper(this);
    }

    // Function to toggle the contact details visibility
    private void toggleContactDetails(View expandView, ImageButton toggleButton) {
        if (expandView.getVisibility() == View.GONE) {
            expandView.setVisibility(View.VISIBLE);
            expandView.animate().alpha(1.0f).setDuration(200).start();
            toggleButton.setImageResource(R.drawable.close_icon); // Change icon to indicate collapse
        } else {
            expandView.animate().alpha(0.0f).setDuration(200).withEndAction(() -> expandView.setVisibility(View.GONE)).start();
            toggleButton.setImageResource(R.drawable.ic_arrow_right); // Change icon to indicate expansion
        }
    }
}
