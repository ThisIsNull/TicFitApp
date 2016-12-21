package com.hack3r77.ticfit.Services;

/**
 * Created by LucP77 on 2016-11-26.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.hack3r77.ticfit.GPXfileOutput;
import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.MobvoiApiClient.ConnectionCallbacks;
import com.mobvoi.android.common.api.MobvoiApiClient.OnConnectionFailedListener;
import com.mobvoi.android.location.LocationListener;
import com.mobvoi.android.location.LocationRequest;
import com.mobvoi.android.location.LocationServices;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;

    LocationListener mLocationListener;
    public MobvoiApiClient mApiClient;

    private TextView mTextView;

    public LocationManager locationManager;

    public Location lastLocation;
    public Location currentLocation;

    public boolean needFirstLocation;
    private boolean started;
    private boolean gpsReady;
    private int counter;
    private final double zero = 0.0;

    public float distance;

    private GPXfileOutput gpxOutput;

    LocationRequest locationRequest;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private Date startTime;

    public void closeGPX() {
        gpxOutput.closeGPX();
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
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
        distance = (float) zero;

        started = false;
        gpsReady = false;
        needFirstLocation = true;

        String dummyprovider = "dummyprovider";

        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        lastLocation = new Location(dummyprovider);
        lastLocation.setLatitude(zero);
        lastLocation.setLongitude(zero);

        currentLocation = new Location(dummyprovider);
        currentLocation.setLatitude(zero);
        currentLocation.setLongitude(zero);

        String gpsprovider = LocationManager.GPS_PROVIDER;
        Log.d(TAG, "provider: " + gpsprovider);
        boolean gpson = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d(TAG, "gps on: " + gpson);

        mApiClient.connect();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient,this);
        mApiClient.disconnect();
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

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

        Location location = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
        Log.d(TAG," LAST location   Latitude:  " + String.valueOf(location.getLatitude()) +
                "  Longitude:  " + String.valueOf(location.getLongitude()));
    }

    public void setStart(boolean value){
        Log.d(TAG,"setStart: " + String.valueOf(value));
        started = value;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

            if((location.getLatitude() != zero) && (location.getLongitude() != zero)){
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

                    long overallPace = getOverallPace(calendar.getTime(), distance);
                    sendMessageToActivity("distance", String.valueOf(distance));
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

    public long getOverallPace(Date time, float currentDistance)
    {
        //TODO pass in time and distance and get pace, float or long?
        //TODO broadcast or use listener to send pace to main activity and write to text view
        long pace = (long) zero;
        if(currentDistance == (float)zero){
            pace = (long) zero;
        }else{
            long timeDiffMillis = time.getTime() - startTime.getTime(); //in milliseconds
            // 1000 millis / 1 sec
            // 60000 millis / 1 min
            long timeDiffMin = (timeDiffMillis / 1000)  / 60;
            int seconds = ((int)timeDiffMillis / 1000) % 60;


            long distanceTemp = (long)(currentDistance / 1000); //current distance (m) / [1000(m)/1(km)] = km

            /*
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
             */
        }
        return pace;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

 /*   private class LocationListener implements com.mobvoi.android.location.LocationListener {

        public LocationListener(com.mobvoi.android.location.LocationListener ll) {
            Log.d(TAG, "LocationListener constructor");
            // Create the LocationRequest object
            LocationRequest locationRequest = LocationRequest.create();
            // Use high accuracy
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            // Set the update interval to 1 second
            locationRequest.setInterval(1000);
            // Set the fastest update interval to 0.5 seconds
            locationRequest.setFastestInterval(900);
            // Set the minimum displacement
            locationRequest.setSmallestDisplacement(0);

            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, locationRequest, ll);

            Location location = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
            Log.d(TAG," LAST location   Latitude:  " + String.valueOf(location.getLatitude()) +
                    "  Longitude:  " + String.valueOf(location.getLongitude()));
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged!!!!!!!: " + location);
        }

        //@Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        //@Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        //@Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: " + provider);
        }

    }*/
}
