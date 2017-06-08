package com.example.ghostechoes.ghostechoes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    String LOG_TAG = "GoogleMapsActivity";
    private GoogleMap mMap;
    private LocationTracker gps;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 9507;
    JSONArray jsonArray;

    int REQUEST_LIMIT = 3;

    // Request
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        gps = new LocationTracker(this);
        queue = Volley.newRequestQueue(this);
        for(int i = 0; i < REQUEST_LIMIT; i++) {
            if (jsonArray == null) {
                getCoordinates();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for(int i = 0; i < REQUEST_LIMIT; i++) {
            if (jsonArray == null) {
                getCoordinates();
            }
        }
    }

    /**
     * Retrieve coordinates from external database and pin to map
     */
    public void getCoordinates() {
        // Request queue
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
                                Double latitude;
                                Double longitude;
                                try {
                                    latitude = jsonObject.getDouble("latitude");
                                    longitude = jsonObject.getDouble("longitude");
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("ECHO").icon(
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                                } catch (JSONException e) {
                                    continue;
                                }
                                jsonList.add(jsonObject);
                            } catch (Exception e) {
                                Log.d(LOG_TAG, e.toString());
                            }
                        }
                        JSONArray jsonResponse = new JSONArray(jsonList);
                        jsonArray = jsonResponse;


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonArrayReq);
    }

    /**
     * Navigate to echo construction activity
     */
    public void setLocation(View v){
        // Pass Location to next Activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            if (gps.canGetLocation()) {
                Toast.makeText(
                        getApplicationContext(),
                        "Your Location is -\nLat: " + gps.getLatitude() + "\nLong: "
                                + gps.getLongitude(), Toast.LENGTH_LONG).show();
            } else {
                gps.showSettingsAlert();
            }
            // Should only go to echo when location can be retrieved
            Intent intent = new Intent(this, EchoInputActivity.class);
            intent.putExtra("longitude", gps.getLongitude());
            intent.putExtra("latitude", gps.getLatitude());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /*
     * Goes directly to the Echoes page
     */
    public void goToEchoes(View v) {
        Intent intent = new Intent(this, GetEcho.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ArrayList<LatLng> latlngs = new ArrayList<>();

        // Add marker for current location
        LatLng ucsc;
        if (gps.canGetLocation()) {
            ucsc = new LatLng(gps.getLatitude(), gps.getLongitude());
        } else {
            ucsc = new LatLng(37.000369, -122.063237);
        }
        mMap.addMarker(new MarkerOptions().position(ucsc).title("Marker in UCSC"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ucsc, 11));
    }
}
