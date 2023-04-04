package ca.yorku.eecs.textinputcomparison;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ResultsActivity extends Activity {

    private final static String HAPTIC_OFF_WPM = "haptic_off_wpm";
    private final static String HAPTIC_OFF_ERROR_RATE = "haptic_off_error_rate";
    private final static String HAPTIC_ON_WPM = "haptic_on_wpm";
    private final static String HAPTIC_ON_ERROR_RATE = "haptic_on_error_rate";
    private final static String VOICE_RECOGNITION_WPM = "voice_recognition_wpm";
    private final static String VOICE_RECOGNITION_ERROR_RATE = "voice_recognition_error_rate";
    float hapticOffWPM, hapticOffErrorRate, hapticOnWPM, hapticOnErrorRate, voiceRecognitionWPM, voiceRecognitionErrorRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        Bundle b = getIntent().getExtras();
        hapticOffWPM = b.getFloat(HAPTIC_OFF_WPM);
        hapticOffErrorRate = b.getFloat(HAPTIC_OFF_ERROR_RATE);
        hapticOnWPM = b.getFloat(HAPTIC_ON_WPM);
        hapticOnErrorRate = b.getFloat(HAPTIC_ON_ERROR_RATE);
        voiceRecognitionWPM = b.getFloat(VOICE_RECOGNITION_WPM);
        voiceRecognitionErrorRate = b.getFloat(VOICE_RECOGNITION_ERROR_RATE);

        TextView no_haptic_speed_value_box = findViewById(R.id.results_no_haptic_speed_value);
        TextView no_haptic_accuracy_value_box = findViewById(R.id.results_no_haptic_accuracy_value);
        TextView yes_haptic_speed_value_box = findViewById(R.id.results_yes_haptic_speed_value);
        TextView yes_haptic_accuracy_value_box = findViewById(R.id.results_yes_haptic_accuracy_value);
        TextView voice_activation_speed_value_box = findViewById(R.id.results_voice_activation_speed_value);
        TextView voice_activation_accuracy_value_box = findViewById(R.id.results_voice_activation_accuracy_value);

        no_haptic_speed_value_box.setText(Float.toString(hapticOffWPM));
        no_haptic_accuracy_value_box.setText(Float.toString(hapticOffErrorRate));
        yes_haptic_speed_value_box.setText(Float.toString(hapticOnWPM));
        yes_haptic_accuracy_value_box.setText(Float.toString(hapticOnErrorRate));
        voice_activation_speed_value_box.setText(Float.toString(voiceRecognitionWPM));
        voice_activation_accuracy_value_box.setText(Float.toString(voiceRecognitionErrorRate));

    }

    public void clickOK(View view) {

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }
}


