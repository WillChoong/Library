package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewReservation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RecyclerViewAdapter.ClickListener {

    private static final String TAG = "ViewReservation";
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private View content, header;
    private Toolbar toolbar = null;
    private TextView username, student_id;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DocumentReference documentReferenced, documentReferenced1;
    private String userId;
    private ImageView qr;
    private ArrayList<reservation> mList;
    private ProgressDialog progressDialog;
    private FirestoreRecyclerAdapter adapter;
    List<reservation> reservations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reservation);

        //Make the content become visible
        content = findViewById(R.id.view_reservation);
        content.setVisibility(View.VISIBLE);

        //The activity does not allow scan QR Code ; Make the QR code gone
        qr = findViewById(R.id.image_qrcode);
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
        header = navigationView.getHeaderView(0);
        username = header.findViewById(R.id.username);
        student_id = header.findViewById(R.id.student_id);

        // Declare firebaseAuth and firebaseFireStore
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        // Retrieve data from Firebase and change the textview in navigation view
        documentReferenced = fStore.collection("users").document(userId);
        documentReferenced.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                        username.setText("Name : " + documentSnapshot.get("Name"));
                        student_id.setText("Student ID : " + documentSnapshot.get("Student_ID"));

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        Task<QuerySnapshot> documentReference2 = fStore.collection("reservation").whereEqualTo("UserID", userId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "Information " + i);
                        Log.d(TAG, "Document : " + document.getId());
                        Log.d(TAG, "CheckIn : " + document.get("CheckIn"));
                        Log.d(TAG, "CheckOut : " + document.get("CheckOut"));
                        Log.d(TAG, "Date : " + document.get("Date"));
                        Log.d(TAG, "Floor : " + document.get("Floor"));
                        Log.d(TAG, "SeatID : " + document.get("SeatID"));
                        Log.d(TAG, "Time : " + document.get("Time"));
                        Log.d(TAG, "UserID : " + document.get("UserID"));
                        i++;

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching data");
        //progressDialog.show();

        recyclerView = findViewById(R.id.recycleView);


        Query query = FirebaseFirestore.getInstance().collection("reservation").whereEqualTo("UserID", userId).orderBy("Date", Query.Direction.DESCENDING).orderBy("Time", Query.Direction.ASCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                } else {
                    reservations = value.toObjects(reservation.class);
                    Log.d(TAG, "Object :" + value.toObjects(reservation.class));
                    //Log.d(TAG, "Reservations :" + reservations.get(3).getCheckOut());
                }
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(reservations, ViewReservation.this, ViewReservation.this);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(ViewReservation.this));
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }
        });


        FirestoreRecyclerOptions<reservation> options = new FirestoreRecyclerOptions.Builder<reservation>()
                .setQuery(query, reservation.class)
                .build();
        Log.d(TAG, "Model :" + options.getSnapshots().size());
        adapter = new FirestoreRecyclerAdapter<reservation, ReservationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReservationViewHolder holder, int position, @NonNull reservation model) {
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());
                holder.floor.setText(model.getFloor());
                holder.seat.setText(model.getSeatID());
                Log.d(TAG, "Model :" + model);
            }

            @NonNull
            @Override
            public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reservation_item, parent, false);
                return new ReservationViewHolder(v);
            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        //EventChangeListener();

        //setupView();

        //end of onCreate
    }

    /*private void setupView()
    {
        CollectionReference reservation = fStore.collection("reservation");
        Query query=reservation.whereEqualTo("UserID",userId).orderBy("Date", Query.Direction.DESCENDING).orderBy("Time", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Reservation> options=new FirestoreRecyclerOptions.Builder<Reservation>()
                .setQuery(query,Reservation.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Reservation, ReservationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReservationViewHolder holder, int position, @NonNull Reservation model) {
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());
                holder.floor.setText(model.getFloor());
                holder.seat.setText(model.getSeatID());
            }

            @NonNull
            @Override
            public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reservation_item,parent,false);
                return new ReservationViewHolder(v);
            }
        };

        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }*/

    private class ReservationViewHolder extends RecyclerView.ViewHolder {
        private TextView date, time, floor, seat;
        private CardView cv;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.layout_cardView);
            date = itemView.findViewById(R.id.tv_date);
            time = itemView.findViewById(R.id.tv_time);
            floor = itemView.findViewById(R.id.tv_floor);
            seat = itemView.findViewById(R.id.tv_seat);
        }
    }

    @Override
    public void onListItemClick(int position, ColorStateList colorStateList) {
        //Toast.makeText(this, "Seat : " + reservations.get(position).getSeatID(), Toast.LENGTH_SHORT).show();
        if (reservations.get(position).getColor() == Color.GRAY) {
            //Toast.makeText(this, "GRAY : ", Toast.LENGTH_SHORT).show();
        } else if (reservations.get(position).getColor() == Color.WHITE) {
            //Toast.makeText(this, "WHITE : ", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewReservation.this);
            builder.setCancelable(false);
            builder.setTitle("Cancel reservation");
            builder.setMessage("Are you sure want to cancel this reservation?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getDocumentID(reservations.get(position).getSeatID()
                            , reservations.get(position).getDate()
                            , reservations.get(position).getTime()
                            , reservations.get(position).getFloor());
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    builder.create().dismiss();
                }
            });
            builder.create().show();
        }
    }

    private void getDocumentID(String seat, String date, String time, String floor) {
        Task<QuerySnapshot> documentReference2 = fStore.collection("reservation")
                .whereEqualTo("UserID", userId)
                .whereEqualTo("SeatID", seat)
                .whereEqualTo("Floor", floor)
                .whereEqualTo("Date", date)
                .whereEqualTo("Time", time).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                String id = task.getResult().getDocuments().get(0).getId();

                                fStore.collection("reservation").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    // avoid end application when the drawer is open
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //finishAffinity();
            startActivity(new Intent(ViewReservation.this,HomePage.class));
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
                Intent h = new Intent(ViewReservation.this, HomePage.class);
                startActivity(h);
                break;
            case R.id.nav_seat:
                Intent i = new Intent(ViewReservation.this, SeatReservationActivity.class);
                startActivity(i);
                break;
            case R.id.nav_book:
                Intent g = new Intent(ViewReservation.this, SearchActivity.class);
                startActivity(g);
                break;
            case R.id.nav_view:
                break;
            case R.id.logout:
                Intent k=new Intent(ViewReservation.this,LogoutActivity.class);
                startActivity(k);
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}