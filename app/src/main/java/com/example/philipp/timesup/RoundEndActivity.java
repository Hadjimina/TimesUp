package com.example.philipp.timesup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

public class RoundEndActivity extends ServerIOActivity implements Button.OnClickListener{
    String team1, team2, activePlayer, username;

    int score1, score2, gameId, clientId, startTime, activeTeam, phaseNumber, wordIndex;

    TextView team1Txt, team2Txt, nxtPlayerTxt;
    Button nextRoundButton;

    SharedPreferences mPreferences;

    SocketHandler handler;
    NetworkHelper networkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_end);
        System.out.println("DO ISCHS AKO");

        //initialize global variables from shared preferences
        mPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);

        team1 = mPreferences.getString("teamName1", "Team1");
        team2 = mPreferences.getString("teamName2", "Team2");
        gameId = mPreferences.getInt("gameId",0);
        clientId = mPreferences.getInt("clientId", 0);
        username = mPreferences.getString("username", "XXX");

        //get scores from Intent
        Intent intent = getIntent();
        score1 = intent.getIntExtra("score1", 0);
        score2 = intent.getIntExtra("score2", 0);

        //initialize TextViews & button
        team1Txt = findViewById(R.id.team1_text);
        team2Txt = findViewById(R.id.team2_text);
        nxtPlayerTxt = findViewById(R.id.next_player_text);

        team1Txt.setText(team1 + " score: " + score1);
        team2Txt.setText(team2 + " score: " + score2);
        nxtPlayerTxt.setText("");

        nextRoundButton = findViewById(R.id.start_next_round);

        //initialize SocketHandler & get next player
        setCallbackActivity(this);
        networkHelper = new NetworkHelper();
        EncodeMessage messageToSend = new EncodeMessage(networkHelper.NEXTROUND, gameId, clientId);
        handler.sendMessage(messageToSend);

    }

    public void startNextRound() {
        //TODO make a servercall to start next round

    }

    @Override
    public void callback(DecodeMessage message) {
        //case distinction on message received
        //TODO change condition below to listen on correct incoming message type
        if (message.getRequestType().equals(networkHelper.NEXTROUND)){
            //if message is normal reply of nextround
            activePlayer = message.getString("activePlayer");
            startTime = message.getInt("startTime");
            activeTeam = message.getInt("activeTeam");
            phaseNumber = message.getInt("phaseNumber");
            wordIndex = message.getInt("wordIndex");

            nxtPlayerTxt.setText("Next Player: " + activePlayer);
            if(username.equals(activePlayer)){
                nextRoundButton.setVisibility(View.GONE);
            }else{
                nextRoundButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        // if button is clicked start new round
        switch(view.getId()) {
            case R.id.start_next_round:
                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                intent.putExtra("activePlayer", activePlayer);
                intent.putExtra("startTime", startTime);
                intent.putExtra("activeTeam", activeTeam);
                intent.putExtra("phaseNumber", phaseNumber);
                intent.putExtra("wordIndex", wordIndex);
                startActivity(intent);
        }
    }
}