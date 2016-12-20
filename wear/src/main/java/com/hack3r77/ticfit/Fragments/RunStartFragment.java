package com.hack3r77.ticfit.Fragments;

/**
 * Created by LucP77 on 2016-12-09.
 */

import android.app.Activity;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
//import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hack3r77.ticfit.R;

public class RunStartFragment extends Fragment {

    final private String TAG = "RunStartFragment";

    Activity activity;
    RunStartInterface runStartInterface;

    TextView gpsReadyText;
    TextView heartReadyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View startView = inflater.inflate(R.layout.fragment_run_start, container, false);

        gpsReadyText = (TextView) startView.findViewById(R.id.gpsReadyText);
        heartReadyText = (TextView) startView.findViewById(R.id.heartReadyText);

        final Button startButton = (Button) startView.findViewById(R.id.run_start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"start pressed");
                if (startButton.getText().equals("Start")){
                    startButton.setText("Stop");
                    runStartInterface.startClicked(false);
                }else{ //stop
                    startButton.setText("Start");
                    runStartInterface.startClicked(true);
                }
            }
        });
        //((RunActivity)getActivity()).test();
        return startView;
    }

    public void gpsReady(){
        gpsReadyText.setText("GPS");
    }

    public void heartRateReady(){
        heartReadyText.setText("HR");
    }

    public void setInterface(RunStartInterface runStartInterface){
        this.runStartInterface = runStartInterface;
    }

    //start fragment interface
    public interface RunStartInterface {
        public void startClicked(boolean isStop);
    }
}