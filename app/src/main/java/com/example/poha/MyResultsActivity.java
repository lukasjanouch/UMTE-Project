package com.example.poha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.poha.model.Record;
import com.example.poha.model.Time;
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

public class MyResultsActivity extends AppCompatActivity {

    private List<Record> list;
    private Record record;
    private MyResultsAdapter adapter;
    private RecyclerView recyclerView;

    private FirebaseUser user;
    private FirebaseDatabase rootNode;
    private DatabaseReference recordsRef;
    private Query recordsQuery;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_results);

        recyclerView = findViewById(R.id.rec_MyResults);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        user = FirebaseAuth.getInstance().getCurrentUser();
        rootNode = FirebaseDatabase.getInstance();
        userID = user.getUid();
        recordsRef = rootNode.getReference("Users/" + userID + "/Records");

        list = new ArrayList<>();
        adapter = new MyResultsAdapter(list);
        recyclerView.setAdapter(adapter);
        recordsQuery = recordsRef.orderByChild("speed");

        recordsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dss: snapshot.getChildren()){
                        Record record = dss.getValue(Record.class);
                        list.add(record);

                    }
                    Collections.reverse(list);
                    adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Načtení záznamů z databáze selhalo.", Toast.LENGTH_SHORT).show();
            }
        });
        /*Time time = new Time(1, 25, 34);
        list.add(new Record(2.54, time, 0.45));

        Time time2 = new Time(1, 16, 25);
        list.add(new Record(3.98, time2, 0.57));
        /*list.add(new OrdersModel("002131","Připravena k odeslání"));
        list.add(new OrdersModel("002132", "Vyřízena"));
        list.add(new OrdersModel("002133", "Vyřízena"));*/

    }
}