package com.example.poha.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poha.R;
import com.example.poha.model.Record;
import com.example.poha.model.RecordUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

public class MyResultsAdapter extends RecyclerView.Adapter<MyResultsAdapter.ViewHolder>{
    private List<Record> list;

    private FirebaseUser user;
    private FirebaseDatabase rootNode;
    private DatabaseReference recordsInUserRef;
    private Query recordsQuery;
    private String userID, key;

    public MyResultsAdapter(List<Record> list, Query recordsQuery){
        this.list = list;
        this.recordsQuery = recordsQuery;
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
        holder.tvStandings.setText(String.valueOf(position + 1) + ".");
        //String.format("%02d", min) + ":" + String.format("%02d", sec) + ":" +
        //                                String.format("%02d", millisec)
        holder.tvTime.setText(String.format("%02d", list.get(position).getTime().getMin()) + ":" +
                              String.format("%02d", list.get(position).getTime().getSec()) + ":" +
                              String.format("%02d", list.get(position).getTime().getMillisec()));
        holder.tvDistance.setText(String.valueOf(list.get(position).getDistance()) + " km");
        holder.tvAvgSpeed.setText(String.valueOf(list.get(position).getSpeed()) + " m/s");
        holder.tvRemoveRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Record record = list.get(holder.getAdapterPosition());//ne jen position, protože pak nahoře červená
                recordsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            Record recordLoc = snapshot.getValue(Record.class);
                            //
                            if(recordLoc != null && recordLoc.getId().equals(record.getId())){
                                snapshot.getRef().removeValue();
                                notifyDataSetChanged();
                             }
                        list.clear();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(holder.itemView.getContext(), "Načtení záznamů z databáze selhalo.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStandings, tvTime, tvDistance, tvAvgSpeed, tvRemoveRecord;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStandings = itemView.findViewById(R.id.text_standings);
            tvTime = itemView.findViewById(R.id.text_time);
            tvDistance = itemView.findViewById(R.id.text_distance);
            tvAvgSpeed = itemView.findViewById(R.id.text_avg_speed);
            tvRemoveRecord = itemView.findViewById(R.id.text_remove_record);

        }
    }
}
