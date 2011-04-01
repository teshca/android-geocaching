package su.geocaching.android.ui.compass;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GeoCacheLocationManager;
import su.geocaching.android.controller.GpsStatusManager;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.controller.IGpsStatusAware;
import su.geocaching.android.controller.ILocationAware;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.PreferencesManager;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.compass.CompassSpeed;
import su.geocaching.android.controller.compass.SmoothCompassThread;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.GpsHelper;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Search GeoCache with the compass.
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class SearchGeoCacheCompass extends Activity {
    private static final String TAG = SearchGeoCacheCompass.class.getCanonicalName();

    private SmoothCompassThread animationThread;
    private GeoCacheLocationManager locationManager;
    private LocationListener locationListener;
    private GpsStatusManager gpsManager;
    private GpsStatusListener gpsListener;
    private PreferencesManager preferenceManager;

    private CompassView compassView;
    private TextView tvOdometer, statusText, targetCoordinates, currentCoordinates;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnim;
    private LinearLayout odometerlayout;

    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "on create");
        setContentView(R.layout.search_geocache_compass);

        compassView = (CompassView) findViewById(R.id.compassView);
        tvOdometer = (TextView) findViewById(R.id.tvOdometer);
        targetCoordinates = (TextView) findViewById(R.id.targetCoordinates);
        currentCoordinates = (TextView) findViewById(R.id.currentCoordinates);
        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnim = (AnimationDrawable) progressBarView.getBackground();
        statusText = (TextView) findViewById(R.id.waitingLocationFixText);
        odometerlayout = (LinearLayout) findViewById(R.id.odometer_layout);

        controller = Controller.getInstance();
        locationManager = controller.getLocationManager();
        gpsManager = controller.getGpsStatusManager();
        preferenceManager = controller.getPreferencesManager();
        locationListener = new LocationListener(this);
        gpsListener = new GpsStatusListener();

        GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.compass_activity_folder));
        tracker.dispatch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManager.d(TAG, "onResume");
        compassView.setKeepScreenOn(preferenceManager.getKeepScreenOnPreference());
        targetCoordinates.setText(GpsHelper.coordinateToString(controller.getSearchingGeoCache().getLocationGeoPoint()));
        if (locationManager.hasLocation()) {
            currentCoordinates.setText(GpsHelper.coordinateToString(GpsHelper.locationToGeoPoint(locationManager.getLastKnownLocation())));
        }
        if (preferenceManager.getOdometerOnPreference()) {
            odometerlayout.setVisibility(View.VISIBLE);
        } else {
            odometerlayout.setVisibility(View.GONE);
        }

        runLogic();
        startAnimation();
    }

    /**
     * Run activity logic
     */
    private void runLogic() {
        if (controller.getSearchingGeoCache() == null) {
            LogManager.e(TAG, "runLogic: null geocache. Finishing.");
            Toast.makeText(this, this.getString(R.string.search_geocache_error_no_geocache), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Save last searched geocache
        // controller.setLastSearchedGeoCache(geoCache, this);

        if (locationManager.hasLocation()) {
            LogManager.d(TAG, "runLogic: location fixed. Update location with last known location");
            locationListener.updateLocation(locationManager.getLastKnownLocation());
            progressBarView.setVisibility(View.GONE);
        } else {
            LogManager.d(TAG, "run logic: location not fixed. Show gps status");
            onBestProviderUnavailable();
            progressBarView.setVisibility(View.VISIBLE);
        }

        locationManager.addSubscriber(locationListener);
        locationManager.enableBestProviderUpdates();
        gpsManager.addSubscriber(gpsListener);
    }

    @Override
    protected void onPause() {
        LogManager.d(TAG, "onPause");
        locationManager.removeSubscriber(locationListener);
        gpsManager.removeSubscriber(gpsListener);
        stopAnimation();
        super.onPause();
    }

    private void startAnimation() {
        if (animationThread == null) {
            animationThread = new SmoothCompassThread(compassView);
            animationThread.setRunning(true);

            animationThread.setSpeed(CompassSpeed.valueOf(preferenceManager.getCompassSpeed()));
            compassView.setHelper(preferenceManager.getCompassAppearence());
            animationThread.start();
        }
    }

    private void stopAnimation() {
        if (animationThread != null) {
            animationThread.setRunning(false);
            try {
                animationThread.join(150);
            } catch (InterruptedException ignored) {
            }
            animationThread = null;
        }
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_geocache_compass, menu);
        return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        GeoCache searchingGC = controller.getPreferencesManager().getLastSearchedGeoCache();
        switch (item.getItemId()) {
            case R.id.menuStartMap:
                UiHelper.startSearchMapActivity(this, searchingGC);
                return true;
            case R.id.menuGeoCacheInfo:
                UiHelper.showGeoCacheInfo(this, searchingGC);
                return true;
            case R.id.stepByStep:
                UiHelper.startCheckpointsFolder(this, searchingGC.getId());
                return true;
            case R.id.compassSettings:
                showCompassPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showCompassPreferences() {
        stopAnimation();
        Intent intent = new Intent(this, CompassPreferenceActivity.class);
        startActivity(intent);
    }

    private void onBestProviderUnavailable() {
        if (progressBarView.getVisibility() == View.GONE) {
            progressBarView.setVisibility(View.VISIBLE);
        }
        gpsListener.updateStatus(getString(R.string.waiting_location_fix_message));
        Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (progressBarAnim.isRunning()) {
            progressBarAnim.start();
        } else {
            progressBarAnim.stop();
        }
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }

    private float odometeDistance;
    private Location lastLocation;

    /**
     *
     */
    class LocationListener implements ILocationAware {
        private final static float CLOSE_DISTANCE_TO_GC_VALUE = 100; // if we nearly than this distance in meters to geocache - gps will be work maximal often

        Activity activity;

        LocationListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void updateLocation(Location location) {

            if (lastLocation != null) {
                odometeDistance += GpsHelper.getDistanceBetween(location, lastLocation);
                tvOdometer.setText(GpsHelper.distanceToString(odometeDistance));
            }
            lastLocation = location;

            if (progressBarView.getVisibility() == View.VISIBLE) {
                progressBarView.setVisibility(View.GONE);
            }
            statusText.setText(GpsHelper.distanceToString(GpsHelper.getDistanceBetween(controller.getSearchingGeoCache().getLocationGeoPoint(), location)));
            statusText.setTextSize(getResources().getDimension(R.dimen.text_size_big));
            if (GpsHelper.getDistanceBetween(location, controller.getSearchingGeoCache().getLocationGeoPoint()) < CLOSE_DISTANCE_TO_GC_VALUE) {
                // TODO: may be need make special preference?
                controller.getLocationManager().updateFrequency(GpsUpdateFrequency.MAXIMAL);
            } else {
                controller.getLocationManager().updateFrequencyFromPreferences();
            }
            compassView.setCacheDirection(GpsHelper.getBearingBetween(location, controller.getSearchingGeoCache().getLocationGeoPoint()));
            currentCoordinates.setText(GpsHelper.coordinateToString(GpsHelper.locationToGeoPoint(location)));
            compassView.setDistance(GpsHelper.getDistanceBetween(location, controller.getSearchingGeoCache().getLocationGeoPoint()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // it is only write message to log and call onBestProviderUnavailable when gps out_of_service
            LogManager.d(TAG, "onStatusChanged:");
            String statusString = "Location fixed: " + Boolean.toString(controller.getLocationManager().hasLocation()) + ". Provider: " + provider + ". ";
            LogManager.d(TAG, "     " + statusString);
            switch (status) {

                case LocationProvider.OUT_OF_SERVICE:
                    statusString += "Status: out of service. ";
                    onBestProviderUnavailable();
                    LogManager.d(TAG, "     Status: out of service.");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString += "Status: temporarily unavailable. ";
                    // TODO: check when it happens. i'm almost sure that it call then gps 'go to sleep'(power saving)
                    // onBestProviderUnavailable();
                    LogManager.d(TAG, "     Status: temporarily unavailable.");
                    break;
                case LocationProvider.AVAILABLE:
                    statusString += "Status: available. ";
                    LogManager.d(TAG, "     Status: available.");
                    break;
            }
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                statusString += "Satellites: " + Integer.toString(extras.getInt("satellites"));
                LogManager.d(TAG, "     Satellites: " + Integer.toString(extras.getInt("satellites")));
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            LogManager.d(TAG, "onProviderDisabled provider: " + provider);
            if (!locationManager.isBestProviderEnabled()) {
                LogManager.d(TAG, "onStatusChanged: best provider (" + locationManager.getBestProvider() + ") disabled. Ask turn on.");
                onBestProviderUnavailable();
                UiHelper.askTurnOnGps(activity);
            }
        }
    }

    /**
     *
     */
    class GpsStatusListener implements IGpsStatusAware {

        @Override
        public void updateStatus(String status) {
            compassView.setLocationFix(locationManager.hasLocation());

            if (!locationManager.hasLocation()) {
                statusText.setText(status);
            }
        }
    }
}
