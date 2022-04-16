package com.example.prototype;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CheckExistedReserved {

    private FirebaseFirestore fStore=FirebaseFirestore.getInstance();
    private FirebaseAuth fAuth=FirebaseAuth.getInstance();

    public String ExistedReserved(String hour) throws ParseException {
        //hour to 12 hour format
        //access fireStore
        //get userid where equal to date and time
        //return userId
        String userID=fAuth.getCurrentUser().getUid();
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat format_24=new SimpleDateFormat("HH:mm");
        SimpleDateFormat format_12=new SimpleDateFormat("hh:mm a");
        Date date_24=format_24.parse(hour);
        String h=format_12.format(date_24);
        h=h.replace("AM","am").replace("PM","pm").replace(" ","");
        String time=h.substring(0,h.indexOf(":"));
        int t=Integer.parseInt(time);
        int t2=0;
        String result=null;
        if(h.contains("am"))
        {
            h=t+".00am";
            if(t==12)
            {
                t2=t+1;
            }
            else
            {
                t2=t+1;
            }
        }
        else if(h.contains("pm"))
        {
            h=t+".00pm";
            if(t==12)
            {
                t2=1;
            }
            else
            {
                t2=t+1;
            }
        }

        if(h.contains("am"))
        {
            if(t==11)
            {
                result=t2+".00pm";
            }
            else if(t==12)
            {
                result=1+".00am";
            }
            else{
                result=t2+".00am";
            }
        }
        else if(h.contains("pm"))
        {
            if(t==11)
            {
                result=t2+".00am";
            }
            else if(t==12)
            {
                result=1+".00pm";
            }
            else
            {
                result=t2+".00pm";
            }
        }

        String total_result=h+"-"+result;
        total_result=total_result.trim();
        DateFormat df=DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
        df.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        String date=df.format(calendar.getTime());

        String[] uid=new String[1];

        Task<QuerySnapshot> dReference=fStore.collection("reservation").whereEqualTo("Date",date).whereEqualTo("Time",total_result).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                for(QueryDocumentSnapshot document : task.getResult())
                {
                    uid[0]=document.get("UserID").toString();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uid[0]="Error";
                e.printStackTrace();
            }
        });

        return total_result;
    }

}