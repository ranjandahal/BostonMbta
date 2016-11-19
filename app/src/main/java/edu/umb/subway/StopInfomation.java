package edu.umb.subway;

import java.util.List;

/**
 * Created by Ranjan on 11/13/2016.
 */

public class StopInfomation {
    private String destination;
    private int timeAway;
    private int color;
    private boolean favorite;

    private StopInfomation(){}

    public StopInfomation(String destination, int timeAway, int color, boolean favorite){
        this.destination = destination;
        this.timeAway = timeAway;
        this.color = color;
        this.favorite = favorite;
    }

    public String getDestination() {
        return destination;
    }

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

    public static boolean checkDuplicateDestination(List<StopInfomation> stopInfomationList, String destination){
        for (StopInfomation si:stopInfomationList) {
            if(si.getDestination().equalsIgnoreCase(destination))
                return true;
        }
        return false;
    }
}
