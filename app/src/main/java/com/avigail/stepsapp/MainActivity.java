package com.avigail.stepsapp;

import static com.avigail.stepsapp.ForegroundService.stepCount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity{
    private TextView stepCountTextView, txtHello;
    public static int TodaySteps;
    int[] stepsPerDay = new int[7]; // Array to store steps for each day of the week
    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_WEEK);
    int min_steps;

    private ProgressBar progressBar;


    private int progress = 0;
    Button buttonIncrement;
//    ProgressBar progressBar;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);
        textView = findViewById(R.id.text_view_progress);




//        Button button1 = findViewById(R.id.button1);
//        Button button2 = findViewById(R.id.button2);
//
//
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Perform actions for Rule 1 button
//
//            }
//        });
//
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Perform actions for Rule 2 button
//
//            }
//        });

        stepCountTextView = findViewById(R.id.stepCountTextView);
        txtHello = findViewById(R.id.txtHello);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("nameKey", " ");
        txtHello.setText("hello "+ name);
        String minSteps = sharedPreferences.getString("minStepsKey", " ");
        min_steps = Integer.parseInt(minSteps);
        progressBar.setMax(min_steps);


        int savedHour = sharedPreferences.getInt("hour", 0); // 0 is the default value if the key is not found
        int savedMinute = sharedPreferences.getInt("minute", 0);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        Log.d("mylog","emuHour"+ hour);
        Log.d("mylog","SetHour"+ savedHour);
        Log.d("mylog","emuMin"+ minute);
        Log.d("mylog","SetMin"+ savedMinute);
        NotificationReceiver.setNotification(getApplicationContext(),savedHour,savedMinute);
        checkIfMyFGServiceRunning();
        bringStepsWeek();
    }

    private void updateProgressBar() {
        progressBar.setProgress(TodaySteps);
        textView.setText(String.valueOf(TodaySteps));
    }
    public void startService(View v)
    {
        stepCount=0;
        startForegroundService(new Intent(this, ForegroundService.class));
        findViewById(R.id.btnStratID).setEnabled(false);
        findViewById(R.id.btnStopID).setEnabled(true);
        Toast.makeText(this, "Service Started!", Toast.LENGTH_LONG).show();
        Log.d("mylog", "Service Started!");
    }

    public void stopService(View v)
    {
        insertData_steps();
        stopService(new Intent(this, ForegroundService.class));

        findViewById(R.id.btnStratID).setEnabled(true);
        findViewById(R.id.btnStopID).setEnabled(false);
        stepsPerDay = new int[7];
        bringStepsWeek();
        Toast.makeText(this, "Service Stoped!", Toast.LENGTH_LONG).show();
        Log.d("mylog", "Service Stoped!");
    }

    private void checkIfMyFGServiceRunning()
    {
        boolean isRunning = false;
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (ForegroundService.class.getName().equals(service.service.getClassName()))
            {
                isRunning = true;
                break;
            }
        }

        if (isRunning)
        {
            findViewById(R.id.btnStratID).setEnabled(false);
            findViewById(R.id.btnStopID).setEnabled(true);
        } else
        {
            findViewById(R.id.btnStratID).setEnabled(true);
            findViewById(R.id.btnStopID).setEnabled(false);
        }
    }

    public void insertData_steps(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        Map<String, Object> steps = new HashMap<>();
        steps.put("steps_count", stepCount);

        DocumentReference stepsDocument = db.collection("steps").document();
        String documentId = stepsDocument.getId();
        steps.put("date", currentDate);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                stepsDocument.set(steps)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Document created successfully
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle any errors that occurred
                            }
                        });
            }
        });
        thread.start();
    }

    private void init_steps(int dayOfWeek , int[] stepsPerDay){
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                stepCountTextView.setText(""+ stepsPerDay[0]);
                TodaySteps = stepsPerDay[0];
                break;
            case Calendar.MONDAY:
                stepCountTextView.setText(""+ stepsPerDay[1]);
                TodaySteps = stepsPerDay[1];
                break;
            case Calendar.TUESDAY:
                stepCountTextView.setText(""+ stepsPerDay[2]);
                TodaySteps = stepsPerDay[2];
                break;
            case Calendar.WEDNESDAY:
                stepCountTextView.setText(""+ stepsPerDay[3]);
                TodaySteps = stepsPerDay[3];
                break;
            case Calendar.THURSDAY:
                stepCountTextView.setText(""+ stepsPerDay[4]);
                TodaySteps = stepsPerDay[4];
                break;
            case Calendar.FRIDAY:
                stepCountTextView.setText(""+ stepsPerDay[5]);
                TodaySteps = stepsPerDay[5];
                break;
            case Calendar.SATURDAY:
                stepCountTextView.setText(""+ stepsPerDay[6]);
                TodaySteps = stepsPerDay[6];
                break;
        }
    }

