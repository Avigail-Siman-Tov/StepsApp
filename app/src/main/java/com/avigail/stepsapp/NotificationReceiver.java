package com.avigail.stepsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class NotificationReceiver extends BroadcastReceiver {
private static final String CHANNEL_ID = "MyNotificationChannel";
private static final String CHANNEL_NAME = "Notification";
private static final int NOTIFICATION_ID = 1;

@Override
public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        // 2. Create Notification-Channel. (JUST ONCE!)
        NotificationChannel notificationChannel = new NotificationChannel(

                CHANNEL_ID, // Constant for Channel ID
                CHANNEL_NAME, // Constant for Channel NAME
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager.createNotificationChannel(notificationChannel);
        // 3. Create & show the Notification. (Every time you want to show notification)
        Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle("This is notify title!")
                .setContentText("This is your notification text.")
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);

        }

}


