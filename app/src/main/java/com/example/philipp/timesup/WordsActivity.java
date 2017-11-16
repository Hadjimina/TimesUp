package com.example.philipp.timesup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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
    SharedPreferences prefs;
    int wordsPerPerson;
    SharedPreferences.Editor editor;
    String [] wordsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);



        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        wordsPerPerson = prefs.getInt("wordsPerPerson", 0);
        wordsArray = new String[wordsPerPerson];
        editor = prefs.edit();

        Button enterbutton = findViewById(R.id.button_enter);
        enterbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // array isn't full yet
                if (wordsArray[wordsPerPerson].equals(null)){
                    //TODO add new word
                }
                //array is full, so send message to server
                else {
                    //TODO send message to server
                }
            }
        });





    }
}
