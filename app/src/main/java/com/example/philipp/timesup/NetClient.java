package com.example.philipp.timesup;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetClient {

    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private String host = null;

    private int port = 9999;


    /**
     * Constructor with Host, Port and MAC Address
     *
     * @param host
     * @param port
     */
    public NetClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.connectWithServer();
    }

    private void connectWithServer() {
        try {
            if (socket == null) {
                socket = new Socket(this.host, this.port);
                out = new PrintWriter(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
        } catch (IOException e) {
            //TODO add error message
            e.printStackTrace();
        }
    }

    public BufferedReader getBufferedReader() {
        return in;
    }


    private void disConnectWithServer() {
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendDataWithString(String message) {
        if (message != null) {


            //prepend size of string (padded)
            String length = String.valueOf(message.length());
            length = String.format("%4s", length);

            out.write(length + message);
            out.flush();
            Log.i("Websocket sending", message);


        }
    }


}