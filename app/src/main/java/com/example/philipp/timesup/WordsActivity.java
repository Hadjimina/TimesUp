package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.ACK;
import static com.example.philipp.timesup.NetworkHelper.ERROR;
import static com.example.philipp.timesup.NetworkHelper.READY;
import static com.example.philipp.timesup.NetworkHelper.UNREADY;

/**
 * Created by MammaGiulietta on 11.11.17.
 *
 * Activity where users can input their words.
 * Comes after JoinCodeActivity or JoinActivity
 * Comes before GameActivity
 * Lets players input specified number of words
 */

public class WordsActivity extends ServerIOActivity {

    SharedPreferences prefs;
    int wordsPerPerson, gameId, clientId;
    String [] wordsArray;
    String numberOfWordsString, getWordsString1, getWordsString2;
    int counter = 0;
    Button enterButton, readyButton;
    TextView numberOfWords, enterWords;
    EditText editText;
    Boolean readyAnswer = false;
    Intent intent;
    Toast toast;
    EncodeMessage sendMessage;
    SocketHandler socketHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        //get WordsPerPerson from shared preferences
        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        wordsPerPerson = prefs.getInt("wordsPerPerson", 5);
        gameId = prefs.getInt("gameId", 0);
        clientId = prefs.getInt("clientId", 0);

        //initialise server connection
        socketHandler = new SocketHandler(this);
        socketHandler.execute();

        //initialize array for words
        wordsArray = new String[wordsPerPerson];

        //initialize textView for number of words that need to be entered
        numberOfWords = findViewById(R.id.number_of_words);
        getWordsString1 = getString(R.string.words_entered1);
        getWordsString2 = getString(R.string.words_entered2);
        numberOfWordsString = counter + " " + getWordsString1 + " " + wordsPerPerson + " " + getWordsString2;
        numberOfWords.setText(numberOfWordsString);

        enterWords = findViewById(R.id.enter_words);


        //initialize enterbutton and editext
        enterButton = findViewById(R.id.button_enter);
        editText = findViewById(R.id.enter_word_edit);

        //set on click listener for enterbutton
        //TODO shouldn't this be a toggle button? to make "unready" thing happening
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // array isn't full yet
                if (counter <  wordsPerPerson - 1){
                    //check if editTex is not empty
                    if (!String.valueOf(editText.getText()).equals("")) {
                        //add word to array and clear edittext
                        wordsArray[counter] = String.valueOf(editText.getText());
                        editText.setText("");
                        counter++;
                        numberOfWordsString = counter + " " + getWordsString1 + " " + wordsPerPerson + " " + getWordsString2;
                        numberOfWords.setText(numberOfWordsString);
                    } else {
                        toast = Toast.makeText(getApplicationContext(), "please enter a word", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                //array is full, so send message to server
                else {
                    numberOfWordsString = counter + " " + getWordsString1 + " " + wordsPerPerson + " " + getWordsString2;
                    numberOfWords.setText(numberOfWordsString);
                    enterWords.setVisibility(View.INVISIBLE);
                    editText.setVisibility(View.INVISIBLE);
                    enterButton.setVisibility(View.INVISIBLE);
                    readyButton.setVisibility(View.VISIBLE);

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
                    intent = new Intent(getApplicationContext(), RoundEndActivity.class);

                    //create message and send it to server
                    sendMessage = new EncodeMessage(gameId, clientId, wordsArray);
                    socketHandler.sendMessage(sendMessage);
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
        if (message.getRequestType().equals(UNREADY) && message.getReturnType().equals(ACK)){
            readyAnswer = true;
        }
        else if (message.getRequestType().equals(UNREADY) && message.getReturnType().equals(ERROR)){
            toast = Toast.makeText(getApplicationContext(), "error with being unready a team", Toast.LENGTH_LONG);
            toast.show();
            socketHandler.sendMessage(sendMessage);
        }
        else if (message.getRequestType().equals(READY) && message.getReturnType().equals(ACK)){

            //TODO get from message which role you will have
            startActivity(intent);
        }
        else if (message.getRequestType().equals(READY) && message.getReturnType().equals(ERROR)){
            toast = Toast.makeText(getApplicationContext(), "error with being ready a team", Toast.LENGTH_LONG);
            toast.show();
            socketHandler.sendMessage(sendMessage);
        } else {
            toast = Toast.makeText(getApplicationContext(), "pretty much everything went wrong with contacting the server", Toast.LENGTH_LONG);
            toast.show();
        }

    }
}
