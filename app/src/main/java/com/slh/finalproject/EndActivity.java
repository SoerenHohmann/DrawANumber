package com.slh.finalproject;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class EndActivity extends AppCompatActivity
{
    TextView resultTxt;
    TextView finalScoreTxt;
    Button playAgainBtn;

    Intent intentFromMain;

    Intent playAgainIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        // Initialize fields with views from layout
        resultTxt = findViewById(R.id.resultTxt);
        finalScoreTxt = findViewById(R.id.finalScoreTxt);


        // Get score passed from MainActivity and set the score and win/lose text view accordingly
        intentFromMain = getIntent();

        int finalScore = intentFromMain.getIntExtra("score", 0 );

        if ( finalScore >= 21 )
        {
            resultTxt.setTextColor(1286557951); // green color
            resultTxt.setText("YOU WON!!!");
            Toast.makeText( EndActivity.this, "You Won!", Toast.LENGTH_LONG).show();
        }
        else
        {
            resultTxt.setTextColor(-383884289); // pink/red color
            resultTxt.setText("YOU LOST.");
            Toast.makeText( EndActivity.this, "You Lost.", Toast.LENGTH_LONG).show();
        }

        finalScoreTxt.setText( "Final Score: " + finalScore + "/21" );


        // Initialize button field with button view and set up the OnClickListener to launch the welcome activity
        playAgainBtn = findViewById(R.id.playAgainBtn);

        playAgainIntent = new Intent(EndActivity.this, WelcomeActivity.class );

        playAgainBtn.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        startActivity(playAgainIntent);
                    }
                }
        );


    } // end method onCreate



} // end class EndActivity