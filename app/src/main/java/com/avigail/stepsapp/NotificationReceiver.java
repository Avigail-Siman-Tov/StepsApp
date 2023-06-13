package com.avigail.stepsapp;

//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//
//import androidx.core.app.ActivityCompat;
//import androidx.core.app.NotificationCompat;
//
//
//public class NotificationReceiver extends BroadcastReceiver {
//private static final String CHANNEL_ID = "MyNotificationChannel";
//private static final String CHANNEL_NAME = "Notification";
//private static final int NOTIFICATION_ID = 1;
//
//@Override
//public void onReceive(Context context, Intent intent) {
//        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
//
//        // 2. Create Notification-Channel. (JUST ONCE!)
//        NotificationChannel notificationChannel = new NotificationChannel(
//
//                CHANNEL_ID, // Constant for Channel ID
//                CHANNEL_NAME, // Constant for Channel NAME
//                NotificationManager.IMPORTANCE_DEFAULT);
//
//        notificationManager.createNotificationChannel(notificationChannel);
//        // 3. Create & show the Notification. (Every time you want to show notification)
//        Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
//                .setSmallIcon(R.drawable.baseline_notifications_24)
//                .setContentTitle("This is notify title!")
//                .setContentText("This is your notification text.")
//                .build();
//        notificationManager.notify(NOTIFICATION_ID, notification);
//
//        }
//
//}

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {
        private static final String CHANNEL_ID = "my_channel";
        private static final int NOTIFICATION_ID = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
                // Create a notification channel (required for Android 8.0 Oreo and above)
//
//
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String minSteps = sharedPreferences.getString("minStepsKey", " ");
//                if (< Integer.parseInt(minSteps)/2)
                createNotificationChannel(context);

                // Build the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.baseline_notifications_24)
                        .setContentTitle("My Notification")
                        .setContentText("This is a notification at 8 PM in the evening")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                // Show the notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        public static void setNotification(Context context) {
                // Get the current time
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                // Set the alarm time to 8 PM
                calendar.set(Calendar.HOUR_OF_DAY, 18);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                // If the alarm time has already passed, set it for the next day
                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                // Create an intent for the broadcast receiver
                Intent intent = new Intent(context, NotificationReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                // Set up the alarm manager to trigger the broadcast receiver at the specified time
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
        }

        private void createNotificationChannel(Context context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence channelName = "My Channel";
                        String channelDescription = "My Notification Channel";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;

                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
                        channel.setDescription(channelDescription);

                        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                }
        }
}






