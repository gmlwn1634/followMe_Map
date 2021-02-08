package com.example.followme_map;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.followme_map.databinding.ActivityLoginBinding;
import com.example.followme_map.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {


    private ActivityMain2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        binding.signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, JoinActivity.class);
                startActivity(intent);
            }
        });
    }


}