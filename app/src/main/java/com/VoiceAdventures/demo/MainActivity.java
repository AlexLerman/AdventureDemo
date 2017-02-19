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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import android.media.MediaPlayer;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.jetbrains.annotations.Nullable;

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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


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
                    Assets assets = new Assets(MainActivity.this);
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

    private String fillerString = "uh,um,a,with,to,the,and";
    private ArrayList<String> fillers = new ArrayList<>(Arrays.asList(fillerString.split(",")));
    private String sceneDescription = "On the floor in front of you the fallen oil painting lays face-down. On the wall there is a faded square where it used to hang with a bent nail hanging askew from its hole. A small, but ornate side table holds up a ceramic pot filled with the remains of a plant that rotted long, long ago.\n";

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    private class Scene {
        private String intro;
        private int introAudio;
        private int atmosphere;
        private String description;
        private int descriptionAudio;

        private ArrayList<Item> objects;

        public String getDescription() {
            return description;
        }

        public int getIntroAudio() {
            return introAudio;
        }

        public int getDescriptionAudioAudio() {
            return descriptionAudio;
        }


        public Scene(int atmosphereP, String introP, int introAudioP,
                     String descriptionP, int descriptionAudioP, ArrayList<Item> objectsP) {
            atmosphere = atmosphereP;
            intro = introP;
            introAudio = introAudioP;
            description = descriptionP;
            descriptionAudio = descriptionAudioP;
            objects = objectsP;
        }

        public ArrayList<Item> getObjects() {
            return objects;
        }
    }


    private ArrayList<Item> inventory;


    private class Item {
        private String name;
        private ArrayList<String> aliases;
        private int nameAudio;
        private String description;
        private int descriptionAudio;
        private Boolean obtainable;
        private Boolean seen = false;
        private List<HashMap> actions;

        public Item(String nameP, ArrayList<String> aliasesP, int nameAudioP, String descriptionP, int descriptionAudioP, Boolean obtainableP, Boolean seenP) {
            name = nameP;
            aliases = aliasesP;
            nameAudio = nameAudioP;
            description = descriptionP;
            descriptionAudio = descriptionAudioP;
            obtainable = obtainableP;
            seen = seenP;
        }

        public ArrayList<String> getAliases() {
            return aliases;
        }

        public int getDescriptionAudio() {
            return descriptionAudio;
        }

        public String getDescription() {
            return description;
        }
    }

    private Scene activeScene;

    private void generateScene() {
        ArrayList<String> nailAliases = new ArrayList<>();
        nailAliases.add("nail");

        Item nail = new Item("nail", nailAliases, 0,
                "The nail is rusty and bent. Strange, though, that it gave in to gravity at the same time you appeared.\n", R.raw.ag_005, true, true);

        ArrayList<Item> sceneObjects = new ArrayList<>();
        sceneObjects.add(nail);

        activeScene = new Scene(0, "", R.raw.ag_001, sceneDescription, R.raw.ag_002, sceneObjects);
    }

    //Action consequences:
    // Change scene (go) |      [changeScene, sceneName]
    // add to inventory, subtract from scene (take)
    // add/subtract [items+] to/from scene (open, look at, use, combine, pick up)  | [[add, [items]] [subtract, [items]]
    // add/subtract [items+] to/from inventory (open, look at, use, combine)
    // change permissions (lock, unlock)
    // make visible [items+]  (look around) |
    //

    @Nullable
    private Item findGameObject(Scene aS, String name) {
        ArrayList<Item> sO = aS.getObjects();
        for (int i = 0; i < sO.size(); i++) {
            ArrayList<String> oA = sO.get(i).getAliases();
            for (int j = 0; j < oA.size(); j++) {
                if (name.equalsIgnoreCase(oA.get(j))) {
                    return sO.get(i);
                }
            }
        }
        return null;
    }

    private void examine(Item item){
        if (item != null) {
            setCaptions(item.getDescription());
            playAudio(item.getDescriptionAudio());
        }
    }
