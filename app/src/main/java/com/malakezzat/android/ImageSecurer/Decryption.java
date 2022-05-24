package com.malakezzat.android.ImageSecurer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class Decryption extends Fragment {

    private static final int RESULT_OK = -1;
    ImageView imageViewEncrypted;
    GifImageView imageViewDecrypted;
    Button buttonLoadPic, buttonDecrypt, buttonSaveDecrypted;
    Uri imageUri;
    BitmapDrawable drawable;
    Bitmap bitmap;
    private static final int PICK_IMAGE = 100;
    String imageString = "";
    Bitmap bmp;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_decryption, container, false);

        imageViewEncrypted = (ImageView) view.findViewById(R.id.imageView);
        imageViewDecrypted = view.findViewById(R.id.imageView2);
        buttonLoadPic = (Button) view.findViewById(R.id.buttonLoadPicture);
        buttonDecrypt = view.findViewById(R.id.buttonDecrypt);
        buttonSaveDecrypted = view.findViewById(R.id.buttonSave);

        imageViewDecrypted.setTag("ph");

        buttonLoadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

            buttonDecrypt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    imageViewDecrypted.setImageBitmap(null);
                    imageViewDecrypted.setBackgroundResource(R.drawable.loading);


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!Python.isStarted()) {

                                Python.start(new AndroidPlatform(getActivity()));
                                final Python py = Python.getInstance();

                                drawable = (BitmapDrawable) imageViewEncrypted.getDrawable();
                                bitmap = drawable.getBitmap();
                                imageString = getStringImage(bitmap);
                                PyObject pyobj = py.getModule("myscript");      // name of script
                                PyObject obj = pyobj.callAttr("Decrypt", imageString);
                                String str = obj.toString();
                                byte data[] = android.util.Base64.decode(str, Base64.DEFAULT);
                                bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                //imageViewEncrypted.setImageBitmap(bmp);


                            } else {
                                final Python py = Python.getInstance();
                                drawable = (BitmapDrawable) imageViewEncrypted.getDrawable();
                                bitmap = drawable.getBitmap();
                                imageString = getStringImage(bitmap);
                                PyObject pyobj = py.getModule("myscript");      // name of script
                                PyObject obj = pyobj.callAttr("Decrypt", imageString);
                                String str = obj.toString();
                                byte data[] = android.util.Base64.decode(str, Base64.DEFAULT);
                                bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                //imageViewEncrypted.setImageBitmap(bmp);


                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    imageViewDecrypted.setBackgroundResource(0);
                                    imageViewDecrypted.setImageBitmap(bmp);
                                    imageViewDecrypted.setTag("noPh");
                                }
                            });

                        }
                    }).start();

                }
            });

            buttonSaveDecrypted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        if(!imageViewDecrypted.getTag().equals("ph")){
                            try {
                                saveToGallery(imageViewDecrypted);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
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
            imageViewEncrypted.setImageURI(imageUri);

        }
    }

    private void saveToGallery(ImageView imageView) throws FileNotFoundException {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        FileOutputStream outputStream = null;
        File file2 = Environment.getExternalStorageDirectory();
        File dir = new File(file2.getAbsolutePath() + "/Pictures/");

//        File imagePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        File file = new File(dir,"IS_"+ System.currentTimeMillis() +".png");

        //dir.mkdirs();
//        String filename = String.format("%d.png",System.currentTimeMillis());
//        File outFile = new File(dir,filename);

        try{
            outputStream = new FileOutputStream(file);
        }catch (Exception e){
            e.printStackTrace();
        }

        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        try{
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(getActivity(),
                new String[] { file.getAbsolutePath()   }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {

                    }});

//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, file.getName());
//        values.put(MediaStore.Images.Media.DESCRIPTION, "Encrypted Image form Image Securer App");
//        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
//        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
//        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
//        values.put("_data", file.getAbsolutePath());
//
//        cr = Objects.requireNonNull(getActivity()).getContentResolver();
//        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), file.getAbsolutePath() , file.getName() ,"Encrypted Image form Image Securer App");

        Toast.makeText(getActivity(),"Image Saved!",Toast.LENGTH_SHORT).show();
    }

    private void requestStoragePermission()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE))
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


}