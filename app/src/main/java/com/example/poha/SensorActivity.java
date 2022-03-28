package com.example.poha;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class SensorActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private TextView tvLat, tvLon, tvAltitude, tvAccuracy, tvSpeed, tvSensor, tvUpdates, tvAddress;
    private SwitchCompat swLocationupdates, swGps;
    //Google's API for location services
    private FusedLocationProviderClient fusedLocationProviderClient;
    //if we are tracking location or not
    private boolean updateOn = false;
    //Location request is a config file for all settings related to FusedLocationProviderClient
    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        tvLat = findViewById(R.id.tv_lat);
        tvLon = findViewById(R.id.tv_lon);
        tvAltitude = findViewById(R.id.tv_altitude);
        tvAccuracy = findViewById(R.id.tv_accuracy);
        tvSpeed = findViewById(R.id.tv_speed);
        tvSensor = findViewById(R.id.tv_sensor);
        tvUpdates = findViewById(R.id.tv_updates);
        tvAddress = findViewById(R.id.tv_address);

        swLocationupdates = findViewById(R.id.sw_locationsupdates);
        swGps = findViewById(R.id.sw_gps);

        locationRequest = new LocationRequest();
        //set all properties of Location Request
        locationRequest.setInterval(30000);
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

        swGps.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if(swGps.isChecked()){
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tvSensor.setText("Senzor GPS.");
                }else{
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tvSensor.setText("Vysílače a Wi-Fi.");
                }
            }
        });
        swLocationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(swLocationupdates.isChecked()){
                    //turn on location tracking
                    startLocationUpdates();
                }else{
                    //turn off location tracking
                    stopLocationUpdates();
                }
            }
        });

        updateGps();
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startLocationUpdates() {
        tvUpdates.setText("Poloha je sledována.");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGps();
    }

    private void stopLocationUpdates() {
        tvUpdates.setText("Poloha není sledována.");
        tvLat.setText("Poloha není sledována.");
        tvLon.setText("Poloha není sledována.");
        tvSpeed.setText("Poloha není sledována.");
        tvAddress.setText("Poloha není sledována.");
        tvAccuracy.setText("Poloha není sledována.");
        tvAltitude.setText("Poloha není sledována.");
        tvSensor.setText("Poloha není sledována.");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGps();
                }else{
                    Toast.makeText(this, "Aby aplikace mohla správně fungovat, potřebuje povolení ke zjišťování Vaší polohy.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateGps(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SensorActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIValues(location);
                }
            });
        }else{
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUIValues(Location location) {
        tvLat.setText(String.valueOf(location.getLatitude()));
        tvLon.setText(String.valueOf(location.getLongitude()));
        tvAccuracy.setText(String.valueOf(location.getAccuracy()));
        if(location.hasAltitude()){
            tvAltitude.setText(String.valueOf(location.getAltitude()));
        }else{
            tvAltitude.setText("Není dostupná");
        }
        if(location.hasSpeed()){
            tvSpeed.setText(String.valueOf(location.getSpeed()));
        }else{
            tvSpeed.setText("Není dostupná");
        }
        Geocoder geocoder = new Geocoder(SensorActivity.this);
        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tvAddress.setText(addresses.get(0).getAddressLine(0));
        }catch (Exception e){
            tvAddress.setText("Adresu nelze získat.");
        }
    }
}