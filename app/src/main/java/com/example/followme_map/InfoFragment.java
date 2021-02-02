package com.example.followme_map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.followme_map.databinding.FragmentInfoBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;
    final String TAG = "infoFragment";
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentInfoBinding.inflate(inflater);

        binding.name.setText(LoginActivity.patientName);
        binding.patientId.setText(LoginActivity.patientId + "");
        binding.phoneNumber.setText(LoginActivity.phoneNumber);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToken();
            }
        });

        binding.logoutBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToken();
            }
        });

        View v = inflater.inflate(R.layout.fragment_info, container, false);
        context = container.getContext();


        return binding.getRoot();
    }


    public void sendToken() {
        String url = "http://172.26.3.122:8000/api/patient/logout";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        System.out.println("로그아웃 요청됨");

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String message = jsonObject.getString("message");

                            System.out.println("message" + message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        Log.d(TAG, response);
                        Toast.makeText(context, "로그아웃에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(TAG, "로그아웃 실패" + e.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + LoginActivity.patientToken);
                return headers;
            }
        };


        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(context); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);
    }

}



