package com.example.ghostechoes.ghostechoes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by alex on 6/5/2017.
 */

public class AnonEchoFormActivity extends AppCompatActivity {

    String LOG_TAG = "EchoForm";
    String message;
    String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_echo_form);

        // Set content to echo message
        Bundle extras = getIntent().getExtras();
        message = extras.getString("echoMessage");
        TextView echoMessage = (TextView) findViewById(R.id.echoMessage);
        echoMessage.setText(message);

        // Set image view to echo image
        try {
            image = extras.getString("echoImage");
            Bitmap bitmapImage = byteArrayToBitmap(image);
            ImageView echoImage = (ImageView) findViewById(R.id.echoImage);
            echoImage.setImageBitmap(bitmapImage);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    /**
     * Convert image back to bitmap to display
     */
    public static Bitmap byteArrayToBitmap(String image) {
        byte[] decodedImage = Base64.decode(image, Base64.DEFAULT);
        Bitmap bm = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
        return bm;
    }
}
