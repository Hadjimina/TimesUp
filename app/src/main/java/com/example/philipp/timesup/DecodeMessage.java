package com.example.philipp.timesup;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class DecodeMessage {

    private String returnType;
    private String requestType;
    private int gameId;
    private int clientId;
    private JSONObject body;

    public DecodeMessage(String rawMessage){
        try{
            JSONObject entireObject = new JSONObject(rawMessage);
            this.returnType = entireObject.getString("returnType");
            this.requestType = entireObject.getString("requestType");
            this.gameId = entireObject.getInt("gameId");
            this.clientId = entireObject.getInt("clientId");
            this.body = entireObject.getJSONObject("body");

        }catch (JSONException e) {
            Log.e("Decode error", "Error in constuructor");
            e.printStackTrace();
        }

    }

    public String getString(String key){
        try {
            return body.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("Decode error", "Your key does not exist, you got null");
        return null;
    }

    public Integer getInt(String key){
        try {
            return body.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("Decode error", "Your key does not exist, you got null");
        return null;
    }

    public Boolean getBoolean(String key){
        try {
            return body.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("Decode error", "Your key does not exist, you got null");
        return null;
    }

    public String[] getStringArray(String key){
        try {
            JSONArray jsonArray = body.getJSONArray(key);
            String[] tempString = new String[jsonArray.length()];

            for(int i = 0; i < jsonArray.length(); i++){
                tempString[i] = jsonArray.getString(i);
            }

            return tempString;

        } catch (JSONException e) {
            e.printStackTrace();

        }
        Log.e("Decode error", "Your key does not exist, you got null");
        return null;
    }

}
