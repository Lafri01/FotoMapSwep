package com.example.fotomap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingActivity extends AppCompatActivity {
    FirebaseAuth fAuth;
    static int mapkoks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        fAuth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);


    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override

                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            Intent intent1 = new Intent(SettingActivity.this, MapActivity.class);
                            startActivity(intent1);
                            break;
                        case R.id.nav_newPost:
                            Intent intent2 = new Intent(SettingActivity.this, CreateImageActivity.class);
                            startActivity(intent2);
                            break;
                        case R.id.nav_Settings:
                        /*    Intent intent3 = new Intent(SettingActivity.this, SettingActivity.class);
                            startActivity(intent3);*/
                            break;
                    }
                    return false;
                }

            };



    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }

    public void switchSchema (View view){
        Toast.makeText(this, "Mapstyle switched!", Toast.LENGTH_SHORT).show();
        mapkoks++;
    }


}
