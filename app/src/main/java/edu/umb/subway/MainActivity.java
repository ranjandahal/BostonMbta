package edu.umb.subway;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
    private ImageView closeImage;

    //private JSONObject jsonObject = null;
    private static List<Stations> stationsList;
    public static List<Marker> markerList;

	private GoogleMap mMap;
    //private int mtype=0;
    //private GroundOverlayOptions overlayOptions;
    private Polyline polylineBlue, polylineGreenB,polylineGreenC,polylineGreenD,
                     polylineGreenE, polylineRedA, polylineRedB, polylineOrange;

    private StationMarker stationMarker;
    private Button rightTopNormal, rightTopHybrid, rightTopSatellite, lb, rb;
    private ImageView lt;
    //Database
    protected DBHandlerMbta myDBHelper;
    private AutoCompleteTextView autoCompleteTextView;
    private List<StopInformation> stopInformationList;
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
        closeImage = (ImageView)findViewById(R.id.close_image);
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FrameLayout)findViewById(R.id.info_fragment)).setVisibility(View.GONE);
            }
        });
        stopInformationList = new ArrayList<StopInformation>();
        stationMarker = new StationMarker(ContextCompat.getColor(this,R.color.blue),ContextCompat.getColor(this,R.color.red),
                                          ContextCompat.getColor(this,R.color.orange), ContextCompat.getColor(this,R.color.green));
        myDBHelper = new DBHandlerMbta(getApplicationContext());
        stationsList = myDBHelper.getAllStation("");

        MapFragment mFragment=((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mFragment.getMapAsync(this);

        lt = (ImageView)findViewById(R.id.lt);
        rightTopNormal = (Button)findViewById(R.id.normal);
        rightTopHybrid = (Button)findViewById(R.id.hybrid);
        rightTopSatellite = (Button)findViewById(R.id.satellite);
        lb = (Button)findViewById(R.id.lb);
        rb = (Button)findViewById(R.id.rb);
        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.autocomplete);

        ArrayAdapter<Object> autocompleteAdapter = new ArrayAdapter<Object>(getApplicationContext(),
                                                    R.layout.autocomplete_layout,
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
                        adjustZoomAndAnimateMap(mk);
                        viewStationDetail(mk);
                        autoCompleteTextView.setText("");
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
                        if(rightTopNormal.getVisibility() == View.GONE){
                            lt.setVisibility(View.VISIBLE);
                            rightTopNormal.setVisibility(View.VISIBLE);
                            rightTopHybrid.setVisibility(View.VISIBLE);
                            rightTopSatellite.setVisibility(View.VISIBLE);
                            rb.setVisibility(View.VISIBLE);
                            lb.setVisibility(View.VISIBLE);
                        }
                        else{
                            lt.setVisibility(View.GONE);
                            rightTopNormal.setVisibility(View.GONE);
                            rightTopHybrid.setVisibility(View.GONE);
                            rightTopSatellite.setVisibility(View.GONE);
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
                adjustZoomAndAnimateMap(marker);
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

    public void adjustZoomAndAnimateMap(Marker marker){
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        if(mMap.getCameraPosition().zoom < Properties.LEVEL_TWO_ZOOM)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), Properties.SEARCH_ZOOM));
        else
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
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

    public void viewStationDetail(Marker marker){
        marker.hideInfoWindow();

        String[] stationId = marker.getTitle().split(",");
        stopInformationList.clear();
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected()) {
//            new WebserviceCaller().execute(BASE_URL + "predictionsbystop?api_key=" + MBTA_KEY + "&stop=" + stationId[0] + "&format=json");
//        }else {
//            Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT);
//        }
        Intent dialogIntent = new Intent(this, DialogActivity.class);
        dialogIntent.putExtra("station_id", stationId[0]);
        dialogIntent.putExtra("station_name", stationId[1]);
        dialogIntent.putExtra("color", getColor(stationId[3]));
        dialogIntent.putExtra("lat", marker.getPosition().latitude);
        dialogIntent.putExtra("lon", marker.getPosition().longitude);
        startActivity(dialogIntent);
    }

    private void setFragments(){
        FrameLayout fmLayout = (FrameLayout)findViewById(R.id.info_fragment);
        FragmentManager fm = getSupportFragmentManager();
        TextView stationName = (TextView)findViewById(R.id.station_name);
        TextView stationInfo = (TextView)findViewById(R.id.station_info);
        StationInfoFragment stationFrag;
        //String[] stationId = marker.getTitle().split(",");
        if (stopInformationList.size() > 0) {
            stationInfo.setVisibility(View.GONE);
            for (StopInformation si: stopInformationList) {
                stationFrag = new StationInfoFragment();
                Bundle bundle = new Bundle();
                bundle.putString("destination", si.getDestination());
                bundle.putInt("timeAway", si.getTimeAway());
                bundle.putInt("color", si.getColor());
                bundle.putBoolean("favorite", si.isFavorite());
                //bundle.putString("destination", si.getDestination());
                //set Fragmentclass Arguments
                stationFrag.setArguments(bundle);
                fm.beginTransaction().add(R.id.station_fragment, stationFrag, "Station");
            }
            fm.beginTransaction().commit();
        }
        else {
            //stationName.setText(stationId[1]);
            stationInfo.setText("No information is found");
            stationInfo.setVisibility(View.VISIBLE);
        }
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int centerY=height/2;
        //fmLayout.setLeft(fmLayout.getHeight()-centerY);
        //fmLayout.setTop(fmLayout.getWidth()-size.x);
        fmLayout.setVisibility(View.VISIBLE);
        fmLayout.bringToFront();
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
            String routeId = "";
            int color = 0;
            String mode = jsonObject.getJSONArray("mode").getJSONObject(0).getString("mode_name").toString();
            if (mode.equalsIgnoreCase("subway")) {
                JSONArray jsonRoute = jsonObject.getJSONArray("mode").getJSONObject(0).getJSONArray("route");
                for (int routeCounter = 0; routeCounter < jsonRoute.length(); routeCounter++) {
                    routeId = jsonRoute.getJSONObject(routeCounter).getString("route_id");
                    JSONArray jsonDirection = jsonRoute.getJSONObject(routeCounter).getJSONArray("direction");
                    for (int directionCounter = 0; directionCounter < jsonDirection.length(); directionCounter++) {
                        JSONArray jsonTrip = jsonDirection.getJSONObject(directionCounter).getJSONArray("trip");
                        for (int tripCounter = 0; tripCounter < jsonTrip.length(); tripCounter++) {
                            if(!StopInformation.checkDuplicateDestination(stopInformationList,
                                        jsonTrip.getJSONObject(tripCounter).getString("trip_headsign"))){
                                if(routeId.toLowerCase().contains("green"))
                                    color = ContextCompat.getColor(getApplicationContext(), R.color.green);
                                else if(routeId.toLowerCase().contains("red"))
                                    color = ContextCompat.getColor(getApplicationContext(), R.color.red);
                                else if(routeId.toLowerCase().contains("orange"))
                                    color = ContextCompat.getColor(getApplicationContext(), R.color.orange);
                                else if(routeId.toLowerCase().contains("blue"))
                                    color = ContextCompat.getColor(getApplicationContext(), R.color.blue);
                                stopInformationList.add(
                                        new StopInformation(
                                                jsonTrip.getJSONObject(tripCounter).getString("trip_headsign"),
                                                jsonTrip.getJSONObject(tripCounter).getInt("pre_away"),
                                                color,
                                                false));
                            }
                        }
                    }
                }
            }
            setFragments();
        } catch (Exception e) {
            Log.v("Error JSON parse", e.getMessage());
        }
    }

    public void setMapType(View v){

    	if (mMap!=null){
            if (v.getId() == R.id.hybrid) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                setupButtonColor((Button)findViewById(R.id.hybrid),(Button)findViewById(R.id.normal), (Button)findViewById(R.id.satellite));
            }else if(v.getId() == R.id.satellite) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                setupButtonColor((Button)findViewById(R.id.satellite),(Button)findViewById(R.id.normal), (Button)findViewById(R.id.hybrid));
            }else{
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                setupButtonColor((Button)findViewById(R.id.normal),(Button)findViewById(R.id.hybrid), (Button)findViewById(R.id.satellite));
            }
        }
    }

    public void setupButtonColor(Button active, Button inactiveOne, Button inactiveTwo){
        active.setTextColor(ContextCompat.getColor(this,R.color.white));
        active.setBackgroundResource(R.color.blue);

        inactiveOne.setTextColor(ContextCompat.getColor(this,R.color.black));
        inactiveOne.setBackgroundResource(R.color.white);

        inactiveTwo.setTextColor(ContextCompat.getColor(this,R.color.black));
        inactiveTwo.setBackgroundResource(R.color.white);
    }
}
