package com.minewbeacon.blescan.demo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
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
import com.yuliwuli.blescan.demo.R;
import com.yuliwuli.blescan.demo.databinding.FragmentHomeBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;
    private Pusher pusher;
    private int eventPatientID;  //안내재생
    private MediaPlayer mediaPlayer;
    private int mode;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater);
        binding.name.setText(LoginActivity.patientName);
        binding.patientId.setText(LoginActivity.patientId + "");

        binding.flowStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = 1;
                checkBluetooth();
            }
        });

        binding.DestSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = 2;
                checkBluetooth();
            }
        });


        binding.paymentBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaymentActivity.class);
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
                                //진료 종료되었을 때

                                binding.qrCodCardView.setVisibility(View.VISIBLE);
                                binding.standbyNumCardView.setVisibility(View.INVISIBLE);
                            } else if (standByNum != 0) {
                                //진료 접수했을 때


                                binding.qrCodCardView.setVisibility(View.INVISIBLE);
                                binding.standbyNumCardView.setVisibility(View.VISIBLE);

                                if (standByNum == 1) {
                                    //대기순번 1일 때

                                    // 안내 메세지
                                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.standby_sound);
                                    mediaPlayer.start();

                                    final CustomDialog customDialog = new CustomDialog(getContext(), new CustomDialogClickListener() {
                                        @Override
                                        public void onPositiveClick() {
                                            return;
                                        }
                                    }, jsonResponse.getString("message"), "");
                                    customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                                    customDialog.setCancelable(false);
                                    customDialog.show();
                                } else {
                                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.scan_sound);
                                    mediaPlayer.start();
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

    private void checkBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "해당 기기는 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
        }

        //블루투스가 켜져있으면 로그인화면으로 이동
        if (bluetoothAdapter.isEnabled()) {
            if (mode == 1) {
                Intent intent = new Intent(getContext(), FlowActivity.class);
                startActivity(intent);
            } else if (mode == 2) {
                Intent intent = new Intent(getActivity(), DestSearchActivity.class);
                startActivity(intent);
            }

        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, RESULT_CANCELED);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getContext(), "블루투스를 활성화 해주세요.", Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_OK) {
            if (mode == 1) {
                Intent intent = new Intent(getActivity(), FlowActivity.class);
                startActivity(intent);
            } else if (mode == 2) {
                Intent intent = new Intent(getActivity(), DestSearchActivity.class);
                startActivity(intent);
            }
        }
    }//onActivityResult()


}
