package com.hack3r77.ticfit.Fragments;

/**
 * Created by LucP77 on 2016-12-09.
 */

import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hack3r77.ticfit.R;

public class RunInfoFragment extends Fragment {

    private TextView infoText;
    private final String TAG = "RunInfoFragment";
    private String distance;
    private String heartRate;
    private String heartRateZone;
    private String avgPace;
    private String instantPace;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View infoView = inflater.inflate(R.layout.fragment_run_info, container, false);

        infoText = (TextView) infoView.findViewById(R.id.infoText);

        distance = "0.0";
        heartRate = "0";
        heartRateZone = "0";
        avgPace = "00:00";
        instantPace = "00:00";

        return infoView;
    }

    /**
     *
     * @param pair
     */
    public void setInfo(Pair<String,String> pair){
        Log.d(TAG, pair.first + ": " + pair.second);
        if(pair.first.equals("distance")){
            distance = pair.second;
        }else if(pair.first.equals("heartRate")){
            heartRate = pair.second;
        }else if(pair.first.equals("heartRateZone")){
            heartRateZone = pair.second;
        }else if(pair.first.equals("avgPace")){
            avgPace = pair.second;
        }else if(pair.first.equals("instantPace")){
            instantPace = pair.second;
        }

        if(!infoText.equals(null)) {
            infoText.setText("distance(km): " + distance +
                    "\nPace(min/km): " + instantPace +
                    "\navg. Pace: " + avgPace +
                    "\nHR(bpm): " + heartRate +
                    "\nZone: " + heartRateZone);
        }
    }
}