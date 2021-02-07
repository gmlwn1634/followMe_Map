package com.example.followme_map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.followme_map.databinding.FragmentHomeBinding;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater);

        binding.name.setText(LoginActivity.patientName);
        binding.patientId.setText(LoginActivity.patientId + "");

        binding.flowStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FlowActivity.class);
                startActivity(intent);
            }
        });

        binding.paymentBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                startActivity(intent);
            }
        });


//        //대기순번 laravel-pusher
        PusherOptions pusherOptions = new PusherOptions();
        pusherOptions.setCluster("ap3");
        Pusher pusher = new Pusher("7ed3a4ce8ebfe9741f98", pusherOptions);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i(GlobalVar.TAG_FRAGMENT_HOME, "State changed from " + change.getPreviousState() + " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i(GlobalVar.TAG_FRAGMENT_HOME, "There was a problem connecting " + "\ncode" + code + "\nmessage" + message + "\nException" + e);
            }
        }, ConnectionState.ALL);


        Channel channel = pusher.subscribe("FollowMe_standby_number");
        channel.bind("FollowMe_standby_number", new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                getStandByNum();
            }
        });

        return binding.getRoot();
    } //onCreateView()

    //대기순번 가져옴
    public void getStandByNum() {

        String url = GlobalVar.URL + GlobalVar.URL_STANDBY_NUMBER;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            binding.standByNum.setText(String.format("%03d", jsonResponse.getInt("standby_number")));
                            binding.qrCodCardView.setVisibility(View.GONE);
                            binding.standbyNumCardView.setVisibility(View.VISIBLE);

                            Log.i(GlobalVar.TAG_FRAGMENT_HOME, "대기순번 요청 성공");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(GlobalVar.TAG_FRAGMENT_HOME, "대기순번 요청 실패" + e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(GlobalVar.TAG_FRAGMENT_HOME, "대기순번 요청 실패" + e.getMessage());
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
        AppHelper.requestQueue = Volley.newRequestQueue(getActivity()); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);

    } //receiveStandByNum()
}


