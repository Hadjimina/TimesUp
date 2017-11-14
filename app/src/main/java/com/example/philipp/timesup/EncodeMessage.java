package com.example.philipp.timesup;


import org.json.JSONObject;

public class EncodeMessage {

    private String requestType;
    private int gameId;
    private int clientId;
    private JSONObject body;

    //New Game
    public EncodeMessage(boolean[] rounds, String teamName1, String teamName2, int timePerRound, String username, int wordsPerPerson){

    }

    //Join Game
    public EncodeMessage(int gameId, String username){

    }

    //Join Team
    public EncodeMessage(int gameId, int clientId, int teamId){

    }

    //Player Ready
    public EncodeMessage(int gameId, int clientId, String[] words){

    }

    //Round finished
    public EncodeMessage(int gameId, int clientId, int phaseNr, int wordIndex){

    }

    //Misc Message (sparse)
    // ack, unready, nextRound,
    public EncodeMessage(int gameId, int clientId, String messageType){

    }

    //entire to JSON
    public JSONObject convertToJson(){
        return null;
    }

}
