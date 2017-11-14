package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
Activity to create a new game.
Comes after StartActivity (create button)
Comes before JoinCodeActivity
Contains settings for new game (Time per round, rounds, team names)
 */

public class CreateActivity extends AppCompatActivity {

    //public EncodeMessage(boolean[] rounds, String teamName1, String teamName2, int timePerRound, String username, int wordsPerPerson){
    boolean[] rounds = {true,true,true,true,true};
    CheckBox explain, pantomime, oneWord, freeze, sounds;
    EditText team1, team2, uName;
    String teamName1, teamName2, username;
    int timePerRound, wordsPerPerson;
    Button cancel, finish;
    Intent intent;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        explain = (CheckBox)  findViewById(R.id.explain);
        explain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[0] = !rounds[0];
            }
        });

        pantomime = (CheckBox)  findViewById(R.id.panto);
        pantomime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[1] = !rounds[1];
            }
        });

        oneWord = (CheckBox)  findViewById(R.id.one_word);
        oneWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[2] = !rounds[2];
            }
        });

        freeze = (CheckBox)  findViewById(R.id.freeze);
        freeze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[3] = !rounds[3];
            }
        });

        sounds = (CheckBox)  findViewById(R.id.sound);
        sounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[4] = !rounds[4];
            }
        });

        //cancel Button goes back to  StartActivity
        cancel = (Button) findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intent);
            }
        });

        //finish button sends information to server and waits for ack
        finish = (Button) findViewById(R.id.button_finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), JoinCodeActivity.class);
                //TODO send settings to server
                //TODO wait for callback
                startActivity(intent);
            }
        });


        //create shared preference object
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);




    }
}