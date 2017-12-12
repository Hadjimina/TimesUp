package com.example.philipp.timesup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.ACK;
import static com.example.philipp.timesup.NetworkHelper.ERROR;
import static com.example.philipp.timesup.NetworkHelper.GAMEID;
import static com.example.philipp.timesup.NetworkHelper.READY;
import static com.example.philipp.timesup.NetworkHelper.WORDSARRAY;

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
    int wordsPerPerson, clientId;
    String [] wordsArray;
    String numberOfWordsString, getWordsString1, getWordsString2;
    int counter = 0;
    Button enterButton, yesButton;
    TextView numberOfWords, enterWords;
    EditText editText;
    Intent intent;
    Toast toast;
    EncodeMessage sendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);


        //add code to actionbar
        getSupportActionBar().setSubtitle("Game code: " +  GAMEID);

        //get information from shared preferences
        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        wordsPerPerson = prefs.getInt("wordsPerPerson", 5);
        clientId = prefs.getInt("clientId", 0);


        //initialize connection
        setCallbackActivity(this);

        //initialize array for words
        wordsArray = new String[wordsPerPerson];

        //initialize textView for number of words that need to be entered
        numberOfWords = findViewById(R.id.number_of_words);
        getWordsString1 = getString(R.string.words_entered1);
        getWordsString2 = getString(R.string.words_entered2);
        numberOfWordsString = counter + " " + getWordsString1 + " " + wordsPerPerson + " " + getWordsString2;
        numberOfWords.setText(numberOfWordsString);

        //set view for textedit
        enterWords = findViewById(R.id.enter_words);


        //initialize enterbutton and editext
        enterButton = findViewById(R.id.button_enter);
        editText = findViewById(R.id.enter_word_edit);


        //initialize yesbutton
        yesButton = findViewById(R.id.button_yes);


        //initialze intent
        intent = new Intent(getApplicationContext(), RoundEndActivity.class);

        //set on click listener for enterbutton
        //TODO shouldn't this be a toggle button? to make "unready" thing happening
        enterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // array isn't full yet
                if (counter <  wordsPerPerson-1){
                    //check if editTex is not empty
                    if (!String.valueOf(editText.getText()).equals("")) {
                        //add word to array and clear edittext
                        wordsArray[counter] = String.valueOf(editText.getText());
                        editText.setText("");
                        Log.d("TAG-WORDS", "first case: counter before update is: " + counter);
                        counter++;
                        Log.d("TAG-WORDS", "first case: counter after update is: " + counter + " words per person: " + wordsPerPerson);
                        numberOfWordsString = counter + " " + getWordsString1 + " " + wordsPerPerson + " " + getWordsString2;
                        numberOfWords.setText(numberOfWordsString);
                    } else {
                        toast = Toast.makeText(getApplicationContext(), "please enter a word", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                //array is full, so send message to server
                else {
                    //add word to array
                    wordsArray[counter] = String.valueOf(editText.getText());
                    Log.d("TAG-WORDS", "2nd case: counter before update is: " + counter);
                    counter++;
                    Log.d("TAG-WORDS", "second case: counter is: " + counter);
                    numberOfWordsString = counter + " " + getWordsString1 + " " + wordsPerPerson + " " + getWordsString2;
                    numberOfWords.setText(numberOfWordsString);
                    enterWords.setText("Are those words correct?");
                    enterButton.setVisibility(View.INVISIBLE);
                    yesButton.setText("YES!");
                    yesButton.setVisibility(View.VISIBLE);
                    editText.setText(wordsArray[0].toString());
                    editText.append("\n");

                    for(int i = 1; i < wordsPerPerson; i++){
                        editText.append(wordsArray[i].toString() + "\n");

                    }


                }
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            for(int i = 0; i < wordsPerPerson; i++) {
                int start = editText.getLayout().getLineStart(i);
                int end = editText.getLayout().getLineEnd(i);
                wordsArray[i] = editText.getText().subSequence(start, end).toString();
                if(wordsArray[i] == null){
                    toast = Toast.makeText(getApplicationContext(), "Please don't delete any words", Toast.LENGTH_LONG);
                    return;
                }
            }
                //Send message to server
                sendMessage = new EncodeMessage(GAMEID, clientId, wordsArray);
                sendMessage(sendMessage);
            }
        });





    }

    @Override
    public void callback(DecodeMessage message) {


        if (message.getRequestType().equals(READY) && message.getReturnType().equals(ACK)){

            //TODO get from message which role you will have
            intent.putExtra(WORDSARRAY, wordsArray);
            startActivity(intent);
        }
        else if (message.getRequestType().equals(READY) && message.getReturnType().equals(ERROR)){
            //now implemnted in websocket
            //toast = Toast.makeText(getApplicationContext(), "error with being ready", Toast.LENGTH_LONG);
            //toast.show();
            //sendMessage(sendMessage);
        } else {
            //now implemented in websocket
            //toast = Toast.makeText(getApplicationContext(), "pretty much everything went wrong with contacting the server", Toast.LENGTH_LONG);
            //toast.show();
        }

    }
    @Override
    public void onBackPressed() {
        toast = Toast.makeText(getApplicationContext(), "Going back to start activity", Toast.LENGTH_LONG);
        toast.show();
        intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
    }
}
