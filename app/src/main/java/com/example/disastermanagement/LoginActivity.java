package com.example.disastermanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailText;
    private EditText passwordText;
    private ImageButton registerButton;
    private Button loginButton;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerButton = findViewById(R.id.registerButton);
        emailText = findViewById(R.id.loginEmail);
        passwordText = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateEmail() && validatePassword()){

                    progressDialog = new ProgressDialog(LoginActivity.this, R.style.MyAlertDialogStyle);
                    progressDialog.setTitle("Logging in...");
                    progressDialog.setMessage("Please wait.");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();


                    mAuth.signInWithEmailAndPassword(getEmailText(), getPasswordText())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        progressDialog.dismiss();
                                        sendUserToHomeActivity();
                                    }else{
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                        builder.setMessage("Incorrect username or password")
                                                .setPositiveButton("Ok", null);
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    }
                                }
                            });

                }
            }
        });
    }

    private String getEmailText(){
        String email = emailText.getText().toString();
        return email;
    }
    private String getPasswordText(){
        String password = passwordText.getText().toString();
        return password;
    }
    private boolean validatePassword(){
        String password = passwordText.getText().toString();
        if(password.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Incorrect username or password")
                    .setPositiveButton("Ok", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return false;
        }else{
            return true;
        }
    }

    private boolean validateEmail(){
        String email = emailText.getText().toString();
        if(email.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Incorrect username or password")
                    .setPositiveButton("Ok", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return false;
        }else{
            return true;
        }
    }

    private void sendUserToHomeActivity(){
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}