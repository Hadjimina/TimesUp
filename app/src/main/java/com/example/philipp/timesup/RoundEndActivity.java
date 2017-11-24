package com.example.philipp.timesup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity which ends each round.
 Comes after GameActivity (after each player except when completely finished)
 Comes before GameActivity
 Shows current scores and which player is next
 */

public class RoundEndActivity extends ServerIOActivity {
    String team1, team2, nextPlayer;

    int score1, score2;

    TextView team1Txt, team2Txt, nxtPlayerTxt;
    Button nextRoundButton;

    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_end);

        //initialize global variables from shared preferences
        mPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);

        team1 = mPreferences.getString("teamName1", "Team1");
        team2 = mPreferences.getString("teamName2", "Team2");

        //initialize TextViews & button
        team1Txt = findViewById(R.id.team1_text);
        team2Txt = findViewById(R.id.team2_text);
        nxtPlayerTxt = findViewById(R.id.next_player_text);

        team1Txt.setText(team1 + " score: " + "loading...");
        team2Txt.setText(team2 + " score: " + "loading...");
        nxtPlayerTxt.setText("");

        nextRoundButton = findViewById(R.id.start_next_round);
        //TODO make button visible if nextplayer = user

        //get nextPlayer
        //TODO make a servercall to get next player
        nextPlayer = "nextPlayer";

        //get Scores
        //TODO make a servercall to get scores
        score1 = 0;
        score2 = 0;


    }

    @Override
    public void callback(DecodeMessage message) {
        //TODO case distinction on messagetype
    }
}