package com.example.philipp.timesup;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EncodeMessage {

    private String requestType;
    private int gameId;
    private int clientId;
    private JSONObject body;

    //New Game
    public EncodeMessage(String teamName1, String teamName2, int timePerRound, int wordsPerPerson, String username, boolean[] rounds ){
        setHeader(NetworkHelper.NEWGAME, -1,-1);

        JSONArray roundsJSON = new JSONArray();
        for(boolean b : rounds){
            roundsJSON.put(b);
        }


        try {
            body.put("teamName1", teamName1);
            body.put("teamName2", teamName2);
            body.put("timePerRound", timePerRound);
            body.put("wordsPerPerson", wordsPerPerson);
            body.put("username", username);
            body.put("rounds", roundsJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Join Game
    //-1 = clientId if the client does not yet have an id
    public EncodeMessage(int gameId, String username){
        setHeader(NetworkHelper.JOIN, gameId, -1);

        try {
            body.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Join Team
    public EncodeMessage(int gameId, int clientId, int teamId){
        setHeader(NetworkHelper.TEAMJOIN, gameId, clientId);

        try {
            body.put("teamToJoin", teamId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Player Ready
    public EncodeMessage(int gameId, int clientId, String[] words){
        setHeader(NetworkHelper.READY, gameId, clientId);

        JSONArray wordList = new JSONArray();
        for(String s : words){
            wordList.put(s);
        }

        try {
            body.put("wordList",wordList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Round finished
    public EncodeMessage(int gameId, int clientId, int phaseNr, int wordIndex){
        setHeader(NetworkHelper.ROUNDFINISHED, gameId, clientId);

        try {
            body.put("phaseNumber", phaseNr);
            body.put("wordIndex", wordIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Misc Message (sparse)
    // ack, unready, nextRound, setup, startround
    public EncodeMessage(String messageType, int gameId, int clientId){
        setHeader(messageType, gameId, clientId);
    }

    private void setHeader(String requestType, int gameId, int clientId){
        this.body = new JSONObject();
        this.requestType = requestType;
        this.gameId = gameId;
        this.clientId = clientId;
    }


    public JSONObject toJSONObject(){
        JSONObject entireObject = new JSONObject();

        try {
            entireObject.put("requestType", requestType);
            entireObject.put("gameId", gameId);
            entireObject.put("clientId", clientId);
            entireObject.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return entireObject;
    }

    public String toJSONString(){
        return toJSONObject().toString();
    }

}
