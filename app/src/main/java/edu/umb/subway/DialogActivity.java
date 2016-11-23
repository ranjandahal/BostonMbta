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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
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
import java.util.logging.Handler;

public class DialogActivity extends FragmentActivity {
    public final static String DEBUG_TAG = "edu.umb.cs443.MYMSG";
    private String stationName;
    private String stationId;
    private String baseURL;
    private String mbtaKey;
    private int color;
    private Boolean favorite;
    private Double desLat, desLon;
    private static String jsonString = "";
    private JSONObject jsonObject = null;
    private TextView stationNameTextView, stationInfoTextView, destination, distanceAway, timer;
    private Chronometer timerChrono;
    private List<StopInformation> stopInformationList;  // = new ArrayList<StopInformation>();;
    private List<String> alertList;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_dialog);
        setContentView(R.layout.infowindow_layout);

        stationId = bundle.getString("station_id");
        stationName = bundle.getString("station_name");
        color = bundle.getInt("color");
        desLat = bundle.getDouble("lat");
        desLon = bundle.getDouble("lon");
        baseURL = getResources().getString(R.string.mbta_base_url);
        mbtaKey = getResources().getString(R.string.mbta_key);
        stopInformationList = new ArrayList<StopInformation>();
        alertList = new ArrayList<String>();
        stationNameTextView = (TextView) findViewById(R.id.station_name);
        stationInfoTextView = (TextView) findViewById(R.id.station_info);
        destination = (TextView) findViewById(R.id.destination);
        distanceAway = (TextView) findViewById(R.id.distance_away);
        timer = (TextView) findViewById(R.id.timer);
        timerChrono = (Chronometer) findViewById(R.id.timerChrono);

        stationNameTextView.setText(stationName);
        stationNameTextView.setBackgroundColor(color);
        //handler = new Handler();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new WebserviceCaller().execute("predictionsbystop", "&stop=", stationId);
        } else {
            stationInfoTextView.setText("No network connection available");
            stationInfoTextView.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT);
        }
    }

