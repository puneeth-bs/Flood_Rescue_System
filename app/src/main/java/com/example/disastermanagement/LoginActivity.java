package com.example.disastermanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

//Login Activity

public class LoginActivity extends AppCompatActivity {

    private EditText emailText;
    private EditText passwordText;
    private ImageButton registerButton;
    private TextView forgotPassword;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private SignInButton signInButtonGoogle;
    private GoogleSignInClient googleSignInClient;
    private int SignInCode = 100;
    ProgressDialog progressDialog1;



    ProgressDialog progressDialog;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        registerButton = findViewById(R.id.registerButton);
        emailText = findViewById(R.id.loginEmail);
        passwordText = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        signInButtonGoogle = findViewById(R.id.SignInUsingGoogle);
        checkCurrentUser();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        broadcastReceiver = new NetworkChangeReciever();


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        signInButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               progressDialog1 = new ProgressDialog(LoginActivity.this,R.style.MyAlertDialogStyle);
               progressDialog1.setTitle("Google Sign In");
               progressDialog1.setMessage("Getting your credentials, please wait...");
               progressDialog1.show();
               googleSignIn();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEmail() && validatePassword()) {

                    progressDialog = new ProgressDialog(LoginActivity.this, R.style.MyAlertDialogStyle);
                    progressDialog.setTitle("Logging in...");
                    progressDialog.setMessage("Please wait.");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();


                    mAuth.signInWithEmailAndPassword(getEmailText(), getPasswordText())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        sendUserToHomeActivity(getEmailText());
                                    } else {
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

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPasswordIntent = new Intent(LoginActivity.this, ResetPassword.class);
                startActivity(resetPasswordIntent);
            }
        });
    }

    private void checkCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            //sendToHomeAvtivityIntent();
        }
    }

    private void sendToHomeAvtivityIntent(){
        Intent homeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(homeActivityIntent);
    }

    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, SignInCode);
        progressDialog1.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SignInCode){
            progressDialog1.setTitle("Signing In");
            progressDialog1.setMessage("Please wait...");
            progressDialog1.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInRequest(task);
        }else{
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    private void handleSignInRequest(Task<GoogleSignInAccount> task) {

        try{
            GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
            Toast.makeText(this, "Signed In Successfully", Toast.LENGTH_SHORT).show();
            //sendUserToHomeActivity();
            FirebaseGoogleAuth(googleSignInAccount);
        }catch (ApiException e){
            progressDialog1.dismiss();
            Toast.makeText(this, "Signed In Failed", Toast.LENGTH_SHORT).show();
            //FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount googleSignInAccount) {

        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    getInformation(firebaseUser);
                }
            }
        });
    }

    private void getInformation(FirebaseUser firebaseUser) {

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(signInAccount != null){
            String personName = signInAccount.getDisplayName();
            String personGivenName = signInAccount.getGivenName();
            String personalEmail = signInAccount.getEmail();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("email", personalEmail);
            intent.putExtra("name", personName);
            startActivity(intent);
            progressDialog1.dismiss();
            Toast.makeText(this, "Signed in as "+personName, Toast.LENGTH_SHORT).show();
        }
    }


    private String getEmailText() {
        String email = emailText.getText().toString();
        return email;
    }

    private String getPasswordText() {
        String password = passwordText.getText().toString();
        return password;
    }

    private boolean validatePassword() {
        String password = passwordText.getText().toString();
        if (password.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Incorrect username or password")
                    .setPositiveButton("Ok", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return false;
        } else {
            return true;
        }
    }

    private boolean validateEmail() {
        String email = emailText.getText().toString();
        if (email.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Incorrect username or password")
                    .setPositiveButton("Ok", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return false;
        } else {
            return true;
        }
    }

    private void sendUserToHomeActivity(String userEmail) {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("email", userEmail);
        startActivity(intent);
    }


    protected void registerNetworkBroadCastReceiver() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisterNetwork() {
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle("Want to exit?")
                .setMessage("Do you want to exit app")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SplashActivity.splashActivity.finish();
                        finish();
                        System.exit(0);
                    }
                });
        builder.show();

    }


}