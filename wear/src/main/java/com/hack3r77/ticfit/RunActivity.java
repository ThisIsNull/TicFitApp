package com.hack3r77.ticfit;

/**
 * Class:   RunActivity
 *
 * Description: Activity to start the run, Starts location and heart rate service,
 *              Writes to GPX file
 *
 * Created by LucP77 on 2016-11-26.
 */

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.app.FragmentManager;

//import android.support.v4.app.Fragment;
/*import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;*/
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v4.view.MotionEventCompat;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.hack3r77.ticfit.Adapters.GridPagerAdapter;
import com.hack3r77.ticfit.Fragments.RunInfoFragment;
import com.hack3r77.ticfit.Fragments.RunStartFragment;
import com.hack3r77.ticfit.Services.HeartRateService;
import com.hack3r77.ticfit.Services.LocationService;

public class RunActivity extends Activity implements RunStartFragment.RunStartInterface, View.OnTouchListener {

    private final String TAG = "RunActivity";
    LocationService locationService;
    com.hack3r77.ticfit.Services.HeartRateService HeartRateService;
    boolean mBound = false;
    FragmentManager fragmentManager;
    private LocationService.LocalBinder locationBinder;
    private HeartRateService.LocalBinder serviceBinder;
    RunStartFragment runStart_fragment;
    RunInfoFragment runInfo_fragment;
    GridPagerAdapter gridAdapter;
    GridViewPager grid;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection locationServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    private ServiceConnection sensorServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HeartRateService.LocalBinder binder = (HeartRateService.LocalBinder) service;
            HeartRateService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            //ready
            if(intent.hasExtra("gpsReady")){
                setGPSready();
            }
            if(intent.hasExtra("heartReady")){
                setHeartRateReady();
            }
            //info
            if(intent.hasExtra("distance")){
                setInfoPair("distance", intent);
            }
            if(intent.hasExtra("heartRate")){
                setInfoPair("heartRate", intent);
            }
            if(intent.hasExtra("heartRateZone")){
                setInfoPair("heartRateZone", intent);
            }
            if(intent.hasExtra("avgPace")){
                setInfoPair("avgPace", intent);
            }
            if(intent.hasExtra("instantPace")){
                setInfoPair("instantPace", intent);
            }
        }

        private void setInfoPair(String key, Intent intent){
            String message = intent.getStringExtra(key);
            Log.d(TAG,"received msg: " + message);
            Pair<String, String> pair = new Pair<>(key, message);
            runInfo_fragment.setInfo(pair);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.run_start);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //location service
        Intent locationServiceIntent = new Intent(this, LocationService.class);
        locationServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(locationServiceIntent);
        bindService(locationServiceIntent, locationServiceConn, Context.BIND_AUTO_CREATE);

        //sensor service
        Intent sensorServiceIntent = new Intent(this, HeartRateService.class);
        sensorServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(sensorServiceIntent);
        bindService(sensorServiceIntent, sensorServiceConn, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("run_activity"));

        //fragments
        runStart_fragment = new RunStartFragment();
        runInfo_fragment = new RunInfoFragment();

        fragmentManager = getFragmentManager();
        //fragmentManager = getSupportFragmentManager();

        grid = (GridViewPager) findViewById(R.id.run_grid);
        gridAdapter = new GridPagerAdapter(fragmentManager, runStart_fragment, runInfo_fragment);
        //grid.setAdapter(new GridPagerAdapter(fragmentManager,fragment_run_start,fragment_run_info));
        grid.setAdapter(gridAdapter);

        runStart_fragment.setInterface(this);

        /*FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RunStartFragment fragment_run_start = new RunStartFragment();
        //fragmentTransaction.add(R.id.fragment_container, fragment_run_start);
        fragmentTransaction.replace(R.id.fragment_container, fragment_run_start);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        fragment_run_start.setInterface(this);*/


        View runView = findViewById(R.id.run_start_view);
        runView.setOnTouchListener(this);

    } /*end onCreate*/

    public void setGPSready(){
        runStart_fragment.gpsReady();
    }

    public void setHeartRateReady(){
        runStart_fragment.heartRateReady();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                Log.d(TAG, "Action was DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE):
                //Log.d(TAG, "Action was MOVE");
                return true;
            case (MotionEvent.ACTION_UP):
                Log.d(TAG, "Action was UP");
                return true;
            case (MotionEvent.ACTION_CANCEL):
                Log.d(TAG, "Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                Log.d(TAG, "Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        locationService.setStart(false);
        HeartRateService.setStart(false);
        // Unbind from the service
        if (mBound) {
            unbindService(locationServiceConn);
            unbindService(sensorServiceConn);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        locationService.onDestroy();
        HeartRateService.onDestroy();
    }

    /*public void setInfo(Pair<String, String> pair ){
        //Pair<Integer, String> simplePair = new Pair<>(42, "Second");
        String key = pair.first; // 42
        String value = pair.second; // "Second"
        fragment_run_info.setInfo(pair);
    }*/

    //override start fragment interface method
    @Override
    public void startClicked(boolean isStop){
        if(!isStop) { //start
            Log.d(TAG, "start pressed");
            if (mBound) {
                gridAdapter.setColCount(2);
                grid.setCurrentItem(0,1);
                locationService.setStart(true);
                HeartRateService.setStart(true);
                //change to info fragment
                /*FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                RunInfoFragment fragment_run_info = new RunInfoFragment();
                fragmentTransaction.replace(R.id.fragment_container, fragment_run_info);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();*/
                //fragment_run_start.setInterface(this);
            } else {
                Log.e(TAG, "*****\nERROR services not bound\n*****");
            }
        }else{ //stop
            Log.d(TAG, "stop pressed");
            locationService.setStart(false);
            HeartRateService.setStart(false);
            locationService.closeGPX();

        }
    }

    public void test(){
        Log.d(TAG," ********called from start frag***********");
    }
/*
    @Override
    public void onBackPressed() {
        Log.d(TAG,"onBackPressed");
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            Log.d(TAG,"onBackPressed - popBackStack");
            //getFragmentManager().popBackStack();
        } else {
            Log.d(TAG,"onBackPressed else");
            super.onBackPressed();
        }
    }*/

}
