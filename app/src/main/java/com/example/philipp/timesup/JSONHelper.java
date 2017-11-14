package com.example.philipp.timesup;


import org.json.JSONObject;

public class JSONHelper {

    private String requestType;
    private int gameId;
    private int clientId;
    private JSONObject body;

    //New Game
    public JSONHelper(boolean[] rounds, String teamName1, String teamName2, int timePerRound, String username){

    }

    //Join Game
    public JSONHelper(int gameId, String username){

    }

    //Join Team
    public JSONHelper(int gameId, int clientId, int teamId){

    }

    //Player Ready
    public JSONHelper(int gameId, int clientId, String[] words){

    }

    //Round finished
    public JSONHelper(int gameId, int clientId, int phaseNr, int wordIndex){

    }


    //Misc Message (sparse)
    // ack, unready, nextRound,
    public  JSONHelper(int gameId, int clientId, String messageType){

    }

    //entire to JSON
    public JSONObject convertToJson(){
        return null;
    }

}
