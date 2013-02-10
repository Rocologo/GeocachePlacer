package dk.rocologo.geocacheplacer;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import dk.rocologo.geocacheplacer.GPSTracker;

public class MainActivity extends Activity implements OnClickListener {

	static final String TAG = "GeocachePlacer";
	Button buttonRun, buttonPause, buttonStop, buttonSave;
	WebView position;
	GPSTracker gps;

	double latitude; // Latitude
	double longitude; // Longitude
	String url; // url to google maps

	double averageLatitude = 0, previousAverageLatitude = 0,
			deltaLatitude = 9999; // Average of Latitude for a number of
									// locations
	double averageLongitude = 0, previousAverageLongitude = 0,
			deltaLongitude = 9999; // Average of Longitude for a number of
									// locations
	Integer numberOfLocations = 0;

	TextView textView1;
	TextView textView2;
	TextView textView3;
	TextView textView4;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);

		buttonRun = (Button) findViewById(R.id.buttonRun);
		buttonPause = (Button) findViewById(R.id.buttonPause);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		buttonSave = (Button) findViewById(R.id.buttonSave);

		buttonRun.setOnClickListener(this);
		buttonPause.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
		buttonSave.setOnClickListener(this);

		position = (WebView) findViewById(R.id.webview);
		position.getSettings().setJavaScriptEnabled(true);

		// gps.resetAverageLocation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onClick(View v) {
		Integer clickedButton = v.getId();
		// Log.d(TAG, "onClicked Button:" + clickedButton.toString());
		if (clickedButton == buttonRun.getId()) {
			gps = new GPSTracker(this);
			if (gps.canGetLocation()) {
				latitude = gps.getLatitude();
				longitude = gps.getLongitude();
				textView1.setText("Current coordinates: " + latitude + ","
						+ longitude);

				previousAverageLatitude = averageLatitude;
				previousAverageLongitude = averageLongitude;

				averageLatitude = ((averageLatitude * numberOfLocations) + latitude)
						/ (numberOfLocations + 1);
				averageLongitude = ((averageLongitude * numberOfLocations) + longitude)
						/ (numberOfLocations + 1);
				textView2.setText("Average coordinates: " + averageLatitude
						+ "," + averageLongitude);

				deltaLatitude = averageLatitude - previousAverageLatitude;
				deltaLongitude = averageLongitude - previousAverageLongitude;
				textView3.setText("Delta Current coordinates: " + deltaLatitude
						+ "," + deltaLongitude);

				numberOfLocations++;
				textView4.setText("Number of runs: " + numberOfLocations);

				// gps.updateAverageLocation(latitude, longitude);
				Log.d(TAG, "n: " + numberOfLocations + " Lat,Lon: " + latitude
						+ "," + longitude);
				Log.d(TAG, "n: " + numberOfLocations + " Avg. Lat,Lon: "
						+ averageLatitude + "," + averageLongitude);
				url = "http://maps.google.com/staticmap?center="
						+ averageLatitude + "," + averageLongitude
						+ "&zoom=16&size=400x300&maptype=mobile/&markers="
						+ averageLatitude + "," + averageLongitude;
				// Log.d(TAG, "url= " + url);
				position.loadUrl(url);
				Toast.makeText(
						this,
						"N:" + numberOfLocations + " Lat:" + latitude
								+ "    AvgLat:" + averageLatitude,
						Toast.LENGTH_LONG).show();
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				gps.showSettingsAlert();
			}
		} else if (clickedButton == buttonPause.getId()) {
			Log.d(TAG, "onClicked ButtonPause:" + clickedButton.toString());
			// gps.resetAverageLocation();
			averageLatitude = 0;
			averageLongitude = 0;
			numberOfLocations = 0;
		}

	}

}
