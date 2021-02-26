package com.example.followme_map;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd");


    //시작날짜 선택기
    DatePickerDialog.OnDateSetListener setStartDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, month);
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            binding.startDate.setText(dateFormat.format(startCalendar.getTime()));
        }
    };

    //끝날짜 선택기

    DatePickerDialog.OnDateSetListener setEndDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, month);
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            binding.endDate.setText(dateFormat.format(endCalendar.getTime()));
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentListBinding.inflate(inflater);

        //기본 설정 날짜 : 한달 전 ~ 오늘
        startCalendar.add(Calendar.MONTH, -1);
        binding.startDate.setText(dateFormat.format(startCalendar.getTime()));
        binding.endDate.setText(dateFormat.format(endCalendar.getTime()));

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
