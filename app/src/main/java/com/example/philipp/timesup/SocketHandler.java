package com.example.philipp.timesup;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;


public class SocketHandler extends AsyncTask<Void, DecodeMessage, DecodeMessage> {

    //private static final String SERVER_IP = "echo.websocket.org";
    private static final String SERVER_IP = "46.101.97.34";
    private String SERVER_PORT = "9999";

    private ServerIOActivity callbackActivity;
    private NetClient nc;
    private String toSend;
    private boolean messageAvailable;

    public SocketHandler() {

        Log.i("Websocket", "initialized without callback!");
        this.execute();
    }

    public void sendMessage(final EncodeMessage messageToSend) {

        Thread sendingThread = new Thread(new Runnable() {
            public void run() {
                nc.sendDataWithString(messageToSend.toJSONString());
            }
        });

        sendingThread.start();

    }

    public void setCallbackActivity(ServerIOActivity callbackActivity) {
        this.callbackActivity = callbackActivity;
    }

    //cancel async, reinitalize & restart
    /*public void resetPort(String port){

        this.cancel(true);
        SERVER_PORT = port;
        try {
            uri = new URI(SERVER_IP+SERVER_PORT);
            this.execute();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }*/

    @Override
    protected DecodeMessage doInBackground(Void... params) {

        nc = new NetClient(SERVER_IP, Integer.parseInt(SERVER_PORT));

        String message = "";
        int charsRead = 0;
        char[] buffer = new char[512];

        try {
            while (true) {

                //get message
                while (!messageAvailable && (charsRead = nc.getBufferedReader().read(buffer)) != -1) {
                    message += new String(buffer).substring(0, charsRead);
                    if (message.substring(message.length() - 2).equals("\\q")) {
                        DecodeMessage decodeMessage = new DecodeMessage(message.substring(0, message.length() - 2));
                        publishProgress(decodeMessage);

                        //reset values
                        message = "";
                        charsRead = 0;
                        Arrays.fill(buffer, Character.MIN_VALUE);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

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
