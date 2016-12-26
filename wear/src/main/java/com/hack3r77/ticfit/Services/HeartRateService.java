package com.hack3r77.ticfit.Services;

/**
 * Created by LucP77 on 2016-11-14.
 */

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class HeartRateService extends Service implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mHeartRate;

    private final String TAG = "Sensors";
    private final int HeartRateConstant = 220;

    private final IBinder mBinder = new LocalBinder();

    public boolean heartReady;

    private int countReady;

    private boolean started;
    boolean logDebug = false;

    private int[] hrZoneBounds;

    private int currentHRzone;
    private int previousHRzone;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public HeartRateService getService() {
            // Return this instance of LocalService so clients can call public methods
            return HeartRateService.this;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //TODO: test for accuracy of heart beat
        if(logDebug) {
            Log.d(TAG, "Got the heart rate (beats per minute) : " +
                    String.valueOf(event.values[0]));
        }
        if(started && heartReady){
            previousHRzone = currentHRzone;
            setHeartRateZone((int)event.values[0]);
            sendMessageToActivity("heartRate", String.valueOf(event.values[0]));
            //TODO: if heart rate zone changed, then send to activity
            if(previousHRzone != currentHRzone){
                sendMessageToActivity("heartRateZone", String.valueOf(currentHRzone));
            }
        }else{
            countReady++;
            if(countReady > 10){
                heartReady = true;
                sendMessageToActivity("heartReady", "true");
                countReady = 0;
                if(logDebug){
                    Log.d(TAG,"ready!");
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(logDebug){
            Log.d(TAG, "onAccuracyChanged: " + accuracy);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        if(logDebug){
            Log.d(TAG, "onStartCommand");
        }
        super.onStartCommand(intent, flags, startId);

        //TODO: test different delays
        mSensorManager.registerListener(this, mHeartRate, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(logDebug){
            Log.d(TAG, "onDestroy");
        }
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(logDebug){
            Log.d(TAG, "onCreate(): ");
        }
        //setContentView(R.layout.round_activity_main);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        heartReady = false;
        started = false;
        countReady = 0;
        currentHRzone = 0;
        previousHRzone = 0;
        //TODO: custom heart rate zones based on age https://www.polar.com/en/running/calculate-maximum-heart-rate-running
        //https://www.polar.com/en/running/running-heart-rate-zones-basics
        //hrZoneBounds = new int[6];
        setHeartRateZoneBounds(22); //TODO user age input
    }

    public void setStart(boolean value){
        if(logDebug){
            Log.d(TAG,"setStart: " + String.valueOf(value));
        }
        started = value;
    }

    private void setHeartRateZoneBounds(int age){
        int maxHeartRateEstimate = HeartRateConstant - age;
        hrZoneBounds = new int[6]; //{104, 144, 133, 152, 172, 190};
        hrZoneBounds[0] = (int)(maxHeartRateEstimate * 0.50);
        hrZoneBounds[1] = (int)(maxHeartRateEstimate * 0.60);
        hrZoneBounds[2] = (int)(maxHeartRateEstimate * 0.70);
        hrZoneBounds[3] = (int)(maxHeartRateEstimate * 0.80);
        hrZoneBounds[4] = (int)(maxHeartRateEstimate * 0.90);
        hrZoneBounds[5] = maxHeartRateEstimate;
    }

    private void setHeartRateZone( int heartRate ){

        /*Zone 5 (90-100% of maximum)
        Zone 4 (80-90% of maximum)
        Zone 3 (70-80% of maximum)
        Zone 2 (60-70% of maximum)
        Zone 1 (50-60% of maximum)*/

        //if in between zone bounds, set zone
        if(heartRate < hrZoneBounds[0]){
            currentHRzone = 0;
        }else if(hrZoneBounds[0] <= heartRate && heartRate < hrZoneBounds[1]){
            currentHRzone = 1;
        }else if(hrZoneBounds[1] <= heartRate && heartRate < hrZoneBounds[2]){
            currentHRzone = 2;
        }else if(hrZoneBounds[2] <= heartRate && heartRate < hrZoneBounds[3]){
            currentHRzone = 3;
        }else if(hrZoneBounds[3] <= heartRate && heartRate < hrZoneBounds[4]){
            currentHRzone = 4;
        }else if(hrZoneBounds[4] <= heartRate && heartRate < hrZoneBounds[5]){
            currentHRzone = 5;
        }else if(hrZoneBounds[5] <= heartRate){
            currentHRzone = 6; //HRzone 6 is not really a zone, just an extra zone for above 190 bpm
        }else{
            //error?
        }

    }

    /**
     *
     * @param key
     * @param msg
     */
    private void sendMessageToActivity(String key, String msg) {
        Intent intent = new Intent("run_activity");
        intent.putExtra(key, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(logDebug){
            Log.d(TAG, "onBind");
        }
        return mBinder;
    }
}
