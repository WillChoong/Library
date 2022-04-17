package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.richpath.RichPath;
import com.richpath.RichPathView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,BookAdapter.ListItemClickListener{

    private static final String TAG ="SearchPage" ;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar=null;
    private View content,header;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private TextView username, student_id;
    private String userId,direction,pathName;
    private DocumentReference documentReferenced;
    private ImageView qr;
    private EditText tv_Search;
    private Button btn_Search;
    private List<BookData> bookDataList=new ArrayList<>();
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private  String[] bookDetails=new String[2];
    private Dialog moreDetail_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Make the content become visible
        content = findViewById(R.id.search_book);
        content.setVisibility(View.VISIBLE);

        //The activity does not allow scan QR Code ; Make the QR code gone
        qr=findViewById(R.id.image_qrcode);
        qr.setVisibility(View.GONE);

        //Change toolbar title
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search Book");

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

        tv_Search = findViewById(R.id.tv_searchBook);
        btn_Search = findViewById(R.id.btn_searchBook);


        tv_Search.setOnEditorActionListener(editorActionListener);

        btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_Search.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        bookAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        bookAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });
                readJsonFromBookData(tv_Search.getText().toString());
            }
        });

        //end of OnCreate
    }

    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            switch (i)
            {
                case EditorInfo.IME_ACTION_SEARCH:
                    tv_Search.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            bookAdapter.getFilter().filter(charSequence);
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            bookAdapter.getFilter().filter(charSequence);
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });
                    readJsonFromBookData(tv_Search.getText().toString());
                    break;
            }
            return false;
        }
    };

    private void readJsonFromBookData(String keyword)
    {
        int k=0;
        String prev_BookName=null;
        try{
            InputStream is=getAssets().open("BookData.json");
            int size=is.available();
            byte[] buffer=new byte[size];
            is.read(buffer);
            is.close();
            String json=new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray=new JSONArray(json);
            //JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)));
            for(int i=0;i< jsonArray.length();i++)
            {
                JSONObject obj=jsonArray.getJSONObject(i);
                if(obj.getString("Title").contains(keyword))
                {
                    BookData bookData=new BookData(obj.getString("Title")
                            ,obj.getString("Call Number")
                            ,obj.getString("floor")
                            ,obj.getString("rak")
                            ,obj.getString("view"));

                    if(k==0) {
                        bookDataList.add(bookData);
                    }
                    else if(!(bookData.getBookName().equals(prev_BookName)))
                    {
                        bookDataList.add(bookData);
                    }

                    k++;
                    prev_BookName=bookData.BookName;
                }
            }
            //Log.d(TAG,"Search :"+bookDataList.get(2).BookName.toString());
            recyclerView=(RecyclerView) findViewById(R.id.searchResultView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
            bookAdapter=new BookAdapter(bookDataList, this);
            recyclerView.setAdapter(bookAdapter);
        }catch(Exception e)
        {
            Log.e(TAG,e.toString());
        }
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
                Intent i = new Intent(SearchActivity.this, SeatReservationActivity.class);
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

    @Override
    public void onListItemClick(int position) {
        //Toast.makeText(this, bookDataList.get(position).getBookName(), Toast.LENGTH_SHORT).show();
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.book_card_view);
        dialog.setCancelable(true);

        TextView name=(TextView) dialog.findViewById(R.id.tv_name);
        TextView number=(TextView) dialog.findViewById(R.id.tv_callNumber);
        TextView floor=(TextView) dialog.findViewById(R.id.tv_floor);
        Button btn=(Button) dialog.findViewById(R.id.btn_details);

        name.setText(bookDataList.get(position).getBookName());
        number.setText(bookDataList.get(position).getCallNumber());
        floor.setText("Floor "+bookDataList.get(position).getFloor());
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                moreDetail(bookDataList.get(position).getBookName()
                        ,bookDataList.get(position).getCallNumber()
                        ,bookDataList.get(position).getFloor()
                        ,bookDataList.get(position).getRak()
                        ,bookDataList.get(position).getView());
            }
        });
    }

    private void moreDetail(String name,String number,String floor,String rak,String view)
    {
        String filename;
        if(view.equals("F"))
        {
            filename="FLOOR"+floor+"_RAK"+rak+"_FRONT.json";
        }else
        {
            filename="FLOOR"+floor+"_RAK"+rak+"_BACK.json";
        }
        bookDetails=readJson(filename,number);
        direction=direction(bookDetails[0],bookDetails[1]);
        pathName = "rak"+rak+"_"+direction;
        openDialog(name,number,floor,rak);
    }

    private void openDialog(String txt_name,String txt_number,String txt_floor,String txt_rak)
    {
        moreDetail_dialog = new Dialog(this);
        moreDetail_dialog.setContentView(R.layout.book_layout_dialog);
        moreDetail_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        moreDetail_dialog.setCancelable(false);
        moreDetail_dialog.show();

        Log.d(TAG,"Path Name"+pathName);
        RichPathView view = moreDetail_dialog.findViewById(R.id.floor_plan);
        if(Integer.valueOf(txt_floor) == 1)
        {
            view.setVectorDrawable(R.drawable.floor2_asset);
            RichPath path = view.findRichPathByName(pathName);
            if(!path.isEmpty())
            {
                path.setFillAlpha(100);
                path.setStrokeAlpha(100);
            }
        }


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this,Floor1_book_fullScreen.class)
                        .putExtra("floor",txt_floor)
                        .putExtra("PathName",pathName));
            }
        });

        TextView title,callNumber,tv_floor,tv_rak;
        title = moreDetail_dialog.findViewById(R.id.tv_title);
        callNumber = moreDetail_dialog.findViewById(R.id.tv_callNumber);
        tv_floor = moreDetail_dialog.findViewById(R.id.tv_floor);
        tv_rak = moreDetail_dialog.findViewById(R.id.tv_rak);
        title.setText
                (txt_name);
        callNumber.setText
                (": "+txt_number);
        tv_floor.setText
                (": "+txt_floor);
        tv_rak.setText
                (": "+txt_rak);

        Button okay;
        okay = moreDetail_dialog.findViewById(R.id.btn_okay);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreDetail_dialog.dismiss();
            }
        });
    }

    private String[] readJson(String filename, String callNumber)
    {

        try {
            InputStream is = getAssets().open(filename);
            int size=is.available();
            byte[] buffer=new byte[size];
            is.read(buffer);
            is.close();

            String json=new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray=new JSONArray(json);

            for(int i=0;i< jsonArray.length();i++)
            {
                JSONObject obj=jsonArray.getJSONObject(i);
                if(obj.getString("Call Number").equals(callNumber))
                {
                    bookDetails[0]=obj.getString("Number");
                    bookDetails[1]= Integer.toString(jsonArray.length());
                    break;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return bookDetails;
    }

    private String direction(String callNumber,String total)
    {
        String[][]book=new String[6][6];
        int number=Integer.valueOf(callNumber);
        int totalBook=Integer.valueOf(total);
        int split=totalBook/36;
        int check=totalBook%36;
        if(check!=0)
        {
            split=split+1;
        }
        int k=1;
        for(int i=0;i<book.length;i++)
        {
            for(int j=0;j<book[i].length;j++)
            {
                book[i][j]=String.valueOf(split*k);
                k++;
            }
        }
        int place=0;
        for(int i=0;i<book.length;i++)
        {
            for(int j=0;j<book[i].length;j++)
            {
                if(Integer.valueOf(number)<=Integer.valueOf(book[i][j].toString()))
                {
                    place=j;
                    break;
                }
            }
        }

        String direction=null;

        if(place==0 || place==1)
        {
            direction="left";
        }else if(place==2 || place==3)
        {
            direction="mid";
        }else if(place==4 || place==5)
        {
            direction="right";
        }

        return direction;
    }
}