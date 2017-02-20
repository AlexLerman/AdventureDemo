package com.VoiceAdventures.demo;

/**
 * Created by alexlerman on 2/19/17.
 */

public class TimedText {
    private String text;
    private int time;
    public void TimedText(String txt, int timeP){
        text = txt;
        time = timeP;
    }

    public String getText() {
        return text;
    }

    public int getTime() {
        return time;
    }
}
