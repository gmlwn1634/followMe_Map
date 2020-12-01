package com.example.followme_map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.followme_map.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    //onCreate->블루투스가 연결되어 있는지 감지-> 진료 동선 버튼 누르면->블루투스가 연결되어 있는지 감지->activity_flow로->해당 환자의 전체 진료 동선을 받아옴-> 안내시작 버튼 누르면
    //첫번째 진료 동선을 받아옴->기본 모드로 환자의 방향에 따라 아이콘 회전(폰의 방위각) & 구글맵은 다음 노드까지 방위각에 따라
    //전환 시 구글맵이 돌아감-> 사용자의 화면 확대/축소/이동이 감지되면 사용자 조작모드로 -> target과 bearing이 유동적으로 변경됨
    //버튼을 누르면 다시 기본 모드로


    private ActivityMainBinding binding;
    BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //블루투스 연결 확인
        checkBluetooth();

        //블루투스 상태 변경 시
        IntentFilter filter = new IntentFilter(bluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);

        //버튼 클릭 시 진료동선 화면으로 넘어감
        redirectFlow();

    } //onCreate()

    private void checkBluetooth() {

        //블루투스 지원 유무 확인
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "해당 기기는 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //블루투스가 꺼져있으면
        if (!bluetoothAdapter.isEnabled()) {
            //블루투스 연결 요청
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RESULT_CANCELED);
        }
    } //checkBluetooth()


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, RESULT_CANCELED);
                }
            }
        }
    }; //BroadcastReceiver


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            //블루투스를 허용하지 않을 시 종료
            Toast.makeText(this, "블루투스를 허용하지 않았으므로 종료합니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }// onActivityResult()


    private void redirectFlow() {
        binding.flowStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FlowActivity.class);
                startActivity(intent);
            }
        });
    } //redirectFlow()

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }



}