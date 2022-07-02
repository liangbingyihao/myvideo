package com.example.myvideo.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.example.myvideo.R;

public class FragmentSubtitle extends DialogFragment {

    public static void showDialog(FragmentActivity activity){
        FragmentSubtitle customDialogFragment = new FragmentSubtitle();
        customDialogFragment.show(activity.getSupportFragmentManager(),"yc");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Dialog_FullScreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_subtitle, container, false);

        return view;
    }
}
