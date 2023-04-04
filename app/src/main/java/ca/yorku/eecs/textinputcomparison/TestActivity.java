package ca.yorku.eecs.textinputcomparison;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

/*
Many thanks to I. Scott MacKenzie and R. William Soukoreff for creating the phrase set used here.
For more information on the creation of this phrase set, you can read their paper on it here:
http://www.yorku.ca/mack/chi03b.html
 */

public class TestActivity extends Activity {

    private final static String MYDEBUG = "MYDEBUG";
    private final static String PHRASES_LOCATION = "R.raw.phrases";
    private final static String CURRENT_QUESTION_NUMBER = "current_question_number";
    private final static String PHRASES_LIST = "phrases_list";
    private final static String PHRASE_LIST_NUMBER_OF_CHARACTERS = "phraseList_total_characters";
    private final static String CURRENT_PHASE = "current_phase";
    private final static String CURRENT_PHASE_START_TIME = "current_phase_start_time";
    private final static String CURRENT_PHASE_NUMBER_OF_ERRORS = "current_errors";

    private final static int HAPTIC_OFF = 0;
    private final static String HAPTIC_OFF_START_TIME = "haptic_off_start_time";
    private final static String HAPTIC_OFF_FINISH_TIME = "haptic_off_finish_time";
    private final static String HAPTIC_OFF_NUMBER_OF_ERRORS = "haptic_off_errors";
    private final static String HAPTIC_OFF_PHRASE_LIST = "haptic_off_list";
    private final static String HAPTIC_OFF_WPM = "haptic_off_wpm";
    private final static String HAPTIC_OFF_ERROR_RATE = "haptic_off_error_rate";

    private final static int HAPTIC_ON = 1;
    private final static String HAPTIC_ON_START_TIME = "haptic_on_start_time";
    private final static String HAPTIC_ON_FINISH_TIME = "haptic_on_finish_time";
    private final static String HAPTIC_ON_NUMBER_OF_ERRORS= "haptic_on_errors";
    private final static String HAPTIC_ON_PHRASE_LIST = "haptic_on_list";

    private final static String HAPTIC_ON_WPM = "haptic_on_wpm";
    private final static String HAPTIC_ON_ERROR_RATE = "haptic_on_error_rate";

    private final static int VOICE_RECOGNITION = 2;
    private final static String VOICE_RECOGNITION_START_TIME = "voice_recognition_start_time";
    private final static String VOICE_RECOGNITION_FINISH_TIME = "voice_recognition_finish_time";
    private final static String VOICE_RECOGNITION_NUMBER_OF_ERRORS = "voice_recognition_errors";
    private final static String VOICE_RECOGNITION_PHRASE_LIST = "voice_recognition_list";
    private final static String VOICE_RECOGNITION_WPM = "voice_recognition_wpm";
    private final static String VOICE_RECOGNITION_ERROR_RATE = "voice_recognition_error_rate";

    private final static int NUMBER_OF_QUESTIONS = 2;

    int phraseListTotalCharacters;
    int currentQuestionNumber, currentErrors, currentPhase, totalCharactersTyped, totalWordsTyped;
    long currentStartTime;
    boolean errorFound, processingEntry, phaseOver;
    int hapticOffErrors, hapticOnErrors, voiceRecognitionErrors;
    long hapticOffStartTime, hapticOffFinishTime, hapticOnStartTime, hapticOnFinishTime, voiceRecognitionStartTime, voiceRecognitionFinishTime;
    float hapticOffWPM, hapticOffErrorRate, hapticOnWPM, hapticOnErrorRate, voiceRecognitionWPM, voiceRecognitionErrorRate;
    String previousText;
    ArrayList<String> testPhraseList, hapticOffList, hapticOnList, voiceRecognitionList;
    TextView text_to_type;
    EditText input_field;
    Button nextPhaseButton;
    Vibrator vib;
    ToneGenerator toneGenerator;
    userInputListener textChangedListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        text_to_type = findViewById(R.id.text_to_type);
        input_field = findViewById(R.id.input_field);
        nextPhaseButton = findViewById(R.id.next_phase_button);
        nextPhaseButton.setVisibility(View.GONE);

