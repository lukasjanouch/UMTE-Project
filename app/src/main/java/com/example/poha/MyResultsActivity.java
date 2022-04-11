package com.example.poha;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.poha.model.Record;

import java.util.ArrayList;
import java.util.List;

public class MyResultsActivity extends AppCompatActivity {

    private List<Record> list;
    private MyResultsAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_results);

        recyclerView = findViewById(R.id.rec_MyResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        /*list.add(new OrdersModel("002131","Připravena k odeslání"));
        list.add(new OrdersModel("002132", "Vyřízena"));
        list.add(new OrdersModel("002133", "Vyřízena"));*/
        adapter = new MyResultsAdapter(list);
        recyclerView.setAdapter(adapter);
    }
}