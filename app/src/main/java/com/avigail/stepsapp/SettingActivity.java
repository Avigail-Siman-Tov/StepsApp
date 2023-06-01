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
    EditText txtWeight,txtEmail,txtMinSteps,txtName;
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

        txtName= findViewById(R.id.txtName);
        txtMinSteps= findViewById(R.id.txtMinSteps);
        txtEmail= findViewById(R.id.txtEmail);
        txtWeight= findViewById(R.id.txtWeight);

        btn1=findViewById(R.id.button);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String name = sharedpreferences.getString(Name, "");
        txtName.setText(name);
        String min_steps = sharedpreferences.getString(MinSteps, "");
        txtMinSteps.setText(min_steps);
        String email = sharedpreferences.getString(Email, "");
        txtEmail.setText(email);
        String weight = sharedpreferences.getString(Weight, "");
        txtWeight.setText(weight);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name  = txtName.getText().toString();
                String mail  = txtEmail.getText().toString();
                String minSteps = txtMinSteps.getText().toString();
                String weight = txtWeight.getText().toString();


                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString(Name, name);
                editor.putString(MinSteps, minSteps);
                editor.putString(Email, mail);
                editor.putString(Weight, weight);
                editor.commit();
                Toast.makeText(SettingActivity.this,"Save",Toast.LENGTH_LONG).show();
            }
        });
    }

}