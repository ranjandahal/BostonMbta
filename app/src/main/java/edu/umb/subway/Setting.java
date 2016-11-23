package edu.umb.subway;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by Ranjan on 11/22/2016.
 */

public class Setting extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

    }


    /**
     * Callback method defined by the View
     *
     * @param v
     */
    public void finishDialog(View v) {
        Setting.this.finish();
    }
}

