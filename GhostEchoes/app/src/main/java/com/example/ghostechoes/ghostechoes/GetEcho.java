package com.example.ghostechoes.ghostechoes;

import android.app.Activity;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class GetEcho extends AppCompatActivity {
    String LOG_TAG = "GetEcho";

    int SET_MILES = 3;
    Double METER_LIMIT = SET_MILES * 1609.34;

    // Buttons, Views
    Button btn_map;

    // Request
    RequestQueue queue;

    // GPS
    LocationTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_echo);
        // Clickable Objects
        btn_map = (Button) findViewById(R.id.button3);
        // Request Queue
        queue = Volley.newRequestQueue(this);
        // Get echoes from database
        getEcho();
    }

    /**
     * Sends GET request to server to retrieve data (i.e. location, text)
     * into database.
     */
    public void getEcho() {
        final TextView msg = (TextView) findViewById(R.id.textView3);
        // Request queue
        queue = Volley.newRequestQueue(this);
        String url = "http://darkfeather2.pythonanywhere.com/get_data";
        // Request a string response from the provided URL.
        final JsonArrayRequest jsonArrayReq = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ArrayList<JSONObject> jsonList = new ArrayList<>();
                for(int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        Double latitude = jsonObject.getDouble("latitude");
                        Double longitude = jsonObject.getDouble("longitude");
                        // Display only echoes within set range
                        if (isEchoInRange(latitude, longitude)) {
                            jsonList.add(jsonObject);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, e.toString());
                    }
                }
                String jsonResponse = jsonList.toString();
                msg.setText("Response is: "+ jsonResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                msg.setText("Please try again!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonArrayReq);
    }

    /**
     * Checks and returns boolean value of whether an echo that is within
     * range of user's set current location.
     */
    public boolean isEchoInRange(double latitude, double longitude) {
        gps = new LocationTracker(getApplicationContext());
        if (gps.canGetLocation()) {
            double currentLatitude = gps.getLatitude();
            double currentLongitude = gps.getLongitude();

            if (gps.radius(latitude, longitude) <= METER_LIMIT) {
                return true;
            }
        } else {
            Log.d(LOG_TAG, "Cannot get current location");
            return false;
        }
        return false;
    }

    /**
     * Return to map activity.
     */
    public void goToMap(View v) {
        // Should only go to echo when location can be retrieved
        Intent intent = new Intent(this, GoogleMapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
