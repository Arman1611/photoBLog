package com.example.armangupta.photoblog;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class activity_setup extends AppCompatActivity {
    private Toolbar setupToobar;
    private EditText txtUsername;
    private CircleImageView circleImageView;
    private Button btnAccSet;
    private Uri resultUri=null;
    private StorageReference mStore;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar mprogress;
    private String user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setupToobar=(Toolbar) findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToobar);
        getSupportActionBar().setTitle("Account Setting");
        mprogress=(ProgressBar)findViewById(R.id.progSetup);

        mStore= FirebaseStorage.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        txtUsername=(EditText)findViewById(R.id.txtUsername);
        circleImageView=(CircleImageView)findViewById(R.id.circleImageView);
        btnAccSet=(Button)findViewById(R.id.btnAccSet);
        //For loading the image at the starting of the activity
        mprogress.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String photouri = task.getResult().getString("PhotoURI");
                        resultUri=Uri.parse(photouri);
                        txtUsername.setText(name);
                        Glide.with(activity_setup.this).applyDefaultRequestOptions(RequestOptions.placeholderOf(R.drawable.default_image)).load(photouri)
                                .into(circleImageView);
                        mprogress.setVisibility(View.INVISIBLE);
                    } else {
                        //Toast.makeText(activity_setup.this, "Your data doesn't exist", Toast.LENGTH_SHORT).show();
                        mprogress.setVisibility(View.INVISIBLE);
                    }

                }
                else {
                    Toast.makeText(activity_setup.this, "Firebase Retrieve error", Toast.LENGTH_SHORT).show();
                    mprogress.setVisibility(View.INVISIBLE);
                }
            }
        });

        //For Selecting the image on circle view
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(activity_setup.this, Manifest.permission.READ_EXTERNAL_STORAGE)+ContextCompat.checkSelfPermission(
                            activity_setup.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)+ContextCompat.checkSelfPermission(activity_setup.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(activity_setup.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},1);
                    }
                    else
                    {
                        startCropActivity();
                    }
                }
                else
                {
                    startCropActivity();
                }
            }
        });

        btnAccSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mprogress.setVisibility(View.VISIBLE);

                final String name = txtUsername.getText().toString();

                if (resultUri == null || TextUtils.isEmpty(name)) {
                    Toast.makeText(activity_setup.this, "Either Photo is missing or username field is empty", Toast.LENGTH_SHORT).show();
                    mprogress.setVisibility(View.INVISIBLE);
                }

                else {
                    StorageReference filepath = mStore.child("UsersPhotos").child(user_id + ".jpeg");
                    File actualImageFile=new File(resultUri.getPath());
                    Bitmap bitmap=null;
                    try{
                         bitmap = new Compressor(activity_setup.this).
                                setMaxHeight(100).
                                setMaxWidth(100).
                                setQuality(2).
                                compressToBitmap(actualImageFile);
                    }
                    catch (Exception e){}
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] thumbdata = baos.toByteArray();
                    filepath.putBytes(thumbdata)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            updateSettings(downloadUri,name);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(resultUri!=null&& !TextUtils.isEmpty(name)){
                                updateSettings(resultUri,name);
                            }
                            else {
                                String error = e.getMessage();
                                mprogress.setVisibility(View.INVISIBLE);
                                Toast.makeText(activity_setup.this, "Image Error occured::" + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateSettings(Uri downloadUri, String name) {
        Map<String,String> map=new HashMap<>();
        map.put("name",name);
        map.put("PhotoURI",downloadUri.toString());
        firebaseFirestore.collection("Users").document(user_id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(activity_setup.this, "The User Settings are Updated", Toast.LENGTH_SHORT).show();
                    sendToPostActivity();
                }
                else {
                    String fireError=task.getException().getMessage();
                    Toast.makeText(activity_setup.this, "Firebase Error occured::" + fireError, Toast.LENGTH_SHORT).show();
                }
                mprogress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void sendToPostActivity() {
        Intent intent=new Intent(activity_setup.this,PostActivity.class);
        startActivity(intent);
        finish();
    }

    private void startCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(activity_setup.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                circleImageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED&&grantResults[2]==PackageManager.PERMISSION_GRANTED&&requestCode==1){
            startCropActivity();
        }
        else
        {
            Toast.makeText(activity_setup.this,"Permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
