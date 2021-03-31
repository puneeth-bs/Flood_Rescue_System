package com.example.disastermanagement;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

//Home Fragment

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ProgressBar progressBar;
    TextView percentageTextView, statusTextView, name_textview, main_welcome_text;
    WebView webView;
    CardView helpline, SMS, relief_fund;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        init(view);

        helpline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneCallIntent = new Intent(Intent.ACTION_CALL);
                String number = "8105810297";
                phoneCallIntent.setData(Uri.parse("tel:" + number));


                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(phoneCallIntent);
                } else {
                    requestPhoneCallPermission();
                }
            }


        });

        SMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    sendSMS();
                } else {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 100);
                }

            }
        });

        relief_fund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReliefFundActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }

    public void getUserInformation() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();
        String name = user.getDisplayName();
        String userEmail = user.getEmail();
        //Toast.makeText(getContext(), "" +userEmail, Toast.LENGTH_LONG).show();
        if(name != null){
            String lastName = extraxtLastName(name);
            name_textview.setText(""+lastName);
            main_welcome_text.setText("Hello "+lastName+", your location has no signs of flood.");
        }else if(userEmail != null){
            getUsernameThroughEmail(userEmail);
        }else{
            name_textview.setText(null);
            main_welcome_text.setText("Hello "+null+", your location has no signs of flood.");
        }
    }

    private void getUsernameThroughEmail(String email){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users_information");
        String name = null;
        reference.orderByChild("Email").equalTo(""+email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userDataSnapshot : snapshot.getChildren()){
                    String userName = (String) userDataSnapshot.child("Name").getValue(String.class);
                    if(userName != null){
                        name_textview.setText(""+userName);
                        main_welcome_text.setText("Hello "+userName+", your location has no signs of flood.");
                    }
                    //Toast.makeText(getContext(), ""+userName, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String extraxtLastName(String name) {

        String input = name;
        if(name == null){
            return null;
        }
        ArrayList<String> stringArrayList = new ArrayList<>();
        String [] substrings = input.split(" ");
        for(String names : substrings){
            stringArrayList.add(names);
        }

        return stringArrayList.get(stringArrayList.size()-1);
    }

    private void sendSMS() {
        String number1 = "8105810297";
        String message = "Need help";

        try {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number1, null, message, null, null);
            Toast.makeText(getContext(), "SMS Sent", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error in sending SMS", Toast.LENGTH_SHORT).show();
        }

    }

    private void requestPhoneCallPermission() {

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 100);
    }

    private void init(View view) {

        //progressBar = view.findViewById(R.id.risk_indicator_progress_bar);
        //percentageTextView = view.findViewById(R.id.risk_percentage_textView);
        //progressBar.setProgress(20);
        helpline = view.findViewById(R.id.call_helpline_cardview);
        SMS = view.findViewById(R.id.send_sms_cardview);
        relief_fund = view.findViewById(R.id.relief_fund_cardview);
        name_textview = view.findViewById(R.id.name_textView);
        main_welcome_text = view.findViewById(R.id.main_welcome_text);
        getUserInformation();


    }


}