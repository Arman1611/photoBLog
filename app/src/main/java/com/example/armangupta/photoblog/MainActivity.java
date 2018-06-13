package com.example.armangupta.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText txtemail,txtpassword;
    private Button btnLogin,btnRegister;
    private final static String TAG="PHOTO_APP";
    private ProgressBar progressBar;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        txtemail=(EditText)findViewById(R.id.txtemail);
        txtpassword=(EditText)findViewById(R.id.txtpassword);
        btnLogin=(Button)findViewById(R.id.btnRegister);
        btnRegister=(Button)findViewById(R.id.btnregister);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signInMethod();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,RegisterationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signInMethod() {
        String email=txtemail.getText().toString();
        String password=txtpassword.getText().toString();
        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
            Toast.makeText(MainActivity.this,"Blank Fields",Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email,password)
                  .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {
                          if(task.isSuccessful()) {
                              Log.d(TAG, "onComplete: User Login is Successful");
                              sendToNextActivity();
                          }
                          else {
                              Log.d(TAG, "onComplete: User Login is failed"+task.getException().getMessage());
                              Toast.makeText(MainActivity.this,"User Login Failed",Toast.LENGTH_SHORT).show();
                              progressBar.setVisibility(View.INVISIBLE);
                          }
                      }
                  });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            sendToNextActivity();
        }
    }
    private  void sendToNextActivity()
    {
        String user_id=mAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if(task.isSuccessful())
                {
                    if(!task.getResult().exists())
                    {
                        Toast.makeText(MainActivity.this,"Please Select Your Profile Photo and Name",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this,activity_setup.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Intent intent=new Intent(MainActivity.this,PostActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this,"FireBase Check Error:"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
