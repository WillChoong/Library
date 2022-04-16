package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class ViewReservation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG ="ViewReservation" ;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private View content,header;
    private Toolbar toolbar=null;
    private TextView username, student_id;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DocumentReference documentReferenced;
    private String userId;
    private ImageView qr;
    private List<Reservation> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reservation);

        //Make the content become visible
        content = findViewById(R.id.view_reservation);
        content.setVisibility(View.VISIBLE);

        //The activity does not allow scan QR Code ; Make the QR code gone
        qr=findViewById(R.id.image_qrcode);
        qr.setVisibility(View.GONE);

        //Change toolbar title
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("View Reservation");

        // add triple line in layout(三)
        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        //Declare navigation view and set listener
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header=navigationView.getHeaderView(0);
        username=header.findViewById(R.id.username);
        student_id=header.findViewById(R.id.student_id);

        // Declare firebaseAuth and firebaseFireStore
        fAuth=FirebaseAuth.getInstance();
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
                        username.setText("Name : "+ documentSnapshot.get("Name"));
                        student_id.setText("Student ID : "+documentSnapshot.get("Student_ID"));

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

        recyclerView = findViewById(R.id.recycleView);
        mList = new ArrayList<>();

        //Get all the reserved information related to the user
        Task<QuerySnapshot> documentReference = fStore.collection("reservation").whereEqualTo("UserID",userId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        mList.add(new Reservation((Boolean) document.get("Check In")
                                ,(Boolean) document.get("Check Out")
                                ,(String) document.get("UserID")
                                ,(String) document.get("Date")
                                ,(String) document.get("Time")
                                ,(String) document.get("Floor")
                                ,(String) document.get("SeatID")));
                    }
                }else{
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error getting documents: ", e);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(mList,this);
        recyclerView.setAdapter(recyclerViewAdapter);

        //end of onCreate
    }

    // avoid end application when the drawer is open
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finishAffinity();
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    // drawer selected action
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.nav_home:
                break;
            case R.id.nav_seat:
                Intent i = new Intent(ViewReservation.this, SeatReservationActivity.class);
                startActivity(i);
                break;
            case R.id.nav_book:
                /*Intent g = new Intent(MainActivity.this, BookAvailability.class);
                startActivity(g);*/
                break;
            case R.id.logout:
                /*Intent k=new Intent(MainActivity.this,LogoutActivity.class);
                startActivity(k);*/
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}