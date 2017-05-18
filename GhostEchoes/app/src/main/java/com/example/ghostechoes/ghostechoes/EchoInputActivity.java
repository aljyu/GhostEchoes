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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by alex on 4/29/2017.
 */

public class EchoInputActivity extends AppCompatActivity {

    // Buttons, Views
    Button btn_captureImage;                // Capture-image button
    Button btn_openImgFolder;               // Open-image folder button
    ImageView imageView;                    // View-Image object

    // Data storage objects
    private LocationTracker gps;            // GPS Coordinates
    private byte[] bpdata;                  // Image Data

    // Request Code
    static final int CAMERA_REQUEST = 1;    // Camera Code
    static final int IMAGE_REQUEST = 2;     // Image Folder Code
    static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3; // Read Image Folder Code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_echo_input);
        // Clickable Objects
        btn_captureImage = (Button) findViewById(R.id.echoSnap);
        btn_openImgFolder = (Button) findViewById(R.id.echoSavedImage);
        imageView = (ImageView) findViewById(R.id.image_view);

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
                bpdata = bitmapToByteArray(bp);
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
    public void setEcho(View v){
        double longitude;
        double latitude;
        String message;
        byte[] image = bpdata;

        // User Message
        EditText edvecho = (EditText) findViewById(R.id.echoMsg);
        message = edvecho.getText().toString();

        Bundle coordinates = getIntent().getExtras();
        if (coordinates == null) {
            Toast.makeText(getApplicationContext(), "No Coordinates Available", Toast.LENGTH_SHORT).show();
            // @TODO - Null coordinates should have halted activities in maps
        } else {
            longitude = coordinates.getDouble("longitude");
            latitude = coordinates.getDouble("latitude");
            Toast.makeText(getApplicationContext(), "Pinning Echo at " + longitude + ", " + latitude +
                    "\nMessage: " + message +
                    "\nImage: " + image, Toast.LENGTH_SHORT).show();
        }
        // @TODO - Store to Database Photo, Text, Location
        // Should only go to echo when location can be retrieved
        Intent intent = new Intent(this, GoogleMapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
