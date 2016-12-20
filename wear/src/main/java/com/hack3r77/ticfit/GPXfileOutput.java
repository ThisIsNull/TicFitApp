package com.hack3r77.ticfit;

/**
 * Class:   GPXfileOutput
 *
 * Description: Creates GPX folder and file
 
 * Created by LucP77 on 2016-10-24.
 */

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GPXfileOutput {

    private final String TAG = "GPXfileOutput";
    public final String gpx = "GPX";
    public final String gpxExt = ".gpx";

    private final String gpx_pre = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<gpx    version=\"1.1\"\n" +
                                    "        creator=\"Ticwatch Fit\"\n" +
                                    "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                    "        xmlns=\"http://www.topografix.com/GPX/1/1\"\n" +
                                    "        xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"\n" +
                                    "        xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\">\n" +
                                    "<trk>\n" +
                                    "<trkseg>";

    private final String gpx_post = "</trkseg>\n" +
                                    "</trk>\n" +
                                    "</gpx>";

    private final String trkpt_pre = "<trkpt";
    private final String trkpt_post = "</trkpt>";

    private final String time_pre = "<time>";
    private final String time_post = "</time>";

    private boolean writing;

    public File gpxFolder;
    public BufferedWriter bw;

    /* sample emtry */
    /* <trkpt lat="43.652317000" lon="-79.464776000"><time>2016-10-16T00:21:30Z</time></trkpt> */

    /**  GPXfileOutput - Constructor, initiate GPX directory and file
    *
    */
    public GPXfileOutput(){
        GPXdirectoryInit();
        GPXfileInit();
        writing = true;
    }

    /**
    * GPXdirectoryInit
    */
    private void GPXdirectoryInit(){
        Log.d(TAG,"GPXdirectoryInit");
        gpxFolder = new File(Environment.getExternalStorageDirectory(), gpx);
        boolean success = true;
        if (!gpxFolder.exists()) {
            success = gpxFolder.mkdirs();
        }
        if (success) {
            Log.d(TAG,"gpx folder creation success");
        } else {
            Log.d(TAG,"gpx folder creation failed");
        }
    }

    private void GPXfileInit() {
        Log.d(TAG,"GPXfileInit");
        String fileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());

        String filePath = gpxFolder.toString() + File.separator + fileName + gpxExt;
        try {
            bw = new BufferedWriter(new FileWriter(new File(filePath), true));
            bw.write(gpx_pre + "\n");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void writeGPX(Location location, Date time){
        Log.d(TAG,"writeGPX");
        if(writing) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                String isoDateTime = sdf.format(time);
                String gpxEntry = trkpt_pre + " lat=\"" + location.getLatitude() +
                        "\" lon=\"" + location.getLongitude() + "\">" +
                        time_pre + isoDateTime + time_post +
                        trkpt_post + "\n";

                //Log.d(TAG, gpxEntry);
                bw.write(gpxEntry);

                //TODO: DEBUG - logging accuracy quickly to view for testing, *****TO BE REMOVED*******
                float gpsAccuracy = location.getAccuracy();; //Get the estimated accuracy of this location, in meters.
                String logAccuracy = "<!-- GPSaccuracy(m)" + gpsAccuracy + "-->\n";
                bw.write(logAccuracy);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void closeGPX(){
        Log.d(TAG,"gpxFileClose");
        writing = false;
        try {
            bw.write(gpx_post);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
