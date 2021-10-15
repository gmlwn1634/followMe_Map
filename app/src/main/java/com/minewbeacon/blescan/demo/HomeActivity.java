package com.minewbeacon.blescan.demo;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yuliwuli.blescan.demo.R;
import com.yuliwuli.blescan.demo.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFrag(1); //디폴트 프래그먼트 선택
        binding.bottomNavi.setSelectedItemId(R.id.action_home);


        binding.bottomNavi.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_info:
                        setFrag(0);
                        break;
                    case R.id.action_home:
                        setFrag(1);
                        break;
                    case R.id.action_list:
                        setFrag(2);
                        break;
                }
                return true;
            }
        });


    } //onCreate()


    //fragment 교체
    private void setFrag(int n) {
        switch (n) {
            case 0:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, new InfoFragment()).commit();
                break;
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, new HomeFragment()).commit();
                break;
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, new ListFragment()).commit();
                break;
        }
    }
}