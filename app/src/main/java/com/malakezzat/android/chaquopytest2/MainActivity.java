package com.malakezzat.android.chaquopytest2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;


public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button buttonLoadPic, buttonEncrypt;
    Uri imageUri;
    private static final int PICK_IMAGE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageView = (ImageView)findViewById(R.id.imageView);
        buttonLoadPic = (Button)findViewById(R.id.buttonLoadPicture);
        buttonEncrypt = findViewById(R.id.buttonEncrypt);
        buttonLoadPic.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {
                openGallery();
            }
        });



        buttonEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"pressed",Toast.LENGTH_SHORT).show();
                if (! Python.isStarted()) {
                    ///////////////// important part //////////////////////
                    //python object to start send data to python script

                    Python.start(new AndroidPlatform(getApplicationContext()));
                    Python py = Python.getInstance();
                    PyObject pyobj = py.getModule("myscript");      // name of script

                    PyObject obj = pyobj.callAttr("Encrypt",imageUri.getPath()); // "Encrypt" is the name of function in python code
                    Toast.makeText(getApplicationContext(),obj.toString(),Toast.LENGTH_LONG).show();  //this only to ensure that is working

                    ///////////////// end   BEST OF LUCK ;D //////////////////////

                }

            }
        });








    }




    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);

        }
    }
}