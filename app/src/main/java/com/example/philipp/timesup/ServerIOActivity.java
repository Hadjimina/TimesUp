package com.example.philipp.timesup;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


abstract public class ServerIOActivity extends AppCompatActivity{

    private boolean isIntent;
    abstract public void callback(DecodeMessage message);

    public void sendMessage(EncodeMessage message){
        NetworkHelper.handler.sendMessage(message);
    }

    public void setCallbackActivity(ServerIOActivity activity){
        if (NetworkHelper.handler == null){
            NetworkHelper.handler = new SocketHandler();
        }
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

        alertDialogBuilder.setMessage("Are you sure you want to return to the star screen?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        isIntent = true;



                        doRestart(getApplicationContext());
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

    public void doRestart(Context c) {
        Intent intent = new Intent(c, StartActivity.class);
        intent.putExtra("isRestart", true);
        startActivity(intent);

        String TAG = "Restart";
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e(TAG, "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e(TAG, "Was not able to restart application, PM null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }
    }

    public void disconnect(){
        NetworkHelper.handler.cancel(true);
        NetworkHelper.handler.disconnect();
    }
    }
