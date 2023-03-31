package ca.yorku.eecs.textinputcomparison;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }

    public void clickOK(View view) {

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

    public void clickCancel(View view) {

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

}
