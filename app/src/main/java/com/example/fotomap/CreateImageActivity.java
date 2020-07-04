package com.example.fotomap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;

import io.grpc.Context;
import io.opencensus.metrics.LongGauge;

public class CreateImageActivity extends AppCompatActivity {

    private Button ChooseImageButton;
    private Button ShareButton;
    private ImageView imageView;
    private EditText Keywords;
    private EditText Description;
    private EditText LocationStyle;
    private EditText Season;
    private EditText TimeOfTheDay;

    private static Uri imageUri;

    private static final  int IMAGE_REQUEST = 2;

    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    //Bitmap to get image from gallery
    private Bitmap bitmap;

    private static String fileRefString;
    private static StorageReference fileRef;

    private static String descriptioncontent;
    private static String locationStylecontent;
    private static String seasoncontent;
    private static String timeOfTheDaycontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createimage_layout);

        //Requesting storage permission
        requestStoragePermission();

        //Initializing views
        ChooseImageButton = (Button) findViewById(R.id.ChooseImageButton);
        ShareButton = (Button) findViewById(R.id.ShareButton);
        imageView = (ImageView) findViewById(R.id.imageView);
        Description = (EditText) findViewById(R.id.Description);
        LocationStyle = (EditText) findViewById(R.id.LocationStyle);
        Season = (EditText) findViewById(R.id.Season);
        TimeOfTheDay = (EditText) findViewById(R.id.TimeOfTheDay);

        ChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        ShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            assert data != null;
            CreateImageActivity.imageUri = data.getData();

            //TODO: Hier GPS des Bildes auslesen und speichern

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                Log.d("Image Uri lautet", String.valueOf(imageUri)); //Funktioniert
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension (Uri uri){
        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {

        CreateImageActivity.descriptioncontent = Description.getText().toString().trim();
        CreateImageActivity.locationStylecontent = LocationStyle.getText().toString().trim();
        CreateImageActivity.seasoncontent = Season.getText().toString().trim();
        CreateImageActivity.timeOfTheDaycontent = TimeOfTheDay.getText().toString().trim();

        if (CreateImageActivity.imageUri != null && CreateImageActivity.descriptioncontent != null &&
                CreateImageActivity.locationStylecontent != null && CreateImageActivity.seasoncontent != null &&
                CreateImageActivity.timeOfTheDaycontent != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference().child("uploads")
                    .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            CreateImageActivity.fileRefString = String.valueOf(storageRef);
            CreateImageActivity.fileRef = storageRef;
           // Log.d("REFERENCE IST: ", String.valueOf(storageRef)); //Funktioniert
            //Log.d("Referenzvariable ist", String.valueOf(CreateImageActivity.fileRefString));

            storageRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();

                            //Log.d("DownloadUrl", url);
                            Toast.makeText(CreateImageActivity.this, "Image upload successfull", Toast.LENGTH_SHORT).show();
                            //Funktioniert!

                            uploadMetadata();

                        }

                    });
                }

            });
        }else{
            Toast.makeText(CreateImageActivity.this,"Please fill in all Parameters", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadMetadata(){

        //FirebaseStorage storage = FirebaseStorage.getInstance();
            //Log.d("FileRef in uploadMetadata", String.valueOf(CreateImageActivity.fileRef));
            //StorageReference storageRef = CreateImageActivity.fileRef;
         //   Log.d("storageRef2 in uploadMetadata", String.valueOf(CreateImageActivity.fileRef));

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("Description", descriptioncontent)
                    .setCustomMetadata("Locationstyle", locationStylecontent)
                    .setCustomMetadata("Season", seasoncontent)
                    .setCustomMetadata("Time of the day", timeOfTheDaycontent)
                    .build();

            CreateImageActivity.fileRef.updateMetadata(metadata)
                    .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            Log.d("METADATA IS WRITTEN", descriptioncontent + locationStylecontent);
                        }
                    });

    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }
 }