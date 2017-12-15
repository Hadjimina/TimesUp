package com.example.philipp.timesup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
Activity to choose between creating or joining a game.
First Activity
Comes before JoinActivity or CreateActivity
Contains two buttons Join and Create
 */


//dummy extends just for testing
public class StartActivity extends ServerIOActivity {

    Button create, join;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.d("START", "start activity running");



        getSupportActionBar().hide();

        Intent i = getIntent();
        boolean isRestart = i.getBooleanExtra("isRestart", false);
        if(isRestart){
            ProgressDialog prog = new ProgressDialog(this);
            prog.setMessage("Please wait while you get reconnected");
            prog.setCancelable(false);
            prog.show();
        }

        create = findViewById(R.id.button_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                initAndStartSocket();
                Log.d("START", "create button clicked");

                //change to createGame class
                intent = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(intent);
            }
        });

        join = findViewById(R.id.button_join);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                initAndStartSocket();
                Log.d("START", "create button clicked");

                intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void callback(DecodeMessage message) {
        Log.i("callback","start");
    }

    public void initAndStartSocket(){
        if(NetworkHelper.handler == null){

            Log.i("Websocket","should now connect");
            //initialize connection
            //Socket started as soon as SocketHandler object is created
            NetworkHelper.handler = new SocketHandler();
            setCallbackActivity(StartActivity.this);
        }

        NetworkHelper.handler.reconnect();
    }
}
