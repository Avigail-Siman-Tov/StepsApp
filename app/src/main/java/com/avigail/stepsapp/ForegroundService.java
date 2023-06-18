package com.avigail.stepsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class ForegroundService extends Service implements SensorEventListener {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";


    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    public static int stepCount=0;
    private boolean isStepCounting = false;
    private float threshold = 9.78f;
    private boolean isStepDetected = false;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show();
        }
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int savedHour = sharedPreferences.getInt("hour", 0); // 0 is the default value if the key is not found
        int savedMinute = sharedPreferences.getInt("minute", 0);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        NotificationReceiver.setNotification(getApplicationContext(),savedHour,savedMinute);
    }

    public static final int FG_NOTIFICATION_ID = 111;
    private boolean isRunning;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("mylog", "MyFGService - onStartCommand()");

        createFGNotification();

        // do heavy work on a background thread
        isRunning = true;
        return START_NOT_STICKY;
    }

    private void createFGNotification()
    {
        // create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "FG Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(serviceChannel);
        }

        // start the MainActivity when notification tap
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // create Notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Service Running...")
                .setContentText("Tap to Stop this Service!")
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                //.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.baseline_notifications_24, "PLAY", pendingIntent)
                .build();

        startForeground(FG_NOTIFICATION_ID, notification);

//        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        int savedHour = sharedPreferences.getInt("hour", 0); // 0 is the default value if the key is not found
//        int savedMinute = sharedPreferences.getInt("minute", 0);
//        Calendar calendar = Calendar.getInstance();
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//        NotificationReceiver.setNotification(getApplicationContext(),savedHour,savedMinute);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        isRunning = false;  // stop thread job

    }


    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onSensorChanged(SensorEvent event) {

        float yAcceleration = event.values[1];


        if (!isStepCounting) {
            isStepCounting = true;
        }
//        Log.d("mylog", "yAcceleration = " + String.valueOf(yAcceleration));
        if (!isStepDetected && yAcceleration > threshold) {
            isStepDetected = true;
        }

        else if (isStepDetected && yAcceleration < threshold) {
            stepCount++;
            Log.d("mylog","count"+ stepCount);
            isStepDetected = false;
            //stepCountTextView.setText(" " + stepCount);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }
}


