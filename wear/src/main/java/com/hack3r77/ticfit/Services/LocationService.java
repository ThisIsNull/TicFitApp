package com.hack3r77.ticfit.Services;

/**
 * Created by LucP77 on 2016-11-26.
 */

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.hack3r77.ticfit.DistanceTimeObject;
import com.hack3r77.ticfit.GPXfileOutput;
import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.MobvoiApiClient.ConnectionCallbacks;
import com.mobvoi.android.common.api.MobvoiApiClient.OnConnectionFailedListener;
import com.mobvoi.android.location.LocationListener;
import com.mobvoi.android.location.LocationRequest;
import com.mobvoi.android.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LocationService extends Service implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;
    private final double ZERO = 0.0;
    //private LocationManager mLocationManager = null;
    //LocationListener mLocationListener;
    private MobvoiApiClient mApiClient;

    private TextView mTextView;

    private LocationManager locationManager;
    private Location lastLocation;
    private Location currentLocation;
    private LocationRequest locationRequest;

    private boolean needFirstLocation;
    private boolean started;
    private boolean gpsReady;
    private boolean isPaceArrayFull;

    private int counter;
    public float distance;

    private GPXfileOutput gpxOutput;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private Date startTime;

    private DistanceTimeObject[] instantPaceArray;

    SimpleDateFormat paceFormat;

    /**
     *
     */
    public class LocalBinder extends Binder {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mApiClient = new MobvoiApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        counter = 0;
        distance = (float) ZERO;

        started = false;
        gpsReady = false;
        needFirstLocation = true;
        isPaceArrayFull = false;

        String dummyprovider = "dummyprovider";

        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        lastLocation = new Location(dummyprovider);
        lastLocation.setLatitude(ZERO);
        lastLocation.setLongitude(ZERO);

        currentLocation = new Location(dummyprovider);
        currentLocation.setLatitude(ZERO);
        currentLocation.setLongitude(ZERO);

        String gpsprovider = LocationManager.GPS_PROVIDER;
        Log.d(TAG, "provider: " + gpsprovider);
        boolean gpson = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d(TAG, "gps on: " + gpson);

        //keep record of last 5 times and distances fro calculating instant pace
        instantPaceArray = new DistanceTimeObject[5];
        paceFormat = new SimpleDateFormat("mm:ss");

        //mApiClient.connect();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        mApiClient.connect();

        return START_STICKY;
    }

    public void closeGPX() {
        gpxOutput.closeGPX();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient,this);
        mApiClient.unregisterConnectionFailedListener(this);
        mApiClient.unregisterConnectionCallbacks(this);
        mApiClient.disconnect();

    }

    /*private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }*/

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG,"onConnected");
        mApiClient.connect();

        gpxOutput = new GPXfileOutput();

        // Create the LocationRequest object
        locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 1 second
        locationRequest.setInterval(1000);
        // Set the fastest update interval to 0.7 seconds
        locationRequest.setFastestInterval(700);
        // Set the minimum displacement
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setExpirationDuration(600000);
        locationRequest.setNumUpdates(10000);
        Log.d(TAG,locationRequest.toString());
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, locationRequest, this);

        /*Location location = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
        Log.d(TAG," LAST location   Latitude:  " + String.valueOf(location.getLatitude()) +
                "  Longitude:  " + String.valueOf(location.getLongitude()));*/
    }

    public void setStart(boolean value){
        Log.d(TAG,"setStart: " + String.valueOf(value));
        started = value;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
       // Log.d(TAG, "onLocationChanged: " + location);
        if(location != null) {
            Calendar calendar = Calendar.getInstance();
            if(!gpsReady){
                counter++;
            }
            //Log.d(TAG, "counter: " + counter);
            /*TODO: FOR PHONE GPS - find alternate way for resetting request, it stops after 13 so we restart it on 10 */
            if( (!gpsReady) && (counter>10) ){
            //   counter=0;
                gpsReady=true;
                sendMessageToActivity("gpsReady", "true");
            //    LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient,this);
             //   LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, locationRequest, this);
            }

            float gpsAccuracy = location.getAccuracy(); //Get the estimated accuracy of this location, in meters.
            Log.d(TAG, "accuracy: " + gpsAccuracy);

            //if location is not ZERO and tracking has started
            if((location.getLatitude() != ZERO) && (location.getLongitude() != ZERO)){
                if(started){
                    if(needFirstLocation){
                        needFirstLocation = false;
                        currentLocation = location;
                        startTime = calendar.getTime();
                        Log.d(TAG,"first location");
                    }else{ //else has started and currentLocation should already be set
                        lastLocation = currentLocation;
                        currentLocation = location;
                        distance = distance + currentLocation.distanceTo(lastLocation);
                        //distanceTo: Returns the approximate distance in meters between this location and the given location.
                        Log.d(TAG, "distance: "+String.valueOf(distance));
                    }

                    sendMessageToActivity("distance", String.valueOf(distance));

                    //instant pace
                    String instantPace = getInstantPace( calendar.getTime(), distance );
                    if (!instantPace.isEmpty()){
                        Log.d(TAG, "instant Pace: " + instantPace);
                        sendMessageToActivity("instantPace", instantPace);
                    }

                    //Average pace
                    String avgPace = getPace(startTime, calendar.getTime(), distance);
                    Log.d(TAG, "avg Pace: " + avgPace);
                    sendMessageToActivity("avgPace", avgPace);

                    gpxOutput.writeGPX(location, calendar.getTime());
                }//else nothing if not started
            }


            // Display the latitude and longitude in the UI
           /* mTextView.setText("accur "+String.valueOf(accuracy)+"\ncount "+counter+
                    "\nLatitude: " + String.valueOf(location.getLatitude()) +
                    "\nLongitude: " + String.valueOf(location.getLongitude()) +
                    "\ndistance " + String.valueOf(distance));*/

        }else{
            Log.d(TAG,"NULL location");
        }
    }

    private void sendMessageToActivity(String key, String msg) {
        Intent intent = new Intent("run_activity");
        // You can also include some extra data.
        intent.putExtra(key, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String getInstantPace(Date time, float currentDistance ){

        String instantPace = "";

        if (!isPaceArrayFull) {
            //start filling array
            for (int i = 0; i < instantPaceArray.length; i++) {
                if (instantPaceArray[i] == null) {
                    instantPaceArray[i] = new DistanceTimeObject(time, currentDistance);
                    if(i == (instantPaceArray.length-1) ){ //array has been filled
                        isPaceArrayFull = true;
                        break;
                    }else{
                        return instantPace;
                    }
                }
            }
        }else{
            //update pace array by shifting array left 1 and newest values are at the end
            for (int j = 0; j < (instantPaceArray.length-1); j++) {
                instantPaceArray[j] = instantPaceArray[j+1];
            }
            instantPaceArray[instantPaceArray.length-1] = new DistanceTimeObject(time, currentDistance);
        }

        //calculate pace from first and last values
        DistanceTimeObject start = instantPaceArray[0];
        DistanceTimeObject end = instantPaceArray[instantPaceArray.length-1];
        float distanceDiff = end.distance - start.distance;
        //Log.d(TAG, "instantPace distanceDiff: " + distanceDiff);
        instantPace = getPace( start.time, end.time, distanceDiff);
        //Log.d(TAG, "instantPace: " + instantPace);
        return instantPace;
    }

    private String getPace(Date start, Date end, float currentDistance) {

        Date pace = new Date();
        pace.setTime((long) ZERO);

        if(currentDistance == (float) ZERO){
            pace.setTime((long) ZERO);
        }else{
            float timeDiffMillis = end.getTime() - start.getTime(); //in milliseconds
            //Log.d(TAG, "timeDiffMin " + timeDiffMin);
            float distanceKM = (float)(currentDistance / 1000); //current distance (m) / [1000(m)/1(km)] = km
            //Log.d(TAG, "distanceKM " + distanceKM);
            long paceMilliTime = (long)(timeDiffMillis / distanceKM); //long - truncate to lower whole number
            //Log.d(TAG, "paceTime " + paceMilliTime);
            pace.setTime(paceMilliTime);
        }

        String paceStr = paceFormat.format(pace);
        return paceStr;
    }



}
