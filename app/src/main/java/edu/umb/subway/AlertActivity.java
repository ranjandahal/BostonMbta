package edu.umb.subway;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ranjan on 11/19/2016.
 */

public class AlertActivity extends FragmentActivity {
    private JSONArray alertArray;
    private String jsonString;
    private ListView alertList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alert_layout);

        //getAlertHeaderList(bundle.getString("alert_json"));

        // We need to use a different list item layout for devices older than Honeycomb
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;

        alertList = (ListView)findViewById(R.id.alertTitle);
        alertList.setAdapter(new ArrayAdapter<String>(this, layout, getAlertHeaderList(bundle.getString("alert_json"))));
    }

    public List<String> getAlertHeaderList(String alertJsonString){
        List < String > alertHeaderList = new ArrayList<>();
        try {
            JSONArray alertHeaders = new JSONArray(alertJsonString);
            //JSONArray alertHeaders = alertHeadersJson.getJSONArray("alert_headers");
            for (int alertCounter = 0; alertCounter < alertHeaders.length(); alertCounter++) {
                alertHeaderList.add(alertHeaders.getJSONObject(alertCounter).getString("header_text"));
            }
        }catch (Exception ex){}
        return alertHeaderList;
    }
}
