package com.avigail.stepsapp;


import android.app.Activity;
import android.os.Bundle;


import android.content.Context;
import android.content.SharedPreferences;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingActivity extends Activity {
    EditText edt1,edt2,edt3,edt4;
    Button btn1;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Name = "nameKey";
    public static final String MinSteps = "minStepsKey";
    public static final String Email = "emailKey";

    public static final String Weight = "weightKey";

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        edt1= findViewById(R.id.editText);
        edt2= findViewById(R.id.editText2);
        edt3= findViewById(R.id.editText3);
        edt4= findViewById(R.id.editTextNumber);

        btn1=findViewById(R.id.button);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String name = sharedpreferences.getString(Name, "");
        edt1.setText(name);
        String min_steps = sharedpreferences.getString(MinSteps, "");
        edt2.setText(min_steps);
        String email = sharedpreferences.getString(Email, "");
        edt3.setText(email);
        String weight = sharedpreferences.getString(Weight, "");
        edt4.setText(weight);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n  = edt1.getText().toString();
                String ph  = edt2.getText().toString();
                String e  = edt3.getText().toString();
                String w = edt4.getText().toString();


                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString(Name, n);
                editor.putString(MinSteps, ph);
                editor.putString(Email, e);
                editor.putString(Weight, w);
                editor.commit();
                Toast.makeText(SettingActivity.this,"Save",Toast.LENGTH_LONG).show();
            }
        });
    }

}