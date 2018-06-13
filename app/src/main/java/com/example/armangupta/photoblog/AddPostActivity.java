package com.example.armangupta.photoblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddPostActivity extends AppCompatActivity {
    private ImageView imgPost;
    private EditText edtDesc;
    private Button btnAddPost;
    Toolbar addtoolbar;
    private Uri resultUri=null,downlaodUri,getThumbUri=null;
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    ProgressDialog mprogress;
    StorageReference storageReference;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        addtoolbar=(Toolbar)findViewById(R.id.addtoolbar);
        setSupportActionBar(addtoolbar);
        getSupportActionBar().setTitle("Upload Photo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        imgPost=(ImageView)findViewById(R.id.imgPost);
        edtDesc=(EditText)findViewById(R.id.edtDesc);
        btnAddPost=(Button)findViewById(R.id.btnAddPost);
        mprogress=new ProgressDialog(this);

        imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCropActivity();
            }
        });

        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc=edtDesc.getText().toString();
                mprogress.setMessage("Posting");
                mprogress.setIndeterminate(true);
                mprogress.show();
                if(TextUtils.isEmpty(desc)||resultUri==null)
                {
                    Toast.makeText(AddPostActivity.this,"Either photo or desc is missing",Toast.LENGTH_SHORT).show();
                    mprogress.dismiss();
                }
                else {
                    final String uid=mAuth.getCurrentUser().getUid();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
                    final String currentTimeStamp = dateFormat.format(new Date());
                    StorageReference childRef=storageReference.child("Posted_Photos").child(currentTimeStamp+".jpg");
                    childRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(!task.isSuccessful())
                            {
                                Toast.makeText(AddPostActivity.this, "Firebase Storage Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                downlaodUri=task.getResult().getDownloadUrl();
                                File actualImageFile=new File(resultUri.getPath());
                                try {
                                    bitmap = new Compressor(AddPostActivity.this).
                                            setMaxHeight(100).
                                            setMaxWidth(100).
                                            setQuality(2).
                                            compressToBitmap(actualImageFile);
                                }
                                catch (Exception e){}
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbdata = baos.toByteArray();
                                final StorageReference thumbStore=storageReference.child("Posted_Photos/thumbs").child(currentTimeStamp+".jpg");
                                thumbStore.putBytes(thumbdata).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            getThumbUri=task.getResult().getDownloadUrl();

                                            Map<String,Object> map=new HashMap<>();
                                            map.put("userId",uid);
                                            map.put("imagethumb",getThumbUri.toString());
                                            map.put("description",desc);
                                            map.put("imageUrl",downlaodUri.toString());
                                            map.put("timestamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("postedPhotos").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(AddPostActivity.this, "Your image has been posted", Toast.LENGTH_SHORT).show();
                                                        mprogress.dismiss();
                                                        sendToPostActivity();
                                                    }
                                                    else{
                                                        Toast.makeText(AddPostActivity.this, "Firebase Error:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        mprogress.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                        else
                                        {
                                            Toast.makeText(AddPostActivity.this, "FireBase Thumb store error:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }

            }
        });
    }

    private void sendToPostActivity() {
        finish();
    }

    private void startCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(AddPostActivity.this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                imgPost.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
