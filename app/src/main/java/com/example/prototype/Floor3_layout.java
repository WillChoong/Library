package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.richpath.RichPath;
import com.richpath.RichPathView;

import java.util.HashMap;
import java.util.Map;

public class Floor3_layout extends AppCompatActivity {

    private static final String TAG = "Floor3_layout";
    private Boolean PcAccess, PowerSocket, NearWindow;
    private Bundle bundle;
    private Intent intent;
    private String date, time,userId;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private LinearLayout msg;
    private Button confirmReserved;
    private ProgressBar pb;
    private RichPathView view;
    private RichPath[] all;
    private float seatW,seatH;
    private int[] nearWindow={1,2,3,4,5,6,7,8,9,10,11,209,210,211,212,213,214,215,216,217,218,219,220,221,222};
    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat mGestureDetectorCompat;
    private Float mScaleFactor = 1.0f;
    private ViewTreeObserver viewTreeObserver;
    private float imageAspectRatio,viewAspectRatio;
    private float viewWidth = 0f;
    private float viewHeight = 0f;
    private float mDefaultImageWidth = 0f, mDefaultImageHeight = 0f;
    private float mViewPortWidth = 0f ,mViewPortHeight = 0f;
    private float translationX = 0f,translationY = 0f, mTranslationX = 0f ,mTranslationY = 0f;
    private float translationXMargin,translationYMargin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor3_layout);
        // access intent message from previous activity
        PcAccess = getIntent().getBooleanExtra("Criteria_PcAccess", false);
        PowerSocket = getIntent().getBooleanExtra("Criteria_PowerSocket", false);
        NearWindow = getIntent().getBooleanExtra("Criteria_NearWindow", false);
        intent = getIntent();
        bundle = intent.getExtras();
        date = (String) bundle.get("Date");
        time = (String) bundle.get("Time");

        // declare firebase and get user id
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        // check the user had reserved the seat at the date and time
        Task<QuerySnapshot> documentReference = fStore.collection("reservation").whereEqualTo("UserID",userId).whereEqualTo("Date",date).whereEqualTo("Time",time).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if((Boolean) document.get("CheckOut") == false)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Floor3_layout.this);
                            builder.setCancelable(false);
                            builder.setTitle("Warning!");
                            String message="You had reserved one seat no." + document.get("SeatID")+" in "+document.get("Floor")+". You are no longer able to reserve any seat at this time.";
                            builder.setMessage(message);
                            builder.setPositiveButton(
                                    "Okay",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivity(new Intent(Floor3_layout.this,SeatReservationActivity.class));
                                        }
                                    }
                            );
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        // declare the RichPath View
        view = findViewById(R.id.floorPlan);
        // get all the path name from the asset layout
        all=view.findAllRichPaths();

        //Scaling feature
        mScaleDetector=new ScaleGestureDetector(this,ScaleListener);
        mGestureDetectorCompat=new GestureDetectorCompat(this,PanListener);

        viewTreeObserver = view.getViewTreeObserver();
        if(viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    imageAspectRatio = (view.getDrawable().getIntrinsicWidth() / view.getDrawable().getIntrinsicWidth());
                    viewAspectRatio = view.getHeight() / view.getWidth();

                    if(imageAspectRatio < viewAspectRatio){
                        viewWidth = view.getWidth();
                    }else{
                        viewWidth = view.getHeight();
                    }

                    if(imageAspectRatio > viewAspectRatio){
                        viewHeight = view.getWidth();
                    }else{
                        viewHeight = view.getHeight();
                    }

                    mDefaultImageWidth = viewWidth;
                    mDefaultImageHeight = viewHeight;

                    mViewPortWidth = view.getWidth();
                    mViewPortHeight = view.getHeight();
                }
            });
        }

        // Highlight preferred seat
        // KEYWORDS : ps pc nw
        // search over where path name has KEYWORDS AND highlight
        // IF HAVE, get the seat number too
        // then, seat highlight should be appear

        if(PcAccess)
        {
            for (RichPath i : all)
            {
                if((i.getName()!=null && i.getName().contains("highlight")) && (i.getName().contains("pc")))
                {
                    i.setFillAlpha(100);
                    i.setStrokeAlpha(100);
                    getSeatNumber(i.getName());
                }
            }
        }

        if(PowerSocket)
        {
            for (RichPath i : all)
            {
                if((i.getName()!=null && i.getName().contains("highlight")) && (i.getName().contains("ps")))
                {
                    i.setFillAlpha(100);
                    i.setStrokeAlpha(100);
                    getSeatNumber(i.getName());
                }
            }
        }

        if(NearWindow){
            for(int j : nearWindow)
            {
                for (RichPath i : all){
                    if(i.getName()!=null && i.getName().equals("seat_highlight_"+j))
                    {
                        i.setHeight(37f);
                        i.setWidth(33f);
                        Log.d(TAG, "Seat Width: "+ seatW + "Seat height : "+seatH);
                        i.setStrokeAlpha(100);
                        i.setFillAlpha(100);
                    }
                }
            }
        }

        // get all the reserved seat information at that date and time
        Task<QuerySnapshot> documentReference2 = fStore.collection("reservation").whereEqualTo("Date",date).whereEqualTo("Time",time).whereEqualTo("Floor","Floor 3").whereEqualTo("CheckOut",false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String id = (String) document.get("SeatID");
                        Log.d(TAG, "ID : "+id);
                        Log.d(TAG,"Document id:"+document.getId());
                        for (RichPath i : all)
                        {
                            if(i.getName()!=null && i.getName().contains("seat"+id))
                            {
                                i.setFillColor(Color.RED);
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


        // seat layout OnClickListener
        view.setOnPathClickListener(new RichPath.OnPathClickListener() {
            @Override
            public void onClick(RichPath richPath) {
                if(richPath.getName() != null) {
                    if (richPath.getName().contains("seat") && !richPath.getName().contains("highlight")) {


                        Boolean check = checkClickMoreThanOne(richPath.getName());
                        if(check == true)
                        {
                            // In case of use click more than one seat, warn dialog will be shown
                            AlertDialog.Builder builder = new AlertDialog.Builder(Floor3_layout.this);
                            builder.setCancelable(false);
                            builder.setTitle("Warning!");
                            builder.setMessage("A student can only choose one seat.");
                            builder.setPositiveButton(
                                    "Okay",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    }
                            );
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                        else{

                            String sub = richPath.getName().substring(4);
                            String[] spilt = sub.split("_",2);
                            for(RichPath i : all)
                            {
                                if(i.getName()!=null && i.getName().contains("seat"+spilt[0]))
                                {
                                    String[] b_split = i.getName().split("_",2);
                                    if(b_split[0].equals("seat"+spilt[0]))
                                    {
                                        if(i.getFillColor() == getResources().getColor(R.color.select))
                                        {
                                            // click seat and change color
                                            i.setFillColor(getResources().getColor(R.color.available));
                                            // disappear message
                                            msg = findViewById(R.id.bottom_msg);
                                            msg.setVisibility(View.GONE);
                                        }
                                        else if(i.getFillColor() == getResources().getColor(R.color.available))
                                        {
                                            // click seat and change color
                                            i.setFillColor(getResources().getColor(R.color.select));
                                            int color = i.getFillColor();
                                            Log.d(TAG,"Color :"+color);
                                            //showMessage()
                                            msg = findViewById(R.id.bottom_msg);
                                            msg.setVisibility(View.VISIBLE);

                                            // button for confirm reserve
                                            confirmReserved = findViewById(R.id.confimReserved);
                                            // set onclick listener for button confirmReserved
                                            confirmReserved.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    pb = findViewById(R.id.progressbar);
                                                    pb.setVisibility(View.VISIBLE);


                                                    Task<QuerySnapshot> checkAgain= fStore.collection("reservation").whereEqualTo("Date",date)
                                                            .whereEqualTo("Time",time).whereEqualTo("SeatID",spilt[0])
                                                            .whereEqualTo("CheckOut",false).whereEqualTo("Floor","Floor 3").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        if(! task.getResult().isEmpty() && task.getResult().getDocuments().get(0).get("SeatID").toString().equals(spilt[0]))
                                                                        {
                                                                            AlertDialog.Builder builder = new AlertDialog.Builder(Floor3_layout.this);
                                                                            builder.setCancelable(false);

                                                                            builder.setTitle("Failed");
                                                                            String message="Someone had reserved the seat";
                                                                            builder.setMessage(message);
                                                                            builder.setPositiveButton(
                                                                                    "Okay",
                                                                                    new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                            builder.create().dismiss();
                                                                                        }
                                                                                    }
                                                                            );
                                                                            AlertDialog alertDialog = builder.create();
                                                                            alertDialog.show();
                                                                            //change color to red
                                                                            String sub = richPath.getName().substring(4);
                                                                            String[] spilt = sub.split("_",2);
                                                                            Log.d(TAG,"Click Name :"+i.getName());
                                                                            for(RichPath i : all) {
                                                                                if (i.getName() != null && i.getFillColor()==getResources().getColor(R.color.select)) {
                                                                                    i.setFillColor(Color.RED);
                                                                                }
                                                                            }
                                                                            pb.setVisibility(View.GONE);
                                                                            msg.setVisibility(View.GONE);
                                                                        }
                                                                        else
                                                                        {
                                                                            Log.d(TAG,"uploading.....");
                                                                            DocumentReference documentReference = fStore.collection("reservation").document();
                                                                            Map<String, Object> reserve = new HashMap<>();
                                                                            reserve.put("SeatID", spilt[0]);
                                                                            reserve.put("Floor","Floor 3");
                                                                            reserve.put("UserID", userId);
                                                                            reserve.put("Date", date);
                                                                            reserve.put("Time", time);
                                                                            reserve.put("CheckIn",false);
                                                                            reserve.put("CheckOut",false);
                                                                            documentReference.set(reserve).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Log.d(TAG, "OnSuccess : reservation is created by " + userId);
                                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(Floor3_layout.this);
                                                                                    builder.setCancelable(false);

                                                                                    builder.setTitle("Successfully");
                                                                                    String message="You had reserved a seat No."+spilt[0];
                                                                                    builder.setMessage(message);
                                                                                    builder.setPositiveButton(
                                                                                            "Okay",
                                                                                            new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                                    startActivity(new Intent(Floor3_layout.this,HomePage.class));
                                                                                                }
                                                                                            }
                                                                                    );
                                                                                    AlertDialog alertDialog = builder.create();
                                                                                    alertDialog.show();
                                                                                    pb.setVisibility(View.GONE);
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.d(TAG, "OnFailure : " + e.toString());
                                                                                    Toast.makeText(Floor3_layout.this, "Failed," + e.toString(), Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }

                                                                    } else {
                                                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                                                    }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        });

        //end of onCreate
    }

    private void getSeatNumber(String name)
    {
        int number = 0;
        String []h_split;
        String no;
        for(int j = 0; j<name.length(); j++) {
            // access each character
            char a = name.charAt(j);
            if(a == '_'){number++;}
        }
        if(number == 2)
        {
            h_split = name.split("_",3);
            no = h_split[2];
            Log.d(TAG, "Number: "+no);
            highlightSeat(no);
        }else if(number == 3)
        {
            h_split = name.split("_",4);
            no =  h_split[3];
            Log.d(TAG, "Number: "+no);
            highlightSeat(no);
        }
    }

    private void highlightSeat(String number)
    {
        for (RichPath i : all){
            if(i.getName()!=null && i.getName().equals("seat_highlight_"+number))
            {
                i.setHeight(37f);
                i.setWidth(33f);
                Log.d(TAG, "Seat Width: "+ seatW + "Seat height : "+seatH);
                i.setStrokeAlpha(100);
                i.setFillAlpha(100);
            }
        }
    }

    private Boolean checkClickMoreThanOne(String name)
    {
        String[] checkName = name.split("_",2);
        for(RichPath i:all)
        {
            if(i.getName()!=null)
            {
                if(i.getFillColor()==getResources().getColor(R.color.select))
                {
                    String[] y_name = i.getName().split("_",2);
                    if(y_name[0].equals(checkName[0]))
                    {
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    private Boolean checkCanReservedSeat(String id)
    {
        final String[] isReserved = {null};
        Task<QuerySnapshot> documentReference= fStore.collection("reservation").whereEqualTo("Date",date).whereEqualTo("Time",time).whereEqualTo("Floor","Floor 1").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.get("SeatID").toString().equals(id))
                        {
                            isReserved[0] = "true";
                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        if(isReserved[0]==null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        mGestureDetectorCompat.onTouchEvent(event);
        return mGestureDetectorCompat.onTouchEvent(event);
    }

    private GestureDetector.SimpleOnGestureListener PanListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            translationX = mTranslationX - distanceX;
            translationY = mTranslationY - distanceY;

            adjustTranslation(translationX,translationY);
            return true;
        }
    };

    private ScaleGestureDetector.SimpleOnScaleGestureListener ScaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener(){
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= mScaleDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor,5.0f));
            view.setScaleX(mScaleFactor);
            view.setScaleY(mScaleFactor);
            viewWidth = mDefaultImageWidth * mScaleFactor;
            viewHeight = mDefaultImageHeight * mScaleFactor;

            adjustTranslation(mTranslationX,mTranslationY);
            return true;
        }
    };

    private void adjustTranslation(Float X,Float Y){
        translationXMargin = Math.abs((viewWidth - mViewPortWidth) / 2);
        translationYMargin = Math.abs((viewHeight - mViewPortHeight) / 2);

        if(X < 0){
            mTranslationX = Math.max(X, -translationXMargin);
        }else{
            mTranslationX = Math.min(X, translationXMargin);
        }

        if(mTranslationY < 0){
            mTranslationY = Math.max(Y, -translationYMargin);
        }else{
            mTranslationY = Math.min(Y, translationYMargin);
        }

        view.setTranslationX(mTranslationX);
        view.setTranslationY(mTranslationY);
    }
}