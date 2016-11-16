package edu.umb.subway;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ZoomLevels{
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
            currentZoom = MIN_ZOOM;
            configChanged = false;
        }
        stationMarker = new StationMarker(ContextCompat.getColor(this,R.color.blue),ContextCompat.getColor(this,R.color.red),
                                          ContextCompat.getColor(this,R.color.orange), ContextCompat.getColor(this,R.color.green));
        myDBHelper = new DBHandlerMbta(getApplicationContext());
        stationsList = myDBHelper.getAllStation("");

        MapFragment mFragment=((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mFragment.getMapAsync(this);

        lt = (ImageView)findViewById(R.id.lt);
        rt = (Button)findViewById(R.id.rt);
        lb = (Button)findViewById(R.id.lb);
        rb = (Button)findViewById(R.id.rb);
        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.autocomplete);

        ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                                    //android.R.layout.simple_dropdown_item_1line,
                                                    R.layout.autocomplete_layout,
                                                    myDBHelper.getStationsArray(stationsList));
        autoCompleteTextView.setAdapter(autocompleteAdapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String stationName = autoCompleteTextView.getText().toString();

                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    autoCompleteTextView.clearFocus();
                }
                for (Marker mk: markerList){
                    if(mk.getTitle().split(",")[1].equalsIgnoreCase(stationName)){
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mk.getPosition(), SEARCH_ZOOM));
                        viewStationDetail(mk);
                        break;
                    }
                }
            }
        });
    }

    protected void showSearch(View view){
        /*view.animate()
                .translationY(view.getHeight())
                .alpha(1.0f)
                .setDuration(300);*/
        if(autoCompleteTextView.getVisibility() == View.GONE)
            autoCompleteTextView.setVisibility(View.VISIBLE);
        else
            autoCompleteTextView.setVisibility(View.GONE);
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, MIN_ZOOM));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for (Marker mk: markerList){
                    if(mk.getPosition().latitude != latLng.latitude && mk.getPosition().longitude != latLng.longitude){
                        if(lt.getVisibility() == View.GONE){
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
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.markers, null);

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
                if (cameraPosition.zoom > MAX_ZOOM) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(MAX_ZOOM));
                } else if (cameraPosition.zoom < MIN_ZOOM) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, MIN_ZOOM));
                }
                for (Marker mk: markerList){
                    //if(cameraPosition.zoom == )
                }
                if (cameraPosition.zoom >= MIN_ZOOM && cameraPosition.zoom <= LEVEL_ONE_ZOOM) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, MIN_ZOOM));
                }
                else if (cameraPosition.zoom < MIN_ZOOM) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, MIN_ZOOM));
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
        Intent dialogIntent = new Intent(this, DialogActivity.class);

        dialogIntent.putExtra("station_id", stationId[0]);
        dialogIntent.putExtra("station_name", stationId[1]);
        startActivity(dialogIntent);
    }
    /*public void display(View view) {
        final TextView msgTextView = (TextView) findViewById(R.id.textView);
        EditText msgTextField = (EditText) findViewById(R.id.editText);

        msgTextView.setText(msgTextField.getText());
        msgTextField.setText(null);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("api_key", "QCKjXYs2HEOBVVdfx_CmFg");
        params.put("stop","place-sbbsta");
        params.put("format","json");

        StringBuilder urlString = new StringBuilder();
        urlString.append("http://realtime.mbta.com/developer/api/v2/predictionsbystop");

        urlString.append("?api_key=").append("QCKjXYs2HEOBVVdfx_CmFg");
        urlString.append("&stop=").append("place-sstat");
        urlString.append("&format=").append("json");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, urlString.toString(), new JSONObject(params), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response){
                        msgTextView.setText("Response: " + response.toString());
                        Log.v("Response", response.toString().substring(0,10));
                        Toast.makeText(getApplicationContext(), response.toString().substring(0,10),Toast.LENGTH_LONG);
                    }
                },  new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        msgTextView.setText("Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error",Toast.LENGTH_LONG);
                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
        //Toast.makeText(getApplicationContext(), jsObjRequest.getBody().toString(),Toast.LENGTH_LONG);
    }*/

    /**
     * Uses AsyncTask to create a task away from the main UI thread. This task
     * takes json data url and download the content.
     */
    /*private class WebserviceCaller extends AsyncTask<String, Void, JSONObject> {
        public JSONObject doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url = null;
            InputStream inStream = null;
            try {
                url = new URL(params[0]);
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
*/
        /**
         * json data gets parsed to get temperature, log and lat values. A new AsyncTask
         * gets call inside this method to download image.
         * @param result Json data
         */
        /*protected void onPostExecute(JSONObject result) {
            if(result!=null) {
                parseJSONObject(result);
                //Make image download call once all the data gets parsed
            }
            else{
                Log.i(DEBUG_TAG, "returned bitmap is null");}
        }
    }*/

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
