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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class DialogActivity extends FragmentActivity {
    private String stationName;
    private String stationId;
    private int color;
    private Double desLat, desLon;
    private static String jsonString = "";
    private JSONObject jsonObject = null;
    private TextView stationNameTextView, stationInfoTextView;
    private List<StopInformation> stopInformationList;
    private Thread webServiveThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.infowindow_layout);

        stationId = bundle.getString("station_id");
        stationName = bundle.getString("station_name");
        color = bundle.getInt("color");
        desLat = bundle.getDouble("lat");
        desLon = bundle.getDouble("lon");
        stopInformationList = new ArrayList<StopInformation>();
        stationNameTextView = (TextView) findViewById(R.id.station_name);
        stationInfoTextView = (TextView) findViewById(R.id.station_info);

        stationNameTextView.setText(stationName);
        stationNameTextView.setBackgroundColor(color);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            webServiveThread = new Thread (webServiceRunnable);
            webServiveThread.start();
        } else {
            stationInfoTextView.setText("No network connection available");
            stationInfoTextView.setVisibility(View.VISIBLE);
        }
    }

    private Runnable webServiceRunnable = new Runnable () {
        public void run() {
            try {
                while (true) {
                    new WebserviceCaller().execute("predictionsbystop", "&stop=", stationId);
                    webServiveThread.sleep(Properties.REFRESH_TIME);
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        //Interrupt thread when dialog is stopped.
        try {
            webServiveThread.interrupt();
        }catch (Exception e){}
    }

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
    public class WebserviceCaller extends AsyncTask<String, Void, JSONObject> {
        public JSONObject doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url = null;
            InputStream inStream = null;
            try {
                url = new URL(Properties.BASE_URL + params[0] + "?api_key=" + Properties.MBTA_KEY + params[1] + params[2] + "&format=json");
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
                Log.i(Properties.DEBUG_TAG, "returned bitmap is null");
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
            String direction = "", destination = "";
            int color = 0;
            StopInformation si;
            FragmentManager fm = getSupportFragmentManager();
            StationInfoFragment stationFrag;
            stopInformationList.clear();
            JSONArray modeArray = jsonObject.getJSONArray("mode");
            int count = 0;
            for (int modeCounter = 0; modeCounter < modeArray.length(); modeCounter++) {
                String mode = jsonObject.getJSONArray("mode").getJSONObject(modeCounter).getString("mode_name").toString();
                if (mode.equalsIgnoreCase("subway")) {
                    JSONArray jsonRoute = jsonObject.getJSONArray("mode").getJSONObject(modeCounter).getJSONArray("route");
                    for (int routeCounter = 0; routeCounter < jsonRoute.length(); routeCounter++) {
                        color = getColor(jsonRoute.getJSONObject(routeCounter).getString("route_id").toLowerCase());
                        JSONArray jsonDirection = jsonRoute.getJSONObject(routeCounter).getJSONArray("direction");
                        for (int directionCounter = 0; directionCounter < jsonDirection.length(); directionCounter++) {
                            JSONArray jsonTrip = jsonDirection.getJSONObject(directionCounter).getJSONArray("trip");
                            direction = jsonDirection.getJSONObject(directionCounter).getInt("direction_id") == 0? Properties.OUTBOUND:Properties.INBOUND;
                            for (int tripCounter = 0; tripCounter < jsonTrip.length(); tripCounter++) {
                                String addDestination = "";
                                if(jsonTrip.getJSONObject(tripCounter).getString("trip_headsign").contains("Alewife") &&
                                        jsonTrip.getJSONObject(tripCounter).getString("trip_name").contains("from Braintree")
                                        && stationName.contains("JFK"))
                                    addDestination = "(Braintree)";
                                else if(jsonTrip.getJSONObject(tripCounter).getString("trip_headsign").contains("Alewife") &&
                                        jsonTrip.getJSONObject(tripCounter).getString("trip_name").contains("from Ashmont")
                                        && stationName.contains("JFK"))
                                    addDestination = "(Ashmont)";
                                destination = jsonTrip.getJSONObject(tripCounter).getString("trip_headsign")+ addDestination + direction;
                                if (!StopInformation.checkDuplicateDestination(stopInformationList, destination)) {
                                    double curLat = 0, curLon = 0;
                                    try {
                                        JSONObject vehicleArray = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle");
                                        curLat = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle").getDouble("vehicle_lat");
                                        curLon = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle").getDouble("vehicle_lon");
                                    }catch (Exception e){
                                        //continue;
                                    }

                                    si = new StopInformation(stationId, stationName, destination,
                                            jsonTrip.getJSONObject(tripCounter).getInt("pre_away"),
                                            color, false, curLat != 0?distance(curLat, curLon, desLat, desLon):0, 2);

                                    stopInformationList.add(si);
                                    stationFrag = new StationInfoFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("station_id", si.getStationId());
                                    bundle.putString("station_name", si.getStationName());
                                    bundle.putString("destination", si.getDestination());
                                    bundle.putInt("timeAway", si.getTimeAway());
                                    bundle.putDouble("distanceAway", si.getDistanceAway());
                                    bundle.putInt("remStop", si.getRemainingStop());
                                    bundle.putInt("color", si.getColor());
                                    bundle.putBoolean("favorite", si.isFavorite());

                                    //set Fragmentclass Arguments
                                    stationFrag.setArguments(bundle);
                                    if (count == 0)
                                        fm.beginTransaction().replace(R.id.station_fragment_one, stationFrag, "Station_one")
                                                .commit();
                                    else if (count == 1)
                                        fm.beginTransaction().replace(R.id.station_fragment_two, stationFrag, "Station_two")
                                                .commit();
                                    else if (count == 2)
                                        fm.beginTransaction().replace(R.id.station_fragment_three, stationFrag, "Station_three")
                                                .commit();
                                    else if (count == 3)
                                        fm.beginTransaction().replace(R.id.station_fragment_four, stationFrag, "Station_four")
                                                .commit();
                                    else if (count == 4)
                                        fm.beginTransaction().replace(R.id.station_fragment_five, stationFrag, "Station_five")
                                                .commit();
                                    else if (count == 5)
                                        fm.beginTransaction().replace(R.id.station_fragment_six, stationFrag, "Station_six")
                                                .commit();
                                    else if (count == 6)
                                        fm.beginTransaction().replace(R.id.station_fragment_seven, stationFrag, "Station_seven")
                                                .commit();
                                    else if (count == 7)
                                        fm.beginTransaction().replace(R.id.station_fragment_eight, stationFrag, "Station_eight")
                                                .commit();
                                    count++;
                                }
                            }
                        }
                    }
                }
                stationInfoTextView.setVisibility(View.GONE);
            }
            if(stopInformationList.size() == 0){
                stationInfoTextView.setText("One/No Subway schedule available. See alerts!");
                stationInfoTextView.setVisibility(View.VISIBLE);
            }
            jsonString = jsonObject.getJSONArray("alert_headers").toString();
        }catch(Exception e){
            Log.v("Error JSON parse", e.getMessage());
        }
    }

    public void openAlert(View view){
        Intent dialogIntent = new Intent(this, AlertActivity.class);
        dialogIntent.putExtra("alert_json", jsonString);
        startActivity(dialogIntent);
    }

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

    public int getColor(String color){
        if(color.contains("green"))
            return ContextCompat.getColor(getApplicationContext(), R.color.green);
        else if(color.contains("red"))
            return ContextCompat.getColor(getApplicationContext(), R.color.red);
        else if(color.contains("orange"))
            return ContextCompat.getColor(getApplicationContext(), R.color.orange);
        else if(color.contains("blue"))
            return ContextCompat.getColor(getApplicationContext(), R.color.blue);
        return 0;
    }
}
