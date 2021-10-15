package com.minewbeacon.blescan.demo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.yuliwuli.blescan.demo.databinding.CustomDialogBinding;

import java.util.Objects;

public class CustomDialog extends Dialog {
    private Context context;
    private CustomDialogClickListener customDialogClickListener;
    private CustomDialogBinding binding;
    private String text1;
    private String text2;

    public CustomDialog(@NonNull Context context, CustomDialogClickListener customDialogClickListener, String text1, String text2) {
        super(context);
        this.context = context;
        this.customDialogClickListener = customDialogClickListener;
        this.text1 = text1;
        this.text2 = text2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CustomDialogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        binding.okBtn.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                customDialogClickListener.onPositiveClick();
                dismiss();
            }
        });
        binding.text1.setText(text1);
        binding.text2.setText(text2);

    }
}