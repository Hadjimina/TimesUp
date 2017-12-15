package com.example.philipp.timesup;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.view.View.VISIBLE;
import static com.example.philipp.timesup.NetworkHelper.GAMEID;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity in which game is played
 Comes after WordsActivity
 Comes before RoundEndActivity (all rounds except last) or GameEndActivity (last round)
 Has three possible views: explainer, guesser, watcher
 For Explainer: shows time remaining, word to explain and "next" button
 For Guesser: shows time remaining and a prompt to guess
 For Watcher: shows time remaining and a prompt that it's not the time to guess
 */

public class GameActivity extends ServerIOActivity {

    static int playerType = -1; // Explain(0)-, Guess(1)- or Watchtype(2)
    static int activeTeam, gameId, clientId, wordIndex, phaseNr, activePlayerId, nextPlayerId, flag, timerFlag;
    SharedPreferences sharedPrefs;
    String username;
    String[] words;
    TextView word;
    Button nextButton;
    CountDownTimer countDown;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setCallbackActivity(this);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get gameId and clientId
        Intent intent = getIntent();
        gameId = NetworkHelper.GAMEID;
        clientId = NetworkHelper.CLIENTID;
        //TODO: get wordarray & active player & wordIndex & phaseNumber from intent of GameEndActivity
        wordIndex = intent.getIntExtra("wordIndex", -1);
        phaseNr = intent.getIntExtra("phaseNumber", -1);
        activePlayerId = intent.getIntExtra("activePlayerId", -1);
        timerFlag = 0;


        //playerType logic
        activeTeam = intent.getIntExtra("activeTeam", -1);
        if(clientId == activePlayerId){
            playerType = 0;
        } else if(activeTeam == NetworkHelper.BELONGSTOTEAM){
            playerType = 1;
        }else{
            playerType = 2;
        }
        nextButton = findViewById(R.id.nextButton);


        words = NetworkHelper.WORDS;

        //add code & team to actionbar
        //Set currentTeamName
        String teamName1 = NetworkHelper.TEAMNAME1;
        String teamName2 = NetworkHelper.TEAMNAME2;
        int currentTeamID = NetworkHelper.BELONGSTOTEAM;
        String currentTeamName = currentTeamID == 1? teamName1 : teamName2;
        getSupportActionBar().setSubtitle("Code: " +  GAMEID+", Team: "+currentTeamName);

        if (playerType == 0) {
            TextView discipline = findViewById(R.id.discipline);
            discipline.setText(getDisciplineString(phaseNr));
            word = findViewById(R.id.wordToGuess);
            word.setVisibility(VISIBLE);
            discipline.setVisibility((VISIBLE));
            nextButton.setVisibility(VISIBLE);
            word.setText(words[wordIndex]);
            nextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    wordIndex++;
                    if(wordIndex < words.length){
                        word.setText(words[wordIndex]);
                    }
                    else{
                        word.setText("No more words..");
                        timerFlag = 1;
                        countDown.cancel();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finishRound();
                    }
                }
            });

        }
        else if(playerType == 1) {
            TextView guessInstruction = findViewById(R.id.guessInstruction);
            guessInstruction.setVisibility(VISIBLE);
        }
        else if(playerType == 2) {
            TextView watchInstruction = findViewById(R.id.watchInstruction);
            watchInstruction.setVisibility(VISIBLE);
        }
        else{
            System.out.println("ERROR: NO ALLOWED playerType");
        }



        //setup timer
        final TextView timer = findViewById(R.id.timer);
        timer.setText(getTimerString(NetworkHelper.TIMEPERROUND));

        countDown = new CountDownTimer(NetworkHelper.TIMEPERROUND*1000, 1000) {
            int timeLeft = NetworkHelper.TIMEPERROUND;

            public void onTick(long millisUntilFinished) {
                timeLeft--;
                timer.setText(getTimerString(timeLeft));
            }

            public void onFinish() {
                nextButton.setVisibility(View.GONE);
                timer.setText("stop!");

                if (playerType == 0 && timerFlag != 1){ finishRound();}
            }
        }.start();
    }

    void finishRound(){
        EncodeMessage message = new EncodeMessage(gameId, clientId, phaseNr, wordIndex);
        sendMessage(message);
    }

    @Override
    public void callback(DecodeMessage message) {
        //new round
        if(message.getRequestType().equals("roundFinished")){
            int scoreTeam1 = message.getInt("scoreTeam1");
            int scoreTeam2 = message.getInt("scoreTeam2");
            String nextPlayer = message.getString("nextPlayerName");
            int nextPlayerId = message.getInt("nextPlayerId");
            int nextPhase = message.getInt("nextPhase");
            flag = 1;
            countDown.cancel();
            if(nextPhase == -1){
                Intent intent = new Intent(getApplicationContext(), GameEndActivity.class);
                intent.putExtra("scoreTeam1", scoreTeam1);
                intent.putExtra("scoreTeam2", scoreTeam2);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(getApplicationContext(), RoundEndActivity.class);
                intent.putExtra("scoreTeam1", scoreTeam1);
                intent.putExtra("scoreTeam2", scoreTeam2);
                intent.putExtra("nextPlayerName", nextPlayer);
                intent.putExtra("nextPhase", nextPhase);
                intent.putExtra("nextPlayerId", nextPlayerId);
                intent.putExtra("flag", flag);
                startActivity(intent);
            }
        }

    }

    String getDisciplineString(int phaseNr){
        switch (phaseNr){
            case 1 : return "explain";
            case 2 : return "mime";
            case 3 : return "one word";
            case 4 : return "freeze";
            case 5 : return "only sounds";
            default: return "wrong phase";
        }
    }

    String getTimerString(int seconds){
        if(seconds < 10){
            return "00:0"+seconds;
        }
        else if(seconds < 60){
            return "00:"+seconds;
        }
        else if(seconds < 600){
            return "0"+seconds/60+":"+seconds%60;
        }
        else{
            return seconds/60+":"+seconds%60;
        }
    }
}