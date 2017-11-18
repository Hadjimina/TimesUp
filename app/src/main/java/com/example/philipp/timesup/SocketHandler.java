package com.example.philipp.timesup;

import android.os.AsyncTask;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;


public class SocketHandler extends AsyncTask<Void, DecodeMessage, DecodeMessage>{

    private static final String SERVER_IP = "<TO BE SET>";

    private ServerIOActivity callbackActivity;
    private WebSocketClient webSocketClient;
    private URI uri;

    public SocketHandler(ServerIOActivity callbackActivity){
        this.callbackActivity = callbackActivity;

        Log.i("Websocket", "initialized");
        //Initialize Socket
        try {
            this.uri = new URI(SERVER_IP);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(EncodeMessage messageToSend){
        webSocketClient.send(messageToSend.toJSONString());
        Log.i("Websocket", "Sent " + messageToSend.toJSONString());
    }

    @Override
    protected DecodeMessage doInBackground(Void... params) {



        webSocketClient = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String message) {

                Log.i("Websocket", "Got" + message);
                DecodeMessage messageResponse = new DecodeMessage(message);
                publishProgress(messageResponse);

            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        webSocketClient.connect();

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