//    private void take(Item item){
//        if (item != null){
//            if (item.obtainable){
//                inventory.add(item);
//                activeScene.setObjects(activeScene.getObjects().remove(item));
//            }
//            if (item.hasTakenConsequence()){
//                setCaptions(item.getTakenConsequence());
//                playAudio(item.getTakenConsequenceAudio());
//            }
//
//        }
//    }

    private void use(){

    }

    private void combine(){

    }

    class Command{
        private String command;
        private Item item1;
        private Item item2;

        public Command(String commandP, Item item1P, Item item2P){
            command = commandP;
            item1 = item1P;
            item2 = item2P;
        }

        public String getCommand(){
            return command;
        }

        @Nullable
        public Item getItem1(){
            return item1;
        }

        @Nullable
        public Item getItem2(){
            return item2;
        }

        public Command(String commandP, Item item1P){
            command = commandP;
            item1 = item1P;
        }

        public Command(String commandP){
            command = commandP;
        }
    }

    private String commandsString = "open,close,pick,take,look,inspect,examine,use,combine,go";
    private ArrayList<String> validCommands = new ArrayList<>(Arrays.asList(commandsString.split(",")));

    private Command parseCommand(ArrayList<String> commands) {
        if(validCommands.contains(commands.get(0))){
            System.out.println("valid command");
            if (commands.size() == 3) {
                if (commands.get(1).equalsIgnoreCase("at") || commands.get(1).equalsIgnoreCase("up")) {
                    Item item = findGameObject(activeScene, commands.get(2));
                    return new Command(TextUtils.join(" ", commands.subList(0, 2)), item);
                } else {
                    if (commands.get(0).equalsIgnoreCase("use") || commands.get(0).equalsIgnoreCase("combine")) {
                        Item item1 = findGameObject(activeScene, commands.get(1));
                        Item item2 = findGameObject(activeScene, commands.get(2));
                        return new Command("combine", item1, item2);
                    }
                }
                return new Command("invalid");
            } else if (commands.size() > 3) {
                return new Command("invalid");
            } else if (commands.get(0).equalsIgnoreCase("look") && (commands.size() == 1 || commands.get(1).equalsIgnoreCase("around"))) {
                return new Command("look around");
            }
            Item item = findGameObject(activeScene, commands.get(1));
            return new Command(commands.get(0), item);
        }
        return new Command("invalid");
    }


    private void parseText(String text) {
        System.out.println("Parsing text");

//        makeText(getApplicationContext(),
//                getString(R.string.command_caption),
//                Toast.LENGTH_LONG).show();
        List<String> separated = Arrays.asList(text.split(" "));
        System.out.print(separated);
        System.out.print(fillers);
        ArrayList<String> commands = new ArrayList<>(separated);
        System.out.println(commands);

        commands.removeAll(fillers);
        Command com = parseCommand(commands);
        System.out.println("Command: " + com.getCommand());
        switch (com.getCommand()){
            case "look around":
                setCaptions(activeScene.getDescription());
                playAudio(activeScene.descriptionAudio);
                break;
            case "examine":
            case "look at":
            case "inspect":
                examine(com.getItem1());
                break;
//            case "pick up":
//            case "take":
//                break;
//            case "open":
//                break;
//            case "close":
//                break;
//            case "play":
//            case "eat":
//            case "enter:
//            case "use": //using one item
//                break;
//            case "combine": //using/combining two items
//                break;
            default:
                setCaptions("Invalid command");
                break;

        }
    }

    MediaPlayer mP;

    private void playAudio(int audio) {
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

    private void playGame() {
        recognizer.stop();
        final EditText edittext = (EditText) findViewById(R.id.inputText);
        generateScene();

        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mP.isPlaying()){
                        mP.stop();
                    }
                    if (recognizer != null){
                        recognizer.stop();
                    }
                    parseText(edittext.getText().toString());
                    edittext.setText("");
                    return true;
                }
                return false;
            }
        });
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

    private Runnable runnableSetCaptions(final String caption) {

        Runnable aRunnable = new Runnable() {
            public void run() {
                setCaptions(caption);
            }
        };

        return aRunnable;

    }

    private void setCaptions(String caption) {
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
        if (humanIsTalking) {
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
