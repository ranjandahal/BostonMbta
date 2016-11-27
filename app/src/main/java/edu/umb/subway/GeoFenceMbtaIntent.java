package edu.umb.subway;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.RenderScript;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

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

import static android.app.Notification.PRIORITY_MAX;

/**
 * Created by Bikesh on 11/19/2016.
 */

public class GeoFenceMbtaIntent extends IntentService {
    protected static final String TAG = "GeoFenceMbtaIntentIS";
    private String stationId, stationName, destination, contentText;
    private int timeAway;
    private static int notificationId;
    private int color;
    private JSONObject jsonObject = null;
    private Thread webServiveThread;

    public GeoFenceMbtaIntent() {
        super(TAG);
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = FenceErrMsg.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
        {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            notificationId = 0;
            for (Geofence gf: triggeringGeofences) {
                String[] stationValues = gf.getRequestId().split(",");
                stationId = stationValues[0];
                stationName = stationValues[1];
                destination = stationValues[2];

                try {
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        webServiveThread = new Thread(webServiceRunnable);
                        webServiveThread.start();
                        Thread.sleep(5000);
                    } else {
                        contentText = "No network connection available";
                    }
                }catch (Exception e){
                    contentText = "Thread Error!!.";
                }

                String geofenceTransitionDetails = "Train From " + stationName + " to " + destination + " Timeline.";
                sendNotification(geofenceTransitionDetails, notificationId);
                notificationId++;
            }
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private Runnable webServiceRunnable = new Runnable () {
        public void run() {
            try {
                new WebserviceCaller().execute("predictionsbystop", "&stop=", stationId);
            } catch (Exception e){
                contentText = "Unknown error occurred during API call.";
            }
        }
    };
    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails, int notificationId) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        notificationIntent.putExtra("station_id", stationId);
        notificationIntent.putExtra("station_name", stationName);
        notificationIntent.putExtra("destination", destination);
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setPriority(PRIORITY_MAX);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(color)
                .setContentTitle(notificationDetails)
                .setContentText(contentText)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(notificationId, builder.build());
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
            timeAway = 0;
            String direction = "", desti = "";
            JSONArray modeArray = jsonObject.getJSONArray("mode");
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
                                        && stationName.equalsIgnoreCase("JFK"))
                                    addDestination = "(Braintree)";
                                else if(jsonTrip.getJSONObject(tripCounter).getString("trip_headsign").contains("Alewife") &&
                                        jsonTrip.getJSONObject(tripCounter).getString("trip_name").contains("from Ashmont")
                                        && stationName.equalsIgnoreCase("JFK"))
                                    addDestination = "(Ashmont)";
                                desti = jsonTrip.getJSONObject(tripCounter).getString("trip_headsign")+ addDestination + direction;
                                if (destination.equalsIgnoreCase(desti)){
                                    timeAway = jsonTrip.getJSONObject(tripCounter).getInt("pre_away");
                                    contentText = String.format("%02d:%02d minutes away. %s", timeAway/(60), timeAway%60, getString(R.string.geofence_transition_notification_text));
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            if(timeAway == 0){
                contentText = "One/No Subway schedule available. See alerts!";
            }
        }catch(Exception e){
            contentText = "Unknown error occurred when parsing data.";
        }
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
