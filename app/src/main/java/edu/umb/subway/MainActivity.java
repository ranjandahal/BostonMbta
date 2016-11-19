package edu.umb.subway;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

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


public class MainActivity extends FragmentActivity implements OnMapReadyCallback{//, GoogleMap.InfoWindowAdapter{
    public String BASE_URL;
    public String MBTA_KEY;
    private static float currentZoom;
    private static LatLng currentLocation;
    private static boolean configChanged;

    //private JSONObject jsonObject = null;
    private static List<Stations> stationsList;
    public static List<Marker> markerList;

	private GoogleMap mMap;
    //private int mtype=0;
    //private GroundOverlayOptions overlayOptions;
    private Polyline polylineBlue, polylineGreenB,polylineGreenC,polylineGreenD,
                     polylineGreenE, polylineRedA, polylineRedB, polylineOrange;

    private StationMarker stationMarker;
    private Button rt, lb, rb;
    private ImageView lt;
    //Database
    protected DBHandlerMbta myDBHelper;
    private AutoCompleteTextView autoCompleteTextView;
    private List<StopInfomation> stopInformationList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Restore saved values
        if (savedInstanceState != null) {
            currentLocation = new LatLng(savedInstanceState.getDouble("lat"),savedInstanceState.getDouble("lng")) ;
            currentZoom = savedInstanceState.getFloat("zoom");
            configChanged = true;
        }
        else {
            currentLocation = new LatLng(0,0);
            currentZoom = Properties.MIN_ZOOM;
            configChanged = false;
        }
        BASE_URL = getApplicationContext().getResources().getString(R.string.mbta_base_url);
        MBTA_KEY = getApplicationContext().getResources().getString(R.string.mbta_key);
        stopInformationList = new ArrayList<StopInfomation>();
        stationMarker = new StationMarker(ContextCompat.getColor(this,R.color.blue),ContextCompat.getColor(this,R.color.red),
                                          ContextCompat.getColor(this,R.color.orange), ContextCompat.getColor(this,R.color.green),
                                            getApplicationContext());
        myDBHelper = new DBHandlerMbta(getApplicationContext());
        stationsList = myDBHelper.getAllStation("");

        MapFragment mFragment=((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mFragment.getMapAsync(this);

        lt = (ImageView)findViewById(R.id.lt);
        rt = (Button)findViewById(R.id.rt);
        lb = (Button)findViewById(R.id.lb);
        rb = (Button)findViewById(R.id.rb);
        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.autocomplete);

        ArrayAdapter<Object> autocompleteAdapter = new ArrayAdapter<Object>(getApplicationContext(),
                                                    android.R.layout.simple_dropdown_item_1line,
                                                    //R.layout.autocomplete_layout,
                                                    myDBHelper.getStationsArray(stationsList));
        autoCompleteTextView.setAdapter(autocompleteAdapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String stationName = autoCompleteTextView.getText().toString();

                if (view != null) {
                    hideSoftKeyboard();
                    autoCompleteTextView.clearFocus();
                }
                for (Marker mk: markerList){
                    if(mk.getTitle().split(",")[1].equalsIgnoreCase(stationName)){
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mk.getPosition(), Properties.SEARCH_ZOOM));
                        viewStationDetail(mk);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    protected void showSearch(View view){
        view.animate().alpha(1.0f)
                .setDuration(400);
        if(autoCompleteTextView.getVisibility() == View.GONE) {
            autoCompleteTextView.setVisibility(View.VISIBLE);
            showSoftKeyboard(view);
        }
        else {
            autoCompleteTextView.setVisibility(View.GONE);
            hideSoftKeyboard();
        }
    }
    /**
     * Save data when orientation changes to restore later.
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putDouble("lat", mMap.getCameraPosition().target.latitude);
        outState.putDouble("lng", mMap.getCameraPosition().target.longitude);
        outState.putFloat("zoom", mMap.getCameraPosition().zoom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.mMap = map;
        mMap.setMyLocationEnabled(true);
        if(configChanged)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, currentZoom));
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Properties.CENTER, Properties.MIN_ZOOM));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for (Marker mk: markerList){
                    if(mk.getPosition().latitude != latLng.latitude && mk.getPosition().longitude != latLng.longitude){
                        if(rt.getVisibility() == View.GONE){
                            lt.setVisibility(View.VISIBLE);
                            rt.setVisibility(View.VISIBLE);
                            rb.setVisibility(View.VISIBLE);
                            lb.setVisibility(View.VISIBLE);
                        }
                        else{
                            lt.setVisibility(View.GONE);
                            rt.setVisibility(View.GONE);
                            rb.setVisibility(View.GONE);
                            lb.setVisibility(View.GONE);
                        }
                        break;
                    }
                }
            }
        });
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.empty_marker, null);

        markerList = stationMarker.addMarkers(mMap, stationsList, drawable);
        polylineBlue = mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.blue), "blue", ""));

        polylineRedA = mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.red), "red", "A"));
        polylineRedB = mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.red), "red", "B"));

        polylineGreenB = mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.green), "green", "B"));
        polylineGreenC = mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.green), "green", "C"));
        polylineGreenD = mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.green), "green", "D"));
        polylineGreenE = mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.green), "green", "E"));

        polylineOrange = mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.orange), "orange", ""));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom > Properties.MAX_ZOOM) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(Properties.MAX_ZOOM));
                } else if (cameraPosition.zoom < Properties.MIN_ZOOM) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Properties.CENTER, Properties.MIN_ZOOM));
                }
                for (Marker mk: markerList){
                    if(cameraPosition.zoom >= Float.parseFloat(mk.getTitle().split(",")[2])){
                        mk.setVisible(true);
                    }
                    else
                        mk.setVisible(false);
                }

                /*LatLng tempCenter = mMap.getCameraPosition().target;;
                LatLngBounds visibleBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                if(!MAPBOUNDARY.contains(visibleBounds.northeast) || !MAPBOUNDARY.contains(visibleBounds.southwest)){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(lastCenter));
                }
                else
                    lastCenter = tempCenter;*/
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                viewStationDetail(marker);
                return true;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });

