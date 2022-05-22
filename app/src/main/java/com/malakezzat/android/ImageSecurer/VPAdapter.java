package com.malakezzat.android.ImageSecurer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

class VPAdapter extends FragmentStateAdapter {

    private String[] titles = new String[] {"Encryption","Decryption","Send Image","Retrieve Image"};


    public VPAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new Encryption();
            case 1:
                return new Decryption();
            case 2:
                return new SendImage();
            case 3:
                return new RetrieveImage();
        }
        return new Encryption();
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
