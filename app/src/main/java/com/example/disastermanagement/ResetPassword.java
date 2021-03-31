package com.example.disastermanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
//Reset Password Activity

public class ResetPassword extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        ProgressDialog progressDialog = new ProgressDialog(ResetPassword.this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Sending a password reset link.");
        emailInputLayout = findViewById(R.id.email_layout1);
        button = findViewById(R.id.submitButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                FirebaseAuth.getInstance().sendPasswordResetEmail(getEmail())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                showAlertDialog();

                            }
                        });
            }
        });


    }

    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), R.style.AlertDialogStyle);
        builder.setMessage("Password reset link has been sent to your email.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        builder.show();
    }


    private boolean validate_emailLayout(){
        String email = emailInputLayout.getEditText().getText().toString().trim();
        if(email.isEmpty()){
            emailInputLayout.setError("Field can't be empty");
            return false;
        }else{
            emailInputLayout.setError(null);
            return true;
        }
    }

    private String getEmail(){
        String Useremail = emailInputLayout.getEditText().getText().toString();
        return Useremail;
    }
}