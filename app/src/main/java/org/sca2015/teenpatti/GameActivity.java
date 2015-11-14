package org.sca2015.teenpatti;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.InputType;
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
import org.sca2015.teenpatti.utils.ShowCardsDialog;
import org.sca2015.teenpatti.utils.SideShowCardsDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AbstractActivity {

    public static int INITIAL_BET = 100;
    public static int INITIAL_AMOUNT = 30*INITIAL_BET;

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
    int balance;
    int currentBet;
    int pot;
    boolean twoPlayersRemaining;
    public List<Card> getMyCards() {
        return myCards;
    }

    public void setMyCards(List<Card> myCards) {
        this.myCards = myCards;
    }

    List<Card> myCards;
    List<Card> sideShowCards;
    String sideShowSender;

    List<Card> showCards;
    String showSender;

    Map<Integer, Boolean> isPlayerInGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        pot = 0;
        isPlayerInGame = new HashMap<Integer, Boolean>();
        ((Button)findViewById(R.id.betButton)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.passButton)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.passButton)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.showButton)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.foldButton)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.sideShowButton)).setVisibility(View.INVISIBLE);

        currentGameState = new HashMap<String, Integer>();
        canMove = true;
        myTurn = false;
        twoPlayersRemaining = false;
        balance = INITIAL_AMOUNT;
        currentBet = INITIAL_BET;
        myCards = new ArrayList<Card>();
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
                for(int i=0;i<peers.size();i++)
                    isPlayerInGame.put(i, true);
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
                peerToMove = getNextPeerToMove(0);
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

        Button foldButton = (Button)findViewById(R.id.foldButton);
        foldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fold();
            }
        });

        Button sideShowButton = (Button)findViewById(R.id.sideShowButton);
        sideShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSideShow();
            }
        });

        Button showButton = (Button)findViewById(R.id.showButton);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestShow();
            }
        });

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String msgStr = msg.getData().getString("msg");
                //Toast.makeText(GameActivity.this, "MSG : "+msgStr,
                 //       Toast.LENGTH_SHORT).show();
                String sender = msg.getData().getString("sender").substring(1);
                processMessage(msgStr, sender);
            }
        };
        gameConnection = new GameConnection(8081, mUpdateHandler, this);

        if(!isDealer())
            ((TextView)findViewById(R.id.dealButton)).setVisibility(View.INVISIBLE);

        Deck.initialize();

        //peerToMove = getNextPeerToMove(0);
    }

    private void fold() {
        gameConnection.sendFold(groupOwnerIp);
        toggleTurn(false);
    }

    private int getNextPeerToMove(int start){
        for(int i=0;i<peers.size();i++){
            int id = (start+i)%peers.size();
            if(isPlayerInGame.get(id))
                return id;
        }
        return -1;
    }

    public void sideShowResult(boolean attempted, boolean won){
        if(attempted){
            if(won){
                balance += 2*currentBet;
                updateMoneyView();
            }else{
                balance -= 2*currentBet;
                updateMoneyView();
            }

            gameConnection.sendSideShowResult(groupOwnerIp, attempted, won);
        }else{
            gameConnection.sendSideShowResult(groupOwnerIp, attempted, won);
        }
    }

    private void requestSideShow() {
        if(balance >= 2*currentBet)
            gameConnection.sendSideShowRequest(groupOwnerIp, this.myCards);
        else
            showToast("You don't have sufficient balance for a side show.");
    }

    private void requestShow() {
        gameConnection.sendShowRequest(groupOwnerIp, this.myCards);
    }

    private void bet() {
        int amount=-1;
        String betString = ((TextView)findViewById(R.id.betView)).getText().toString();
        try{
            amount = Integer.parseInt(betString);
            if(amount > 2*this.currentBet || amount < this.currentBet){
                return;
            }
        } catch(NumberFormatException e){
            showToast("Please enter an amount between "+this.currentBet+" and "+2*this.currentBet);
            return;
        }
        balance -= amount;
        if(!isDealer())
            pot += amount;
        gameConnection.sendBet(groupOwnerIp, amount);
        updateMoneyView();
        toggleTurn(false);
    }

    private void updateMoneyView(){
        ((TextView)findViewById(R.id.moneyView)).setText("Balance : " + balance + " ; Pot : " + pot);
    }

    private void pass() {
        balance -= currentBet;
        if(!isDealer())
            pot += currentBet;
        gameConnection.sendPass(groupOwnerIp);
        updateMoneyView();
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
        if(id == R.id.leave_game){
            if(myTurn)
                leaveGame();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void leaveGame() {
        if(isDealer()){

        }else{
            toggleTurn(false);
            gameConnection.sendIAmLeaving(groupOwnerIp);
        }
    }

    private void deal(){
        List<Card> cards = Deck.shuffle(3 * peers.size());
        Button dealButton = (Button)findViewById(R.id.dealButton);
        dealButton.setVisibility(View.INVISIBLE);
        pot = 0;
        for(String peer : peers)
            pot += INITIAL_BET;
        sendCardsToPeers(cards);
    }

    private void sendCardsToPeers(List<Card> cards){
        List<Card> cardsForPeer = new ArrayList<Card>();
        int i=0;
        for(Card card : cards){
            i++;
            cardsForPeer.add(card);
            if(i%3==0){
                gameConnection.sendDealtCards(cardsForPeer, pot, peers.get((i/3)-1));
                cardsForPeer = new ArrayList<Card>();
            }
        }
    }

    private void updateGameState(String msg) throws JSONException {
        JSONObject reader = new JSONObject(msg);
        int currentBet = reader.getInt("CURRENT_BET");
        this.currentBet = currentBet;
        int pot = reader.getInt("POT");
        this.pot = pot;
        updateMoneyView();
        ((TextView)findViewById(R.id.currentBetView)).setText("Current Bet : "+currentBet);
        ((TextView)findViewById(R.id.betView)).setText("" + currentBet);
        ((TextView)findViewById(R.id.betView)).setCursorVisible(true);
        ((TextView)findViewById(R.id.betView)).setFocusableInTouchMode(true);
        ((TextView)findViewById(R.id.betView)).setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView)findViewById(R.id.betView)).requestFocus();

        boolean gameOn = reader.getBoolean("GAME_ON");

        if(!gameOn){
            toggleTurn(false);
            initializeDealtCards();
            if(isDealer()) {
                peerToMove = 0;
                for (int i = 0; i < peers.size(); i++)
                    isPlayerInGame.put(i, true);
            }
        }
    }

    private void initializeDealtCards() {
        myCards = new ArrayList<Card>();
        ((TextView)findViewById(R.id.card0)).setText("Card 0");
        ((TextView)findViewById(R.id.card1)).setText("Card 1");
        ((TextView)findViewById(R.id.card2)).setText("Card 2");
        if(isDealer())
            ((Button)findViewById(R.id.dealButton)).setVisibility(View.VISIBLE);
    }

    @Override
    void processMessage(String msg, String sender) {
        try {
            JSONObject reader = new JSONObject(msg);
            String messageType = reader.getString("TYPE");
            if(messageType.equals("STATE")){
                handleGameState(reader.getString("DATA"), sender);
            } else if(messageType.equals("DEALT_CARDS"))
                updateDealtCards(reader.getJSONArray("CARDS"), reader.getInt("POT"));
            else if(messageType.equals("TURN")){
                executeTurn(msg);
            } else if(messageType.equals("MOVE"))
                handleMove(msg);
            else if (messageType.equals(Constants.GAME_STATE))
                updateGameState(msg);
            else if (messageType.equals(Constants.SIDE_SHOW_REQUEST))
                performSideShow(msg, sender);
            else if (messageType.equals(Constants.SIDE_SHOW_CARDS))
                presentSideShowCards(msg, sender);
            else if(messageType.equals(Constants.SIDE_SHOW_RESULT))
                handleSideShowResult(sender, reader.getBoolean("ATTEMPTED"), reader.getBoolean("WON"));
            else if(messageType.equals(Constants.GAME_LEAVING_MSG))
                handleGameLeavingMsg(reader.getInt("AMOUNT"));
            else if(messageType.equals(Constants.SIDE_SHOW_WON_NOTIFICATION))
                handleSideShowWonNotification();
            else if(messageType.equals(Constants.TWO_PLAYERS_REM))
                twoPlayersRemaining();
            else if(messageType.equals(Constants.SHOW_REQUEST))
                performShow(reader.getJSONArray("CARDS"), sender);
            else if (messageType.equals(Constants.SHOW_CARDS))
                presentShowCards(msg, sender);
            else if(messageType.equals(Constants.SHOW_RESULT))
                handleShowResult(sender, reader.getBoolean("ATTEMPTED"), reader.getBoolean("WON"));
            else if(messageType.equals(Constants.SHOW_WON_NOTIFICATION))
                handleShowWonNotification();
            else if(messageType.equals(Constants.I_AM_LEAVING))
                handlePlayerLeavingGame(sender);
            else if(messageType.equals(Constants.YOU_WIN))
                IWin(reader.getInt("POT"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void IWin(int pot) {
        showToast("I WIN");
        this.balance += pot;
        this.pot = 0;
        updateMoneyView();
    }

    private void handlePlayerLeavingGame(String sender) {
        List<String> newPeers = new ArrayList<String>();
        Map<Integer, Boolean> newIsPlayerInGame = new HashMap<Integer, Boolean>();
        int i = 0, senderId = 0, j=0;
        for(String peer : peers){
            if(!peer.equals(sender)){
                newPeers.add(peer);
                newIsPlayerInGame.put(i, isPlayerInGame.get(j));
                i++;
            }else{
                senderId = i;
            }
            j++;
        }
        peers = newPeers;
        isPlayerInGame = newIsPlayerInGame;
        peerToMove = getNextPeerToMove(senderId);

        checkForTwoRemainingPlayers();
        checkForOneRemainingPlayer();

        gameConnection.sendTurnInfo(currentBet, peers.get(peerToMove));

    }

    private void handleShowWonNotification() {
        balance += pot;
        updateMoneyView();
    }

    private void handleShowResult(String sender, boolean attempted, boolean won) {
        if(attempted){
            pot = 0;
            if(won){
                userLeavesGame(showSender, 0);
            }else{
                gameConnection.sendWonShowNotification(showSender);
                userLeavesGame(sender, 0);
            }

            for(String peer : peers)
                gameConnection.sendGameState(currentBet, peer, pot, false);

        }else{
        }
    }

    private void presentShowCards(String msg, String sender) throws JSONException {
        JSONObject reader = new JSONObject(msg);
        JSONArray cards = reader.getJSONArray("CARDS");

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ShowCardsDialog showCardsDialog = ShowCardsDialog.newInstance(cards.toString(), sender);

        showCardsDialog.show(ft, "dialog");
    }

    private void performShow(JSONArray cards, String sender) throws JSONException {
        int cardSuite, cardNumber;
        showCards = new ArrayList<Card>();
        cardSuite = ((JSONObject)cards.get(0)).getInt(Constants.CARD_SUITE);
        cardNumber = ((JSONObject)cards.get(0)).getInt(Constants.CARD_NUMBER);
        showCards.add(new Card(cardSuite, cardNumber));

        cardSuite = ((JSONObject)cards.get(1)).getInt(Constants.CARD_SUITE);
        cardNumber = ((JSONObject)cards.get(1)).getInt(Constants.CARD_NUMBER);
        showCards.add(new Card(cardSuite, cardNumber));

        cardSuite = ((JSONObject)cards.get(2)).getInt(Constants.CARD_SUITE);
        cardNumber = ((JSONObject)cards.get(2)).getInt(Constants.CARD_NUMBER);
        showCards.add(new Card(cardSuite, cardNumber));

        showSender = sender;

        //int previousPeer = (peerToMove==0)?(peers.size()-1):(peerToMove-1);
        int previousPeer = getPreviousPlayerInGame(peerToMove);

        gameConnection.sendShowCards(peers.get(previousPeer), showCards, showSender);
    }

    private void twoPlayersRemaining() {
        twoPlayersRemaining = true;
        //((Button)findViewById(R.id.sideShowButton)).setVisibility(View.INVISIBLE);
        //((Button)findViewById(R.id.showButton)).setVisibility(View.VISIBLE);
    }

    private void handleSideShowWonNotification() {
        balance += 2*currentBet;
        updateMoneyView();
    }

    private void handleGameLeavingMsg(int amount) {
        balance -= amount;
        toggleTurn(false);
    }

    private void handleSideShowResult(String sender, boolean attempted, boolean won) {
        if(attempted){
            if(won){
                userLeavesGame(sideShowSender, 2*currentBet);
            }else{
                gameConnection.sendWonSideShowNotification(sideShowSender);
                userLeavesGame(sender, 0);
            }

            int i;
            for(i=0;i<peers.size();i++){
                if(sideShowSender.equals(peers.get(i)))
                    break;
            }
            peerToMove = getNextPeerToMove((i+1)%peers.size());
            gameConnection.sendTurnInfo(currentBet, peers.get(peerToMove));

        }else{

        }
    }

    private void userLeavesGame(String sideShowSender, int amount) {
        for(int i=0;i<peers.size();i++){
            if(peers.get(i).equals(sideShowSender))
                isPlayerInGame.put(i, false);
        }
        gameConnection.sendGameLeavingMsg(sideShowSender, amount);

        checkForTwoRemainingPlayers();
        checkForOneRemainingPlayer();

    }

    private void checkForOneRemainingPlayer() {
        int playerCount = 0;
        for(int i=0;i<peers.size();i++){
            if(isPlayerInGame.get(i)){
                playerCount++;
            }
        }
        if(playerCount == 1){
            for(int i=0;i<peers.size();i++){
                if(isPlayerInGame.get(i)){
                    gameConnection.sendYouWin(peers.get(i), pot);
                    pot = 0;
                    for(String peer : peers){
                        gameConnection.sendGameState(currentBet, peer, 0, false);
                    }
                }
            }
        }
    }

    private void checkForTwoRemainingPlayers(){
        int playerCount = 0;
        for(int i=0;i<peers.size();i++){
            if(isPlayerInGame.get(i)){
                playerCount++;
            }
        }
        if(playerCount == 2){
            for(int i=0;i<peers.size();i++){
                if(isPlayerInGame.get(i)){
                    gameConnection.sendTwoPlayersRemainingMsg(peers.get(i));
                }
            }
        }
    }

    private void presentSideShowCards(String msg, String sender) throws JSONException {
        JSONObject reader = new JSONObject(msg);
        JSONArray cards = reader.getJSONArray("CARDS");

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        SideShowCardsDialog sideShowCardsDialog = SideShowCardsDialog.newInstance(cards.toString(), sender);

        sideShowCardsDialog.show(ft, "dialog");
    }

    private void performSideShow(String msg, String sender) throws JSONException {
        JSONObject reader = new JSONObject(msg);
        JSONArray cards = reader.getJSONArray("CARDS");
        int cardSuite, cardNumber;
        sideShowCards = new ArrayList<Card>();
        cardSuite = ((JSONObject)cards.get(0)).getInt(Constants.CARD_SUITE);
        cardNumber = ((JSONObject)cards.get(0)).getInt(Constants.CARD_NUMBER);
        sideShowCards.add(new Card(cardSuite, cardNumber));

        cardSuite = ((JSONObject)cards.get(1)).getInt(Constants.CARD_SUITE);
        cardNumber = ((JSONObject)cards.get(1)).getInt(Constants.CARD_NUMBER);
        sideShowCards.add(new Card(cardSuite, cardNumber));

        cardSuite = ((JSONObject)cards.get(2)).getInt(Constants.CARD_SUITE);
        cardNumber = ((JSONObject)cards.get(2)).getInt(Constants.CARD_NUMBER);
        sideShowCards.add(new Card(cardSuite, cardNumber));

        sideShowSender = sender;

        //int previousPeer = (peerToMove==0)?(peers.size()-1):(peerToMove-1);
        int previousPeer = getPreviousPlayerInGame(peerToMove);

        gameConnection.sendSideShowCards(peers.get(previousPeer), sideShowCards, sideShowSender);
    }

    private int getPreviousPlayerInGame(int currentId){
        for(int i=1;i<peers.size();i++){
            int id = (currentId+peers.size()-i)%peers.size();

            if(isPlayerInGame.get(id))
                return id;
        }
        return -1;
    }

    private void executeTurn(String msg) throws JSONException {
        toggleTurn(true);
        JSONObject reader = new JSONObject(msg);
        int currentBet = reader.getInt("CURRENT_BET");
        this.currentBet = currentBet;
        ((TextView)findViewById(R.id.currentBetView)).setText("Current Bet : "+currentBet);
        ((TextView)findViewById(R.id.betView)).setText(""+currentBet);
        ((TextView)findViewById(R.id.betView)).setCursorVisible(true);
        ((TextView)findViewById(R.id.betView)).setFocusableInTouchMode(true);
        ((TextView)findViewById(R.id.betView)).setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView)findViewById(R.id.betView)).requestFocus();
    }

    private void handleMove(String msg) throws JSONException {
        JSONObject reader = new JSONObject(msg);

        if(reader.getString("MOVE").equals("BET"))
            handleBet(reader.getInt("AMOUNT"));
        else if(reader.getString("MOVE").equals("PASS"))
            handlePass();
        else if(reader.getString("MOVE").equals("FOLD"))
            handleFold();


        showToast(isPlayerInGame.toString());

        try{
            peerToMove = getNextPeerToMove((peerToMove + 1)%peers.size());
        }catch(Exception e){
            showToast("Exception in peerToMove()"+isPlayerInGame);
        }

        for(String peer : peers)
            gameConnection.sendGameState(currentBet, peer, pot, true);

        gameConnection.sendTurnInfo(currentBet, peers.get(peerToMove));
        showToast("Sending turn info to " + peers.get(peerToMove));
    }

    private void handleFold() {
        userLeavesGame(peers.get(peerToMove), 0);
    }

    private void handleBet(int amount) {
        currentBet = amount;
        pot += currentBet;
    }

    private void handlePass() {
        pot += currentBet;
    }

    private void toggleTurn(boolean enable) {
        if (enable)
            showToast("MY TURN");
        myTurn = enable;
        ((Button)findViewById(R.id.betButton)).setVisibility((enable)?View.VISIBLE:View.INVISIBLE);
        ((TextView)findViewById(R.id.betView)).setVisibility((enable)?View.VISIBLE:View.INVISIBLE);
        ((Button)findViewById(R.id.passButton)).setVisibility((enable)?View.VISIBLE:View.INVISIBLE);
        ((Button)findViewById(R.id.sideShowButton)).setVisibility((enable)?View.VISIBLE:View.INVISIBLE);
        ((Button)findViewById(R.id.showButton)).setVisibility((enable && twoPlayersRemaining)?View.VISIBLE:View.INVISIBLE);
        ((Button)findViewById(R.id.foldButton)).setVisibility((enable) ? View.VISIBLE : View.INVISIBLE);
//        ((MenuItem)findViewById(R.id.leave_game)).setVisible(enable);
    }

    private void updateDealtCards(JSONArray cards, int pot) {
        this.pot = pot;
        twoPlayersRemaining = false;
        myCards = new ArrayList<Card>();
        int cardSuite = 0, cardNumber;
        try {
            cardSuite = ((JSONObject)cards.get(0)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cards.get(0)).getInt(Constants.CARD_NUMBER);
            ((TextView)findViewById(R.id.card0)).setText(Card.getCardName(cardSuite, cardNumber));
            myCards.add(new Card(cardSuite, cardNumber));

            cardSuite = ((JSONObject)cards.get(1)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cards.get(1)).getInt(Constants.CARD_NUMBER);
            ((TextView)findViewById(R.id.card1)).setText(Card.getCardName(cardSuite, cardNumber));
            myCards.add(new Card(cardSuite, cardNumber));

            cardSuite = ((JSONObject)cards.get(2)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cards.get(2)).getInt(Constants.CARD_NUMBER);
            ((TextView)findViewById(R.id.card2)).setText(Card.getCardName(cardSuite, cardNumber));
            myCards.add(new Card(cardSuite, cardNumber));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        balance -= INITIAL_BET;

        updateMoneyView();
    }

    private void handleGameState(String gameState, String sender){

    }

    public boolean isDealer() {
        return isDealer;
    }

    public void setIsDealer(boolean isDealer) {
        this.isDealer = isDealer;
    }

    public void showResult(boolean attempted, boolean won) {
        if(attempted){
            if(won){
                showToast("YOU WON SHOW.");
                balance += pot;
                updateMoneyView();
            }else{
                showToast("YOU LOST SHOW.");
                updateMoneyView();
            }

            gameConnection.sendShowResult(groupOwnerIp, attempted, won);
        }else{
            gameConnection.sendShowResult(groupOwnerIp, attempted, won);
        }
    }
}
