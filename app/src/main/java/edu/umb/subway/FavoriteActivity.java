package edu.umb.subway;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import edu.umb.external.ExpandableListAdapter;

/**
 * Created by Ranjan on 11/24/2016.
 */

public class FavoriteActivity extends FragmentActivity {
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<FavoriteStations>> listDataChild;
    List<FavoriteStations> favoriteStations;
    DBHandlerMbta dbHandlerMbta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expandable_layout);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.favorite_station_listView);
        dbHandlerMbta = new DBHandlerMbta(getApplicationContext());
        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        favoriteStations = dbHandlerMbta.getAllFavoriteStation("", false, true);
        List<FavoriteStations> favStations;
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<FavoriteStations>>();

        for (FavoriteStations fs: favoriteStations) {
            if(!isExists(listDataHeader, fs.getName())) {
                listDataHeader.add(fs.getName());
            }

        }
        for(int count = 0; count < listDataHeader.size(); count++){
            favStations = new ArrayList<>();
            for (FavoriteStations fs: favoriteStations) {
                if(listDataHeader.get(count).equalsIgnoreCase(fs.getName())) {
                    favStations.add(fs);
                }
            }
            listDataChild.put(listDataHeader.get(count), favStations);
        }
    }

    public boolean isExists(List<String> list, String value){
        for (String l: list) {
            if(l.equalsIgnoreCase(value))
                return true;
        }
        return false;
    }
}
