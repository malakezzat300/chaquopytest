package com.malakezzat.android.chaquopytest2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {

    ImageView imageViewOriginal, imageViewEncrypted;
    Button buttonLoadPic, buttonEncrypt, buttonSaveEncrypted;
    Uri imageUri;
    BitmapDrawable drawable;
    Bitmap bitmap;
    private static final int PICK_IMAGE = 100;
    String imageString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        imageViewOriginal = (ImageView) findViewById(R.id.imageView);
        imageViewEncrypted = (ImageView) findViewById(R.id.imageView2);
        buttonLoadPic = (Button) findViewById(R.id.buttonLoadPicture);
        buttonEncrypt = findViewById(R.id.buttonEncrypt);
        buttonSaveEncrypted = findViewById(R.id.buttonSave);

        buttonLoadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        //Toast.makeText(getApplicationContext(),"pressed",Toast.LENGTH_SHORT).show();
        if (!Python.isStarted()) {


            Python.start(new AndroidPlatform(getApplicationContext()));
            final Python py = Python.getInstance();

            buttonEncrypt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawable = (BitmapDrawable) imageViewOriginal.getDrawable();
                    bitmap = drawable.getBitmap();
                    imageString = getStringImage(bitmap);
                    PyObject pyobj = py.getModule("myscript");      // name of script
                    PyObject obj = pyobj.callAttr("Encrypt",imageString);
                    String str = obj.toString();
                    byte data[] = android.util.Base64.decode(str,Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0 , data.length);
                    imageViewEncrypted.setImageBitmap(bmp);
                }
            });

            buttonSaveEncrypted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveToGallery(imageViewEncrypted);

                }
            });

        }

    }

    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
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
            imageViewOriginal.setImageURI(imageUri);

        }
    }

    private void saveToGallery(ImageView imageView){
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        FileOutputStream outputStream = null;
        File file = Environment.getExternalStorageDirectory();
        File dir = new File(file.getAbsolutePath() + "/MyPics");
        dir.mkdirs();

        String filename = String.format("%d.png",System.currentTimeMillis());
        File outFile = new File(dir,filename);
        try{
            outputStream = new FileOutputStream(outFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        try{
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}