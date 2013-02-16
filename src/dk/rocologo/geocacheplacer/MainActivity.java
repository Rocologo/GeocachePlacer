package dk.rocologo.geocacheplacer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class MainActivity extends Activity implements OnClickListener,
		OnSharedPreferenceChangeListener {

	static final String TAG = "GeocachePlacer";
	SharedPreferences prefs;
	private Button buttonRun, buttonReset, buttonStop, buttonSave;
	ProgressBar progressBar;
	WebView webView;
	GPSTracker gps;
	ZoomControls zoomControls;

	double latitude; // Latitude
	double longitude; // Longitude
	double altitude;
	String url; // url to google maps

	double averageLatitude = 0, previousAverageLatitude = 0,
			deltaLatitude = 9999; // Average of Latitude for a number of
									// locations
	double averageLongitude = 0, previousAverageLongitude = 0,
			deltaLongitude = 9999; // Average of Longitude for a number of
									// locations
	double averageAltitude = 0, previousAverageAltitude = 0,
			deltaAltitude = 9999; // Average of Alitude for a number of
									// locations
	Integer numberOfLocations = 0;

	TextView textView1;
	TextView textView2;
	TextView textView3;
	TextView textView4;
	TextView textView5;

	int zoomFactor = 16;
	int numberOfRuns;

	// private BannerAds adView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(MainActivity.this);
		numberOfRuns = prefs.getInt("numberOfRuns", 5);

		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);

		buttonRun = (Button) findViewById(R.id.buttonRun);
		buttonRun.setOnClickListener(this);
		buttonReset = (Button) findViewById(R.id.buttonReset);
		buttonReset.setOnClickListener(this);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		buttonStop.setOnClickListener(this);
		buttonSave = (Button) findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(this);

		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(false);

		url = "http://maps.google.com/staticmap?center=" + averageLatitude
				+ "," + averageLongitude
				+ "&zoom=0&size=400x300&maptype=mobile/";
		webView.loadUrl(url);

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);

		zoomControls = (ZoomControls) findViewById(R.id.zoomControls1);
		// zoom speed in milliseconds
		zoomControls.setZoomSpeed(10);
		zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// max is 21
				final int MAX_ZOOM = 21;
				if (zoomFactor < MAX_ZOOM) {
					zoomControls.setIsZoomInEnabled(true);
					zoomFactor++;
					url = "http://maps.google.com/staticmap?center="
							+ averageLatitude + "," + averageLongitude
							+ "&zoom=" + zoomFactor
							+ "&size=400x300&maptype=mobile/&markers="
							+ averageLatitude + "," + averageLongitude;
					webView.loadUrl(url);
					Log.d(TAG, "ZoomIn: factor is set to " + zoomFactor);
				}
				if (zoomFactor == MAX_ZOOM) {
					zoomControls.setIsZoomInEnabled(false);
				}
				zoomControls.setIsZoomOutEnabled(true);
			}
		});

		zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int MIN_ZOOM = 12;
				// min is 0
				if (zoomFactor > MIN_ZOOM) {
					zoomControls.setIsZoomOutEnabled(true);
					zoomFactor--;
					url = "http://maps.google.com/staticmap?center="
							+ averageLatitude + "," + averageLongitude
							+ "&zoom=" + zoomFactor
							+ "&size=400x300&maptype=mobile/&markers="
							+ averageLatitude + "," + averageLongitude;
					webView.loadUrl(url);
					Log.d(TAG, "ZoomOut: factor is set to " + zoomFactor);
				}
				if (zoomFactor == MIN_ZOOM) {
					zoomControls.setIsZoomOutEnabled(false);
				}
				zoomControls.setIsZoomInEnabled(true);
			}
		});
	}

	public void onClick(View v) {
		Integer clickedButton = v.getId();
		final String status = "";
		Log.d(TAG, "onClicked Button:" + clickedButton.toString());
		Log.d(TAG, "ZoomControls: " + zoomControls.toString());
		if (clickedButton == buttonRun.getId()) {
			gps = new GPSTracker(this);
			if (gps.canGetLocation()) {
				new MessureAverageLocation().execute(status);
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				gps.showSettingsAlert();
			}
		} else if (clickedButton == buttonReset.getId()) {
			Log.d(TAG, "onClicked ButtonPause:" + clickedButton.toString());
			averageLatitude = 0;
			averageLongitude = 0;
			numberOfLocations = 0;
			textView1.setText("Current coordinates: 0,0");
			textView2.setText("Average coordinates: 0,0");
			textView3.setText("Delta coordinates: 0");
			textView4.setText("Number of runs: 0");
			progressBar.setProgress(0);
		}
	}

	public class MessureAverageLocation extends AsyncTask<String, Void, String> {
		int n = 0;

		@Override
		protected String doInBackground(String... params) {

			while (n < numberOfRuns) {

				latitude = gps.getLatitude();
				longitude = gps.getLongitude();
				altitude = gps.getAltitude();

				previousAverageLatitude = averageLatitude;
				previousAverageLongitude = averageLongitude;
				previousAverageAltitude = averageAltitude;

				averageLatitude = ((averageLatitude * numberOfLocations) + latitude)
						/ (numberOfLocations + 1);
				averageLongitude = ((averageLongitude * numberOfLocations) + longitude)
						/ (numberOfLocations + 1);
				averageAltitude = ((averageAltitude * numberOfLocations) + altitude)
						/ (numberOfLocations + 1);

				deltaLatitude = averageLatitude - previousAverageLatitude;
				deltaLongitude = averageLongitude - previousAverageLongitude;
				deltaAltitude = averageAltitude - previousAverageAltitude;

				numberOfLocations++;

				Log.d(TAG, "n: " + numberOfLocations + " Alt: " + altitude
						+ " Avg. Alt: " + averageAltitude);

				n++;
				progressBar.setProgress(n);

				// Wait 0,5 sec before messuring next location.
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			return params[0];
		}

		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setProgress(0);
			progressBar.setMax(numberOfRuns);
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			textView1.setText("Current coordinates: "
					+ gps.decimalToDM(latitude, longitude));
			textView2.setText("Average coordinates: "
					+ gps.decimalToDM(averageLatitude, averageLongitude));
			textView3.setText("Delta coordinates: "
					+ gps.decimalToDM(deltaLatitude, deltaLongitude));
			textView4.setText("Number of runs: " + numberOfLocations);
			textView5.setText("Altitude: " + averageAltitude + " +- "
					+ deltaAltitude);
			url = "http://maps.google.com/staticmap?center=" + averageLatitude
					+ "," + averageLongitude + "&zoom=" + zoomFactor
					+ "&size=400x300&&maptype=mobile/&markers="
					+ averageLatitude + "," + averageLongitude;
			webView.loadUrl(url);
			Toast.makeText(
					MainActivity.this,
					"Average of " + numberOfLocations
							+ " locations is messured.", Toast.LENGTH_LONG)
					.show();
		}

	}

	// implemention the menu
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		// return super.onCreateOptionsMenu(menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intentSettings = new Intent(this, PrefsActivity.class);
		Intent intentAbout = new Intent(this, AboutActivity.class);
		switch (item.getItemId()) {
		case R.id.item_settings:
			startActivity(intentSettings);
			return true;

		case R.id.item_about:
			Log.d(TAG, "Getting About");
			startActivity(intentAbout);
			return true;

		default:
			return false;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		numberOfRuns = prefs.getInt("numberOfRuns", 5);
		Log.d(TAG, "onSharedPreferenceChanged: numberOfRuns=" + numberOfRuns);

	}

}
