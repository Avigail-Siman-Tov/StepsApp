package com.avigail.stepsapp;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity{
    private TextView stepCountTextView, txtHello;
    private int stepCount;

    private Button startStopButton;


    int steps_day[]= new int[7];

    Calendar calendar = Calendar.getInstance();
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepCountTextView = findViewById(R.id.stepCountTextView);
        txtHello = findViewById(R.id.txtHello);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("nameKey", " ");
        txtHello.setText("hello "+ name);
        String minSteps = sharedPreferences.getString("minStepsKey", " ");
//        NotificationReceiver.setNotification(getApplicationContext());


        startStopButton = findViewById(R.id.btn_start_stop);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceRunning()) {
                    startForegroundService();
                    startStopButton.setText("Stop");
                } else {
                    stopForegroundService();
                    startStopButton.setText("Start");
                }
            }
        });

//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        counter = stepCountTextView.getText().toString();
//        steps_day[0] = Integer.parseInt(counter);
//        Map<String, Object> steps = new HashMap<>();
//        steps.put("steps_day", Arrays.asList(steps_day[0],steps_day[1],steps_day[2],steps_day[3],steps_day[4],steps_day[5],steps_day[6]));
//
//        db.collection("steps")
//                .add(steps)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });


//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        if (accelerometerSensor != null) {
//            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        } else {
//            Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show();
//        }
    }

    private boolean isServiceRunning() {
        // Check if the service is running (Implement your own logic)
        // For example, you can use a boolean flag in your service class
        // or use the ActivityManager to check running services.
        return ForegroundService.isServiceRunning;
    }

    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.setAction("start");
        startService(serviceIntent);
    }

    private void stopForegroundService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.setAction("stop");
        startService(serviceIntent);
    }

    private void bringData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("steps").document("week");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<Long> stepsArray = (List<Long>) document.get("steps_day");
                        for (int i = 0; i < stepsArray.size(); i++) {
                            steps_day[i] = ((Long) stepsArray.get(i)).intValue();
                        }
                        Log.d("mylog", "Steps array: " + stepsArray);
                        init_steps(dayOfWeek);
                    } else {
                        Log.d("mylog", "No such document");
                    }
                } else {
                    Log.d(TAG, "Error getting document: " + task.getException());
                }
            }

        });
    }
    private void insertData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> steps = new HashMap<>();
        steps.put("steps_day", Arrays.asList(steps_day[0], steps_day[1], steps_day[2], steps_day[3], steps_day[4], steps_day[5], steps_day[6]));

        db.collection("steps").document("week")
                .set(steps)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
    private void bring_barChart(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("steps").document("week")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<Long> stepsList = (List<Long>) document.get("steps_day");
                                List<Integer> values = new ArrayList<>();
                                for (Long steps : stepsList) {
                                    values.add(steps.intValue());
                                }

                                // Create bar chart with values
                                createBarChart(values);
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "Error getting document: ", task.getException());
                        }
                    }
                });
    }

    private void init_steps(int dayOfWeek){

        Log.d("mylog", "I am in init");
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                stepCountTextView.setText(""+ steps_day[0]);
                stepCount = steps_day[0];
                break;
            case Calendar.MONDAY:
                stepCountTextView.setText(""+ steps_day[1]);
                stepCount = steps_day[1];
                break;
            case Calendar.TUESDAY:
                stepCountTextView.setText(""+ steps_day[2]);
                stepCount = steps_day[2];
                break;
            case Calendar.WEDNESDAY:
                stepCountTextView.setText(""+ steps_day[3]);
                stepCount = steps_day[3];
                break;
            case Calendar.THURSDAY:
                stepCountTextView.setText(""+ steps_day[4]);
                stepCount = steps_day[4];
                break;
            case Calendar.FRIDAY:
                stepCountTextView.setText(""+ steps_day[5]);
                stepCount = steps_day[5];
                break;
            case Calendar.SATURDAY:
                stepCountTextView.setText(""+ steps_day[6]);
                stepCount = steps_day[6];
                break;
        }
    }
    private void update_steps(int dayOfWeek){
        String count= stepCountTextView.getText().toString();
        count = count.trim();
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                steps_day[0] = Integer.parseInt(count);
                break;
            case Calendar.MONDAY:
                steps_day[1] = Integer.parseInt(count);
                break;
            case Calendar.TUESDAY:
                steps_day[2] = Integer.parseInt(count);
                break;
            case Calendar.WEDNESDAY:
                steps_day[3] = Integer.parseInt(count);
                break;
            case Calendar.THURSDAY:
                steps_day[4] = Integer.parseInt(count);
                break;
            case Calendar.FRIDAY:
                steps_day[5] = Integer.parseInt(count);
                break;
            case Calendar.SATURDAY:
                steps_day[6] = Integer.parseInt(count);
                break;
        }
    }

    private void createBarChart(List<Integer> values) {
        // Use your preferred chart library to create the bar chart
        // Here, you can use MPAndroidChart or any other chart library of your choice
        // Implement the chart creation logic here using the 'values' list
        // Example chart creation code using MPAndroidChart:

        BarChart barChart = findViewById(R.id.barChart);

        List<BarEntry> entries = new ArrayList<>();
        String[] daysOfWeek = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

        for (int i = 0; i < values.size(); i++) {
            entries.add(new BarEntry(i, values.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Steps");
        BarData barData = new BarData(dataSet);

        // Set days of the week as initial title
        barChart.getDescription().setText(" ");

        // Set x-axis labels as days of the week
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < daysOfWeek.length) {
                    return daysOfWeek[index];
                }
                return "";
            }
        });

        // Increase headers on the side (y-axis)
        YAxis yAxisLeft = barChart.getAxisLeft();
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisLeft.setTextSize(12f);
        yAxisRight.setTextSize(12f);

        // Increase headers above (x-axis)
        xAxis.setTextSize(12f);

        // Increase numbers above each column
        dataSet.setValueTextSize(12f);

        yAxisLeft.setAxisLineColor(Color.TRANSPARENT);
        yAxisLeft.setAxisLineWidth(0f);
        yAxisLeft.setGridColor(Color.TRANSPARENT);

        yAxisRight.setAxisLineColor(Color.TRANSPARENT);
        yAxisRight.setAxisLineWidth(0f);
        yAxisRight.setGridColor(Color.TRANSPARENT);

        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setAxisLineWidth(0f);
        xAxis.setGridColor(Color.TRANSPARENT);
        barChart.setData(barData);
        barChart.invalidate(); // Refresh the chart
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem aboutMenu = menu.add("About");
        MenuItem settingsMenu = menu.add("Settings");
        MenuItem exitMenu = menu.add("Exit");

        aboutMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                aboutAlertDialog();
                return false;
            }
        });

        settingsMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                IntentSettingActivity();
                return false;
            }
        });

        exitMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                exitAlertDialog();
                return false;
            }
        });

        return true;
    }

    private void aboutAlertDialog() {
        String strDeviceOS = "Android OS " + Build.VERSION.RELEASE + " API " + Build.VERSION.SDK_INT;

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("About App");
        dialog.setMessage("\nMy First Android mobile App!" + "\n\n" + strDeviceOS + "\n\n" + "By ILAN PERETZ 2023.");

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();   // close this dialog
            }
        });
        dialog.show();
    }

    private void IntentSettingActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void exitAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.baseline_exit_to_app_24);
        dialog.setTitle("Exit App");
        dialog.setMessage("Are you sure ?");
        dialog.setCancelable(false);

        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                stopForegroundService();
//                startStopButton.setText("Start");
                finish();   // destroy this activity
            }
        });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();   // close this dialog
            }
        });
        dialog.show();
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        bringData();
        bring_barChart();
    }
    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        update_steps(dayOfWeek);
        insertData();
    }
}










