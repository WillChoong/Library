package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ipsec.ike.SaProposal;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String TAG = "RegisterActivity";
    private TextInputLayout textInput_username;
    private TextInputLayout textInput_password;
    private TextInputLayout textInput_email;
    private TextInputLayout textInput_StudentId;
    private Spinner spinnerFaculty;
    private ProgressBar PB;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

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

        textInput_username=findViewById(R.id.layout_username);
        textInput_email=findViewById(R.id.layout_mail);
        textInput_password=findViewById(R.id.layout_pwd);
        textInput_StudentId=findViewById(R.id.layout_studentId);
        PB=findViewById(R.id.progress_register);
        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        //end of onCreate
    }

    private void openDialog()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(RegisterActivity.this);

        builder.setTitle("Success");
        builder.setMessage("User Created ! Please go to the email to verity your account.");
        builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
        builder.create().show();
    }
    private boolean validateUsername()
    {
        TextInputEditText username=findViewById(R.id.username);
        if(username.getText().toString().trim().isEmpty())
        {
            textInput_username.setError("Field cannot be empty");
            return false;
        }
        else
        {
            textInput_username.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail()
    {
        TextInputEditText email=findViewById(R.id.mail);
        if(email.getText().toString().trim().isEmpty())
        {
            textInput_email.setError("Field cannot be empty");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches())
        {
            textInput_email.setError("Please enter a valid email");
            return false;
        }
        else
        {
            textInput_email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword()
    {
        TextInputEditText password=findViewById(R.id.pwd);
        if(password.getText().toString().trim().isEmpty())
        {
            textInput_password.setError("Field cannot be empty");
            return false;
        }

        else
        {
            textInput_password.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateStudentId()
    {
        TextInputEditText student_id=findViewById(R.id.studentID);
        if(student_id.getText().toString().trim().isEmpty())
        {
            textInput_StudentId.setError("Field cannot be empty");
            return false;
        }
        else
        {
            textInput_StudentId.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateFaculty()
    {
        Spinner faculty=findViewById(R.id.spinner_faculty);
        if(faculty.getSelectedItemPosition()>0)
        {

            return true;
        }
        else
        {
            TextView errorTextview = (TextView) faculty.getSelectedView();
            errorTextview.setError("Your Error Message here");
            return false;
        }
    }

    public void inputView(View view)
    {
        if(!validateUsername()| !validateEmail()|!validatePassword()|!validateStudentId()|!validateFaculty())
        {
            return;
        }
        else
        {
            PB.setVisibility(View.VISIBLE);
            fAuth.createUserWithEmailAndPassword(textInput_email.getEditText().getText().toString().trim(),textInput_password.getEditText().getText().toString().trim()).
                    addOnCompleteListener((task ->{
                        if(task.isSuccessful())
                        {
                            Spinner spinnerFaculty=findViewById(R.id.spinner_faculty);
                            String faculty=spinnerFaculty.getSelectedItem().toString();
                            Toast.makeText(RegisterActivity.this,"User created",Toast.LENGTH_SHORT).show();
                            userID=fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference=fStore.collection("users").document(userID);
                            Map<String,Object> user=new HashMap<>();
                            user.put("Name",textInput_username.getEditText().getText().toString().trim());
                            user.put("Email",textInput_email.getEditText().getText().toString().trim());
                            user.put("Password",textInput_password.getEditText().getText().toString().trim());
                            user.put("Student_ID",textInput_StudentId.getEditText().getText().toString().trim());
                            user.put("Faculty",faculty);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"OnSuccess : user is created for"+ userID);
                                    FirebaseUser fUser=fAuth.getCurrentUser();
                                    fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            openDialog();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG,"OnFailure"+e.getMessage());
                                        }
                                    });



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull  Exception e) {
                                    Log.d(TAG,"OnFailure : "+ e.toString());
                                }
                            });



                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this,"User created failed ! "+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            PB.setVisibility(View.GONE);
                        }

                    }));
        }
    }
}