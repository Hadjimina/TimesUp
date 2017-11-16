package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by MammaGiulietta on 11.11.17.
 */

/**
 Activity where users can input their words.
 Comes after JoinCodeActivity or JoinActivity
 Comes before GameActivity
 Lets players input specified number of words
 */

public class WordsActivity extends ServerIOActivity {
    SharedPreferences prefs;
    int wordsPerPerson;
    String [] wordsArray;
    int counter = 0;
    Button enterButton;
    Button readyButton;
    EditText editText;
    Boolean readyAnswer = false;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);


        //get WordsPerPerson from shared preferences
        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        wordsPerPerson = prefs.getInt("wordsPerPerson", 5);

        //initialize array for words
        wordsArray = new String[wordsPerPerson];


        //initialize enterbutton and editext
        enterButton = findViewById(R.id.button_enter);
        editText = findViewById(R.id.enter_word_edit);

        //set on click listener for enterbutton
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // array isn't full yet
                if (counter <  wordsPerPerson){

                    //add word to array and clear edittext
                    wordsArray[counter] = String.valueOf(editText.getText());
                    editText.setText("");
                    counter++;
                }
                //array is full, so send message to server
                else {
                    enterButton.setVisibility(View.INVISIBLE);
                    readyButton.setVisibility(View.VISIBLE);
                    //TODO send message to server with request type unready and gameid, clientid (shouldn't it be words list here?)
                }
            }
        });

        //initialize readybutton
        readyButton = findViewById(R.id.button_ready);

        //set onclicklistener for readybutton
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (readyAnswer){
                    intent = new Intent(getApplicationContext(), GameEndActivity.class);

                    //TODO send message to server
                }
                else {
                    //do nothing
                }
            }
        });





    }

    @Override
    public void callback(DecodeMessage message) {

        //if callback from unready, set global variable to true so that user can click on the readybutton
        if (message.getRequestType().equals("unready") && message.getReturnType().equals("ACK")){
            readyAnswer = true;
        }
        else if (message.getRequestType().equals("unready") && message.getReturnType().equals("error")){
            //TODO errorhandling
        }
        else if (message.getRequestType().equals("ready") && message.getReturnType().equals("ACK")){

            //get from message which role you will have
            startActivity(intent);
        }
        else if (message.getRequestType().equals("ready") && message.getReturnType().equals("error")){
            //TODO errorhandling
        }

    }
}
