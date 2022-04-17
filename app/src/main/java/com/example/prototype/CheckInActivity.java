package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CheckInActivity extends AppCompatActivity {

    private static final String TAG ="Check In" ;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DocumentReference documentReferenced;
    private String userId;
    private String username;
    private TextView tv_name, tv_date, tv_slot, tv_floor, tv_seat;
    private Intent intent;
    private Bundle bundle;
    private String name, date, slot, floor, seat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        // Declare firebaseAuth and firebaseFireStore
        fAuth= FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        // Retrieve data from Firebase and change the textview in navigation view
        documentReferenced=fStore.collection("users").document(userId);
        documentReferenced.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot= task.getResult();
                    if(documentSnapshot.exists())
                    {
                        Log.d(TAG,"DocumentSnapshot data: " + documentSnapshot.getData());
                        username = (String) documentSnapshot.get("Name");
                    }
                    else
                    {
                        Log.d(TAG, "No such document");
                    }
                }
                else
                {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        //get intent getExtra
        intent = getIntent();
        bundle = intent.getExtras();
        date = (String) bundle.get("Date");
        slot = (String) bundle.get("Slot");
        floor = (String) bundle.get("Floor");
        seat = (String) bundle.get("Seat");


        //declare textview
        tv_name = findViewById(R.id.tv_username);
        tv_date = findViewById(R.id.tv_date);
        tv_slot = findViewById(R.id.tv_slot);
        tv_floor = findViewById(R.id.tv_floor);
        tv_seat = findViewById(R.id.tv_seat);

        //set text
        tv_name.setText(username);
        tv_date.setText(date);
        tv_slot.setText(slot);
        tv_floor.setText(floor);
        tv_seat.setText(seat);


        // end of OnCreate
    }
}