private void createBarChart(int[] values) {
    Log.d("mylog","values"+ Arrays.toString(values)) ;
    BarChart barChart = findViewById(R.id.barChart);

    List<BarEntry> entries = new ArrayList<>();
    String[] daysOfWeek = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

    for (int i = 0; i < values.length; i++) {
        entries.add(new BarEntry(i, values[i]));
    }

    BarDataSet dataSet = new BarDataSet(entries, "Steps");
    BarData barData = new BarData(dataSet);


    barChart.getDescription().setText(" ");

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

    YAxis yAxisLeft = barChart.getAxisLeft();
    YAxis yAxisRight = barChart.getAxisRight();
    yAxisLeft.setTextSize(12f);
    yAxisRight.setTextSize(12f);

    xAxis.setTextSize(12f);

    dataSet.setValueTextSize(12f);

    yAxisLeft.setAxisLineColor(Color.TRANSPARENT);
    yAxisLeft.setAxisLineWidth(0f);
    yAxisLeft.setGridColor(Color.TRANSPARENT);

    yAxisRight.setEnabled(false); // Disable the right-side axis

    xAxis.setAxisLineColor(Color.TRANSPARENT);
    xAxis.setAxisLineWidth(0f);
    xAxis.setGridColor(Color.TRANSPARENT);

    barChart.setData(barData);
    barChart.invalidate();
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
//                stopService(v);
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
    private void bringStepsWeek(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Set to the first day of the week
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Set to the last day of the week
        Date endDate = calendar.getTime();

//        final int[] stepsPerDay = new int[7]; // Assuming you have an array to store the steps count per day

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference stepsCollectionRef = db.collection("steps");
                Query query = stepsCollectionRef
                        .whereGreaterThanOrEqualTo("date", formatDate(startDate))
                        .whereLessThanOrEqualTo("date", formatDate(endDate));

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String documentDate = document.getString("date");
                                if (documentDate != null) {
                                    int dayOfWeek = getDayOfWeekFromDate(documentDate);
                                    int steps = 0;

                                    if (document.contains("steps_count")) {
                                        steps = document.getLong("steps_count").intValue(); // Assuming "steps_count" is a field in your document
                                    } else {
                                        Log.d("mylog", "Missing steps_count field in document: " + document.getId());
                                    }

                                    if (dayOfWeek >= Calendar.SUNDAY && dayOfWeek <= Calendar.SATURDAY) {
                                        stepsPerDay[dayOfWeek - 1] += steps; // Increment steps for the corresponding day
                                    } else {
                                        Log.d("mylog", "Invalid dayOfWeek value for document: " + document.getId());
                                    }
                                } else {
                                    Log.d("mylog", "Missing date field in document: " + document.getId());
                                }
                            }

                            Log.d("mylog", "arrayDay: " + Arrays.toString(stepsPerDay));
                            // Perform any UI updates or further processing using the stepsPerDay array
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    init_steps(day,stepsPerDay);
                                    updateProgressBar();
                                    createBarChart(stepsPerDay);
                                }
                            });
                        } else {
                            Log.e("Firestore", "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });

        thread.start();

    }

    // Utility method to format Date object as "yyyy-MM-dd" string
    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return dateFormat.format(date);
    }

    // Utility method to get the day of the week (1-7) from a "yyyy-MM-dd" formatted date string
    private int getDayOfWeekFromDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = dateFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            return calendar.get(Calendar.DAY_OF_WEEK);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1; // Invalid day
    }

    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int savedHour = sharedPreferences.getInt("hour", 0); // 0 is the default value if the key is not found
        int savedMinute = sharedPreferences.getInt("minute", 0);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        Log.d("mylog","emuHour"+ hour);
        Log.d("mylog","SetHour"+ savedHour);
        Log.d("mylog","emuMin"+ minute);
        Log.d("mylog","SetMin"+ savedMinute);
        NotificationReceiver.setNotification(getApplicationContext(),savedHour,savedMinute);
    }

}










