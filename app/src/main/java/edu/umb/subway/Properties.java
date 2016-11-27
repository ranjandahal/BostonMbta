package edu.umb.subway;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Ranjan on 11/16/2016.
 */

public abstract class Properties {
    public static float MIN_ZOOM = 11.0f;
    public static float LEVEL_ONE_ZOOM = 12.2f;
    public static float LEVEL_TWO_ZOOM = 13.1f;
    public static float LEVEL_THREE_ZOOM = 14.1f;
    public static float LEVEL_FOUR_ZOOM = 14.8f;
    public static float MAX_ZOOM = 18.0f;
    public static float SEARCH_ZOOM = 14.0f;

    public static final LatLng CENTER = new LatLng(42.326075, -71.084983);
    public static final LatLngBounds MAPBOUNDARY = new LatLngBounds(new LatLng(0,0),new LatLng(0,0));

    public static final String IMG_EXTENSION = ".png";
    public static final int REFRESH_TIME = 10000;

    public  static String BASE_URL = "";
    public  static String MBTA_KEY = "";
    public final static String DEBUG_TAG = "edu.umb.subway";
    public final static String INBOUND = " [I]";
    public final static String OUTBOUND = " [O]";
    public final static int REQUEST_CODE = 0;
}
