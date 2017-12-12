package com.example.philipp.timesup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class RoundEndActivity extends ServerIOActivity  {
    String teamName1, teamName2, username;

    String[] words;

    int score1, score2, gameId, clientId, startTime, activeTeam, phaseNumber, wordIndex, activePlayer;

    TextView team1Txt, team2Txt, nxtPlayerTxt, phaseTxt;
    Button nextRoundButton;

    NetworkHelper networkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_end);

        //Set this Activity as CallbackActivity
        Log.d("#RoundEndActivity", "RoundEndActivity is beeing created");
        setCallbackActivity(this);

        //initialize global variables from shared static NetworkHelper Class
        //networkHelper = new NetworkHelper();

        teamName1 = networkHelper.TEAMNAME1;
        teamName2 = networkHelper.TEAMNAME2;
        gameId = networkHelper.GAMEID;
        clientId = networkHelper.CLIENTID;
        username = networkHelper.USERNAME;

        //get scores from Intent
        Intent intent = getIntent();
        score1 = intent.getIntExtra("score1", 0);
        score2 = intent.getIntExtra("score2", 0);

        //initialize TextViews & button
        team1Txt = findViewById(R.id.team1_text);
        team2Txt = findViewById(R.id.team2_text);
        nxtPlayerTxt = findViewById(R.id.next_player_text);
        phaseTxt = findViewById(R.id.phase_text);


        team1Txt.setText(teamName1 + " score: " + score1);
        team2Txt.setText(teamName2 + " score: " + score2);
        nxtPlayerTxt.setText("Next Player: loading...");
        phaseTxt.setText("Phase: loading...");

        nextRoundButton = findViewById(R.id.start_next_round);
        nextRoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.start_next_round:
                        Intent intent2 = new Intent(getApplicationContext(), GameActivity.class);
                        intent2.putExtra("activePlayer", activePlayer);
                        intent2.putExtra("startTime", startTime);
                        intent2.putExtra("activeTeam", activeTeam);
                        intent2.putExtra("phaseNumber", phaseNumber);
                        intent2.putExtra("wordIndex", wordIndex);
                        startActivity(intent2);
                }
            }
        });
    }

    @Override
    public void callback(DecodeMessage message) {
        Log.d("#RoundEndActivity", "Callback function is called with message: " + message.getReturnType());

        //case distinction on message received
        //TODO make handling if receive ERROR message
        if (message.getReturnType().equals(networkHelper.STARTROUND)/* && message.getRequestType().equals(networkHelper.NEXTROUND)*/) {
            //if message is normal reply of nextRound
            Log.d("#RoundEndActivity", "STARTROUND message received!");

            activePlayer = message.getInt("activePlayer");
            startTime = message.getInt("startTime");
            activeTeam = message.getInt("activeTeam");
            phaseNumber = message.getInt("phaseNumber");
            wordIndex = message.getInt("wordIndex");

            nxtPlayerTxt.setText("Next Player: " + activePlayer);
            //TODO Case distinction on phaseNumber
            phaseTxt.setText("Phase: " + phaseNumber);
            if (clientId == activePlayer) {
                nextRoundButton.setVisibility(View.VISIBLE);
            } else {
                nextRoundButton.setVisibility(View.GONE);
            }
        }
        if (message.getReturnType().equals(networkHelper.SETUP)) {
            //if message is Setup Broadcast
            //TODO stimmt WORDSARRAY? weil online ist wordList!

            words = message.getStringArray(networkHelper.WORDSARRAY);
            //TODO stimmt Words oder sind das die einzelnen Wörter?
            networkHelper.WORDS = words;

            //if we receive Setup BCAST make NextRound Servercall
            EncodeMessage messageToSend = new EncodeMessage("nextRound", gameId, clientId);
            sendMessage(messageToSend);
        } else {
            Log.d("#RoundEndActivity", "Received wrong message: " + message.getReturnType());
        }
    }
}