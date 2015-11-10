package org.sca2015.teenpatti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sca2015.teenpatti.org.sca2015.teenpatti.connection.GameConnection;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AbstractActivity implements WifiP2pManager.PeerListListener {
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private static final String TAG = "MainActivity";
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    private Handler mUpdateHandler;
    GameConnection gameConnection;
    private final IntentFilter mIntentFilter = new IntentFilter();
    TpConnectionInfoListener mConnectionInfoListener;
    private boolean isOwner;
    private List<String> connectedPeers;

    public void showToast(String text){
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        Log.d(TAG, mManager.toString());
        Log.d(TAG, mChannel.toString());
        mReceiver = new TpBroadcastReceiver(mManager, mChannel, MainActivity.this);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mConnectionInfoListener = new TpConnectionInfoListener(this);
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String msgStr = msg.getData().getString("msg");
                Toast.makeText(MainActivity.this, "MSG : "+msgStr,
                        Toast.LENGTH_SHORT).show();
                String sender = msg.getData().getString("sender").substring(1);
                processMessage(msgStr, sender);
            }
        };
        gameConnection = new GameConnection(8080, mUpdateHandler, this);
        connectedPeers = new ArrayList<String>();

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

    }

    private void startGame(){
        Intent openGameActivity = new Intent("org.sca2015.teenpatti.GAME");
        if(isOwner()) {
            for (String peer : connectedPeers)
                gameConnection.sendControlMessage("START_GAME", peer);
            JSONObject peers = new JSONObject();
            JSONArray peerArr = new JSONArray();
            for (String peer : connectedPeers)
                peerArr.put(peer);
            try {
                peers.put("peers", peerArr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            openGameActivity.putExtra("peers", peers.toString());
        }

        openGameActivity.putExtra("isOwner", isOwner());
        startActivity(openGameActivity);
    }

    private void handleControlMessage(String function, String sender){
        showToast("Got control msg : "+function);
        if(function.equals("INIT")){
            if(!connectedPeers.contains(sender))
                connectedPeers.add(sender);
            updateConnectedPeers();
        }else if(function.equals("START_GAME")){
            showToast("START GAME RECEIVED");
            startGame();
        }
    }

    @Override
    public void processMessage(String msg, String sender){
        try {
            JSONObject reader = new JSONObject(msg);
            String messageType = reader.getString("TYPE");
            if(messageType.equals("CONTROL")){
                handleControlMessage(reader.getString("ACTION"), sender);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateConnectedPeers(){
        TextView textView = ((TextView) findViewById(R.id.text1));
        textView.setText("Peers:");
        for(String peer : connectedPeers){
            textView.setText(textView.getText()+peer+"\n");
        }
    }

    public void sendConnectionInitMessage(String groupOwnerIp){
        gameConnection.sendControlMessage("INIT", groupOwnerIp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public WifiP2pManager.Channel getmChannel() {
        return mChannel;
    }

    public void setmChannel(WifiP2pManager.Channel mChannel) {
        this.mChannel = mChannel;
    }

    public WifiP2pManager getmManager() {
        return mManager;
    }

    public void setmManager(WifiP2pManager mManager) {
        this.mManager = mManager;
    }

    public BroadcastReceiver getmReceiver() {
        return mReceiver;
    }

    public void setmReceiver(BroadcastReceiver mReceiver) {
        this.mReceiver = mReceiver;
    }

    public TpConnectionInfoListener getmConnectionInfoListener() {
        return mConnectionInfoListener;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        TextView tv = (TextView)findViewById(R.id.hello_world);
        tv.setText("");
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        for(WifiP2pDevice wifiP2pDevice : peers){
            tv.setText(tv.getText() + wifiP2pDevice.deviceName);
        }
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        ((TextView)findViewById(R.id.text1)).setVisibility((isOwner)?View.VISIBLE:View.INVISIBLE);
        ((Button)findViewById(R.id.button)).setVisibility((isOwner)?View.VISIBLE:View.INVISIBLE);
        this.isOwner = isOwner;

    }

}
