package com.example.poha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poha.model.Record;

import java.util.List;

public class MyResultsAdapter extends RecyclerView.Adapter<MyResultsAdapter.ViewHolder>{
    private List<Record> list;

    public MyResultsAdapter(List<Record> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_results, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /*int min = list.get(position).getTime().getMin();
        int sec = list.get(position).getTime().getSec();
        int millisec = list.get(position).getTime().getMillisec();*/
        holder.tvTime.setText(String.format("%02d", list.get(position).getTime().getMin()) + ":" +
                              String.format("%02d", list.get(position).getTime().getSec()) + ":" +
                              String.format("%02d", list.get(position).getTime().getMillisec()));
        holder.tvDistance.setText(String.valueOf(list.get(position).getDistance()) + R.string.m);
        holder.tvAvgSpeed.setText(String.valueOf(list.get(position).getSpeed()) + R.string.m_s);
    }

    //String.format("%02d", min) + ":" + String.format("%02d", sec) + ":" +
    //                                String.format("%02d", millisec)

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvDistance, tvAvgSpeed;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.text_time);
            tvDistance = itemView.findViewById(R.id.text_distance);
            tvAvgSpeed = itemView.findViewById(R.id.text_avg_speed);
        }
    }
}
