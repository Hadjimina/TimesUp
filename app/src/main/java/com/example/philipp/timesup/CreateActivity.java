package com.example.philipp.timesup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.ACK;
import static com.example.philipp.timesup.NetworkHelper.CLIENTID;
import static com.example.philipp.timesup.NetworkHelper.GAMEID;
import static com.example.philipp.timesup.NetworkHelper.NEWGAME;
import static com.example.philipp.timesup.NetworkHelper.ROUNDS;
import static com.example.philipp.timesup.NetworkHelper.TEAMNAME1;
import static com.example.philipp.timesup.NetworkHelper.TEAMNAME2;
import static com.example.philipp.timesup.NetworkHelper.TIMEPERROUND;
import static com.example.philipp.timesup.NetworkHelper.USERNAME;
import static com.example.philipp.timesup.NetworkHelper.WORDSPERPERSON;

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

    boolean[] rounds = {true,true,true,true,true};
    CheckBox explain, pantomime, oneWord, freeze, sounds;
    EditText team1Edit, team2Edit, usernameEdit, timeEdit, wordsEdit;
    String teamName1, teamName2, username, time, words;
    int timePerRound, wordsPerPerson;
    Button cancel, finish;
    Intent intent;
    Toast toast;
    EncodeMessage sendMessage;
    ImageButton infoTimePerPerson, infoRounds, infoWords;
    Boolean DEBUG;
    AlertDialog.Builder alertDialogBuilder;
    AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);


        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        DEBUG = false;

        setCallbackActivity(this);

        //time picker
        timeEdit = findViewById(R.id.time);

        //set on click listeners for checkboxes about rounds
        explain = findViewById(R.id.explain);
        explain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[0] = !rounds[0];
            }
        });

        pantomime =  findViewById(R.id.panto);
        pantomime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[1] = !rounds[1];
            }
        });

        oneWord = findViewById(R.id.one_word);
        oneWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[2] = !rounds[2];
            }
        });

        freeze = findViewById(R.id.freeze);
        freeze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[3] = !rounds[3];
            }
        });

        sounds = findViewById(R.id.sound);
        sounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rounds[4] = !rounds[4];
            }
        });

        //EditText fields
        team1Edit = findViewById(R.id.team_a);
        team2Edit = findViewById(R.id.team_b);
        usernameEdit = findViewById(R.id.username);

        //Words per Person
        wordsEdit = findViewById(R.id.words_number);

        //alertDialog for infobuttons
        alertDialogBuilder = new AlertDialog.Builder(this);


        infoTimePerPerson = findViewById(R.id.info_time_round);
        infoTimePerPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogBuilder.setTitle("Time per Person");
                alertDialogBuilder.setMessage("Time which each person has per round to do explain the words. Recommended are 30 to 120 seconds")
                        .setCancelable(true)
                        .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
                alert = alertDialogBuilder.create();

                alert.show();
            }
        });

        infoRounds = findViewById(R.id.info_rounds);
        infoRounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogBuilder.setTitle("Rounds");
                alertDialogBuilder.setMessage("The rounds through which the game will go through. Recommendation: choose at least EXPLAIN and PANTOMIME, as the game builds up on them")
                        .setCancelable(true)
                        .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
                alert = alertDialogBuilder.create();

                alert.show();
            }
        });
        infoWords = findViewById(R.id.info_words);
        infoWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogBuilder.setTitle("Words per Person");
                alertDialogBuilder.setMessage("The number of words each player enters in the game. Choose a number according to how much time you have to play the entire game through. Recommendation: 4 - 6")
                        .setCancelable(true)
                        .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
                alert = alertDialogBuilder.create();

                alert.show();
            }
        });

        //cancel Button goes back to StartActivity
        cancel = findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intent);
            }
        });

        //finish button sends information to server and waits for ack
        finish = findViewById(R.id.button_finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), JoinCodeActivity.class);

                //set time, teamNames, no of words etc
                time = timeEdit.getText().toString();
                teamName1 = team1Edit.getText().toString();
                teamName2 = team2Edit.getText().toString();
                words = wordsEdit.getText().toString();
                username = usernameEdit.getText().toString();

                if(time.equals("1")) {
                    //activate DEBUG
                    teamName1 = "Team A";
                    teamName2 = "Team B";
                    username = "Tyler, the Creator";
                    timePerRound = 20;
                    wordsPerPerson = 2;
                } else {

                    if (time == null || time.equals("") || time.equals("0")) {
                        toast = Toast.makeText(getApplicationContext(), "Please enter a time per round", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    } else {
                        timePerRound = Integer.parseInt(time);
                    }

                    if (teamName1 == null || teamName1.equals("")) {
                        toast = Toast.makeText(getApplicationContext(), "Please enter Name for Team A", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }

                    if (teamName2 == null || teamName2.equals("")) {
                        toast = Toast.makeText(getApplicationContext(), "Please enter Name for Team B", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }

                    if (words == null || words.equals("")) {
                        toast = Toast.makeText(getApplicationContext(), "Please enter how many words per person", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    } else {
                        wordsPerPerson = Integer.parseInt(words);
                    }

                    if (username == null || username.equals("")) {
                        toast = Toast.makeText(getApplicationContext(), "Please enter a username", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }
                }

                TEAMNAME1 = teamName1;
                TEAMNAME2 = teamName2;
                USERNAME = username;
                ROUNDS = rounds;
                TIMEPERROUND = timePerRound;
                WORDSPERPERSON = wordsPerPerson;

                Log.d("CREATE",  teamName1 + teamName2 + timePerRound + username + wordsPerPerson);

                //Send message to server
                sendMessage = new EncodeMessage(TEAMNAME1, TEAMNAME2, TIMEPERROUND, WORDSPERPERSON, USERNAME, ROUNDS);
                sendMessage(sendMessage);

            }
        });

    }

    @Override
    public void callback(DecodeMessage message) {
        int gameId, clientId;
        Log.i("callback","createActivity");

        // if right return message from server, start new Activity
        if(message.getReturnType().equals(ACK) && message.getRequestType().equals(NEWGAME)){

            gameId = message.getGameId();
            clientId = message.getClientId();
            Log.d("TAGmessage", "gameId: " + gameId);
            Log.d("TAGmessage", "clientId: " + clientId);
            //add retrieved information to sharedPreferences
            GAMEID = gameId;
            CLIENTID = clientId;

            startActivity(intent);
            Log.d("TAG-GAMEVALUES", "gameId, clientId, teamName1, teamName2, timePerRound, wordsPerPerson, username, rounds" +  " " + GAMEID + " " + CLIENTID + " " + TEAMNAME1 + " " + TEAMNAME2 + " " + TIMEPERROUND +  " " +  WORDSPERPERSON);
        }
    }
}