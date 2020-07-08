package com.example.fotomap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class Detailsactivity extends AppCompatActivity {

    TextView markertxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailsactivity);
        markertxt = findViewById(R.id.marker);
        // we will get data from our maps activity
        String title = getIntent().getStringExtra("title");
        markertxt.setText(title);
        //add internet permission in manifest
    }
}