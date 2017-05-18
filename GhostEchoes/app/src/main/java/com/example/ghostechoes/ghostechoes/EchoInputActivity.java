package com.example.ghostechoes.ghostechoes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by alex on 4/29/2017.
 */

public class EchoInputActivity extends AppCompatActivity {

    Button button;
    ImageView imageView;
    private LocationTracker gps;
    private byte[] bpdata;

    // Request Code
    static final int CAMERA_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_echo_input);

        // Button and Image Objects
        button = (Button) findViewById(R.id.echoSnap);
        imageView = (ImageView) findViewById(R.id.image_view);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start camera application
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Get file path for new image storage
                //File file = getFile();

                // Pass file location (uri) into intent object; key will identify it
                //camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(camera_intent, CAMERA_REQUEST);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    // Store Created Echo
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

    // Create Folder in external storage
    private File getFile() {
        File folder =  new File("sdcard/camera_app");
        // Check if folder is available or not; create folder it not
        if (!folder.exists()) {
            folder.mkdir();
        }
        // Create file with file name and folder
        File img = new File(folder, "camera_image.jpg");
        return img;
    }

    // Called after startActivityForResult in main activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            Bitmap bp = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bp);
            bpdata = bitmapToByteArray(bp);
        } else {
            // Handle NullPointerException from Image Cancel
            Toast.makeText(getApplicationContext(), "Image Not Taken", Toast.LENGTH_LONG).show();
        }

       // String path = "sdcard/camera_app/cam_image.jpg";
       // imageView.setImageDrawable(Drawable.createFromPath(path));
    }

    /* Converts Bitmap to ByteArray for database storage
     */
    public static byte[] bitmapToByteArray(Bitmap bm) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

}