        phraseListTotalCharacters = 0;
        testPhraseList = generatePhraseSet();
        for (String s : testPhraseList) {
            phraseListTotalCharacters += s.length();
        }

        text_to_type.setText(testPhraseList.get(currentQuestionNumber));

        // Turn haptic feedback off
//        View view = findViewById(android.R.id.content);
        View view = getWindow().getDecorView();
        view.setHapticFeedbackEnabled(false);

        errorFound = false;
        processingEntry = false;
        phaseOver = false;
        currentPhase = HAPTIC_OFF;
        currentErrors = 0;
        currentStartTime = System.currentTimeMillis();
        totalCharactersTyped = 0;
        totalWordsTyped = 0;

        hapticOffStartTime = 0;
        hapticOffFinishTime = 0;
        hapticOffErrors = 0;
        hapticOffList = new ArrayList<String>();

        hapticOnStartTime = 0;
        hapticOnFinishTime = 0;
        hapticOnErrors = 0;
        hapticOnList = new ArrayList<String>();

        voiceRecognitionStartTime = 0;
        voiceRecognitionFinishTime = 0;
        voiceRecognitionErrors = 0;
        voiceRecognitionList = new ArrayList<String>();

        hapticOffWPM = 0f;
        hapticOffErrorRate = 0f;
        hapticOnWPM = 0f;
        hapticOnErrorRate = 0f;
        voiceRecognitionWPM = 0f;
        voiceRecognitionErrorRate = 0f;

        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        textChangedListener = new userInputListener();
        input_field.addTextChangedListener(textChangedListener);


    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Phrase List Info
        testPhraseList = savedInstanceState.getStringArrayList(PHRASES_LIST);
        phraseListTotalCharacters = savedInstanceState.getInt(PHRASE_LIST_NUMBER_OF_CHARACTERS);

        // Current Phase
        currentPhase = savedInstanceState.getInt(CURRENT_PHASE);
        currentQuestionNumber = savedInstanceState.getInt(CURRENT_QUESTION_NUMBER);
        currentStartTime = savedInstanceState.getLong(CURRENT_PHASE_START_TIME);
        currentErrors = savedInstanceState.getInt(CURRENT_PHASE_NUMBER_OF_ERRORS);

        // Haptics Off Phase
        hapticOffStartTime = savedInstanceState.getLong(HAPTIC_OFF_START_TIME);
        hapticOffFinishTime = savedInstanceState.getLong(HAPTIC_OFF_FINISH_TIME);
        hapticOffErrors = savedInstanceState.getInt(HAPTIC_OFF_NUMBER_OF_ERRORS);
        hapticOffList = savedInstanceState.getStringArrayList(HAPTIC_OFF_PHRASE_LIST);


        // Haptics On Phase
        hapticOnStartTime = savedInstanceState.getLong(HAPTIC_ON_START_TIME);
        hapticOnFinishTime = savedInstanceState.getLong(HAPTIC_ON_FINISH_TIME);
        hapticOnErrors = savedInstanceState.getInt(HAPTIC_OFF_NUMBER_OF_ERRORS);
        hapticOnList = savedInstanceState.getStringArrayList(HAPTIC_ON_PHRASE_LIST);

