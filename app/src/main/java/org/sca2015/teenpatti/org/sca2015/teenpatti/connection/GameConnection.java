package org.sca2015.teenpatti.org.sca2015.teenpatti.connection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;
import org.sca2015.teenpatti.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class GameConnection {
    private ServerSocket serverSocket;
    Handler mHandler;
    MainActivity mainActivity;

    public GameConnection(Handler mHandler, MainActivity mainActivity){
        this.mHandler = mHandler;
        this.mainActivity = mainActivity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public void sendMessage(String ip, String msg) {
        MyClientTask myClientTask = new MyClientTask(ip, 8080, msg);
        myClientTask.execute();
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

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);

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
