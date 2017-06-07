package com.example.ghostechoes.ghostechoes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationTracker gps;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 9507;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Navigate to echo construction activity
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
            gps = new LocationTracker(this);
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
        Intent intent = new Intent(this, getEcho.class);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(37.000369, -122.063237);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in UCSC"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
