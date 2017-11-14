package com.example.philipp.timesup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity which ends whole game.
 Comes after GameActivity (after all rounds played)
 Comes before StartActivity
 Shows final scores
 */

public class GameEndActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_end);
    }
}