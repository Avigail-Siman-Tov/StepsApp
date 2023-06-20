package com.avigail.stepsapp;

import static com.avigail.stepsapp.ForegroundService.stepCount;
import static com.avigail.stepsapp.TodayFragment.progressBar;
import static com.avigail.stepsapp.TodayFragment.stepCountTextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
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
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;
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
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {
    public static int TodaySteps;
    int[] stepsPerDay = new int[7]; // Array to store steps for each day of the week
    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_WEEK);
    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bringStepsWeek();
        //create tab of fragment of today and week
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("TODAY"));
        tabLayout.addTab(tabLayout.newTab().setText("WEEK"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final MyAdapter adapter = new MyAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (viewPager != null) {
                    viewPager.setCurrentItem(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselected event if needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselected event if needed
            }
        });
        send_notfitication();

    }

    //The method is start the forground servies
    public void startService(View v)
    {
        stepCount=0;
        startForegroundService(new Intent(this, ForegroundService.class));
        findViewById(R.id.btnStratID).setEnabled(false);
        findViewById(R.id.btnStopID).setEnabled(true);
        Toast.makeText(this, "Service Started!", Toast.LENGTH_LONG).show();
        Log.d("mylog", "Service Started!");
    }

    //The method is stop the forground servies
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

    //The function insert the steps count and date to firestore
    public void insertData_steps(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //create the date in date format
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
                                Log.d("mylog","Document created successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle any errors that occurred
                                Log.d("mylog","Document created failure");
                            }
                        });
            }
        });
        thread.start();
    }

    //The function init the today steps in textview and progressBar
    private void init_steps(int dayOfWeek , int[] stepsPerDay){
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                stepCountTextView.setText(""+ stepsPerDay[0]);
                TodaySteps = stepsPerDay[0];
                progressBar.setProgress(TodaySteps);
                break;
            case Calendar.MONDAY:
                stepCountTextView.setText(""+ stepsPerDay[1]);
                TodaySteps = stepsPerDay[1];
                progressBar.setProgress(TodaySteps);
                break;
            case Calendar.TUESDAY:
                stepCountTextView.setText(""+ stepsPerDay[2]);
                TodaySteps = stepsPerDay[2];
                progressBar.setProgress(TodaySteps);
                break;
            case Calendar.WEDNESDAY:
                stepCountTextView.setText(""+ stepsPerDay[3]);
                TodaySteps = stepsPerDay[3];
                progressBar.setProgress(TodaySteps);
                break;
            case Calendar.THURSDAY:
                stepCountTextView.setText(""+ stepsPerDay[4]);
                TodaySteps = stepsPerDay[4];
                progressBar.setProgress(TodaySteps);
                break;
            case Calendar.FRIDAY:
                stepCountTextView.setText(""+ stepsPerDay[5]);
                TodaySteps = stepsPerDay[5];
                progressBar.setProgress(TodaySteps);
                break;
            case Calendar.SATURDAY:
                stepCountTextView.setText(""+ stepsPerDay[6]);
                TodaySteps = stepsPerDay[6];
                progressBar.setProgress(TodaySteps);
                break;
        }
    }

    //The function receive array of steps of this week and create bar chart to this steps of week
    private void createBarChart(int[] values) {
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
        //set size of text
        yAxisLeft.setTextSize(12f);
        yAxisRight.setTextSize(12f);

        xAxis.setTextSize(12f);

        dataSet.setValueTextSize(12f);

        //set the axit color be transparent
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

    //The function passed on all the document in firestore and count the number of steps every day and enter it to array
    private void bringStepsWeek(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Set to the first day of the week
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Set to the last day of the week
        Date endDate = calendar.getTime();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference stepsCollectionRef = db.collection("steps");
                //the query of relevant documents
                Query query = stepsCollectionRef
                        .whereGreaterThanOrEqualTo("date", formatDate(startDate))
                        .whereLessThanOrEqualTo("date", formatDate(endDate));

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //this loop count the steps of every day than in the week
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
                            createBarChart(stepsPerDay); //create bar chart to steps week
                            init_steps(day,stepsPerDay); //update the day steps

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

    //The function Order the brodcast receiever to check whether to send notfitication according time and min steps
    private void send_notfitication(){
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int savedHour = sharedPreferences.getInt("hour", 8); // 0 is the default value if the key is not found
        int savedMinute = sharedPreferences.getInt("minute", 0);
        Calendar calendar = Calendar.getInstance();
        NotificationReceiver.setNotification(getApplicationContext(),savedHour,savedMinute);
    }

    @Override
    protected void onResume(){
        super.onResume();
        send_notfitication();
    }

    //Creating a three point menu
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

    //The function jumped alert dialog with a text
    private void aboutAlertDialog() {
        String strDeviceOS = "Android OS " + Build.VERSION.RELEASE + " API " + Build.VERSION.SDK_INT;

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("About App");
        dialog.setMessage("\nSteps mobile App!" + "\n\n" + strDeviceOS + "\n\n" + "By Avigail Siman Tov , 19.06.2023");

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();   // close this dialog
            }
        });
        dialog.show();
    }

    //the function intent to settingActivity
    private void IntentSettingActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    //The function jumped alert dialog of exit with a text and two button of yes and no
    private void exitAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.baseline_exit_to_app_24);
        dialog.setTitle("Exit App");
        dialog.setMessage("Are you sure ?");
        dialog.setCancelable(false);

        //if choose YES we stop servies , update steps and exit from app
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                insertData_steps();
                stopService(new Intent(MainActivity.this, ForegroundService.class));
                findViewById(R.id.btnStratID).setEnabled(true);
                findViewById(R.id.btnStopID).setEnabled(false);
                Toast.makeText(MainActivity.this, "Service Stoped!", Toast.LENGTH_LONG).show();
                // Inside your MainActivity class
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentByTag("TodayFragment");
                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                }
                finish();
                finish();   // destroy this activity
            }
        });
        //if you choose No we stay in app
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();   // close this dialog
            }
        });
        dialog.show();
    }

    //The function is check if the network available before you reach to firestore
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}










