package com.example.prototype;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;

import com.richpath.RichPath;
import com.richpath.RichPathView;

public class Floor1_book_fullScreen extends AppCompatActivity {

    private Intent intent;
    private Bundle bundle;
    private String pathName,floor;
    private RichPathView view;
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
        setContentView(R.layout.activity_floor1_book_full_screen);

        intent = getIntent();
        bundle = intent.getExtras();
        pathName = (String) bundle.get("PathName");
        floor = (String) bundle.get("floor");

        view = findViewById(R.id.floorPlan);

        if(Integer.valueOf(floor) == 1)
        {
            view.setVectorDrawable(R.drawable.floor2_asset);
            RichPath path = view.findRichPathByName(pathName);
            if(!path.isEmpty())
            {
                path.setFillAlpha(100);
                path.setStrokeAlpha(100);
            }
        }

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

        //end of OnCreate
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