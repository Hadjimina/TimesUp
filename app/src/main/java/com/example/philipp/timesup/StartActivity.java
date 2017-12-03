package com.example.philipp.timesup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*
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

        create = findViewById(R.id.button_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //initialize connection
                NetworkHelper.handler = new SocketHandler();
                setCallbackActivity(StartActivity.this);

                //change to createGame class
                intent = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(intent);
            }
        });

        join = findViewById(R.id.button_join);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void callback(DecodeMessage message) {

    }
}
