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

public class PaymentRecordListAdapter extends RecyclerView.Adapter<PaymentRecordListAdapter.PaymentRecordListViewHolder> {

    private ArrayList<ArrayList<PaymentRecord>> AllPayment;
    private Context context;

    public PaymentRecordListAdapter(Context context, ArrayList<ArrayList<PaymentRecord>> data) {
        this.context = context;
        this.AllPayment = data;
    }


    public class PaymentRecordListViewHolder extends RecyclerView.ViewHolder {
        protected RecyclerView recyclerView;
        protected TextView date;
        protected TextView totalPrice;


        public PaymentRecordListViewHolder(View view) {
            super(view);
            this.recyclerView = (RecyclerView) view.findViewById(R.id.payRecRecycler);
            this.date = (TextView) view.findViewById(R.id.date);
            this.totalPrice = (TextView) view.findViewById(R.id.totalPrice);

        }
    }


    @Override
    public PaymentRecordListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_record_list, null);
        return new PaymentRecordListAdapter.PaymentRecordListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PaymentRecordListAdapter.PaymentRecordListViewHolder holder, int position) {

        PaymentRecordAdapter adapter = new PaymentRecordAdapter(AllPayment.get(position));
        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.recyclerView.setAdapter(adapter);
        holder.date.setText(AllPayment.get(position).get(0).date);
        holder.totalPrice.setText(AllPayment.get(position).get(adapter.getItemCount()-1).dayPrice);

    }

    @Override
    public int getItemCount() {
        return AllPayment.size();
    }

}
