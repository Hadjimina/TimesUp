package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.*;


/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity which lets user join a game.
 Comes after StartActivity (join button)
 Comes before WordsActivity
 Lets user input a game code and name, lets them choose team afterwards
 */

public class JoinActivity extends ServerIOActivity {

    TextView team;
    EditText codeEdit, nameEdit;
    Button joinGame, go;
    RadioButton teamA, teamB;
    String gameCode, username;
    int gameId, clientId;
    Toast toast;
    EncodeMessage message, sendMessage;
    Intent intent;
    SharedPreferences.Editor editor;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //create texteditfields for code and username
        codeEdit = findViewById(R.id.game_code_edit);
        nameEdit = findViewById(R.id.enter_name_edit);

        joinGame = findViewById(R.id.button_join);

        //initialise server connection
        setCallbackActivity(this);

        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameCode = codeEdit.getText().toString();
                username = nameEdit.getText().toString();


                //add to shared Preferences
                /*prefs = getSharedPreferences(MYPREFS, MODE_PRIVATE);
                editor = prefs.edit();
                editor.putString("username", username);
                editor.apply();*/

                //find out if game code was set
                if (gameCode == null || gameCode.equals("")) {
                    toast = Toast.makeText(getApplicationContext(), "please enter a code to join the game", Toast.LENGTH_LONG);
                    toast.show();

                // if game code was set, parse it and send message to server and create intent
                } else {
                    GAMEID = Integer.parseInt(gameCode);
                    //find out if username was set
                    if (username == null || username.equals("")) {
                        toast = Toast.makeText(getApplicationContext(), "please enter a name to join the game", Toast.LENGTH_LONG);
                        toast.show();
                     //if usercname was set, proceed
                    } else {
                        USERNAME = username;

                        //send message to server
                        message = new EncodeMessage(GAMEID, USERNAME);
                        sendMessage(message);

                        intent = new Intent(getApplicationContext(), WordsActivity.class);
                    }
                }
            }
        });
    }

    @Override
    public void callback(DecodeMessage message) {
        Log.i("callback","joinactivity");
        String teamName1, teamName2;

        //initialize shared preferences
        /*prefs = getSharedPreferences(MYPREFS, MODE_PRIVATE);
        editor = prefs.edit();*/

        // if right return message from server and request type was join, start show which teams you can join
        if(message.getReturnType().equals(ACK) && message.getRequestType().equals(JOIN)){

            //set button to invisible
            joinGame.setVisibility(View.GONE);

            teamName1 = message.getString("teamName1");
            teamName2 = message.getString("teamName2");
            int teamId1 = message.getInt("teamId1");
            int teamId2 = message.getInt("teamId2");
            clientId = message.getClientId();
            CLIENTID = clientId;
            TEAMNAME1 = teamName1;
            TEAMNAME2 = teamName2;
            TEAMID1 = teamId1;
            TEAMID2 = teamId2;

            Log.d("TAG-JOIN", "client id is: " + clientId);

            //add to shared preferences
            /*editor.putString("teamName1", teamName1);
            editor.putString("teamName2", teamName2);
            editor.putInt("teamId1", teamId1);
            editor.putInt("teamId2", teamId2);
            editor.putInt("clientId", clientId);
            editor.apply();*/

            //make radioButtons to join a team visible
            teamA = findViewById(R.id.team_a1);
            teamB = findViewById(R.id.team_b1);

            if (teamName1 != null) {
                teamA.setText(TEAMNAME1);
            }
            if (teamName2 != null) {
                teamB.setText(TEAMNAME2);
            }

            teamA.setVisibility(View.VISIBLE);
            teamB.setVisibility(View.VISIBLE);

            //make go button visible and implement its function
            go = findViewById(R.id.button_go);
            go.setVisibility(View.VISIBLE);
            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (teamA.isChecked() || teamB.isChecked()) {
                        if(teamA.isChecked()){
                            BELONGSTOTEAM = TEAMID1;
                        }
                        else {
                            BELONGSTOTEAM = TEAMID2;
                        }

                        //Send message to server
                        sendMessage = new EncodeMessage(GAMEID, CLIENTID, BELONGSTOTEAM);
                        sendMessage(sendMessage);

                    } else {
                        toast = Toast.makeText(getApplicationContext(), "please select a team", Toast.LENGTH_LONG);
                        toast.show();
                    }

                }
            });

        }
        //game code doesn't exist, show error and set joingame button to visible again
        else if(message.getReturnType().equals(ERROR) && message.getRequestType().equals(JOIN)){
            //now implemented in WebSocket
            //toast = Toast.makeText(getApplicationContext(), "error with joining a team", Toast.LENGTH_LONG);
            //toast.show();
        }
        // if right return message from server and request type was teamjoin, proceed to next activity
        else if(message.getReturnType().equals(ACK) && message.getRequestType().equals(TEAMJOIN)){

            boolean hasStarted;
            int startTime, timePerRound, wordsPerPerson;

            //retrieve values from message and put them into shared preferences
            hasStarted = message.getBoolean("hasStarted");
            startTime = message.getInt("startTime");
            timePerRound = message.getInt("timePerRound");
            wordsPerPerson = message.getInt("wordsPerPerson");
            /*editor.putInt("wordsPerPerson", wordsPerPerson);
            editor.apply();*/
            WORDSPERPERSON = wordsPerPerson;
            TIMEPERROUND = timePerRound;

            //check if game has started or not
            if (!hasStarted) {
                //create intent and start wordsActivity
                intent = new Intent(getApplicationContext(), WordsActivity.class);
                startActivity(intent);

            } else {
                intent = new Intent(getApplicationContext(), GameActivity.class);
                intent.putExtra("startTime", startTime);
                startActivity(intent);
            }

        }
        //try to send message again and show error
        else if(message.getReturnType().equals(ERROR) && message.getRequestType().equals(TEAMJOIN)){
            //now implemented in Websocket
            //toast = Toast.makeText(getApplicationContext(), "error with joining a team", Toast.LENGTH_LONG);
            //toast.show();
            //sendMessage(sendMessage);
        }
        //show error and go back to start
        else{
            //now implemented in websocket
            //toast = Toast.makeText(getApplicationContext(), "pretty much everything went wrong with contacting the server", Toast.LENGTH_LONG);
            //toast.show();
        }

    }


}