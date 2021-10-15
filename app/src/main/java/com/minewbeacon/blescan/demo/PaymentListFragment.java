package com.minewbeacon.blescan.demo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yuliwuli.blescan.demo.databinding.FragmentPaymentListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class PaymentListFragment extends Fragment {

    private FragmentPaymentListBinding binding;
    private ArrayList<ArrayList<PaymentRecord>> allPaymentList = new ArrayList();

    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();
    String startDate;
    String endDate;


    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


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


            endDate = dateFormat.format(endCalendar.getTime());
            binding.endDate.setText(endDate);
            Log.i("날짜", endDate + "시작");

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
        allPaymentList.clear();
        String url = GlobalVar.URL + GlobalVar.URL_STORAGE_RECORD;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray storage_record = jsonObject.getJSONArray("storage_record");
                            int totalMoney = 0;

                            Log.i("RECO",storage_record.toString());


                            for (int i = 0; i < storage_record.length(); i++) {
                                ArrayList<PaymentRecord> paymentRecords = new ArrayList();
                                int dayTotal = 0;

                                for (int j = 0; j < storage_record.getJSONArray(i).length(); j++) {

                                    PaymentRecord paymentRecord = new PaymentRecord();
                                    paymentRecord.setPlace(storage_record.getJSONArray(i).getJSONObject(j).getString("clinic_subject_name"));
                                    paymentRecord.setPrice(storage_record.getJSONArray(i).getJSONObject(j).getInt("storage") + "");
                                    paymentRecord.setTime(storage_record.getJSONArray(i).getJSONObject(j).getString("clinic_time"));
                                    paymentRecord.setDate(storage_record.getJSONArray(i).getJSONObject(j).getString("clinic_date"));
                                    dayTotal += Integer.parseInt(paymentRecord.price);
                                    paymentRecord.setDayPrice(dayTotal + "");
                                    paymentRecords.add(paymentRecord);

                                }
                                totalMoney += dayTotal;
                                allPaymentList.add(paymentRecords);
                            }


                            PaymentRecordListAdapter paymentRecordListAdapter = new PaymentRecordListAdapter(getContext(), allPaymentList);
                            binding.totalPrice.setText(moneyFormatToWon(totalMoney));
                            binding.recyclerView.setHasFixedSize(true);
                            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                            binding.recyclerView.setAdapter(paymentRecordListAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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

    public static String moneyFormatToWon(int inputMoney) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        return decimalFormat.format(inputMoney);
    }


}
