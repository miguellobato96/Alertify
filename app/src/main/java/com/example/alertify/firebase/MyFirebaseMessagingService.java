package com.example.alertify.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.alertify.R;
import com.example.alertify.Home;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = "SOS Alert"; // Default title
        String message = "You received an SOS alert."; // Default message

        // Handle notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle() != null ? remoteMessage.getNotification().getTitle() : title;
            message = remoteMessage.getNotification().getBody() != null ? remoteMessage.getNotification().getBody() : message;
        }

        // Handle data payload (if exists)
        if (!remoteMessage.getData().isEmpty()) {
            message += "\nLocation: " + remoteMessage.getData().get("location");
        }

        // Show the notification
        showNotification(title, message);
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "sos_alerts";

        // Create a notification channel
        NotificationChannel channel = new NotificationChannel(channelId, "SOS Alerts", NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);

        // Intent to open Home when notification is clicked
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Ensures compatibility
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Use an appropriate icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        notificationManager.notify(0, builder.build());
    }
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        // Log the new token (for debugging)
        System.out.println("New FCM Token: " + token);

        // Send this token to your server or store it for later use
        sendTokenToServer(token);
    }

    // Example method to send the token to your backend
    private void sendTokenToServer(String token) {
        // TODO: Implement this method to send the token to your server
    }

}
