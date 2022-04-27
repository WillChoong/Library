package com.example.prototype;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

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

public class ComputeReservation {

    private static final String TAG ="ComputeReservation" ;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Boolean compareDateAndTime(String date, String time)
    {

        Boolean checkDate = CompareDate(date,time);
        if(checkDate)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Boolean CompareDate(String date, String time)
    {
        // parse date
        DateFormat df_medium_uk = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
        Date parse_date = null;
        try {
            parse_date = df_medium_uk.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"Parse Date :" +parse_date);

        // parse current date
        Calendar calendar = Calendar.getInstance();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
        df.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        String current_date = df.format(calendar.getTime());

        Date currentDate = null;
        try {
            currentDate = df.parse(current_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"Today :" +currentDate);

        //compare date
        if(date.equals(current_date))
        {
            Log.d(TAG,"Today : Same Date");
            Boolean check = CompareTime(time);
            if (check)
            {
                return true;
            }
            else
            {
                return false;
            }
        }else
        {
            if(parse_date.after(currentDate))
            {
                return true;
            }else{
                return false;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Boolean CompareTime(String slot)
    {
        String[] n_time=slot.split("-",2);
        n_time[0]=n_time[0].replace("am","AM").replace("pm","PM").replace(".",":");
        String time=n_time[0].substring(0,n_time[0].indexOf(":"));




        if(n_time[0].contains("PM"))
        {
            time = time + ":00 PM";
        }else if(n_time[0].contains("AM"))
        {
            time = time + ":00 AM";
        }
        Log.d(TAG,"Time"+time);

        ZonedDateTime dateTime= ZonedDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"));
        String current_time=dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        SimpleDateFormat h_mm   = new SimpleDateFormat("HH:mm");
        SimpleDateFormat h_mm_a   = new SimpleDateFormat("h:mm a");

        try {
            Date d1 = h_mm_a.parse(time);
            String getSlot = h_mm_a.format(d1);
            getSlot=getSlot.substring(0,n_time[0].indexOf(":"));
            Date current_date = h_mm.parse(current_time);
            String CurrentTime = h_mm.format(current_date);
            CurrentTime=CurrentTime.substring(0,n_time[0].indexOf(":"));
            Log.d(TAG,"d1 :"+d1+" . current_date : "+current_date);
            Log.d(TAG,"getSlot :"+getSlot+" . current_time : "+CurrentTime);

            if(getSlot.equals(CurrentTime))
            {
                Log.d(TAG,"Equal : Same Date"+CurrentTime);
                return true;
            }else
            {
                if(d1.after(current_date))
                {
                    Log.d(TAG,"Is before");
                    return true;
                }
                else if(d1.before(current_date))
                {
                    Log.d(TAG,"Is after");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.d(TAG,"Exception : Same Date"+e);
            e.printStackTrace();

        }
        return false;
    }
}
