package com.example.philipp.timesup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class SocketHandler extends AsyncTask<Void, DecodeMessage, DecodeMessage> {

    private static String SERVERIP = NetworkHelper.SERVERIP;
    private static String SERVERPORT = NetworkHelper.SERVERPORT;

    private ServerIOActivity callbackActivity;
    private NetClient nc;

    public SocketHandler() {
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

    public void disconnect(){
        this.cancel(true);
        if(nc != null){
            Log.i("Websocket","disconnecting");
            nc.disConnectWithServer();

        }else{
            Log.i("Websocket","tried to disconnect even though no connection was established");
        }
    }

    public boolean isConnected(){
        if(nc != null){
            return nc.isConnected();
        }
        return false;
    }

    public void setCallbackActivity(ServerIOActivity callbackActivity) {
        this.callbackActivity = callbackActivity;
    }

    // (re)initialize netClient
    public void initNetClient(String port){

        if(isConnected()){
            Log.e("Websocket","Sockethandler reinitialized! Are you really really sure you want this?");
        }

        SERVERPORT = port;
        nc = new NetClient(SERVERIP, Integer.parseInt(SERVERPORT));
    }


    @Override
    protected DecodeMessage doInBackground(Void... params) {

        initNetClient(SERVERPORT);

        String message = "";
        int charsRead = 0;
        char[] buffer = new char[512];

        try {
            while (true) {

                //get message
                while (nc.getBufferedReader() != null && (charsRead = nc.getBufferedReader().read(buffer)) != -1) {
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

        //Check if some error occured
        if(values[0].getRequestType().equals(NetworkHelper.ERROR)){
            try {
                Toast.makeText(callbackActivity, values[0].getBody().getString(NetworkHelper.ERRORType),
                            Toast.LENGTH_LONG).show();
                Log.e("Websocket", "Error Type: "+ values[0].getBody().getString(NetworkHelper.ERRORType));

                //Show alert if some error occured
                AlertDialog.Builder builder = new AlertDialog.Builder(callbackActivity);
                builder.setMessage("The following error occured: "+values[0].getBody().getString(NetworkHelper.ERRORType))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //restart app if an error occured
                                Intent i = callbackActivity.getPackageManager()
                                        .getLaunchIntentForPackage( callbackActivity.getPackageName() );
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                callbackActivity.startActivity(i);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        Log.i("Websocket", "Got message: "+values[0].getRawString());
        callbackActivity.callback(values[0]);
    }

}
