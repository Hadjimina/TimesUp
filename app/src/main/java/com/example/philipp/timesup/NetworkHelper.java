package com.example.philipp.timesup;

/**
 * Created by MammaGiulietta on 16.11.17.
 */

/**
 * All static variables for the messages are saved here
 * */

public class NetworkHelper {

    public static String ERROR = "error";

    public static String ACK = "ACK";

    public static String JOIN = "join";

    public static String NEWGAME = "newGame";

    public static String TEAMJOIN = "teamJoin";

    public static String READY = "ready";

    public static String UNREADY = "unready";

    public static String SETUP = "setup";

    public static String STARTROUND = "startRound";

    public static String ROUNDFINISHED = "roundFinished";

    public static String NEXTROUND = "nextRound";

    public static String MYPREFS = "myPrefs";

    public static String WORDSARRAY = "wordsArray";

    public static SocketHandler handler;

    public void setCallback(ServerIOActivity activity){
        handler.setCallbackActivity(activity);
    }
}
