package com.avigail.stepsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ForegroundService extends Service implements SensorEventListener {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static boolean isServiceRunning = false;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private int stepCount=0;
    private boolean isStepCounting = false;
    private float threshold = 9.776321f;
    private boolean isStepDetected = false;
    private float diff = 0;
    private float positiveThreshold = 0.8f;
    private float negativeThreshold = -0.8f;
    private float previousY = 0.0f;

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("start")) {
                startForegroundService();
                isServiceRunning = true;
            } else if (intent.getAction().equals("stop")) {
                stopForegroundService();
                isServiceRunning = false;
            }
        }
        return START_STICKY;
    }

//    private void startForegroundService() {
//        createNotificationChannel();
//
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//        Notification notification = new Notification.Builder(this, CHANNEL_ID)
//                .setContentTitle("Foreground Service")
//                .setContentText("Service is running")
//                .setSmallIcon(R.drawable.baseline_notifications_24)
//                .setContentIntent(pendingIntent)
//                .build();
//
//        startForeground(NOTIFICATION_ID, notification);
//    }

    private void startForegroundService() {
        createNotificationChannel();

        Intent stopIntent = new Intent(this, ForegroundService.class);
        stopIntent.setAction("stop");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.baseline_notifications_24)
//                .addAction(R.drawable.baseline_notifications_24, "Stop", stopPendingIntent)
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

    public void onSensorChanged(SensorEvent event) {

        float yAcceleration = event.values[1];


        if (!isStepCounting) {
            previousY = yAcceleration;
            isStepCounting = true;
        }
//        Log.d("mylog", "yAcceleration= " + String.valueOf(yAcceleration));
       // Log.d("mylog" , "previousY= " + String.valueOf(previousY));
        diff = yAcceleration - previousY;
//        Log.d("mylog" , "diff= " + String.valueOf(diff));
//        if (Math.abs(diff) > threshold) {
//            if (yAcceleration > previousY && yAcceleration > positiveThreshold) {
//                stepCount++;
//                stepCountTextView.setText("Step count: " + stepCount);
//            } else if (yAcceleration < previousY && yAcceleration < negativeThreshold) {
//                stepCount++;
//                stepCountTextView.setText("Step count: " + stepCount);
//            }
//            stepCount++;
//            stepCountTextView.setText("Step count: " + stepCount);
//        }

//        if (Math.abs(diff) > threshold) {
//            if (diff > 0) {
//                if (hasNegativeToPositiveTransition && yAcceleration > positiveThreshold) {
//                    stepCount++;
//                    stepCountTextView.setText(""+stepCount);
//
////                    insertData();
//                    hasNegativeToPositiveTransition = false;
//                }
//                hasPositiveToNegativeTransition = true;
//            } else {
//                if (hasPositiveToNegativeTransition && yAcceleration < negativeThreshold) {
//                    stepCount++;
//                    stepCountTextView.setText(""+stepCount);
////                    update_steps(dayOfWeek);
////                    insertData();
//                    hasPositiveToNegativeTransition = false;
//                }
//                hasNegativeToPositiveTransition = true;
//            }
//        }
//
//        previousY = yAcceleration;
        if (!isStepDetected && Math.abs(yAcceleration) > threshold) {
            Log.d("mylog", "yAcceleration> " + String.valueOf(yAcceleration));
            isStepDetected = true;
        }

        else if (isStepDetected && Math.abs(yAcceleration) < threshold) {
            Log.d("mylog", "yAcceleration< " + String.valueOf(yAcceleration));
            // Step completed
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


