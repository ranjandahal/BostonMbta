package edu.umb.subway;

/**
 * Created by Ranjan on 11/20/2016.
 */

public class FavoriteStations extends Stations{
    private String destination;

    public FavoriteStations(String sid, String cStationName, String color, String destination ){
        super(sid,cStationName,"", 0.0,0.0,0,color,"",0.0f);
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
