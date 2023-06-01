package com.avigail.stepsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

public class ForegroundService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static boolean isServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("start")) {
            startForegroundService();
            isServiceRunning = true;
        } else if (intent.getAction().equals("stop")) {
            stopForegroundService();
            isServiceRunning = false;
        }
        return START_STICKY;
    }

    private void startForegroundService() {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
