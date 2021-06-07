package com.example.followme_map;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.followme_map.databinding.ActivityLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    static String patientToken;
    static int patientId;
    static String patientName;
    static String loginId;
    static int postalCode;
    static String address;
    static String detailAddress;
    static String phoneNumber;
    static String notes;
    private static String residentNumber;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    int REQUEST_ENABLE_BT = 10;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentFilter bluFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver1,bluFilter);

                loginAPI();
            }
        });


    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (bluetoothAdapter == null) {
                            Toast.makeText(getApplicationContext(), "해당 기기는 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                            Log.i(GlobalVar.TAG_ACTIVITY_MAIN, "블루투스 미지원 종료");
                            finish();
                        } else{
                            Intent intent2 = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(intent2, REQUEST_ENABLE_BT);
                        }



                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }

            }
        }
    };


    public void loginAPI() {

        final String id = binding.id.getText().toString();
        final String password = binding.password.getText().toString();


        //공란 체크
        if (id.equals("") || password.equals(""))
            Toast.makeText(getApplication(), "공란이 있습니다.", Toast.LENGTH_SHORT).show();

        else {
            String url = GlobalVar.URL + GlobalVar.URL_LOGIN;
            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    url,
                    new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Log.i("res", response + "");

                                JSONObject patientObj = jsonObject.getJSONObject("patient_info");

                                String status = jsonObject.getString("status");
                                Log.i("status", status + "");

                                patientId = patientObj.getInt("patient_id");
                                patientName = patientObj.getString("patient_name");
                                loginId = patientObj.getString("login_id");
                                residentNumber = patientObj.getString("resident_number");
                                postalCode = patientObj.getInt("postal_code");
                                address = patientObj.getString("address");
                                detailAddress = patientObj.getString("detail_address");
                                phoneNumber = patientObj.getString("phone_number");
                                notes = patientObj.getString("notes");

                                patientToken = jsonObject.getString("token");


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            Log.d(GlobalVar.TAG_ACTIVITY_LOGIN, response);
                            Toast.makeText(getApplication(), "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    },
                    new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            e.printStackTrace();
                            Toast.makeText(getApplication(), "로그인에 실패하였습니다. 아이디/비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            Log.i(GlobalVar.TAG_ACTIVITY_LOGIN, "로그인 실패" + e.getMessage());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("login_id", id);
                    params.put("password", password);


                    return params;
                }
            };


            request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
            AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
            AppHelper.requestQueue.add(request);
        }


    }


}