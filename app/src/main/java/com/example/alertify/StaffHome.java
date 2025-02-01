package com.example.alertify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class StaffHome extends AppCompatActivity implements OnMapReadyCallback {

    private LinearLayout notificationList;
    private GoogleMap googleMap;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng alertLocation; // Stores the location of the alert

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        Button btnLogout = findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(StaffHome.this, StaffLogIn.class);
            startActivity(intent);
        });

        // Subscribe staff users to receive alerts
        FirebaseMessaging.getInstance().subscribeToTopic("staff_alerts");

        // Initialize map
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize UI elements
        notificationList = findViewById(R.id.notificationList);

        // Example notification (Testing Purposes)
        createAlertNotification("John Doe", new LatLng(37.7749, -122.4194));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        googleMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng staffLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(staffLocation, 15));
            }
        });
    }

    /**
     * Creates an SOS alert notification in the UI
     */
    private void createAlertNotification(String userName, LatLng alertLatLng) {
        View notificationView = getLayoutInflater().inflate(R.layout.item_notification, null);
        TextView alertMessage = notificationView.findViewById(R.id.alertMessage);
        ImageButton btnIgnore = notificationView.findViewById(R.id.btnIgnore);
        ImageButton btnGo = notificationView.findViewById(R.id.btnGo);

        alertMessage.setText(userName + " needs help!");

        btnIgnore.setOnClickListener(v -> notificationList.removeView(notificationView));

        btnGo.setOnClickListener(v -> {
            startNavigation(alertLatLng);
            updateNotificationState(notificationView, true);
        });

        notificationList.addView(notificationView, 0);
    }

    /**
     * Updates notification UI when an alert is accepted by someone
     */
    private void updateNotificationState(View notificationView, boolean accepted) {
        TextView alertMessage = notificationView.findViewById(R.id.alertMessage);
        ImageButton btnGo = notificationView.findViewById(R.id.btnGo);

        if (accepted) {
            alertMessage.setText("Someone is already responding...");
            btnGo.setEnabled(false);
            btnGo.setAlpha(0.5f);
        }
    }

    /**
     * Starts navigation from staff's location to the alert location
     */
    private void startNavigation(LatLng destination) {
        if (googleMap == null || destination == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng staffLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Draw path from staff to user
                drawRoute(staffLocation, destination);
            }
        });
    }

    /**
     * Fetches and draws a route between staff and alert location
     */
    private void drawRoute(LatLng staffLocation, LatLng alertLocation) {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");

            String url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=" + staffLocation.latitude + "," + staffLocation.longitude +
                    "&destination=" + alertLocation.latitude + "," + alertLocation.longitude +
                    "&key=" + apiKey;

            new Thread(() -> {
                try {
                    URL apiUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                    connection.setRequestMethod("GET");

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    parseRoute(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Parses the JSON response and draws the route on the map
     */
    private void parseRoute(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");

                List<LatLng> points = PolyUtil.decode(encodedPolyline);

                runOnUiThread(() -> {
                    googleMap.addPolyline(new PolylineOptions()
                            .addAll(points)
                            .width(10)
                            .color(getResources().getColor(R.color.orange)));
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No route found.", Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribe from Firebase when leaving the screen
     */
    @Override
    protected void onDestroy() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("staff_alerts");
        super.onDestroy();
    }
}
