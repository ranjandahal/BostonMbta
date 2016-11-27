package edu.umb.subway;

/**
 * Created by Ranjan on 11/12/2016.
 */

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 1000;
    public static DBHandlerMbta dbHandler;
    public static int screenSize;
    private static int hasLocationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        setupPermission();

        Properties.BASE_URL = getApplicationContext().getResources().getString(R.string.mbta_base_url);
        Properties.MBTA_KEY = getApplicationContext().getResources().getString(R.string.mbta_key);

        setupZoomLevels(getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK);

        dbHandler = new DBHandlerMbta(getApplicationContext());
        dbHandler.initialSetup();

        runMainActivity();
    }

//    @TargetApi(Build.VERSION_CODES.M)
    protected void setupPermission(){
        hasLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }
        runMainActivity();
    }
    protected void runMainActivity(){
        //if (hasLocationPermission == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(new Runnable() {
                /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */
                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    Intent mainActivity = new Intent(SplashActivity.this, MainActivity.class);
                    mainActivity.putExtra("screen_size", screenSize);
                    startActivity(mainActivity);

                    // close this activity
                    finish();
                }
            }, SPLASH_TIME_OUT);
        //}
    }

    protected void setupPermissionBelowM(){
        if (Build.VERSION.SDK_INT < 23) {

        }
    }

    protected void setupZoomLevels(int screenSize){
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                Properties.MIN_ZOOM = 11.5f;
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                Properties.MIN_ZOOM = 11.0f;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                Properties.MIN_ZOOM = 10.5f;
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                Properties.MIN_ZOOM = 10.0f;
                break;
            default:
        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }
}