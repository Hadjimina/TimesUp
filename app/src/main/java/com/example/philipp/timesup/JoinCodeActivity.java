package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.ACK;
import static com.example.philipp.timesup.NetworkHelper.ERROR;
import static com.example.philipp.timesup.NetworkHelper.MYPREFS;
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
    int gameId, clientId;
    String teamName1, teamName2;
    RadioButton teamA, teamB;
    Button go;
    EncodeMessage sendMessage;
    Toast toast;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_code);


        //retrieve information from shared Preferences
        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        clientId = prefs.getInt("clientId", 0);
        teamName1 = prefs.getString("teamName1", "team1");
        teamName2 = prefs.getString("teamName2", "team2");
        gameId = prefs.getInt("gameId", -1);

        //set code to join the game
        code = findViewById(R.id.code);
        code.setText(String.valueOf(gameId));


        //initialise server connection
        setCallbackActivity(this);

        //set texts for teamA and teamB buttons
        teamA = findViewById(R.id.team_a1);
        Log.d("TAG", teamA.toString());
        teamB = findViewById(R.id.team_b1);

        if (teamName1 != null) {
            teamA.setText(teamName1);
        }

        if (teamName2 != null) {
            teamB.setText(teamName2);
        }

        //set go button
        go = findViewById(R.id.button_go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int teamId;
                if (teamA.isChecked() || teamB.isChecked()) {
                    if(teamA.isChecked()){
                        teamId = 1;
                    }
                    else{
                        teamId = 2;
                    }

                } else {
                    toast = Toast.makeText(getApplicationContext(), "please select a team", Toast.LENGTH_LONG);
                    return;
                }
                prefs = getSharedPreferences(MYPREFS, MODE_PRIVATE);
                editor = prefs.edit();
                editor.putInt("teamId", teamId);
                editor.apply();

                //create message and send it to server
                sendMessage = new EncodeMessage(gameId, clientId, teamId);
                sendMessage(sendMessage);
                Log.d("TAGsent", sendMessage.toString());

            }
        });
    }

    @Override
    public void callback(DecodeMessage message) {

        boolean hasStarted;
        int startTime, timePerRound, wordsPerPerson;

        //initialize shared preferences
        prefs = getSharedPreferences(MYPREFS, MODE_PRIVATE);
        editor = prefs.edit();

        Log.d("TAGmessagetype", message.getReturnType());
        Log.d("TAGmessagetype", message.getRequestType());

        // if right return message from server, start new Activity
        if(message.getReturnType().equals(ACK) && message.getRequestType().equals(TEAMJOIN)){

            hasStarted = message.getBoolean("hasStarted");
            startTime = message.getInt("startTime");
            timePerRound = message.getInt("timePerRound");
            Log.d("TAGmessagetype", ""+ hasStarted);

            //cretae intent and add extras
            intent = new Intent(getApplicationContext(), WordsActivity.class);
            intent.putExtra("hasStarted", hasStarted);
            intent.putExtra("startTime", startTime);
            intent.putExtra("timePerRound", timePerRound);

            //put wordsPerPerson into shared preferences
            wordsPerPerson = message.getInt("wordsPerPerson");
            editor.putInt("wordsPerPerson", wordsPerPerson);
            editor.apply();

            //start next activity
            startActivity(intent);
        }
        //else try to send message to server again and show error
        else if(message.getReturnType().equals(ERROR) && message.getRequestType().equals(TEAMJOIN)){
            toast = Toast.makeText(getApplicationContext(), "error with joining a team", Toast.LENGTH_LONG);
            toast.show();
            sendMessage(sendMessage);
        }
        //show error and go back to start
        else {
            toast = Toast.makeText(getApplicationContext(), "pretty much everything went wrong with contacting the server", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}