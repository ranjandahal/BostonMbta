package edu.umb.subway;

import android.widget.TextView;

import java.security.PublicKey;

/**
 * Created by Ranjan on 11/18/2016.
 */

public class TimerThread {
    private static final int DELAY = 1000;
    TextView timeTextView;
    int count;

    public TimerThread(int count){
        this.count = count;
    }

    void run(){
        try {
            int minute = 0, seconds = 0;
            while (true) {
                count--;
                Thread.sleep (DELAY);
                //threadHandler.sendEmptyMessage(0);

                minute = count / 60;
                seconds = count % 60;

                timeTextView.setText("" + minute + ":"
                        + String.format("%02d", seconds) + ":");
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
