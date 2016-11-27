package edu.umb.subway;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.ProcessingInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ranjan on 11/12/2016.
 */

public class Stations {
    private  Stations instance = null;
    private String stationID;
    private String name;
    private String shortName;
    private Double lat;
    private Double lng;
    private int rank;
    private String route;
    private  String color;
    private float zoomLevel;

    private Stations()
    {}
    public Stations(String stationID, String name, String shortName, double lat,
                    double lng, int rank, String color, String route,
                    float zoomLevel)
    {
        this.stationID = stationID;
        this.name = name;
        this.shortName = shortName;
        this.lat = lat;
        this.lng = lng;
        this.rank = rank;
        this.color = color;
        this.route = route;
        this.zoomLevel = zoomLevel;
    }
    public String getStationID() {
        return stationID;
    }

    public  void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public  Double getLat() {
        return lat;
    }

    public  void setLat(Double lat) {
        lat = lat;
    }

    public  Double getLng() {
        return lng;
    }

    public  void setLng(Double lng) {
        this.lng = lng;
    }

    public  String getName() {
        return name;
    }

    public  void setName(String name) {
        this.name = name;
    }

    public String getShortName() {return shortName;}

    public void setShortName(String shortName) {this.shortName = shortName;}

    public  int getRank() {
        return rank;
    }

    public  void setRank(int rank) {
        this.rank = rank;
    }

    public String getRoute() { return route; }

    public void setRoute(String route) { this.route = route; }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public  Stations getInstance() {
        if(instance == null) {
            instance = new Stations();
        }
        return instance;
    }

    public static LatLng getLatLng(List<Stations> stationsList, String stationName){
        for (Stations st:stationsList){
            if(st.getName().equalsIgnoreCase(stationName))
                return new LatLng(st.getLat(), st.getLng());
        }
        return null;
    }

    public float getZoomLevel() { return zoomLevel;}

    public void setZoomLevel(float zoomLevel) { this.zoomLevel = zoomLevel;}

    public class FavoriteStations{
    }

    public int hashcode() {
        return 1;
    }

    public boolean equals(Stations st) {
        return this.name.equalsIgnoreCase(st.getName());
    }

    @Override
    public String toString(){
        return this.stationID + "," + this.name + "," + this.shortName + ","
                + this.lat + "," + this.lng + "," + this.rank + ","
                + this.color.replace(',','|') + "," + this.route + "," + this.zoomLevel;
    }

    public static Stations parseStation(String stationString){
        try {
            String[] stationValues = stationString.split(",");
            return new Stations(stationValues[0], stationValues[1], stationValues[2], Double.parseDouble(stationValues[3]),
                                Double.parseDouble(stationValues[4]), Integer.parseInt(stationValues[5]),
                                stationValues[6], stationValues[7], Float.parseFloat(stationValues[8]));
        }catch (Exception ex){}
        return null;
    }
}
