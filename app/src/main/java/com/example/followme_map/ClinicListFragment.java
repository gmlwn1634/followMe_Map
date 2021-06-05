package com.example.followme_map;

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

import com.example.followme_map.databinding.FragmentPaymentListBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ClinicListFragment extends Fragment {

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


        return binding.getRoot();
    }

}
