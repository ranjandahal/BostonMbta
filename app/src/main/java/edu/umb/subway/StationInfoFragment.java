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
    private String stationId, stationName, dest;
    private int color;
    TextView destination;
    TextView timer;
    TextView distanceAway;
    TextView remainingStop;
    private ImageView favImage;
    private Boolean favorite;
    private static DBHandlerMbta dbHandlerMbta;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dbHandlerMbta = new DBHandlerMbta(getContext());
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
        stationId = savedInstanceState.getString("station_id");
        stationName = savedInstanceState.getString("station_name");
        dest = savedInstanceState.getString("destination");
        color = savedInstanceState.getInt("color");
        ((LinearLayout)view.findViewById(R.id.linear_layout)).setBackgroundColor(color);
        destination = (TextView) view.findViewById(R.id.destination);
        timer = (TextView) view.findViewById(R.id.timer);
        timer.setText("");
        distanceAway = (TextView)view.findViewById(R.id.distance_away);
        remainingStop = (TextView)view.findViewById(R.id.remaining_stop);
        favImage = (ImageView)view.findViewById(R.id.fav_image);

        destination.setText(dest);
        distanceAway.setText(String.format("%.1f mile", savedInstanceState.getDouble("distanceAway") ));
        //remainingStop.setText("" + savedInstanceState.getInt("remStop"));
        Log.v("destination", destination.getText().toString());
        Log.v("timeAway", timer.getText().toString());
        int favInt = dbHandlerMbta.isFavorite(stationId, dest, false);
        favorite =  favInt > 0 ? true:false;
        if(favorite)
            favImage.setImageResource(R.drawable.fav_on1);
        else
            favImage.setImageResource(R.drawable.fav_off);
        favImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(favorite){
                    favImage.setImageResource(R.drawable.fav_off);
                    favorite = false;
                }else {
                    favImage.setImageResource(R.drawable.fav_on1);
                    favorite = true;
                }
                int val = dbHandlerMbta.isFavorite(stationId, dest, true);
                if(dbHandlerMbta.isFavorite(stationId, dest, true) == -1)
                    dbHandlerMbta.addFavorite(stationId, stationName,String.format("#%06X", (0xFFFFFF & color)), dest, (favorite)?1:0 );
                else
                    dbHandlerMbta.updateFavoriteStation(stationId, dest, (favorite)?1:0);
            }
        });

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
}
