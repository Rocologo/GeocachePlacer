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
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import dk.rocologo.geocacheplacer.*;

public class MainActivity extends Activity implements OnClickListener,
		OnSharedPreferenceChangeListener {

	static final String TAG = "GeocachePlacer";
	SharedPreferences prefs;
	private Button buttonRun, buttonReset, buttonStop, buttonSend;
	ProgressBar progressBar;
	WebView webView;
	GPSTracker gps;
	ZoomControls zoomControls;
	Boolean averageRunning = false;

	double latitude; // Latitude
	double longitude; // Longitude
	double altitude;
	String url=""; // url to google maps

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
	final static String label1 = "Coordinates: ";
	final static String label2 = "Avg.Coordinates: ";
	final static String label3 = "Deviation : ";
	final static String label4 = "Number of coordinates: ";
	final static String label5 = "Altitude:  ";

	int zoomFactor = 16;
	int numberOfRuns, currentRun;
	int delay;
	
	private ShareActionProvider shareActionProvider;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		int screenwidth = getScreenWidth();
		int screenheight = getScreenHeight();
		Log.d(TAG, "ScreenWidth+Height=" + screenwidth + "," + screenheight);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(MainActivity.this);
		numberOfRuns = Integer.valueOf(prefs.getString("numberOfRuns", "5"));
		delay = Integer.valueOf(prefs.getString("delay", "500"));
		currentRun = 0;	
		
		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);

		buttonRun = (Button) findViewById(R.id.buttonRun);
		buttonRun.setOnClickListener(this);
		buttonReset = (Button) findViewById(R.id.buttonReset);
		buttonReset.setOnClickListener(this);
		buttonStop = (Button) findViewById(R.id.buttonPause);
		buttonStop.setOnClickListener(this);
		buttonSend = (Button) findViewById(R.id.buttonSend);
		buttonSend.setOnClickListener(this);

		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(false);

		url = "http://maps.google.com/staticmap?center=" + averageLatitude
				+ "," + averageLongitude
				+ "&zoom=0&size=400x300&maptype=mobile/";
		webView.loadUrl(url);

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		
		gps = new GPSTracker(this);
		
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
		if (clickedButton == buttonRun.getId()) {
			
			if (gps.canGetLocation()) {
				averageRunning = true;
				new MessureAverageLocation().execute(status);
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				averageRunning = false;
				gps.showSettingsAlert();
			}
		} else if (clickedButton == buttonReset.getId()) {
			averageLatitude = 0;
			averageLongitude = 0;
			numberOfLocations = 0;
			currentRun = 0;
			textView1.setText(label1 + "0,0");
			textView2.setText(label2 + "0,0");
			textView3.setText(label3 + "0");
			textView4.setText(label4 + "0");
			textView5.setText(label5 + "0");
			progressBar.setProgress(0);
			averageRunning = false;
		} else if (clickedButton == buttonStop.getId()) {
			averageRunning = false;
		} else if (clickedButton == buttonSend.getId()) {
			startActivity(Intent.createChooser(shareTheResult(), "Share via"));			
		}

	}
	
	public Intent shareTheResult(){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent
				.putExtra(
						Intent.EXTRA_TEXT,
						"The average position was: \n"
								+ gps.decimalToDM(averageLatitude,
										averageLongitude)
								+ "\n\n\nGoogle maps: " + url);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Geocache Placer");
		sendIntent.setType("text/plain");
		return sendIntent;
	}

	public class MessureAverageLocation extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			while (currentRun < numberOfRuns && averageRunning) {
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

				currentRun++;
				progressBar.setProgress(currentRun);

				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (currentRun == numberOfRuns)
				currentRun = 0;
			return params[0];
		}

		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setProgress(0);
			progressBar.setMax(numberOfRuns);
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(TAG, "onPostExecute");
			textView1.setText(label1 + gps.decimalToDM(latitude, longitude));
			textView2.setText(label2
					+ gps.decimalToDM(averageLatitude, averageLongitude));
			textView3.setText(label3
					+ gps.decimalToDM(deltaLatitude, deltaLongitude));
			textView4.setText(label4 + numberOfLocations);
			DecimalFormat df = new DecimalFormat("###0.00");
			textView5.setText(label5 + df.format(averageAltitude) + " +- "
					+ df.format(deltaAltitude));
			url = "http://maps.google.com/staticmap?center=" + averageLatitude
					+ "," + averageLongitude + "&zoom=" + zoomFactor
					+ "&size=400x300&&maptype=mobile/&markers="
					+ averageLatitude + "," + averageLongitude;
			webView.loadUrl(url);
			Toast.makeText(
					MainActivity.this,
					"Average of " + numberOfLocations
							+ " locations is calculated.", Toast.LENGTH_LONG)
					.show();
			setShareIntent(shareTheResult());
		}

	}

	// implemention the menu
	  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public boolean onCreateOptionsMenu(Menu menu) {
	
		// Inflate activity menu resource file.
		getMenuInflater().inflate(R.menu.activity_main, menu);

		// Locate MenuItem with ShareActionProvider
		MenuItem menuItem = menu.findItem(R.id.menu_item_share);
	/*	
		 menuItem.setOnActionExpandListener(new OnActionExpandListener() {
		        @Override
		        public boolean onMenuItemActionCollapse(MenuItem item) {
		            // Do something when collapsed
		            return true;  // Return true to collapse action view
		        }

		        @Override
		        public boolean onMenuItemActionExpand(MenuItem item) {
		            // Do something when expanded
		            return true;  // Return true to expand action view
		        }
		    });
*/
		// Fetch and store ShareActionProvider
		shareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
		shareActionProvider
		.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

		// Inflate options menu
		getMenuInflater().inflate(R.menu.menu, menu);
	// Return true to display menu
		return true;
	}

	// Call to update the share intent
	
	private void setShareIntent(Intent shareIntent) {
		if (shareActionProvider != null) {
			shareActionProvider.setShareIntent(shareIntent);
		} 
	}

	/*
	 * public View onCreateActionView() { // Inflate the action view to be shown
	 * on the action bar. LayoutInflater layoutInflater =
	 * LayoutInflater.from(mContext); View view =
	 * layoutInflater.inflate(R.layout.action_provider, null); ImageButton
	 * button = (ImageButton) view.findViewById(R.id.button);
	 * button.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { // Do something... } }); return
	 * view; }
	 */

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intentSettings = new Intent(this, PrefsActivity.class);
		Intent intentAbout = new Intent(this, AboutActivity.class);
		//Intent intentShare = new Intent(this, ShareActivity.class);
		switch (item.getItemId()) {
		case R.id.item_share:
			Intent intentShare=shareTheResult();
			startActivity(intentShare);
			//startActivity(shareTheResult());
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
		numberOfRuns = Integer.valueOf(prefs.getString("numberOfRuns", "5"));
		delay = Integer.valueOf(prefs.getString("delay", "500"));
		Log.d(TAG, "onSharedPreferenceChanged: numberOfRuns=" + numberOfRuns);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@SuppressWarnings("deprecation")
	public int getScreenWidth() {
		int screenWidth = 0;
		Point size = new Point();
		WindowManager w = getWindowManager();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			// getSize implemented in API-13
			w.getDefaultDisplay().getSize(size);
			screenWidth = size.x;
		} else {
			Display d = w.getDefaultDisplay();
			screenWidth = d.getWidth();
		}
		return screenWidth;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@SuppressWarnings("deprecation")
	public int getScreenHeight() {
		int screenHeight = 0;
		Point size = new Point();
		WindowManager w = getWindowManager();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			// getSize implemented in API-13
			w.getDefaultDisplay().getSize(size);
			screenHeight = size.y;
		} else {
			Display d = w.getDefaultDisplay();
			screenHeight = d.getHeight();
		}
		return screenHeight;
	}

}
