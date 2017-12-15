package com.example.philipp.timesup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import static com.example.philipp.timesup.NetworkHelper.GAMEID;

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
    String teamName1, teamName2, username, activePlayerName, nextPlayerName, currentTeamName;

    String[] words;

    int score1, score2, gameId, clientId, startTime, activeTeam, phaseNumber, wordIndex, activePlayerId, nextPlayerId, fromGAFlag, currentTeamID;

    TextView team1Txt, team2Txt, nxtPlayerTxt, phaseTxt, currentTeamTxt;
    Button nextRoundButton;
    ImageButton roundInfo;

    ProgressDialog progressdialog;
    AlertDialog.Builder alertDialogBuilder;
    AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_end);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Set this Activity as CallbackActivity
        Log.d("#RoundEndActivity", "RoundEndActivity is beeing created");
        setCallbackActivity(this);

        //if roundFinished is not received, this will no interfere with the clientId 0
        nextPlayerId = -1;

        //initialize global variables from shared static NetworkHelper Class
        teamName1 = NetworkHelper.TEAMNAME1;
        teamName2 = NetworkHelper.TEAMNAME2;
        gameId = NetworkHelper.GAMEID;
        clientId = NetworkHelper.CLIENTID;
        username = NetworkHelper.USERNAME;
        currentTeamID = NetworkHelper.BELONGSTOTEAM;



        //Set currentTeamName
        currentTeamName = currentTeamID == 1? teamName1 : teamName2;

        //add code & teamnam to actionbar
        getSupportActionBar().setSubtitle("Code: " +  GAMEID+", Team: "+currentTeamName);

        //initialize Button
        nextRoundButton = findViewById(R.id.start_next_round);
        nextRoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send startRound to server
                EncodeMessage messageToSend = new EncodeMessage("nextRound", gameId, clientId);
                sendMessage(messageToSend);
            }
        });

        //show info for Rounds if infoButton pressed
        alertDialogBuilder = new AlertDialog.Builder(this);
        roundInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogBuilder.setTitle("Rounds");
                alertDialogBuilder.setMessage("Explanation of Rounds: \n" +
                "Explain: Simply explain the word without using it. \n" +
                "Pantomime: Try to describe the word without speaking \n" +
                "One Word: Use a single Word to describe the solution. Of course it should be a different word! \n" +
                "Freeze: You're allowed to use a single posture to describe the word, but don't move! \n" +
                "Sounds: Use any sounds which remind of the word in question. Talking is not allowed here! \n")
                        .setCancelable(true)
                        .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
                alert = alertDialogBuilder.create();
                alert.show();
            }
        });

        //get scores from Intent
        Intent intent = getIntent();
        score1 = intent.getIntExtra("scoreTeam1", 0);
        score2 = intent.getIntExtra("scoreTeam2", 0);

        //initialize TextViews
        team1Txt = findViewById(R.id.team1_text);
        team2Txt = findViewById(R.id.team2_text);
        nxtPlayerTxt = findViewById(R.id.nextUserName);
        phaseTxt = findViewById(R.id.nextPhaseName);
        roundInfo = findViewById(R.id.info_rounds);
        //nxtPlayerTxt = findViewById(R.id.nextUserName);
        //phaseTxt = findViewById(R.id.nextPhaseName);
        //currentTeamTxt = findViewById(R.id.current_team_text);

        team1Txt.setText(teamName1 + " score: " + score1);
        team2Txt.setText(teamName2 + " score: " + score2);
        //currentTeamTxt.setText("Your Team: " + currentTeamName);

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
        if(message.getReturnType().equals(NetworkHelper.ROUNDFINISHED)){
            Log.d("#RoundEndActivity", "message: " + message);
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
                setTextMethod();
            }

        } else {
            Log.d("#RoundEndActivity", "Received wrong message: " + message.getReturnType());
        }
    }

    //set the TextView values
    void setTextMethod(){
        nxtPlayerTxt.setText(nextPlayerName);
        phaseTxt.setText(getPhaseName(phaseNumber));
        if (clientId == nextPlayerId) {
            nextRoundButton.setVisibility(View.VISIBLE);
        } else {
            nextRoundButton.setVisibility(View.GONE);
        }
    }
}