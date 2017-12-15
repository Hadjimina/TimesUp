package com.example.philipp.timesup;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.philipp.timesup.NetworkHelper.ACK;
import static com.example.philipp.timesup.NetworkHelper.CLIENTID;
import static com.example.philipp.timesup.NetworkHelper.GAMEID;
import static com.example.philipp.timesup.NetworkHelper.READY;
import static com.example.philipp.timesup.NetworkHelper.WORDS;
import static com.example.philipp.timesup.NetworkHelper.WORDSARRAY;
import static com.example.philipp.timesup.NetworkHelper.WORDSPERPERSON;

/**
 * Created by MammaGiulietta on 11.11.17.
 *
 * Activity where users can input their words.
 * Comes after JoinCodeActivity or JoinActivity
 * Comes before GameActivity
 * Lets players input specified number of words
 */

public class WordsActivity extends ServerIOActivity {

    int wordsPerPerson;
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

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //add code & team to actionbar
        //Set currentTeamName
        String teamName1 = NetworkHelper.TEAMNAME1;
        String teamName2 = NetworkHelper.TEAMNAME2;
        int currentTeamID = NetworkHelper.BELONGSTOTEAM;
        String currentTeamName = currentTeamID == 1? teamName1 : teamName2;
        getSupportActionBar().setSubtitle("Code: " +  GAMEID+", Team: "+currentTeamName);

        //initialize connection
        setCallbackActivity(this);

        wordsPerPerson = WORDSPERPERSON;

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
                    //add word to array
                    wordsArray[counter] = String.valueOf(editText.getText());
                    counter++;
                    numberOfWordsString = counter + " " + getWordsString1 + " " + wordsPerPerson + " " + getWordsString2;
                    numberOfWords.setText(numberOfWordsString);
                    enterWords.setText("Are those words correct?");
                    enterButton.setVisibility(View.INVISIBLE);
                    yesButton.setText("YES!");
                    yesButton.setVisibility(View.VISIBLE);
                    editText.setText(wordsArray[0].toString());

                    for(int i = 1; i < wordsPerPerson; i++){

                        editText.append("\n");
                        editText.append(wordsArray[i].toString());

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
                }

                WORDS = wordsArray;
            for(int i = 0; i < wordsPerPerson ; i++) {
                int start = editText.getLayout().getLineStart(i);
                int end = editText.getLayout().getLineEnd(i);
                if(i == wordsPerPerson - 1){
                    wordsArray[i] = editText.getText().subSequence(start, end).toString();
                } else {
                    wordsArray[i] = editText.getText().subSequence(start, end - 1).toString();
                }
                if(wordsArray[i].equals("\n")){
                    toast = Toast.makeText(getApplicationContext(), "Please don't delete any words", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
            }
                //Send message to server
                sendMessage = new EncodeMessage(GAMEID, CLIENTID, wordsArray);
                sendMessage(sendMessage);
                Log.d("TAG-WORDS", "sendig message, pressed YES");
            }
        });

    }


    @Override
    public void callback(DecodeMessage message) {

        Log.d("TAG-WORDS", "got callback");

        if (message.getRequestType().equals(READY) && message.getReturnType().equals(ACK)){
            //TODO get from message which role you will have
            intent.putExtra(WORDSARRAY, wordsArray);
            startActivity(intent);
        }
    }
}