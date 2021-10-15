package com.minewbeacon.blescan.demo;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.yuliwuli.blescan.demo.R;

import java.util.ArrayList;

public class ClinicRecordAdapter extends RecyclerView.Adapter<ClinicRecordAdapter.ClinicRecordViewHolder> {

    private ArrayList<ClinicRecord> dataList;

    public ClinicRecordAdapter(ArrayList<ClinicRecord> data) {
        this.dataList = data;
    }

    public class ClinicRecordViewHolder extends RecyclerView.ViewHolder {

        protected TextView time;
        protected TextView place;
        protected TextView price;

        public ClinicRecordViewHolder(View view) {
            super(view);
            time = view.findViewById(R.id.time);
            place = view.findViewById(R.id.place);
            price = view.findViewById(R.id.price);
        }
    }

    public ClinicRecordViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.clinic_record_list_row, null);

        return new ClinicRecordAdapter.ClinicRecordViewHolder(v);
    }


    public void onBindViewHolder(ClinicRecordViewHolder clinicRecordViewHolder, int position) {
        clinicRecordViewHolder
                .time
                .setText(dataList.get(position).time);
        clinicRecordViewHolder
                .place
                .setText(dataList.get(position).place);


    }

    public int getItemCount() {
        return dataList.size();
    }

}
