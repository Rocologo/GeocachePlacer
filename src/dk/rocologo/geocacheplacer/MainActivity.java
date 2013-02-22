package dk.rocologo.geocacheplacer;

import java.text.DecimalFormat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class MainActivity extends Activity implements OnClickListener,
		OnSharedPreferenceChangeListener {

	static final String TAG = "GeocachePlacer";
	SharedPreferences prefs;
	private Button buttonRun, buttonReset, buttonSend;
	ProgressBar progressBar;
	WebView webView;
	GPSTracker gps;
	ZoomControls zoomControls;
	Boolean averageRunning = false;

	double latitude; // Latitude
	double longitude; // Longitude
	double altitude;
	String url = ""; // url to google maps

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
	// final static String label1 = "Coordinates: ";
	// final static String label2 = "Avg.Coordinates: ";
	// final static String label3 = "Deviation : ";
	// final static String label4 = "Number of coordinates: ";
	// final static String label5 = "Altitude:  ";

	String mapsize = "400x300";
	String maptype;
	int zoomFactor = 15;

	Integer currentRun = 0;

	private ShareActionProvider shareActionProvider;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(MainActivity.this);
		maptype = prefs.getString("maptype", "mobile");

		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		int density = (int) getResources().getDisplayMetrics().density;
		int dpHeight = outMetrics.heightPixels / density;
		int dpWidth = outMetrics.widthPixels / density;
		mapsize = dpWidth + "x" + dpHeight / 2;
		Log.d(TAG, "dpWidth+Height=" + dpWidth + "," + dpHeight + " mapsize="
				+ mapsize);
		Log.d(TAG, "Label1:"+R.string.textView1);

		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);

		buttonRun = (Button) findViewById(R.id.buttonRunStop);
		buttonRun.setOnClickListener(this);
		buttonReset = (Button) findViewById(R.id.buttonReset);
		buttonReset.setOnClickListener(this);
		buttonSend = (Button) findViewById(R.id.buttonSend);
		buttonSend.setOnClickListener(this);

		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(false);

		gps = new GPSTracker(this);
		gps.getLocation();
		averageLatitude = gps.getLatitude();
		averageLongitude = gps.getLongitude();

		url = "http://maps.googleapis.com/maps/api/staticmap?center="
				+ averageLatitude + "," + averageLongitude + "&zoom=0&size="
				+ mapsize + "&maptype=" + maptype + "&sensor=true";
		webView.loadUrl(url);

		textView1.setText(getString(R.string.textView1)
				+ gps.decimalToDM(averageLatitude, averageLongitude));
		textView2.setText(getString(R.string.textView2) + "0,0");
		textView3.setText(getString(R.string.textView3) + "0");
		textView4.setText(getString(R.string.textView4) + "0");
		textView5.setText(getString(R.string.textView5) + "0");

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);

		setShareIntent(shareTheResult());

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
					url = "http://maps.googleapis.com/maps/api/staticmap?center="
							+ averageLatitude
							+ ","
							+ averageLongitude
							+ "&zoom="
							+ zoomFactor
							+ "&size="
							+ mapsize
							+ "&maptype="
							+ maptype
							+ "&sensor=true&markers="
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
				final int MIN_ZOOM = 5;
				// min is 0
				if (zoomFactor > MIN_ZOOM) {
					zoomControls.setIsZoomOutEnabled(true);
					zoomFactor--;
					// http://maps.google.com/staticmap?center=55.224617,11.759217&zoom=17&size=512x512&maptype=terrain/&markers=55.224617,11.759217
					url = "http://maps.googleapis.com/maps/api/staticmap?center="
							+ averageLatitude
							+ ","
							+ averageLongitude
							+ "&zoom="
							+ zoomFactor
							+ "&size="
							+ mapsize
							+ "&maptype="
							+ maptype
							+ "&sensor=true&markers="
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
		if (clickedButton == buttonRun.getId()) {
			if (gps.canGetLocation()) {
				if (averageRunning) {
					averageRunning = false;
					buttonRun.setText(R.string.button_run_start);
				} else {
					averageRunning = true;
					buttonRun.setText(R.string.button_run_stop);
					new MessureAverageLocation().execute(status);
				}
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				averageRunning = false;
				gps.showSettingsAlert();
			}
		} else if (clickedButton == buttonReset.getId()) {
			buttonRun.setText(R.string.button_run_start);
			averageLatitude = gps.getLatitude();
			averageLongitude = gps.getLongitude();
			averageAltitude = gps.getAltitude();
			numberOfLocations = 0;
			currentRun = 0;
			zoomFactor = 15;

			textView1.setText(getString(R.string.textView1)
					+ gps.decimalToDM(latitude, longitude));
			textView2.setText(getString(R.string.textView2) + "0,0");
			textView3.setText(getString(R.string.textView3) + "0");
			textView4.setText(getString(R.string.textView4) + "0");
			textView5.setText(getString(R.string.textView5) + "0");
			progressBar.setProgress(0);
			averageRunning = false;
			setShareIntent(shareTheResult());
		} else if (clickedButton == buttonSend.getId()) {
			startActivity(Intent.createChooser(shareTheResult(), "Share via"));
		}

	}

	public Intent shareTheResult() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "The average position was: \n"
				+ gps.decimalToDM(averageLatitude, averageLongitude)
				+ "\n\n\nGoogle maps: " + url);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Geocache Placer");
		sendIntent.setType("text/plain");
		return sendIntent;
	}

	public class MessureAverageLocation extends AsyncTask<String, Void, String> {

		Integer numberOfRunsPrefs = Integer.valueOf(prefs.getString(
				"numberOfRuns", "5"));
		Integer delay = Integer.valueOf(prefs.getString("delay", "500"));

		@Override
		protected String doInBackground(String... params) {

			while (currentRun < numberOfRunsPrefs && averageRunning) {
				gps.getNextLocation();
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

				Log.d(TAG, "Number:" + numberOfLocations + " Lat:" + latitude
						+ " Avg.lat:" + averageLatitude);

				numberOfLocations++;
				currentRun++;
				progressBar.setProgress(currentRun);

				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (currentRun == numberOfRunsPrefs) {
				currentRun = 0;
				averageRunning = false;
			}
			return params[0];
		}

		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setProgress(0);
			progressBar.setMax(numberOfRunsPrefs);
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(TAG, "onPostExecute");
			textView1.setText(getString(R.string.textView1)
					+ gps.decimalToDM(latitude, longitude));
			textView2.setText(getString(R.string.textView2)
					+ gps.decimalToDM(averageLatitude, averageLongitude));
			textView3.setText(getString(R.string.textView3)
					+ gps.decimalToDM(deltaLatitude, deltaLongitude));
			textView4.setText(getString(R.string.textView4) + numberOfLocations);
			DecimalFormat df = new DecimalFormat("###0.00");
			textView5.setText(getString(R.string.textView5) + df.format(averageAltitude)
					+ " ± " + df.format(Math.abs(deltaAltitude)));
			maptype = prefs.getString("maptype", "Mobile");
			url = "http://maps.googleapis.com/maps/api/staticmap?center="
					+ averageLatitude + "," + averageLongitude + "&zoom="
					+ zoomFactor + "&size=" + mapsize + "&&maptype=" + maptype
					+ "&sensor=true&markers=" + averageLatitude + ","
					+ averageLongitude;
			webView.loadUrl(url);
			Toast.makeText(
					MainActivity.this,
					"Average of " + numberOfLocations
							+ " locations is calculated.", Toast.LENGTH_LONG)
					.show();
			setShareIntent(shareTheResult());
			buttonRun.setText(R.string.button_run_start);
		}

	}

	// implemention the menu

	@SuppressLint("NewApi")
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate options menu
		getMenuInflater().inflate(R.menu.settings, menu);
		// Inflate activity menu resource file.
		getMenuInflater().inflate(R.menu.action_bar, menu);
		// Locate MenuItem with ShareActionProvider
		MenuItem menuItem = menu.findItem(R.id.menu_item_share);
		// Fetch and store ShareActionProvider
		shareActionProvider = (ShareActionProvider) menuItem
				.getActionProvider();
		shareActionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		// Return true to display menu
		return true;
	}

	// Call to update the share intent

	private void setShareIntent(Intent shareIntent) {
		if (shareActionProvider != null) {
			shareActionProvider.setShareIntent(shareIntent);
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intentSettings = new Intent(this, PrefsActivity.class);
		Intent intentAbout = new Intent(this, AboutActivity.class);
		// Intent intentShare = new Intent(this, ShareActivity.class);
		switch (item.getItemId()) {
		case R.id.item_share:
			Intent intentShare = shareTheResult();
			startActivity(intentShare);
			// startActivity(shareTheResult());
			return true;
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
		// TODO: set summary to new text.
		url = "http://maps.googleapis.com/maps/api/staticmap?center="
				+ averageLatitude + "," + averageLongitude + "&zoom="
				+ zoomFactor + "&size=" + mapsize + "&maptype=" + maptype
				+ "&sensor=true&markers=" + averageLatitude + ","
				+ averageLongitude;
		webView.loadUrl(url);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		gps.stopUsingGPS();
	}

}
