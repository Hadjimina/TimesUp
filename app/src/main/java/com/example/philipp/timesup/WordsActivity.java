package com.example.philipp.timesup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity where users can input their words.
 Comes after JoinCodeActivity or JoinActivity
 Comes before GameActivity
 Lets players input specified number of words
 */

public class WordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
    }
}
