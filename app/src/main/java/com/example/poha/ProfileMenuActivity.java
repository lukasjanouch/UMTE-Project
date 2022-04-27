package com.example.poha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.poha.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileMenuActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private Button btnNewRun, btnMyResults, btnStandings, btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_menu);

        btnNewRun = findViewById(R.id.btn_new_run);
        btnNewRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileMenuActivity.this, SensorActivity.class));
            }
        });

        btnMyResults = findViewById(R.id.btn_my_results);
        btnMyResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileMenuActivity.this, MyResultsActivity.class));
            }
        });

        btnStandings = findViewById(R.id.btn_standings);
        btnStandings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileMenuActivity.this, StandingsActivity.class));
            }
        });

        btnLogout = (Button) findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileMenuActivity.this, MainActivity.class));
            }
        });
        //zobrazení uživatelského jména uživatele v této aktivitě
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        final TextView textViewUserName = (TextView) findViewById(R.id.textViewUserName);

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if(userProfile != null){
                    String userName = userProfile.getUserName();
                    textViewUserName.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileMenuActivity.this, "Něco se pokazilo.", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void onBackPressed() {
        Toast.makeText(ProfileMenuActivity.this, "Chcete-li se odhlásit, klepněte na tlačítko Odhlásít se.", Toast.LENGTH_LONG).show();
    }
}