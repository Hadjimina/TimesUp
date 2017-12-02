package com.example.philipp.timesup;

import android.os.AsyncTask;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;


public class SocketHandler extends AsyncTask<Void, DecodeMessage, DecodeMessage>{

    //private static final String SERVER_IP = "echo.websocket.org";
    private static final String SERVER_IP = "46.101.97.34";
    private String SERVER_PORT = "9999";

    private ServerIOActivity callbackActivity;
    //private WebSocketClient webSocketClient;
    private NetClient nc;
    private URI uri;


    public SocketHandler(){

        Log.i("Websocket", "initialized without callback!");

        //Initialize Socket
        try {
            this.uri = new URI(SERVER_IP+SERVER_PORT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        this.execute();
    }

    public void sendMessage(EncodeMessage messageToSend){

        nc.sendDataWithString(messageToSend.toJSONString());
        Log.i("Websocket", "Sent " + messageToSend.toJSONString());
    }

    public void setCallbackActivity(ServerIOActivity callbackActivity){
        this.callbackActivity = callbackActivity;
    }

    //cancel async, reinitalize & restart
    public void resetPort(String port){

        this.cancel(true);
        SERVER_PORT = port;
        try {
            uri = new URI(SERVER_IP+SERVER_PORT);
            this.execute();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected DecodeMessage doInBackground(Void... params) {


        nc = new NetClient(SERVER_IP, Integer.parseInt(SERVER_PORT));

        String r = nc.receiveDataFromServer();
        Log.i("websocket Data", r);

        return null;

    }

    @Override
    protected void onPostExecute(DecodeMessage o) {
        super.onPostExecute(o);

    }

    @Override
    protected void onProgressUpdate(DecodeMessage... values) {
        super.onProgressUpdate(values);

        callbackActivity.callback(values[0]);
    }

}
