package edu.umb.subway;

import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Ranjan on 11/19/2016.
 */

public class StationInfoFragment extends Fragment {
    TextView destination;
    TextView timer;
    TextView distanceAway;
    TextView remainingStop;
    private static ImageView favImage;
    private static Boolean favorite;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.station_layout, container, false);
        return inflater.inflate(R.layout.station_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayout(view);
    }
    /**
     * Convert method that takes position to determine the conversion
     * factor for different unit of temperature.
     */
    public void setLayout(View view){
        Bundle savedInstanceState = getArguments();
        ((LinearLayout)view.findViewById(R.id.linear_layout)).setBackgroundColor(savedInstanceState.getInt("color"));
        TextView destination = (TextView) view.findViewById(R.id.destination);
        final TextView timer = (TextView) view.findViewById(R.id.timer);
        timer.setText("");
        distanceAway = (TextView)view.findViewById(R.id.distance_away);
        remainingStop = (TextView)view.findViewById(R.id.remaining_stop);
        favImage = (ImageView)view.findViewById(R.id.fav_image);
        //ImageView infoImage = (ImageView)view.findViewById(R.id.info_image);

        destination.setText(savedInstanceState.getString("destination"));
        distanceAway.setText(String.format("%.1f mile", savedInstanceState.getDouble("distanceAway") ));
        //remainingStop.setText("" + savedInstanceState.getInt("remStop"));
        Log.v("destination", destination.getText().toString());
        Log.v("timeAway", timer.getText().toString());
        favorite = savedInstanceState.getBoolean("favorite");
        if(favorite)
            favImage.setImageResource(R.drawable.fav_on);
        else
            favImage.setImageResource(R.drawable.fav_off);
        /*favImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(favorite){
                    favImage.setImageResource(R.drawable.fav_on);
                }else {
                    favImage.setImageResource(R.drawable.fav_off);
                }
            }
        });*/
        //infoImage.setImageResource(R.drawable.fav_off);
        new CountDownTimer(savedInstanceState.getInt("timeAway")*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timer.setText(String.format("%02d", millisUntilFinished/(60*1000)) + ":"
                        + String.format("%02d", (millisUntilFinished%(60*1000))/1000));
            }
            public void onFinish() {
                timer.setText("Arv!");
            }
        }.start();
    }

    public static void favoriteAction(View v){
        favImage = (ImageView)v.findViewById(R.id.fav_image);
        if(favorite){
            favImage.setImageResource(R.drawable.fav_on);
        }else {
            favImage.setImageResource(R.drawable.fav_off);
        }
    }
}
