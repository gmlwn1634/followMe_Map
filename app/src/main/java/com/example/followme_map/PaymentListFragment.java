package com.example.followme_map;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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
import com.example.followme_map.databinding.FragmentPaymentListBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PaymentListFragment extends Fragment {

    private FragmentPaymentListBinding binding;

    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();
    String startDate;
    String endDate;

    SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");


    //시작날짜 선택기
    DatePickerDialog.OnDateSetListener setStartDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, month);
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            startDate = dateFormat.format(startCalendar.getTime());
            binding.startDate.setText(startDate);
            Log.i("날짜", startDate + "시작");
        }
    };

    //끝날짜 선택기

    DatePickerDialog.OnDateSetListener setEndDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, month);
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


            startDate = dateFormat.format(startCalendar.getTime());
            binding.startDate.setText(startDate);
            Log.i("날짜", startDate + "시작");

        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentListBinding.inflate(inflater);

        //기본 설정 날짜 : 한달 전 ~ 오늘
        startCalendar.add(Calendar.MONTH, -1);
        startDate = dateFormat.format(startCalendar.getTime());
        endDate = dateFormat.format(endCalendar.getTime());
        binding.startDate.setText(startDate);
        binding.endDate.setText(endDate);

        //시작 날짜 설정
        binding.startDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), setStartDate, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //끝 날짜 설정
        binding.endDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), setEndDate, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPayListApi();

            }
        });


        return binding.getRoot();
    }

    public void searchPayListApi() {
        String url = GlobalVar.URL + GlobalVar.URL_STORAGE_RECORD;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("결제내역",jsonObject.toString());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                        Log.i(GlobalVar.TAG_FRAGMENT_INFO, response);
//                        Toast.makeText(context, "로그아웃에 성공하였습니다.", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(context, MainActivity2.class);
//                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(GlobalVar.TAG_FRAGMENT_INFO, "결제내역 불러오기 실패" + e.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + LoginActivity.patientToken);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("start_date", startDate);
                params.put("end_date", endDate);


                return params;
            }
        };


        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(getContext()); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);
    }


}
