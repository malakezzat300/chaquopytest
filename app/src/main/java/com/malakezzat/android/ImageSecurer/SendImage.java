package com.malakezzat.android.ImageSecurer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.SecureRandom;

public class SendImage extends Fragment {

    //TODO variables
    private static final int PICK_IMAGE = 100;
    private Button mButtonUpload,mButtonGetImage,mGenerateKey;
    private ImageButton mCopy;
    private StorageReference mStorageRef; //to use firebase database
    private Uri mImageUri;
    private ProgressBar mProgressBar;
    private ImageView img;
    private TextView mKeyText;
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();
    private String keyString = null;
    private ClipboardManager clipboard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_send_image, container, false);

        img=(ImageView) view.findViewById(R.id.imageView3);
        mButtonUpload=view.findViewById(R.id.upload);
        mButtonGetImage=view.findViewById(R.id.openGallery);
        mGenerateKey = view.findViewById(R.id.keyButton);
        mCopy = view.findViewById(R.id.copyButton);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mProgressBar = view.findViewById(R.id.progressBar);
        mKeyText = view.findViewById(R.id.keyCode);

        mButtonUpload.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });
        mButtonGetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        mGenerateKey.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                keyString = randomString(8);
                mKeyText.setText(keyString);
            }
        });

        mCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                String getstring = mKeyText.getText().toString();
                ClipData clip = ClipData.newPlainText("key", getstring);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(),"Key copied",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    //TODO methods

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == 100){
            mImageUri = data.getData();
            ImageSwitcher imageViewOriginal ;
            img.setImageURI(mImageUri);


        }
    };

    private void uploadFile() {
        if(mImageUri != null){
            if(mKeyText.getText().equals("key")){
                Toast.makeText(getActivity(),"Generate Key first!",Toast.LENGTH_LONG).show();
            }
            else {
                StorageReference fileReference = mStorageRef.child(mKeyText.getText().toString());
                fileReference.putFile(mImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(0);
                                    }
                                }, 500);
                                Toast.makeText(getActivity(), "Upload successful", Toast.LENGTH_LONG).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mProgressBar.setProgress((int) progress);
                    }
                });
            }
        }else {
            Toast.makeText( getActivity(), "No file selected",Toast.LENGTH_SHORT).show();
        }
    }
    public String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }


}