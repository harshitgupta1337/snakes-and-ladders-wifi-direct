package org.sca2015.teenpatti.utils;

/**
 * Created by hp on 12-11-2015.
 */
public class Card {

    public static int SPADE = 1;
    public static int CLUB = 2;
    public static int DIAMOND = 3;
    public static int HEART = 4;

    public static int ACE = 5;
    public static int TWO = 6;
    public static int THREE = 7;
    public static int FOUR = 8;
    public static int FIVE = 9;
    public static int SIX = 10;
    public static int SEVEN = 11;
    public static int EIGHT = 12;
    public static int NINE = 13;
    public static int TEN = 14;
    public static int JACK = 15;
    public static int QUEEN = 16;
    public static int KING = 17;

    int suit;
    int number;

    public Card(int suit, int number){
        this.suit = suit;
        this.number = number;
    }

    public int getSuit() {
        return suit;
    }

    public void setSuit(int suit) {
        this.suit = suit;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
