package edu.umb.subway;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Ranjan on 11/19/2016.
 */

public class StationInfoFragment extends Fragment {
    //public StationInfoFragment(String destination, String tim)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.station_layout, container, false);
    }

    /**
     * Convert method that takes position to determine the conversion
     * factor for different unit of temperature.
     * @param position
     */
    public void tempConvert(int position){
        //Log.v("test", "tempConvert method");
        TextView destination = (TextView) getActivity().findViewById(R.id.destination);
        TextView timer = (TextView) getActivity().findViewById(R.id.timer);
        TextView distanceAway = (TextView) getActivity().findViewById(R.id.distance_away);
        TextView remaininStop = (TextView) getActivity().findViewById(R.id.remaining_stop);
        ImageView favImage = (ImageView) getActivity().findViewById(R.id.fav_image);
        ImageView infoImage = (ImageView) getActivity().findViewById(R.id.info_image);
    }

}
