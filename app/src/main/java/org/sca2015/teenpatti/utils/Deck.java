package org.sca2015.teenpatti.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp on 12-11-2015.
 */
public class Deck {
    public static List<Card> cards = new ArrayList<Card>();

    public static void initialize(){
        int i,j;
        for(i=Card.SPADE;i<=Card.HEART;i++){
            for(j=Card.ACE;j<=Card.KING;j++){
                cards.add(new Card(i,j));
            }
        }
    }

    public static List<Card> shuffle(int numCards){
        int N = cards.size();
        List<Card> cardsCopy = new ArrayList<Card>(cards);

        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
            Card temp = new Card(cardsCopy.get(i).getSuit(), cardsCopy.get(i).getNumber());
            cardsCopy.get(i).setSuit(cardsCopy.get(r).getSuit());
            cardsCopy.get(i).setNumber(cardsCopy.get(r).getNumber());

            cardsCopy.get(r).setSuit(temp.getSuit());
            cardsCopy.get(r).setNumber(temp.getNumber());
        }

        return cardsCopy.subList(0, numCards);
    }

}
