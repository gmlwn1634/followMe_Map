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
import com.yuliwuli.blescan.demo.databinding.FragmentClinicListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ClinicListFragment extends Fragment {

    private FragmentClinicListBinding binding;
    private ArrayList<ArrayList<ClinicRecord>> allClinicList = new ArrayList();


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
            Log.i("날짜", endDate + "끝");
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClinicListBinding.inflate(inflater);

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
                searchFlowListApi();

            }
        });


        return binding.getRoot();
    }

    public void searchFlowListApi() {
        allClinicList.clear();
        String url = GlobalVar.URL + GlobalVar.URL_FLOW_RECORD;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray flow_record = jsonObject.getJSONArray("flow_record");

                            Log.i("RECORD", flow_record.toString());


                            //날짜
                            //룸네임
                            //시간


                            for (int i = 0; i < flow_record.length(); i++) {
                                ArrayList<ClinicRecord> clinicRecords = new ArrayList();

                                for (int j = 0; j < flow_record.getJSONArray(i).length(); j++) {

                                    ClinicRecord clinicRecord = new ClinicRecord();
                                    clinicRecord.setPlace(flow_record.getJSONArray(i).getJSONObject(j).getJSONObject("room_location").getString("room_name"));
                                    clinicRecord.setDate(flow_record.getJSONArray(i).getJSONObject(j).getString("flow_create_date").substring(0,10));
                                    clinicRecord.setTime(flow_record.getJSONArray(i).getJSONObject(j).getString("flow_create_date").substring(11, 19));
                                    clinicRecords.add(clinicRecord);

                                }
                                allClinicList.add(clinicRecords);
                            }


                            ClinicRecordListAdapter clinicRecordListAdapter = new ClinicRecordListAdapter(getContext(), allClinicList);
                            binding.recyclerView.setHasFixedSize(true);
                            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                            binding.recyclerView.setAdapter(clinicRecordListAdapter);

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
