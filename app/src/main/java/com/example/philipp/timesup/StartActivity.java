package com.example.philipp.timesup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/*
Activity to choose between creating or joining a game.
First Activity
Comes before JoinActivity or CreateActivity
Contains two buttons Join and Create
 */

public class StartActivity extends AppCompatActivity {

    Button create, join;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        create = (Button) findViewById(R.id.button_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(intent);
            }
        });

        join = (Button) findViewById(R.id.button_join);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        });

    }
}
