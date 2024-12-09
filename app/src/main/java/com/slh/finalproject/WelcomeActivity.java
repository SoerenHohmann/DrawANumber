package com.slh.finalproject;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity
{

    Button button;
    Intent mainIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initializes the intent to launch the MainActivity
        mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);

        // Assigns button view to button field
        button = findViewById(R.id.button);

        // Sets up the onClickListener to launch the main activity when the button is pressed
        button.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        startActivity(mainIntent);
                    }
                }
        );

    } // end method onCreate



} // end class WelcomeActivity