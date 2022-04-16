package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutionException;

public class ScanActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG ="ScanQR" ;
    private Toolbar toolbar = null;
    private ImageView qr;
    private CoordinatorLayout layout;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private View scan,header;
    private TextView username, student_id,dateText,date;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private String userId;
    private DocumentReference documentReferenced;
    private PreviewView cameraView;
    private static final int PERMISSION_REQUEST_CAMERA=1;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        //Make the content become visible
        scan = findViewById(R.id.scan_QR);
        scan.setVisibility(View.VISIBLE);

        //Change toolbar title
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Scan QR");

        //The activity does not allow scan QR Code ; Make the QR code gone
        qr=findViewById(R.id.image_qrcode);
        qr.setVisibility(View.GONE);

        // check xia pls
        //layout = findViewById(R.id.tool_bar_layout);
        //layout.getBackground();

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
        fAuth= FirebaseAuth.getInstance();
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

        cameraView=(PreviewView) findViewById(R.id.view_camera);
        cameraProviderListenableFuture=ProcessCameraProvider.getInstance(this);
        requestCamera();
        //end of onCreate
    }

    private void requestCamera()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED)
        {
            startCamera();
        }
        else
        {
            ActivityCompat.requestPermissions(ScanActivity.this,new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_REQUEST_CAMERA)
        {
            if(grantResults.length==1&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                startCamera();
            }
            else
            {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera()
    {
        cameraProviderListenableFuture.addListener(()->{
            try{
                ProcessCameraProvider cameraProvider=cameraProviderListenableFuture.get();
                bindCameraPreview(cameraProvider);
            }
            catch (ExecutionException | InterruptedException e)
            {
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(ProcessCameraProvider cameraProvider)
    {
        cameraView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        Preview preview=new Preview.Builder().build();

        CameraSelector cameraSelector=new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        ImageAnalysis imageAnalysis=new ImageAnalysis.Builder().setTargetResolution(new Size(1280,720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String qrCode) {
                cameraProvider.unbindAll();
                startActivity(new Intent(ScanActivity.this,ScanResultActivity.class).putExtra("content",qrCode));
                finish();
            }

            @Override
            public void qrCodeNotFound() {

            }
        }));
        Camera camera= cameraProvider.bindToLifecycle((LifecycleOwner) this,cameraSelector,preview,imageAnalysis);
    }

    // avoid end application when the drawer is open
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(ScanActivity.this,HomePage.class));
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
                Intent h = new Intent(ScanActivity.this, HomePage.class);
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