package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.ACK;
import static com.example.philipp.timesup.NetworkHelper.GAMEID;
import static com.example.philipp.timesup.NetworkHelper.MYPREFS;
import static com.example.philipp.timesup.NetworkHelper.NEWGAME;

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
    String teamName1, teamName2, username;
    int timePerRound, wordsPerPerson;
    Button cancel, finish;
    Intent intent;
    Toast toast;
    EncodeMessage sendMessage;
    SharedPreferences.Editor editor;
    ImageButton infoTimePerPerson, infoRounds, infoWords;
    //PopupWindow popupWindow;
    //TextView popupContent;
    Boolean DEBUG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        DEBUG = true;

        setCallbackActivity(this);
        //TODO: solve bug when pressing finish and nothing at all is filled in

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

        //initialise infobuttons
        infoTimePerPerson = findViewById(R.id.info_time_round);
        infoTimePerPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*popupContent = findViewById(R.id.popup_content);
                popupContent.setText("Recommended: 30 to 120 seconds");
                popupWindow = new PopupWindow(60, 60);
                popupWindow.showAsDropDown(infoTimePerPerson, 60, 0);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                popupWindow.setContentView(popupContent);*/
                toast = Toast.makeText(getApplicationContext(), "Time which each person has per round to do explain the words. Recommended are 30 to 120 seconds", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        infoRounds = findViewById(R.id.info_rounds);
        infoRounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast = Toast.makeText(getApplicationContext(), "The rounds through which the game will go through. Recommendation: choose at least EXPLAIN and PANTOMIME, as the game builds up on them", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        infoWords = findViewById(R.id.info_words);
        infoWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast = Toast.makeText(getApplicationContext(), "The number of words each player enters in the game. Choose a number according to how much time you have. Recommendation: 4 - 6", Toast.LENGTH_LONG);
                toast.show();
            }
        });


        //initialize shared preferences object
        editor = getSharedPreferences(MYPREFS, MODE_PRIVATE).edit();


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

                if(DEBUG) {
                    teamName1 = "Team A";
                    teamName2 = "Team B";
                    username = "Tyler, the Creator";
                    timePerRound = 60;
                    editor.putInt("timePerRound", timePerRound);
                }

                //read teamnames and username and add them to shared preferences
                if (!DEBUG) {
                    teamName1 = team1Edit.getText().toString();
                    teamName2 = team2Edit.getText().toString();
                    username = usernameEdit.getText().toString();
                }
                editor.putString("teamName1", teamName1);
                editor.putString("teamName2", teamName2);
                editor.putString("username", username);


                //add rounds to shared preferences
                editor.putString("rounds", rounds.toString());
                Log.d("Rounds", rounds.toString());


                if (teamName1 == null && !DEBUG) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter Name for Team A", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                if (teamName2 == null && !DEBUG) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter Name for Team B", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                if (username == null && !DEBUG) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter a username", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                if (timeEdit != null && !timeEdit.equals("") && !DEBUG) {
                    //parse time per round and put into shared preferences
                    timePerRound = Integer.parseInt(timeEdit.getText().toString());
                    editor.putInt("timePerRound", timePerRound);
                }


                //parse words per person and put into shared preferences
                if(!DEBUG) {
                    wordsPerPerson = Integer.parseInt(wordsEdit.getText().toString());
                } else {
                    wordsPerPerson = 3;
                }
                editor.putInt("wordsPerPerson", wordsPerPerson);

                if (timePerRound == 0 || timeEdit == null && timeEdit.equals("") && !DEBUG ) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter the time per round", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                if (wordsPerPerson == 0 && !DEBUG) {
                    toast = Toast.makeText(getApplicationContext(), "Please enter a number of words per person", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                Log.d("CREATE", rounds.toString() + teamName1 + teamName2 + timePerRound + username + wordsPerPerson);

                //apply shared preferences
                editor.apply();

                //Send message to server
                sendMessage = new EncodeMessage(teamName1, teamName2, timePerRound, wordsPerPerson, username, rounds);
                sendMessage(sendMessage);

            }
        });

    }

    @Override
    public void callback(DecodeMessage message) {
        int gameId, clientId;
        Log.i("callback","creatActivity");

        // if right return message from server, start new Activity
        if(message.getReturnType().equals(ACK) && message.getRequestType().equals(NEWGAME)){

            gameId = message.getGameId();
            clientId = message.getClientId();
            Log.d("TAGmessage", "gameId: " + gameId);
            Log.d("TAGmessage", "clientId: " + clientId);
            //add retrieved information to sharedPreferences
            GAMEID = gameId;
            editor.putInt("clientId", clientId);

            editor.apply();

            startActivity(intent);
        }
        //else try to send message to server again
        else {
            //now implemented in websocket
            //sendMessage(sendMessage);
            //toast = Toast.makeText(getApplicationContext(), "error contacting server, trying to send message again", Toast.LENGTH_LONG);
            //toast.show();
        }


    }
    @Override
    public void onBackPressed() {
        toast = Toast.makeText(getApplicationContext(), "Going back to start activity", Toast.LENGTH_LONG);
        toast.show();
        intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
    }
}