package com.example.philipp.timesup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity which lets user join a game.
 Comes after CreateJoinActivity (join button)
 Comes before WordsActivity
 Lets user input a game code and name, lets them choose team afterwards
 */

public class JoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
    }
}