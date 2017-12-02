package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.view.View.VISIBLE;

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
    static int activeTeam, gameId, clientId, wordsPerPerson, wordIndex, count, phaseNr;
    String activePlayer;
    SharedPreferences sharedPrefs;
    String username;
    String[] words;
    TextView word;
    SocketHandler handler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //get gameId and clientId
        Intent intent = getIntent();
        gameId = intent.getIntExtra("gameId", -1);
        clientId = intent.getIntExtra("clientId", -1);
        //TODO: get wordarray & active player & wordIndex & phaseNumber from intent of GameEndActivity
        //wordIndex = intent.getIntExtra("wordIndex", -1);

        //get username
        sharedPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        username = sharedPrefs.getString("username", null);
        wordsPerPerson = sharedPrefs.getInt("wordsPerPerson", 0);
        words = new String[wordsPerPerson];


        playerType = 0;
        count = 0;

        //handler = new SocketHandler(this);


        if (playerType == 0) {
            TextView discipline = findViewById(R.id.discipline);
            word = findViewById(R.id.wordToGuess);
            word.setVisibility(VISIBLE);
            discipline.setVisibility((VISIBLE));
            Button nextButton = findViewById(R.id.nextButton);
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
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.execute();
                        EncodeMessage message = new EncodeMessage(gameId, clientId, phaseNr, wordIndex);
                        Intent intent = new Intent(getApplicationContext(), RoundEndActivity.class);
                        startActivity(intent);
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
        timer.setText("00:30");

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setText("00:" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timer.setText("stop!");
            }
        }.start();

    }

    @Override
    public void callback(DecodeMessage message) {
        //new round
        if(message.getReturnType().equals("ACK") && message.getRequestType().equals("roundFinished")){
            Intent intent = new Intent(getApplicationContext(), RoundEndActivity.class);
            startActivity(intent);
        }

    }
}