package dk.rocologo.geocacheplacer;

import java.text.DecimalFormat;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

	double latitude;
	double averageLatitude = 0;
	double previousAverageLatitude = 0;
	double deltaLatitude = 9999;
	double longitude;
	double averageLongitude = 0;
	double previousAverageLongitude = 0;
	double deltaLongitude = 9999;
	double altitude;
	double averageAltitude = 0;
	double previousAverageAltitude = 0;
	double deltaAltitude = 9999;

	Integer numberOfLocations = 0;

	TextView textView1;
	TextView textView2;
	TextView textView3;
	TextView textView4;
	TextView textView5;

	int dpWidth;
	int dpHeight;
	int webViewHeight, webViewWidth;
	int zoomFactor = 17;
	int density;
	Boolean forceDisplayOn;

	Integer currentRun = 0;

	private ShareActionProvider shareActionProvider;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(MainActivity.this);

		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		density = (int) getResources().getDisplayMetrics().density;
		dpHeight = outMetrics.heightPixels / density;
		dpWidth = outMetrics.widthPixels / density;
		// Log.d(TAG,"Density:"+density);
		// Log.d(TAG, "dpWidth+Height=" + dpWidth + "," + dpHeight + " mapsize="
		// + mapsize);

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

		if (webViewHeight == 0) {
			Log.d(TAG, "Loading dropbox url");
			webView.loadUrl("https://dl.dropbox.com/u/36067670/Geocache%20Placer/Welcome.html");
		}

		gps = new GPSTracker(this);
		gps.getLocation();
		averageLatitude = gps.getLatitude();
		averageLongitude = gps.getLongitude();
		loadWebView(averageLatitude, averageLongitude);

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
					loadWebView(averageLatitude, averageLongitude);
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
					loadWebView(averageLatitude, averageLongitude);
					Log.d(TAG, "ZoomOut: factor is set to " + zoomFactor);
				}
				if (zoomFactor == MIN_ZOOM) {
					zoomControls.setIsZoomOutEnabled(false);
				}
				zoomControls.setIsZoomInEnabled(true);
			}
		});

		webView.setWebViewClient(new WebViewClient() {

			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (webViewHeight == 0) {
					webViewHeight = view.getHeight();
					webViewWidth = view.getWidth();
					Log.d(TAG, "OnPageFinished - Width,Height:" + webViewWidth
							+ "," + webViewHeight);
					loadWebView(averageLatitude, averageLongitude);
				}
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);

				if (webViewHeight == 0) {
					webViewHeight = view.getHeight();
					webViewWidth = view.getWidth();
					Log.d(TAG, "onLoadResource - Width,Height:" + webViewWidth
							+ "," + webViewHeight);
					loadWebView(averageLatitude, averageLongitude);
				}
			}
		});

		if (prefs.getBoolean("forceDisplayOn", true)) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// webView.loadUrl("https://dl.dropbox.com/u/36067670/Geocache%20Placer/Welcome.html");
		// loadWebView(averageLatitude, averageLongitude);
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
		sendIntent.putExtra(
				Intent.EXTRA_TEXT,
				"The average position was: \n"
						+ gps.decimalToDM(averageLatitude, averageLongitude)
						+ "\n\n\nGoogle maps: "
						+ createUrl(averageLatitude, averageLongitude));
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
			// Log.d(TAG, "onPostExecute");
			textView1.setText(getString(R.string.textView1)
					+ gps.decimalToDM(latitude, longitude));
			textView2.setText(getString(R.string.textView2)
					+ gps.decimalToDM(averageLatitude, averageLongitude));
			textView3.setText(getString(R.string.textView3)
					+ gps.decimalToDM(deltaLatitude, deltaLongitude));
			textView4
					.setText(getString(R.string.textView4) + numberOfLocations);
			DecimalFormat df = new DecimalFormat("###0.00");
			textView5.setText(getString(R.string.textView5)
					+ df.format(averageAltitude) + " ± "
					+ df.format(Math.abs(deltaAltitude)));
			loadWebView(averageLatitude, averageLongitude);
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
		if (key == "maptype") {
			loadWebView(averageLatitude, averageLongitude);
		} else if (key == "forceDisplayOn") {
			if (prefs.getBoolean("forceDisplayOn", true)) {
				getWindow().addFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			} else {
				getWindow()
						.addFlags(
								WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
			}
		}
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "orientation changed: height=" + webViewHeight);
		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
		// webView = (WebView) findViewById(R.id.webview);
		// webView.getSettings().setJavaScriptEnabled(true);
		// webView.getSettings().setBuiltInZoomControls(false);
		// webView.loadUrl("https://dl.dropbox.com/u/36067670/Geocache%20Placer/Welcome.html");

		// gps = new GPSTracker(this);
		// gps.getLocation();
		// averageLatitude = gps.getLatitude();
		// averageLongitude = gps.getLongitude();
		// loadWebView(averageLatitude, averageLongitude);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		gps.stopUsingGPS();
	}

	public void loadWebView(double latitude, double longitude) {
		String url = createUrl(latitude, longitude);
		Log.d(TAG, "loadWebView - Height=" + webViewHeight);
		if (webViewHeight != 0) {
			webView.loadUrl(url);
			Log.d(TAG, "Loading google maps");
		}

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public String createUrl(double latitude, double longitude) {
		String url;
		int width, height;
		int display_mode = getResources().getConfiguration().orientation;
		if (display_mode == 1) {
			// Portrait
			width = webViewWidth / density;
			height = webViewHeight / density;
			Log.d(TAG, "webView portrait width,height:" + width + "," + height);
		} else {
			// Landscape
			width = webViewWidth / density;
			height = webViewHeight / density;
			Log.d(TAG, "webView landscape width,height:" + width + "," + height);
		}
		String maptype = prefs.getString("maptype", "satellite");
		url = "http://maps.googleapis.com/maps/api/staticmap?center="
				+ averageLatitude + "," + averageLongitude + "&zoom="
				+ zoomFactor + "&size=" + width + "x" + height + "&maptype="
				+ maptype + "&sensor=true&markers=" + averageLatitude + ","
				+ averageLongitude;
		return url;
	}

}
