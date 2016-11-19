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
    public static final float LEVEL_ONE_ZOOM = 12.2f;
    public static final float LEVEL_TWO_ZOOM = 13.1f;
    public static final float LEVEL_THREE_ZOOM = 14.1f;
    public static final float LEVEL_FOUR_ZOOM = 15.0f;
    public static final float MAX_ZOOM = 16.0f;
    public static final float SEARCH_ZOOM = 14.0f;

    public static final LatLng CENTER = new LatLng(42.326075, -71.084983);
    public static final LatLngBounds MAPBOUNDARY = new LatLngBounds(new LatLng(0,0),new LatLng(0,0));

    public final static String DEBUG_TAG = "edu.umb.cs443.project";
    public static final String IMG_EXTENSION = ".png";
}
