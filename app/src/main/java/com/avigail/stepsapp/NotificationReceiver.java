package com.avigail.stepsapp;

import static com.avigail.stepsapp.ForegroundService.stepCount;
import static com.avigail.stepsapp.MainActivity.TodaySteps;

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
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {
        private static final String CHANNEL_ID = "my_channel";
        private static final int NOTIFICATION_ID = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String minSteps = sharedPreferences.getString("minStepsKey", " ");
                int min_steps = Integer.parseInt(minSteps);

                Log.d("mylog","minSteps"+min_steps);
                Log.d("mylog","today"+TodaySteps);
                int savedHour = sharedPreferences.getInt("hour", 0);
                int savedMinute = sharedPreferences.getInt("minute", 0);

                // Get the current time
                Calendar currentTime = Calendar.getInstance();
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int currentMinute = currentTime.get(Calendar.MINUTE);

                // Check if the current time matches the saved time
                if (currentHour == savedHour && currentMinute == savedMinute && TodaySteps < min_steps) {
                        createNotificationChannel(context);
                        sendNotification(context);
                }
        }

        private void sendNotification(Context context) {
                // Build the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.baseline_notifications_24)
                        .setContentTitle("Warning!")
                        .setContentText("You walked a few steps, start walking")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                // Show the notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        public static void setNotification(Context context, int hour, int minute) {
                // Get the current time
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                if (calendar.before(Calendar.getInstance())) {
                        calendar.add(Calendar.DATE, 1); // Schedule for the next day if the desired time has already passed
                }

                Intent intent = new Intent(context, NotificationReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, hour * 100 + minute, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }



        private void createNotificationChannel(Context context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "My Channel";
                        String description = "Notification Channel";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                        channel.setDescription(description);

                        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                }
        }
}






