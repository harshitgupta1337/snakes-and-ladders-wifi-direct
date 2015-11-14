package org.sca2015.teenpatti.utils;

/**
 * Created by hp on 12-11-2015.
 */
public class Card {

    public static int SPADE = 1;
    public static int CLUB = 2;
    public static int DIAMOND = 3;
    public static int HEART = 4;

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
    public static int ACE = 18;

    public static String getCardName(int suit, int number){
        String card = "";
        if(suit==1)
            card = card+"Spade ";
        if(suit==2)
            card = card+"Club ";
        if(suit==3)
            card = card+"Diamond ";
        if(suit==4)
            card = card+"Heart ";

        if(number==6)
            card = card+"2";
        if(number==7)
            card = card+"3";
        if(number==8)
            card = card+"4";
        if(number==9)
            card = card+"5";
        if(number==10)
            card = card+"6";
        if(number==11)
            card = card+"7";
        if(number==12)
            card = card+"8";
        if(number==13)
            card = card+"9";
        if(number==14)
            card = card+"10";
        if(number==15)
            card = card+"J";
        if(number==16)
            card = card+"Q";
        if(number==17)
            card = card+"K";
        if(number==18)
            card = card+"A";


        return card;
    }

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
