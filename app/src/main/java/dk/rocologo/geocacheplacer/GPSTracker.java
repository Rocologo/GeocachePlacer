package dk.rocologo.geocacheplacer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
    private volatile int satelliteCount = 0;
    private GnssStatus.Callback gnssCallback;
    private LocationCallback locationCallback;

    public interface LocationCallback {
        void onLocationUpdated(Location loc);
    }

    public void setLocationCallback(LocationCallback cb) {
        this.locationCallback = cb;
    }

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    gnssCallback = new GnssStatus.Callback() {
                        @Override
                        public void onSatelliteStatusChanged(GnssStatus status) {
                            int used = 0;
                            for (int i = 0; i < status.getSatelliteCount(); i++) {
                                if (status.usedInFix(i)) used++;
                            }
                            satelliteCount = used;
                        }
                    };
                    locationManager.registerGnssStatusCallback(gnssCallback);
                }
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

    public int getSatelliteCount() {
        return satelliteCount;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && gnssCallback != null) {
                locationManager.unregisterGnssStatusCallback(gnssCallback);
            }
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        this.location = loc;
        this.latitude = loc.getLatitude();
        this.longitude = loc.getLongitude();
        this.altitude = loc.getAltitude();
        if (locationCallback != null) {
            locationCallback.onLocationUpdated(loc);
        }
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
        DecimalFormat df = new DecimalFormat("00.000");
        return String.format("%s %02d° %s, %s %03d° %s",
                latDir, latDeg, df.format(latMin),
                lonDir, lonDeg, df.format(lonMin));
    }

    public String decimalToDD(double lat, double lon) {
        String latDir = lat < 0 ? "S" : "N";
        String lonDir = lon < 0 ? "W" : "E";
        return String.format("%s %09.5f°, %s %010.5f°",
                latDir, Math.abs(lat), lonDir, Math.abs(lon));
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
        DecimalFormat df = new DecimalFormat("00.0");
        return String.format("%s %02d° %02d' %s\", %s %03d° %02d' %s\"",
                latDir, latDeg, latMin, df.format(latSec),
                lonDir, lonDeg, lonMin, df.format(lonSec));
    }
}
