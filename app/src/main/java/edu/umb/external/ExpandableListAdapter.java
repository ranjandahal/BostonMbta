package edu.umb.external;

/**
 * Created by Ranjan on 11/24/2016.
 */


import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.umb.subway.FavoriteStations;
import edu.umb.subway.R;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<FavoriteStations>> _listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<FavoriteStations>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = ((FavoriteStations) getChild(groupPosition, childPosition)).getDestination();
        final int childSlected = ((FavoriteStations) getChild(groupPosition, childPosition)).getActive();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //convertView = infalInflater.inflate(R.layout.station_layout, parent, false);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
        RelativeLayout listItem = (RelativeLayout)convertView.findViewById(R.id.list_item);
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        listItem.setBackgroundColor(getColor(((FavoriteStations) getChild(groupPosition, childPosition)).getColor()));
        txtListChild.setBackgroundColor(getColor(((FavoriteStations) getChild(groupPosition, childPosition)).getColor()));
        CheckBox childCheckBox = (CheckBox)convertView.findViewById(R.id.checkbox);

        txtListChild.setText(childText);
        //childCheckBox.setChecked(childSlected == 0? false:true);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public int getColor(String color){
        if(color.contains("green"))
            return ContextCompat.getColor(_context, R.color.green);
        else if(color.contains("red"))
            return ContextCompat.getColor(_context, R.color.red);
        else if(color.contains("orange"))
            return ContextCompat.getColor(_context, R.color.orange);
        else if(color.contains("blue"))
            return ContextCompat.getColor(_context, R.color.blue);
        return 0;
    }
}
