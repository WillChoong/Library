package com.example.prototype;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private Timer timer;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fAuth = FirebaseAuth.getInstance();
                fUser = fAuth.getCurrentUser();
                if(fAuth.getCurrentUser() != null){
                    if(fUser.isEmailVerified()){
                        startActivity(new Intent(MainActivity.this,HomePage.class));
                    }else{
                        startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    }
                }
                else{
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                }
                overridePendingTransition(0,R.anim.fade_out);
                finish();
            }
        },5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*overridePendingTransition(0,0);
        overridePendingTransition(0,R.anim.fade_out);*/
    }
}