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

public class RegisterationActivity extends AppCompatActivity {

    private EditText txtemail,txtpassword,txtconfirm;
    private Button btnRegister,btnLoginPage;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        txtemail=(EditText)findViewById(R.id.txtemail);
        txtconfirm=(EditText)findViewById(R.id.txtconfirm);
        txtpassword=(EditText)findViewById(R.id.txtpassword);
        btnLoginPage=(Button) findViewById(R.id.btnLoginPage);
        btnRegister=(Button)findViewById(R.id.btnRegister);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);

        mAuth=FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewUser();
            }
        });
        btnLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterationActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerNewUser() {
        String email=txtemail.getText().toString();
        String password=txtpassword.getText().toString();
        String confirmPass=txtconfirm.getText().toString();

        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)||TextUtils.isEmpty(confirmPass))
        {
            Toast.makeText(RegisterationActivity.this,"Fields are empty",Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirmPass)){
            Toast.makeText(RegisterationActivity.this, "Password is not matched", Toast.LENGTH_SHORT).show();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Intent setupIntent=new Intent(RegisterationActivity.this,activity_setup.class);
                                startActivity(setupIntent);
                                finish();
                            }
                            else
                            {
                                String exception=task.getException().getMessage();
                                Log.d("error", "onComplete: "+exception);
                                Toast.makeText(RegisterationActivity.this,"Creation of Account is failed",Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        }
    }
}
