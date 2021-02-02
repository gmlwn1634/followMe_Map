package com.example.followme_map;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.followme_map.databinding.CustomDialogBinding;

import java.util.Objects;

public class CustomDialog extends Dialog {
    private Context context;
    private CustomDialogClickListener customDialogClickListener;
    private CustomDialogBinding binding;

    public CustomDialog(@NonNull Context context, CustomDialogClickListener customDialogClickListener) {
        super(context);
        this.context = context;
        this.customDialogClickListener = customDialogClickListener;
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
    }
}