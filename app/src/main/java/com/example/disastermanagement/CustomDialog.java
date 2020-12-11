package com.example.disastermanagement;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class CustomDialog extends AppCompatDialogFragment {

    private EditText editTextFullname;
    private EditText editTextAge;
    private CustomDialogListener customDialogListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog, null);
        builder.setView(view)
                .setTitle("Enter")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fullname = editTextFullname.getText().toString();
                String age = editTextAge.getText().toString();
                customDialogListener.applyTexts(fullname, age);
            }
        });

        editTextFullname = view.findViewById(R.id.edit_fullname);
        editTextAge = view.findViewById(R.id.edit_age);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            customDialogListener = (CustomDialogListener) context;

        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    public interface CustomDialogListener{
        void applyTexts(String fullname, String age);
    }
}
