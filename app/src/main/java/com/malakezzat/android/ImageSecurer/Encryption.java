package com.malakezzat.android.ImageSecurer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class Encryption extends Fragment implements Runnable{

    private static final int RESULT_OK = -1;
    ImageView imageViewOriginal;
    GifImageView imageViewEncrypted,tempImage;
    Button buttonLoadPic, buttonEncrypt, buttonSaveEncrypted;
    Uri imageUri;
    BitmapDrawable drawable;
    Bitmap bitmap;
    private static final int PICK_IMAGE = 100;
    String imageString = "";
    Bitmap bmp;
    Thread imageThread,imageThread2;
    int j = 1;
    ArrayList<Thread> threads;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_encryption, container, false);

        imageViewOriginal = (ImageView) view.findViewById(R.id.imageView);
        imageViewEncrypted =  view.findViewById(R.id.imageView2);
        buttonLoadPic = (Button) view.findViewById(R.id.buttonLoadPicture);
        buttonEncrypt = view.findViewById(R.id.buttonEncrypt);
        buttonSaveEncrypted = view.findViewById(R.id.buttonSave);
        threads = new ArrayList<Thread>(10);

        imageViewEncrypted.setTag("ph");


        buttonLoadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        Runnable loading = new Runnable() {
            @Override
            public void run() {

                if (!Python.isStarted()) {

                    Python.start(new AndroidPlatform(getActivity()));
                    final Python py = Python.getInstance();

                    drawable = (BitmapDrawable) imageViewOriginal.getDrawable();
                    bitmap = drawable.getBitmap();
                    imageString = getStringImage(bitmap);
                    PyObject pyobj = py.getModule("myscript");      // name of script
                    PyObject obj = pyobj.callAttr("Encrypt",imageString);
                    String str = obj.toString();
                    byte data[] = android.util.Base64.decode(str, Base64.DEFAULT);
                    bmp = BitmapFactory.decodeByteArray(data, 0 , data.length);
                    //imageViewEncrypted.setImageBitmap(bmp);

                }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageViewEncrypted.setBackgroundResource(0);
                        imageViewEncrypted.setImageBitmap(bmp);

                    }
                });
            }

        };

        buttonEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViewEncrypted.setImageBitmap(null);
                imageViewEncrypted.setBackgroundResource(R.drawable.loading);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!Python.isStarted()) {

                            Python.start(new AndroidPlatform(getActivity()));
                            final Python py = Python.getInstance();

                            drawable = (BitmapDrawable) imageViewOriginal.getDrawable();
                            bitmap = drawable.getBitmap();
                            imageString = getStringImage(bitmap);
                            PyObject pyobj = py.getModule("myscript");      // name of script
                            PyObject obj = pyobj.callAttr("Encrypt", imageString);
                            String str = obj.toString();
                            byte data[] = android.util.Base64.decode(str, Base64.DEFAULT);
                            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            //imageViewEncrypted.setImageBitmap(bmp);


                        } else {
                            final Python py = Python.getInstance();
                            drawable = (BitmapDrawable) imageViewOriginal.getDrawable();
                            bitmap = drawable.getBitmap();
                            imageString = getStringImage(bitmap);
                            PyObject pyobj = py.getModule("myscript");      // name of script
                            PyObject obj = pyobj.callAttr("Encrypt", imageString);
                            String str = obj.toString();
                            byte data[] = android.util.Base64.decode(str, Base64.DEFAULT);
                            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            //imageViewEncrypted.setImageBitmap(bmp);


                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                imageViewEncrypted.setBackgroundResource(0);
                                imageViewEncrypted.setImageBitmap(bmp);
                                imageViewEncrypted.setTag("noPh");
                            }
                        });

                    }
                }).start();
            }
        });

            buttonSaveEncrypted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                            if(!imageViewEncrypted.getTag().equals("ph")){
                                saveToGallery(imageViewEncrypted);
                            }else{
                                Toast.makeText(getActivity(),"There is no image to save!",Toast.LENGTH_LONG).show();
                            }

                        } else {
                            requestStoragePermission();
                        }
                }
            });

        return view;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data){
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
        File dir = new File(file.getAbsolutePath() + "/DCIM/MyPics5/");
        dir.mkdirs();

        String filename = String.format("%d.png",System.currentTimeMillis());
        File outFile = new File(dir,filename);
        MediaScannerConnection.scanFile(getActivity(),
                new String[] { outFile.getAbsolutePath()  }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }});
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
        Toast.makeText(getActivity(),"Image Saved!",Toast.LENGTH_SHORT).show();
    }

    private void requestStoragePermission()
    {
        if(/*ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), Manifest.permission.READ_EXTERNAL_STORAGE) && */ ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                    .setCancelable(false)
                    .setTitle("Permission Required!")
                    .setMessage("App needs Permissions to save photos. \nDo you accept this permissions?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
        else
        {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }


    @Override
    public void run() {
        if (!Python.isStarted()) {


            Python.start(new AndroidPlatform(getActivity()));
//            final Python py = Python.getInstance();

            final Python py = Python.getInstance();
            drawable = (BitmapDrawable) imageViewOriginal.getDrawable();
            bitmap = drawable.getBitmap();
            imageString = getStringImage(bitmap);
            PyObject pyobj = py.getModule("myscript");      // name of script
            PyObject obj = pyobj.callAttr("Encrypt", imageString);
            String str = obj.toString();
            byte data[] = android.util.Base64.decode(str, Base64.DEFAULT);
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

        }
    }
}