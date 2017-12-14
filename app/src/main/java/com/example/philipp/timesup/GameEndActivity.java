package com.example.philipp.timesup;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity which ends whole game.
 Comes after GameActivity (after all rounds played)
 Comes before StartActivity
 Shows final scores
 */

public class GameEndActivity extends ServerIOActivity implements Button.OnClickListener {

    String teamName1, teamName2;
    int score1, score2;
    TextView winnerTxt, endScoreTxt, team1ScoreTxt, team2ScoreTxt;
    Button newGame;
    NetworkHelper networkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_end);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //initialize Global variables from static NetworkHandler
        networkHelper = new NetworkHelper();

        teamName1 = networkHelper.TEAMNAME1;
        teamName2 = networkHelper.TEAMNAME2;

        //get scores from Intent
        Intent intent = getIntent();
        score1 = intent.getIntExtra("scoreTeam1", 0);
        score2 = intent.getIntExtra("scoreTeam2", 0);

        //initialize TextViews and Buttons
        winnerTxt = findViewById(R.id.winner_text);
        endScoreTxt = findViewById(R.id.end_score_text);
        team1ScoreTxt = findViewById(R.id.team1_end_score_text);
        team2ScoreTxt = findViewById(R.id.team2_end_score_text);
        newGame = findViewById(R.id.new_game_button);
        newGame.setOnClickListener(this);

        //Set Text
        if (score1 > score2) {
            winnerTxt.setText(teamName1 + " wins!");
        } else if (score2 > score1) {
            winnerTxt.setText(teamName2 + " wins!");
        } else {
            winnerTxt.setText("It's a draw!");
        }
        endScoreTxt.setText("Final Score:");
        team1ScoreTxt.setText(teamName1 + ":    " + score1);
        team2ScoreTxt.setText(teamName2 + ":    " + score2);
    }

    @Override
    public void onClick(View view) {
        //if Button clicked switch to StartActivity
        switch (view.getId()) {
            case R.id.new_game_button:
                //TODO evt. Einträge bei NetworkHelper löschen
                onBackPressed();
                //Intent intent2 = new Intent(getApplicationContext(), StartActivity.class);
                //startActivity(intent2);
        }
    }

    @Override
    public void callback(DecodeMessage message) {

    }
}
