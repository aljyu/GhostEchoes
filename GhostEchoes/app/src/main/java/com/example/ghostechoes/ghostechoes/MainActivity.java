package com.example.ghostechoes.ghostechoes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "Ghost_Echoes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Enter the application and change activity to MapActivity (or whatever it is called)
    public void onClickEnterApp(View V){
        Log.d(LOG_TAG,"Hitting the Enter Button");
        //Intent intent = new Intent(this, Echo_Input_Text.class);
        Intent intent = new Intent(this, GoogleMapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
