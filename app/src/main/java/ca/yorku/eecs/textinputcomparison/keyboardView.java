package ca.yorku.eecs.textinputcomparison;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;

public class keyboardView extends LinearLayout implements View.OnKeyListener {

    private boolean shiftOn = false;
    private boolean hapticFeedbackOn = true;
    private Vibrator vibrator;
    private EditText input_field;
    private PopupWindow popup;

    public keyboardView(Context context, EditText input_field) {
        super(context);
        this.input_field = input_field;
        init();
    }

    public keyboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public keyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Log.i("MYDEBUG", "init keyboard");
        LayoutInflater.from(getContext()).inflate(R.layout.keyboard, this, true);
        // Find all the key views and set their OnKeyListener to this view
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView instanceof Button) {
                childView.setOnKeyListener(this);
                childView.setFocusable(true);
                childView.setFocusableInTouchMode(true);
            }
        }
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public void displayAppKeyboard() {
        Log.i("MYDEBUG", "display keyboard");

        if (popup != null && popup.isShowing()) {
            return;
        }

        // Create a popup window to display the keyboard view
        popup = new PopupWindow(this, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // Set the popup window's background to null to remove the default shadow
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set the popup window's input method mode to allow the keyboard to be shown
        popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);

        // Show the popup window below the EditText
        popup.showAsDropDown(input_field);
    }

    private void hideAppKeyboard() {
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
        }
    }


    public void updateText(String text) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                String buttonText = button.getText().toString();
                if (buttonText.length() == 1) {
                    //button.setText(text.toUpperCase().contains(buttonText) ? buttonText.toUpperCase() : buttonText.toLowerCase());
                }
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.i("MYDEBUG", "keypress detected");

        // Check if the key event is a key down event
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // Get the button text
            String buttonText = ((Button) v).getText().toString();

            Log.i("MYDEBUG", "buttonText = " + buttonText );

            // Insert the key text into input_field
            Editable editable = input_field.getText();
            int start = input_field.getSelectionStart();
            int end = input_field.getSelectionEnd();
            editable.replace(start, end, buttonText);

            // Turn haptic feedback on for the key view
            v.setHapticFeedbackEnabled(true);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            // Turn haptic feedback off for the key view
            v.setHapticFeedbackEnabled(false);
        }
        // Return false to allow the key event to propagate to other views
        return false;
    }

}
