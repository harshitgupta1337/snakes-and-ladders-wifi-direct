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
import java.util.ArrayList;
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

    public void sendDealtCards(List<Card> cards, int pot, String destAddr){
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
            initMsg.put("POT", pot);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //activity.showToast("Sending msg "+initMsg.toString()+" to "+destAddr);
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

    public void sendGameState(int currentBet, String destAddr, int pot, boolean isGameRunning) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.GAME_STATE);
            initMsg.put("CURRENT_BET", currentBet);
            initMsg.put("POT", pot);
            initMsg.put("GAME_ON", isGameRunning);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(destAddr, initMsg.toString());
    }

    public void sendGameLeavingMsg(String destAddr, int amount){
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.GAME_LEAVING_MSG);
            initMsg.put("AMOUNT", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(destAddr, initMsg.toString());
    }

    public void sendSideShowResult(String groupOwnerIp, boolean attempted, boolean won){
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.SIDE_SHOW_RESULT);
            initMsg.put("ATTEMPTED", attempted);
            initMsg.put("WON", won);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(groupOwnerIp, initMsg.toString());
    }

    public void sendSideShowRequest(String groupOwnerIp, List<Card> myCards) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.SIDE_SHOW_REQUEST);
            JSONArray arr = new JSONArray();
            for(Card card : myCards){
                JSONObject obj = new JSONObject();
                obj.put(Constants.CARD_SUITE, card.getSuit());
                obj.put(Constants.CARD_NUMBER, card.getNumber());
                arr.put(obj);
            }
            initMsg.put("CARDS", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(groupOwnerIp, initMsg.toString());
    }

    public void sendSideShowCards(String destAddr, List<Card> sideShowCards, String sideShowSender) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.SIDE_SHOW_CARDS);
            initMsg.put("REQUESTER", sideShowSender);
            JSONArray arr = new JSONArray();
            for(Card card : sideShowCards){
                JSONObject obj = new JSONObject();
                obj.put(Constants.CARD_SUITE, card.getSuit());
                obj.put(Constants.CARD_NUMBER, card.getNumber());
                arr.put(obj);
            }
            initMsg.put("CARDS", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(destAddr, initMsg.toString());
    }

    public void sendWonSideShowNotification(String destAddr) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.SIDE_SHOW_WON_NOTIFICATION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(destAddr, initMsg.toString());
    }

    public void sendTwoPlayersRemainingMsg(String destAddr) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.TWO_PLAYERS_REM);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(destAddr, initMsg.toString());
    }

    public void sendShowRequest(String groupOwnerIp, List<Card> myCards) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.SHOW_REQUEST);
            JSONArray arr = new JSONArray();
            for(Card card : myCards){
                JSONObject obj = new JSONObject();
                obj.put(Constants.CARD_SUITE, card.getSuit());
                obj.put(Constants.CARD_NUMBER, card.getNumber());
                arr.put(obj);
            }
            initMsg.put("CARDS", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(groupOwnerIp, initMsg.toString());
    }

    public void sendShowCards(String destAddr, List<Card> showCards, String showSender) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.SHOW_CARDS);
            initMsg.put("REQUESTER", showSender);
            JSONArray arr = new JSONArray();
            for(Card card : showCards){
                JSONObject obj = new JSONObject();
                obj.put(Constants.CARD_SUITE, card.getSuit());
                obj.put(Constants.CARD_NUMBER, card.getNumber());
                arr.put(obj);
            }
            initMsg.put("CARDS", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(destAddr, initMsg.toString());
    }

    public void sendShowResult(String groupOwnerIp, boolean attempted, boolean won) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.SHOW_RESULT);
            initMsg.put("ATTEMPTED", attempted);
            initMsg.put("WON", won);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(groupOwnerIp, initMsg.toString());
    }

    public void sendWonShowNotification(String destAddr) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.SHOW_WON_NOTIFICATION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(destAddr, initMsg.toString());
    }

    public void sendFold(String groupOwnerIp) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", "MOVE");
            initMsg.put("MOVE", "FOLD");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(groupOwnerIp, initMsg.toString());
    }

    public void sendIAmLeaving(String groupOwnerIp) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.I_AM_LEAVING);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(groupOwnerIp, initMsg.toString());
    }

    public void sendYouWin(String destAddr, int pot) {
        JSONObject initMsg = new JSONObject();
        try {
            initMsg.put("TYPE", Constants.YOU_WIN);
            initMsg.put("POT", pot);
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
