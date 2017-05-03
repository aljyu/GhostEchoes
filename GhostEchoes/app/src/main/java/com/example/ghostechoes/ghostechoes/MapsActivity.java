package com.example.ghostechoes.ghostechoes;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by alex on 4/29/2017.
 */

public class MapsActivity extends AppCompatActivity {

    Button button;
    ImageView imageView;
    // reqest call
    static final int CAM_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        button = (Button) findViewById(R.id.pinEcho);
        imageView = (ImageView) findViewById(R.id.image_view);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start camer app
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Pass the file location for image into intent object
                // Get file path from getFile
                File file = getFile();
                // Pass file into intern object, arg - key, file in form of uri
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(camera_intent, CAM_REQUEST);

            }
        });
    }


    // Create folder on external storage
    private File getFile() {
        File folder =  new File("sdcard/camera_app");
        // Check if folder is available or not
        if (!folder.exists()) {
            folder.mkdir();
        }
        // folder name, and file name
        File image_file = new File(folder, "cam_image.jpg");
        return image_file;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    // Because of onStartActivity, after call will call this below within main activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path = "sdcard/camera_app/cam_image.jpg";
        imageView.setImageDrawable(Drawable.createFromPath(path));
    }

    /*
        Get current location
        Save that location for next activity
        Get user id
        Navigate to next activity
         */
    public void createEcho() {
        // @TODO



        return;
    }
}
