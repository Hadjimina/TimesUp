package com.example.philipp.timesup;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.ACK;
import static com.example.philipp.timesup.NetworkHelper.BELONGSTOTEAM;
import static com.example.philipp.timesup.NetworkHelper.CLIENTID;
import static com.example.philipp.timesup.NetworkHelper.GAMEID;
import static com.example.philipp.timesup.NetworkHelper.TEAMID1;
import static com.example.philipp.timesup.NetworkHelper.TEAMID2;
import static com.example.philipp.timesup.NetworkHelper.TEAMJOIN;
import static com.example.philipp.timesup.NetworkHelper.TEAMNAME1;
import static com.example.philipp.timesup.NetworkHelper.TEAMNAME2;

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
    String teamName1, teamName2;
    RadioButton teamA, teamB;
    Button go;
    EncodeMessage sendMessage;
    Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_code);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //set code to join the game
        code = findViewById(R.id.code);
        code.setText(String.valueOf(GAMEID));

        getSupportActionBar().setSubtitle("Code: " +  GAMEID);

        //initialise server connection
        setCallbackActivity(this);

        //set texts for teamA and teamB buttons
        teamA = findViewById(R.id.team_a1);
        Log.d("TAG", teamA.toString());
        teamB = findViewById(R.id.team_b1);

        if (teamName1 != null) {
            teamA.setText(TEAMNAME1);
        }

        if (teamName2 != null) {
            teamB.setText(TEAMNAME2);
        }

        //set go button
        go = findViewById(R.id.button_go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (teamA.isChecked() || teamB.isChecked()) {
                    if(teamA.isChecked()){
                        BELONGSTOTEAM = TEAMID1;
                    }
                    else{
                        BELONGSTOTEAM = TEAMID2;
                    }

                } else {
                    toast = Toast.makeText(getApplicationContext(), "please select a team", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                //create message and send it to server
                sendMessage = new EncodeMessage(GAMEID, CLIENTID, BELONGSTOTEAM);
                sendMessage(sendMessage);
                Log.d("TAGsent", sendMessage.toString());

            }
        });
    }

    @Override
    public void callback(DecodeMessage message) {

        boolean hasStarted;
        int startTime, timePerRound, wordsPerPerson;

        Log.d("TAGmessagetype", message.getReturnType());
        Log.d("TAGmessagetype", message.getRequestType());

        // if right return message from server, start new Activity
        if(message.getReturnType().equals(ACK) && message.getRequestType().equals(TEAMJOIN)){

            hasStarted = message.getBoolean("hasStarted");
            startTime = message.getInt("startTime");
            timePerRound = message.getInt("timePerRound");
            Log.d("TAGmessagetype", ""+ hasStarted);

            //create intent and add extras
            intent = new Intent(getApplicationContext(), WordsActivity.class);
            intent.putExtra("hasStarted", hasStarted);
            intent.putExtra("startTime", startTime);
            intent.putExtra("timePerRound", timePerRound);

            //start next activity
            startActivity(intent);
        }
    }
}