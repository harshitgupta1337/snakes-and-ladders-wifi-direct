package org.sca2015.teenpatti;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.sca2015.teenpatti.org.sca2015.teenpatti.connection.GameConnection;

import java.util.HashMap;
import java.util.Map;

public class GameActivity extends AbstractActivity {
    String id;
    Map<String, Integer> currentGameState;
    boolean canMove;
    boolean myTurn;
    private Handler mUpdateHandler;
    GameConnection gameConnection;
    Map<Integer, Integer> snakesAndLadders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        currentGameState = new HashMap<String, Integer>();
        canMove = false;
        snakesAndLadders = new HashMap<Integer, Integer>();
        myTurn = false;

        Button rollDiceButton = (Button)findViewById(R.id.rollDiceButton);
        rollDiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myTurn)
                    makeMove();
            }
        });

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String msgStr = msg.getData().getString("msg");
                Toast.makeText(GameActivity.this, "MSG : "+msgStr,
                        Toast.LENGTH_SHORT).show();
                String sender = msg.getData().getString("sender").substring(1);
                //processMessage(msgStr, sender);
            }
        };
        //gameConnection = new GameConnection(8081, mUpdateHandler, this);


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

    public void showToast(String text){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private int checkSnakeAndLadder(int nextPosition){
        if(snakesAndLadders.containsKey(nextPosition))
            return snakesAndLadders.get(nextPosition);
        else
            return nextPosition;
    }

    private void makeMove(){
        int diceRoll = (int) (Math.random()*6.0 + 1);

        if(canMove){
            int currentPosition = currentGameState.get(id);
            int nextPosition = currentPosition + diceRoll;
            int finalPosition = checkSnakeAndLadder(nextPosition);
            currentGameState.put(id, finalPosition);
            updateGameState();
        } else{
            if(diceRoll == 6){
                canMove = true;
                showToast("You can move now.");
            }
        }

        sendGameState();
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleGameState(String gameState, String sender){
        
    }
}
