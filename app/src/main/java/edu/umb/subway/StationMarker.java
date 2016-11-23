package edu.umb.subway;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;

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

    public PolylineOptions getPolyLine(int color, String colorName, String route){
        polylineOptions = new PolylineOptions();
        if(colorName.equalsIgnoreCase("blue")) {
            return polylineOptions.addAll(blueLatLng)
                    .color(color)
                    .width(20)
                    .visible(true)
                    .zIndex(-1);
        }
        else if(colorName.equalsIgnoreCase("red")){
            if(route.equalsIgnoreCase("A"))
                return polylineOptions.addAll(redLatLngA)
                    .color(color)
                    .width(20)
                    .visible(true)
                    .zIndex(-1);
            else if(route.equalsIgnoreCase("B"))
                return polylineOptions.addAll(redLatLngB)
                        .color(color)
                        .width(20)
                        .visible(true)
                        .zIndex(-1);
        }
        else if(colorName.equalsIgnoreCase("green")){
            if(route.equalsIgnoreCase("B"))
                return polylineOptions.addAll(greenLatLngB)
                    .color(color)
                    .width(20)
                    .visible(true)
                    .zIndex(-1);
            if(route.equalsIgnoreCase("C"))
                return polylineOptions.addAll(greenLatLngC)
                        .color(color)
                        .width(20)
                        .visible(true)
                        .zIndex(-1);
            if(route.equalsIgnoreCase("D"))
                return polylineOptions.addAll(greenLatLngD)
                        .color(color)
                        .width(20)
                        .visible(true)
                        .zIndex(-1);
            if(route.equalsIgnoreCase("E"))
                return polylineOptions.addAll(greenLatLngE)
                        .color(color)
                        .width(20)
                        .visible(true)
                        .zIndex(-1);
        }
        else {
            return polylineOptions.addAll(orangeLatLng)
                    .color(color)
                    .width(20)
                    .visible(true)
                    .zIndex(-1);
        }
        return null;
    }

    public List<Marker> addMarkers(GoogleMap mMap, List<Stations> stationsList, Drawable drawable){
        Marker marker = null;
        LatLng ltlg;
        BitmapDescriptor bitmapStation = null; //, bitmapName = null;
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Rect bounds = new Rect();
        String textToDraw;
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.setTextSize(35.0f);
        paint.getFontMetrics(fm);
        paint.setTextAlign(Paint.Align.CENTER);

        for (Stations st:stationsList) {
            ltlg = new LatLng(st.getLat(), st.getLng());

            textToDraw = st.getShortName().length() > 16?st.getShortName().substring(0,15):st.getShortName();
            paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);
            int width = bounds.width() + 20;
            int height = bounds.height() + 15;
            drawable.setBounds(0, 0, width, height);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int)((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));

            if(st.getColor().startsWith("blue")) {
                blueLatLng.add(ltlg);

                if(st.getColor().contains(",") && st.getColor().contains("orange")) {
                    paintMultiStop(canvas, paint, blue, orange, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.blue_orange);
                }
                else if(st.getColor().contains(",") && st.getColor().contains("green")) {
                    paintMultiStop(canvas, paint, blue, green, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.green_blue);
                }
                else{
                    paint.setColor(blue);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(new RectF(0, 0, width, height), 15, 15, paint);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.blue);
                }
            }
            else if(st.getColor().startsWith("red")){
                if(st.getRoute().contains("B"))
                    redLatLngB.add(ltlg);
                if(st.getRoute().contains("A"))
                    redLatLngA.add(ltlg);

                if(st.getColor().contains(",") && st.getColor().contains("orange")) {
                    paintMultiStop(canvas, paint, red, orange, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.orange_red);
                }
                else if(st.getColor().contains(",") && st.getColor().contains("green")) {
                    paintMultiStop(canvas, paint, red, green, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.green_red);
                }
                else{
                    paint.setColor(red);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(new RectF(0, 0, width, height), 15, 15, paint);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.red);
                }
            }
            else if(st.getColor().startsWith("green")){
                if(st.getRoute().contains("B"))
                    greenLatLngB.add(ltlg);
                if(st.getRoute().contains("C"))
                    greenLatLngC.add(ltlg);
                if(st.getRoute().contains("D"))
                    greenLatLngD.add(ltlg);
                if(st.getRoute().contains("E"))
                    greenLatLngE.add(ltlg);

                if(st.getColor().contains(",") && st.getColor().contains("orange")) {
                    paintMultiStop(canvas, paint, green, orange, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.green_orange);
                }
                else if(st.getColor().contains(",") && st.getColor().contains("red")) {
                    paintMultiStop(canvas, paint, green, red, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.green_red);
                }
                else if(st.getColor().contains(",") && st.getColor().contains("blue")){
                    paintMultiStop(canvas, paint, green, blue, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.green_blue);
                }
                else{
                    paint.setColor(green);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(new RectF(0, 0, width, height), 15, 15, paint);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.green);
                }
            }
            else if(st.getColor().startsWith("orange")){
                orangeLatLng.add(ltlg);

                if(st.getColor().contains(",") && st.getColor().contains("red")) {
                    paintMultiStop(canvas, paint, orange, red, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.orange_red);
                }
                else if(st.getColor().contains(",") && st.getColor().contains("blue")) {
                    paintMultiStop(canvas, paint, orange, blue, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.blue_orange);
                }
                else if(st.getColor().contains(",") && st.getColor().contains("green")) {
                    paintMultiStop(canvas, paint, orange, green, width, height);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.green_orange);
                }
                else {
                    paint.setColor(orange);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(new RectF(0, 0, width, height), 15, 15, paint);
                    bitmapStation = BitmapDescriptorFactory.fromResource(R.drawable.orange);
                }
            }
            paint.setColor(Color.WHITE);
            canvas.drawText(textToDraw, xPos, yPos, paint);
            marker = mMap.addMarker(new MarkerOptions()
                    .position(ltlg)
                    .title(st.getStationID() + "," + st.getName() + "," + st.getZoomLevel()  + "," + st.getColor())
                    .icon(bitmapStation)
                    .anchor(0.5f, 0.5f));

            marker = mMap.addMarker(new MarkerOptions()
                    .position(ltlg)
                    .title(st.getStationID() + "," + st.getName() + "," + st.getZoomLevel()  + "," + st.getColor())
                    //.zIndex(1)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .anchor(-0.08f,0)
                    //.rotation(180.0f)
                    //.alpha(0.7f)
                    .visible(false));

            markerList.add(marker);
        }
        return markerList;
    }

    public void paintMultiStop(Canvas canvas, Paint paint, int colorOne,
                               int colorTwo, int width, int height){
        paint.setColor(colorOne);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(0, 0, width, height), 15, 15, paint);
        paint.setColor(colorTwo);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(width/2, 0, width, height), 15, 15, paint);
    }
}

