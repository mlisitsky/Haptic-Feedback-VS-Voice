package ca.yorku.eecs.textinputcomparison;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    Button startButton, settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        startButton = findViewById(R.id.start_button);
//        settingsButton = findViewById(R.id.settings_button);

    }

    public void clickStart(View view) {

        Intent i = new Intent(getApplicationContext(), TestActivity.class);
        startActivity(i);
    }

    public void clickSettings(View view) {

        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(i);
    }

    // This is for testing only, will delete later
    public void clickTestResults(View view) {

        Intent i = new Intent(getApplicationContext(), ResultsActivity.class);
        startActivity(i);
    }
}
