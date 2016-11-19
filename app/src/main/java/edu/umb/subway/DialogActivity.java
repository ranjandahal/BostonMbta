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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
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

public class DialogActivity extends Activity {
    public final static String DEBUG_TAG = "edu.umb.cs443.MYMSG";
    private String stationName;
    private String stationId;
    private String baseURL;
    private String mbtaKey;
    private JSONObject jsonObject = null;
    private TextView stopName, destination, distanceAway, timer;
    private Chronometer timerChrono;
    private List<StopInfomation> stopInformation;  // = new ArrayList<StopInfomation>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        stationId = bundle.getString("station_id");
        stationName = bundle.getString("station_name");
        baseURL = getResources().getString(R.string.mbta_base_url);
        mbtaKey = getResources().getString(R.string.mbta_key);
        stopInformation = new ArrayList<StopInfomation>();
        stopName = (TextView) findViewById(R.id.stop_name);
        destination = (TextView) findViewById(R.id.destination);
        distanceAway = (TextView) findViewById(R.id.distance_away);
        timer = (TextView) findViewById(R.id.timer);
        timerChrono = (Chronometer) findViewById(R.id.timerChrono);

        stopName.setText(stationName);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new WebserviceCaller().execute("predictionsbystop", "&stop=", stationId);
        } else {
            stopName.setText("No network connection available");
            Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT);
        }
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
            String mode = jsonObject.getJSONArray("mode").getJSONObject(0).getString("mode_name").toString();
            if (mode.equalsIgnoreCase("subway")) {
                JSONArray jsonRoute = jsonObject.getJSONArray("mode").getJSONObject(0).getJSONArray("route");
                for (int routeCounter = 0; routeCounter < jsonRoute.length(); routeCounter++) {
                    JSONArray jsonDirection = jsonRoute.getJSONObject(routeCounter).getJSONArray("direction");
                    for (int directionCounter = 0; directionCounter < jsonDirection.length(); directionCounter++) {
                        JSONArray jsonTrip = jsonDirection.getJSONObject(directionCounter).getJSONArray("trip");
                        for (int tripCounter = 0; tripCounter < jsonTrip.length(); tripCounter++) {
                            if(tripCounter == 0 || (tripCounter > 0 &&
                                    !stopInformation.get(tripCounter-1).getDestination().equalsIgnoreCase(
                                                    jsonTrip.getJSONObject(tripCounter).getString("trip_headsign")))){
                                stopInformation.add(
                                        new StopInfomation(
                                                jsonTrip.getJSONObject(tripCounter).getString("trip_headsign"),
                                                jsonTrip.getJSONObject(tripCounter).getInt("pre_away"),
                                                1,
                                                false));
                            }
                        }
                    }
                }
            }
            if (stopInformation.size() > 0) {
                timer.setText("");
                destination.setText(stopInformation.get(0).getDestination());
                new CountDownTimer(stopInformation.get(0).getTimeAway()*1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timer.setText(String.format("%02d", millisUntilFinished/(60*1000)) + ":"
                                + String.format("%02d", (millisUntilFinished%(60*1000))/1000));
                    }
                    public void onFinish() {
                        timer.setText("Arrived!");
                    }
                }.start();
            }
            else{
                destination.setText("No information found");
            }
        } catch (Exception e) {
            Log.v("Error JSON parse", e.getMessage());
        }
    }

    private class TimerUpdate extends AsyncTask<String, Void, Integer> {
        public Integer doInBackground(String... params) {
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
            return new Integer(1);
            //return (jsonObject);
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

    /*private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            customHandler.postDelayed(this, 0);
        }
    };
    //**************HANDLER****************/
    /*public Handler threadHandler = new Handler() {
        public void handleMessage (android.os.Message message){
            countTextView.setText(count.toString());
        }
    };

    //*************RUNNABLE **************/
    /*private Runnable countNumbers = new Runnable () {
        private static final int DELAY = 1000;
        public void run() {
            try {
                while (true) {
                    count++;
                    Thread.sleep (DELAY);
                    threadHandler.sendEmptyMessage(0);
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };*/
}
