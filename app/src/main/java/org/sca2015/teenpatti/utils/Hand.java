package org.sca2015.teenpatti.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hp on 13-11-2015.
 */
public class Hand {

    public static int THREE_OF_A_KIND = 5;
    public static int STRAIGHT_RUN = 4;
    public static int NORMAL_RUN = 3;
    public static int SAME_SUIT = 2;
    public static int PAIR = 1;
    public static int NO_CLASS = 0;

    public static int isThreeOfAKind(List<Card> cards) {
        Card card0 = cards.get(0);
        Card card1 = cards.get(1);
        Card card2 = cards.get(2);
        if (card0.getNumber() == card1.getNumber() && card1.getNumber() == card2.getNumber())
            return card0.getNumber();
        return -1;
    }

    public static int isStraightRun(List<Card> cards) {
        final Card card0 = cards.get(0);
        final Card card1 = cards.get(1);
        final Card card2 = cards.get(2);
        if (card0.getSuit() == card1.getSuit() && card1.getSuit() == card2.getSuit()) {
            List<Integer> cardList = new ArrayList<Integer>() {{
                add(card0.getNumber());
                add(card1.getNumber());
                add(card2.getNumber());
            }};
            Collections.sort(cardList);
            if (cardList.get(0) + 1 == cardList.get(1) && cardList.get(1) + 1 == cardList.get(2))
                return cardList.get(2);
        }
        return -1;
    }

    public static int isNormalRun(List<Card> cards) {
        final Card card0 = cards.get(0);
        final Card card1 = cards.get(1);
        final Card card2 = cards.get(2);
        List<Integer> cardList = new ArrayList<Integer>() {{
            add(card0.getNumber());
            add(card1.getNumber());
            add(card2.getNumber());
        }};
        Collections.sort(cardList);
        if (cardList.get(0) + 1 == cardList.get(1) && cardList.get(1) + 1 == cardList.get(2))
            return cardList.get(2);
        return -1;
    }

    public static int isSameSuite(List<Card> cards) {
        final Card card0 = cards.get(0);
        final Card card1 = cards.get(1);
        final Card card2 = cards.get(2);
        if (card0.getSuit() == card1.getSuit() && card1.getSuit() == card2.getSuit()) {
            return 1;
        }
        return -1;
    }

    public static int isPair(List<Card> cards) {
        final Card card0 = cards.get(0);
        final Card card1 = cards.get(1);
        final Card card2 = cards.get(2);

        if (card0.getNumber() == card1.getNumber())
            return card0.getNumber();
        if(card1.getNumber() == card2.getNumber())
            return card1.getNumber();
        if(card0.getNumber() == card2.getNumber())
            return card0.getNumber();
        return -1;
    }

    public static int getKindOfHand(List<Card> cards){
        if(isThreeOfAKind(cards) > 0)
            return THREE_OF_A_KIND;
        if(isStraightRun(cards) > 0)
            return STRAIGHT_RUN;
        if(isNormalRun(cards) > 0)
            return NORMAL_RUN;
        if(isSameSuite(cards) > 0)
            return SAME_SUIT;
        if(isPair(cards) > 0)
            return PAIR;
        return NO_CLASS;
    }

    public static int compare(final List<Card> cards0, final List<Card> cards1){
        if(getKindOfHand(cards0) == getKindOfHand(cards1)){
            if(getKindOfHand(cards0) == THREE_OF_A_KIND){
                return (isThreeOfAKind(cards0) > isThreeOfAKind(cards1))?1:-1;
            } else if (getKindOfHand(cards0) == STRAIGHT_RUN){
                return (isStraightRun(cards0) > isStraightRun(cards1))?1:-1;
            } else if (getKindOfHand(cards0) == NORMAL_RUN){
                return (isNormalRun(cards0) > isNormalRun(cards1))?1:-1;
            } else if (getKindOfHand(cards0) == SAME_SUIT){
                List<Integer> cardList0 = new ArrayList<Integer>() {{
                    add(cards0.get(0).getNumber());
                    add(cards0.get(1).getNumber());
                    add(cards0.get(2).getNumber());
                }};
                Collections.sort(cardList0);
                List<Integer> cardList1 = new ArrayList<Integer>() {{
                    add(cards1.get(0).getNumber());
                    add(cards1.get(1).getNumber());
                    add(cards1.get(2).getNumber());
                }};
                Collections.sort(cardList1);

                if(cardList0.get(2) == cardList1.get(2)){
                    if(cardList0.get(1) == cardList1.get(1)){
                        if (cardList0.get(0) == cardList1.get(0)){
                            return 0;
                        } else
                            return (cardList0.get(0) > cardList1.get(0))?1:-1;
                    } else
                        return (cardList0.get(1) > cardList1.get(1))?1:-1;
                } else
                    return (cardList0.get(2) > cardList1.get(2))?1:-1;

            } else if (getKindOfHand(cards0) == PAIR){
                if(isPair(cards0) == isPair(cards1)){
                    int sum0 = 0, sum1 = 0;
                    for(Card card : cards0)
                        sum0 += card.getNumber();
                    for(Card card : cards1)
                        sum1 += card.getNumber();
                    return (sum0 > sum1)?1:-1;
                } else{
                    return (isPair(cards0)>isPair(cards1))?1:-1;
                }
            } else{
                // NO CLASS FOR BOTH HANDS
                List<Integer> cardList0 = new ArrayList<Integer>() {{
                    add(cards0.get(0).getNumber());
                    add(cards0.get(1).getNumber());
                    add(cards0.get(2).getNumber());
                }};
                Collections.sort(cardList0);
                List<Integer> cardList1 = new ArrayList<Integer>() {{
                    add(cards1.get(0).getNumber());
                    add(cards1.get(1).getNumber());
                    add(cards1.get(2).getNumber());
                }};
                Collections.sort(cardList1);

                if(cardList0.get(2) == cardList1.get(2)){
                    if(cardList0.get(1) == cardList1.get(1)){
                        if (cardList0.get(0) == cardList1.get(0)){
                            return 0;
                        } else
                            return (cardList0.get(0) > cardList1.get(0))?1:-1;
                    } else
                        return (cardList0.get(1) > cardList1.get(1))?1:-1;
                } else
                    return (cardList0.get(2) > cardList1.get(2))?1:-1;
            }
        } else{
            return (getKindOfHand(cards0) > getKindOfHand(cards1))?1:-1;
        }
    }

}