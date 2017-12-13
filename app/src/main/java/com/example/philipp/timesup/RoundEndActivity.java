package com.example.philipp.timesup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
    String teamName1, teamName2, username, activePlayerName, nextPlayerName;

    String[] words;

    int score1, score2, gameId, clientId, startTime, activeTeam, phaseNumber, wordIndex, activePlayerId, nextPlayerId, fromGAFlag;

    TextView team1Txt, team2Txt, nxtPlayerTxt, phaseTxt;
    Button nextRoundButton;

    ProgressDialog progressdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_end);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Set this Activity as CallbackActivity
        Log.d("#RoundEndActivity", "RoundEndActivity is beeing created");
        setCallbackActivity(this);

        //initialize global variables from shared static NetworkHelper Class
        teamName1 = NetworkHelper.TEAMNAME1;
        teamName2 = NetworkHelper.TEAMNAME2;
        gameId = NetworkHelper.GAMEID;
        clientId = NetworkHelper.CLIENTID;
        username = NetworkHelper.USERNAME;

        nextRoundButton = findViewById(R.id.start_next_round);
        nextRoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send startRound to server
                EncodeMessage messageToSend = new EncodeMessage("nextRound", gameId, clientId);
                sendMessage(messageToSend);
            }
        });

        //get scores from Intent
        Intent intent = getIntent();
        score1 = intent.getIntExtra("scoreTeam1", 0);
        score2 = intent.getIntExtra("scoreTeam2", 0);

        //initialize TextViews & button
        team1Txt = findViewById(R.id.team1_text);
        team2Txt = findViewById(R.id.team2_text);
        nxtPlayerTxt = findViewById(R.id.next_player_text);
        phaseTxt = findViewById(R.id.phase_text);


        team1Txt.setText(teamName1 + " score: " + score1);
        team2Txt.setText(teamName2 + " score: " + score2);
        nxtPlayerTxt.setText("Next Player: loading...");
        phaseTxt.setText("Phase: loading...");


        //get Next player and next phase from Intent if from game activity
        fromGAFlag = intent.getIntExtra("flag", 0);
        if(fromGAFlag != 0) {
            Log.d("FLAG", "FLAGGO");
            nextPlayerName = intent.getStringExtra("nextPlayerName");
            phaseNumber = intent.getIntExtra("nextPhase", -1);
            score1 = intent.getIntExtra("score1", 0);
            score2 = intent.getIntExtra("score2", 0);
            nextPlayerId = intent.getIntExtra("nextPlayerId", -1);
            setTextMethod();
        }

        //Set info ProgressDialog "waiting for others" if last Activity was WordActivity
        if(fromGAFlag == 0){
            progressdialog = new ProgressDialog(this);
            progressdialog.setMessage("Please wait for other Players to finish their words.");
            progressdialog.setCancelable(false);
            progressdialog.show();
        }
    }

    public String getPhaseName(int phaseNr){
        switch(phaseNr){
            case 1:
                return "Explain";
            case 2:
                return "Pantomime";
            case 3:
                return "One Word";
            case 4:
                return "Freeze";
            case 5:
                return "Sounds";
            default:
                return Integer.toString(phaseNr);
        }
    }

    @Override
    public void callback(DecodeMessage message) {
        Log.d("#RoundEndActivity", "Callback function is called with message: " + message.getReturnType());

        //case distinction on message received
        //TODO make handling if receive ERROR message
        if(message.getReturnType().equals(NetworkHelper.ROUNDFINISHED)){
            nextPlayerName = message.getString("nextPlayerName");
            nextPlayerId = message.getInt("nextPlayerId");
            phaseNumber = message.getInt("nextPhase");
            setTextMethod();
        }

        if (message.getReturnType().equals(NetworkHelper.STARTROUND)/* && message.getRequestType().equals(NetworkHelper.NEXTROUND)*/) {
            //if message is normal reply of nextRound
            Log.d("#RoundEndActivity", "STARTROUND message received!");

            activePlayerId = message.getInt("activePlayerId");
            activePlayerName = message.getString("activePlayerName");
            startTime = message.getInt("startTime");
            activeTeam = message.getInt("activeTeam");
            phaseNumber = message.getInt("phaseNumber");
            wordIndex = message.getInt("wordIndex");

            Intent intent2 = new Intent(getApplicationContext(), GameActivity.class);
            intent2.putExtra("activePlayerId", activePlayerId);
            intent2.putExtra("activePlayerName", activePlayerName);
            intent2.putExtra("startTime", startTime);
            intent2.putExtra("activeTeam", activeTeam);
            intent2.putExtra("phaseNumber", phaseNumber);
            intent2.putExtra("wordIndex", wordIndex);
            intent2.putExtra("phaseName", getPhaseName(phaseNumber));
            startActivity(intent2);
        }

        if (message.getReturnType().equals(NetworkHelper.SETUP)) {
            //if message is Setup Broadcast
            //TODO stimmt WORDSARRAY? weil online ist wordList!

            words = message.getStringArray(NetworkHelper.WORDSARRAY);
            //TODO stimmt Words oder sind das die einzelnen Wörter?
            NetworkHelper.WORDS = words;

            //if we receive Setup BCAST and last Activity was WordsActivity dismiss ProgressDialog
            if(fromGAFlag == 0){
                progressdialog.dismiss();
            }

        } else {
            Log.d("#RoundEndActivity", "Received wrong message: " + message.getReturnType());
        }
    }

    //isch mr kei besser Name igfalle..
    void setTextMethod(){
        nxtPlayerTxt.setText("Next Player: " + nextPlayerName);
        //TODO Case distinction on phaseNumber
        phaseTxt.setText("Phase: " + getPhaseName(phaseNumber));
        if (clientId == nextPlayerId) {
            nextRoundButton.setVisibility(View.VISIBLE);
        } else {
            nextRoundButton.setVisibility(View.GONE);
        }
    }
}