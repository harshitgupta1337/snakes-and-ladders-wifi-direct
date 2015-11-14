package org.sca2015.teenpatti.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sca2015.teenpatti.GameActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp on 13-11-2015.
 */
public class SideShowCardsDialog extends DialogFragment{

    List<Card> cards;
    String requester;

    public static SideShowCardsDialog newInstance(String cards, String requester) {
        SideShowCardsDialog sideShowCardsDialog = new SideShowCardsDialog();

        Bundle args = new Bundle();
        args.putString("CARDS", cards);
        args.putString("REQUESTER", requester);
        sideShowCardsDialog.setArguments(args);

        return sideShowCardsDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        boolean winning = false;

        JSONArray cardsJson = null;
        try {
            cardsJson = new JSONArray(getArguments().getString("CARDS"));
            int cardSuite, cardNumber;
            cards = new ArrayList<Card>();
            cardSuite = ((JSONObject)cardsJson.get(0)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cardsJson.get(0)).getInt(Constants.CARD_NUMBER);
            cards.add(new Card(cardSuite, cardNumber));

            cardSuite = ((JSONObject)cardsJson.get(1)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cardsJson.get(1)).getInt(Constants.CARD_NUMBER);
            cards.add(new Card(cardSuite, cardNumber));

            cardSuite = ((JSONObject)cardsJson.get(2)).getInt(Constants.CARD_SUITE);
            cardNumber = ((JSONObject)cardsJson.get(2)).getInt(Constants.CARD_NUMBER);
            cards.add(new Card(cardSuite, cardNumber));

            List<Card> myCards = ((GameActivity)getActivity()).getMyCards();

            if(Hand.compare(myCards, cards) > 0){
                winning = true;
            }else{
                winning = false;
            }

            requester = getArguments().getString("REQUESTER");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((GameActivity)getActivity()).showToast("Cards : "+cards);
        final boolean finalWinning = winning;
        builder.setMessage("Do you accept ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((GameActivity)getActivity()).sideShowResult(true, finalWinning);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((GameActivity)getActivity()).sideShowResult(false, finalWinning);
                    }
                })
                .setTitle("Side Show Cards");
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
