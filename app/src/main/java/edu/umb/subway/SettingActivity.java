package edu.umb.subway;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ranjan on 11/22/2016.
 */

public class SettingActivity extends FragmentActivity {
    SeekBar seekBar;
    TextView minzoomValTextView;
    Spinner spinner;
    DBHandlerMbta dbHandlerMbta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        dbHandlerMbta = new DBHandlerMbta(getApplicationContext());
        spinner = (Spinner)findViewById(R.id.default_stop_spinner);
        //spinner.setAdapter(dbHandlerMbta.getAllFavoriteStation(""));

        seekBar = (SeekBar)findViewById(R.id.zoom_seekBar);
        minzoomValTextView = (TextView)findViewById(R.id.minzoom_val);
        minzoomValTextView.setText("" + Properties.MIN_ZOOM);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = Float.parseFloat("" + progresValue) + Properties.MIN_ZOOM;
                minzoomValTextView.setText("" + progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Callback method defined by the View
     *
     * @param v
     */
    public void finishDialog(View v) {
        SettingActivity.this.finish();
    }
}

