package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG ="HomePage" ;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar=null;
    private View content,header;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private TextView username, student_id,tv_checkInOut;
    private String userId;
    private CardView sr, bs, vs;
    private DocumentReference documentReferenced;
    private LinearLayout layout_checkInOut;
    private String date,time,t;
    private Button btn_checkInOut;
    private ImageView qr;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //Make the content become visible
        content = findViewById(R.id.home_page);
        content.setVisibility(View.VISIBLE);

        //Change toolbar title
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        //add triple line in layout(三)
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

        // layout for check in and out
        layout_checkInOut = findViewById(R.id.checkInOut);
        btn_checkInOut = findViewById(R.id.btn_checkInOut);
        tv_checkInOut = findViewById(R.id.tv_checkInOut);

        Calendar calendar=Calendar.getInstance();
        DateFormat df=DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
        df.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        date=df.format(calendar.getTime());

        ZonedDateTime dateTime= ZonedDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"));
        time=dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        t=null;
        CheckExistedReserved check=new CheckExistedReserved();
        try {
            //get slot
            t=check.ExistedReserved(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Task<QuerySnapshot> dReference=fStore.collection("reservation")
                .whereEqualTo("Date",date).whereEqualTo("Time",t).whereEqualTo("UserID",userId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().getDocuments().isEmpty())
                        {
                            layout_checkInOut.setVisibility(View.GONE);
                        }
                        else
                        {
                            if(task.getResult().getDocuments().size()==1)
                            {

                            }
                            Log.d(TAG,"Size :"+task.getResult().getDocuments().size());
                            for(QueryDocumentSnapshot dc : task.getResult())
                            {
                                String id=dc.get("UserID").toString();
                                Boolean checkIn= (Boolean) dc.get("CheckIn");
                                Boolean checkOut= (Boolean) dc.get("CheckOut");
                                if(dc.get("UserID").equals(userId))
                                {
                                    Log.d(TAG,"User id : "+id);
                                    Log.d(TAG,"Check in : "+checkIn);
                                    Log.d(TAG,"Checkout : "+checkOut);
                                    if(checkIn==false)
                                    {
                                        layout_checkInOut.setVisibility(View.VISIBLE);
                                        btn_checkInOut.setText("Check in Now");
                                        btn_checkInOut.setTextSize(TypedValue.COMPLEX_UNIT_SP,14.0f);
                                        countdownTimer(task.getResult().getDocuments().get(0).get("Time").toString());
                                        btn_checkInOut.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                startActivity(new Intent(HomePage.this,ScanActivity.class));
                                            }
                                        });
                                    }
                                    if(checkOut==true)
                                    {
                                        layout_checkInOut.setVisibility(View.GONE);
                                    }if(checkIn==true && checkOut==false)
                                    {
                                    layout_checkInOut.setVisibility(View.VISIBLE);
                                    btn_checkInOut.setText("Check out now");
                                    btn_checkInOut.setTextSize(TypedValue.COMPLEX_UNIT_SP,14.0f);
                                    countdownTimer(task.getResult().getDocuments().get(0).get("Time").toString());
                                    btn_checkInOut.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder builder=new AlertDialog.Builder(HomePage.this);
                                            builder.setCancelable(false);
                                            builder.setTitle("Check out");
                                            builder.setMessage("Are you sure want to check out and release the seat?");
                                            builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    task.getResult().getDocuments().get(0).getReference().update("CheckIn",true,"CheckOut",true);
                                                    layout_checkInOut.setVisibility(View.GONE);
                                                    builder.create().dismiss();
                                                }
                                            });
                                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    builder.create().dismiss();
                                                }
                                            });
                                            builder.create().show();
                                        }
                                    });
                                }
                                }
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "Error "+e, Toast.LENGTH_SHORT).show();
                    }
                });

        //The activity allow scan QR Code ; Make the QR code function
        qr=findViewById(R.id.image_qrcode);

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomePage.this,ScanActivity.class));
            }
        });


        // get card view
        sr = findViewById(R.id.cv_sr);
        bs = findViewById(R.id.cv_bs);
        vs = findViewById(R.id.cv_sc);

        // seat reservation card view onClickListener
        sr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomePage.this,SeatReservationActivity.class));
            }
        });

        // book search card view onClickListener
        bs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomePage.this,SearchActivity.class));
            }
        });

        // scan QR code card view onClickListener
        vs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomePage.this,ViewReservation.class));
            }
        });

        //end of onCreate
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void countdownTimer(String slot){
        String[] n_time=slot.split("-",2);
        n_time[1]=n_time[1].replace("am","AM").replace("pm","PM").replace(".",":");
        String time=n_time[1].substring(0,n_time[1].indexOf(":"));
        if(n_time[1].contains("PM"))
        {
            time = time + ":00 PM";
        }else if(n_time[1].contains("AM"))
        {
            time = time + ":00 AM";
        }
        long duration = 0;
        SimpleDateFormat h_mm_a   = new SimpleDateFormat("h:mm a");
        SimpleDateFormat hh_mm_ss = new SimpleDateFormat("HH:mm:ss");
        ZonedDateTime dateTime= ZonedDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"));
        String current_time=dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        try {
            Date d1 = h_mm_a.parse(time);
            Date systemDate = Calendar.getInstance().getTime();
            Date d2 = hh_mm_ss.parse(current_time);
            long millse = d1.getTime() - d2.getTime();
            long mills = Math.abs(millse);
            duration = mills;
        } catch (Exception e) {
            e.printStackTrace();
        }


        new CountDownTimer(duration,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long hoursInMilli = minutesInMilli * 60;

                long elapsedHours = millisUntilFinished / hoursInMilli;
                millisUntilFinished = millisUntilFinished % hoursInMilli;

                long elapsedMinutes = millisUntilFinished / minutesInMilli;
                millisUntilFinished = millisUntilFinished % minutesInMilli;

                long elapsedSeconds = millisUntilFinished / secondsInMilli;

                String yy = String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes,elapsedSeconds);
                tv_checkInOut.setText("   Time left for your seat : "+yy);
            }
            @Override
            public void onFinish() {
                tv_checkInOut.setText("00:00");
                ZonedDateTime dateTime= ZonedDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"));
                String current_time=dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                t=null;
                CheckExistedReserved check=new CheckExistedReserved();
                try {
                    //get slot
                    t=check.ExistedReserved(current_time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Task<QuerySnapshot> dReference=fStore.collection("reservation")
                        .whereEqualTo("Date",date).whereEqualTo("Time",t)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.getResult().getDocuments().isEmpty())
                                {
                                    autoFillCheckOut();
                                    layout_checkInOut.setVisibility(View.GONE);
                                }
                                else
                                {
                                    String id=task.getResult().getDocuments().get(0).get("UserID").toString();
                                    Boolean checkIn= (Boolean) task.getResult().getDocuments().get(0).get("CheckIn");
                                    Boolean checkOut= (Boolean) task.getResult().getDocuments().get(0).get("CheckOut");
                                    if(id.equals(userId))
                                    {
                                        Log.d(TAG,"User id : "+id);
                                        Log.d(TAG,"Check out : "+checkOut);
                                        Log.d(TAG,"Check in : "+checkIn);
                                        if(checkIn==false)
                                        {
                                            layout_checkInOut.setVisibility(View.VISIBLE);
                                            btn_checkInOut.setText("Check in Now");
                                            btn_checkInOut.setTextSize(TypedValue.COMPLEX_UNIT_SP,14.0f);
                                            countdownTimer(task.getResult().getDocuments().get(0).get("Time").toString());
                                            btn_checkInOut.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    startActivity(new Intent(HomePage.this,ScanActivity.class));
                                                }
                                            });
                                        }
                                        if(checkOut==true)
                                        {
                                            layout_checkInOut.setVisibility(View.GONE);
                                        }if(checkIn==true && checkOut==false)
                                    {
                                        layout_checkInOut.setVisibility(View.VISIBLE);
                                        btn_checkInOut.setText("Check out now");
                                        btn_checkInOut.setTextSize(TypedValue.COMPLEX_UNIT_SP,14.0f);
                                        countdownTimer(task.getResult().getDocuments().get(0).get("Time").toString());
                                        btn_checkInOut.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                AlertDialog.Builder builder=new AlertDialog.Builder(HomePage.this);
                                                builder.setCancelable(false);
                                                builder.setTitle("Check out");
                                                builder.setMessage("Are you sure want to check out and release the seat?");
                                                builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        task.getResult().getDocuments().get(0).getReference().update("CheckIn",true,"CheckOut",true);
                                                        layout_checkInOut.setVisibility(View.GONE);
                                                        builder.create().dismiss();
                                                    }
                                                });
                                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        builder.create().dismiss();
                                                    }
                                                });
                                                builder.create().show();
                                            }
                                        });
                                    }
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(HomePage.this, "Error "+e, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }.start();
    }

    private void autoFillCheckOut()
    {
        Task<QuerySnapshot> dReference=fStore.collection("reservation")
                .whereEqualTo("Date",date).whereEqualTo("Time",t).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        task.getResult().getDocuments().get(0).getReference().update("CheckOut",true);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

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
                Intent i = new Intent(HomePage.this, SeatReservationActivity.class);
                startActivity(i);
                break;
            case R.id.nav_book:
                Intent g = new Intent(HomePage.this, SearchActivity.class);
                startActivity(g);
                break;
            case R.id.nav_view:
                Intent h = new Intent(HomePage.this, ViewReservation.class);
                startActivity(h);
                break;
            case R.id.logout:
                Intent k=new Intent(HomePage.this,LogoutActivity.class);
                startActivity(k);
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}