package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar pb;
    private TextView resetPassword;
    private TextInputLayout textInputLayout_mail, textInputLayout_pws;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private static final String TAG = "LoginActivity";

    //press back can end the application
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK)
        {
            finishAffinity();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputLayout_mail = findViewById(R.id.layout_mail);
        textInputLayout_pws = findViewById(R.id.layout_pwd);
        resetPassword = findViewById(R.id.tv_fp);

        //Create new account onClickListener
        findViewById(R.id.tv_newAcc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        //Reset password onClickListener
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText reset_email=new EditText(v.getContext());
                final AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Reset Password");
                builder.setMessage("Please enter your registered email to received reset link.");
                builder.setView(reset_email);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String resetEmail=reset_email.getText().toString().trim();
                        fAuth.sendPasswordResetEmail(resetEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this,"Reset link had been sent to your email",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this,"Error ! Reset link is not sent",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
            }
        });

    }

    //Alert Dialog for the user not yet verify mail
    private void openDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
        builder.setCancelable(false);
        builder.setTitle("Alert");
        builder.setMessage("Please go to the email to verity your account.");
        builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseUser fUser=fAuth.getCurrentUser();
                fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LoginActivity.this,"Verification email has been sent",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG","OnFailure "+ e.getMessage());
                    }
                });
            }
        });
        builder.create().show();
    }

    //valid mail format
    private boolean validateEmail() {
        TextInputEditText email = (TextInputEditText) findViewById(R.id.mail);
        if (email.getText().toString().trim().isEmpty()) {
            textInputLayout_mail.setError("Field cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
            textInputLayout_mail.setError("Please enter a valid email");
            return false;
        } else {
            textInputLayout_mail.setErrorEnabled(false);
            return true;
        }
    }

    //valid password format
    private boolean validatePassword() {
        TextInputEditText password = findViewById(R.id.pwd);
        if (password.getText().toString().trim().isEmpty()) {
            textInputLayout_pws.setError("Field cannot be empty");
            return false;
        } else {

            textInputLayout_pws.setErrorEnabled(false);
            return true;
        }
    }

    public void validInput(View view)
    {
        if(!validateEmail() | !validatePassword())
        {
            return;
        }
        else{
            //Progress Bar visible
            pb  = findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);

            //Declare FirebaseAuth
            fAuth = FirebaseAuth.getInstance();

            fAuth.signInWithEmailAndPassword(textInputLayout_mail.getEditText().getText().toString().trim(),textInputLayout_pws.getEditText().getText().toString().trim()).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                FirebaseUser fUser=fAuth.getCurrentUser();
                                if(!fUser.isEmailVerified())
                                {
                                    pb.setVisibility(View.GONE);
                                    openDialog();
                                }
                                else
                                {
                                    pb.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this,"Logged in Successfully",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),HomePage.class));
                                }

                            }
                            else
                            {
                                Toast.makeText(LoginActivity.this,"Error ! "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"Error = "+task.getException().getMessage());
                                pb.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }
}