package edu.umb.subway;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.TextView;

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

import edu.umb.external.WrapContentHeightViewPager;

/**
 * Created by Ranjan on 11/24/2016.
 */
public class DialogPagerActivity extends FragmentActivity {
    public final static String DEBUG_TAG = "edu.umb.cs443.MYMSG";
    private String stationName;
    private String stationId;
    private String baseURL;
    private String mbtaKey;
    private int color;
    private Double desLat, desLon;
    private static String jsonString = "";
    private JSONObject jsonObject = null;
    private TextView stationNameTextView, stationInfoTextView;
    private List<StopInformation> stopInformationList, stopInformationList1;
    private static ArrayList<ArrayList<StopInformation>> stopInformationListList;
    private Thread webServiveThread;

    SchedulePagerAdapter schedulePagerAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pager);

        stationId = bundle.getString("station_id");
        stationName = bundle.getString("station_name");
        color = bundle.getInt("color");
        desLat = bundle.getDouble("lat");
        desLon = bundle.getDouble("lon");
        baseURL = getResources().getString(R.string.mbta_base_url);
        mbtaKey = getResources().getString(R.string.mbta_key);
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

    /**
     * Callback method defined by the View
     *
     * @param v
     */
    public void finishDialog(View v) {
        this.finish();
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
                parseJSONObjectPager(result);
                //parseJSONObject(result);
            } else {
                Log.i(DEBUG_TAG, "returned bitmap is null");
            }
        }
    }

    public void parseJSONObjectPager(JSONObject jsonObject) {
        try {
            String routeId = "";
            int color = 0;
            StopInformation si;
            FragmentManager fm = getSupportFragmentManager();
            StationInfoFragment stationFrag;
            //stopInformationList1.clear();
            JSONArray modeArray = jsonObject.getJSONArray("mode");
            int count = 0;
            stopInformationListList = new ArrayList<ArrayList<StopInformation>>();
            for (int modeCounter = 0; modeCounter < modeArray.length(); modeCounter++) {
                String mode = jsonObject.getJSONArray("mode").getJSONObject(modeCounter).getString("mode_name").toString();
                if (mode.equalsIgnoreCase("subway")) {
                    JSONArray jsonRoute = jsonObject.getJSONArray("mode").getJSONObject(modeCounter).getJSONArray("route");
                    for (int routeCounter = 0; routeCounter < jsonRoute.length(); routeCounter++) {
                        routeId = jsonRoute.getJSONObject(routeCounter).getString("route_id");
                        JSONArray jsonDirection = jsonRoute.getJSONObject(routeCounter).getJSONArray("direction");
                        for (int directionCounter = 0; directionCounter < jsonDirection.length(); directionCounter++) {
                            stopInformationList1 = new ArrayList<StopInformation>();
                            JSONArray jsonTrip = jsonDirection.getJSONObject(directionCounter).getJSONArray("trip");
                            for (int tripCounter = 0; tripCounter < jsonTrip.length(); tripCounter++) {
                                String addDesti = "";
                                if(jsonTrip.getJSONObject(tripCounter).getString("trip_headsign").contains("Alewife") &&
                                        jsonTrip.getJSONObject(tripCounter).getString("trip_name").contains("from Braintree")
                                        && stationName.contains("JFK"))
                                    addDesti = "(Braintree)";
                                else if(jsonTrip.getJSONObject(tripCounter).getString("trip_headsign").contains("Alewife") &&
                                        jsonTrip.getJSONObject(tripCounter).getString("trip_name").contains("from Ashmont")
                                        && stationName.contains("JFK"))
                                    addDesti = "(Ashmont)";

                                //if (!StopInformation.checkDuplicateDestination(stopInformationList,
                                //      jsonTrip.getJSONObject(tripCounter).getString("trip_headsign")+addDesti)) {
                                if (routeId.toLowerCase().contains("green"))
                                    color = ContextCompat.getColor(getApplicationContext(), R.color.green);
                                else if (routeId.toLowerCase().contains("red"))
                                    color = ContextCompat.getColor(getApplicationContext(), R.color.red);
                                else if (routeId.toLowerCase().contains("orange"))
                                    color = ContextCompat.getColor(getApplicationContext(), R.color.orange);
                                else if (routeId.toLowerCase().contains("blue"))
                                    color = ContextCompat.getColor(getApplicationContext(), R.color.blue);

                                double curLat = 0, curLon = 0;
                                try {
                                    JSONObject vehicleArray = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle");
                                    curLat = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle").getDouble("vehicle_lat");
                                    curLon = jsonTrip.getJSONObject(tripCounter).getJSONObject("vehicle").getDouble("vehicle_lon");
                                }catch (Exception e){
                                    //continue;
                                }

                                si = new StopInformation(stationId, stationName, jsonTrip.getJSONObject(tripCounter).getString("trip_headsign")+addDesti,
                                        jsonTrip.getJSONObject(tripCounter).getInt("pre_away"),
                                        color, false, curLat != 0?distance(curLat, curLon, desLat, desLon):0, 2);

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
                                stopInformationList1.add(si);
                            }
                            stopInformationListList.add((ArrayList)stopInformationList1);
                        }
                    }
                    stationInfoTextView.setVisibility(View.GONE);
                } else {
                    stationInfoTextView.setText("One/No Subway schedule available. See alerts!");
                    stationInfoTextView.setVisibility(View.VISIBLE);
                }
            }
            jsonString = jsonObject.getJSONArray("alert_headers").toString();
            schedulePagerAdapter = new SchedulePagerAdapter(getSupportFragmentManager());
            // Set up the ViewPager, attaching the adapter.
            viewPager = (WrapContentHeightViewPager) findViewById(R.id.pager);
            viewPager.setAdapter(schedulePagerAdapter);
        }catch(Exception e){
            Log.v("Error JSON parse", e.getMessage());
        }
    }

    public void openAlert(View view){
        Intent dialogIntent = new Intent(this, AlertActivity.class);
        dialogIntent.putExtra("alert_json", jsonString);
        startActivity(dialogIntent);
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

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class SchedulePagerAdapter extends FragmentStatePagerAdapter {
        FragmentManager fm;
        int count;

        ScheduleFragment scheduleFragment = new ScheduleFragment();

        public SchedulePagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
            this.count = scheduleFragment.getMinimumLength();
        }

        @Override
        public Fragment getItem(int i) {
            ScheduleFragment scheduleFragment = new ScheduleFragment();
            scheduleFragment.setFragmentManager(fm);
            count = scheduleFragment.getMinimumLength();

            Fragment fragment = scheduleFragment;
            Bundle args = new Bundle();
            args.putInt(ScheduleFragment.ARG_OBJECT, i + 1); // Our object is just an integer :-P
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Schedule " + (position + 1);
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class ScheduleFragment extends Fragment {

        public static final String ARG_OBJECT = "Schedule";
        private int index;
        FragmentManager fragmentManager;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.pager_layout, container, false);
            //Bundle args = getArguments();
            setFragments();
            return rootView;
        }

        public void setFragmentManager(FragmentManager fragmentManager){
            this.fragmentManager = fragmentManager;
        }

        public void setIndex(int index){
            this.index = index;
        }
        public void setFragments(){
            FragmentManager fm = fragmentManager;
            for(int count = 0; count < getMinimumLength(); count++) {
                for(int stopCounter = 0; stopCounter < stopInformationListList.size(); stopCounter++) {
                    StopInformation si = stopInformationListList.get(stopCounter).get(count);

                    StationInfoFragment stationFrag;
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
                    if (stopCounter == 0)
                        fm.beginTransaction().replace(R.id.station_fragment_one1, stationFrag, "Station_one")
                                .commit();
                    else if (stopCounter == 1)
                        fm.beginTransaction().replace(R.id.station_fragment_two2, stationFrag, "Station_two")
                                .commit();
                    else if (stopCounter == 2)
                        fm.beginTransaction().replace(R.id.station_fragment_three3, stationFrag, "Station_three")
                                .commit();
                    else if (stopCounter == 3)
                        fm.beginTransaction().replace(R.id.station_fragment_four4, stationFrag, "Station_four")
                                .commit();
                    else if (stopCounter == 4)
                        fm.beginTransaction().replace(R.id.station_fragment_five5, stationFrag, "Station_five")
                                .commit();
                    else if (stopCounter == 5)
                        fm.beginTransaction().replace(R.id.station_fragment_six6, stationFrag, "Station_six")
                                .commit();
                    else if (stopCounter == 6)
                        fm.beginTransaction().replace(R.id.station_fragment_seven7, stationFrag, "Station_seven")
                                .commit();
                    else if (stopCounter == 7)
                        fm.beginTransaction().replace(R.id.station_fragment_eight8, stationFrag, "Station_eight")
                                .commit();
                }
            }
        }

        public int getMinimumLength(){
            int min = 10;
            for(int count = 0; count < stopInformationListList.size(); count++) {
                if(stopInformationListList.get(count).size() < min)
                    min = stopInformationListList.get(count).size();
            }
            return min;
        }
    }

    }