        // Voice Recognition Phase
        voiceRecognitionStartTime = savedInstanceState.getLong(VOICE_RECOGNITION_START_TIME);
        voiceRecognitionFinishTime = savedInstanceState.getLong(VOICE_RECOGNITION_FINISH_TIME);
        voiceRecognitionErrors = savedInstanceState.getInt(VOICE_RECOGNITION_NUMBER_OF_ERRORS);
        voiceRecognitionList = savedInstanceState.getStringArrayList(VOICE_RECOGNITION_PHRASE_LIST);

    }

    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Phrase List Info
        savedInstanceState.putStringArrayList(PHRASES_LIST, testPhraseList);
        savedInstanceState.putInt(PHRASE_LIST_NUMBER_OF_CHARACTERS, phraseListTotalCharacters);

        // Current Phase
        savedInstanceState.putInt(CURRENT_PHASE,currentPhase);
        savedInstanceState.putInt(CURRENT_QUESTION_NUMBER,currentQuestionNumber);
        savedInstanceState.putLong(CURRENT_PHASE_START_TIME, currentStartTime);
        savedInstanceState.putInt(CURRENT_PHASE_NUMBER_OF_ERRORS, currentErrors);

        // Haptics Off Phase
        savedInstanceState.putLong(HAPTIC_OFF_START_TIME, hapticOffStartTime);
        savedInstanceState.putLong(HAPTIC_OFF_FINISH_TIME, hapticOffFinishTime);
        savedInstanceState.putInt(HAPTIC_OFF_NUMBER_OF_ERRORS, hapticOffErrors);
        savedInstanceState.putStringArrayList(HAPTIC_OFF_PHRASE_LIST, hapticOffList);


        // Haptics On Phase
        savedInstanceState.putLong(HAPTIC_ON_START_TIME, hapticOnStartTime);
        savedInstanceState.putLong(HAPTIC_ON_FINISH_TIME, hapticOnFinishTime);
        savedInstanceState.putInt(HAPTIC_ON_NUMBER_OF_ERRORS, hapticOnErrors);
        savedInstanceState.putStringArrayList(HAPTIC_ON_PHRASE_LIST, hapticOnList);

        // Voice Recognition Phase
        savedInstanceState.putLong(VOICE_RECOGNITION_START_TIME, voiceRecognitionStartTime);
        savedInstanceState.putLong(VOICE_RECOGNITION_FINISH_TIME, voiceRecognitionFinishTime);
        savedInstanceState.putInt(VOICE_RECOGNITION_NUMBER_OF_ERRORS, voiceRecognitionErrors);
        savedInstanceState.putStringArrayList(VOICE_RECOGNITION_PHRASE_LIST, voiceRecognitionList);


        super.onSaveInstanceState(savedInstanceState);
    }


    protected ArrayList<String> generatePhraseSet() {
        ArrayList<String> fullPhraseList = new ArrayList<>();
        ArrayList<String> curatedPhrases = new ArrayList<>();

        Resources resources = getApplicationContext().getResources();
        InputStream inputStream = resources.openRawResource(R.raw.phrases);

        currentQuestionNumber = 0;
        try {
            InputStreamReader iReader = new InputStreamReader(inputStream);
            BufferedReader bReader = new BufferedReader(iReader);
            String line;

            // Add each line of the initial phrase list text file to phrases as a String
            while ((line = bReader.readLine()) != null) {
                fullPhraseList.add(line);
            }
            iReader.close();

            Random rand = new Random();

            // Pull ten random items from the initial phrase list and add them to test phrase list
            for (int i = 0; i < NUMBER_OF_QUESTIONS; i++) {
                int listItemNumber = rand.nextInt(fullPhraseList.size() - 1);
                curatedPhrases.add(fullPhraseList.get(listItemNumber));
            }
        }
        catch (FileNotFoundException e) {
            Log.i(MYDEBUG, "File not found.");
        }
        catch (IOException e) {
            Log.i(MYDEBUG, "IOException");
        }
        return curatedPhrases;
    }

    protected float calculateWPM(ArrayList<String> list, long millisTime) {
        float secondsTime = millisTime/1000;
        int totalWordsTyped = 0;

        for (String s : list) {
            String[] words = s.trim().split("\\s+");
            totalWordsTyped += words.length;
        }

        Log.i(MYDEBUG, "WPM: " + totalWordsTyped + "/" + (secondsTime/60) + "minutes");


        return totalWordsTyped / (secondsTime / 60);
    }

    protected float calculateErrorRate(ArrayList<String> list, int errors) {
        int totalCharactersTyped = 0;

        for (String s : list) {
            totalCharactersTyped += s.length();
        }

        Log.i(MYDEBUG, "Accuracy Rate = 100% * " + (totalCharactersTyped - errors) + "/" + totalCharactersTyped);

        float errorRatio = (float) (totalCharactersTyped - errors) / totalCharactersTyped;

        return 100f * errorRatio;
    }

    protected void phaseChange() {
        Log.i(MYDEBUG, "Phase Change from " + currentPhase + " to " + (currentPhase + 1));
//        currentQuestionNumber = 0;
        input_field.removeTextChangedListener(textChangedListener);
        nextPhaseButton.setVisibility(View.VISIBLE);
        input_field.setVisibility(View.GONE);
        input_field.setEnabled(false);
        input_field.setText("");

        text_to_type.setText(R.string.main_test_next_phase_warning_text_haptic_off);
        Log.i(MYDEBUG, "Number of current errors is " + currentErrors);

        if (currentPhase == HAPTIC_OFF) {
            hapticOffFinishTime = System.currentTimeMillis();
            hapticOffStartTime = currentStartTime;
            hapticOffErrors = currentErrors;
            currentPhase = HAPTIC_ON;
            text_to_type.setText(R.string.main_test_next_phase_warning_text_haptic_on);
            hapticOffList.addAll(testPhraseList);

            // Turn haptic feedback on
//            View view = findViewById(android.R.id.content);
            View view = getWindow().getDecorView();
            view.setHapticFeedbackEnabled(true);
        } else if (currentPhase == HAPTIC_ON) {
            hapticOnFinishTime = System.currentTimeMillis();
            hapticOnStartTime = currentStartTime;
            hapticOnErrors = currentErrors;
            currentPhase = VOICE_RECOGNITION;
            text_to_type.setText(R.string.main_test_next_phase_warning_text);
            hapticOnList.addAll(testPhraseList);

            // Turn haptic feedback off again
            View view = getWindow().getDecorView();
            view.setHapticFeedbackEnabled(false);


            // Enable Voice Recognition instead of keyboard here






        } else if (currentPhase == VOICE_RECOGNITION){
            voiceRecognitionFinishTime = System.currentTimeMillis();
            voiceRecognitionStartTime = currentStartTime;
            voiceRecognitionErrors = currentErrors;
            text_to_type.setText("");
            voiceRecognitionList.addAll(testPhraseList);

            nextPhaseButton.setVisibility(View.GONE);

            Log.i(MYDEBUG, "Before calculating, haptic_off time = " + hapticOffFinishTime + " - " + hapticOffStartTime + " = " + (hapticOffFinishTime-hapticOffStartTime));
            Log.i(MYDEBUG, "Before calculating, haptic_off errors = " + hapticOffErrors);
            Log.i(MYDEBUG, "Before calculating, haptic_on time = " + hapticOnFinishTime + " - " + hapticOnStartTime + " = " + (hapticOnFinishTime-hapticOnStartTime));
            Log.i(MYDEBUG, "Before calculating, haptic_on errors = " + hapticOnErrors);
            Log.i(MYDEBUG, "Before calculating, voice recognition time = " + voiceRecognitionFinishTime + " - " + voiceRecognitionStartTime + " = " + (voiceRecognitionFinishTime-voiceRecognitionStartTime));
            Log.i(MYDEBUG, "Before calculating, voice recognition errors = " + voiceRecognitionErrors);

            hapticOffWPM = calculateWPM(hapticOffList, (hapticOffFinishTime-hapticOffStartTime));
            hapticOffErrorRate = calculateErrorRate(hapticOffList, hapticOffErrors);
            hapticOnWPM = calculateWPM(hapticOnList, (hapticOnFinishTime-hapticOnStartTime));
            hapticOnErrorRate = calculateErrorRate(hapticOnList, hapticOnErrors);
            voiceRecognitionWPM = calculateWPM(voiceRecognitionList, (voiceRecognitionFinishTime-voiceRecognitionStartTime));
            voiceRecognitionErrorRate = calculateErrorRate(voiceRecognitionList, voiceRecognitionErrors);

            Log.i(MYDEBUG, "After calculating, haptic_off wpm = " + hapticOffWPM);
            Log.i(MYDEBUG, "After calculating, haptic_off error rate = " + hapticOffErrorRate);
            Log.i(MYDEBUG, "After calculating, haptic_on wpm = " + hapticOnWPM);
            Log.i(MYDEBUG, "After calculating, haptic_on error rate = " + hapticOnErrorRate);
            Log.i(MYDEBUG, "After calculating, voice recognition wpm = " + voiceRecognitionWPM);
            Log.i(MYDEBUG, "After calculating, voice recognition error rate = " + voiceRecognitionErrorRate);

            getResults();
        }
    }

    public void clickNextPhase(View view) {
        testPhraseList = generatePhraseSet();
        text_to_type.setText(testPhraseList.get(currentQuestionNumber));
        input_field.setVisibility(View.VISIBLE);
        input_field.setEnabled(true);
        nextPhaseButton.setVisibility(View.GONE);
        currentErrors = 0;
        currentStartTime = System.currentTimeMillis();
        input_field.setText("");
        input_field.addTextChangedListener(textChangedListener);

    }

    public void getResults() {
        Intent i = new Intent(getApplicationContext(), ResultsActivity.class);
        Bundle b = new Bundle();

        Log.i(MYDEBUG, "Before going to the results activity, haptic_off wpm = " + hapticOffWPM);
        Log.i(MYDEBUG, "Before going to the results activity, haptic_off error rate = " + hapticOffErrorRate);
        Log.i(MYDEBUG, "Before going to the results activity, haptic_on wpm = " + hapticOnWPM);
        Log.i(MYDEBUG, "Before going to the results activity, haptic_on error rate = " + hapticOnErrorRate);
        Log.i(MYDEBUG, "Before going to the results activity, voice recognition wpm = " + voiceRecognitionWPM);
        Log.i(MYDEBUG, "Before going to the results activity, voice recognition error rate = " + voiceRecognitionErrorRate);


        b.putFloat(HAPTIC_OFF_WPM, hapticOffWPM);
        b.putFloat(HAPTIC_OFF_ERROR_RATE, hapticOffErrorRate);
        b.putFloat(HAPTIC_ON_WPM, hapticOnWPM);
        b.putFloat(HAPTIC_ON_ERROR_RATE, hapticOnErrorRate);
        b.putFloat(VOICE_RECOGNITION_WPM, voiceRecognitionWPM);
        b.putFloat(VOICE_RECOGNITION_ERROR_RATE, voiceRecognitionErrorRate);
        i.putExtras(b);

        startActivity(i);

        finish();
    }

    // ==================================================================================================
    private class userInputListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            previousText = s.toString();
        }


        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

