package com.example.todomanager001;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;

public class mainPage extends AppCompatActivity {

    private int progressbarvalue;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main_page);

        progressBar=findViewById(R.id.progressBarmainid);
        //progressBar.setVisibility(View.VISIBLE);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                spinning();
                startApp();
            }
        });
        thread.start();
    }

    public void spinning(){
        for(progressbarvalue=1;progressbarvalue<=3;progressbarvalue++) {
            try {
                Thread.sleep(1000);
                progressBar.setProgress(progressbarvalue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void startApp(){
        Intent intent=new Intent(mainPage.this, loginPage.class);
        startActivity(intent);
        finish();
    }
}