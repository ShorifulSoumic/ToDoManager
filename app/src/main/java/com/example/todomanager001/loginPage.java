package com.example.todomanager001;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class loginPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    int progressbarvalue;
    ProgressBar progressBar;
    TextView email, password, registerText;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_login_page);


        email=findViewById(R.id.emailid);
        password=findViewById(R.id.passwordid);
        loginButton=findViewById(R.id.loginbuttonid);
        registerText=findViewById(R.id.registertextid);

        mAuth=FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String mail, pass;
                    mail=email.getText().toString();
                    pass=password.getText().toString();

                    loginButton.requestFocusFromTouch();

                    login(mail, pass);
                }
                catch (Exception e){
                }
            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(loginPage.this, registerPage.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void login(String mail, String pass){
        progressBar=findViewById(R.id.progressBarid);
        progressBar.setVisibility(View.VISIBLE);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                spinning();
            }
        });
        thread.start();

        if(mail.length()==0){
            progressBar.setVisibility(View.INVISIBLE);
            email.setError("Enter a valid email address!");
            email.requestFocus();
            return;
        }
        else if(pass.length()==0){
            progressBar.setVisibility(View.INVISIBLE);
            password.setError("Enter password");
            password.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(loginPage.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent=new Intent(loginPage.this, MainActivity.class);
                    intent.putExtra("key", email.getText().toString());
                    startActivity(intent);
                    finish();
                }
                else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(loginPage.this, "Login Failed!\n\n"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void spinning(){
        for(progressbarvalue=1;progressbarvalue<=10;progressbarvalue++) {
            try {
                Thread.sleep(1000);
                progressBar.setProgress(progressbarvalue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user=mAuth.getCurrentUser();

        if(user!=null){
            Intent intent=new Intent(loginPage.this, MainActivity.class);
            intent.putExtra("key", user.getEmail().toString());
            startActivity(intent);
            finish();
        }
    }
}