//            Log.i(MYDEBUG, "Number of current errors is " + currentErrors);

            // Check if the length of the new text is less than the length of the previous text. If so, the user has deleted a character using the backspace key, which is not allowed. Undo this.
            if (s.length() < previousText.length()) {
                Log.i(MYDEBUG, "Backspace detected");
                input_field.setText(previousText);
                input_field.setSelection(start+1);
            }

        // Get the phrase that the user is currently trying to type. Then determine which character in the phrase they are required to type.
            String currentQuestionPhrase = testPhraseList.get(currentQuestionNumber);
            int indexOfTypedCharacter = s.length() - 1;
            if (indexOfTypedCharacter < 0) {
                indexOfTypedCharacter = 0;
            }

            if (testPhraseList.isEmpty() || currentQuestionNumber >= testPhraseList.size()) {
                Log.i(MYDEBUG, "Invalid currentQuestionNumber or testPhraseList is empty.");
                if (testPhraseList.isEmpty()) {
                    Log.e(MYDEBUG, "testPhraseList is empty.");
                }
                if (currentQuestionNumber >= testPhraseList.size()) {
                    Log.e(MYDEBUG, "Invalid currentQuestionNumber ");
                }
            }

            //   This stops the TextWatcher from trying to read characters while a new phrase is loading in.
            if (s == null || s.length() <= indexOfTypedCharacter) {
                return;
            }

            /*
                Check if the user is typing the last character of the phrase. If not, compare the typed char to the correct answer.
                If typed char is incorrect, then temporarily disable the watcher and send out a beeping sound.
                After that, delete the most recently entered character from the input box and flag that user's most recent input was incorrect.
                If the user's input was correct, remove any flag marking the most recent input as incorrect.
                If the user is typing the last character of the phrase, check if there are any more phrases left in the list.
                If there are phrases remaining, go to the next one, otherwise it's time to switch input phases.
             */
            if (indexOfTypedCharacter < currentQuestionPhrase.length() - 1) {
                // typed character is not last in phrase
                char correctChar = currentQuestionPhrase.charAt(indexOfTypedCharacter);
//                Log.i(MYDEBUG, "correctChar: " + correctChar);
                char typedChar = s.charAt(indexOfTypedCharacter);
//                Log.i(MYDEBUG, "typedChar: " + typedChar);

                if (typedChar != correctChar) {
                    // user typed an incorrect character
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                    input_field.removeTextChangedListener(this);
                    Log.i(MYDEBUG, "incorrect char at indexOfTypedCharacter: " + indexOfTypedCharacter);
                    Log.i(MYDEBUG, "currentQuestionPhrase.length(): " + currentQuestionPhrase.length());
                    input_field.setText(currentQuestionPhrase.substring(0, Math.min(indexOfTypedCharacter, currentQuestionPhrase.length())));
                    if (indexOfTypedCharacter < s.length()) {
                        input_field.setSelection(indexOfTypedCharacter);
                    }
                    input_field.addTextChangedListener(this);
                    currentErrors++;
                    errorFound = true;
                } else {
                    errorFound = false;
                }
            } else {
                // typed character is last in phrase
                Log.i(MYDEBUG, "1 currentQuestionNumber: " + currentQuestionNumber);
                Log.i(MYDEBUG, "1 testPhraseList.size(): " + testPhraseList.size());
                currentQuestionNumber++;
                if (currentQuestionNumber <= testPhraseList.size() - 1) {
                    // unused phrases remain in list
                    indexOfTypedCharacter = 0;
                    input_field.removeTextChangedListener(this);
                    if (indexOfTypedCharacter < currentQuestionPhrase.length() - 1 && indexOfTypedCharacter < s.length()) {
                        input_field.setSelection(indexOfTypedCharacter);
                    } else {
                        input_field.setSelection(currentQuestionPhrase.length());
                    }
                    input_field.removeTextChangedListener(this);
                    input_field.setText(new Editable.Factory().newEditable(""));
                    text_to_type.setText(testPhraseList.get(currentQuestionNumber));
                    Log.i(MYDEBUG, "changing test to type to: " + testPhraseList.get(currentQuestionNumber));

                    input_field.addTextChangedListener(this);

                } else {
                    // no unused phrases remain in list
                    Log.i(MYDEBUG, "2 currentQuestionNumber: " + currentQuestionNumber);
                    Log.i(MYDEBUG, "2 testPhraseList.size(): " + testPhraseList.size());
                    phaseOver = true;
                    errorFound = false;
                    currentQuestionNumber = 0;
                    phaseChange();
                }
                errorFound = false;
            }
        }
    }
}

