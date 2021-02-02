package com.example.followme_map;//package com.example.followme_map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.followme_map.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String TAG = "LoginActivity";
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //블루투스 연결 확인
                checkBluetooth();
                connectBluetooth();

            }
        }, 2000);


    } //onCreate()


    private void checkBluetooth() {
        //블루투스 지원 유무 확인
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "해당 기기는 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "블루투스 미지원 종료");
            finish();
        }
    } //checkBluetooth()


    private void connectBluetooth() {

        //블루투스가 켜져있으면 로그인화면으로 이동
        if (bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, RESULT_CANCELED);
        }
    } //connectBluetooth()


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "블루투스를 허용하지 않았으므로 종료합니다.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "블루투스 비허용 종료");
            finish();
        } else if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }//onActivityResult()


    @Override
    protected void onDestroy() {
        super.onDestroy();
    } //onDestroy()
}