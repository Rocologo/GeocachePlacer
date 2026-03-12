package dk.rocologo.geocacheplacer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.text.DecimalFormat;

public class GPSTracker extends Service implements LocationListener {

    static final String TAG = "GeocachePlacer";

    private final Context mContext;
    private boolean isGPSEnabled = false;
    private boolean canGetLocation = false;

    private Location location;
    private double latitude;
    private double longitude;
    private double altitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;

    protected LocationManager locationManager;

    public GPSTracker(MainActivity mainActivity) {
        this.mContext = mainActivity;
        getLocation();
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGPSEnabled) {
                Log.d(TAG, "GPS er ikke aktiveret");
            } else {
                this.canGetLocation = true;
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this);
                Log.d(TAG, "getLocation: GPS aktiveret");
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        altitude = location.getAltitude();
                        Log.d(TAG, "getLocation (GPS): " + location.toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    @SuppressLint("MissingPermission")
    public Location getNextLocation() {
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return location;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public float getAccuracy() {
        if (location != null && location.hasAccuracy()) {
            return location.getAccuracy();
        }
        return Float.MAX_VALUE;
    }

    public double getAltitude() {
        if (location != null) {
            altitude = location.getAltitude();
        }
        return altitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS ikke aktiveret");
        alertDialog.setMessage("GPS er ikke aktiveret. Vil du åbne indstillinger?");
        alertDialog.setPositiveButton("Indstillinger", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        });
        alertDialog.setNegativeButton("Annuller", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        // Ignoreres — vi bruger getLastKnownLocation
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Deprecated i API 29, men ingen handling nødvendig
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String decimalToDM(double lat, double lon) {
        String latDir = lat < 0 ? "S" : "N";
        String lonDir = lon < 0 ? "W" : "E";
        double absLat = Math.abs(lat);
        double absLon = Math.abs(lon);
        int latDeg = (int) absLat;
        int lonDeg = (int) absLon;
        double latMin = (absLat - latDeg) * 60;
        double lonMin = (absLon - lonDeg) * 60;
        DecimalFormat df = new DecimalFormat("00.00000");
        return latDir + " " + latDeg + "° " + df.format(latMin) + "  " +
               lonDir + " " + lonDeg + "° " + df.format(lonMin);
    }

    public String decimalToDD(double lat, double lon) {
        String latDir = lat < 0 ? "S" : "N";
        String lonDir = lon < 0 ? "W" : "E";
        DecimalFormat df = new DecimalFormat("###0.00000");
        return latDir + " " + df.format(Math.abs(lat)) + "°  " +
               lonDir + " " + df.format(Math.abs(lon)) + "°";
    }

    public String decimalToDMS(double lat, double lon) {
        String latDir = lat < 0 ? "S" : "N";
        String lonDir = lon < 0 ? "W" : "E";
        double absLat = Math.abs(lat);
        double absLon = Math.abs(lon);
        int latDeg = (int) absLat;
        int lonDeg = (int) absLon;
        int latMin = (int) ((absLat - latDeg) * 60);
        int lonMin = (int) ((absLon - lonDeg) * 60);
        double latSec = ((absLat - latDeg) * 60 - latMin) * 60;
        double lonSec = ((absLon - lonDeg) * 60 - lonMin) * 60;
        DecimalFormat df = new DecimalFormat("00.00");
        DecimalFormat di = new DecimalFormat("00");
        return latDir + " " + latDeg + "° " + di.format(latMin) + "' " + df.format(latSec) + "\"  " +
               lonDir + " " + lonDeg + "° " + di.format(lonMin) + "' " + df.format(lonSec) + "\"";
    }
}
