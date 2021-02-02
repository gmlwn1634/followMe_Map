package com.example.followme_map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class PaymentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {

        TextView time;
        TextView place;
        TextView price;

        public PaymentViewHolder(@NonNull final View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            place = itemView.findViewById(R.id.place);
            price = itemView.findViewById(R.id.totalPrice);
        }
    }


    private ArrayList<PaymentInfo> paymentInfoArrayList;

    PaymentAdapter(ArrayList<PaymentInfo> paymentInfoArrayList) {
        this.paymentInfoArrayList = paymentInfoArrayList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_row, parent, false);
        return new PaymentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PaymentViewHolder paymentViewHolder = (PaymentViewHolder) holder;
        paymentViewHolder.time.setText(paymentInfoArrayList.get(position).time);
        paymentViewHolder.place.setText(paymentInfoArrayList.get(position).place);
        paymentViewHolder.price.setText(Integer.toString(paymentInfoArrayList.get(position).price));
    }

    @Override
    public int getItemCount() {
        return paymentInfoArrayList.size();
    }
}
