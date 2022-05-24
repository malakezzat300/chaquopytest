package com.malakezzat.android.ImageSecurer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class RetrieveImage extends Fragment {

    //TODO variables
    private static final int PICK_IMAGE = 100;
    Button mButtonSaveImage,mButtonGetImage,mEnterKey;
    private StorageReference mStorageRef;
    private ImageView img;
    private EditText mKeyText;
    private String keyString = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_retrieve_image, container, false);

        img=(ImageView)view.findViewById(R.id.imageView3);
        mButtonGetImage=view.findViewById(R.id.getImage);
        mButtonSaveImage=view.findViewById(R.id.save);
        mEnterKey = view.findViewById(R.id.keyButton);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mKeyText = view.findViewById(R.id.keyCode);

        img.setTag("ph");

        mButtonGetImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                getImage();
                img.setTag("noPh");
            }
        });

        mButtonSaveImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    if(!img.getTag().equals("ph")){
                        saveToGallery(img);
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

    private void getImage() {
        if(mKeyText.getText().toString().equals("")){
            Toast.makeText(getActivity(),"Enter Key first!",Toast.LENGTH_LONG).show();
        }
        else {
            StorageReference fileReference = mStorageRef.child(mKeyText.getText().toString());
            final long ONE_MEGABYTE = 1024 * 1024;
            fileReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    img.setImageBitmap(BitmapFactory.decodeByteArray(bytes,0,bytes.length));
                    Toast.makeText(getActivity(), "Image Downloaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if(e.toString().equals("com.google.firebase.storage.StorageException: Object does not exist at location.")) {
                        Toast.makeText(getActivity(), "Image Is Not Exist!",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Log.v("downloading","Error",e);
                    }
                }
            });

        }
    }

    private void saveToGallery(ImageView imageView){
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        FileOutputStream outputStream = null;
        File file = Environment.getExternalStorageDirectory();
        File dir = new File(file.getAbsolutePath() + "/DCIM/MyPics/");
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

        MediaScannerConnection.scanFile(getActivity(),
                new String[] { outFile.getAbsolutePath()  }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }});
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