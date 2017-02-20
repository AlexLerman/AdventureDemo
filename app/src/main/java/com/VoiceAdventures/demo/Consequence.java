package com.VoiceAdventures.demo;

import java.util.ArrayList;

/**
 * Created by alexlerman on 2/19/17.
 */

public class Consequence {
    private String result = null;
    private ArrayList<String> items = null;
    private ArrayList<TimedText> timedText = null;
    private int audio = -1;

    public ArrayList getItems() {
        return items;
    }

    public int getAudio() {
        return audio;
    }

    public ArrayList<TimedText>  getTimedText() {
        return timedText;
    }

    public String getResult() {
        return result;
    }

    public Consequence(String resultP, ArrayList<String> itemsP, ArrayList<TimedText>  textP, int audioP){
        items = itemsP;
        audio = audioP;
        timedText = textP;
        result = resultP;
    }
    public Consequence(String resultP, ArrayList<String> itemsP){
        items = itemsP;
        result = resultP;
    }

    public Consequence(String resultP, ArrayList<String> itemsP, int audioP){
        items = itemsP;
        result = resultP;
        audio = audioP;
    }


    public Consequence(ArrayList<TimedText>  textP, int audioP){
        audio = audioP;
        timedText = textP;
    }

    public Consequence(int audioP){
        audio = audioP;
    }
}
