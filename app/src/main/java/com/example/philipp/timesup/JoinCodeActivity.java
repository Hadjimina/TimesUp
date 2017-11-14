package com.example.philipp.timesup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity which displays code of game for others to join.
 Comes after CreateActivity
 Comes before WordsActivity
 Displays game code and lets game creator choose team
 */

public class JoinCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameEnd);
    }
}