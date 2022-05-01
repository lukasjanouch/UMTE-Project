package com.example.poha;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.poha.model.Record;
import com.example.poha.model.RecordUser;
import com.example.poha.model.Time;
import com.example.poha.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

import org.apache.commons.math3.util.Precision;

import java.util.List;

public class SensorActivity extends AppCompatActivity {
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private static final int PERMISSIONS_COARSE_LOCATION = 98;
    private static final int DEFAULT_UPDATE_INTERVAL = 30;
    private static final int FASTEST_UPDATE_INTERVAL = 5;

    private TextView tvStartLat, tvStartLon, tvDistance, tvCurrentLat, tvCurrentLon;
    private ImageButton btnStart, btnStop;
    //GPS
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private Location startLocation;
    private Location currentLocation;
    private double distance, newDistance;
    //Čas
    private Chronometer chronometer;
    private Handler handler;
    private boolean isResume;
    private long tMillisec, tStart, tBuff, tUpdate = 0L;
    private int sec, min, millisec;
    //Firebase
    private FirebaseUser user;
    private FirebaseDatabase rootNode;
    private DatabaseReference recordsInUserRef;
    private DatabaseReference usersRef;
    private DatabaseReference recordsRef;
    private Query recordsQuery;
    private String userID;

    private Time time;
    private Record record;
    private RecordUser recordUser;
    private String username;
    private List<RecordUser> listRecUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        tvStartLat = findViewById(R.id.tv_lat2);
        tvStartLon = findViewById(R.id.tv_lon2);
        tvCurrentLat = findViewById(R.id.tv_lat_current);
        tvCurrentLon = findViewById(R.id.tv_lon_current);
        tvDistance = findViewById(R.id.tv_distance);

        chronometer = findViewById(R.id.chronometer);

        btnStart = findViewById(R.id.imgbtn_start);
        btnStop = findViewById(R.id.imgbtn_stop);

        handler = new Handler();

        locationRequest = new LocationRequest();
        //set all properties of Location Request
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        user = FirebaseAuth.getInstance().getCurrentUser();
        rootNode = FirebaseDatabase.getInstance();
        userID = user.getUid();
        recordsInUserRef = rootNode.getReference("Users/" + userID + "/Records");
        usersRef = rootNode.getReference("Users");
        recordsRef = rootNode.getReference("Records");
        recordsQuery = recordsRef.orderByChild("speed");

        //získání uživatelského jména přihlášeného uživatele
        usersRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if(userProfile != null){
                    username = userProfile.getUserName();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SensorActivity.this,
                        "Pro vytvoření záznamu se nepodařilo se zjistit vaše uživatelské jméno.", Toast.LENGTH_LONG).show();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                if(!isResume){
                    //gps - vzdálenost
                    getStartLocation();
                    startLocationUpdates();
                    //čas
                    tStart = SystemClock.uptimeMillis();
                    handler.postDelayed(runnable, 0);
                    chronometer.start();

                    isResume = true;
                    btnStop.setVisibility(View.GONE);
                    btnStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
                }else{
                    tBuff += tMillisec;
                    handler.removeCallbacks(runnable);
                    chronometer.stop();
                    isResume = false;
                    btnStop.setVisibility(View.VISIBLE);
                    btnStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
                }
            }
        });


        btnStop.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                //gps
                fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
                //čas
                if(!isResume){
                    btnStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
                    //výpočet průměrné rychlosti a uložení záznamu (record) pro aktivitu MyResults do databáze
                    time = new Time(min, sec, millisec);
                    int timeInSec = (millisec / 1000) + sec + (min * 60);
                    double avgSpeed = (distance * 1000) / timeInSec;
                    avgSpeed = Precision.round(avgSpeed, 2);
                    //avgSpeed = 1.1;
                    record = new Record(distance, time, avgSpeed);
                    DatabaseReference newRecordInUserRef = recordsInUserRef.push();
                    newRecordInUserRef.setValue(record).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "Záznam byl úspěšně nahrán do databáze", Toast.LENGTH_LONG).show();
                        }
                    });
                    String key = newRecordInUserRef.getKey();
                    newRecordInUserRef.child("id").setValue(key);
                    // We can also chain the two calls together
                    //recordsRef.push().setValueAsync(record);
                    //Záznam pro aktivitu Standings
                    recordUser = new RecordUser(username, distance, time, avgSpeed);

                    recordsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                RecordUser recordUserLoc = snapshot.getValue(RecordUser.class);
                                //jestliže je rychlost záznamu v databázi MENŠÍ nebo ROVNA, záznam se nahradí novým
                                if(recordUserLoc != null && recordUserLoc.getUsername().equals(username)
                                        && recordUserLoc.getSpeed() <= recordUser.getSpeed()){
                                    snapshot.getRef().removeValue();
                                    snapshot.getRef().setValue(recordUser);
                                }}}
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(getApplicationContext(), "Načtení záznamů z databáze selhalo.", Toast.LENGTH_SHORT).show();
                                }
                            });

