package com.minewbeacon.blescan.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yuliwuli.blescan.demo.databinding.ActivityPaymentBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {


    private ActivityPaymentBinding binding;
    private int totalPrice;
    public static Activity paymentActivity;

    //recyclerView
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<PaymentInfo> paymentInfoArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        paymentActivity = PaymentActivity.this;

        //오늘 날짜로
        binding.today.setText(new SimpleDateFormat("yyyy - MM - dd", Locale.getDefault()).format(Calendar.getInstance().getTime()));


        //recyclerView
        binding.recyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 1);
        binding.recyclerView.setLayoutManager(mLayoutManager);

        //volley 통신 결제 내역 받아오기
        getPayList();

        binding.goToPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentActivity.this, ImPortActivity.class);
                startActivity(intent);
            }
        });


    } //onCreate()

    public static String moneyFormatToWon(int inputMoney) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        return decimalFormat.format(inputMoney);
    }


    //결제내역 가져옴
    protected void getPayList() {

        String url = GlobalVar.URL + GlobalVar.URL_STORAGE;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            JSONArray paymentArr = jsonResponse.getJSONArray("storage");
                            for (int i = 0; i < paymentArr.length(); i++) {
                                JSONObject paymentObj = paymentArr.getJSONObject(i);
                                paymentInfoArrayList.add(new PaymentInfo(paymentObj.getString("clinic_time"), paymentObj.getString("clinic_subject_name"), moneyFormatToWon(paymentObj.getInt("storage"))));
                                totalPrice += paymentObj.getInt("storage");
                            }

                            PaymentInfoAdapter paymentInfoAdapter = new PaymentInfoAdapter(paymentInfoArrayList);
                            binding.recyclerView.setAdapter(paymentInfoAdapter);


                            //결제 금액이 없을 때
                            if (totalPrice <= 0)
                                binding.noPayment.setVisibility(View.VISIBLE);
                            else
                                binding.goToPayBtn.setVisibility(View.VISIBLE);

                            binding.price.setText(moneyFormatToWon(totalPrice));


                            Log.i(GlobalVar.TAG_ACTIVITY_PAYMENT, "서버에 결제내역 요청 성공" + paymentInfoArrayList.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(GlobalVar.TAG_ACTIVITY_PAYMENT, "서버에 결제내역 요청 실패" + e.getMessage());
                        }



                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(GlobalVar.TAG_ACTIVITY_PAYMENT, "서버에 결제내역 요청 실패" + e.getMessage());
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
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);
    } //getPayList()


}