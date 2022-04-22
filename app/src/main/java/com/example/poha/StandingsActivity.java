package com.example.poha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.poha.adapter.MyResultsAdapter;
import com.example.poha.adapter.StandingsAdapter;
import com.example.poha.model.Record;
import com.example.poha.model.RecordUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StandingsActivity extends AppCompatActivity {

    private List<RecordUser> list;
    private Record record;
    private StandingsAdapter adapter;
    private RecyclerView recyclerView;

    private FirebaseUser user;
    private FirebaseDatabase rootNode;
    private DatabaseReference recordsRef;
    private Query recordsQuery;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standings);

        recyclerView = findViewById(R.id.rec_standings);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        user = FirebaseAuth.getInstance().getCurrentUser();
        rootNode = FirebaseDatabase.getInstance();
        userID = user.getUid();
        recordsRef = rootNode.getReference("Records");

        list = new ArrayList<>();
        adapter = new StandingsAdapter(list);
        recyclerView.setAdapter(adapter);
        recordsQuery = recordsRef.orderByChild("speed");

        recordsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dss: snapshot.getChildren()){
                    RecordUser recordUser = dss.getValue(RecordUser.class);
                    /*if(list.size() > 0){
                        for(int i = 0; i < list.size(); i++){
                            if (recordUser.getUsername() != null && list.get(i).getUsername().equals(recordUser.getUsername())) {
                                List<RecordUser> list2 = new ArrayList<>();
                                list2.add(list.get(i));
                            }
                        }
                    }*/
                    list.add(recordUser);
                }
                Collections.sort(list, (p1, p2) -> p1.getUsername().compareTo(p2.getUsername()));
                Collections.reverse(list);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Načtení záznamů z databáze selhalo.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}