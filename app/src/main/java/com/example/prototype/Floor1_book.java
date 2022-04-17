package com.example.prototype;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.richpath.RichPath;
import com.richpath.RichPathView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Floor1_book extends AppCompatActivity {

    private String name,number,floor,rak,view,filename,direction;
    private  String[] bookDetails=new String[2];
    private static final String TAG="Receive Floor 1 book";
    private Intent intent;
    private Bundle bundle;
    private RichPathView richPathView;
    private RichPath path;
    private String pathName;
    private ToolTipWindow toolTipWindow;
    private Dialog dialog,fullScreen_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor1_book);



        intent=getIntent();
        bundle=intent.getExtras();
        name=bundle.getString("Name");
        number=bundle.getString("Number");
        floor=bundle.getString("Floor");
        rak=bundle.getString("Rak");
        view=bundle.getString("View");

        if(view.equals("F"))
        {
            filename="FLOOR"+floor+"_RAK"+rak+"_FRONT.json";
        }else
        {
            filename="FLOOR"+floor+"_RAK"+rak+"_BACK.json";
        }
        Log.d(TAG,"Filename : "+filename);

        bookDetails=readJson(filename,number);
        Log.d(TAG,"Result : "+bookDetails[0]);
        Log.d(TAG,"Result : "+bookDetails[1]);

        direction=direction(bookDetails[0],bookDetails[1]);
        Log.d(TAG,"Direction :"+direction);

        Log.d(TAG,"Overall : Rak"+rak+" . Direction : "+direction);
        pathName = "rak"+rak+"_"+direction;
        richPathView = findViewById(R.id.floorPlan);
        path = richPathView.findRichPathByName(pathName);
        if(!path.isEmpty())
        {
            path.setFillAlpha(100);
            path.setStrokeAlpha(100);
        }

        dialog = new Dialog(this);
        openDialog();


        //end of OnCreate
    }

    private void openDialog()
    {
        dialog.setContentView(R.layout.book_layout_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();


        RichPathView view = dialog.findViewById(R.id.floor_plan);
        RichPath path = view.findRichPathByName(pathName);
        if(!path.isEmpty())
        {
            path.setFillAlpha(100);
            path.setStrokeAlpha(100);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Floor1_book.this,Floor1_book_fullScreen.class));
            }
        });

        TextView title,callNumber,tv_floor,tv_rak;
        title = dialog.findViewById(R.id.tv_title);
        callNumber = dialog.findViewById(R.id.tv_callNumber);
        tv_floor = dialog.findViewById(R.id.tv_floor);
        tv_rak = dialog.findViewById(R.id.tv_rak);
        title.setText
                (name);
        callNumber.setText
                (": "+number);
        tv_floor.setText
                (": "+floor);
        tv_rak.setText
                (": "+rak);
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