package edu.umb.external;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.umb.subway.Stations;

/**
 * Created by Ranjan on 11/15/2016.
 */

public class VenueFilter extends Filter {
    @Override
    protected Filter.FilterResults performFiltering(CharSequence constraint) {
        List<String> list = new ArrayList<String>();
        FilterResults result = new FilterResults();
        String substr = constraint.toString().toLowerCase();
        // if no constraint is given, return the whole list
        if (substr == null || substr.length() == 0) {
            result.values = list;
            result.count = list.size();
        } /*else {
            // iterate over the list of venues and find if the venue matches the constraint. if it does, add to the result list
            final ArrayList<String> retList = new ArrayList<String>();
            for (String venue : list) {
                try {
                    if (venue.getString("name").toLowerCase().contains(constraint) ||  venue.getString("address").toLowerCase().contains(constraint) ||
                            {
                                    retList.add(venue);
                    }
                } catch (JSONException e) {
                    Log.i(Consts.TAG, e.getMessage());
                }
            }
            result.values = retList;
            result.count = retList.size();
        }*/
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        // we clear the adapter and then pupulate it with the new results
        /*searchAdapter.clear();
        if (results.count > 0) {
            for (JSONObject o : (ArrayList<JSONObject>) results.values) {
                searchAdapter.add(o);
            }
        }*/
    }

}
