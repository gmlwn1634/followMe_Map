package com.minewbeacon.blescan.demo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.yuliwuli.blescan.demo.R;

import java.util.ArrayList;

public class DestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class DestViewHolder extends RecyclerView.ViewHolder {

        TextView num;
        TextView from;
        TextView to;

        public DestViewHolder(@NonNull final View itemView, final FlowActivity flowActivity) {
            super(itemView);
            num = itemView.findViewById(R.id.num);
            from = itemView.findViewById(R.id.from);
            to = itemView.findViewById(R.id.to);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        //클릭 이벤트 처리
                        Log.i(GlobalVar.TAG_ADAPTER_DEST, getAdapterPosition() + "");
                        flowActivity.getNFlowNode(getAdapterPosition());

                    }
                }
            });

        }
    }

    private ArrayList<DestInfo> destInfoArrayList;
    private FlowActivity flowActivity;

    DestAdapter(FlowActivity flowActivity, ArrayList<DestInfo> destInfoArrayList) {
        this.flowActivity = flowActivity;
        this.destInfoArrayList = destInfoArrayList;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dest_row, parent, false);
        return new DestViewHolder(v, flowActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DestViewHolder destViewHolder = (DestViewHolder) holder;
        destViewHolder.num.setText(destInfoArrayList.get(position).num);
        destViewHolder.from.setText(destInfoArrayList.get(position).from);
        destViewHolder.to.setText(destInfoArrayList.get(position).to);
    }

    @Override
    public int getItemCount() {
        return destInfoArrayList.size();
    }
}
