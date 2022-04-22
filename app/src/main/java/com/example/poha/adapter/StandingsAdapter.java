package com.example.poha.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poha.R;
import com.example.poha.model.Record;
import com.example.poha.model.RecordUser;

import java.util.List;

public class StandingsAdapter extends RecyclerView.Adapter<StandingsAdapter.ViewHolder>{
    private List<RecordUser> list;


    public StandingsAdapter(List<RecordUser> list){
        this.list = list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_standings, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /*int min = list.get(position).getTime().getMin();
        int sec = list.get(position).getTime().getSec();
        int millisec = list.get(position).getTime().getMillisec();*/
        holder.tvUsername.setText(list.get(position).getUsername());
        holder.tvTime.setText(String.format("%02d", list.get(position).getTime().getMin()) + ":" +
                String.format("%02d", list.get(position).getTime().getSec()) + ":" +
                String.format("%02d", list.get(position).getTime().getMillisec()));
        holder.tvDistance.setText(String.valueOf(list.get(position).getDistance()) + " km");
        holder.tvAvgSpeed.setText(String.valueOf(list.get(position).getSpeed()) + " m/s");
    }

    //String.format("%02d", min) + ":" + String.format("%02d", sec) + ":" +
    //                                String.format("%02d", millisec)

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvTime, tvDistance, tvAvgSpeed;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_st_username);
            tvTime = itemView.findViewById(R.id.tv_st_time);
            tvDistance = itemView.findViewById(R.id.tv_st_distance);
            tvAvgSpeed = itemView.findViewById(R.id.tv_st_avg_speed);
        }
    }
}

