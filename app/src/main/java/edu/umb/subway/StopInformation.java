package edu.umb.subway;

import java.util.List;

/**
 * Created by Ranjan on 11/13/2016.
 */

public class StopInformation {
    private String stationId;
    private String stationName;
    private String destination;
    private int timeAway;
    private int color;
    private boolean favorite;
    private double distanceAway;
    private int remainingStop;

    private StopInformation(){}

    public StopInformation(String stationId, String stationName, String destination, int timeAway, int color,
                           boolean favorite, double distanceAway, int remainingStop){
        this.stationId = stationId;
        this.stationName = stationName;
        this.destination = destination;
        this.timeAway = timeAway;
        this.color = color;
        this.favorite = favorite;
        this.distanceAway = distanceAway;
        this.remainingStop = remainingStop;
    }

    public String getDestination() {return destination;}

    public String getStationId() {return stationId;}

    public void setStationId(String stationId) {this.stationId = stationId;}

    public String getStationName() {return stationName;}

    public void setStationName(String stationName) {this.stationName = stationName;}

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getTimeAway() {
        return timeAway;
    }

    public void setTimeAway(int timeAway) {
        this.timeAway = timeAway;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isFavorite() { return favorite;}

    public void setFavorite(boolean favorite) { this.favorite = favorite;}

    public double getDistanceAway() {return distanceAway;}

    public void setDistanceAway(double distanceAway) {this.distanceAway = distanceAway;}

    public int getRemainingStop() {return remainingStop;}

    public void setRemainingStop(int remainingStop) {this.remainingStop = remainingStop;}

    public static boolean checkDuplicateDestination(List<StopInformation> stopInfomationList, String destination){
        for (StopInformation si:stopInfomationList) {
            if(si.getDestination().equalsIgnoreCase(destination))
                return true;
        }
        return false;
    }
}
