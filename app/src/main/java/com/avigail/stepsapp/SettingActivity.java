package com.avigail.stepsapp;


import android.app.Activity;
import android.os.Bundle;


import android.content.Context;
import android.content.SharedPreferences;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class SettingActivity extends Activity {
    EditText txtMinSteps,txtName;
    Button btn1;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Name = "nameKey";
    public static final String MinSteps = "minStepsKey";

    private static final String ALARM_HOUR_KEY = "hour";
    private static final String ALARM_MINUTE_KEY = "minute";

    SharedPreferences sharedpreferences;
    TimePicker timerPicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        txtName= findViewById(R.id.txtName);
        txtMinSteps= findViewById(R.id.txtMinSteps);

        timerPicker = findViewById(R.id.timerPicker);


        btn1=findViewById(R.id.button);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String name = sharedpreferences.getString(Name, "");
        txtName.setText(name);
        String min_steps = sharedpreferences.getString(MinSteps, "");
        txtMinSteps.setText(min_steps);
        int savedHour = sharedpreferences.getInt(ALARM_HOUR_KEY, 0);
        int savedMinute = sharedpreferences.getInt(ALARM_MINUTE_KEY, 0);
        timerPicker.setCurrentHour(savedHour);
        timerPicker.setCurrentMinute(savedMinute);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name  = txtName.getText().toString();
                String minSteps = txtMinSteps.getText().toString();

                int hour = timerPicker.getCurrentHour();
                int minute = timerPicker.getCurrentMinute();
                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString(Name, name);
                editor.putString(MinSteps, minSteps);
                editor.putInt(ALARM_HOUR_KEY, hour);
                editor.putInt(ALARM_MINUTE_KEY, minute);
                editor.commit();

                Toast.makeText(SettingActivity.this,"Save",Toast.LENGTH_LONG).show();
            }
        });
    }

}