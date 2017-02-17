/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package com.VoiceAdventures.demo;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.os.Handler;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import android.media.MediaPlayer;

@SuppressWarnings("unused")

public class MainActivity extends Activity implements
        RecognitionListener {

    private final int REQ_CODE_SPEECH_INPUT = 100;

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PHONE_SEARCH = "phones";
    private static final String MENU_SEARCH = "menu";
    private static final String COMMAND_SEARCH = "command";


    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "okay";

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    private Handler h = new Handler();


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
//        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(MENU_SEARCH, R.string.menu_caption);
        captions.put(COMMAND_SEARCH, R.string.command_caption);
        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
                .setText("Preparing the recognizer");

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(com.VoiceAdventures.demo.MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(COMMAND_SEARCH);
                    playGame();

                }
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
    }
    
    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    private boolean humanIsTalking;

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
    	    return;
        humanIsTalking = true;
        String text = hypothesis.getHypstr();
        ((TextView) findViewById(R.id.result_text)).setText(text);

//        if (text.equals(KEYPHRASE)) {
//            recognizer.stop();
//            ((TextView) findViewById(R.id.result_text)).setText(text);
//}
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            System.out.println("Result: " + text);
            ((TextView) findViewById(R.id.result_text)).setText("");
//        ((TextView) findViewById(R.id.result_text)).setText(text);
            parseText(text);
        }
    }
    private String fillers = "uh|um|a|with|to|the|and";
    private ArrayList<String> filler = new ArrayList<>(Arrays.asList(fillers.split("|")));
    private String sceneDescription = "On the floor in front of you the fallen oil painting lays face-down. On the wall there is a faded square where it used to hang with a bent nail hanging askew from its hole. A small, but ornate side table holds up a ceramic pot filled with the remains of a plant that rotted long, long ago.\n";
    private Scene activeScene = new Scene(0, "", R.raw.ag_001, sceneDescription, R.raw.ag_002);

    private class Scene {
        private String intro;
        private int introAudio;
        private int atmosphere;
        private String description;
        private int descriptionAudio;

        private List<GameObjects> objects;
        public String getDescription(){
            return description;
        }
        public int getIntroAudio(){
            return introAudio;
        }
        public int getDescriptionAudioAudio(){
            return descriptionAudio;
        }


        public Scene(int atmosphereP, String introP, int introAudioP, String descriptionP, int descriptionAudioP ){
            atmosphere = atmosphereP;
            intro = introP;
            introAudio =  introAudioP;
            description = descriptionP;
            descriptionAudio = descriptionAudioP;

        }
    }

    private class Inventory {
        private List<GameObjects> objects;
    }


    private class GameObjects {
        private String name;
        private int nameAudio;
        private String description;
        private int descriptionAudio;
        private Boolean obtainable;
        private Boolean seen = false;
        private List<HashMap> actions;
    }


    protected void parseText(String text){
        System.out.println("Parsing text");
//        makeText(getApplicationContext(),
//                getString(R.string.command_caption),
//                Toast.LENGTH_LONG).show();
        List<String> separated = Arrays.asList( text.split(" "));
        ArrayList<String> commands = new ArrayList<>(separated);
        commands.removeAll(filler);
        if (commands.get(0).equalsIgnoreCase("look")){
            if(commands.size() > 1){
                if (commands.get(1).equalsIgnoreCase("around")){
                    setCaptions(activeScene.getDescription());
                    playAudio(activeScene.descriptionAudio);
                }else{
                    for (i)
                        commands.get(1)
                }

            }else{
                setCaptions(activeScene.getDescription());
                playAudio(activeScene.descriptionAudio);

            }

        }else if (commands.get(0).equalsIgnoreCase("play")){
            if (commands.get(1).equalsIgnoreCase("charlie")) {
                ((TextView) findViewById(R.id.caption_text))
                        .setText("Playing Charlie");
            }else if(commands.get(1).equalsIgnoreCase("caroline")){
                ((TextView) findViewById(R.id.caption_text))
                        .setText("Playing Caroline");
            }else{
                ((TextView) findViewById(R.id.caption_text))
                        .setText(text);
            }

        }else{
            ((TextView) findViewById(R.id.caption_text))
                    .setText(text);
        }

    }

    MediaPlayer mP;

    private void playAudio(int audio){
        mP = MediaPlayer.create(this, audio);
        mP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    recognizer.startListening(COMMAND_SEARCH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mP.start();
    }
    private void playGame(){
        recognizer.stop();
        Boolean game  = true;
        mP = MediaPlayer.create(this, activeScene.getIntroAudio());
        mP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    recognizer.startListening(COMMAND_SEARCH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mP.start();

//        setCaptions("Your eyes blink open as the world around you comes into focus. You are in a small, four cornered room. You don’t remember how you got here. Come to think of it, you don’t know who you are at all. As you reach back into your mind to uncover the missing details…");
//        h.postDelayed(runnableSetCaptions("(A loud thump)"), 10000);
//        h.postDelayed(runnableSetCaptions("An oil painting has fallen off the wall in front of you. For the first time you notice the space. You snap out of your reverie. The room is sparsely decorated with an antique style reminiscent of a time you can’t quite place.\n"), 11000);mP
    }
//
//    class myMedia {
//        MediaPlayer()
//
//    }
//
//    mP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//        @Override
//        public void onCompletion(MediaPlayer mp) {
//            try {
//                recognizer.startListening(COMMAND_SEARCH);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    });

    private Runnable runnableSetCaptions(final String caption){

        Runnable aRunnable = new Runnable(){
            public void run(){
                setCaptions(caption);
            }
        };

        return aRunnable;

    }

    private void setCaptions(String caption){
        ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }



    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (humanIsTalking){
            recognizer.stop();
            humanIsTalking = false;
        }
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        
        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(COMMAND_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }


    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                
//                // To disable logging of raw audio comment out this call (takes a lot of space on the device)
//                .setRawLogDir(assetsDir)
                
                // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-1f)
                
                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)
                
                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

//        // Create keyword-activation search.
//        recognizer.addKeyphraseSearch(COMMAND_SEARCH, KEYPHRASE);
//
        // Create grammar-based search for selection between demos
        File commandGrammar = new File(assetsDir, "commands.gram");
        recognizer.addGrammarSearch(COMMAND_SEARCH, commandGrammar);

//
        // Create language model search
//        File languageModel = new File(assetsDir, "en-us.lm.bin");
//        recognizer.addNgramSearch(COMMAND_SEARCH, languageModel);

//        // Phonetic search
//        File phoneticModel = new File(assetsDir, "en-phone.dmp");
//        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(COMMAND_SEARCH);
    }
}
