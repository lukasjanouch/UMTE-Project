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
import com.example.poha.model.Time;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.math3.util.Precision;

public class SensorActivity2 extends AppCompatActivity {
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private static final int DEFAULT_UPDATE_INTERVAL = 30;
    private static final int FASTEST_UPDATE_INTERVAL = 5;

    private TextView tvStartLat, tvStartLon, tvDistance1, tvDistance2, tvCurrentLat, tvCurrentLon, tvTime;
    private ImageButton btnStart, btnStop;
    //GPS
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private Location startLocation;
    private Location currentLocation;
    private double distance1, distance2;
    //Čas
    private Chronometer chronometer;
    private Handler handler;
    private boolean isResume;
    private long tMillisec, tStart, tBuff, tUpdate = 0L;
    private int sec, min, millisec;
    //Firebase
    private FirebaseUser user;
    private FirebaseDatabase rootNode;
    private DatabaseReference recordsRef;
    private String userID;

    private Time time;
    private Record record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor2);

        tvStartLat = findViewById(R.id.tv_lat2);
        tvStartLon = findViewById(R.id.tv_lon2);
        tvCurrentLat = findViewById(R.id.tv_lat_current);
        tvCurrentLon = findViewById(R.id.tv_lon_current);
        tvDistance1 = findViewById(R.id.tv_distance1);
        tvDistance2 =findViewById(R.id.tv_distance2);

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
        recordsRef = rootNode.getReference("Users/" + userID + "/Records");

        btnStart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                //gps - vzdálenost
                getStartLocation();
                startLocationUpdates();
                if(!isResume){
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
                    //výpočet průměrné rychlosti a uložení záznamu (record) do databáze
                    time = new Time(min, sec, millisec);
                    int timeInSec = (millisec / 1000) + sec + (min * 60);
                    double avgSpeed = (distance1 * 1000) / timeInSec;
                    record = new Record(distance1, time, avgSpeed);
                    DatabaseReference newRecordRef = recordsRef.push();
                    newRecordRef.setValue(record);
                    // We can also chain the two calls together
                    //recordsRef.push().setValueAsync(record);
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SensorActivity2.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
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
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getDistanceFromStart(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SensorActivity2.this);
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
        distance1 = currentLocation.distanceTo(startLocation);
        float[] results = new float[1];
        Location.distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
                currentLocation.getLatitude(), currentLocation.getLongitude(), results);
        distance2 = results[0];
        tvCurrentLat.setText(String.valueOf(currentLocation.getLatitude()));
        tvCurrentLon.setText(String.valueOf(currentLocation.getLongitude()));
        distance1 = distance1/1000;
        distance2 = distance2/1000;
        //zaokrouhlení na 3 des. místa
        distance1 = Precision.round(distance1, 3);
        distance2 = Precision.round(distance2, 3);
        tvDistance1.setText(String.valueOf(distance1) + " km");
        tvDistance2.setText(String.valueOf(distance2) + " km");
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_FINE_LOCATION:
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