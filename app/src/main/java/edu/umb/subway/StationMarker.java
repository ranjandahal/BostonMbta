package edu.umb.subway;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Ranjan on 11/12/2016.
 */

public class StationMarker {
    public PolylineOptions polylineOptions;
    public StationMarker(){
        polylineOptions = new PolylineOptions()
                .add(new LatLng(42.349907596228419, -71.077975453851138), new LatLng(42.353021315373091, -71.064602140703201) ,
                     new LatLng(42.356203268361703, -71.062309835191911),new LatLng(42.359296597794511, -71.059458668220898))
                .color(Color.GREEN)
                .width(20)
                .visible(true)
                .zIndex(100);// Same longitude, and 16km to the south
                //.add(new LatLng(37.35, -122.0)); // Closes the polyline.

    }

    public PolylineOptions getPolylineOptions(){
        return polylineOptions;
    }
}

