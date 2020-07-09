package com.example.fotomap;

import android.util.Log;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

class FindPicture {

    static double loclat;
    static double loclon;
    static DatabaseReference ref = FirebaseDatabase.getInstance().getReference("uploads");
    final static GeoFire geoFire = new GeoFire(ref);
    final static GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(50.5841, 8.6784), 15);

    static ArrayList<String> searchResultsID = new ArrayList<String>();
    static ArrayList<String> searchResultsLocation = new ArrayList<String>();
    static ArrayList<Double> searchResultsLat = new ArrayList<Double>();
    static ArrayList<Double> searchResultsLon = new ArrayList<Double>();

    public static void findpics() {

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                searchResultsID.add(key); //funktioniert
                String location2 = String.valueOf(location);
                searchResultsLocation.add(location2);

                String [] parts = location2.split(", ");
                String lat = parts[0].substring(12, 19);
                searchResultsLat.add (Double.parseDouble(lat));
                String lon = parts [1].substring(0,7);
                searchResultsLon.add (Double.parseDouble(lon));
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {

               // Log.d("ArrayList", String.valueOf(searchResultsID)); //funktioniert
                //Log.d ("LocationList", String.valueOf(searchResultsLocation));

                if (searchResultsID != null){
                    //Log.d("LatList: ", String.valueOf(searchResultsLat));//funktioniert
                    //Log.d ("LongList", String.valueOf(searchResultsLon));//funktioniert

                    if (FindPicture.searchResultsID != null) {
                        for (int i = 0; i < (FindPicture.searchResultsID.size()); i++) {
                            double lat = FindPicture.searchResultsLat.get(i);
                            double lon = FindPicture.searchResultsLon.get(i);
                            //String id = findPicture.searchResultsID.get(i);

                            LatLng name = new LatLng(lat, lon);
                            String n = String.valueOf(i + 1);
                           MapActivity.MarkerarrayList.add(name);
                            MapActivity.title.add(n);
                            //MarkerarrayList.add(new MarkerOptions().position(new LatLng(fileLat, fileLng)).title(fileTitle));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }
    public static void reset(){
         searchResultsID = null;
         searchResultsLocation = null;
         searchResultsLat = null;
          searchResultsLon = null;
    }
}

