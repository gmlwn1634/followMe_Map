package com.example.followme_map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class DestViewHolder extends RecyclerView.ViewHolder {

        TextView from;
        TextView to;

        public DestViewHolder(@NonNull final View itemView) {
            super(itemView);
            from = itemView.findViewById(R.id.from);
            to = itemView.findViewById(R.id.to);
        }
    }

    private ArrayList<DestInfo> destInfoArrayList;

    DestAdapter(ArrayList<DestInfo> destInfoArrayList) {
        this.destInfoArrayList = destInfoArrayList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dest_row, parent, false);
        return new DestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DestViewHolder destViewHolder = (DestViewHolder) holder;
        destViewHolder.from.setText(destInfoArrayList.get(position).from);
        destViewHolder.to.setText(destInfoArrayList.get(position).to);
    }

    @Override
    public int getItemCount() {
        return destInfoArrayList.size();
    }
}
