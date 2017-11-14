package com.example.philipp.timesup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/*
Activity to choose between creating or joining a game.
First Activity
Comes before JoinActivity or CreateActivity
Contains two buttons Join and Create
 */

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

    }
}
