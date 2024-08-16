package com.example.distancetrackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;
    TextView currentAddress, lat, lon, distanceTraveled, addressVisited1, addressVisited2, addressVisited3, timeSpent1, timeSpent2, timeSpent3;
    double latitude, longitude, totalDistance;
    Location locationOld = new Location("old");
    Location locationNew = new Location("new");
    Geocoder geo;
    List<Address> address;
    long lastLocationChangeTime = SystemClock.elapsedRealtime();
    int first = 0;
    String allAdd = "";
    String allTime = "";
    long lastElapsedTime = 0;


    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("traveled", totalDistance);
        outState.putString("allTime", allTime);
        outState.putString("allAdd", allAdd);
        outState.putLong("lastElapsedTime", lastElapsedTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentAddress = findViewById(R.id.address);
        lat = findViewById(R.id.latitude);
        lon = findViewById(R.id.longitude);
        distanceTraveled = findViewById(R.id.distanceTraveled);
        addressVisited1 = findViewById(R.id.addressVisited1);
        timeSpent1 = findViewById(R.id.timeSpent1);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET}, 100);
        else
            locationUpdates();

//change orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (savedInstanceState != null) {
                totalDistance = savedInstanceState.getDouble("traveled");
                allAdd = savedInstanceState.getString("allAdd");
                allTime = savedInstanceState.getString("allTime");
                lastElapsedTime = savedInstanceState.getLong("lastElapsedTime");
            }
        }

        else { //portrait mode
            if (savedInstanceState != null) {
                totalDistance = savedInstanceState.getDouble("traveled");
                allTime = savedInstanceState.getString("allTime");
                allAdd = savedInstanceState.getString("allAdd");
                lastElapsedTime = savedInstanceState.getLong("lastElapsedTime");
            }
        }

    }//closes onCreate

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationUpdates();
            }
            else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void locationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        else {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        if (first == 0) {
            locationNew = location;
            first = 1;
        }
        locationOld = locationNew;
        locationNew = location;

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d("longitude", String.valueOf(longitude));
        Log.d("latitude", String.valueOf(latitude));
        lat.setText("Latitude: " + Double.toString(latitude));
        lon.setText("Longitude: " + Double.toString(longitude));

        try {
            geo = new Geocoder(MainActivity.this, Locale.getDefault());

            address = (List<Address>) geo.getFromLocation(latitude, longitude, 1);
            Log.d("LatCords", String.valueOf(latitude));
            Log.d("LongCords", String.valueOf(longitude));

            Log.d("address", "what " + address);
            String addNew = address.get(0).getAddressLine(0);
            Log.d("edit", addNew);

            if (allAdd.isEmpty()) {
                allAdd = addNew;
            }
            else if (!allAdd.endsWith(addNew)) {
                allAdd += "\n" + addNew;
            }

            addressVisited1.setText(allAdd);

            // Calculate time spent at previous location
            long timeSpentAtPreviousLocation = (SystemClock.elapsedRealtime() - lastLocationChangeTime) / 1000;

            if (allTime.isEmpty()) {
                allTime = "CURRENTLY HERE";
            }
            else if (!addNew.equals(allAdd.endsWith(addNew))) {
                if (!allTime.isEmpty()){
                    if (allTime.equals("CURRENTLY HERE"))
                        allTime = "";
                    else if (allTime.substring(allTime.length() - 14).equals("CURRENTLY HERE"))
                        allTime = allTime.substring(0, allTime.length() - 15);

                    allTime += "\n" + timeSpentAtPreviousLocation + "\nCURRENTLY HERE";
                }
            }

            timeSpent1.setText(allTime);
            lastLocationChangeTime = SystemClock.elapsedRealtime();

            //distance
            totalDistance += locationOld.distanceTo(locationNew);
            distanceTraveled.setText("Distance Traveled: " + Double.toString(totalDistance) + " meters");

            currentAddress.setText("Current Address: " + addNew);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

} //closes mainActivity