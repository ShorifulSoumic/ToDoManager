package com.example.todomanager001;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.w3c.dom.Text;

public class registerPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    int progressbarvalue;
    ProgressBar progressBar;
    TextView email, password, confirmPassword, loginText;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_register_page);

        password=findViewById(R.id.passwordid);
        registerButton=findViewById(R.id.registerbuttonid);
        loginText=findViewById(R.id.logintextid);
        confirmPassword=findViewById(R.id.confirmpasswordid);
        email=findViewById(R.id.emailid);

        mAuth=FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String pass, mail, confirmpass;
                    mail=email.getText().toString();
                    pass=password.getText().toString();
                    confirmpass=confirmPassword.getText().toString();

                    registerButton.requestFocusFromTouch();

                    try{
                        register(mail, pass, confirmpass);
                    }catch (Exception e){

                    }
            }
        });

        //if already have a account
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(registerPage.this, loginPage.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void register(String mail, String pass, String confirmpass){

        progressBar=findViewById(R.id.progressBarid);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                spinning();
            }
        });
        thread.start();

        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            email.setError("Enter a valid email address!");
            email.requestFocus();
            return;
        }
        else if(pass.length()<6){
            password.setError("Password must be at least 6 characters long!");
            password.requestFocus();
            return;
        }
        else if(!pass.equals(confirmpass)){
            confirmPassword.setError("Password fields do not match!");
            confirmPassword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(registerPage.this, "User Created Successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent=new Intent(registerPage.this, MainActivity.class);
                    intent.putExtra("key", mail);
                    startActivity(intent);
                    finish();
                }
                else{
                    progressBar.setVisibility(View.INVISIBLE);
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(registerPage.this, "User is already registered, Login!", Toast.LENGTH_SHORT).show();
                        loginText.requestFocus();
                    }
                    else{
                        Toast.makeText(registerPage.this, "Registration Failed! "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
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
}