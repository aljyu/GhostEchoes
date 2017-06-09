package com.example.ghostechoes.ghostechoes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 4/29/2017.
 */

public class EchoInputActivity extends AppCompatActivity {

    private final String LOG_TAG = "EchoInput";

    // Buttons, Views
    Button btn_captureImage;                // Capture-image button
    Button btn_openImgFolder;               // Open-image folder button
    ImageView imageView;                    // View-Image object

    // Data storage objects
    private double longitude;               // GPS Coordinates
    private double latitude;
    private byte[] bpdata;                  // Image Data
    private String message;

    // Request Code
    static final int CAMERA_REQUEST = 1;    // Camera Code
    static final int IMAGE_REQUEST = 2;     // Image Folder Code
    static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3; // Read Image Folder Code

    // Request
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_echo_input);
        // Request objects
        queue = Volley.newRequestQueue(this);

        // Clickable Objects
        btn_captureImage = (Button) findViewById(R.id.echoSnap);
        btn_openImgFolder = (Button) findViewById(R.id.echoSavedImage);
        imageView = (ImageView) findViewById(R.id.image_view);

        // Set passed data
        Bundle coordinates = getIntent().getExtras();
        if (coordinates == null) {
            Toast.makeText(getApplicationContext(), "No Coordinates Available", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, GoogleMapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            setCoordinates(coordinates.getDouble("longitude"), coordinates.getDouble("latitude"));
        }

        // Open Camera Application
        btn_captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, CAMERA_REQUEST);
            }
        });
        // Default Photo Browse Application
        btn_openImgFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browse_intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(browse_intent, IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Checks for valid image and creates storable image data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle request and return of camera function
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bp);
                setImageData(bp);
            } else {
                // Handles NullPointerException from Image Cancel
                Toast.makeText(getApplicationContext(), "No Image Captured", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bp = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                    imageView.setImageBitmap(bp);
                    bpdata = bitmapToByteArray(bp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    // Check Permissions; build.gradle modified to use min sdk 23 for checkSelfPermission
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Converts Bitmap to ByteArray for database storage
     */
    public static byte[] bitmapToByteArray(Bitmap bm) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Stores echo data to database, which includes image bytes,
     * geographic location (longitude, latitude), and message text.
     */
    public void saveEcho(View v){
        // Set User's Message
        setMessage();
        String imageString;
        // Retrieve String Base64 format for HTTP Post
        try {
            imageString = Base64.encodeToString(bpdata, Base64.DEFAULT);
        } catch (NullPointerException e){
            imageString = "No Image";
        }
        // Store echo data into database
        postEcho(longitude, latitude, message, imageString);
        Toast.makeText(getApplicationContext(), "Pinning Echo at " + longitude + ", " + latitude +
                "\nMessage: " + message +
                "\nImage: " + bpdata, Toast.LENGTH_SHORT).show();

        // Navigate to next activity containing several echoes by geolocation
        Intent intent = new Intent(this, GetEcho.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Sends POST request to server to store data (i.e. location, text)
     * into database.
     */
    public void postEcho(final double longitude, final double latitude, final String message, final String image) {
        StringRequest sr = new StringRequest(Request.Method.POST,
                "http://darkfeather2.pythonanywhere.com/post_data",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Got:" + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("longitude", String.valueOf(longitude));
                params.put("latitude" , String.valueOf(latitude));
                params.put("echo", message);
                params.put("image", image);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

    /* Getter and Setter */
    public void setImageData(Bitmap image) {
        byte[] bpImage = bitmapToByteArray(image);
        bpdata = bpImage;
        return;
    }

    public byte[] getImageData() {
        return bpdata;
    }

    public void setCoordinates(double lon, double lat) {
        longitude = lon;
        latitude = lat;
        return;
    }

    public void setMessage() {
        EditText edv = (EditText) findViewById(R.id.echoMsg);
        message = edv.getText().toString();
        return;
    }

    public String getMessage() {
        return message;
    }
}
