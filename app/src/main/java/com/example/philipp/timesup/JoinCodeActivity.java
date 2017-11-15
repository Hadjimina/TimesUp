package com.example.philipp.timesup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity which displays code of game for others to join.
 Comes after CreateActivity
 Comes before WordsActivity
 Displays game code and lets game creator choose team
 */

public class JoinCodeActivity extends ServerIOActivity {
    TextView code;
    Intent intent;
    int gameId;
    String teamName1, teamName2;
    RadioButton teamA, teamB;
    Button go;
    EncodeMessage message;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_code);
        intent = getIntent();
        gameId = intent.getIntExtra("gameId", 0);
        teamName1 = intent.getStringExtra("teamName1");
        teamName2 = intent.getStringExtra("teamName2");

        code = (TextView) findViewById(R.id.code);
        code.setText(String.valueOf(gameId));

        teamA = (RadioButton) findViewById(R.id.team_a);
        teamB = (RadioButton) findViewById(R.id.team_b);

        if (teamName1 != null) {
            teamA.setText(teamName1);
        }

        if (teamName2 != null) {
            teamB.setText(teamName2);
        }

        go = (Button) findViewById(R.id.button_go);
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

    @Override
    public void callback(DecodeMessage message) {

        boolean hasStarted;
        int startTime, timePerRound;

        // if right return message from server, start new Activity
        if(message.getReturnType() == "ACK" && message.getRequestType() == "teamJoin"){
            hasStarted = message.getBoolean("hasStarted");
            startTime = message.getInt("startTime");
            timePerRound = message.getInt("timePerRound");
            intent.putExtra("hasStarted", hasStarted);
            intent.putExtra("startTime", startTime);
            intent.putExtra("timePerRound", timePerRound);
            startActivity(intent);
        }
        //else try to send message to server again
        else {
            //send message again
        }
    }
}