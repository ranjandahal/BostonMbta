package edu.umb.subway;

/**
 * Created by Ranjan on 11/12/2016.
 */

import android.os.AsyncTask;
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
    private static String mbta_key = "";
    private static String mbta_base_url = "";
    private JSONObject jsonObject = null;
    public final static String DEBUG_TAG="edu.umb.cs443.MYMSG";
    public static DBHandlerMbta dbHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        mbta_key = getString(R.string.mbta_key);
        mbta_base_url = getString(R.string.mbta_base_url);
        dbHandler = new DBHandlerMbta(getApplicationContext());
        dbHandler.initialSetup();

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
                startActivity(mainActivity);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    /*Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }*/
    private class WebserviceCaller extends AsyncTask<String, Void, JSONObject> {
        public JSONObject doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url = null;
            InputStream inStream = null;
            try {
                url = new URL(mbta_base_url + params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                inStream = urlConnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp, response = "";
                while ((temp = bReader.readLine()) != null) {
                    response += temp;
                }
                jsonObject = (JSONObject) new JSONTokener(response).nextValue();
            } catch (Exception e) {
            } finally {
                if (inStream != null) {
                    try {
                        // this will close the bReader as well
                        inStream.close();
                    } catch (IOException ignored) {
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return (jsonObject);
        }

        /**
         * json data gets parsed to get temperature, log and lat values. A new AsyncTask
         * gets call inside this method to download image.
         * @param result Json data
         */
        protected void onPostExecute(JSONObject result) {
            if(result!=null) {
                parseJSONObject(result);
                //Make image download call once all the data gets parsed
            }
            else{
                Log.i(DEBUG_TAG, "returned bitmap is null");}
        }
    }

    /**
     * Takes in Json data and parse it to usable information for this app purpose
     * @param jsonObject
     */
    public void parseJSONObject(JSONObject jsonObject){
        try {
            /*temp = jsonObject.getJSONObject("main").getDouble("temp") - 273.0;
            //Set temperature value
            tempTextView.setText(String.format("%.1fC" ,temp));

            //Get lattitude and longitude values
            lat = jsonObject.getJSONObject("coord").getDouble("lat");
            lon = jsonObject.getJSONObject("coord").getDouble("lon");

            //Set image name value for next image download Async task.
            imageName = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");*/
        }catch (Exception ex){}
    }
}