package com.minewbeacon.blescan.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.yuliwuli.blescan.demo.R;

import java.util.ArrayList;

public class PaymentRecordAdapter extends RecyclerView.Adapter<PaymentRecordAdapter.PaymentRecordViewHolder> {

    private ArrayList<PaymentRecord> dataList;

    public PaymentRecordAdapter(ArrayList<PaymentRecord> data) {
        this.dataList = data;
    }

    public class PaymentRecordViewHolder extends RecyclerView.ViewHolder {

        protected TextView time;
        protected TextView place;
        protected TextView price;

        public PaymentRecordViewHolder(View view) {
            super(view);
            time = view.findViewById(R.id.time);
            place = view.findViewById(R.id.place);
            price = view.findViewById(R.id.price);
        }
    }

    public PaymentRecordViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.payment_record_list_row, null);

        return new PaymentRecordAdapter.PaymentRecordViewHolder(v);
    }


    public void onBindViewHolder(PaymentRecordViewHolder paymentRecordViewHolder, int position) {
        paymentRecordViewHolder
                .time
                .setText(dataList.get(position).time);
        paymentRecordViewHolder
                .place
                .setText(dataList.get(position).place);
//        paymentRecordViewHolder
//                .price
//                .setText(dataList.get(position).price);


    }

    public int getItemCount() {
        return dataList.size();
    }

}