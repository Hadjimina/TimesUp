package com.example.philipp.timesup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

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

    EditText codeEdit, nameEdit;
    Button joinGame, go;
    RadioButton teamA, teamB;
    String gameCode, username;
    int gameId;
    Toast toast;
    EncodeMessage message;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //create texteditfields for code and username
        codeEdit = findViewById(R.id.game_code_edit);
        nameEdit = findViewById(R.id.enter_name_edit);

        joinGame = findViewById(R.id.button_join);
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameCode = codeEdit.getText().toString();
                username = nameEdit.getText().toString();

                //set button to invisible
                joinGame.setVisibility(View.GONE);

                //find out if game code was set
                if (gameCode == null || gameCode.equals("")) {
                    toast = Toast.makeText(getApplicationContext(), "please enter a code to join the game", Toast.LENGTH_LONG);
                    toast.show();
                    return;

                // if game code was set, parse it and send message to server and create intent
                } else {
                    gameId = Integer.parseInt(gameCode);
                    //find out if username was set
                    if (username == null || username.equals("")) {
                        toast = Toast.makeText(getApplicationContext(), "please enter a name to join the game", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                     //if username was set, proceed
                    } else {
                        //TODO add client ID
                        //message = new EncodeMessage(gameId, clientId, username);

                        intent = new Intent(getApplicationContext(), WordsActivity.class);



                        //ALL BELOW BELONGS TO CALLBACK FUNCTION
                        //TODO nachher useneh
                        //make radioButtons to join a team visible
                        teamA = findViewById(R.id.team_a1);
                        teamB = findViewById(R.id.team_b1);

                        teamA.setVisibility(View.VISIBLE);
                        teamB.setVisibility(View.VISIBLE);


                        //dummy values, will get deleted
                        String teamName1 = "Hellö1";
                        String teamName2 = "Hellö2";

                        if (teamName1 != null) {
                            teamA.setText(teamName1);
                        }

                        if (teamName2 != null) {
                            teamB.setText(teamName2);
                        }


                        //FROM HERE ON COPY CODE to end of first case of callback function
                        go = findViewById(R.id.button_go);
                        go.setVisibility(View.VISIBLE);
                        go.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int teamId;
                                if (teamA.isChecked() || teamB.isChecked()) {
                                    //TODO check here what to do with questions
                                    if(teamA.isChecked()){
                                        teamId = 1;
                                    }
                                    else{
                                        teamId = 0;
                                    }
                                    intent = new Intent(getApplicationContext(), WordsActivity.class);

                                    //TODO put in shared preferences object
                                    intent.putExtra("gameId", gameId);
                                } else {
                                    toast = Toast.makeText(getApplicationContext(), "please select a team", Toast.LENGTH_LONG);
                                    return;
                                }

                                //TODO: find out about clientId and why teamId is an int
                                //TODO create message for server
                                //message = new EncodeMessage(gameId, clientId, teamId)
                                //TODO take this out
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void callback(DecodeMessage message) {

        int gameId;
        String teamName1, teamName2;

        // if right return message from server and request type was join, start show which teams you can join
        if(message.getReturnType() == "ACK" && message.getRequestType() == "Join"){
            gameId = message.getGameId();
            teamName1 = message.getString("teamName1");
            teamName2 = message.getString("teamName2");
            intent.putExtra("gameId", gameId);

            //make radioButtons to join a team visible
            teamA = findViewById(R.id.team_a1);
            teamB = findViewById(R.id.team_b1);

            if (teamName1 != null) {
                teamA.setText(teamName1);
            }

            if (teamName2 != null) {
                teamB.setText(teamName2);
            }

            teamA.setVisibility(View.VISIBLE);
            teamB.setVisibility(View.VISIBLE);


            //TODO here belongs text from above

        }
        //game code doesn't exist, show error and set joingame button to visible again
        else if(message.getReturnType() == "error" && message.getRequestType() == "Join"){
            //TODO
        }
        // if right return message from server and request type was teamjoin, proceed to next activity
        else if(message.getReturnType() == "ACK" && message.getRequestType() == "teamJoin"){

            boolean hasStarted;
            int startTime, timePerRound;

            hasStarted = message.getBoolean("hasStarted");
            startTime = message.getInt("startTime");
            timePerRound = message.getInt("timePerRound");
            intent.putExtra("hasStarted", hasStarted);
            intent.putExtra("startTime", startTime);
            intent.putExtra("timePerRound", timePerRound);
            startActivity(intent);
        }
        //try to send message again and show error
        else if(message.getReturnType() == "error" && message.getRequestType() == "teamJoin"){
            //TODO
        }
        //show error and go back to start
        else{
            //TODO
        }

    }
}