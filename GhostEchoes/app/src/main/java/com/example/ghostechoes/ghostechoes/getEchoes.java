package com.example.ghostechoes.ghostechoes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;


public class getEchoes extends AppCompatActivity {
    // Request queue
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_echoes);
        // Request queue
        queue = Volley.newRequestQueue(this);
        // Store messages to the database
        storeMessage();
    }
    
    public void storeMessage() {
        final TextView msg = (TextView) findViewById(R.id.textView3);
        // Instantiate the RequestQueue
        String url = "darkfeather2.pythonanywhere.com/get_data";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String output = response.toString();
                        /*
                        int begin = output.indexOf(":\"");
                        int end = output.lastIndexOf("\"");
                        String result = output.substring(begin + 2, end);
                        msg.setText(result);
                        */
                        msg.setText(output);
                        //Log.d(LOG_TAG, "Received: " + response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.d(LOG_TAG, error.toString());
                    }
                });
        queue.add(jsObjRequest);
    }
    
    public void goToMap(View v) {
        // Should only go to echo when location can be retrieved
        Intent intent = new Intent(this, GoogleMapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
