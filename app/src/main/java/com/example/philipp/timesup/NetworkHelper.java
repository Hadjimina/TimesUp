package com.example.philipp.timesup;

/**
 * Created by MammaGiulietta on 16.11.17.
 */

/**
 * All static variables for the messages are saved here
 * */

public class NetworkHelper {

    public static String ERROR = "error";

    public static String ERRORType = "errorType";

    public static String ACK = "ACK";

    public static String JOIN = "join";

    public static String NEWGAME = "newGame";

    public static String TEAMJOIN = "joinTeam";

    public static String READY = "ready";

    public static String UNREADY = "unready";

    public static String SETUP = "setup";

    public static String STARTROUND = "startRound";

    public static String ROUNDFINISHED = "roundFinished";

    public static String NEXTROUND = "nextRound";

    public static String MYPREFS = "myPrefs";

    public static String WORDSARRAY = "wordsArray";

    public static String SERVERIP = "46.101.97.34";

    public static String SERVERPORT = "9999";

    public static SocketHandler handler;

    //game values

    public static int GAMEID;

    public static int CLIENTID;

    public static int TIMEPERROUND;

    public static int WORDSPERPERSON;

    public static String TEAMNAME1;

    public static String TEAMNAME2;

    public static int BELONGSTOTEAM; //teamId of the chosen Team

    public static String USERNAME;

    public static int TEAMID1 = 1;

    public static int TEAMID2 = 2;

    public static boolean[] ROUNDS;

    public static String[] WORDS;

    public void setCallback(ServerIOActivity activity){
        handler.setCallbackActivity(activity);
    }
}
