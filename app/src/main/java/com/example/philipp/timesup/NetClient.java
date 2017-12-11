package com.example.philipp.timesup;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
                Log.i("Websocket","connected to server");

            }
        } catch (IOException e) {
            //TODO add error message
            e.printStackTrace();
        }
    }

    public boolean isConnected(){
        return socket.isConnected();
    }

    public BufferedReader getBufferedReader() {
        return in;
    }


    public void disConnectWithServer() {
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                    Log.i("Websocket","disconnected from server");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendDataWithString(String message) {
        if (message != null) {




            try {
                //prepend size of string (padded)
                String length = String.valueOf((message).getBytes("UTF-8").length);
                length = String.format("%4s", length);

                out.write(length + message);
                out.flush();
                Log.i("Websocket", "Sending: "+message);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }



        }
    }


}