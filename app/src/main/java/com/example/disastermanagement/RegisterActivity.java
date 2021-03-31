package com.example.disastermanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

//Registration Activity

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout fullNameLayout;
    private TextInputLayout confirm_passwordLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout phone_number_layout;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance(); //getting firebase for authentication

        fullNameLayout = findViewById(R.id.full_name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        phone_number_layout = findViewById(R.id.phone_number_layout);
        registerButton = findViewById(R.id.registerButton);
        confirm_passwordLayout = findViewById(R.id.confirm_password_layout);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateFullName() && validate_emailLayout() && validate_phone()
                        && validatepasswordLayout() && confirm_password_layout()){

                    progressDialog = new ProgressDialog(RegisterActivity.this, R.style.MyAlertDialogStyle);
                    progressDialog.setTitle("Creating account...");
                    progressDialog.setMessage("Please wait while we create your account.");
                    sendUserInfoToDatabase();
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

    private void sendUserInfoToDatabase() {

        HashMap<String, Object> map = new HashMap<>();//creating hashmap datastructure
        map.put("Name", getFullName());//pushing attribiutes into hashmap()
        map.put("Email", getEmail());
        map.put("Phone", getPhone());

        FirebaseDatabase.getInstance().getReference("Users_information").push()
                .setValue(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void SendUserToLoginActivity(){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private boolean validateFullName(){
        String firstName = fullNameLayout.getEditText().getText().toString().trim();
        if(firstName.isEmpty()){
            fullNameLayout.setError("Field can't be empty");
            return false;
        }else{
            fullNameLayout.setError(null);
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

    private boolean validate_phone(){
        String phone = phone_number_layout.getEditText().getText().toString().trim();

        if(phone.isEmpty()){
            phone_number_layout.setError("Field can't be empty");
            return false;
        }else if(phone.length() > 10){
            phone_number_layout.setError("Enter a valid phone number");
            return false;
        }else{
            emailLayout.setError(null);
            return true;
        }
    }

    private String getEmail(){
        String Useremail = emailLayout.getEditText().getText().toString();
        return Useremail;
    }

    private String getFullName(){
        String full_name = fullNameLayout.getEditText().getText().toString().trim();
        return full_name;
    }
    private String getPhone(){
        String phone_number = phone_number_layout.getEditText().getText().toString().trim();
        return phone_number;
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