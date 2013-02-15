package dk.rocologo.geocacheplacer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.ZoomButtonsController;
import android.widget.ZoomControls;
//import dk.rocologo.geocacheplacer.BannerAds;

public class MainActivity extends Activity implements OnClickListener {

	static final String TAG = "GeocachePlacer";
	Button buttonRun, buttonReset, buttonStop, buttonSave;
	ProgressBar progressBar;
	WebView position;
	GPSTracker gps;
	ZoomControls zoomControls1;

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
	
	//private BannerAds adView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);

		buttonRun = (Button) findViewById(R.id.buttonRun);
		buttonReset = (Button) findViewById(R.id.buttonReset);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		buttonSave = (Button) findViewById(R.id.buttonSave);

		buttonRun.setOnClickListener(this);
		buttonReset.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
		buttonSave.setOnClickListener(this);

		position = (WebView) findViewById(R.id.webview);
		position.getSettings().setJavaScriptEnabled(true);
		// position.getSettings().setBuiltInZoomControls(true);
		// position.getSettings().setDisplayZoomControls(true);

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);

		zoomControls1 = (ZoomControls) findViewById(R.id.zoomControls1);
		
		//adView = (AdView) findViewById(R.id.adView);
//		adView = new BannerAds(this);
	

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onClick(View v) {
		Integer clickedButton = v.getId();
		final String status = "";

		// Log.d(TAG, "onClicked Button:" + clickedButton.toString());
		Log.d(TAG, "ZoomControls1: " + zoomControls1.toString());
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

		int n = 0, numberOfRuns = 5;

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

				url = "http://maps.google.com/staticmap?center="
						+ averageLatitude + "," + averageLongitude
						+ "&zoom=18&size=400x300&maptype=mobile/&markers="
						+ averageLatitude + "," + averageLongitude;
				n++;
				progressBar.setProgress(n);
				// progressBar.set

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
			position.loadUrl(url);
			Toast.makeText(
					MainActivity.this,
					"Average of " + numberOfLocations
							+ " locations is messured.", Toast.LENGTH_LONG)
					.show();
		}

	}

	/*
	public class zoomListener implements OnZoomListener {

		// public zoomButtonsController zoomy;
		// private WebView myWebView;

		public zoomListener() {
			// zBC = new ZoomButtonsController(this);
			zoomButtonsController.setOnZoomListener(this);
			zoomButtonsController.setZoomSpeed(500);
			zoomButtonsController.setAutoDismissed(false);
		}

		@Override
		public void onZoom(boolean zoomIn) {
			if (zoomIn) {
				position.zoomIn();
			} else {
				position.zoomOut();
			}
		}

		public void toggleZoom() {
			if (!zoomButtonsController.isVisible()) {
				zoomButtonsController.setVisible(true);
			} else {
				zoomButtonsController.setVisible(false);
			}
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			// TODO Auto-generated method stub

		}

	}
	*/
	

}
