package com.slh.finalproject;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nex3z.fingerpaintview.FingerPaintView;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity
{
    // Fields for FingerPaint functionality
    FingerPaintView fpv; // drawing area
    Paint p; // pen for FingerPaint view

    // Fields for buttons
    Button clrButton;
    Button submitBtn;
    RadioButton buttonR;
    RadioButton buttonG;
    RadioButton buttonB;

    // Fields for number text views
    TextView numberOneTxt;
    TextView numberTwoTxt;
    TextView numberThreeTxt;

    // Fields for score, predicted val., round and certainty text views
    TextView PredictedValTxt;
    TextView certaintyTxt;
    TextView scoreTxt;
    TextView roundTxt;

    // Fields for storing and counting score
    private int digitCount;
    private int numberCount;
    private int score;
    private int roundCount;

    // Array for storing the value of each digit in the output number
    private int[] digitValues;

    // Array for storing the color of each digit in the output number
    private int[] digitColorValues;

    // Field for creating random values
    SecureRandom randomValGenerator;

    // Field for .tflite file Interpreter
    Interpreter interpreter;

    // Field for ByteBuffer that stores the player's drawings
    ByteBuffer processedDrawing;

    // Intent for starting the end activity
    Intent endIntent;


    // Activity lifecycle function that creates and initializes the activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Sets main activity as the content view and calls the super class onCreate activity lifecycle function
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes random value generator
        randomValGenerator = new SecureRandom();

        // Initializes the score and progression variables
        digitCount = 0;
        numberCount = 0;
        score = 0;
        roundCount = 0;

        // Initializes the text view fields with their respective views in the layout
        numberOneTxt = findViewById(R.id.numberOneTxt);
        numberTwoTxt = findViewById(R.id.numberTwoTxt);
        numberThreeTxt = findViewById(R.id.numberThreeTxt);
        PredictedValTxt = findViewById(R.id.predictedValTxt);
        certaintyTxt = findViewById(R.id.certaintyTxt);
        scoreTxt = findViewById(R.id.scoreTxt);
        roundTxt = findViewById(R.id.roundTxt);

        // Initializes the arrays that store the digit's values and colors
        digitValues = new int[3];
        digitColorValues = new int[3];

        // Initializes the values and colors of the digit text views
        refreshNumber();


        // Initializes the button fields with their respective views in the layout
        p = new Paint();
        fpv = findViewById(R.id.fpv);
        clrButton = findViewById(R.id.clearBtn);
        submitBtn = findViewById(R.id.submitBtn);
        buttonR = findViewById(R.id.radioButtonR);
        buttonG = findViewById(R.id.radioButtonG);
        buttonB = findViewById(R.id.radioButtonB);

        // Sets the initial stroke style and color of the Paint object
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(60);
        p.setColor(Color.RED);

        // Sets the fpv's pen to the Paint object and assigns an OnClickListener to all the
        // activity's buttons
        fpv.setPen(p);
        ButtonClickListener buttonClickListener = new ButtonClickListener();
        clrButton.setOnClickListener(buttonClickListener);
        submitBtn.setOnClickListener(buttonClickListener);
        buttonR.setOnClickListener(buttonClickListener);
        buttonG.setOnClickListener(buttonClickListener);
        buttonB.setOnClickListener(buttonClickListener);

        // Initializes the Interpreter with the MemoryMappedByteBuffer returned by loadModelFile
        try
        {
            interpreter = new Interpreter(loadModelFile());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        } // end try/catch


        processedDrawing = ByteBuffer.allocateDirect(3136);
        processedDrawing.order(ByteOrder.nativeOrder());

        // Initializes the end activity Intent
        endIntent = new Intent( MainActivity.this, EndActivity.class );

    } // end method onCreate


    // Handles button click logic for all the app's buttons
    private class ButtonClickListener implements View.OnClickListener
    {

        public void onClick( View v )
        {

            if ( v.getId() == R.id.clearBtn )
            {
                fpv.clear();
            }
            else if ( v.getId() == R.id.submitBtn )
            {

                // Increment number if third drawing has been submitted
                if ( digitCount >= 2 )
                {
                    numberCount++;
                } // end if


                // if the 10 or fewer numbers have been output, do this:
                if ( numberCount < 10 )
                {
                    checkDrawing();
                }
                else
                {
                    // Check final digit and launch the end activity
                    checkDrawing();
                    endIntent.putExtra("score", score );
                    startActivity(endIntent);
                } // end if/else


                // Output a new number if third drawing has been submitted and reset current digit to 0
                if ( digitCount >= 2 )
                {
                    digitCount = 0;
                    refreshNumber();
                }
                else
                {
                    digitCount++; // increment current digit
                } // end if/else

            }
            else if ( v.getId() == R.id.radioButtonR )
            {
                p.setStrokeWidth(60);
                p.setColor(Color.RED);
            }
            else if ( v.getId() == R.id.radioButtonG )
            {
                p.setStrokeWidth(60);
                p.setColor(Color.GREEN);
            }
            else
            {
                p.setStrokeWidth(60);
                p.setColor(Color.BLUE);
            } // end multi-way if/else

        } // end method onClick

    } // end private inner class ButtonClickListener

    // Compares drawing to digit color and does an inference to check if the drawing's value matches the digit's value
    private void checkDrawing()
    {


        // Exports drawing in fpv to a Bitmap and assigns the Bitmap's pixels to an array
        Bitmap drawing = fpv.exportToBitmap(28, 28);
        int[] pixels = new int[784];
        drawing.getPixels( pixels, 0, 28, 0, 0, 28, 28 );

        // Checks what color the drawing is drawn in
        int drawingColor = 0;
        boolean foundColor = false;
        for ( int i = 0; i < 784; i++ )
        {

            switch ( pixels[i] )
            {
                case Color.RED:
                    drawingColor = Color.RED;
                    foundColor = true;
                    break;
                case Color.GREEN:
                    drawingColor = Color.GREEN;
                    foundColor = true;
                    break;
                case Color.BLUE:
                    drawingColor = Color.BLUE;
                    foundColor = true;
                    break;
            } // end switch

            if ( foundColor )
            {
                break;
            } // end if

        } // end for

        // Sets the ByteBuffer to position 0
        processedDrawing.rewind();


        // Adds each pixel to the ByteBuffer as a normalized float value between 0 and 1
        for ( int i = 0; i < 784; i++ )
        {

                // Log.d("checkDrawing", "pixel color: " + pixels[i] );

                if ( pixels[i] == -5920347 )  // If the pixel is the background color, don't count it
                {
                    processedDrawing.putFloat(0);
                }
                else
                {

                    // logic for converting drawing pixels to normalized grayscale values
                    float pixel = 0;

                    switch (drawingColor)
                    {
                        case Color.RED:
                            pixel = (float) (pixels[i] >> 16 & 0xFF) / 255;
                            processedDrawing.putFloat(pixel);
                            break;
                        case Color.GREEN:
                            pixel = (float) (pixels[i] >> 8 & 0xFF) / 255;
                            processedDrawing.putFloat(pixel);
                            break;
                        case Color.BLUE:
                            pixel = (float) (pixels[i] & 0xFF) / 255;
                            processedDrawing.putFloat(pixel);
                            break;
                    } // end switch

                } // end if/else


        } // end for



        // Creates an array to store the TFLite model's inference
        float[] inferenceOutput = doInference( processedDrawing );

        // Finds the digit with the highest probability in the TFLite model's inference output
        int predictedValIndex = 0;
        for ( int i = 1; i < 10; i++ )
        {

            if ( inferenceOutput[i] > inferenceOutput[predictedValIndex] )
            {
                predictedValIndex = i;
            } // end if

        } // end for

        // Logic for incrementing score if the player draws the right number with the right color
        if ( drawingColor == digitColorValues[digitCount] && predictedValIndex == digitValues[digitCount] )
        {

            Toast.makeText( MainActivity.this, "Nice job!", Toast.LENGTH_LONG).show();
            score++;
            scoreTxt.setText("SCORE: " + score);

        }
        else  // logic for telling the player what they did wrong if they did not draw the number correctly or in the right color
        {

            if ( drawingColor == digitColorValues[digitCount] )
            {
                Toast.makeText( MainActivity.this, "Wrong number", Toast.LENGTH_LONG).show();
            }
            else if ( predictedValIndex == digitValues[digitCount] )
            {
                Toast.makeText( MainActivity.this, "Wrong color", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText( MainActivity.this, "Wrong color and wrong number", Toast.LENGTH_LONG).show();
            } // end multi-way if/else


        } // end if/else

        // Sets the text fields and clears the fpv
        PredictedValTxt.setText( "Predicted Value: " + predictedValIndex );
        certaintyTxt.setText( "Percent Certainty: " + (inferenceOutput[predictedValIndex] * 100) +"%" );
        fpv.clear();

    } // end method checkDigit


    // Randomizes the values and colors of the number text views
    private void refreshNumber()
    {

        for ( int i = 0; i < 3; i++ )
        {
            digitValues[i] = randomValGenerator.nextInt(10);
        } // end for

        numberOneTxt.setText(String.valueOf(digitValues[0]));
        numberTwoTxt.setText(String.valueOf(digitValues[1]));
        numberThreeTxt.setText(String.valueOf(digitValues[2]));

        for ( int i = 0; i < 3; i++ )
        {
            digitColorValues[i] = randomValGenerator.nextInt(3);

            switch ( digitColorValues[i] )
            {
                case 0:
                    digitColorValues[i] = Color.RED;
                    break;
                case 1:
                    digitColorValues[i] = Color.GREEN;
                    break;
                case 2:
                    digitColorValues[i] = Color.BLUE;
                    break;
            } // end switch

        } // end for

        numberOneTxt.setTextColor(digitColorValues[0]);
        numberTwoTxt.setTextColor(digitColorValues[1]);
        numberThreeTxt.setTextColor(digitColorValues[2]);

        roundTxt.setText( "ROUND: " + ++roundCount );

    } // end method refreshNumber


    // Maps .tflite file to a MemoryMappedByteBuffer
    private MappedByteBuffer loadModelFile() throws IOException
    {
        AssetFileDescriptor assetFileDescriptor =
                this.getAssets().openFd("shthDigit.tflite");
        FileInputStream fileInputStream = new
                FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();
        //return the mapped byte buffer
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    } // end method loadModelFile             (Credit goes to Doctor Comitz for this method)


    // Method for doing inferencing with the TensorFlow Lite file
    private float[] doInference( ByteBuffer inputImage )
    {
        //user input
        float[][] output = new float[1][10];
        interpreter.run(inputImage,output);
        return output[0];
    } // end method doInference



} // end class MainActivity