//    public void scheduleSendLocation() {
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                sendLocation();          // this method will contain your almost-finished HTTP calls
//                handler.postDelayed(this, FIVE_SECONDS);
//            }
//        }, FIVE_SECONDS);
//    }
    /**
     * Callback method defined by the View
     *
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
                url = new URL(baseURL + params[0] + "?api_key=" + mbtaKey + params[1] + params[2] + "&format=json");
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
                Log.v("Error API call", e.getMessage());
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
         *
         * @param result Json data
         */
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                parseJSONObject(result);
            } else {
                Log.i(DEBUG_TAG, "returned bitmap is null");
            }
        }
    }

    /**
     * Takes in Json data and parse it to usable information for this app purpose
     *
     * @param jsonObject
     */
    public void parseJSONObject(JSONObject jsonObject) {
        try {
            long time;
            String routeId = "";
            int color = 0;
            StopInformation si;
            FragmentManager fm = getSupportFragmentManager();
            TextView stationInfo = (TextView) findViewById(R.id.station_info);
            StationInfoFragment stationFrag;

            JSONArray modeArray = jsonObject.getJSONArray("mode");
            int count = 0;
            for (int modeCounter = 0; modeCounter < modeArray.length(); modeCounter++) {
                String mode = jsonObject.getJSONArray("mode").getJSONObject(modeCounter).getString("mode_name").toString();
                if (mode.equalsIgnoreCase("subway")) {
                    JSONArray jsonRoute = jsonObject.getJSONArray("mode").getJSONObject(modeCounter).getJSONArray("route");
                    for (int routeCounter = 0; routeCounter < jsonRoute.length(); routeCounter++) {
                        routeId = jsonRoute.getJSONObject(routeCounter).getString("route_id");
                        JSONArray jsonDirection = jsonRoute.getJSONObject(routeCounter).getJSONArray("direction");
                        for (int directionCounter = 0; directionCounter < jsonDirection.length(); directionCounter++) {
                            JSONArray jsonTrip = jsonDirection.getJSONObject(directionCounter).getJSONArray("trip");
                            for (int tripCounter = 0; tripCounter < jsonTrip.length(); tripCounter++) {
                                if (!StopInformation.checkDuplicateDestination(stopInformationList,
                                        jsonTrip.getJSONObject(tripCounter).getString("trip_headsign"))) {
                                    if (routeId.toLowerCase().contains("green"))
                                        color = ContextCompat.getColor(getApplicationContext(), R.color.green);
                                    else if (routeId.toLowerCase().contains("red"))
                                        color = ContextCompat.getColor(getApplicationContext(), R.color.red);
                                    else if (routeId.toLowerCase().contains("orange"))
                                        color = ContextCompat.getColor(getApplicationContext(), R.color.orange);
                                    else if (routeId.toLowerCase().contains("blue"))
                                        color = ContextCompat.getColor(getApplicationContext(), R.color.blue);

                                    si = new StopInformation(jsonTrip.getJSONObject(tripCounter).getString("trip_headsign"),
                                            jsonTrip.getJSONObject(tripCounter).getInt("pre_away"),
                                            color,
                                            false);
                                    double curLat = 0, curLon = 0;
                                    try {
                                        JSONObject vehicleArray = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle");
                                        curLat = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle").getDouble("vehicle_lat");
                                        curLon = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle").getDouble("vehicle_lon");
                                    }catch (Exception e){
                                        //continue;
                                    }

                                    stopInformationList.add(si);
                                    stationFrag = new StationInfoFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("destination", si.getDestination());
                                    bundle.putInt("timeAway", si.getTimeAway());
                                    bundle.putDouble("distanceAway", curLat != 0?distance(curLat, curLon, desLat, desLon):0);
                                    bundle.putInt("remStop", 2);
                                    bundle.putInt("color", si.getColor());
                                    bundle.putBoolean("favorite", si.isFavorite());

                                    //set Fragmentclass Arguments
                                    stationFrag.setArguments(bundle);
                                    if (count == 0)
                                        fm.beginTransaction().add(R.id.station_fragment_one, stationFrag, "Station_one")
                                                .commit();
                                    else if (count == 1)
                                        fm.beginTransaction().add(R.id.station_fragment_two, stationFrag, "Station_two")
                                                .commit();
                                    else if (count == 2)
                                        fm.beginTransaction().add(R.id.station_fragment_three, stationFrag, "Station_three")
                                                .commit();
                                    else if (count == 3)
                                        fm.beginTransaction().add(R.id.station_fragment_four, stationFrag, "Station_four")
                                                .commit();
                                    else if (count == 4)
                                        fm.beginTransaction().add(R.id.station_fragment_five, stationFrag, "Station_five")
                                                .commit();
                                    else if (count == 5)
                                        fm.beginTransaction().add(R.id.station_fragment_six, stationFrag, "Station_six")
                                                .commit();
                                    else if (count == 6)
                                        fm.beginTransaction().add(R.id.station_fragment_seven, stationFrag, "Station_seven")
                                                .commit();
                                    else if (count == 7)
                                        fm.beginTransaction().add(R.id.station_fragment_eight, stationFrag, "Station_eight")
                                                .commit();
                                    count++;
                                }
                            }
                        }
                    }
                    //setFragments();
                } else {
                    stationInfoTextView.setText("One/No Subway schedule available. See alerts!");
                    stationInfoTextView.setVisibility(View.VISIBLE);
                }
            }
            jsonString = jsonObject.getJSONArray("alert_headers").toString();
            Log.v("test", "test");
        }catch(Exception e){
            Log.v("Error JSON parse", e.getMessage());
        }
    }

    private void setFragments(){
        FragmentManager fm = getSupportFragmentManager();
        TextView stationInfo = (TextView)findViewById(R.id.station_info);
        StationInfoFragment stationFrag;
        //String[] stationId = marker.getTitle().split(",");
        if (stopInformationList.size() > 0) {
            //stationInfo.setVisibility(View.GONE);
            int count = 0;
            for (StopInformation si: stopInformationList) {
                stationFrag = new StationInfoFragment();
                Bundle bundle = new Bundle();
                bundle.putString("destination", si.getDestination());
                bundle.putInt("timeAway", si.getTimeAway());
                bundle.putInt("distanceAway", 200);
                bundle.putInt("remStop", 2);
                bundle.putInt("color", si.getColor());
                bundle.putBoolean("favorite", si.isFavorite());
                stationFrag.setArguments(bundle);

                if(count == 0)
                    fm.beginTransaction().add(R.id.station_fragment_one, stationFrag, "Station").commit();
                else if(count == 1)
                    fm.beginTransaction().add(R.id.station_fragment_two, stationFrag, "Station").commit();
                else if(count == 2)
                    fm.beginTransaction().add(R.id.station_fragment_three, stationFrag, "Station").commit();
                else if(count == 3)
                    fm.beginTransaction().add(R.id.station_fragment_four, stationFrag, "Station").commit();
                else if(count == 4)
                    fm.beginTransaction().add(R.id.station_fragment_five, stationFrag, "Station").commit();
                else if(count == 5)
                    fm.beginTransaction().add(R.id.station_fragment_six, stationFrag, "Station").commit();
                else if(count == 6)
                    fm.beginTransaction().add(R.id.station_fragment_seven, stationFrag, "Station").commit();
                else if(count == 7)
                    fm.beginTransaction().add(R.id.station_fragment_eight, stationFrag, "Station").commit();
                count++;
            }
        }
        else {
            //stationName.setText(stationId[1]);
            stationInfo.setText("No information is found");
            stationInfo.setVisibility(View.VISIBLE);
        }
    }

    public void openAlert(View view){
        Intent dialogIntent = new Intent(this, AlertActivity.class);
        dialogIntent.putExtra("alert_json", jsonString);
        startActivity(dialogIntent);
    }

    /*public void favoriteAction(View v){
        StationInfoFragment frg = new StationInfoFragment();
        frg.favoriteAction(v);
    }*/

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                      + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                        * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515/1.6;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
