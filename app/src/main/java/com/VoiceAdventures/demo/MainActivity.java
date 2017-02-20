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
import java.lang.reflect.Array;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    //Action consequences:
    // Change scene (go) |      [[changeScene, sceneName]]
    // add to inventory, subtract from scene (take)
    // add/subtract [items+] to/from scene (open, look at, use, combine, pick up)  | [[add, [items]], [subtract, [items]]]
    // add/subtract [items+] to/from inventory (open, look at, use, combine)
    // change permissions (lock, unlock) | [[unlock/lock, item, consequenceAudio/Text]]
    // make visible [items+]  (look around, pick up plant) | [[reveal, [items], consequenceAudio/Text]]
    //
    // e.g [[add, [pot]], [reveal, [knife, matchstick], consequenceAudio]]
    // [{add: {items: [pots], consequenceAudio: R.raw.ag_001}}, {reveal: {items: [knife, matchstick], consequenceAudio: R.raw.ag_002 }]


    private void addToScene(ArrayList<String> items){
        for( String object: items){
            activeScene.addObject(findGameObject(allGameObjects, object));
        }
    }

    private void subtractFromScene(ArrayList<String> items){
        for( String object: items){
            activeScene.removeObject(findSceneObject(activeScene, object));
        }
    }

    private void takeFromScene(ArrayList<String> items){
        for( String object: items){
            Item item  = findSceneObject(activeScene, object);
            System.out.println(item.getName());
            activeScene.removeObject(item);
            System.out.println(item.getName());
            System.out.println(inventory);
            inventory.add(item);
            System.out.println(inventory);
            System.out.println(item.getName());
        }
    }

    private void replaceInScene(ArrayList<String> items){
        System.out.println("Replacing...");
        System.out.println(" with " + findGameObject(allGameObjects, items.get(1)).getName());
        System.out.println("Replacing: " + findSceneObject(activeScene, items.get(0)).getName());
        activeScene.replaceObject(findSceneObject(activeScene, items.get(0)), findGameObject(allGameObjects, items.get(1)));
    }

    private void addInventory(ArrayList<String> items){
        for( String object: items){
            inventory.add(findSceneObject(activeScene, object));
        }
    }


    private void subtractInventory(ArrayList<String> items){
        for( String object: items){
            inventory.remove(findSceneObject(activeScene, object));
        }
    }
    private void replaceInventory(ArrayList<String> items){
        Item oldItem = findSceneObject(activeScene, items.get(0));
        Item newItem = findSceneObject(activeScene, items.get(1));
        int index = inventory.indexOf(oldItem);
        inventory.remove(index);
        inventory.add(index, newItem);
    }

//    private void reveal(ArrayList<String> items){
//        for( String object: items){
//            findSceneObject(activeScene, object).reveal();
//        }
//    }

//    private void hide(ArrayList<String> items){
//        for( String object: items){
//            findSceneObject(activeScene, object).hide();
//        }
//    }

//    MediaPlayer consequenceMedia;
//
//    private void playConsequnceAudio(int audio) {
//        consequenceMedia = MediaPlayer.create(this, audio);
//        consequenceMedia.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                try {
//                    executeConsequence();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        mP.start();
//    }
    private void executeConsequences(ArrayList<Consequence> consequenceList){
        //TODO: wait properly for audio to finish before executing next consequence
        if (consequenceList == null){
            setCaptions("That won't work");
            playAudio(R.raw.ag_022);
        }else{
            System.out.println("Length of consequences" + consequenceList.size());
            for (Consequence con : consequenceList) {
                while (mP.isPlaying()) {
                    //do nothing. this is probably the wrong way to do this....
                }
                executeConsequence(con);
            }
        }

    }



    private void executeConsequence(Consequence con) {
        String consequence = con.getResult();
        System.out.println("Consequence: " + consequence);
        if (con.getTimedText() != null) {
            readTimedTextArray(con.getTimedText());
        }
        if (con.getAudio() != -1) {
            playAudio(con.getAudio());
        }
        if (consequence != null) {

            switch (consequence) {
                case "reveal":
                    System.out.println("Reveal: " + con.getItems());

                    addToScene(con.getItems());
                    //changes item's seen field to true
                    break;
                case "hide":
                    System.out.println("Hide: " + con.getItems());

                    subtractFromScene(con.getItems());
                    //changes item's seen field to false
                    break;
                case "addToScene":
                    System.out.println("AddToScene: " + con.getItems());
                    addToScene(con.getItems());
                    break;
                case "subtractFromScene":
                    System.out.println("subtractFromScene: " + con.getItems());
                    subtractFromScene(con.getItems());
                    break;
                case "replaceInScene":
                    System.out.println("Replace: " + con.getItems());
                    replaceInScene(con.getItems());
                    //replaces item with new item
                    break;
                case "addInventory":
                    addInventory(con.getItems());
                    break;
                case "subtractInventory":
                    subtractInventory(con.getItems());
                    break;
                case "replaceInventory":
                    replaceInventory(con.getItems());
                    //replaces item with new item
                    break;
                case "takeFromScene":
                    takeFromScene(con.getItems());
                case "unlock":
                    //                unlock(con.getItems());
                    break;
                case "lock":
                    //                lock(con.getItems());
                    break;
                default:
                    break;
            }
        }
    }