/*
                    DatabaseReference newRecordsRef = recordsRef.push();
                    newRecordsRef.setValue(recordUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "Záznam byl úspěšně nahrán do databáze", Toast.LENGTH_LONG).show();
                        }
                    });*/

                    tMillisec = 0L;
                    tStart = 0L;
                    tBuff = 0L;
                    tUpdate = 0L;
                    sec = 0;
                    min = 0;
                    millisec = 0;
                    chronometer.setText("00:00:00");
                }
            }
        });
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            tMillisec = SystemClock.uptimeMillis() - tStart;
            tUpdate = tBuff + tMillisec;
            sec = (int) (tUpdate/1000);
            min = sec / 60;
            sec = sec % 60;
            millisec = (int) (tUpdate/100);
            chronometer.setText(String.format("%02d", min) + ":" + String.format("%02d", sec) + ":" +
                                String.format("%02d", millisec));
            handler.postDelayed(this, 60);
        }
    };

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startLocationUpdates() {
        //tvUpdates.setText("Poloha je sledována.");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        getDistanceFromStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getStartLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SensorActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    startLocation = location;
                    tvStartLat.setText(String.valueOf(startLocation.getLatitude()));
                    tvStartLon.setText(String.valueOf(startLocation.getLongitude()));
                }
            });
        }else{
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_COARSE_LOCATION);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getDistanceFromStart(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SensorActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //testování
                    //location.setLatitude(52);
                    //location.setLongitude(17);
                    updateUIValues(location);
                }
            });
        }else{
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }
    private void updateUIValues(Location location){
        currentLocation = location;
        tvCurrentLat.setText(String.valueOf(currentLocation.getLatitude()));
        tvCurrentLon.setText(String.valueOf(currentLocation.getLongitude()));
        //distance = currentLocation.distanceTo(startLocation);
        //distance = distance1/1000;
        //distance = Precision.round(distance1, 3);
        newDistance = distance;
        float[] results = new float[1];
        Location.distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
                currentLocation.getLatitude(), currentLocation.getLongitude(), results);
        distance = results[0];
        if(distance < newDistance){
            startLocation = currentLocation;
            float[] results2 = new float[1];
            Location.distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
                    currentLocation.getLatitude(), currentLocation.getLongitude(), results2);
            newDistance = results2[0];
            distance = distance + newDistance;
        }
        distance = distance /1000;
        //zaokrouhlení na 3 des. místa
        distance = Precision.round(distance, 3);

        String distanceStr = String.valueOf(distance) + " km";
        tvDistance.setText(distanceStr);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_FINE_LOCATION:
            case PERMISSIONS_COARSE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getStartLocation();
                }else{
                    Toast.makeText(this, "Aby aplikace mohla správně fungovat, potřebuje povolení ke zjišťování Vaší polohy.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}