package com.example.fotomap;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.example.fotomap.Constants;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;

import static com.example.fotomap.Constants.PERMISSION_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.fotomap.SettingActivity.mapstyle;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private static final String TAG = "1337";

   static ArrayList<LatLng> MarkerarrayList = new ArrayList<LatLng>();
   static ArrayList<LatLng> MarkerOptions = new ArrayList<LatLng>();
   static ArrayList<String > title = new ArrayList<>();


    private GoogleMap mMap;

    boolean locationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationClient;

    findPicture find = new findPicture();

  TextView verifyMsg;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    Button resendCode;

    String userId;


    // ArrayList für die Marker


    // Anlegen der Markerobjekte enthält double Latitude longitude



    LatLng sydney = new LatLng (50.57922810 , 8.66658747);

    //arraylist für die namen der marker


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

         resendCode = findViewById(R.id.resendCode);
        verifyMsg = findViewById(R.id.verifyMsg);
        fAuth = FirebaseAuth.getInstance();
        final FirebaseUser user= fAuth.getCurrentUser();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(!user.isEmailVerified()){
            verifyMsg.setVisibility(View.VISIBLE);
            resendCode.setVisibility(View.VISIBLE);

            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag", "onFailure: Email not sent " + e.getMessage());
                        }
                    });
                }
            });
        }







    // for( int i = 0 ; i < .length ; i++){
//
//            for(int j = 0; j < .length; j++) {
//                MarkerarrayList.add(new MarkerOptions().position(new LatLng(fileLat, fileLng)).title(fileTitle);
//
//
//            }
//    }




    BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
    Menu menu = bottomNav.getMenu();
    MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
}
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override

                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
 /*    Intent intent1 = new Intent(MapActivity.this, MapActivity.class);
                            startActivity(intent1);*/
                            break;
                        case R.id.nav_newPost:
                            Intent intent2 = new Intent(MapActivity.this, CreateImageActivity.class);
                            startActivity(intent2);
                            break;
                        case R.id.nav_Settings:
                           Intent intent3 = new Intent(MapActivity.this, SettingActivity.class);
                            startActivity(intent3);
                            break;
                    }
                    return false;
                }

            };

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
        find.findpics();

        Log.d("TITLEARRAY1 IST: ", String.valueOf(findPicture.searchResultsID));
        // Onclicklistener für die Marker auf der karte
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override


            public boolean onMarkerClick(Marker marker) {
                String markertitle = marker.getTitle();
                Intent i = new Intent(MapActivity.this, ImageViewActivity.class);
                i.putExtra("title", markertitle);
                startActivity(i);

                return false;
            }
        });

        if (mapstyle%2==0) {
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                boolean success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.mapstyle2));

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style. Error: ", e);
            }
//        requestLocationPermission();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestLocationPermission();
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);


    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                locationPermissionGranted = true;
                Toast.makeText(this, "Permission granted now you can check your location", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        getLastKnownLocation();
        Log.d("TITLEARRAY2 IST: ", String.valueOf(findPicture.searchResultsID));
        for ( int i = 0 ; i < MarkerarrayList.size(); i++){                                                                 // Schleife zum hinzufügen der marker

            for ( int j = 0 ; j < findPicture.searchResultsID.size(); j++){                                                                           // Schleife zum setzen der Namen

                mMap.addMarker(new MarkerOptions().position(MarkerarrayList.get(i)).title(String.valueOf(findPicture.searchResultsID.get(i))));                                              //position nimmt ein LatLng Objekt an, title String , get() ist von Arraylist dem wird ein index (int) übergeben
            }

                                                                                              // bewegt kamera zu diesem objekt mit moveCamera , newLatLng bewegt den screen so dass der punkt im center ist

        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(MarkerarrayList.get(0)));


        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null){
                        findPicture.loclat = location.getLatitude();
                        findPicture.loclon = location.getLongitude();
                        Log.d ("userlat: ", String.valueOf(findPicture.loclat));
                        Log.d("Userlon: ", String.valueOf(findPicture.loclon));
                    }

                }
            }
        });
      //  findPicture.findpics();

    }
    
    public void moveToGiessen(View view){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Giessen, 13));

    }
}
