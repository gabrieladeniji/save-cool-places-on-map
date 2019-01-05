package com.tech104.isreal.memorialplacesapp;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    LocationManager locationManager;
    LocationListener locationListener;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        //
        Intent intent = getIntent();
        int placePosition = intent.getIntExtra("placePosition", 0);
        //
        setUpLocationService(placePosition);
    }


    public void addMarker(Location location, String title) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if( title != "Your location" ) {
            mMap.addMarker(new MarkerOptions().position(userLatLng).title(title));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 5));
    }


    public void setUpLocationService(int p) {
        if(p == 0) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
//                    addMarker(location, "Your location");
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            };

            if(Build.VERSION.SDK_INT < 23) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    //
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    addMarker(lastKnownLocation, "Your location");
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        } else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(p).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(p).longitude);
            //
            addMarker(placeLocation, MainActivity.places.get(p));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                addMarker(lastKnownLocation, "Your location");
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.i("Geocode", latLng.toString());
        String address = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if(listAddress != null && listAddress.size() > 0) {
                Log.i("Geocode", listAddress.toString());
                if(listAddress.get(0).getThoroughfare() != null) {
                    if(listAddress.get(0).getSubThoroughfare() != null) {
                        address += listAddress.get(0).getSubThoroughfare() + " ";
                    }
                    address += listAddress.get(0).getThoroughfare() + " ";
                }
            }
            address += (listAddress.get(0).getAdminArea() == null ? "" : listAddress.get(0).getAdminArea() + " ");
            address += (listAddress.get(0).getCountryName() == null ? "" : listAddress.get(0).getCountryName() + " ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(address == "") {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm-MM-dd-yyyy");
            address = sdf.format(new Date());
        }
        //
        checkForDuplicateLocation(address, latLng);
    }

    public void checkForDuplicateLocation(String address, LatLng latLng) {
        if( !MainActivity.places.contains(address) && !MainActivity.locations.contains(latLng) ) {
            MainActivity.places.add(address);
            MainActivity.locations.add(latLng);
            mMap.addMarker(new MarkerOptions().position(latLng).title(address));
            saveToSharedPreference();
            Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Place have been saved bebore!", Toast.LENGTH_SHORT).show();
        }
        // Refresh ArrayAdapter
        MainActivity.arrayAdapter.notifyDataSetChanged();
    }

    public void saveToSharedPreference() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        try {
            ArrayList<String> latitude = new ArrayList<>();
            ArrayList<String> longitude = new ArrayList<>();
            for(int i=0; i<MainActivity.locations.size(); i++) {
                latitude.add(Double.toString(MainActivity.locations.get(i).latitude));
                longitude.add(Double.toString(MainActivity.locations.get(i).longitude));
            }
            sharedPreferences.edit().putString("latitude", ObjectSerializer.serialize(latitude)).apply();
            sharedPreferences.edit().putString("longitude", ObjectSerializer.serialize(longitude)).apply();
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
