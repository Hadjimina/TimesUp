package com.example.philipp.timesup;

import android.support.v7.app.AppCompatActivity;


abstract public class ServerIOActivity extends AppCompatActivity{

    abstract public void callback(DecodeMessage message);
}
