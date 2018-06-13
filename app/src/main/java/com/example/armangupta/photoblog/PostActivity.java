package com.example.armangupta.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostActivity extends AppCompatActivity {

    Toolbar main_toolbar;
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    FloatingActionButton floatAddPost;
    BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            main_toolbar = (Toolbar) findViewById(R.id.main_tool);
            setSupportActionBar(main_toolbar);
            getSupportActionBar().setTitle("Photo Blog");

            firebaseFirestore = FirebaseFirestore.getInstance();
            floatAddPost = (FloatingActionButton) findViewById(R.id.floatAddPost);
            bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

            replacefragment(homeFragment);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_action_home:
                            replacefragment(homeFragment);
                            return true;
                        case R.id.menu_action_nav:
                            replacefragment(notificationFragment);
                            return true;
                        case R.id.menu_action_acc:
                            replacefragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            floatAddPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PostActivity.this, AddPostActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
    protected  void replacefragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null) {
            sendToLogin();
        }
        else
        {
            String user_id=currentUser.getUid();
            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists())
                        {
                            Toast.makeText(PostActivity.this,"Please Select Your Profile Photo and Name",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(PostActivity.this,activity_setup.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    else
                    {
                        Toast.makeText(PostActivity.this,"FireBase Check Error:"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendToLogin() {
        Intent intent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                logout();
                return true;
            case R.id.accountSet:
                Intent intent=new Intent(PostActivity.this,activity_setup.class);
                startActivity(intent);
                return true;
            default:

                return  true;
        }
    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }
}
