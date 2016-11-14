/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umb.subway;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DialogActivity extends Activity {
    public final static String DEBUG_TAG="edu.umb.cs443.MYMSG";
    private String stationName;
    private String stationId;
    private String baseURL;
    private String mbtaKey;
    private JSONObject jsonObject = null;
    private TextView stopName;
    private List<StopInfomation> stopInformation;  // = new ArrayList<StopInfomation>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        stationId = bundle.getString("station_id");
        stationName = bundle.getString("station_name");
        baseURL = getResources().getString(R.string.mbta_base_url);
        mbtaKey = getResources().getString(R.string.mbta_key);
        stopInformation = new ArrayList<StopInfomation>();
        stopName = (TextView)findViewById(R.id.stop_name);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Remove all the space from the city or zipcode
            new WebserviceCaller().execute("predictionsbystop", stationId);
        } else {
            stopName.setText("No network connection available");
            Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);
    }

    /**
     * Callback method defined by the View
     * @param v
     */
    public void finishDialog(View v) {
        DialogActivity.this.finish();
    }

    /**
     * Uses AsyncTask to create a task away from the main UI thread. This task
     * takes json data url and download the content.
     */
    private class WebserviceCaller extends AsyncTask<String, Void, JSONObject> {
        public JSONObject doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url = null;
            InputStream inStream = null;
            try {
                url = new URL(baseURL + "?api_key=" + params[0] + mbtaKey + "&" + params[1] + "&format=json");
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
            long time;
            String mode = jsonObject.getJSONArray("mode").getJSONObject(0).getString("mode_name").toString();
            if(mode.equalsIgnoreCase("subway")){
                JSONArray jsonSubway = jsonObject.getJSONArray("mode").getJSONObject(0).getJSONArray("route");
                for (int routeCounter = 0; routeCounter < jsonSubway.length(); routeCounter++) {
                    JSONArray jsonRoute = jsonSubway.getJSONObject(routeCounter).getJSONArray("direction");
                    for(int directionCounter = 0; directionCounter < jsonRoute.length(); directionCounter++){
                        JSONArray jsonTrip = jsonRoute.getJSONObject(directionCounter).getJSONArray("trip");
                        for(int tripCounter = 0; tripCounter < jsonTrip.length(); tripCounter++){
                            stopInformation.add(new StopInfomation(
                                                    jsonTrip.getJSONObject(tripCounter).getString("trip_headsign"),
                                                    jsonTrip.getJSONObject(tripCounter).getInt("pre_away"),
                                                    jsonTrip.getJSONObject(tripCounter).getInt("trip_headsign")));
                        }
                    }
                }
            }
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
