package com.example.fotomap;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
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
    private static final LatLng Giessen = new LatLng(50.583298, 8.676937);

    FindPicture find = new FindPicture();

  TextView verifyMsg;
    FirebaseAuth fAuth;
    Button resendCode;

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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Log.d("TITLEARRAY1 IST: ", String.valueOf(findPicture.searchResultsID));
        // Onclicklistener für die Marker auf der karte

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for(int i=0;i<MarkerarrayList.size();i++) {
                    if (marker.getTitle().equals("Marker in Position" + i)){
                        ImageViewActivity.kind = String.valueOf(find.searchResultsID.get(i));

                        String markertitle = marker.getTitle();
                Intent in = new Intent(MapActivity.this, ImageViewActivity.class);
                in.putExtra("title", markertitle);
                startActivity(in);
                    }
                }return false;
            }
        });
//

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
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        find.findpics();
        getLastKnownLocation();
        //Log.d("TITLEARRAY2 IST: ", String.valueOf(findPicture.searchResultsID));

        for(int i=0;i<MarkerarrayList.size();i++) {
            mMap.addMarker(new MarkerOptions().position(MarkerarrayList.get(i)).title("Marker in Position"+i).icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_fotomap_marker)));

        }
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    private void getLastKnownLocation() {
        //Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null){
                        find.loclat = location.getLatitude();
                        find.loclon = location.getLongitude();
                        //Log.d ("userlat: ", String.valueOf(findPicture.loclat));
                        //Log.d("Userlon: ", String.valueOf(findPicture.loclon));
                    }

                }
            }
        });
    }
    
    public void moveToGiessen(View view){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Giessen, 13));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId){
        Drawable vectorDrawable=ContextCompat.getDrawable(context,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap=Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas= new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}