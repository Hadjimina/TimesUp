package com.example.philipp.timesup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


abstract public class ServerIOActivity extends AppCompatActivity{

    private boolean isIntent;
    abstract public void callback(DecodeMessage message);

    public void sendMessage(EncodeMessage message){
        NetworkHelper.handler.sendMessage(message);
    }

    public void setCallbackActivity(ServerIOActivity activity){
        NetworkHelper.handler.setCallbackActivity(activity);
    }

    @Override
    public void onBackPressed() {

        //Dont do anything if on start activity back is pressed
        if (this instanceof StartActivity)
            return;

        //Show alert if some error occured
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Exit Game");

        alertDialogBuilder.setMessage("Are you sure you want to return to the start activity?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        isIntent = true;

                        //go to start activity
                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        if(!isIntent){
            NetworkHelper.handler.cancel(true);
            NetworkHelper.handler.disconnect();
        }
        isIntent = false;

        //TODO Send disconnect message
        super.onDestroy();
    }
}
