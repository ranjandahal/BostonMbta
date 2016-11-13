package edu.umb.subway;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback{
    public final static String DEBUG_TAG="edu.umb.cs443.MYMSG";
    public static final String URL_PIC = "http://openweathermap.org/img/w/";
    public static final String URL_JSON = "http://api.openweathermap.org/data/2.5/weather?q=";
    public static final String APPID = "&APPID=f9a0da7858696d1453d0faa23006c2d9";
    private static final LatLngBounds MAPBOUNDARY = new LatLngBounds(new LatLng(0,0),new LatLng(0,0));
    public static String mbta_key = "";
    public static final String IMG_EXTENSION = ".png";
    private JSONObject jsonObject = null;
    private List<Stations> stationsList;

	private GoogleMap mMap;
    private int mtype=0;
    private GroundOverlayOptions overlayOptions;
    private Polyline polyline;

    private StationMarker stationMarker;

    //Database
    protected DBHandlerMbta myDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stationMarker = new StationMarker();
        myDBHelper = new DBHandlerMbta(getApplicationContext());
        File path = getApplicationContext().getDatabasePath("db_mbta");
        stationsList = myDBHelper.getAllStation("blue");

        MapFragment mFragment=((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mFragment.getMapAsync(this);
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

    public void search(View v){
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

    @Override
    public void onMapReady(GoogleMap map) {
        this.mMap = map;

        stationMarker.addMarkers(mMap, stationsList);
        polyline = mMap.addPolyline(stationMarker.addPolyLine(ContextCompat.getColor(getApplicationContext(), R.color.blue)));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                float maxZoom = 16.0f;
                float minZoom = 12.0f;

                if (cameraPosition.zoom > maxZoom) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
                } else if (cameraPosition.zoom < minZoom) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(minZoom));
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
                return false;
            }
        });
        //mMap.setMinZoomPreference(6.0f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.354459, -71.064090), 14.0f));
        Log.v("Lang-left", mMap.getProjection().getVisibleRegion().latLngBounds.toString());
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
    private class WebserviceCaller extends AsyncTask<String, Void, JSONObject> {
        public JSONObject doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url = null;
            InputStream inStream = null;
            try {

                url = new URL(URL_JSON + params[0] + APPID);
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
