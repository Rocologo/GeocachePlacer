package dk.rocologo.geocacheplacer;

import java.text.DecimalFormat;

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
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener {

	static final String TAG = "GeocachePlacer";

	private final Context mContext;

	// flag for GPS status
	private boolean isGPSEnabled = false;

	// flag for network status
	private boolean isNetworkEnabled = false;

	private boolean canGetLocation = false;

	private Location location; // location
	private double latitude; // latitude
	private double longitude; // longitude
	private double altitude; // altitude
	// private double averageLatitude; // Average latitude
	// private double averageLongitude; // Average longitude
	// private Integer numberOfLocations; // the number of locations used in the
	// average

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker(MainActivity mainActivity) {
		this.mContext = mainActivity;
		getLocation();
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);
			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				// First get location from Network Provider
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d(TAG, "getLocation: Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						Log.d(TAG,
								"getLocation(1): location:"
										+ location.toString());
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
							altitude = location.getAltitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d(TAG, "getLocation: GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							Log.d(TAG,
									"getLocation(2): location:"
											+ location.toString());
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
								altitude = location.getAltitude();
							}
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}
		return latitude;
	}

	/**
	 * Function to get average latitude
	 * */
	// double getAverageLatitude() {
	// return averageLatitude;
	// }

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}
		return longitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getAltitude() {
		if (location != null) {
			altitude = location.getAltitude();
		}
		return altitude;
	}

	/**
	 * Function to get average longitude
	 * */
	// public double getAverageLongitude() {
	// return averageLongitude;
	// }

	/**
	 * Function to get the number of locations used in the average calculation
	 * */
	// public Integer getNumberOfLocations() {
	// return numberOfLocations;
	// }

	/**
	 * Function to check if best network provider
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog
				.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// Setting Icon to Dialog
		// alertDialog.setIcon(R.drawable.delete);

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						mContext.startActivity(intent);
					}
				});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

	/**
	 * Reset the measurement of the average location
	 * */
	/*
	 * public void resetAverageLocation() { Log.d(TAG, "resetAverageLocation");
	 * averageLatitude = 0; averageLongitude = 0; numberOfLocations = 0; }
	 */

	/**
	 * Update the average with the new location
	 * */
	/*
	 * public void updateAverageLocation(double latitude, double longitude) {
	 * //if (averageLatitude) Log.d(TAG, "updateAverageLocation");
	 * averageLatitude = ((averageLatitude * numberOfLocations) + latitude) /
	 * (numberOfLocations + 1); averageLongitude = ((averageLongitude *
	 * numberOfLocations) + longitude) / (numberOfLocations + 1);
	 * numberOfLocations++; }
	 */

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "onProviderDisabled");
		Toast.makeText(this, "GPS Disabled!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "onProviderEnabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "onStatusChanged");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return null;
	}


	public String decimalToDM(double coord1, double coord2) {
        String output1, degrees1, minutes1, output2, degrees2, minutes2;
        //, seconds;
 
        // gets the modulus the coordinate divided by one (MOD1).
        // in other words gets all the numbers after the decimal point.
        // e.g. mod = 87.728056 % 1 == 0.728056
        //
        // next get the integer part of the coord. On other words the whole number part.
        // e.g. intPart = 87
 
        double mod1 = coord1 % 1;
        int intPart1 = (int)coord1;
        
        double mod2 = coord2 % 1;
        int intPart2 = (int)coord2;
 
        //set degrees to the value of intPart
        //e.g. degrees = "87"
 
        degrees1 = String.valueOf(intPart1);
        degrees2 = String.valueOf(intPart2);
        
 
        // next times the MOD1 of degrees by 60 so we can find the integer part for minutes.
        // get the MOD1 of the new coord to find the numbers after the decimal point.
        // e.g. coord = 0.728056 * 60 == 43.68336
        //      mod = 43.68336 % 1 == 0.68336
        //
        // next get the value of the integer part of the coord.
        // e.g. intPart = 43
 
        coord1 = mod1 * 60;
        mod1 = coord1 % 1;
        intPart1 = (int)coord1;
        
        coord2 = mod2 * 60;
        mod2 = coord2 % 1;
        intPart2 = (int)coord2;
 
        String.valueOf(coord1);
		// set minutes to the value of intPart.
        // e.g. minutes = "43"
        //minutes1 = String.valueOf(coord1);
        DecimalFormat df = new DecimalFormat("00.###");
        minutes1=df.format(coord1);
        //minutes1 = String.format("%2.3f", coord1);
        //String.valueOf(coord2);
        minutes2=df.format(coord2);
		//minutes2 = String.format("%2.3f", coord2);
        
        //do the same again for minutes
        //e.g. coord = 0.68336 * 60 == 41.0016
        //e.g. intPart = 41
        //coord = mod * 60;
        //intPart = (int)coord;
 
        // set seconds to the value of intPart.
        // e.g. seconds = "41"
        // seconds = String.valueOf(intPart);
 
        // I used this format for android but you can change it 
        // to return in whatever format you like
        // e.g. output = "87/1,43/1,41/1"
        //output = degrees + "/1," + minutes + "/1," + seconds + "/1";
 
        //Standard output of D°M′S″
        //output = degrees + "°" + minutes + "'" + seconds + "\"";
        
        // Type   Dir.   Sign    Test
        // Lat.   N      +       > 0
        // Lat.   S      -       < 0
        // Long.  E      +       > 0
        // Long.  W      -       < 0        
        
        if (coord1<0) {
        	output1 = "S "+degrees1 + "° " + minutes1 ;
        } else {
        	output1 = "N "+degrees1 + "° " + minutes1 ;
        }

        if (coord2<0) {
        	output2 = "W "+degrees2 + "° " + minutes2 ;
        } else {
        	output2 = "E "+degrees2 + "° " + minutes2 ;
        }
 
        return output1+" "+output2;
}
 
       /*
        * Conversion DMS to decimal 
        *
        * Input: latitude or longitude in the DMS format ( example: N 43° 36' 15.894")
        * Return: latitude or longitude in decimal format   
        * hemisphereOUmeridien => {W,E,S,N}
        *
        */
        public double DMSToDecimal(String hemisphereOUmeridien,double degres,double minutes,double secondes)
        {
                double LatOrLon=0;
                double signe=1.0;
 
                if((hemisphereOUmeridien=="W")||(hemisphereOUmeridien=="S")) {signe=-1.0;}              
                LatOrLon = signe*(Math.floor(degres) + Math.floor(minutes)/60.0 + secondes/3600.0);
 
                return(LatOrLon);               
        }
	
}
