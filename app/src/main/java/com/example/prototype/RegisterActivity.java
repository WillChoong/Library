package com.example.prototype;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.net.ipsec.ike.SaProposal;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private String[]faculty=new String[]{
            "Select your faculty",
            "CEE",
            "CFS",
            "FAM",
            "FAS",
            "FBF",
            "FCI",
            "FEGT",
            "FICT",
            "FMHS",
            "FSc",
            "ICS",
            "LKC"
    };
    private Spinner spinnerFaculty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        spinnerFaculty = findViewById(R.id.spinner_faculty);
        List<String> faculty_list=new ArrayList<>(Arrays.asList(faculty));
        ArrayAdapter<String> faculty_adapter=new ArrayAdapter<String>(this,R.layout.spinner_item,faculty_list)
        {
            @Override
            public boolean isEnabled(int position) {
                //disable the first position as selected option
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view=super.getDropDownView(position,convertView,parent);
                TextView tv=(TextView) view;
                if(position==0)
                {
                    tv.setTextColor(Color.GRAY);
                }
                else
                {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        faculty_adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerFaculty.setAdapter(faculty_adapter);

    }
}