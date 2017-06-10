package com.example.ghostechoes.ghostechoes;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by alex on 5/17/2017.
 */

public class LocationTracker extends Service implements LocationListener{

    private final Context context;

    // Access to Location Services
    protected LocationManager locationManager;

    // GPS / Network Provider Check
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    // Geographic Location Data
    Location location;
    double latitude;
    double longitude;

    // Frequency of Location Updates
    // Location updates when distance is changed or time is passed
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 4; // 4 meters
    private static final long MIN_TIME_BW_UPDATES = 1 * 20 * 1000; // 20 second, or 20000 ms

    public LocationTracker(Context context) {
        this.context = context;
        getLocation();
    }

    /**
     * Retrieve current location based on network or gps.
     * Location is updated via distance or duration.
     * Source Assistance: https://stackoverflow.com/questions/41310624/android-how-to-get-current-location-in-longitude-and-latitude
     */
    public Location getLocation() {

        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            // Get status of GPS and Network Provider
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // GPS and Network Provider are not enabled
            if(!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(context, "No Service Found", Toast.LENGTH_SHORT).show();
                return null;
            } else {
                // GPS or Network Provider are enabled, Location can be updated
                this.canGetLocation = true;
                // Network Provider is enabled on device for application
                if (isNetworkEnabled) {
                    // Request for location be be updated periodically via provider
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("Service", "Network");

                        // Retrieve Longitude and Latitude if Network Provider is enabled
                        // Last Known Location
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (location != null) {
                                //Toast.makeText(context, "Network Service", Toast.LENGTH_SHORT).show();
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    } catch (SecurityException e) {
                        Log.d("Network Service", "Security Exception");
                    }
                }
                // GPS is enabled on device for applications
                if(isGPSEnabled) {
                    try {
                        // Network Provider was not available
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    } catch (SecurityException e) {
                        Log.d("GPS Service", "Security Exception");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Halts GPS application usage.
     */
    public void stopUsingGPS() {
        if(locationManager != null) {
            locationManager.removeUpdates(LocationTracker.this);
        }
    }

    /**
     * Return Coordinate's latitude
     */
    public double getLatitude() {
        if(location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * Return Coordinate's longitude
     */
    public double getLongitude() {
        if(location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    /**
     * Returns approximate meters between current location and given location.
     */
    public float radius(double latitude, double longitude) {
        Location currentLocation = getLocation();
        Location echoLocation = new Location ("echo");
        echoLocation.setLatitude(latitude);
        echoLocation.setLongitude(longitude);
        float meters = currentLocation.distanceTo(echoLocation);
        return meters;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    // If GPS is not enabled, application will prompt user to enable
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not enabled. Would you like to go to Device Settings to enable GPS?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            /**
             * User navigates to settings.
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /**
             * User declines settings.
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}