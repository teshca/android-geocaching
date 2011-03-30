package su.geocaching.android.ui.compass;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GeoCacheLocationManager;
import su.geocaching.android.controller.GpsStatusManager;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.controller.IGpsStatusAware;
import su.geocaching.android.controller.ILocationAware;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.compass.CompassPreferenceManager;
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

    private CompassView compassView;
    private TextView statusText, targetCoordinates, currentCoordinates;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnim;

    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "on create");
        setContentView(R.layout.search_geocache_compass);

        compassView = (CompassView) findViewById(R.id.compassView);
        // distanceToCache = (TextView) findViewById(R.id.DistanceValue);
        targetCoordinates = (TextView) findViewById(R.id.targetCoordinates);
        currentCoordinates = (TextView) findViewById(R.id.currentCoordinates);
        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnim = (AnimationDrawable) progressBarView.getBackground();
        statusText = (TextView) findViewById(R.id.waitingLocationFixText);

        controller = Controller.getInstance();
        locationManager = controller.getLocationManager();
        gpsManager = controller.getGpsStatusManager();
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
        compassView.setKeepScreenOn(Controller.getInstance().getPreferencesManager().getKeepScreenOnPreference());
        targetCoordinates.setText(GpsHelper.coordinateToString(controller.getSearchingGeoCache().getLocationGeoPoint()));
        if (locationManager.hasLocation()) {
            currentCoordinates.setText(GpsHelper.coordinateToString(GpsHelper.locationToGeoPoint(locationManager.getLastKnownLocation())));
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

            CompassPreferenceManager preferManager = CompassPreferenceManager.getPreference(this);
            String speed = preferManager.getString(CompassPreferenceManager.PREFS_COMPASS_SPEED_KEY, getString(R.string.prefer_speed_default_value));
            animationThread.setSpeed(CompassSpeed.valueOf(speed));

            String appearance = preferManager.getString(CompassPreferenceManager.PREFS_COMPASS_APPEARENCE_KEY, getString(R.string.prefer_appearance_default_value));
            compassView.setHelper(appearance);
            animationThread.start();
        }
    }

    private void stopAnimation() {
        if (animationThread != null) {
            animationThread.setRunning(false);
            try {
                animationThread.join(150); // TODO Is it need?
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

    // TODO check it
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!progressBarAnim.isRunning()) {
            progressBarAnim.start();
        } else {
            progressBarAnim.stop();
        }
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }

    /**
     *
     */
    class LocationListener implements ILocationAware {
        private final static float CLOSE_DISTANCE_TO_GC_VALUE = 30; // if we nearly than this distance in meters to geocache - gps will be work maximal often

        Activity activity;

        LocationListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void updateLocation(Location location) {
            // TODO is it need?
            // if (!locationManager.hasLocation()) {
            // return;
            // }
            if (progressBarView.getVisibility() == View.VISIBLE) {
                progressBarView.setVisibility(View.GONE);
            }
            statusText.setText(GpsHelper.distanceToString(GpsHelper.getDistanceBetween(Controller.getInstance().getSearchingGeoCache().getLocationGeoPoint(), location)));
            if (GpsHelper.getDistanceBetween(location, Controller.getInstance().getSearchingGeoCache().getLocationGeoPoint()) < CLOSE_DISTANCE_TO_GC_VALUE) {
                // TODO: may be need make special preference?
                Controller.getInstance().getLocationManager().updateFrequency(GpsUpdateFrequency.MAXIMAL);
            } else {
                Controller.getInstance().getLocationManager().updateFrequencyFromPreferences();
            }
            compassView.setCacheDirection(GpsHelper.getBearingBetween(location, controller.getSearchingGeoCache().getLocationGeoPoint()));
            currentCoordinates.setText(GpsHelper.coordinateToString(GpsHelper.locationToGeoPoint(location)));
            compassView.setDistance(GpsHelper.getDistanceBetween(location, controller.getSearchingGeoCache().getLocationGeoPoint()));
        }

        /*
         * (non-Javadoc)
         * 
         * @see su.geocaching.android.controller.ILocationAware#onStatusChanged(java.lang.String, int, android.os.Bundle)
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // it is only write message to log and call onBestProviderUnavailable when gps out_of_service
            LogManager.d(TAG, "onStatusChanged:");
            String statusString = "Location fixed: " + Boolean.toString(Controller.getInstance().getLocationManager().hasLocation()) + ". Provider: " + provider + ". ";
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
