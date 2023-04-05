package ca.yorku.eecs.textinputcomparison;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultsActivity extends Activity {

    private final static String MYDEBUG = "MYDEBUG";
    private final static String HAPTIC_OFF_WPM = "haptic_off_wpm";
    private final static String HAPTIC_OFF_ACCURACY_RATE = "haptic_off_error_rate";
    private final static String HAPTIC_ON_WPM = "haptic_on_wpm";
    private final static String HAPTIC_ON_ACCURACY_RATE = "haptic_on_error_rate";
    private final static String VOICE_RECOGNITION_WPM = "voice_recognition_wpm";
    private final static String VOICE_RECOGNITION_ACCURACY_RATE = "voice_recognition_error_rate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);
        Log.i(MYDEBUG, "Results Activity Started");

        TextView hapticNoSpeedValue = findViewById(R.id.results_no_haptic_speed_value);
        TextView hapticNoAccuracyValue = findViewById(R.id.results_no_haptic_accuracy_value);
        TextView hapticYesSpeedValue = findViewById(R.id.results_yes_haptic_speed_value);
        TextView hapticYesAccuracyValue = findViewById(R.id.results_yes_haptic_accuracy_value);
        TextView voiceRecognitionSpeedValue = findViewById(R.id.results_voice_activation_speed_value);
        TextView voiceRecognitionAccuracyValue = findViewById(R.id.results_voice_activation_accuracy_value);
        TextView bestSpeedValue = findViewById(R.id.results_fastest_method_value);
        TextView bestAccuracyValue = findViewById(R.id.results_most_accurate_method_value);

        float hapticOffWPM = 0f;
        float hapticOffAccuracyRate = 0f;
        float hapticOnWPM = 0f;
        float hapticOnAccuracyRate = 0f;
        float voiceRecognitionWPM = 0f;
        float voiceRecognitionAccuracyRate = 0f;

        String bestMethodSpeed = "";
        String bestMethodAccuracy = "";

        Intent callingIntent = getIntent();
        Bundle extras = callingIntent.getExtras();

        if (extras != null) {
            hapticOffWPM = extras.getFloat(HAPTIC_OFF_WPM);
            hapticOffAccuracyRate = extras.getFloat(HAPTIC_OFF_ACCURACY_RATE);
            hapticOnWPM = extras.getFloat(HAPTIC_ON_WPM);
            hapticOnAccuracyRate = extras.getFloat(HAPTIC_ON_ACCURACY_RATE);
            voiceRecognitionWPM = extras.getFloat(VOICE_RECOGNITION_WPM);
            voiceRecognitionAccuracyRate = extras.getFloat(VOICE_RECOGNITION_ACCURACY_RATE);
        }

        hapticNoSpeedValue.setText(String.format("%.1f", hapticOffWPM) + " Words per Minute");
        hapticNoAccuracyValue.setText(String.format("%.1f", hapticOffAccuracyRate) + "%");
        hapticYesSpeedValue.setText(String.format("%.1f", hapticOnWPM) + " Words per Minute");
        hapticYesAccuracyValue.setText(String.format("%.1f", hapticOnAccuracyRate) + "%");
        voiceRecognitionSpeedValue.setText(String.format("%.1f", voiceRecognitionWPM) + " Words per Minute");
        voiceRecognitionAccuracyValue.setText(String.format("%.1f", voiceRecognitionAccuracyRate) + "%");

        String[] categoryNames = {"Without Haptic Feedback", "With Haptic Feedback", "Voice Recognition"};
        ArrayList<String> maxCategoryWinners = new ArrayList<>();
        float[] wpmValues = {hapticOffWPM, hapticOnWPM, voiceRecognitionWPM};
        float[] accuracyValues = {hapticOffAccuracyRate, hapticOnAccuracyRate, voiceRecognitionAccuracyRate};
        float maxWPM = Math.max(hapticOffWPM, Math.max(hapticOnWPM, voiceRecognitionWPM));
        float maxAccuracy = Math.max(hapticOffAccuracyRate, Math.max(hapticOnAccuracyRate, voiceRecognitionAccuracyRate));

        for (int i = 0; i < wpmValues.length; i++) {
            if (wpmValues[i] == maxWPM) {
                maxCategoryWinners.add(categoryNames[i]);
            }
        }

        int winnerCounter = maxCategoryWinners.size();
        for (String s : maxCategoryWinners) {
            if (winnerCounter > 1) {
                bestMethodSpeed += s + " tied with ";
                winnerCounter--;
            } else {
                bestMethodSpeed += s;
            }
        }

        maxCategoryWinners.clear();
        for (int i = 0; i < accuracyValues.length; i++) {
            if (accuracyValues[i] == maxAccuracy) {
                maxCategoryWinners.add(categoryNames[i]);
            }
        }

        winnerCounter = maxCategoryWinners.size();
        for (String s : maxCategoryWinners) {
            if (winnerCounter > 1) {
                bestMethodAccuracy += s + " tied with ";
                winnerCounter--;
            } else {
                bestMethodAccuracy += s;
            }
        }

        bestSpeedValue.setText(bestMethodSpeed);
        bestAccuracyValue.setText(bestMethodAccuracy);

        Log.i(MYDEBUG, "After going to the results activity, haptic_off wpm = " + hapticOffWPM);
        Log.i(MYDEBUG, "After going to the results activity, haptic_off accuracy rate = " + hapticOffAccuracyRate);
        Log.i(MYDEBUG, "After going to the results activity, haptic_on wpm = " + hapticOnWPM);
        Log.i(MYDEBUG, "After going to the results activity, haptic_on accuracy  rate = " + hapticOnAccuracyRate);
        Log.i(MYDEBUG, "After going to the results activity, voice recognition wpm = " + voiceRecognitionWPM);
        Log.i(MYDEBUG, "After going to the results activity, voice recognition accuracy  rate = " + voiceRecognitionAccuracyRate);
    }

    public void clickOK(View view) {

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }
}