        Log.v("Lang-left", mMap.getProjection().getVisibleRegion().latLngBounds.toString());
    }

    public void viewStationDetail(Marker marker){
        marker.hideInfoWindow();

        String[] stationId = marker.getTitle().split(",");
        stopInformationList.clear();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new WebserviceCaller().execute(BASE_URL + "predictionsbystop?api_key=" + MBTA_KEY + "&stop=" + stationId[0] + "&format=json");
        }else {
            Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT);
        }
        //Intent dialogIntent = new Intent(this, DialogActivity.class);
//
//        dialogIntent.putExtra("station_id", stationId[0]);
//        dialogIntent.putExtra("station_name", stationId[1]);
//        startActivity(dialogIntent);
    }

    /*@Override
    public View getInfoWindow(Marker marker) {
        return null;
        //return prepareInfoView(marker);
    }

    @Override
    public View getInfoContents(Marker marker) {
        //return null;
        return prepareInfoView(marker);

    }*/

    private void setFragments(Marker marker){
        LinearLayout infoView =  (LinearLayout)findViewById(R.id.infowindow_layout); // new LinearLayout(MainActivity.this);
//        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        infoView.setOrientation(LinearLayout.HORIZONTAL);
//        infoView.setLayoutParams(infoViewParams);
        FrameLayout fmLayout = (FrameLayout)findViewById(R.id.info_fragment);
        FragmentManager fm = getSupportFragmentManager();
        TextView stationName = (TextView)findViewById(R.id.station_name);
        TextView stationInfo = (TextView)findViewById(R.id.station_info);
        StationInfoFragment stationFrag;
        String[] stationId = marker.getTitle().split(",");
        if (stopInformationList.size() > 0) {
            stationInfo.setVisibility(View.GONE);
            for (StopInfomation si: stopInformationList) {
                //FrameLayout frameLayout = new FrameLayout(MainActivity.this);
                //frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                //        ViewGroup.LayoutParams.WRAP_CONTENT));
                stationFrag = new StationInfoFragment();
                fm.beginTransaction()
                        .add(R.id.info_fragment, stationFrag, "Station");

                TextView destination = (TextView) findViewById(R.id.destination);
                final TextView timer = (TextView) findViewById(R.id.timer);
                TextView distanceAway = (TextView)findViewById(R.id.distance_away);
                TextView remaininStop = (TextView)findViewById(R.id.remaining_stop);
                ImageView favImage = (ImageView)findViewById(R.id.fav_image);
                ImageView infoImage = (ImageView)findViewById(R.id.info_image);

                timer.setText("");
                destination.setText(si.getDestination());
                favImage.setImageResource(R.drawable.fav_on); //getResources().getDrawable(R.drawable.fav_off));
                infoImage.setImageResource(R.drawable.fav_off);
                new CountDownTimer(si.getTimeAway()*1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timer.setText(String.format("%02d", millisUntilFinished/(60*1000)) + ":"
                                + String.format("%02d", (millisUntilFinished%(60*1000))/1000));
                    }
                    public void onFinish() {
                        timer.setText("Arrived!");
                    }
                }.start();
            }
            fm.beginTransaction().commit();
        }
        else {
            stationName.setText(stationId[1]);
            stationInfo.setText("No information is found");
            stationInfo.setVisibility(View.VISIBLE);
        }

        /*ImageView infoImageView = new ImageView(MainActivity.this);
        //Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        Drawable drawable = getResources().getDrawable(android.R.drawable.ic_dialog_map);
        infoImageView.setImageDrawable(drawable);
        infoView.addView(infoImageView);

        LinearLayout subInfoView = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subInfoView.setOrientation(LinearLayout.VERTICAL);
        subInfoView.setLayoutParams(subInfoViewParams);

        TextView subInfoLat = new TextView(MainActivity.this);
        subInfoLat.setText("Lat: " + marker.getPosition().latitude);
        TextView subInfoLnt = new TextView(MainActivity.this);
        subInfoLnt.setText("Lnt: " + marker.getPosition().longitude);
        subInfoView.addView(subInfoLat);
        subInfoView.addView(subInfoLnt);
        infoView.addView(subInfoView);*/

        //return infoView;
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
            String temp, response = "";
            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                inStream = urlConnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                ;
                while ((temp = bReader.readLine()) != null) {
                    response += temp;
                }
                return (JSONObject) new JSONTokener(response).nextValue();
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
            return null;
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
            long time;
            String mode = jsonObject.getJSONArray("mode").getJSONObject(0).getString("mode_name").toString();
            if (mode.equalsIgnoreCase("subway")) {
                JSONArray jsonRoute = jsonObject.getJSONArray("mode").getJSONObject(0).getJSONArray("route");
                for (int routeCounter = 0; routeCounter < jsonRoute.length(); routeCounter++) {
                    JSONArray jsonDirection = jsonRoute.getJSONObject(routeCounter).getJSONArray("direction");
                    for (int directionCounter = 0; directionCounter < jsonDirection.length(); directionCounter++) {
                        JSONArray jsonTrip = jsonDirection.getJSONObject(directionCounter).getJSONArray("trip");
                        for (int tripCounter = 0; tripCounter < jsonTrip.length(); tripCounter++) {
                            if(StopInfomation.checkDuplicateDestination(stopInformationList,
                                        jsonTrip.getJSONObject(tripCounter).getString("trip_headsign"))){
                                stopInformationList.add(
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
        } catch (Exception e) {
            Log.v("Error JSON parse", e.getMessage());
        }
    }
    /*public void b1(View v){

    	if (mMap!=null){
            if (mtype==0) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mtype = 1;
            }else if(mtype==1){
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                mtype=2;
            }else{
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mtype=0;
            }
        }
    }

    public void b2(View v){
        CameraUpdate umb= CameraUpdateFactory.newLatLng(new LatLng(42.313895, -71.038833));
        Log.v("Lang-left", mMap.getProjection().getVisibleRegion().latLngBounds.toString());
        if (mMap!=null){
            mMap.moveCamera(umb);
        }
    }

    public void b3(View v){
        float zoom=(float)Math.random()*10+5;
        Toast.makeText(getApplicationContext(),"Zoom to "+Float.toString(zoom),Toast.LENGTH_SHORT).show();
        CameraUpdate mzoom= CameraUpdateFactory.zoomTo(zoom);
        if (mMap!=null){
            mMap.animateCamera(mzoom);
            Log.v("Lang-left", mMap.getProjection().getVisibleRegion().latLngBounds.toString());
            Log.v("Lang-left", mMap.getProjection().getVisibleRegion().latLngBounds.toString());
            //V/Lang-left: LatLngBounds{southwest=lat/lng: (34.20517867359235,-78.68875980377199),
            // northeast=lat/lng: (49.570917583486406,-63.41445654630661)}
        }
    }

    public void b4(View v){

        if (mMap!=null){
            mMap.addMarker(new MarkerOptions()
                    .title("UMass Boston")
                    .position(new LatLng(42.313895, -71.038833)));
        }
    }
    */
}
