package edu.umb.subway;

import org.w3c.dom.ProcessingInstruction;

/**
 * Created by Ranjan on 11/12/2016.
 */

public class Stations {
    private  Stations instance = null;
    private String stationID;
    private String name;
    private Double lat;
    private Double lng;
    private int rank;
    private String route;
    private  String color;

//    private  String webServiceName;
//    private  String message;
//    private  Boolean fsavorite;

    private Stations()
    {}
    public Stations(String stationID, String name, double lat,
                    double lng, int rank, String color, String route)
    {
        this.stationID = stationID;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.rank = rank;
        this.color = color;
        this.route = route;
//        this.webServiceName = webServiceName;
//        this.color = color;
//        this.message = message;
//        this.favorite = favorite;
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
}
