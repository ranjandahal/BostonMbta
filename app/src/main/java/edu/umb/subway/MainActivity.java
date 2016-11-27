package edu.umb.subway;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    ResultCallback<Status> {
    private static float currentZoom;
    private static LatLng currentLocation;
    private static boolean configChanged;
    private static List<Stations> stationsList;
    public static List<Marker> markerList;
	private GoogleMap mMap;
    private StationMarker stationMarker;
    private static Marker currentMarker;
    private Button rightTopNormal, rightTopHybrid, rightTopSatellite;
    private ImageView search, setting, favorite;
    //Database
    protected DBHandlerMbta myDBHelper;
    private AutoCompleteTextView autoCompleteTextView;
    private List<StopInformation> stopInformationList;

    /************Geofence starts*************************/
    final Handler myRunHandler = new Handler();
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private boolean mGeofencesAdded;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences mSharedPreferences;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private List<FavoriteStations> favoriteStationsList;
    /************Geofence ends*************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setupPermission();

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

        MapFragment mFragment=(MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mFragment.getMapAsync(this);
        initializeViews();

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String stationName = autoCompleteTextView.getText().toString();
                currentMarker.remove();
                if (view != null) {
                    hideSoftKeyboard();
                    autoCompleteTextView.clearFocus();
                }
                stationClickAction(markerList, stationName);
            }
        });
    }

    protected void stationClickAction(List<Marker> markerList, String stationName){
        for (Marker marker: markerList){
            if(marker.getTitle().split(",")[1].equalsIgnoreCase(stationName)){
                //adjustZoomAndAnimateMap(mk);
                setCurrentMarker(marker);
                viewStationDetail(marker);
                autoCompleteTextView.setText("");
                break;
            }
        }
    }

    protected void initializeViews(){
        myDBHelper = new DBHandlerMbta(getApplicationContext());

        buildGoogleApiClient();
        if (populateGeofenceList()) {
            myRunHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    addGeofencesHandler();
                }
            }, 5000);
        }
        stopInformationList = new ArrayList<StopInformation>();
        stationMarker = new StationMarker(ContextCompat.getColor(this,R.color.blue),ContextCompat.getColor(this,R.color.red),
                ContextCompat.getColor(this,R.color.orange), ContextCompat.getColor(this,R.color.green));
        stationsList = myDBHelper.getAllStation("");

        search = (ImageView)findViewById(R.id.search);
        rightTopNormal = (Button)findViewById(R.id.normal);
        rightTopHybrid = (Button)findViewById(R.id.hybrid);
        rightTopSatellite = (Button)findViewById(R.id.satellite);
        favorite = (ImageView)findViewById(R.id.favorite);
        setting = (ImageView) findViewById(R.id.setting);

        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.autocomplete);

        ArrayAdapter<Object> autocompleteAdapter = new ArrayAdapter<Object>(getApplicationContext(),
                R.layout.autocomplete_layout,
                myDBHelper.getStationsArray(stationsList));
        autoCompleteTextView.setAdapter(autocompleteAdapter);
    }

    protected void setupPermission(){
        int hasLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }
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
        if(autoCompleteTextView.getVisibility() == View.GONE) {
            autoCompleteTextView.setVisibility(View.VISIBLE);
            showSoftKeyboard(view);
        }
        else {
            autoCompleteTextView.setVisibility(View.GONE);
            hideSoftKeyboard();
        }
        view.animate().alpha(1.0f)
                .setDuration(800);
    }

    protected void showSettings(View view){
        Intent settingIntent = new Intent(this, SettingActivity.class);
        /*dialogIntent.putExtra("station_id", stationId[0]);
        dialogIntent.putExtra("station_name", stationId[1]);
        dialogIntent.putExtra("color", getColor(stationId[3]));
        dialogIntent.putExtra("lat", marker.getPosition().latitude);
        dialogIntent.putExtra("lon", marker.getPosition().longitude);*/
        startActivity(settingIntent);
    }

    protected void showFavorite(View view){
        Intent favoriteIntent = new Intent(this, FavoriteActivity.class);
        startActivity(favoriteIntent);
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
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        //Dummy current marker
        currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)));

        if(configChanged)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, currentZoom));
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Properties.CENTER, Properties.MIN_ZOOM));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(rightTopNormal.getVisibility() == View.GONE){
                    search.setVisibility(View.VISIBLE);
                    rightTopNormal.setVisibility(View.VISIBLE);
                    rightTopHybrid.setVisibility(View.VISIBLE);
                    rightTopSatellite.setVisibility(View.VISIBLE);
                    setting.setVisibility(View.VISIBLE);
                    favorite.setVisibility(View.VISIBLE);
                }
                else{
                    search.setVisibility(View.GONE);
                    rightTopNormal.setVisibility(View.GONE);
                    rightTopHybrid.setVisibility(View.GONE);
                    rightTopSatellite.setVisibility(View.GONE);
                    setting.setVisibility(View.GONE);
                    favorite.setVisibility(View.GONE);
                }
            }
        });

        markerList = stationMarker.addMarkers(mMap, stationsList, ResourcesCompat.getDrawable(getResources(), R.drawable.empty_marker, null));
        mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.blue), "blue", ""));

        mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.red), "red", "A"));
        mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.red), "red", "B"));

        mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.green), "green", "B"));
        mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.green), "green", "C"));
        mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.green), "green", "D"));
        mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.green), "green", "E"));

        mMap.addPolyline(stationMarker.getPolyLine(ContextCompat.getColor(this,R.color.orange), "orange", ""));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom > Properties.MAX_ZOOM) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(Properties.MAX_ZOOM));
                } else if (cameraPosition.zoom < Properties.MIN_ZOOM) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Properties.CENTER, Properties.MIN_ZOOM));
                }
                for (Marker mk: markerList){
                    String[] split = mk.getTitle().split(",");
                    if(cameraPosition.zoom >= Float.parseFloat(mk.getTitle().split(",")[8])){
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
                currentMarker.remove();
                //adjustZoomAndAnimateMap(marker);
                setCurrentMarker(marker);
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

        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("station_name") != null){
            stationClickAction(markerList, bundle.getString("station_name"));
        }
    }

    public void setCurrentMarker(Marker marker){
        currentMarker = mMap.addMarker(new MarkerOptions().position(marker.getPosition())
                .anchor(0.5f, 1.0f).visible(true).title("")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.selected_station)));
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
        int result = 0;
        marker.hideInfoWindow();
        if(marker.getTitle().length() == 0)
            return;
        String[] stationId = marker.getTitle().split(",");
        stopInformationList.clear();
        Intent dialogIntent = new Intent(this, DialogActivity.class);
        dialogIntent.putExtra("station_id", stationId[0]);
        dialogIntent.putExtra("station_name", stationId[1]);
        dialogIntent.putExtra("color", getColor(stationId[6]));
        dialogIntent.putExtra("lat", marker.getPosition().latitude);
        dialogIntent.putExtra("lon", marker.getPosition().longitude);
        startActivityForResult(dialogIntent, result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Properties.REQUEST_CODE) {
            // Make sure the request was successful
            if (populateGeofenceList()) {
                addGeofencesHandler();
            }
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

    // Asks for permission
    /*private void askPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }*/
    /******************************************************************************************/
    //GEOFENCING CODES STARTS

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
//        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.connect();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mGoogleApiClient.disconnect();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesHandler() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesHandler() {
        int status;
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            List<String> geofenceRequestIdList = new ArrayList<>();
            Stations stations;
            List<FavoriteStations> nonFavoriteStationsList = new ArrayList<>();
            nonFavoriteStationsList = myDBHelper.getAllFavoriteStation("none", false, false);

            if(nonFavoriteStationsList.size() == 0)
                return;

            for (FavoriteStations st : nonFavoriteStationsList) {
                stations = myDBHelper.getStation(st.getStationID());
                geofenceRequestIdList.add(stations.getStationID() + "," + stations.getName() + "," + st.getDestination());
            }

            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    geofenceRequestIdList
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e("GEOFENCING", "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     * <p>
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();
        } else {
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeoFenceMbtaIntent.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public boolean populateGeofenceList() {
        removeGeofencesHandler();

        mGeofenceList = new ArrayList<Geofence>();
        mGeofencePendingIntent = null;
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);

        Stations stations;
        favoriteStationsList = new ArrayList<>();
        favoriteStationsList = myDBHelper.getAllFavoriteStation("none", false, true);

        if(favoriteStationsList.size() == 0)
            return false;

        for (FavoriteStations st : favoriteStationsList) {
            stations = myDBHelper.getStation(st.getStationID());
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(stations.getStationID() + "," + stations.getName() + "," + st.getDestination())
                    .setCircularRegion(
                            stations.getLat(),
                            stations.getLng(),
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }
        return true;
    }
    /**********************GEOFENCING CODES ENDS ************************************************/
}
