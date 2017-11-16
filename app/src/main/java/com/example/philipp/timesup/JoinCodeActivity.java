package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.ACK;
import static com.example.philipp.timesup.NetworkHelper.ERROR;
import static com.example.philipp.timesup.NetworkHelper.TEAMJOIN;

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
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_code);
        intent = getIntent();
        gameId = intent.getIntExtra("gameId", 0);
        teamName1 = intent.getStringExtra("teamName1");
        teamName2 = intent.getStringExtra("teamName2");

        code = findViewById(R.id.code);
        code.setText(String.valueOf(gameId));

        teamA = findViewById(R.id.team_a1);
        teamB = findViewById(R.id.team_b1);

        if (teamName1 != null) {
            teamA.setText(teamName1);
        }

        if (teamName2 != null) {
            teamB.setText(teamName2);
        }

        go = findViewById(R.id.button_go);
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
        int startTime, timePerRound, wordsPerPerson;
        //initialize shared preferences
        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = prefs.edit();

        // if right return message from server, start new Activity
        if(message.getReturnType().equals(ACK) && message.getRequestType().equals(TEAMJOIN)){
            hasStarted = message.getBoolean("hasStarted");
            startTime = message.getInt("startTime");
            timePerRound = message.getInt("timePerRound");
            intent.putExtra("hasStarted", hasStarted);
            intent.putExtra("startTime", startTime);
            intent.putExtra("timePerRound", timePerRound);

            //TODO they should add this to message (Backendguys)
            wordsPerPerson = message.getInt("wordsPerPerson");
            editor.putInt("wordsPerPerson", wordsPerPerson);
            editor.apply();

            startActivity(intent);
        }
        //else try to send message to server again and show error
        else if(message.getReturnType().equals(ERROR) && message.getRequestType().equals(TEAMJOIN)){
            //TODO, same as in JoinCode
        }
        //show error and go back to start
        else {
            //TODO
        }
    }
}