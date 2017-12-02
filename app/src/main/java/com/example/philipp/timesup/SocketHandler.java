package com.example.philipp.timesup;

import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;


public class SocketHandler extends AsyncTask<Void, DecodeMessage, DecodeMessage>{

    //private static final String SERVER_IP = "echo.websocket.org";
    private static final String SERVER_IP = "46.101.97.34";
    private String SERVER_PORT = "9999";

    private ServerIOActivity callbackActivity;
    //private WebSocketClient webSocketClient;
    private Socket socket;
    private URI uri;
    private String response;
    private  PrintWriter output;

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
        //webSocketClient.send(messageToSend.toJSONString());
        try{
            OutputStream out = socket.getOutputStream();
            PrintWriter output = new PrintWriter(out);
            output.println(messageToSend.toJSONString());
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

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


        NetClient nc = new NetClient(SERVER_IP, Integer.parseInt(SERVER_PORT));
        nc.sendDataWithString("{}");

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
