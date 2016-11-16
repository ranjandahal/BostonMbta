package edu.umb.subway;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import edu.umb.external.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ranjan on 11/12/2016.
 */

public class StationMarker{
    public static List<Marker> markerList;
    public static List<LatLng> blueLatLng, greenLatLngB,
                                greenLatLngC,greenLatLngD, greenLatLngE,
                                redLatLngA, redLatLngB, orangeLatLng;
    public static PolylineOptions polylineOptions;
    int blue, red, orange, green;

    private TextDrawable textDrawable;
    private TextDrawable.Builder builder;

    public StationMarker(int blue, int red, int orange, int green){
        this.blue = blue;
        this.red = red;
        this.orange = orange;
        this.green = green;

        polylineOptions = new PolylineOptions();
        markerList = new ArrayList<Marker>();
        blueLatLng = new ArrayList<LatLng>();
        greenLatLngB = new ArrayList<LatLng>();
        greenLatLngC = new ArrayList<LatLng>();
        greenLatLngD = new ArrayList<LatLng>();
        greenLatLngE = new ArrayList<LatLng>();
        redLatLngA = new ArrayList<LatLng>();
        redLatLngB = new ArrayList<LatLng>();
        orangeLatLng = new ArrayList<LatLng>();
    }

    public PolylineOptions getPolylineOptions(){
        return polylineOptions;
    }

    public PolylineOptions getPolyLine(int color, String colorName, String route){
        polylineOptions = new PolylineOptions();
        if(colorName.equalsIgnoreCase("blue")) {
            return polylineOptions.addAll(blueLatLng)
                    .color(color)
                    .width(25)
                    .visible(true)
                    .zIndex(100);
        }
        else if(colorName.equalsIgnoreCase("red")){
            if(route.equalsIgnoreCase("A"))
                return polylineOptions.addAll(redLatLngA)
                    .color(color)
                    .width(25)
                    .visible(true)
                    .zIndex(100);
            else if(route.equalsIgnoreCase("B"))
                return polylineOptions.addAll(redLatLngB)
                        .color(color)
                        .width(25)
                        .visible(true)
                        .zIndex(100);
        }
        else if(colorName.equalsIgnoreCase("green")){
            if(route.equalsIgnoreCase("B"))
                return polylineOptions.addAll(greenLatLngB)
                    .color(color)
                    .width(32)
                    .visible(true)
                    .zIndex(100);
            if(route.equalsIgnoreCase("C"))
                return polylineOptions.addAll(greenLatLngC)
                        .color(color)
                        .width(32)
                        .visible(true)
                        .zIndex(100);
            if(route.equalsIgnoreCase("D"))
                return polylineOptions.addAll(greenLatLngD)
                        .color(color)
                        .width(32)
                        .visible(true)
                        .zIndex(100);
            if(route.equalsIgnoreCase("E"))
                return polylineOptions.addAll(greenLatLngE)
                        .color(color)
                        .width(32)
                        .visible(true)
                        .zIndex(100);
        }
        else {
            return polylineOptions.addAll(orangeLatLng)
                    .color(color)
                    .width(20)
                    .visible(true)
                    .zIndex(100);
        }
        return null;
    }

    public List<Marker> addMarkers(GoogleMap mMap, List<Stations> stationsList, Drawable drawable){
        Marker marker = null;

        LatLng ltlg;
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        BitmapDescriptor bitmapDescriptor;
        Rect bounds = new Rect();
        String textToDraw;
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.setTextSize(30.0f);
        paint.getFontMetrics(fm);
        paint.setTextAlign(Paint.Align.CENTER);

        for (Stations st:stationsList) {
            ltlg = new LatLng(st.getLat(), st.getLng());
            textToDraw = st.getName().length() > 15?st.getName().substring(0,15):st.getName();

            paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);

            int heightT = bounds.height();
            int widthT = bounds.width();

            int width = widthT + 20; //getResources().getDimensionPixelOffset(R.dimen.marker_width);
            int height = heightT + 20; //getResources().getDimensionPixelOffset(R.dimen.marker_height);

            drawable.setBounds(0, 0, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);

            if(st.getColor().equals("blue")) {
                blueLatLng.add(ltlg);
                canvas.drawColor(blue);
                paint.setColor(Color.WHITE);
                canvas.drawText(textToDraw, 80, 30, paint);
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

                marker = mMap.addMarker(new MarkerOptions()
                        .position(ltlg)
                        .title(st.getStationID() + "," + st.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue))
                        .anchor(0.5f, 0.5f));
                marker = mMap.addMarker(new MarkerOptions()
                        .position(ltlg)
                        .title(st.getStationID() + "," + st.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .anchor(0,0)
                        .alpha(0.7f));
            }
            else if(st.getColor().equals("red")){
                if(st.getRoute().contains("B"))
                    redLatLngB.add(ltlg);
                if(st.getRoute().contains("A"))
                    redLatLngA.add(ltlg);

                canvas.drawColor(red);
                paint.setColor(Color.WHITE);
                canvas.drawText(textToDraw, 80, 30, paint);
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

                marker = mMap.addMarker(new MarkerOptions()
                        .position(ltlg)
                        .title(st.getStationID() + "," + st.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.red))
                        .anchor(0.5f, 0.5f));

                marker = mMap.addMarker(new MarkerOptions()
                        .position(ltlg)
                        .title(st.getStationID() + "," + st.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .anchor(0,0)
                        .alpha(0.7f));
            }
            else if(st.getColor().equals("green")){
                if(st.getRoute().contains("B"))
                    greenLatLngB.add(ltlg);
                if(st.getRoute().contains("C"))
                    greenLatLngC.add(ltlg);
                if(st.getRoute().contains("D"))
                    greenLatLngD.add(ltlg);
                if(st.getRoute().contains("E"))
                    greenLatLngE.add(ltlg);

                canvas.drawColor(green);
                paint.setColor(Color.WHITE);
                canvas.drawText(textToDraw, 80, 30, paint);
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

                marker = mMap.addMarker(new MarkerOptions()
                        .position(ltlg)
                        .title(st.getStationID() + "," + st.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.green))
                        .anchor(0.5f, 0.5f));

                marker = mMap.addMarker(new MarkerOptions()
                        .position(ltlg)
                        .title(st.getStationID() + "," + st.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .anchor(0,0)
                        .alpha(0.7f));
            }
            else{
                orangeLatLng.add(ltlg);

                canvas.drawColor(orange);
                paint.setColor(Color.WHITE);
                canvas.drawText(textToDraw, 80, 30, paint);
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

                marker = mMap.addMarker(new MarkerOptions()
                        .position(ltlg)
                        .title(st.getStationID() + "," + st.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange))
                        .anchor(0.5f, 0.5f));

                marker = mMap.addMarker(new MarkerOptions()
                        .position(ltlg)
                        .title(st.getStationID() + "," + st.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .anchor(0,0)
                        .alpha(0.7f));
            }
            markerList.add(marker);
        }
        return markerList;
    }
}

