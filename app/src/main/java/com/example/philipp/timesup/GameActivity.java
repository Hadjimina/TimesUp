package com.example.philipp.timesup;

import android.os.Bundle;
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

    static int playerType; // Explain(0)-, Guess(1)- or Watchtype(2)
    static int activeTeam;
    String activePlayer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);



        if (playerType == 0) {
            TextView discipline = findViewById(R.id.discipline);
            TextView word = findViewById(R.id.wordToGuess);
            word.setVisibility(VISIBLE);
            discipline.setVisibility((VISIBLE));
            Button nextButton = findViewById(R.id.nextButton);
            nextButton.setVisibility(VISIBLE);
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
    }

    @Override
    public void callback(DecodeMessage message) {
        if (message.getReturnType().equals("startRound")){
            activeTeam = message.getInt("activeTeam");
            activePlayer = message.getString("activePlayer");
        }
    }
}