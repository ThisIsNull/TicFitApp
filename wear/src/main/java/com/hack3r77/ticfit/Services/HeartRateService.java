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

    private final IBinder mBinder = new LocalBinder();

    public boolean heartReady;

    private int countReady;

    private boolean started;
    boolean logDebug = false;
    
    private int[] heartRateZones;
    private int currentHRzone;

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
            sendMessageToActivity("heartRate", String.valueOf(event.values[0]));
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
        heartRateZones = new int[]{104, 144, 133, 152, 172, 190};
    }

    public void setStart(boolean value){
        if(logDebug){
            Log.d(TAG,"setStart: " + String.valueOf(value));
        }
        started = value;
    }

    private void setHeartRateZone( int heartRate ){

        //if in between zone bounds ..... set zone

        //currentHRzone = ?;
    }

    private void sendMessageToActivity(String key, String msg) {
        Intent intent = new Intent("run_activity");
        // You can also include some extra data.
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
