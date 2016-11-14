package edu.umb.subway;

/**
 * Created by Ranjan on 11/13/2016.
 */

public class StopInfomation {
    private String destination;
    private int timeAway;
    private int color;

    private StopInfomation(){
    }

    public StopInfomation(String destination, int timeAway, int color){
        this.destination = destination;
        this.timeAway = timeAway;
        this.color = color;
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




}
