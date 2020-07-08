package com.example.fotomap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

private ImageView ImageView;
private TextView Description;
private TextView LocationStyle;
private TextView Season;
private TextView TimeOfTheDay;
private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
private DatabaseReference reference=firebaseDatabase.getReference();
    private DatabaseReference childreference=reference.child("uploads").child("1594144775211");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        ImageView=findViewById(R.id.ImageView);
        Description=findViewById(R.id.Description);
        LocationStyle=findViewById(R.id.LocationStyle);
        Season=findViewById(R.id.Season);
        TimeOfTheDay=findViewById(R.id.TimeOfTheDay);

    }

    @Override
    protected void onStart() {
        super.onStart();
        childreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               String url=dataSnapshot.child("FileURL").getValue(String.class);
                    Log.d("Message lautet", url );
                                        Picasso.get()
                                        .load(url)
                                        .into(ImageView);
                String description =dataSnapshot.child("Description").getValue(String.class);
                Description.setText(description);
                String locationstyle=dataSnapshot.child("LocationStyle").getValue(String.class);
                LocationStyle.setText(locationstyle);
                String season=dataSnapshot.child("Season").getValue(String.class);
                Season.setText(season);
                String timeoftheday=dataSnapshot.child("TimeOfTheDay").getValue(String.class);
                TimeOfTheDay.setText(timeoftheday);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}