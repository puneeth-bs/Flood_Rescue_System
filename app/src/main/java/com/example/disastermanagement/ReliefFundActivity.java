package com.example.disastermanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

//Relief fund activity

public class ReliefFundActivity extends AppCompatActivity {

    EditText amount, note, name, upi_id;
    Button pay;

    final int UPI_PAYMENT = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relief_fund);
        intializeMethod();

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amountTxt = amount.getText().toString();
                String notesTxt = note.getText().toString();
                String nameTxt = name.getText().toString();
                String upiTxt = upi_id.getText().toString();
                payUsingUpi(amountTxt, notesTxt, nameTxt, upiTxt);
            }
        });
    }

    private void openDialog(String amountTxt, String notesTxt, String nameTxt) {

        if(amountTxt.equals("") && notesTxt.equals("") && nameTxt.equals(""))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReliefFundActivity.this);
            builder.setMessage("Fill all the fields").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();



        }
    }


    private void payUsingUpi(String amountTxt, String notesTxt, String nameTxt, String upiTxt)
    {
        Uri uri = Uri.parse("upi://pay").buildUpon().appendQueryParameter("pa",upiTxt)
                .appendQueryParameter("pn", nameTxt)
                .appendQueryParameter("tn", notesTxt)
                .appendQueryParameter("am", amountTxt)
                .appendQueryParameter("cu", "INR").build();

        openDialog(amountTxt, notesTxt, nameTxt);
        Intent upi_payment = new Intent(Intent.ACTION_VIEW);
        upi_payment.setData(uri);
        Intent chooser = Intent.createChooser(upi_payment, "Pay with");// this will choose the apps suitable for payment
        if(null!= chooser.resolveActivity(getPackageManager())){
            startActivityForResult(chooser, UPI_PAYMENT);
        }

        else{
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void intializeMethod() {
        pay = (Button) findViewById(R.id.buttonpay);
        name = (EditText) findViewById(R.id.name);
        amount = (EditText) findViewById(R.id.amount);
        note = (EditText) findViewById(R.id.notes);
        upi_id = (EditText) findViewById(R.id.upi_id);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPI_PAYMENT:
                if (RESULT_OK == resultCode || (resultCode == 11)) {
                    if (data != null) {
                        String txt = data.getStringExtra("response");
                        Log.d("UPI", "onActivityResult:" + txt);
                        ArrayList<String> dataLst = new ArrayList<>();
                        dataLst.add("Nothing");
                        upiPaymentDataOperation(dataLst);
                    } else {
                        String txt = data.getStringExtra("response");
                        Log.d("UPI", "onActivityResult:" + "Return data is null");
                        ArrayList<String> dataLst = new ArrayList<>();
                        dataLst.add("Nothing");
                        upiPaymentDataOperation(dataLst);
                    }
                }
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> dataLst) {
        if(isConnectionAvailable(ReliefFundActivity.this)){
            String str = dataLst.get(0);
            Log.d("UPIPAY", "upipaymentoperation:"+str);
            String paymentCancel = "";
            if(str == null) str = "Discard";
            String status = "";
            String approvalref = "";
            String response[] = str.split("g");
            for(int i = 0; i<response.length; i++)
            {
                String equalStr[] = response[i].split("g");
                if(equalStr.length>= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("approval ref".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalref = equalStr[1];
                    }
                }

                else{
                    paymentCancel = "Payment cancelled by the user";
                    if(status.equals("success"))
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                        builder.setTitle("Status").setMessage("Payment Sucessfull")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                    else if("Payment cancelled by the user".equals(paymentCancel)){
                        Toast.makeText(this, "Payment cancel by the user", Toast.LENGTH_SHORT).show();
                    }

                    else{
                        Toast.makeText(this, "Trasaction Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }

    public boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected() && networkInfo.isConnectedOrConnecting())
            {
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }
}