package com.example.followme_map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.example.followme_map.databinding.FragmentHomeBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
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
    private Pusher pusher;
    private int eventPatientID;


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

        binding.DestSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DestSearchActivity.class);
                startActivity(intent);
            }
        });

        //QR코드 생성
        createQRCord(LoginActivity.patientToken);

        //대기순번
        laravelPusher();

        return binding.getRoot();
    } //onCreateView()

    //대기순번
    public void laravelPusher() {


        //대기순번 laravel-pusher
        PusherOptions pusherOptions = new PusherOptions();
        pusherOptions.setCluster("ap3");
        pusher = new Pusher("7ed3a4ce8ebfe9741f98", pusherOptions);

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


                try {
                    JSONObject jsonObject = new JSONObject(event.getData());
                    Log.i("대기순번 확인", "pusher : " + jsonObject.getInt("event"));
                    new Exception();
                    eventPatientID = jsonObject.getInt("event");
                    // qr코드 인식 시 0 받음
                    // 진료 종료시 종료된 사람 회원번호 받음
                    //event 수신될 때마다 http통신으로 standby 컬럼 값 받음
                    // event랑 현 핸드폰 회원번호랑 같으면
                    getStandByNum();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    }


    //대기순번 가져옴
    public void getStandByNum() {

        String url = GlobalVar.URL + GlobalVar.URL_STANDBY_NUMBER;
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            Log.i("대기순번 확인", "http:" + jsonResponse.toString());

                            int standByNum = jsonResponse.getInt("standby_number");
                            binding.standByNum.setText(String.format("%03d", standByNum));
                            JSONObject clinicInfo = jsonResponse.getJSONObject("clinic_info");

                            binding.clinicPlace.setText(clinicInfo.getString("clinic_subject_name"));
                            binding.acceptTime.setText(clinicInfo.getString("clinic_time"));


                            if (LoginActivity.patientId == eventPatientID) {
                                binding.qrCodCardView.setVisibility(View.VISIBLE);
                                binding.standbyNumCardView.setVisibility(View.INVISIBLE);
                            } else if (standByNum != 0) {

                                binding.qrCodCardView.setVisibility(View.INVISIBLE);
                                binding.standbyNumCardView.setVisibility(View.VISIBLE);

                                if (jsonResponse.getInt("standby_number") == 1) {
                                    // 안내 메세지
                                    final CustomDialog customDialog = new CustomDialog(getContext(), new CustomDialogClickListener() {
                                        @Override
                                        public void onPositiveClick() {
                                            return;
                                        }
                                    }, jsonResponse.getString("message"), "");
                                    customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                                    customDialog.setCancelable(false);
                                    customDialog.show();
                                }

                                Log.i("대기순번", jsonResponse.getString("message"));
                                Log.i(GlobalVar.TAG_FRAGMENT_HOME, "대기순번 요청 성공");
                            }


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


    //회원번호를 담은 QR코드 생성
    public void createQRCord(String data) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data + "", BarcodeFormat.QR_CODE, 1000, 1000);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            binding.qrImg.setImageBitmap(bitmap);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pusher.disconnect();
    } //onDestroy()

}


