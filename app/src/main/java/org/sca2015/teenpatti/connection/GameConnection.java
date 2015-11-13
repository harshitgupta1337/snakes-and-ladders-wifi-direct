package org.sca2015.teenpatti.connection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sca2015.teenpatti.AbstractActivity;
import org.sca2015.teenpatti.utils.Card;
import org.sca2015.teenpatti.utils.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class GameConnection {
    int port;
    private ServerSocket serverSocket;
    Handler mHandler;
    AbstractActivity activity;

    public GameConnection(int port, Handler mHandler, AbstractActivity abstractActivity){
        this.port = port;
        this.mHandler = mHandler;
        this.activity = abstractActivity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public void sendMessage(String ip, String msg) {
        MyClientTask myClientTask = new MyClientTask(ip, port, msg);
        myClientTask.execute();
    }

    public void sendBet(String groupOwnerIp, int amount){
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", "MOVE");
            initMsg.put("MOVE", "BET");
            initMsg.put("AMOUNT", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(groupOwnerIp, initMsg.toString());
    }
    public void sendPass(String groupOwnerIp){
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", "MOVE");
            initMsg.put("MOVE", "PASS");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(groupOwnerIp, initMsg.toString());
    }

    public void sendControlMessage(String action, String destAddr) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", "CONTROL");
            initMsg.put("ACTION", action);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendMessage(destAddr, initMsg.toString());
    }

    public void sendDealtCards(List<Card> cards, String destAddr){
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", "DEALT_CARDS");
            JSONArray arr = new JSONArray();
            for(Card card : cards){
                JSONObject obj = new JSONObject();
                obj.put(Constants.CARD_SUITE, card.getSuit());
                obj.put(Constants.CARD_NUMBER, card.getNumber());
                arr.put(obj);
            }
            initMsg.put("CARDS", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        activity.showToast("Sending msg "+initMsg.toString()+" to "+destAddr);
        sendMessage(destAddr, initMsg.toString());
    }

    public void sendTurnInfo(int currentBet, String destAddr){
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", "TURN");
            initMsg.put("CURRENT_BET", currentBet);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(destAddr, initMsg.toString());
    }

    private class SocketServerThread extends Thread {

        //static final int SocketServerPORT = port;
        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);

                while (true) {
                    Socket socket = serverSocket.accept();
                    count++;
                    String input = new BufferedReader(new InputStreamReader(
                            socket.getInputStream())).readLine();
                    String msg = input;
                    Bundle messageBundle = new Bundle();
                    messageBundle.putString("msg", msg);
                    messageBundle.putString("sender", socket.getInetAddress().toString());

                    Message message = new Message();
                    message.setData(messageBundle);
                    mHandler.sendMessage(message);
                    /*SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();*/

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {
        String destAddr;
        int port;
        String message;

        public MyClientTask(String addr, int port, String message) {
            this.destAddr = addr;
            this.port = port;
            this.message = message;
        }


        @Override
        protected Void doInBackground(Void... params) {
            Socket socket = null;
            try {
                socket = new Socket(destAddr, port);

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(message);
                out.flush();

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
            return null;
        }
    }
}
