package com.example.philipp.timesup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity which ends each round.
 Comes after GameActivity (after each player except when completely finished)
 Comes before GameActivity
 Shows current scores and which player is next
 */

public class RoundEndActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_end);
    }
}