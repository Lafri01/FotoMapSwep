package com.example.fotomap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CreateImageActivity extends AppCompatActivity {

    private Button ChooseImageButton;
    private Button ShareButton;
    private ImageView imageView;
    private EditText Description;
    private EditText LocationStyle;
    private EditText Season;
    private EditText TimeOfTheDay;

    private static Uri imageUri;

    private static final int IMAGE_REQUEST = 2;

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

    static double latitude;
    static double longitude;
    static String fileName = String.valueOf(System.currentTimeMillis());
    String downloadURL;

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
        intent.setType("image/jpeg");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            assert data != null;
            CreateImageActivity.imageUri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                Log.d("Image Uri lautet", String.valueOf(imageUri)); //Funktioniert
                getGPS(imageUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {

        setFields();

        if ((CreateImageActivity.imageUri != null) && (CreateImageActivity.descriptioncontent != null) &&
                (CreateImageActivity.locationStylecontent != null) && (CreateImageActivity.seasoncontent != null) &&
                (CreateImageActivity.timeOfTheDaycontent != null)) {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference().child("uploads")
                    .child(fileName + "." + getFileExtension(imageUri));

            CreateImageActivity.fileRefString = String.valueOf(storageRef);
            CreateImageActivity.fileRef = storageRef;
            downloadURL = "https://firebasestorage.googleapis.com/v0/b/fotomap-4df10.appspot.com/o/uploads%2F";
            downloadURL +=fileName;
            downloadURL +=".jpg?alt=media";
           // Log.d("REFERENCE IST: ", String.valueOf(storageRef)); //Funktioniert
            Log.d("Referenzvariable ist", String.valueOf(CreateImageActivity.fileRefString));

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

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("uploads").child(fileName);
                            GeoFire geoFire = new GeoFire(ref);
                            geoFire.setLocation("",new GeoLocation(latitude,longitude));
                            ref.child("FileURL").setValue(downloadURL);
                            ref.child ("Description").setValue(descriptioncontent);
                            ref.child ("LocationStyle").setValue(locationStylecontent);
                            ref.child ("Season").setValue(seasoncontent);
                            ref.child ("TimeOfTheDay").setValue(timeOfTheDaycontent);
                            startActivity(new Intent(getApplicationContext(),MapActivity.class));
                        }
                    });
                }
            });
        } else {
            Toast.makeText(CreateImageActivity.this, "Please fill in all Parameters", Toast.LENGTH_LONG).show();
        }
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

    void getGPS(Uri photoUri){
        if(photoUri != null){

            ParcelFileDescriptor parcelFileDescriptor = null;

            try {
                parcelFileDescriptor = getContentResolver().openFileDescriptor(photoUri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                ExifInterface exifInterface = new ExifInterface(fileDescriptor);

                String attrLATITUDE= exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String attrLATITUDEREF= exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                String attrLONGITUDE= exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                String attrLONGITUDEREF= exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                parcelFileDescriptor.close();

                if((attrLATITUDE !=null)
                        && (attrLATITUDEREF !=null)
                        && (attrLONGITUDE != null)
                        && (attrLONGITUDEREF !=null))
                {

                    if(attrLATITUDEREF.equals("N")){
                        latitude = convertToDegree(attrLATITUDE);
                    }
                    else{
                        latitude = 0 - convertToDegree(attrLATITUDE);
                    }

                    if(attrLONGITUDEREF.equals("E")){
                        longitude = convertToDegree(attrLONGITUDE);
                    }
                    else{
                        longitude = 0 - convertToDegree(attrLONGITUDE);
                    }
                    Log.d("LAT SUCKT BALLZ UND LAUTT WIE FOLGT", String.valueOf(latitude));
                    Log.d("LONG SUCKT BALLZ UND LAUTT WIE FOLGT", String.valueOf(longitude));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            }

            String strPhotoPath = photoUri.getPath();

        }else{
            Toast.makeText(getApplicationContext(),
                    "photoUri == null",
                    Toast.LENGTH_LONG).show();
        }
    };

    private double convertToDegree(String stringDMS) {
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        double D0 = new Double(stringD[0]);
        double D1 = new Double(stringD[1]);
        double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        double M0 = new Double(stringM[0]);
        double M1 = new Double(stringM[1]);
        double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        double S0 = new Double(stringS[0]);
        double S1 = new Double(stringS[1]);
        double FloatS = S0 / S1;

        double result = new Double(FloatD + (FloatM / 60) + (FloatS / 3600));

        return result;
    }

    void setFields(){
        CreateImageActivity.descriptioncontent = Description.getText().toString().trim();
        CreateImageActivity.locationStylecontent = LocationStyle.getText().toString().trim();
        CreateImageActivity.seasoncontent = Season.getText().toString().trim();
        CreateImageActivity.timeOfTheDaycontent = TimeOfTheDay.getText().toString().trim();
    }

 }
