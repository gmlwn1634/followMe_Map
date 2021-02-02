package com.example.followme_map;

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
import com.example.followme_map.databinding.ActivityJoinBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JoinActivity extends AppCompatActivity {

    private ActivityJoinBinding binding;
    private String TAG = "JoinActivity";

    private String emailVali = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private String pwdVali = "^.*(?=^.{8,20}$)(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Volley 통신 requestQueue 생성 및 초기화
        if (AppHelper.requestQueue != null)
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext());


        binding.JoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAPI();
                finish(); //test
            }
        });


    }

    public void registerAPI() {

        final String name = binding.name.getText().toString();
        final String residentNumber1 = binding.residentNumber1.getText().toString();
        final String residentNumber2 = binding.residentNumber2.getText().toString();
        final String id = binding.id.getText().toString();
        final String password = binding.password.getText().toString();
        final String passwordConfirm = binding.passwordConfirm.getText().toString();
//        final String email = binding.email.getText().toString();
        final String phone1 = binding.phoneNum1.getText().toString();
        final String phone2 = binding.phoneNum2.getText().toString();
        final String phone3 = binding.phoneNum3.getText().toString();
        final String postalCode = binding.postalCode.getText().toString();
        final String address = binding.address.getText().toString();
        final String detailAddress = binding.detailAddress.getText().toString();


        //공란 체크
        if (id.equals("") || residentNumber1.equals("") || residentNumber2.equals("") || password.equals("") || passwordConfirm.equals("") || name.equals("") || phone1.equals("") || phone2.equals("") || phone3.equals("") || postalCode.equals("") || address.equals("") || detailAddress.equals(""))
            Toast.makeText(getApplication(), "공란이 있습니다.", Toast.LENGTH_SHORT).show();

//        else if (!email.matches(emailVali)) {
//            Toast.makeText(getApplication(), "이메일형식 불일치", Toast.LENGTH_SHORT).show();
//        } else if (!password.matches(pwdVali)) {
//            Toast.makeText(getApplication(), "비밀번호 형식 불일치", Toast.LENGTH_SHORT).show();
//        }

        //패스워드 형식 확인
        if (!password.matches(pwdVali)) {
            Toast.makeText(getApplication(), "비밀번호 형식 불일치", Toast.LENGTH_SHORT).show();
        }

        //패스워드 일치 확인
        else if (!password.equals(passwordConfirm))
            Toast.makeText(getApplication(), "패스워드 불일치", Toast.LENGTH_SHORT).show();

        else {
            String url = "http://172.26.3.122:8000/api/patient/signup";
            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    url,
                    new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String message = jsonObject.getString("message");
                                System.out.println("message" + message);
                                Log.d(TAG, message);

                                Toast.makeText(getApplication(), "회원 등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            e.printStackTrace();
                            Log.i(TAG, "회원가입 실패" + e.getMessage());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("patient_name", name);
                    params.put("login_id", id);
                    params.put("password", password);
                    params.put("password_confirmation", passwordConfirm);
                    params.put("resident_number", residentNumber1 + "-" + residentNumber2); //주민번호
                    params.put("postal_code", postalCode);
                    params.put("address", address);
                    params.put("detail_address", detailAddress);
                    params.put("phone_number", phone1+"-"+phone2+"-"+phone3);


                    return params;
                }
            };


            request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
            AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
            AppHelper.requestQueue.add(request);
        }

    }


}