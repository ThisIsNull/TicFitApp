package com.hack3r77.ticfit;

/**
 * Class:   MainMenuActivity
 *
 * Description: Activity contains the main menu list
 *
 * Created by LucP77 on 2016-11-25.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;

import com.hack3r77.ticfit.Adapters.ListAdapter;

public class MainMenuActivity extends Activity implements WearableListView.ClickListener {

    private static final String TAG = "MainMenuActivity";
    private final String[] mElements = {"Location GPS", "Run Activity", "Sensor", "test3"};

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        // Get the list component from the layout of the activity
        WearableListView listView = (WearableListView) findViewById(R.id.wearable_list);

        // Assign an adapter to the list
        listView.setAdapter(new ListAdapter(this, mElements));

        listView.setFocusable(true);
        listView.setFocusableInTouchMode(true);

        // Set a click listener
        listView.setClickListener(this);

    }

    // WearableListView click listener

    /**
     *
     * @param v
     */
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        switch (tag) {
            case 0: {
                Intent runActivityIntent = new Intent(this, RunActivity.class);
                runActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(runActivityIntent);
                Log.d(TAG, "Run activity");
                break;
            }
            case 1: {
                /*Intent runActivityIntent = new Intent(this, RunActivity.class);
                runActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(runActivityIntent);*/
                Log.d(TAG, "case 1");
                break;
            }
            case 2: {
                /*Intent sensorActivityIntent = new Intent(this, Sensors.class);
                sensorActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(sensorActivityIntent);*/
                Log.d(TAG, "case 2");
                break;
            }
            case 3: {
                Log.d(TAG, "case 3");
                break;
            }
            default: {
                /*Intent locationIntent = new Intent(this, LocationGPS.class);
                locationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(locationIntent);*/
                Log.d(TAG, "Default");
                break;
            }
        }
    }

    /**
     *
     */
    @Override
    public void onTopEmptyRegionClick() {
    }
}