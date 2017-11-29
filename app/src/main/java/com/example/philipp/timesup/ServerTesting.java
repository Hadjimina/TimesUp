package com.example.philipp.timesup;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ServerTesting extends ServerIOActivity {

    public int gameID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_testing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        NetworkHelper.handler = new SocketHandler();
        NetworkHelper.handler.setCallbackActivity(this);

        Button newGame = (Button) findViewById(R.id.button_create_game);
        Button joinGame = (Button) findViewById(R.id.button_join_game);
        Button joinTeam = (Button) findViewById(R.id.button_join_team);
        Button ready = (Button) findViewById(R.id.button_ready);
        Button unready = (Button) findViewById(R.id.button_unready);
        Button nextRound = (Button) findViewById(R.id.button_next_round);
        Button roundFinished = (Button) findViewById(R.id.button_round_finished);

        newGame.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //String teamName1, String teamName2, int timePerRound, int wordsPerPerson, String username, boolean[] rounds )
                EncodeMessage msg = new EncodeMessage("TeamName1", "TeamName2", 60, 3, "philipp", new boolean[]{true,true,true,true,true});
                NetworkHelper.handler.sendMessage(msg);

            }
        });



    }

    @Override
    public void callback(DecodeMessage message) {
        Log.i("SERVER RESPONSE:", message.getRawString());

        gameID = message.getGameId();
    }
}
