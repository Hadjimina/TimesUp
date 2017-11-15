package com.example.philipp.timesup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
Activity to create a new game.
Comes after StartActivity (create button)
Comes before JoinCodeActivity
Contains settings for new game (Time per round, rounds, team names)
 */

public class CreateActivity extends ServerIOActivity{

    //public EncodeMessage(boolean[] rounds, String teamName1, String teamName2, int timePerRound, String username, int wordsPerPerson){
    boolean[] rounds = {true,true,true,true,true};
    CheckBox explain, pantomime, oneWord, freeze, sounds;
    EditText team1Edit, team2Edit, usernameEdit, timeEdit, wordsEdit;
    String teamName1, teamName2, username;
    int timePerRound, wordsPerPerson;
    NumberPicker minutes, seconds, words;
    Button cancel, finish;
    Intent intent;
    Toast toast;
    EncodeMessage message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        //TODO: solve bug when pressing finish and nothing at all is filled in

        //timepicker
/*
        minutes = (NumberPicker) findViewById(R.id.minute_picker);

        seconds = (NumberPicker) findViewById(R.id.seconds_picker);*/

        timeEdit = (EditText) findViewById(R.id.time);

        //set on click listeners for checkboxes about rounds

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

        //EditText fields

        team1Edit = (EditText) findViewById(R.id.team_a);

        team2Edit = (EditText) findViewById(R.id.team_b);

        usernameEdit = (EditText) findViewById(R.id.username);

        //Words per Person

        //words = (NumberPicker) findViewById(R.id.number_picker);

        wordsEdit = (EditText) findViewById(R.id.words_number);

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

                //read teamnames and username
                teamName1 = team1Edit.getText().toString();
                teamName2 = team2Edit.getText().toString();
                username = usernameEdit.getText().toString();

                if (teamName1 == null) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter Name for Team A", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                if (teamName2 == null) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter Name for Team B", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                if (username == null) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter a username", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                timePerRound = Integer.parseInt(timeEdit.getText().toString());

                wordsPerPerson = Integer.parseInt(wordsEdit.getText().toString());

                if (timePerRound == 0) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter the time per round", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                if (wordsPerPerson == 0) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter a number of words per person", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                message = new EncodeMessage(rounds, teamName1, teamName2, timePerRound, username, wordsPerPerson);


                Log.d("CREATE", rounds.toString() + teamName1 + teamName2 + timePerRound + username + wordsPerPerson);

                intent.putExtra("teamName1", teamName1);
                intent.putExtra("teamName2", teamName2);

                // TODO fake gameId as long as callback not working
                int gameId = 1;
                intent.putExtra("gameId", gameId);
                //TODO do send message with helper here, remove startActivity
                startActivity(intent);


            }
        });

    }

    @Override
    public void callback(DecodeMessage message) {
        int gameId;

        //TODO was isch mit clientID
        // if right return message from server, start new Activity
        if(message.getReturnType() == "ACK" && message.getRequestType() == "new game"){
            gameId = message.getGameId();
            intent.putExtra("gameId", gameId);
            startActivity(intent);
        }
        //else try to send message to server again
        else {
            //send message again
        }


    }
}