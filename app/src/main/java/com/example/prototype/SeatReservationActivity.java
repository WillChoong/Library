package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class SeatReservationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG ="SeatReservationForm" ;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar = null;
    private ImageView qr;
    private View seat,header;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private TextView username, student_id,dateText,date;
    private String userId;
    private DocumentReference documentReferenced;
    private Spinner spinner_level,spinnerTime,spinnerFloor;
    private Calendar cv;
    private Date current_date;
    private GregorianCalendar gregorianCalendar;
    private int dayOfWeek;
    private CheckBox ps,pc,dr,nf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_reservation);

        //Change toolbar title
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Seat Reservation");

        //The activity does not allow scan QR Code ; Make the QR code gone
        qr=findViewById(R.id.image_qrcode);
        qr.setVisibility(View.GONE);

        //Make the content become visible
        seat = findViewById(R.id.seat_reservation_form);
        seat.setVisibility(View.VISIBLE);

        //add triple line in layout(三)
        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Declare navigation view and set listener
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header=navigationView.getHeaderView(0);
        username=header.findViewById(R.id.username);
        student_id=header.findViewById(R.id.student_id);

        // Declare firebaseAuth and firebaseFireStore
        fStore= FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
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

        //Setup  spinner to select floor level
        spinner_level = findViewById(R.id.floor_level);
        final String[] floor_level = new String[]{"Upper ground", "First floor","Second floor","Third Floor"};
        final List<String> floor = new ArrayList<>(Arrays.asList(floor_level));
        final ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, floor) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return true;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                tv.setTextColor(Color.BLACK);

                return view;
            }
        };
        stringArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner_level.setAdapter(stringArrayAdapter);

        // Setup calendar and get current date
        cv = Calendar.getInstance();
        int c_year = cv.get(Calendar.YEAR);
        int c_month = cv.get(Calendar.MONTH);
        int c_day = cv.get(Calendar.DAY_OF_MONTH);
        int c_hour = cv.get(Calendar.HOUR_OF_DAY);
        current_date = Calendar.getInstance().getTime();
        gregorianCalendar = new GregorianCalendar(c_year, c_month, c_day - 1);
        dayOfWeek = gregorianCalendar.get(Calendar.DAY_OF_WEEK);

        // dialog to select date
        dateText = findViewById(R.id.date_choice);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(SeatReservationActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(0);
                        calendar.set(year, month, dayOfMonth);
                        Date chosenDate = calendar.getTime();
                        GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, dayOfMonth - 1);
                        int dayofWeek = gregorianCalendar.get(Calendar.DAY_OF_WEEK);

                        DateFormat df_medium_uk = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
                        String displayDate = df_medium_uk.format(chosenDate);
                        Date date = new Date();
                        String todayDate = df_medium_uk.format(date);
                        final Spinner start_timeText = findViewById(R.id.start_time);
                        //t start_timeText.setVisibility(View.VISIBLE);
                        if (displayDate.equals(todayDate)) {
                            //Toast.makeText(SeatReservation.this, "You have choose today " + cv.get(Calendar.HOUR_OF_DAY), Toast.LENGTH_SHORT).show();

                            //spinner for time
                            ZonedDateTime dateTime= ZonedDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"));
                            String time=dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                            int hour=Integer.parseInt(time.substring(0,time.indexOf(":")));
                            isToday(hour, dayofWeek);
                        } else {
                            //spinner for time
                            weekend(dayofWeek);
                        }
                        dateText.setText(displayDate);

                    }

                }, c_year, c_month, c_day);

                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        //end of onCreate
    }

    // statement to provide valid time to select when the date select is today
    private void isToday(int i, int weekofDay) {
        final String[] time_list_normal = new String[]{"Select your time", "8.00am-9.00am", "9.00am-10.00am", "10.00am-11.00am", "11.00am-12.00pm", "12.00pm-1.00pm", "1.00pm-2.00pm", "2.00pm-3.00pm", "3.00pm-4.00pm", "4.00pm-5.00pm", "5.00pm-6.00pm", "6.00pm-7.00pm", "7.00pm-8.00pm", "8.00pm-9.00pm"};
        final String[] time_list_weekend = new String[]{"Select your time", "9.00am-10.00am", "10.00am-11.00am", "11.00am-12.00pm", "12.00pm-1.00pm", "1.00pm-2.00pm", "2.00pm-3.00pm", "3.00pm-4.00pm", "4.00pm-5.00pm"};
        if (weekofDay == 6 | weekofDay == 7) {
            if (i <= 17) {
                if (i >= 9) {
                    for (int j = 0; j < time_list_weekend.length; j++) {
                        if (i < 11) {
                            String time = i + ".00am-" + (i + 1) + ".00am";
                            if (time_list_weekend[j].equals(time)) {
                                final List<String> timeslot = new ArrayList<>(Arrays.asList(time_list_weekend));
                                setSpinner(timeslot, j - 1);
                                break;
                            }
                        } else if (i == 11) {
                            String time = i + ".00am-" + (i + 1) + ".00pm";
                            if (time_list_weekend[j].equals(time)) {
                                final List<String> timeslot = new ArrayList<>(Arrays.asList(time_list_weekend));
                                setSpinner(timeslot, j - 1);
                                break;
                            }
                        } else if (i == 12) {
                            String time = "12.00pm-1.00pm";
                            //Toast.makeText(SeatReservation.this,"Time is"+time,Toast.LENGTH_SHORT).show();
                            if (time_list_weekend[j].equals(time)) {
                                //Toast.makeText(SeatReservation.this,"Now is 12pm",Toast.LENGTH_SHORT).show();
                                final List<String> timeslot = new ArrayList<>(Arrays.asList(time_list_weekend));
                                setSpinner(timeslot, j - 1);
                                break;
                            }
                        } else if (i >= 13) {
                            String time = (i - 12) + ".00pm-" + (i + 1 - 12) + ".00pm";
                            //Toast.makeText(SeatReservation.this,"Time is"+time,Toast.LENGTH_SHORT).show();
                            if (time_list_weekend[j].equals(time)) {
                                final List<String> timeslot = new ArrayList<>(Arrays.asList(time_list_weekend));
                                setSpinner(timeslot, j - 1);
                                break;
                            }
                        }
                    }
                } else {
                    final List<String> time = new ArrayList<>(Arrays.asList(time_list_weekend));
                    setSpinner(time, 0);
                }
            } else {
                final List<String> time = new ArrayList<>(Arrays.asList(time_list_weekend));
                setSpinner(time, time_list_weekend.length);
            }
        } else {
            if (i <= 21) {
                if (i >= 8) {
                    for (int j = 0; j < time_list_normal.length; j++) {
                        if (i < 11) {
                            String time = i + ".00am-" + (i + 1) + ".00am";
                            if (time_list_normal[j].equals(time)) {
                                final List<String> timeslot = new ArrayList<>(Arrays.asList(time_list_normal));
                                setSpinner(timeslot, j - 1);
                                break;
                            }
                        } else if (i == 11) {
                            String time = i + ".00am-" + (i + 1) + ".00pm";
                            if (time_list_normal[j].equals(time)) {
                                final List<String> timeslot = new ArrayList<>(Arrays.asList(time_list_normal));
                                setSpinner(timeslot, j - 1);
                                break;
                            }
                        } else if (i == 12) {
                            String time = "12.00pm-1.00pm";
                            if (time_list_normal[j].equals(time)) {
                                final List<String> timeslot = new ArrayList<>(Arrays.asList(time_list_normal));
                                setSpinner(timeslot, j - 1);
                                break;
                            }
                        } else if (i >= 13) {
                            String time = (i - 12) + ".00pm-" + (i + 1 - 12) + ".00pm";
                            if (time_list_normal[j].equals(time)) {
                                final List<String> timeslot = new ArrayList<>(Arrays.asList(time_list_normal));
                                setSpinner(timeslot, j - 1);
                                break;
                            }
                        }
                    }
                } else {
                    final List<String> time = new ArrayList<>(Arrays.asList(time_list_normal));
                    setSpinner(time, 0);
                }
            } else {
                final List<String> time = new ArrayList<>(Arrays.asList(time_list_normal));
                setSpinner(time, time_list_normal.length);
            }

        }
    }

    // statement to provide valid time to select when the date select is weekend
    public void weekend(int weekofDay) {

        final String[] time_list_normal = new String[]{"Select your time", "8.00am-9.00am", "9.00am-10.00am", "10.00am-11.00am", "11.00am-12.00pm", "12.00pm-1.00pm", "1.00pm-2.00pm", "2.00pm-3.00pm", "3.00pm-4.00pm", "4.00pm-5.00pm", "5.00pm-6.00pm", "6.00pm-7.00pm", "7.00pm-8.00pm", "8.00pm-9.00pm"};
        final String[] time_list_weekend = new String[]{"Select your time", "9.00am-10.00am", "10.00am-11.00am", "11.00am-12.00pm", "12.00pm-1.00pm", "1.00pm-2.00pm", "2.00pm-3.00pm", "3.00pm-4.00pm", "4.00pm-5.00pm"};
        int position = 0;
        if (weekofDay == 6 | weekofDay == 7) {
            final List<String> time = new ArrayList<>(Arrays.asList(time_list_weekend));
            setSpinner(time, position);
        } else {
            final List<String> time = new ArrayList<>(Arrays.asList(time_list_normal));
            setSpinner(time, position);
        }
    }

    public void setSpinner(List<String> time, int time_position) {
        final Spinner start_timeText = findViewById(R.id.start_time);

        final ArrayAdapter<String> stringArrayAdapter_time = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, time) {
            @Override
            public boolean isEnabled(int position) {
                return position > time_position;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (position <= time_position) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(Color.GRAY);
                } else {
                    TextView textView = (TextView) view;
                    textView.setTextColor(Color.BLACK);
                }

                return view;
            }
        };
        stringArrayAdapter_time.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        start_timeText.setAdapter(stringArrayAdapter_time);

    }

    // Checkbox to select seat type
    // If select 'no preference', other checkbox uncheck
    public void checkbox_validating(View view) {
        ps = findViewById(R.id.checkbox_PowerSocket);
        pc = findViewById(R.id.checkbox_PCAccess);
        dr = findViewById(R.id.checkbox_NearWindow);
        nf = findViewById(R.id.checkbox_NoPreference);
        if (nf.isChecked()) {
            ps.setChecked(false);
            pc.setChecked(false);
            dr.setChecked(false);
        }
        if (ps.isChecked()) {
            nf.setChecked(false);
        }
        if (pc.isChecked()) {
            nf.setChecked(false);
        }
        if (dr.isChecked()) {
            nf.setChecked(false);
        }
    }

    // Checkbox to select seat type
    // If checkbox that is not 'no preference', no preference is uncheck too
    public void checkbox_validate(View view) {
        ps = findViewById(R.id.checkbox_PowerSocket);
        pc = findViewById(R.id.checkbox_PCAccess);
        dr = findViewById(R.id.checkbox_NearWindow);
        nf = findViewById(R.id.checkbox_NoPreference);
        if (ps.isChecked()) {
            nf.setChecked(false);
        }
        if (pc.isChecked()) {
            nf.setChecked(false);
        }
        if (dr.isChecked()) {
            nf.setChecked(false);
        }
    }

    // Check the field either is empty or not
    private boolean validateDate() {
        TextView dateText = findViewById(R.id.date_choice);
        if (dateText.getText().toString().isEmpty()) {
            dateText.setError("Field cannot be empty");
            return false;
        } else {
            dateText.setError(null);
            return true;
        }
    }

    // Check the field either is empty or not
    private boolean validateTime() {
        Spinner spinner_time = findViewById(R.id.start_time);

        if (spinner_time.getSelectedItemPosition() > 0) {
            return true;
        } else {
            if (spinner_time.getAdapter() != null && spinner_time.getAdapter().getCount() ==0) {
                TextView errorTextView = (TextView) spinner_time.getSelectedView();
                errorTextView.setError("Your Error Message here");
            }else
            {
                Toast.makeText(this, "Please select the time !", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    public void detailSeat(View view) {
        if (!validateTime() | !validateDate()) {
            return;
        } else {
            //Get selected date
            date = findViewById(R.id.date_choice);
            String dateText = date.getText().toString();

            //Get selected start time
            spinnerTime = findViewById(R.id.start_time);
            String time = spinnerTime.getSelectedItem().toString();

            //Get selected floor level
            spinnerFloor = findViewById(R.id.floor_level);
            String floor_level = spinnerFloor.getSelectedItem().toString();


            boolean selectPowerSocket = false, selectPcAccess = false, selectNearWindow = false;
            CheckBox ps, pc, nr;
            ps = findViewById(R.id.checkbox_PowerSocket);
            pc = findViewById(R.id.checkbox_PCAccess);
            nr = findViewById(R.id.checkbox_NearWindow);

            if (ps.isChecked()) {
                selectPowerSocket = true;
            }
            if (pc.isChecked()) {
                selectPcAccess = true;
            }
            if (nr.isChecked()) {
                selectNearWindow = true;
            }

            Intent floor1 = new Intent(SeatReservationActivity.this, Floor2_layout.class);
            Intent floor2 = new Intent(SeatReservationActivity.this, HomePage.class);

            if (selectPcAccess == true) {
                floor2.putExtra("Criteria_PcAccess", true);
                floor1.putExtra("Criteria_PcAccess", true);
            }
            if (selectPowerSocket == true) {
                floor2.putExtra("Criteria_PowerSocket", true);
                floor1.putExtra("Criteria_PowerSocket", true);
            }
            if (selectNearWindow == true) {
                floor2.putExtra("Criteria_NearWindow", true);
                floor1.putExtra("Criteria_NearWindow", true);
            }

            // Still need add things
            if (floor_level == "First floor") {
                floor1.putExtra("Date", dateText);
                floor1.putExtra("Time", time);
                startActivity(floor1);
            } else {
                floor2.putExtra("Date", dateText);
                floor2.putExtra("Time", time);
                startActivity(floor2);
            }
        }
    }


    // avoid end application when the drawer is open
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(SeatReservationActivity.this,HomePage.class));
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
                Intent h = new Intent(SeatReservationActivity.this, HomePage.class);
                startActivity(h);
                break;
            case R.id.nav_seat:
                break;
            case R.id.nav_book:
                //Intent g = new Intent(SeatReservationActivity.this, BookAvailability.class);
                //startActivity(g);
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}