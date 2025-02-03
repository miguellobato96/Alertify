package com.example.alertify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.messaging.FirebaseMessaging;

public class StaffHome extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private LinearLayout notificationList;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        // Subscribe staff users to receive alerts
        FirebaseMessaging.getInstance().subscribeToTopic("staff_alerts");

        // Initialize UI Elements
        mapView = findViewById(R.id.mapView);
        notificationList = findViewById(R.id.notificationList);
        btnLogout = findViewById(R.id.btn_logout);

        // MapView Setup
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Logout Button Click -> Redirect to Login
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(StaffHome.this, StaffLogIn.class);
            startActivity(intent);
            finish(); // Close current activity
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // Future: Add markers when an alert is received
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        FirebaseMessaging.getInstance().unsubscribeFromTopic("staff_alerts");
    }
}
