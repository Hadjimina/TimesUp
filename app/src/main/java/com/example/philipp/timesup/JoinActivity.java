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

        codeEdit = (EditText) findViewById(R.id.game_code_edit);

        nameEdit = (EditText) findViewById(R.id.enter_name_edit);

        joinGame = (Button) findViewById(R.id.button_join);
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameCode = codeEdit.getText().toString();
                username = nameEdit.getText().toString();


                if (gameCode == null || gameCode.equals("")) {
                    toast = Toast.makeText(getApplicationContext(), "please enter a code to join the game", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                } else {
                    gameId = Integer.parseInt(gameCode);

                    if (username == null || username.equals("")) {
                        toast = Toast.makeText(getApplicationContext(), "please enter a name to join the game", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    } else {
                        //TODO add client ID
                        //message = new EncodeMessage(gameId, clientId, username);
                        Log.d("JOIN", "gameId is " + gameId + "/ username is " + username);
                        intent = new Intent(getApplicationContext(), WordsActivity.class);
                        //TODO nachher useneh
                        //make radioButtons to join a team visible
                        teamA = (RadioButton) findViewById(R.id.team_a);
                        teamB = (RadioButton) findViewById(R.id.team_b);

                        go = (Button) findViewById(R.id.button_go);

                        String teamName1 = "team A";
                        String teamName2 = "team B";

                        if (teamName1 != null) {
                            teamA.setText(teamName1);
                        }

                        if (teamName2 != null) {
                            teamB.setText(teamName2);
                        }

                        teamA.setVisibility(View.VISIBLE);
                        teamB.setVisibility(View.VISIBLE);

                        joinGame.setVisibility(View.INVISIBLE);

                        go.setVisibility(View.VISIBLE);

                    }
                }
            }
        });
    }

    @Override
    public void callback(DecodeMessage message) {

        int gameId;
        String teamName1, teamName2;

        // if right return message from server, start new Activity
        if(message.getReturnType() == "ACK" && message.getRequestType() == "Join"){
            gameId = message.getGameId();
            teamName1 = message.getString("teamName1");
            teamName2 = message.getString("teamName2");
            intent.putExtra("gameId", gameId);

            //make radioButtons to join a team visible
            teamA = (RadioButton) findViewById(R.id.team_a);
            teamB = (RadioButton) findViewById(R.id.team_b);

            if (teamName1 != null) {
                teamA.setText(teamName1);
            }

            if (teamName2 != null) {
                teamB.setText(teamName2);
            }

            teamA.setVisibility(View.VISIBLE);
            teamB.setVisibility(View.VISIBLE);

            joinGame.setVisibility(View.INVISIBLE);

            go.setVisibility(View.VISIBLE);

            startActivity(intent);
        }
        //else try to send message to server again
        else {
            //send message again
        }
    }
}