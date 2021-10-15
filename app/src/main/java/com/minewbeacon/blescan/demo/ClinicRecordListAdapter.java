package com.minewbeacon.blescan.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yuliwuli.blescan.demo.R;

import java.util.ArrayList;

public class ClinicRecordListAdapter extends RecyclerView.Adapter<ClinicRecordListAdapter.ClinicRecordListViewHolder> {

    private ArrayList<ArrayList<ClinicRecord>> allClinic;
    private Context context;

    public ClinicRecordListAdapter(Context context, ArrayList<ArrayList<ClinicRecord>> data) {
        this.context = context;
        this.allClinic = data;
    }


    public class ClinicRecordListViewHolder extends RecyclerView.ViewHolder {
        protected RecyclerView recyclerView;
        protected TextView date;


        public ClinicRecordListViewHolder(View view) {
            super(view);
            this.recyclerView = (RecyclerView) view.findViewById(R.id.clinicRecRecycler);
            this.date = (TextView) view.findViewById(R.id.date);

        }
    }


    @Override
    public ClinicRecordListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clinic_record_list, null);
        return new ClinicRecordListAdapter.ClinicRecordListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ClinicRecordListAdapter.ClinicRecordListViewHolder holder, int position) {

        ClinicRecordAdapter adapter = new ClinicRecordAdapter(allClinic.get(position));
        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.recyclerView.setAdapter(adapter);
        holder.date.setText(allClinic.get(position).get(0).date);
    }

    @Override
    public int getItemCount() {
        return allClinic.size();
    }

}