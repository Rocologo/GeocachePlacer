package dk.rocologo.geocacheplacer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
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
import android.widget.ToggleButton;
import android.widget.ZoomControls;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.WindowCompat;
import androidx.appcompat.widget.ShareActionProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    static final String TAG = "GeocachePlacer";
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    SharedPreferences prefs;
    private Button buttonRun, buttonReset, buttonPause, buttonSend;
    ProgressBar progressBar;
    WebView webView;
    GPSTracker gps;
    ZoomControls zoomControls;
    volatile boolean averageRunning = false;

    double latitude;
    double longitude;
    double altitude;

    double averageLatitude = 0, previousAverageLatitude = 0, deltaLatitude = 9999;
    double averageLongitude = 0, previousAverageLongitude = 0, deltaLongitude = 9999;
    double averageAltitude = 0, previousAverageAltitude = 0, deltaAltitude = 9999;
    int numberOfLocations = 0;
    int currentRun = 0;
    int zoomFactor = 16;

    TextView textView1, textView2, textView3, textView4, textView5;
    final static String label1 = "Coordinates: ";
    final static String label2 = "Avg.Coordinates: ";
    final static String label3 = "Deviation : ";
    final static String label4 = "Number of coordinates: ";
    final static String label5 = "Altitude:  ";

    private ShareActionProvider shareActionProvider;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private String currentMapType = "standard";
    private String leafletCssCache = null;
    private String leafletJsCache = null;
    private final java.util.List<double[]> measurementPoints = new java.util.ArrayList<>();
    private ToggleButton buttonFollow;
    private boolean followMap = true;
    private boolean mapInitialized = false;
    private MapView mapView;
    private GoogleMap googleMap;
    private final java.util.List<Marker> googleMeasurementMarkers = new java.util.ArrayList<>();
    private Marker googleAverageMarker;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Deaktivér Android 15 edge-to-edge — layout er ikke designet til det
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);

        buttonRun = findViewById(R.id.buttonRun);
        buttonRun.setOnClickListener(this);
        buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(this);
        buttonPause = findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(this);
        buttonSend = findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                mapInitialized = true;
            }
        });

        buttonFollow = findViewById(R.id.buttonFollow);
        buttonFollow.setOnCheckedChangeListener((btn, isChecked) -> {
            followMap = isChecked;
            if (followMap && numberOfLocations > 0) {
                if (isGoogleMapsType(currentMapType) && googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(averageLatitude, averageLongitude)));
                } else {
                    webView.evaluateJavascript("panTo(" + averageLatitude + "," + averageLongitude + ");", null);
                }
            }
        });

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(gMap -> {
            googleMap = gMap;
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            if ("googlemaps".equals(currentMapType)) {
                updateGoogleMapFull(averageLatitude, averageLongitude, zoomFactor);
            }
        });

        progressBar = findViewById(R.id.progressBar1);

        loadMap(0, 0, zoomFactor);

        zoomControls = findViewById(R.id.zoomControls1);
        zoomControls.setZoomSpeed(10);
        zoomControls.setOnZoomInClickListener(v -> {
            if (isGoogleMapsType(currentMapType) && googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            } else {
                zoomFactor = Math.min(zoomFactor + 1, 19);
                loadMap(averageLatitude, averageLongitude, zoomFactor);
            }
        });
        zoomControls.setOnZoomOutClickListener(v -> {
            if (isGoogleMapsType(currentMapType) && googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.zoomOut());
            } else {
                zoomFactor = Math.max(zoomFactor - 1, 3);
                loadMap(averageLatitude, averageLongitude, zoomFactor);
            }
        });

        // Hold skærm tændt hvis sat i preferences
        applyScreenOnSetting();

        // AdMob initialisering
        MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Anmod om GPS-tilladelse
        requestLocationPermission();
    }

    private void applyScreenOnSetting() {
        boolean keepOn = prefs.getBoolean("keepScreenOn", true);
        if (keepOn) {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            initializeGPS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeGPS();
            } else {
                Toast.makeText(this, "GPS-tilladelse kræves for at måle position.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeGPS() {
        gps = new GPSTracker(this);
        showCurrentPosition();
    }

    private void showCurrentPosition() {
        if (gps == null) return;
        double curLat = gps.getLatitude();
        double curLon = gps.getLongitude();
        if (curLat != 0 || curLon != 0) {
            textView1.setText(label1 + formatCoords(curLat, curLon));
            loadMap(curLat, curLon, zoomFactor);
        } else {
            textView1.setText(label1 + "—");
            loadMap(0, 0, zoomFactor);
        }
        textView2.setText(label2 + "—");
        textView3.setText(label3 + "—");
        textView4.setText(label4 + "0");
        textView5.setText(label5 + "—");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.buttonRun) {
            if (gps == null) {
                Toast.makeText(this, "Venter på GPS-tilladelse...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (gps.canGetLocation()) {
                averageRunning = true;
                startMeasuring();
            } else {
                averageRunning = false;
                gps.showSettingsAlert();
            }
        } else if (id == R.id.buttonReset) {
            averageLatitude = 0;
            averageLongitude = 0;
            averageAltitude = 0;
            numberOfLocations = 0;
            currentRun = 0;
            progressBar.setProgress(0);
            averageRunning = false;
            measurementPoints.clear();
            for (Marker m : googleMeasurementMarkers) m.remove();
            googleMeasurementMarkers.clear();
            if (googleAverageMarker != null) { googleAverageMarker.remove(); googleAverageMarker = null; }
            showCurrentPosition();
            setShareIntent(shareTheResult());
        } else if (id == R.id.buttonPause) {
            averageRunning = false;
        } else if (id == R.id.buttonSend) {
            startActivity(Intent.createChooser(shareTheResult(), "Del via"));
        }
    }

    private void startMeasuring() {
        final int numberOfRunsPrefs = Integer.parseInt(prefs.getString("numberOfRuns", "5"));
        final int delay = Integer.parseInt(prefs.getString("delay", "500"));

        mainHandler.post(() -> {
            progressBar.setProgress(0);
            progressBar.setMax(numberOfRunsPrefs);
        });

        executor.execute(() -> {
            while (currentRun < numberOfRunsPrefs && averageRunning) {
                if (gps == null) break;
                gps.getNextLocation();
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                altitude = gps.getAltitude();

                previousAverageLatitude = averageLatitude;
                previousAverageLongitude = averageLongitude;
                previousAverageAltitude = averageAltitude;

                averageLatitude = ((averageLatitude * numberOfLocations) + latitude) / (numberOfLocations + 1);
                averageLongitude = ((averageLongitude * numberOfLocations) + longitude) / (numberOfLocations + 1);
                averageAltitude = ((averageAltitude * numberOfLocations) + altitude) / (numberOfLocations + 1);

                deltaLatitude = averageLatitude - previousAverageLatitude;
                deltaLongitude = averageLongitude - previousAverageLongitude;
                deltaAltitude = averageAltitude - previousAverageAltitude;

                numberOfLocations++;
                currentRun++;

                final double pointLat = latitude;
                final double pointLon = longitude;
                final double avgLat = averageLatitude;
                final double avgLon = averageLongitude;
                final int progress = currentRun;
                mainHandler.post(() -> {
                    progressBar.setProgress(progress);
                    measurementPoints.add(new double[]{pointLat, pointLon});
                    updateMapJS(pointLat, pointLon, avgLat, avgLon);
                });

                Log.d(TAG, "Nr:" + numberOfLocations + " Lat:" + latitude + " Avg.lat:" + averageLatitude);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (currentRun >= numberOfRunsPrefs) currentRun = 0;

            // Opdater UI på main thread
            final double finalLat = averageLatitude;
            final double finalLon = averageLongitude;
            final double finalAlt = averageAltitude;
            final double finalDeltaLat = deltaLatitude;
            final double finalDeltaLon = deltaLongitude;
            final double finalDeltaAlt = deltaAltitude;
            final double finalCurLat = latitude;
            final double finalCurLon = longitude;
            final int finalCount = numberOfLocations;

            mainHandler.post(() -> {
                if (gps != null) {
                    textView1.setText(label1 + formatCoords(finalCurLat, finalCurLon));
                    textView2.setText(label2 + formatCoords(finalLat, finalLon));
                    textView3.setText(label3 + formatCoords(finalDeltaLat, finalDeltaLon));
                }
                textView4.setText(label4 + finalCount);
                DecimalFormat df = new DecimalFormat("###0.00");
                textView5.setText(label5 + df.format(finalAlt) + " +- " + df.format(finalDeltaAlt));
                loadMap(finalLat, finalLon, zoomFactor);
                Toast.makeText(MainActivity.this,
                        "Gennemsnit af " + finalCount + " positioner er beregnet.",
                        Toast.LENGTH_LONG).show();
                setShareIntent(shareTheResult());
            });
        });
    }

    private String formatCoords(double lat, double lon) {
        if (gps == null) return lat + ", " + lon;
        String format = prefs.getString("coordinateFormat", "DM");
        switch (format) {
            case "DD":  return gps.decimalToDD(lat, lon);
            case "DMS": return gps.decimalToDMS(lat, lon);
            default:    return gps.decimalToDM(lat, lon);
        }
    }

    private String readAsset(String filename) {
        try {
            java.io.InputStream is = getAssets().open(filename);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Could not read asset: " + filename, e);
            return "";
        }
    }

    private int getGoogleMapType() {
        switch (prefs.getString("mapType", "gm_normal")) {
            case "gm_satellite": return GoogleMap.MAP_TYPE_SATELLITE;
            case "gm_terrain":   return GoogleMap.MAP_TYPE_TERRAIN;
            case "gm_hybrid":    return GoogleMap.MAP_TYPE_HYBRID;
            default:             return GoogleMap.MAP_TYPE_NORMAL;
        }
    }

    private boolean isGoogleMapsType(String type) {
        return type.startsWith("gm_");
    }

    private void loadMap(double lat, double lon, int zoom) {
        mapInitialized = false;
        currentMapType = prefs.getString("mapType", "gm_normal");
        if (lat == 0 && lon == 0) { lat = 56.0; lon = 10.5; zoom = 7; }

        if (isGoogleMapsType(currentMapType)) {
            webView.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);
            updateGoogleMapFull(lat, lon, zoom);
            return;
        }

        // OpenStreetMap via Leaflet
        webView.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.GONE);
        if (leafletCssCache == null) leafletCssCache = readAsset("leaflet.css");
        if (leafletJsCache == null) leafletJsCache = readAsset("leaflet.js");

        String tileUrl, attribution;
        switch (currentMapType) {
            case "topo":
                tileUrl = "https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png";
                attribution = "&copy; OpenTopoMap";
                break;
            case "osm_satellite":
                tileUrl = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}";
                attribution = "&copy; Esri";
                break;
            default:
                tileUrl = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
                attribution = "&copy; OpenStreetMap";
                break;
        }

        StringBuilder pointsJs = new StringBuilder("var pts=[");
        for (double[] pt : measurementPoints) {
            pointsJs.append("[").append(pt[0]).append(",").append(pt[1]).append("],");
        }
        if (!measurementPoints.isEmpty()) pointsJs.deleteCharAt(pointsJs.length() - 1);
        pointsJs.append("];");

        String pinIconJs =
                "var pinIcon=L.divIcon({" +
                "html:'<div style=\"width:22px;height:30px;position:relative;\">" +
                "<div style=\"width:22px;height:22px;background:#2980b9;border:3px solid white;" +
                "border-radius:50% 50% 50% 0;transform:rotate(-45deg);box-shadow:0 2px 6px rgba(0,0,0,0.5);\"></div>" +
                "<div style=\"width:6px;height:6px;background:white;border-radius:50%;position:absolute;top:6px;left:5px;\"></div>" +
                "</div>'," +
                "iconSize:[22,30],iconAnchor:[11,30],className:''});";

        String html = "<!DOCTYPE html><html><head>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no'>" +
                "<style>" + leafletCssCache + "</style>" +
                "<style>html,body{margin:0;padding:0;height:100%;}#map{width:100%;height:100%;}</style>" +
                "</head><body><div id='map'></div>" +
                "<script>" + leafletJsCache + "</script>" +
                "<script>" +
                pinIconJs +
                "var map=L.map('map',{zoomControl:false}).setView([" + lat + "," + lon + "]," + zoom + ");" +
                "L.tileLayer('" + tileUrl + "',{maxZoom:19,attribution:'" + attribution + "'}).addTo(map);" +
                pointsJs +
                "var circles=[];" +
                "pts.forEach(function(p){var c=L.circleMarker(p,{radius:8,color:'#e74c3c',weight:2,fillColor:'#e74c3c',fillOpacity:0.4}).addTo(map);circles.push(c);});" +
                "var avgMarker=L.marker([" + lat + "," + lon + "],{icon:pinIcon}).addTo(map);" +
                "function addCircle(la,lo){var c=L.circleMarker([la,lo],{radius:8,color:'#e74c3c',weight:2,fillColor:'#e74c3c',fillOpacity:0.4}).addTo(map);circles.push(c);}" +
                "function updatePin(la,lo){if(avgMarker)map.removeLayer(avgMarker);avgMarker=L.marker([la,lo],{icon:pinIcon}).addTo(map);}" +
                "function panTo(la,lo){map.panTo([la,lo]);}" +
                "function clearMap(){circles.forEach(function(c){map.removeLayer(c);});circles=[];if(avgMarker){map.removeLayer(avgMarker);avgMarker=null;}}" +
                "</script></body></html>";
        webView.loadDataWithBaseURL("https://tile.openstreetmap.org/", html, "text/html", "UTF-8", null);
    }

    private void updateMapJS(double pointLat, double pointLon, double avgLat, double avgLon) {
        if (isGoogleMapsType(currentMapType)) {
            if (googleMap == null) return;
            Marker m = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pointLat, pointLon))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .anchor(0.5f, 0.5f));
            if (m != null) googleMeasurementMarkers.add(m);
            if (googleAverageMarker != null) googleAverageMarker.remove();
            googleAverageMarker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(avgLat, avgLon))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            if (followMap) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(avgLat, avgLon)));
            }
        } else {
            if (!mapInitialized) { loadMap(avgLat, avgLon, zoomFactor); return; }
            webView.evaluateJavascript("addCircle(" + pointLat + "," + pointLon + ");", null);
            webView.evaluateJavascript("updatePin(" + avgLat + "," + avgLon + ");", null);
            if (followMap) webView.evaluateJavascript("panTo(" + avgLat + "," + avgLon + ");", null);
        }
    }

    private void updateGoogleMapFull(double lat, double lon, int zoom) {
        if (googleMap == null) return;
        googleMap.setMapType(getGoogleMapType());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom));
        if (googleAverageMarker != null) { googleAverageMarker.remove(); googleAverageMarker = null; }
        for (Marker m : googleMeasurementMarkers) m.remove();
        googleMeasurementMarkers.clear();
        for (double[] pt : measurementPoints) {
            Marker m = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pt[0], pt[1]))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .anchor(0.5f, 0.5f));
            if (m != null) googleMeasurementMarkers.add(m);
        }
        googleAverageMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    public Intent shareTheResult() {
        String osmUrl = "https://www.openstreetmap.org/?mlat=" + averageLatitude +
                "&mlon=" + averageLongitude + "#map=" + zoomFactor + "/" + averageLatitude + "/" + averageLongitude;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String coords = formatCoords(averageLatitude, averageLongitude);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Gennemsnitsposition:\n" + coords + "\n\nOpenStreetMap: " + osmUrl);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Geocache Placer");
        sendIntent.setType("text/plain");
        return sendIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        getMenuInflater().inflate(R.menu.action_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (shareActionProvider != null) {
            shareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        }
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_settings) {
            startActivity(new Intent(this, PrefsActivity.class));
            return true;
        } else if (id == R.id.item_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("keepScreenOn".equals(key)) {
            applyScreenOnSetting();
        } else if ("mapType".equals(key)) {
            loadMap(averageLatitude, averageLongitude, zoomFactor);
        } else if ("coordinateFormat".equals(key)) {
            if (numberOfLocations > 0) {
                textView1.setText(label1 + formatCoords(latitude, longitude));
                textView2.setText(label2 + formatCoords(averageLatitude, averageLongitude));
                textView3.setText(label3 + formatCoords(deltaLatitude, deltaLongitude));
            } else {
                showCurrentPosition();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        if (gps != null) gps.stopUsingGPS();
        executor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
