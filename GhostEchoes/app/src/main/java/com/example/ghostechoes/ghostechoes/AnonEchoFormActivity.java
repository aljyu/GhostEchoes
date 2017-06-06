package com.example.ghostechoes.ghostechoes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by alex on 6/5/2017.
 */

public class AnonEchoFormActivity extends AppCompatActivity {

    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_echo_form);

        // Set content to echo message
        Bundle extras = getIntent().getExtras();
        message = extras.getString("echoMessage");
        TextView echoMessage = (TextView) findViewById(R.id.echoMessage);
        echoMessage.setText(message);
    }
}
