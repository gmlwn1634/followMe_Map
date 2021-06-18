package com.example.followme_map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
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


        public PaymentRecordListViewHolder(View view) {
            super(view);
            this.recyclerView = (RecyclerView) view.findViewById(R.id.payRecRecycler);

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

    }

    @Override
    public int getItemCount() {
        return AllPayment.size();
    }

}
