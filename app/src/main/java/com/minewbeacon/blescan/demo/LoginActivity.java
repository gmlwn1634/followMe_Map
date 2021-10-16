package com.minewbeacon.blescan.demo;

import static com.minewbeacon.blescan.demo.GlobalVar.REQUEST_ENABLE_BT;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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
import com.minew.beacon.MinewBeaconManager;
import com.yuliwuli.blescan.demo.databinding.ActivityLoginBinding;

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

    //Bluetooth 연결
    BluetoothAdapter mBluetoothAdapter;
    final static int BLUETOOTH_REQUEST_CODE = 100;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GlobalVar.mMinewBeaconManager = MinewBeaconManager.getInstance(this);


        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                startActivity(intent);
                loginAPI();

            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    loginAPI();
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), GlobalVar.MSG_REQUEST_BLUETOOTH, Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);


    }

    private void showBLEDialog() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

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


                            Toast.makeText(getApplication(), GlobalVar.MSG_REQUEST_LOGIN_SUCCESS, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    },
                    new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            e.printStackTrace();
                            Toast.makeText(getApplication(), GlobalVar.MSG_REQUEST_LOGIN_FAILED+", 아이디/비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            Log.i(GlobalVar.TAG_ACTIVITY_LOGIN, GlobalVar.MSG_REQUEST_LOGIN_FAILED + e.getMessage());
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