package com.tech104.isreal.memorialplacesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> places = new ArrayList<>();
    static ArrayList<LatLng> locations = new ArrayList<>();
    static ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        ListView placesListView = (ListView) findViewById(R.id.placesListView);
        SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        places.clear();
        locations.clear();
        try {
            ArrayList<String> latitude = (ArrayList<String>) ObjectSerializer.deserialize( sharedPreferences.getString("latitude", ObjectSerializer.serialize(new ArrayList<>())) );
            ArrayList<String> longitude = (ArrayList<String>) ObjectSerializer.deserialize( sharedPreferences.getString("longitude", ObjectSerializer.serialize(new ArrayList<>())) );
            for (int i=0; i<longitude.size(); i++) {
                locations.add( new LatLng(Double.parseDouble(latitude.get(i)), Double.parseDouble(longitude.get(i))) );
            }
            places = (ArrayList<String>) ObjectSerializer.deserialize( sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<>())) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(places.size() <= 0 && locations.size() <= 0){
            places.add("Add New Place");
            locations.add(new LatLng(0,0));
        } else {
            if(places.size() != locations.size()) {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }
        Log.i("Places", places.toString());
        //
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);
        placesListView.setAdapter(arrayAdapter);
        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("placePosition", position);
                startActivity(intent);
            }
        });
    }

}
