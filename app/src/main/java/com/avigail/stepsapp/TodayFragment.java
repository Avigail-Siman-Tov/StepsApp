package com.avigail.stepsapp;


import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.Calendar;

public class TodayFragment extends Fragment {
    private TextView  txtHello,txtGoal;
    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_WEEK);
    int min_steps;
    public static ProgressBar progressBar;
    static TextView stepCountTextView ;

    public TodayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);


        ImageView gifImageView = view.findViewById(R.id.gifImageView);
        String gifUrl = "https://www.linkpicture.com/q/giphy-1_1.gif";
        int desiredWidth = 380; // Specify your desired width
        int desiredHeight = 400; // Specify your desired height

        //to display the gif on steps
        Glide.with(this)
                .asGif()
                .load(gifUrl)
                .override(desiredWidth, desiredHeight)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(gifImageView);

        progressBar = view.findViewById(R.id.progress_bar);
        stepCountTextView = view.findViewById(R.id.stepCountTextView);
        txtHello = view.findViewById(R.id.txtHello);
        txtGoal = view.findViewById(R.id.txtMinSteps);
        data_shared_preference();
        checkIfMyFGServiceRunning(view);

        return view;
    }

    //the function bring the setting data from sharedpreference
    private void data_shared_preference(){
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("nameKey", " ");
        String minSteps = sharedPreferences.getString("minStepsKey", "30");
        txtHello.setText("Hello " + name);
        txtGoal.setText("Goal: "+ minSteps);
        min_steps = Integer.parseInt(minSteps);
        progressBar.setMax(min_steps);
    }
    //this function check if the forgroun servies is running
    private void checkIfMyFGServiceRunning(View view) {
        boolean isRunning = false;
        ActivityManager manager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName())) {
                isRunning = true;
                break;
            }
        }

        if (isRunning) {
            view.findViewById(R.id.btnStratID).setEnabled(false);
            view.findViewById(R.id.btnStopID).setEnabled(true);
        } else {
            view.findViewById(R.id.btnStratID).setEnabled(true);
            view.findViewById(R.id.btnStopID).setEnabled(false);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        data_shared_preference();
    }
}

