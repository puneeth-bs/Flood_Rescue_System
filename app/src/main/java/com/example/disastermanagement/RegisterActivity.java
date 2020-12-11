package com.example.disastermanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout confirm_passwordLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        firstNameLayout = findViewById(R.id.first_name_layout);
        lastNameLayout = findViewById(R.id.last_name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        registerButton = findViewById(R.id.registerButton);
        confirm_passwordLayout = findViewById(R.id.confirm_password_layout);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateFirstName() && validate_emailLayout()
                        && validateLastName() && validatepasswordLayout() && confirm_password_layout()){

                    progressDialog = new ProgressDialog(RegisterActivity.this, R.style.MyAlertDialogStyle);
                    progressDialog.setTitle("Creating account");
                    progressDialog.setMessage("Please wait while we create your account.");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    mAuth.createUserWithEmailAndPassword(getEmail(), getPassword())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        progressDialog.setMessage("Redirecting to Login");
                                        progressDialog.dismiss();
                                        SendUserToLoginActivity();
                                        Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                    }else{
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Some error occured", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void SendUserToLoginActivity(){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private boolean validateFirstName(){
        String firstName = firstNameLayout.getEditText().getText().toString().trim();
        if(firstName.isEmpty()){
            firstNameLayout.setError("Field can't be empty");
            return false;
        }else{
            firstNameLayout.setError(null);
            return true;
        }
    }

    private boolean validateLastName(){
        String LastName = lastNameLayout.getEditText().getText().toString().trim();
        if(LastName.isEmpty()){
            lastNameLayout.setError("Field can't be empty");
            return false;
        }else{
            lastNameLayout.setError(null);
            return true;
        }
    }


    private boolean validatepasswordLayout(){
        String password = passwordLayout.getEditText().getText().toString().trim();
        if(password.isEmpty()) {
            passwordLayout.setError("Field can't be empty");
            return false;
        }

        if(password.length() < 8){
            passwordLayout.setError("Password must be greater than 8 characters");
            return false;
        }
        passwordLayout.setError(null);
        return true;
    }

    private boolean validate_emailLayout(){
        String email = emailLayout.getEditText().getText().toString().trim();
        if(email.isEmpty()){
            emailLayout.setError("Field can't be empty");
            return false;
        }else{
            emailLayout.setError(null);
            return true;
        }
    }

    private String getEmail(){
        String Useremail = emailLayout.getEditText().getText().toString().trim();
        return Useremail;
    }

    private String getPassword(){
        String Userpassword = passwordLayout.getEditText().getText().toString().trim();
        return Userpassword;
    }

    private boolean confirm_password_layout(){
        String password1 = passwordLayout.getEditText().getText().toString().trim();
        String confirm_password = confirm_passwordLayout.getEditText().getText().toString().trim();

        if(password1.equals(confirm_password)){
            return true;
        }else{
            confirm_passwordLayout.setError("Password not matched");
            return false;
        }
    }




}