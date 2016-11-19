package edu.umb.external;

/**
 * Created by Ranjan on 11/18/2016.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import edu.umb.subway.MainActivity;
import edu.umb.subway.R;
import edu.umb.subway.Stations;

public class AutoCompleteCustomArrayAdapter extends ArrayAdapter<Object> {

    final String TAG = "AutocompleteCustomArrayAdapter.java";
    TextView stationNameTextView;
    TextView stationColorTextView;
    Context mContext;
    int layoutResourceId;
    Object[] stations = null;
    Stations station;

    public AutoCompleteCustomArrayAdapter(Context mContext, int layoutResourceId, Object[] stations) {

        super(mContext, layoutResourceId, stations);

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.stations = stations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try{

            /*
             * The convertView argument is essentially a "ScrapView" as described is Lucas post
             * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
             * It will have a non-null value when ListView is asking you recycle the row layout.
             * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
             */
            if(convertView==null){
                // inflate the layout
                LayoutInflater inflater = ((MainActivity) mContext).getLayoutInflater();
                //convertView = inflater.inflate(layoutResourceId, parent, false);
                convertView = ((MainActivity) mContext).getLayoutInflater().inflate(R.layout.test_layout, parent, false);
            }

            // object item based on the position
            station = (Stations)stations[position];
//            stationColorTextView = (TextView)convertView.findViewById(R.id.station_color);
//            if(station.getColor().contains("green") && !station.getColor().contains(","))
//                stationColorTextView.setBackgroundResource(R.drawable.green);
//            else if(station.getColor().contains("red") && !station.getColor().contains(","))
//                stationColorTextView.setBackgroundResource(R.drawable.red);
//            else if(station.getColor().contains("orange") && !station.getColor().contains(","))
//                stationColorTextView.setBackgroundResource(R.drawable.orange);
//            else if(station.getColor().contains("blue") && !station.getColor().contains(","))
//                stationColorTextView.setBackgroundResource(R.drawable.blue);
//            else if(station.getColor().contains("green") && station.getColor().contains("blue"))
//                stationColorTextView.setBackgroundResource(R.drawable.green_blue);
//            else if(station.getColor().contains("green") && station.getColor().contains("red"))
//                stationColorTextView.setBackgroundResource(R.drawable.green_red);
//            else if(station.getColor().contains("green") && station.getColor().contains("orange"))
//                stationColorTextView.setBackgroundResource(R.drawable.green_orange);
//            else if(station.getColor().contains("red") && station.getColor().contains("orange"))
//                stationColorTextView.setBackgroundResource(R.drawable.orange_red);
//            else if(station.getColor().contains("blue") && station.getColor().contains("orange"))
//                stationColorTextView.setBackgroundResource(R.drawable.blue_orange);
//            // get the TextView and then set the text (item name) and tag (item ID) values
            stationNameTextView = (TextView) convertView.findViewById(R.id.station_name);
            stationNameTextView.setText(station.getName());

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;

    }
}