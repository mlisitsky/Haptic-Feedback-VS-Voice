package ca.yorku.eecs.textinputcomparison;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.Manifest;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

/*
Many thanks to I. Scott MacKenzie and R. William Soukoreff for creating the phrase set used in the following test.
For more information on the creation of this phrase set, you can read their paper on it here:
http://www.yorku.ca/mack/chi03b.html
 */

public class TestActivity extends Activity {

    // Test-wide parameters
    private final static String MYDEBUG = "MYDEBUG";
    private final static String SPEECHRECOGNITIONLOG = "SPEECHRECOGNITIONLOG";
    private final static String TEXTCOMPARISONLOG = "TEXTCOMPARISONLOG";
    private final static int PHRASES_LIST_SOURCE = R.raw.phrases;
    private final static int RECORD_AUDIO_REQUEST_CODE = 111;
    private final static int NUMBER_OF_QUESTIONS = 5;
    private final static String PHRASES_LIST = "phrases_list";
    private final static String PHRASE_LIST_NUMBER_OF_CHARACTERS = "phraseList_total_characters";

    // Current Phase parameters
    private final static String CURRENT_PHASE = "current_phase";
    private final static String CURRENT_QUESTION_NUMBER = "current_question_number";
    private final static String CURRENT_PHASE_START_TIME = "current_phase_start_time";
    private final static String CURRENT_PHASE_NUMBER_OF_ERRORS = "current_errors";

    // Data collected from the "Without Haptic Feedback" phase
    private final static int HAPTIC_OFF = 0;
    private final static String HAPTIC_OFF_START_TIME = "haptic_off_start_time";
    private final static String HAPTIC_OFF_FINISH_TIME = "haptic_off_finish_time";
    private final static String HAPTIC_OFF_NUMBER_OF_ERRORS = "haptic_off_errors";
    private final static String HAPTIC_OFF_PHRASE_LIST = "haptic_off_list";
    private final static String HAPTIC_OFF_WPM = "haptic_off_wpm";
    private final static String HAPTIC_OFF_ERROR_RATE = "haptic_off_error_rate";

    // Data collected from the "With Haptic Feedback" phase
    private final static int HAPTIC_ON = 1;
    private final static String HAPTIC_ON_START_TIME = "haptic_on_start_time";
    private final static String HAPTIC_ON_FINISH_TIME = "haptic_on_finish_time";
    private final static String HAPTIC_ON_NUMBER_OF_ERRORS = "haptic_on_errors";
    private final static String HAPTIC_ON_PHRASE_LIST = "haptic_on_list";
    private final static String HAPTIC_ON_WPM = "haptic_on_wpm";
    private final static String HAPTIC_ON_ERROR_RATE = "haptic_on_error_rate";

    // Data collected from the "Voice Recognition" phase
    private final static int VOICE_RECOGNITION = 2;
    private final static String VOICE_RECOGNITION_START_TIME = "voice_recognition_start_time";
    private final static String VOICE_RECOGNITION_FINISH_TIME = "voice_recognition_finish_time";
    private final static String VOICE_RECOGNITION_NUMBER_OF_ERRORS = "voice_recognition_errors";
    private final static String VOICE_RECOGNITION_PHRASE_LIST = "voice_recognition_list";
    private final static String VOICE_RECOGNITION_WPM = "voice_recognition_wpm";
    private final static String VOICE_RECOGNITION_ERROR_RATE = "voice_recognition_error_rate";

    int phraseListTotalCharacters;
    int currentQuestionNumber, currentErrors, currentPhase, totalCharactersTyped, totalWordsTyped;
    long currentStartTime;
    boolean errorFound, phaseOver, recordingAudio;
    int hapticOffErrors, hapticOnErrors, voiceRecognitionErrors;
    long hapticOffStartTime, hapticOffFinishTime, hapticOnStartTime, hapticOnFinishTime, voiceRecognitionStartTime, voiceRecognitionFinishTime;
    float hapticOffWPM, hapticOffErrorRate, hapticOnWPM, hapticOnErrorRate, voiceRecognitionWPM, voiceRecognitionErrorRate;
    String previousText, userVoiceInput;
    ArrayList<String> testPhraseList, hapticOffList, hapticOnList, voiceRecognitionList;
    TextView textToType, voiceRecognitionText;
    EditText userInputField;
    Button nextPhaseButton, recordButton;
    Vibrator vib;
    ToneGenerator toneGenerator;
    UserInputListener userTextChangedListener;
    SpeechRecognizer userSpeechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        textToType = findViewById(R.id.text_to_type);
        userInputField = findViewById(R.id.input_field);
        voiceRecognitionText = findViewById(R.id.voice_recognition_text);
        voiceRecognitionText.setVisibility(View.GONE);
        nextPhaseButton = findViewById(R.id.next_phase_button);
        nextPhaseButton.setVisibility(View.GONE);
        recordButton = findViewById(R.id.record_audio_button);
        recordButton.setVisibility(View.GONE);

