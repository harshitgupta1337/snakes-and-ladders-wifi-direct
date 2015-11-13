package org.sca2015.teenpatti;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sca2015.teenpatti.connection.GameConnection;
import org.sca2015.teenpatti.utils.Card;
import org.sca2015.teenpatti.utils.Constants;
import org.sca2015.teenpatti.utils.Deck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AbstractActivity {

    public static int INITIAL_BET = 100;
    public static int INITIAL_AMOUNT = 500;

    String id;
    String groupOwnerIp;
    Map<String, Integer> currentGameState;
    boolean canMove;
    boolean myTurn;
    List<String> peers;
    boolean isDealer;
    private Handler mUpdateHandler;
    GameConnection gameConnection;
    int peerToMove;
    int amount;
    int currentBet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ((Button)findViewById(R.id.betButton)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.passButton)).setVisibility(View.INVISIBLE);

        currentGameState = new HashMap<String, Integer>();
        canMove = false;
        myTurn = false;
        amount = INITIAL_AMOUNT;
        currentBet = INITIAL_BET;
        Intent intent = getIntent();
        setIsDealer(intent.getBooleanExtra(Constants.IS_DEALER, false));
        groupOwnerIp = intent.getStringExtra(Constants.GROUP_OWNER_IP);
        if(isDealer()){
            peers = new ArrayList<String>();
            peers.add("127.0.0.1");
            String peersStr = intent.getStringExtra(Constants.PEERS_LIST);
            try {
                JSONObject reader = new JSONObject(peersStr);
                JSONArray peersListArr = reader.getJSONArray("peers");
                for(int i=0;i<peersListArr.length();i++){
                    peers.add(peersListArr.getString(i));
                }
                //showToast(peers.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else{
            peers = null;
        }

        Button dealButton = (Button)findViewById(R.id.dealButton);
        dealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deal();
                peerToMove = 0;
                gameConnection.sendTurnInfo(currentBet, peers.get(peerToMove));
            }
        });
        Button betButton = (Button)findViewById(R.id.betButton);
        betButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bet();
            }
        });
        Button passButton = (Button)findViewById(R.id.passButton);
        passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass();
            }
        });

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String msgStr = msg.getData().getString("msg");
                Toast.makeText(GameActivity.this, "MSG : "+msgStr,
                        Toast.LENGTH_SHORT).show();
                String sender = msg.getData().getString("sender").substring(1);
                processMessage(msgStr, sender);
            }
        };
        gameConnection = new GameConnection(8081, mUpdateHandler, this);

        if(!isDealer())
            ((TextView)findViewById(R.id.dealButton)).setVisibility(View.INVISIBLE);

        Deck.initialize();

        peerToMove = 0;

    }

    private void bet() {
        gameConnection.sendBet(groupOwnerIp, 100);
        toggleTurn(false);
    }

    private void pass() {
        gameConnection.sendPass(groupOwnerIp);
        toggleTurn(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
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

    private void deal(){
        showToast("Inside deal()");
        List<Card> cards = Deck.shuffle(3 * peers.size());
        sendCardsToPeers(cards);
        updateGameState();
        sendGameState();
    }

    private void sendCardsToPeers(List<Card> cards){
        List<Card> cardsForPeer = new ArrayList<Card>();
        int i=0;
        for(Card card : cards){
            i++;
            cardsForPeer.add(card);
            if(i%3==0){
                gameConnection.sendDealtCards(cardsForPeer, peers.get((i/3)-1));
                cardsForPeer = new ArrayList<Card>();
            }
        }
    }

    private void sendGameState(){

    }

    private void updateGameState(){
        // UI Code
    }

    @Override
    void processMessage(String msg, String sender) {
        try {
            JSONObject reader = new JSONObject(msg);
            String messageType = reader.getString("TYPE");
            if(messageType.equals("STATE")){
                handleGameState(reader.getString("DATA"), sender);
            } else if(messageType.equals("DEALT_CARDS"))
                updateDealtCards(reader.getJSONArray("CARDS"));
            else if(messageType.equals("TURN")){
                executeTurn(msg);
            } else if(messageType.equals("MOVE"))
                handleMove(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void executeTurn(String msg) throws JSONException {
        toggleTurn(true);
        JSONObject reader = new JSONObject(msg);
        int currentBet = reader.getInt("CURRENT_BET");
        ((TextView)findViewById(R.id.currentBetView)).setText("Current Bet : "+currentBet);
        ((TextView)findViewById(R.id.betView)).setText(""+currentBet);
    }

    private void handleMove(String msg) throws JSONException {
        JSONObject reader = new JSONObject(msg);

        if(reader.getString("MOVE").equals("BET"))
            handleBet(reader.getInt("AMOUNT"));
        else if(reader.getString("MOVE").equals("PASS"))
            handlePass();

        peerToMove = (peerToMove + 1)%peers.size();

        gameConnection.sendTurnInfo(currentBet, peers.get(peerToMove));
        showToast("Sending turn info to "+peers.get(peerToMove));
    }

    private void handleBet(int amount) {
    }

    private void handlePass() {
    }

    private void toggleTurn(boolean enable) {
        if (enable)
            showToast("MY TURN");
        ((Button)findViewById(R.id.betButton)).setVisibility((enable)?View.VISIBLE:View.INVISIBLE);
        ((Button)findViewById(R.id.passButton)).setVisibility((enable)?View.VISIBLE:View.INVISIBLE);
    }

    private void updateDealtCards(JSONArray cards) {
        int cardSuite = 0, cardNumber;
        try {
            cardSuite = ((JSONObject)cards.get(0)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cards.get(0)).getInt(Constants.CARD_NUMBER);
            ((TextView)findViewById(R.id.card0)).setText(cardSuite+"\t"+cardNumber);

            cardSuite = ((JSONObject)cards.get(1)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cards.get(1)).getInt(Constants.CARD_NUMBER);
            ((TextView)findViewById(R.id.card1)).setText(cardSuite+"\t"+cardNumber);

            cardSuite = ((JSONObject)cards.get(2)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cards.get(2)).getInt(Constants.CARD_NUMBER);
            ((TextView)findViewById(R.id.card2)).setText(cardSuite+"\t"+cardNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void handleGameState(String gameState, String sender){

    }

    public boolean isDealer() {
        return isDealer;
    }

    public void setIsDealer(boolean isDealer) {
        this.isDealer = isDealer;
    }
}
