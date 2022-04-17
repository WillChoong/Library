package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ScanResultActivity extends AppCompatActivity {

    private String qrcode;
    private String secretKey = "Xp2s5v8y/B?E(H+MbQeThWmZq3t6w9z$";
    private String AES = "AES";
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID, date, time, t, floor, seat;
    private String[] content;
    private Intent intent;
    private Bundle bundle;
    private Button btn_checkInOut;
    private static final String TAG = "Scan Result";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        intent = getIntent();
        bundle = intent.getExtras();
        qrcode = bundle.getString("content");
        try {
            qrcode = decrypt(qrcode, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        content = qrcode.split(";", 3);
        floor = content[0];
        seat = content[1];
        Log.d(TAG,"Floor"+floor+" Seat"+seat);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        Calendar calendar = Calendar.getInstance();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
        df.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        date = df.format(calendar.getTime());

        ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"));
        time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        t = null;
        CheckExistedReserved check = new CheckExistedReserved();
        try {
            //get slot
            t = check.ExistedReserved(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Task<QuerySnapshot> dReference = fStore.collection("reservation")
                .whereEqualTo("Date", date).whereEqualTo("Time", t)
                .whereEqualTo("Floor", floor).whereEqualTo("SeatID", seat).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().getDocuments().isEmpty()) {
                            //Toast.makeText(ScanResultActivity.this, "No record found ", Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"No record found");
                            Task<QuerySnapshot> documentReference = fStore.collection("reservation").whereEqualTo("UserID",userID).whereEqualTo("Date", date).whereEqualTo("Time", t).whereEqualTo("Check Out",false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if(!task.getResult().getDocuments().isEmpty())
                                        {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ScanResultActivity.this);
                                            builder.setCancelable(false);
                                            builder.setTitle("Warning!");
                                            String message = "You had reserved one seat no. " + task.getResult().getDocuments().get(0).get("SeatID") + " at " + task.getResult().getDocuments().get(0).get("Floor") + ".You are no longer able to take any seat at this time.";
                                            builder.setMessage(message);
                                            builder.setPositiveButton(
                                                    "Okay",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            builder.create().dismiss();
                                                            startActivity(new Intent(ScanResultActivity.this,HomePage.class));
                                                        }
                                                    }
                                            );
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();
                                        }else
                                        {
                                            DialogReserved();
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });

                        } else {
                            Log.d(TAG,"Record found");
                            String id = task.getResult().getDocuments().get(0).get("UserID").toString();
                            String checkOut = task.getResult().getDocuments().get(0).get("Check Out").toString();
                            if (id.equals(userID)) {
                                //Toast.makeText(ScanResultActivity.this, "Check In"+checkOut, Toast.LENGTH_SHORT).show();
                                task.getResult().getDocuments().get(0).getReference().update("Check In", true, "Check Out", false);
                                CheckInSuccess();
                            } else if (id != userID) {
                                //Toast.makeText(ScanResultActivity.this, "Someone reserved the seat", Toast.LENGTH_SHORT).show();
                                if (Boolean.parseBoolean(checkOut) == true) {
                                    DialogReserved();
                                } else {
                                    DialogSomeOneReserved();
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanResultActivity.this, "Error " + e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] key = messageDigest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    private String decrypt(String data, String password) throws Exception {
        SecretKeySpec keySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decodedvalue = Base64.decode(data, Base64.DEFAULT);
        byte[] decValue = cipher.doFinal(decodedvalue);
        String decryptValue = new String(decValue);
        return decryptValue;
    }

    private void CheckInSuccess() {
        //get time, date, floor, seat no
        String d = date;
        String time = t;
        String floor = content[0];
        String seat = content[1];
        /*Toast.makeText(this, ""+seat, Toast.LENGTH_SHORT).show();*/

        startActivity(new Intent(ScanResultActivity.this, CheckInActivity.class)
                .putExtra("Floor", floor).putExtra("Seat", seat)
                .putExtra("Date", d).putExtra("Slot", t));
        finish();
    }

    private void DialogReserved() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("No Record Found !");
        dialog.setMessage("No one has reserved the seat.Do you want to reserve the seat?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DocumentReference dr = fStore.collection("reservation").document();
                        Map<String, Object> rs = new HashMap<>();
                        rs.put("SeatID", content[1]);
                        rs.put("Floor", content[0]);
                        rs.put("UserID", userID);
                        rs.put("Date", date);
                        rs.put("Time", t);
                        rs.put("Check In", true);
                        rs.put("Check Out", false);
                        dr.set(rs).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //Toast.makeText(ScanResultActivity.this, "You had reserved the seat", Toast.LENGTH_SHORT).show();
                                ReservedSuccess();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ScanResultActivity.this, "Fail to reserve:" + e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                startActivity(new Intent(ScanResultActivity.this, HomePage.class));
                finish();
            }
        });

        dialog.show();
    }

    private void DialogSomeOneReserved() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Warning!");
        dialog.setMessage("Someone has reserved the seat")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(ScanResultActivity.this, HomePage.class));
                        finish();
                        //Toast.makeText(ScanResultActivity.this, "You cannot take this seat! Someone has reserved the seat!", Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.show();
    }

    private void ReservedSuccess() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Success!");
        dialog.setMessage("You has reserved and check in the seat")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(ScanResultActivity.this, HomePage.class));
                        finish();
                        //Toast.makeText(ScanResultActivity.this, "You cannot take this seat! Someone has reserved the seat!", Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.show();
    }

}