//
//    private JSONArray generateConsequence(String consequence, ArrayList<String> items, int audio, String text){
//        try {
//            return new JSONArray("[{\""+ consequence+ "\": {\"items\": , \"audio\": " + Integer.toString(R.raw.ag_009) + " }}]");
//        }catch(JSONException e){
//            System.out.println(e);
//            return null;
//        }
//    }


    private void readTimedTextArray(ArrayList<TimedText> array){
        setCaptions(array.get(0).getText());
        int delay = 0;
        for (int i = 1; i < array.size(); i++){
            delay +=  array.get(i-1).getTime();
            h.postDelayed(runnableSetCaptions(array.get(i).getText()), delay);
        }
    }



    private class Scene {
        private String intro = null;
        private int introAudio = -1;
        private int atmosphere = -1;
        private String description = null;
        private int descriptionAudio = -1;

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

        public void addObject(Item item){
            objects.add(item);
        }

        public void removeObject(Item item){
            objects.remove(item);
        }

        public void replaceObject(Item oldItem, Item newItem){
            int index = objects.indexOf(oldItem);
            objects.remove(index);
            objects.add(index, newItem);
        }

        public ArrayList<Integer> getDescriptionAudioArray(){
            ArrayList<Integer> audio = new ArrayList<>();
            for (Item i : objects){
                int a = i.getDescriptionAudio();
                if (a != 0 && a != -1){
                    audio.add(a);
                }
            }
            return audio;
        }
    }


    private ArrayList<Item> inventory;
    private ArrayList<Item> allGameObjects;


    private class Item {
        private String name;
        private ArrayList<String> aliases;
        private int nameAudio;
        private String description;
        private int descriptionAudio;
        private Boolean obtainable;
        private Boolean visible = false;
        private HashMap<String, ArrayList<Consequence>> actions;

        public Item(String nameP, ArrayList<String> aliasesP, int nameAudioP, String descriptionP, int descriptionAudioP, Boolean obtainableP, Boolean seenP, HashMap<String, ArrayList<Consequence>> actionsP) {
            name = nameP;
            aliases = aliasesP;
            nameAudio = nameAudioP;
            description = descriptionP;
            descriptionAudio = descriptionAudioP;
            obtainable = obtainableP;
            visible = seenP;
            actions = actionsP;
        }

        public ArrayList<String> getAliases() {
            return aliases;
        }

        public String getName() {
            return name;
        }

        public int getDescriptionAudio() {
            return descriptionAudio;
        }

        public String getDescription() {
            return description;
        }

        public HashMap<String, ArrayList<Consequence>> getActions() {
            return actions;
        }

        public Boolean getObtainable() {
            return obtainable;
        }

        public Boolean isVisible() {
            return visible;
        }

        public void reveal(){
            visible = true;
        }

        public void hide(){
            visible = false;
        }
    }

    private Scene activeScene;


    @Nullable
    private Item findSceneObject(Scene aS, String name) {
        ArrayList<Item> itemList = new ArrayList<>();
        itemList.addAll(aS.getObjects());
        itemList.addAll(inventory);
        for (int i = 0; i < itemList.size(); i++) {
            ArrayList<String> oA = itemList.get(i).getAliases();
            for (int j = 0; j < oA.size(); j++) {
                if (name.equalsIgnoreCase(oA.get(j))) {
                    return itemList.get(i);
                }
            }
        }
        return null;
    }

    @Nullable
    private Item findGameObject(ArrayList<Item> itemList, String name) {
        for (int i = 0; i < itemList.size(); i++) {
            String objName = itemList.get(i).getName();
            if (name.equalsIgnoreCase(objName)) {
                return itemList.get(i);
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


    private void executeCommand(String name, Command command){
        if (command.getItem1() != null) {
            executeConsequences(command.getItem1().getActions().get(name));
        }else{
            playAudio(R.raw.invalid_command);
        }
    }

    class Command{
        private String command;
        private Item item1 = null;
        private Item item2 = null;

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
                    Item item = findSceneObject(activeScene, commands.get(2));
                    return new Command(TextUtils.join(" ", commands.subList(0, 2)), item);
                } else {
                    if (commands.get(0).equalsIgnoreCase("use") || commands.get(0).equalsIgnoreCase("combine")) {
                        Item item1 = findSceneObject(activeScene, commands.get(1));
                        Item item2 = findSceneObject(activeScene, commands.get(2));
                        return new Command("combine", item1, item2);
                    }
                }
                return new Command("invalid");
            } else if (commands.size() > 3) {
                return new Command("invalid");
            } else if (commands.get(0).equalsIgnoreCase("look") && (commands.size() == 1 || commands.get(1).equalsIgnoreCase("around"))) {
                return new Command("look around");
            }
            Item item = findSceneObject(activeScene, commands.get(1));
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
        String command = com.getCommand();
        if (com.getItem1() != null){
            System.out.println("First item: " + com.getItem1().getName());
        }
        if (com.getItem2() != null){
            System.out.println("Second Item: " +  com.getItem2().getName());
        }
        try{
            switch (command) {
                case "look around":
                    if (mP.isPlaying()){
                        mP.stop();
                    }
                    setCaptions(activeScene.getDescription());
//                    playAudio(activeScene.descriptionAudio);
                    playAudioArray(activeScene.getDescriptionAudioArray());
                    break;
                case "examine":
                case "look at":
                case "inspect":
                    executeCommand("inspect", com);
                    break;
                case "pick up":
                case "take":
                    executeCommand("take", com);
                    break;
                case "open":
                    executeCommand("open", com);
                    break;
                case "close":
                    executeCommand("close", com);
                    break;
                case "push":
                    executeCommand("push", com);
                    break;
                case "pull":
                    executeCommand("pull", com);
                    break;
                //            case "play":
                //            case "eat":
                //            case "enter:
                case "use": //using one item
                    executeCommand("use", com);
                    break;
                case "combine": //using/combining two items
                    System.out.println("Combining...");
                    if (com.getItem1() != null && com.getItem2() != null) {
                        ArrayList<Consequence> con = com.getItem1().getActions().get("combine " + com.getItem2().getName());
                        System.out.print("Consequence: ");
                        System.out.println(con);

                        if (con == null) {
                            con = com.getItem2().getActions().get("combine " + com.getItem1().getName());
                            System.out.print("Other consequence: ");
                            System.out.println(con);
                            executeConsequences(con);
                        }
                    }else{
                        setCaptions("Invalid command");
                        playAudio(R.raw.invalid_command);
                    }
                    break;
                default:
                    setCaptions("Invalid command");
                    playAudio(R.raw.invalid_command);
                    break;
            }
        }catch(Exception e) {
            setCaptions("undefined consequence (programmer error)");
            playAudio(R.raw.invalid_command);
            System.out.println(e);
        }
    }

    MediaPlayer mP;
    MediaPlayer mPlayer;


    private void playAudioArray(final ArrayList<Integer> queue){
        System.out.println(queue);
        mPlayer = MediaPlayer.create(this, queue.get(0));
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                queue.remove(0);
                if (queue.size() == 0){
                    try {
                        recognizer.startListening(COMMAND_SEARCH);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    System.out.println("On complete: " + queue);
                    playAudioArray(queue);
                }
            }
        });
        mPlayer.start();
    }


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
        playAudio(activeScene.getIntroAudio());

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
//        switchSearch(COMMAND_SEARCH);
    }

    private ArrayList<String> toArrayList(String[] stringList){
        return new ArrayList<>(Arrays.asList(stringList));
    }


    private ArrayList<Consequence> simpleResponse(int audio){
        Consequence inspectNail = new Consequence(audio);
        ArrayList<Consequence> nailInspectCon = new ArrayList<>();
        nailInspectCon.add(inspectNail);
        return nailInspectCon;
    }
    private void generateScene() {
        Item fallenPainting;
        Item painting;
        Item cutPainting;
        Item emptyPainting;
        Item nail; //
        Item table;//
        Item plant; //
        Item uselessPlant; //
        Item knife; //
        Item certificate;//
        Item wall;

        ArrayList<String> fallenPaintingAliases = new ArrayList<>();
        fallenPaintingAliases.add("painting");
        fallenPaintingAliases.add("fallenPainting");
        ArrayList<String> paintingAliases = new ArrayList<>();
        paintingAliases.add("painting");
        ArrayList<String> cutPaintingAliases = new ArrayList<>();
        cutPaintingAliases.add("painting");
        cutPaintingAliases.add("cutPainting");
        ArrayList<String> emptyPaintingAliases = new ArrayList<>();
        emptyPaintingAliases.add("painting");
        emptyPaintingAliases.add("emptyPainting");

        ArrayList<String> nailAliases = new ArrayList<>();
        nailAliases.add("nail");
        ArrayList<String> tableAliases = new ArrayList<>();
        tableAliases.add("table");
        ArrayList<String> plantAliases = new ArrayList<>();
        plantAliases.add("plant");
        plantAliases.add("pot");
        ArrayList<String> uselessPlantAliases = new ArrayList<>();
        uselessPlantAliases.add("plant");
        uselessPlantAliases.add("pot");
        uselessPlantAliases.add("uselessPlant");
        ArrayList<String> knifeAliases = new ArrayList<>();
        knifeAliases.add("knife");
        ArrayList<String> certificateAliases = new ArrayList<>();
        certificateAliases.add("certificate");
        certificateAliases.add("note");
        ArrayList<String> wallAliases = new ArrayList<>();
        wallAliases.add("wall");




//      Look around | take | inspect
        HashMap<String, ArrayList<Consequence>> nailActions = new HashMap<>();
        ArrayList<String> stub = new ArrayList<>();
        stub.add("nail");
        Consequence takeNail =  new Consequence("takeFromScene", stub, R.raw.ag_006);
        ArrayList<String> replaceWall = new ArrayList<>();
        replaceWall.add("wall");
        replaceWall.add("emptyWall");
        Consequence replaceWallCon =  new Consequence("replaceInScene", replaceWall);
        ArrayList<Consequence> nailCon = new ArrayList<>();
        nailCon.add(replaceWallCon);
        nailCon.add(takeNail);
        Consequence inspectNail = new Consequence(R.raw.ag_005);
        ArrayList<Consequence> nailInspectCon = new ArrayList<>();
        nailInspectCon.add(inspectNail);
        nailActions.put("take", nailCon);
        nailActions.put("inspect", nailInspectCon);
        nailActions.put("use", simpleResponse(R.raw.ag_019));
        nail = new Item("nail", nailAliases, R.raw.inv_nail, getString(R.string.ag_005), 0, true, true, nailActions);

        HashMap<String, ArrayList<Consequence>> plantActions = new HashMap<>();
        stub = new ArrayList<>();
        stub.add("knife");
        Consequence takePlant =  new Consequence("reveal", stub, R.raw.ag_009);
        ArrayList<Consequence> plantCon = new ArrayList<>();
        plantCon.add(takePlant);
        Consequence inspectPlant = new Consequence(R.raw.ag_007);
        ArrayList<Consequence> plantInspectCon = new ArrayList<>();
        plantInspectCon.add(inspectPlant);
        plantActions.put("take", plantCon);
        plantActions.put("inspect", plantInspectCon);
        plantActions.put("use", simpleResponse(R.raw.useforwhat));
        plantActions.put("combine knife", simpleResponse(R.raw.use_plant_w_knife));
        plantActions.put("combine nail", simpleResponse(R.raw.ag_019));
        plant = new Item("plant", plantAliases, 0, getString(R.string.ag_005), R.raw.room1_examine_003a, false, true, plantActions);


        HashMap<String, ArrayList<Consequence>> uselessPlantActions = new HashMap<>();
        uselessPlantActions.put("take", simpleResponse(R.raw.plant_pickup_postmanhandle));
        uselessPlantActions.put("inspect", simpleResponse(R.raw.plant_examine_postmanhandle));
        uselessPlantActions.put("use", simpleResponse(R.raw.useforwhat));
        uselessPlantActions.put("combine knife", simpleResponse(R.raw.use_plant_w_knife));
        uselessPlantActions.put("combine nail", simpleResponse(R.raw.ag_019));
        uselessPlant = new Item("uselessPlant", uselessPlantAliases, 0, getString(R.string.ag_005), R.raw.room1_examine_003b, false, true, uselessPlantActions);

        HashMap<String, ArrayList<Consequence>> knifeActions = new HashMap<>();
        stub = new ArrayList<>();
        stub.add("knife");
        Consequence takeKnife =  new Consequence("takeFromScene", stub, R.raw.ag_013);
        ArrayList<String> replacement = new ArrayList<>();
        replacement.add("plant");
        replacement.add("uselessPlant");
        Consequence replacePlant =  new Consequence("replaceInScene", replacement);
        ArrayList<Consequence> knifeCon = new ArrayList<>();
        knifeCon.add(replacePlant);
        knifeCon.add(takeKnife);
        knifeActions.put("use", simpleResponse(R.raw.useforwhat));
        knifeActions.put("take", knifeCon);
        knifeActions.put("inspect", simpleResponse(R.raw.ag_012));
//        knifeActions.put("combine painting", simpleResponse(R.raw.ag_019));
        knifeActions.put("combine certificate", simpleResponse(R.raw.ag_018));
        knifeActions.put("combine nail", simpleResponse(R.raw.ag_014));
        //TODO: Combine other for general "That doesn't need to be cut" kind of things
        knife = new Item("knife", knifeAliases, R.raw.inv_nail, getString(R.string.ag_005), R.raw.room1_examine_004, true, false, knifeActions);

        HashMap<String, ArrayList<Consequence>> tableActions = new HashMap<>();
        tableActions.put("inspect", simpleResponse(R.raw.ag_008));
        tableActions.put("take", simpleResponse(R.raw.ag_010));
        tableActions.put("use", simpleResponse(R.raw.ag_011));
        tableActions.put("combine knife", simpleResponse(R.raw.ag_020));
        tableActions.put("combine nail", simpleResponse(R.raw.ag_019));
        table = new Item("table", tableAliases, 0, getString(R.string.ag_005), 0, false, true, tableActions);


        HashMap<String, ArrayList<Consequence>> certificateActions = new HashMap<>();
        stub = new ArrayList<>();
        stub.add("certificate");
        System.out.println("Stub length: "+ stub.size());
        Consequence takeCertificate =  new Consequence("takeFromScene", stub, R.raw.ag_017);
        replacement = new ArrayList<>();
        replacement.add("cutPainting");
        replacement.add("emptyPainting");
        Consequence replaceCutPainting =  new Consequence("replaceInScene", replacement);
        ArrayList<Consequence> certificateCon = new ArrayList<>();
        certificateCon.add(replaceCutPainting);
        certificateCon.add(takeCertificate);
        certificateActions.put("inspect", certificateCon);
        certificateActions.put("take", certificateCon);
        certificateActions.put("use", simpleResponse(R.raw.ag_017));
        certificateActions.put("combine knife", simpleResponse(R.raw.ag_018));
        certificateActions.put("combine nail", simpleResponse(R.raw.ag_019));
        certificate = new Item("certificate", certificateAliases, 0, getString(R.string.ag_005), 0, false, true, certificateActions);

        HashMap<String, ArrayList<Consequence>> fallenPaintingActions = new HashMap<>();
        replacement = new ArrayList<>();
        replacement.add("fallenPainting");
        replacement.add("painting");
        Consequence takeFallenPainting =  new Consequence("replaceInScene", replacement, R.raw.ag_003);
        ArrayList<Consequence> fallenPaintingCon = new ArrayList<>();
        fallenPaintingCon.add(takeFallenPainting);
        fallenPaintingActions.put("inspect", fallenPaintingCon);
        fallenPaintingActions.put("take", fallenPaintingCon);
        fallenPaintingActions.put("use", fallenPaintingCon);
//        certificateActions.put("combine knife", simpleResponse(R.raw.ag_018)); TODO figure out options
        fallenPaintingActions.put("combine nail", simpleResponse(R.raw.ag_019));
        fallenPainting = new Item("fallenPainting", fallenPaintingAliases, 0, getString(R.string.ag_005), R.raw.room1_examine_001a, false, true, fallenPaintingActions);

        HashMap<String, ArrayList<Consequence>> paintingActions = new HashMap<>();
        replacement = new ArrayList<>();
        replacement.add("painting");
        replacement.add("cutPainting");
        Consequence killPainting =  new Consequence("replaceInScene", replacement, R.raw.ag_015);
        stub = new ArrayList<>();
        stub.add("certificate");
        Consequence revealCertificate =  new Consequence("reveal", stub);
        ArrayList<Consequence> paintingCon = new ArrayList<>();
        paintingCon.add(revealCertificate);
        paintingCon.add(killPainting);
        paintingActions.put("inspect", simpleResponse(R.raw.painting_examine_a));
        paintingActions.put("take", simpleResponse(R.raw.ag_004));
        paintingActions.put("use", simpleResponse(R.raw.useforwhat));
        paintingActions.put("combine knife", paintingCon);
        paintingActions.put("combine nail", simpleResponse(R.raw.ag_019));
        painting = new Item("painting", paintingAliases, 0, getString(R.string.ag_005), R.raw.room1_examine_001b, false, true, paintingActions);

        HashMap<String, ArrayList<Consequence>> cutPaintingActions = new HashMap<>();
        cutPaintingActions.put("inspect", simpleResponse(R.raw.ag_016));
        cutPaintingActions.put("take", simpleResponse(R.raw.ag_004));
        cutPaintingActions.put("use", simpleResponse(R.raw.useforwhat));
        cutPaintingActions.put("combine knife", simpleResponse(R.raw.ag_022));
        cutPaintingActions.put("combine nail", simpleResponse(R.raw.ag_019));
        cutPainting = new Item("cutPainting", cutPaintingAliases, 0, getString(R.string.ag_005), R.raw.room1_examine_001c, false, true, cutPaintingActions);

        HashMap<String, ArrayList<Consequence>> emptyPaintingActions = new HashMap<>();
        emptyPaintingActions.put("inspect", simpleResponse(R.raw.painting_examine_b));
        emptyPaintingActions.put("take", simpleResponse(R.raw.ag_004));
        emptyPaintingActions.put("use", simpleResponse(R.raw.useforwhat));
        emptyPaintingActions.put("combine knife", simpleResponse(R.raw.ag_022));
        emptyPaintingActions.put("combine nail", simpleResponse(R.raw.ag_019));
        emptyPainting = new Item("emptyPainting", emptyPaintingAliases, 0, getString(R.string.ag_005), R.raw.room1_examine_001c, false, true, emptyPaintingActions);

        HashMap<String, ArrayList<Consequence>> wallActions = new HashMap<>();
        wall = new Item("wall", wallAliases, 0, getString(R.string.ag_005), R.raw.room1_examine_002a, false, true, wallActions);

        HashMap<String, ArrayList<Consequence>> emptyWallActions = new HashMap<>();
        Item emptyWall = new Item("emptyWall", wallAliases, 0, getString(R.string.ag_005), R.raw.room1_examine_002b, false, true, wallActions);

        //      Item(String nameP, ArrayList<String> aliasesP, int nameAudioP, String descriptionP, int descriptionAudioP, Boolean obtainableP, Boolean seenP, HashMap<String, ArrayList<Consequence>> actionsP) {

        ArrayList<Item> sceneObjects = new ArrayList<>();
        inventory =  new ArrayList<>();
        allGameObjects =  new ArrayList<>();

        sceneObjects.add(fallenPainting);
        sceneObjects.add(nail);
        sceneObjects.add(wall);
        sceneObjects.add(table);
        sceneObjects.add(plant);

        allGameObjects.add(nail);
        allGameObjects.add(plant);
        allGameObjects.add(uselessPlant);
        allGameObjects.add(knife);
        allGameObjects.add(table);
        allGameObjects.add(certificate);
        allGameObjects.add(fallenPainting);
        allGameObjects.add(painting);
        allGameObjects.add(cutPainting);
        allGameObjects.add(emptyPainting);
        allGameObjects.add(wall);
        allGameObjects.add(emptyWall);




        activeScene = new Scene(0, "", R.raw.ag_001, sceneDescription, R.raw.ag_002, sceneObjects);
    }
}
