package com.example.fotomap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

class findPicture {
    static double loclat;
    static double loclon;
    static DatabaseReference ref = FirebaseDatabase.getInstance().getReference("uploads");
    final static GeoFire geoFire = new GeoFire(ref);
    final static GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(50.579229, 8.666584), 3);

    static ArrayList<String> searchResultsID = new ArrayList<String>();
    static ArrayList<String> searchResultsLocation = new ArrayList<String>();
    static ArrayList<Double> searchResultsLat = new ArrayList<Double>();
    static ArrayList<Double> searchResultsLon = new ArrayList<Double>();

    public static void findpics() {

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            // ArrayList<ArrayList<String>> searchResults = new ArrayList<ArrayList<String>>();

            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                searchResultsID.add(key); //funktioniert
                searchResultsLocation.add( String.valueOf(location));
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                //This method will be called when all the locations which are within 3km from the user's location has been loaded Now you can do what you wish with this data
                // getArray();
                Log.d("ArrayList", String.valueOf(searchResultsID)); //funktioniert
                Log.d ("LocationList", String.valueOf(searchResultsLocation));

                if (searchResultsID != null){
                    for (String s : searchResultsLocation){
                        String[] parts = s.split(", ");
                        String lat = parts[0].substring (12,28) ;
                        searchResultsLat.add(Double.parseDouble(lat));
                        String lon = parts[1].substring (0,10);
                        searchResultsLon.add(Double.parseDouble(lon));
                        Log.d("LatList: ", String.valueOf(searchResultsLat));//funktioniert
                        Log.d ("LongList", String.valueOf(searchResultsLon));//funktioniert
                    }

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }
}