        phraseListTotalCharacters = 0;
        testPhraseList = generatePhraseSet();
        for (String s : testPhraseList) {
            phraseListTotalCharacters += s.length();
        }
        textToType.setText(testPhraseList.get(currentQuestionNumber));

        totalCharactersTyped = 0;
        totalWordsTyped = 0;

        errorFound = false;
        recordingAudio = false;

        currentPhase = HAPTIC_OFF;
        currentErrors = 0;
        currentStartTime = System.currentTimeMillis();

        hapticOffStartTime = 0;
        hapticOffFinishTime = 0;
        hapticOffErrors = 0;
        hapticOffList = new ArrayList<>();

        hapticOnStartTime = 0;
        hapticOnFinishTime = 0;
        hapticOnErrors = 0;
        hapticOnList = new ArrayList<>();

        voiceRecognitionStartTime = 0;
        voiceRecognitionFinishTime = 0;
        voiceRecognitionErrors = 0;
        voiceRecognitionList = new ArrayList<>();

        hapticOffWPM = 0f;
        hapticOffErrorRate = 0f;
        hapticOnWPM = 0f;
        hapticOnErrorRate = 0f;
        voiceRecognitionWPM = 0f;
        voiceRecognitionErrorRate = 0f;

        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        userTextChangedListener = new UserInputListener();
        userInputField.addTextChangedListener(userTextChangedListener);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkSpeechPermissions();
        }

        userSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        userSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Called when the speech recognition is ready to begin.
                Log.i(SPEECHRECOGNITIONLOG, "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                // Called when the user has started to speak.
                voiceRecognitionText.setText(R.string.test_recording_in_progress_text);
                Log.i(SPEECHRECOGNITIONLOG, "onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Called when the volume of the user's speech changes.
                Log.i(SPEECHRECOGNITIONLOG, "onRmsChanged");
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.i(SPEECHRECOGNITIONLOG, "onBufferReceived");
            }

            @Override
            public void onEndOfSpeech() {
                // Called when the user has finished speaking.
                Log.i(SPEECHRECOGNITIONLOG, "onEndOfSpeech");
            }

            @Override
            public void onError(int error) {
                // Called when there is an error in the recognition process.
                Log.i(SPEECHRECOGNITIONLOG, "onError: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                // Called when the speech recognition has produced results.
                Log.i(SPEECHRECOGNITIONLOG, "onResults");
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                voiceRecognitionText.setText(data.get(0));
                userVoiceInput = data.get(0);
                checkUserVoiceInput(userVoiceInput);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Called when partial recognition results are available.
                Log.i(SPEECHRECOGNITIONLOG, "onPartialResults");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Called when a speech recognition event occurs.
                Log.i(SPEECHRECOGNITIONLOG, "onEvent");
            }
        });

        View.OnTouchListener userOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    recordButton.setText(R.string.test_record_button_start_text);
                    Log.i(MYDEBUG, "Record Button unpressed");
                    userSpeechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    recordButton.setText(R.string.test_record_button_stop_text);
                    voiceRecognitionText.setText(R.string.test_recording_in_progress_text);
                    recordingAudio = true;
                    Log.i(MYDEBUG, "Record Button pressed");
                    userSpeechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        };

        recordButton.setOnTouchListener(userOnTouchListener);
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
        savedInstanceState.putInt(CURRENT_PHASE, currentPhase);
        savedInstanceState.putInt(CURRENT_QUESTION_NUMBER, currentQuestionNumber);
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
        InputStream inputStream = resources.openRawResource(PHRASES_LIST_SOURCE);

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
        } catch (FileNotFoundException e) {
            Log.i(MYDEBUG, "File not found.");
        } catch (IOException e) {
            Log.i(MYDEBUG, "IOException");
        }
        return curatedPhrases;
    }

    /*
        First, create a String array the words in the phrase to say and one of all the words detected in user voice input.
        Put these in lowercase for comparison without considering case sensitivity, and also convert all integer representations of numbers to the corresponding english words.
        Then, go word by word in the user voice input and compare to the corresponding word in the correct answer.
        For each word that doesn't match, increment current phase errors by 1. If all words match, go to next phrase using the same logic as in the text input phases.
        Otherwise, inform the user of what the application heard them say rather than the correct answer and ask them to try again.
    */
    public void checkUserVoiceInput(String input) {
        String[] wordsToSay = testPhraseList.get(currentQuestionNumber).toLowerCase().trim().split("\\s+");
        String[] wordsInInput = input.toLowerCase().trim().split("\\s+");
        boolean allWordsCorrect = true;
        String userWord = "";
        String testWord = "";
        int receivedWords = 0;
        int requiredWords = 0;

        for (String s : wordsToSay) {
            requiredWords++;
        }

        for (String s : wordsInInput) {
            receivedWords++;
        }

        if (receivedWords < requiredWords) {
            currentErrors += (requiredWords - receivedWords);
            allWordsCorrect = false;
        }

        for (int i = 0; i < receivedWords - 1; i++) {
            if (wordsToSay[i] == null || i > requiredWords) {
                currentErrors += receivedWords - requiredWords;
                allWordsCorrect = false;
                break;
            } else {
                testWord = wordsToSay[i];
            }
            userWord = wordsInInput[i];

            if (checkIfNumber(userWord)) {
                userWord = convertNumberToWord(userWord);
            }

            if (!userWord.equals(testWord)) {
                currentErrors++;
                allWordsCorrect = false;
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
            }
            Log.i(MYDEBUG, "Comparing user input: " + userWord + " to correct word: " + testWord);
        }

        if (allWordsCorrect) {
            currentQuestionNumber++;
            if (currentQuestionNumber <= testPhraseList.size() - 1) {
                // unused phrases remain in list
                voiceRecognitionText.setText("");
                textToType.setText(testPhraseList.get(currentQuestionNumber));
                Log.i(MYDEBUG, "changing test to type: " + testPhraseList.get(currentQuestionNumber));

            } else {
                // no unused phrases remain in list
                Log.i(MYDEBUG, "currentQuestionNumber: " + currentQuestionNumber);
                Log.i(MYDEBUG, "testPhraseList.size(): " + testPhraseList.size());
                phaseOver = true;
                currentQuestionNumber = 0;
                phaseChange();
            }
        } else {
            Log.i(MYDEBUG, "user input sounded like: " + voiceRecognitionText.getText().toString());
            String sorryText = "We heard \"" + voiceRecognitionText.getText().toString() + "\". Please try again.";
            voiceRecognitionText.setText(sorryText);
        }
    }

    public boolean checkIfNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public String convertNumberToWord(String s) {
        int num = Integer.parseInt(s);
        String[] ones = {"", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
        String[] tens = {"", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
        String convertedWord = "";

        if (num == 0) {
            return "zero";
        }

        if (num == 100) {
            return "hundred";
        }

        if (num == 1000) {
            return "thousand";
        }

        if (num < 0 || num > 999) {
            return "invalid number";
        }

        if (num < 20) {
            convertedWord = ones[num];
        } else {
            convertedWord = tens[num / 10];
        }

        return convertedWord;
    }

    protected float calculateWPM(ArrayList<String> list, long millisTime) {
        float secondsTime = millisTime / 1000f;
        int totalWordsTyped = 0;

        for (String s : list) {
            String[] words = s.trim().split("\\s+");
            totalWordsTyped += words.length;
        }

        Log.i(MYDEBUG, "WPM: " + totalWordsTyped + "/" + (secondsTime / 60) + "minutes");

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

    // Voice recognition calculates errors by word rather than character so it needs its own accuracy rate calculation
    protected float calculateVoiceRecognitionErrorRate(ArrayList<String> list, int errors) {
        int totalWordsTyped = 0;

        for (String s : list) {
            String[] words = s.trim().split("\\s+");
            totalWordsTyped += words.length;
        }

        Log.i(MYDEBUG, "Accuracy Rate = 100% * " + (totalWordsTyped - errors) + "/" + totalWordsTyped);

        float errorRatio = (float) (totalWordsTyped - errors) / totalWordsTyped;
        return 100f * errorRatio;
    }

    /*
        Based on the ID of the current phase, save all current phase parameters into the proper phase-specific parameters for calculation later.
        Make adjustments to between-phase display based on what the upcoming phase is. If final phase has ended, calculate final results and send to results activity.
     */
    protected void phaseChange() {
        Log.i(MYDEBUG, "Phase Change from " + currentPhase + " to " + (currentPhase + 1));
        userInputField.removeTextChangedListener(userTextChangedListener);
        nextPhaseButton.setVisibility(View.VISIBLE);
        userInputField.setVisibility(View.GONE);
        userInputField.setEnabled(false);
        userInputField.setText("");

        textToType.setText(R.string.test_next_phase_warning_text);
        Log.i(MYDEBUG, "Number of current errors is " + currentErrors);

        if (currentPhase == HAPTIC_OFF) {
            hapticOffFinishTime = System.currentTimeMillis();
            hapticOffStartTime = currentStartTime;
            hapticOffErrors = currentErrors;
            currentPhase = HAPTIC_ON;
            textToType.setText(R.string.test_next_phase_warning_text_haptic_off);
            hapticOffList.addAll(testPhraseList);

        } else if (currentPhase == HAPTIC_ON) {
            hapticOnFinishTime = System.currentTimeMillis();
            hapticOnStartTime = currentStartTime;
            hapticOnErrors = currentErrors;
            currentPhase = VOICE_RECOGNITION;
            textToType.setText(R.string.test_next_phase_warning_text);
            hapticOnList.addAll(testPhraseList);

            // Hide keyboard input field and reveal record button
            userInputField.setVisibility(View.GONE);
            voiceRecognitionText.setVisibility(View.VISIBLE);

        } else if (currentPhase == VOICE_RECOGNITION) {
            voiceRecognitionFinishTime = System.currentTimeMillis();
            voiceRecognitionStartTime = currentStartTime;
            voiceRecognitionErrors = currentErrors;
            textToType.setText("");
            voiceRecognitionList.addAll(testPhraseList);

            nextPhaseButton.setVisibility(View.GONE);

            Log.i(MYDEBUG, "Before calculating, haptic_off time = " + hapticOffFinishTime + " - " + hapticOffStartTime + " = " + (hapticOffFinishTime - hapticOffStartTime));
            Log.i(MYDEBUG, "Before calculating, haptic_off errors = " + hapticOffErrors);
            Log.i(MYDEBUG, "Before calculating, haptic_on time = " + hapticOnFinishTime + " - " + hapticOnStartTime + " = " + (hapticOnFinishTime - hapticOnStartTime));
            Log.i(MYDEBUG, "Before calculating, haptic_on errors = " + hapticOnErrors);
            Log.i(MYDEBUG, "Before calculating, voice recognition time = " + voiceRecognitionFinishTime + " - " + voiceRecognitionStartTime + " = " + (voiceRecognitionFinishTime - voiceRecognitionStartTime));
            Log.i(MYDEBUG, "Before calculating, voice recognition errors = " + voiceRecognitionErrors);

            hapticOffWPM = calculateWPM(hapticOffList, (hapticOffFinishTime - hapticOffStartTime));
            hapticOffErrorRate = calculateErrorRate(hapticOffList, hapticOffErrors);
            hapticOnWPM = calculateWPM(hapticOnList, (hapticOnFinishTime - hapticOnStartTime));
            hapticOnErrorRate = calculateErrorRate(hapticOnList, hapticOnErrors);
            voiceRecognitionWPM = calculateWPM(voiceRecognitionList, (voiceRecognitionFinishTime - voiceRecognitionStartTime));
            voiceRecognitionErrorRate = calculateVoiceRecognitionErrorRate(voiceRecognitionList, voiceRecognitionErrors);

            Log.i(MYDEBUG, "After calculating, haptic_off wpm = " + hapticOffWPM);
            Log.i(MYDEBUG, "After calculating, haptic_off error rate = " + hapticOffErrorRate);
            Log.i(MYDEBUG, "After calculating, haptic_on wpm = " + hapticOnWPM);
            Log.i(MYDEBUG, "After calculating, haptic_on error rate = " + hapticOnErrorRate);
            Log.i(MYDEBUG, "After calculating, voice recognition wpm = " + voiceRecognitionWPM);
            Log.i(MYDEBUG, "After calculating, voice recognition error rate = " + voiceRecognitionErrorRate);

            getResults();
        }
    }

    /*
        Remove between-phase display and reset current phase parameters.
     */
    public void clickNextPhase(View view) {
        testPhraseList = generatePhraseSet();
        if (currentPhase == VOICE_RECOGNITION) {
            recordButton.setVisibility(View.VISIBLE);
        }
        textToType.setText(testPhraseList.get(currentQuestionNumber));
        userInputField.setVisibility(View.VISIBLE);
        userInputField.setEnabled(true);
        nextPhaseButton.setVisibility(View.GONE);
        currentErrors = 0;
        currentStartTime = System.currentTimeMillis();
        userInputField.setText("");
        userInputField.addTextChangedListener(userTextChangedListener);
        phaseOver = false;
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

    public void checkSpeechPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userSpeechRecognizer.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted to Record Audio", Toast.LENGTH_SHORT).show();
        }
    }


    // ==================================================================================================
    private class UserInputListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            previousText = s.toString();
        }


        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            // If in the haptic feedback phrase, respond to every user key input with a vibration pulse similar to the default keyboard's haptic feedback setting
            if (currentPhase == 1) {
                long[] pattern = {0, 10, 10, 20};
                vib.vibrate(pattern, -1);
            }

            // Check if the length of the new text is less than the length of the previous text. If so, the user has deleted a character using the backspace key, which is not allowed. Undo this.
            if (s.length() < previousText.length()) {
                Log.i(TEXTCOMPARISONLOG, "Backspace detected");
                userInputField.setText(previousText);
                userInputField.setSelection(start + 1);
            }

            // Get the phrase that the user is currently trying to type. Then determine which character in the phrase they are required to type.
            String currentQuestionPhrase = testPhraseList.get(currentQuestionNumber);
            int indexOfTypedCharacter = s.length() - 1;
            if (indexOfTypedCharacter < 0) {
                indexOfTypedCharacter = 0;
            }

            if (testPhraseList.isEmpty() || currentQuestionNumber >= testPhraseList.size()) {
                Log.i(TEXTCOMPARISONLOG, "Invalid currentQuestionNumber or testPhraseList is empty.");
                if (testPhraseList.isEmpty()) {
                    Log.e(TEXTCOMPARISONLOG, "testPhraseList is empty.");
                }
                if (currentQuestionNumber >= testPhraseList.size()) {
                    Log.e(TEXTCOMPARISONLOG, "Invalid currentQuestionNumber ");
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
//                Log.i(TEXTCOMPARISONLOG, "correctChar: " + correctChar);
                char typedChar = s.charAt(indexOfTypedCharacter);
//                Log.i(TEXTCOMPARISONLOG, "typedChar: " + typedChar);

                if (typedChar != correctChar) {
                    // user typed an incorrect character
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                    userInputField.removeTextChangedListener(this);
                    Log.i(TEXTCOMPARISONLOG, "incorrect char at indexOfTypedCharacter: " + indexOfTypedCharacter);
                    Log.i(TEXTCOMPARISONLOG, "currentQuestionPhrase.length(): " + currentQuestionPhrase.length());
                    userInputField.setText(currentQuestionPhrase.substring(0, Math.min(indexOfTypedCharacter, currentQuestionPhrase.length())));
                    if (indexOfTypedCharacter < s.length()) {
                        userInputField.setSelection(indexOfTypedCharacter);
                    }
                    userInputField.addTextChangedListener(this);
                    currentErrors++;
                    errorFound = true;
                } else {
                    errorFound = false;
                }
            } else {
                // typed character is last in phrase
                Log.i(TEXTCOMPARISONLOG, "1 currentQuestionNumber: " + currentQuestionNumber);
                Log.i(TEXTCOMPARISONLOG, "1 testPhraseList.size(): " + testPhraseList.size());
                currentQuestionNumber++;
                if (currentQuestionNumber <= testPhraseList.size() - 1) {
                    // unused phrases remain in list
                    indexOfTypedCharacter = 0;
                    userInputField.removeTextChangedListener(this);
                    if (indexOfTypedCharacter < currentQuestionPhrase.length() - 1 && indexOfTypedCharacter < s.length()) {
                        userInputField.setSelection(indexOfTypedCharacter);
                    } else {
                        userInputField.setSelection(currentQuestionPhrase.length());
                    }
                    userInputField.removeTextChangedListener(this);
                    userInputField.setText(new Editable.Factory().newEditable(""));
                    textToType.setText(testPhraseList.get(currentQuestionNumber));
                    Log.i(TEXTCOMPARISONLOG, "changing test to type to: " + testPhraseList.get(currentQuestionNumber));

                    userInputField.addTextChangedListener(this);

                } else {
                    // no unused phrases remain in list
                    Log.i(TEXTCOMPARISONLOG, "2 currentQuestionNumber: " + currentQuestionNumber);
                    Log.i(TEXTCOMPARISONLOG, "2 testPhraseList.size(): " + testPhraseList.size());
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

