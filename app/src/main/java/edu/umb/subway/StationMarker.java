package edu.umb.subway;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ranjan on 11/12/2016.
 */

public class StationMarker {
    public static List<Marker> markerList;
    public static List<LatLng> latLng;
    public static PolylineOptions polylineOptions;

    public StationMarker(){
        polylineOptions = new PolylineOptions();
        markerList = new ArrayList<Marker>();
        latLng = new ArrayList<LatLng>();
//        polylineOptions = new PolylineOptions()
//                .add(new LatLng(42.349907596228419, -71.077975453851138), new LatLng(42.353021315373091, -71.064602140703201) ,
//                     new LatLng(42.356203268361703, -71.062309835191911),new LatLng(42.359296597794511, -71.059458668220898))
//                .color(Color.GREEN)
//                .width(20)
//                .visible(true)
//                .zIndex(100);// Same longitude, and 16km to the south
//                //.add(new LatLng(37.35, -122.0)); // Closes the polyline.

    }

    public PolylineOptions getPolylineOptions(){
        return polylineOptions;
    }

    public PolylineOptions addPolyLine(int color){
        return polylineOptions.addAll(latLng)
                        .color(color)
                        .width(20)
                        .visible(true)
                        .zIndex(100);
    }

    public void addMarkers(GoogleMap mMap, List<Stations> stationsList){
        Marker marker;
        LatLng ltlg;
        for(int count = 0; count < stationsList.size(); count++){
            //Log.v("Stop", stationsList.get(count).getName());
            ltlg = new LatLng(stationsList.get(count).getLat(), stationsList.get(count).getLng());
            latLng.add(ltlg);

            //if(st.getColor().equals("blue"))
            marker = mMap.addMarker(new MarkerOptions()
                    .position(ltlg)
                    .title(stationsList.get(0).getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue))
                    .anchor(0.5f, 0.5f));
            markerList.add(marker);
        }
//        for (Stations st:stationsList) {
//            Log.v("Stop", st.getName());
//            ltlg = new LatLng(st.getLat(), st.getLng());
//            latLng.add(ltlg);
//
//            //if(st.getColor().equals("blue"))
//                marker = mMap.addMarker(new MarkerOptions()
//                    .position(ltlg)
//                    .title(st.getName())
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.green)));
//            markerList.add(marker);
//        }
    }
}

