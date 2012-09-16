package su.geocaching.android.ui.compass;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.controller.compass.CompassSpeed;
import su.geocaching.android.controller.compass.SmoothCompassThread;
import su.geocaching.android.controller.managers.*;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.utils.UiHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.preferences.CompassPreferenceActivity;

/**
 * Search GeoCache with the compass.
 *
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class CompassActivity extends SherlockActivity {
    private static final String TAG = CompassActivity.class.getCanonicalName();
    private static final String COMPASS_ACTIVITY_NAME = "/CompassActivity";
    private static final int DIALOG_ID_TURN_ON_GPS = 1000;

    private SmoothCompassThread animationThread;
    private AccurateUserLocationManager locationManager;
    private LocationListener locationListener;
    private PreferencesManager preferenceManager;

    private CompassView compassView;
    private TextView tvOdometer, statusText, cacheCoordinates, userCoordinates;
    private ImageView cacheIcon;
    private ProgressBar progressBarCircle;
    private RelativeLayout odometerLayout;
    private Toast providerUnavailableToast;
    private ImageView startButton;

    private GeoCache geoCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "on create");

        geoCache = (GeoCache) getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(geoCache.getName());

        setContentView(R.layout.compass_activity);
        compassView = (CompassView) findViewById(R.id.compassView);
        tvOdometer = (TextView) findViewById(R.id.tvOdometer);
        cacheCoordinates = (TextView) findViewById(R.id.cacheCoordinates);
        cacheIcon = (ImageView) findViewById(R.id.ivCacheCoordinate);
        userCoordinates = (TextView) findViewById(R.id.userCoordinates);
        progressBarCircle = (ProgressBar) findViewById(R.id.progressCircle);

        statusText = (TextView) findViewById(R.id.waitingLocationFixText);
        odometerLayout = (RelativeLayout) findViewById(R.id.odometer_layout);
        startButton = (ImageView) findViewById(R.id.startButton);
        providerUnavailableToast = Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG);

        locationManager = Controller.getInstance().getLocationManager();
        preferenceManager = Controller.getInstance().getPreferencesManager();
        locationListener = new LocationListener();

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(COMPASS_ACTIVITY_NAME);
    }


    @Override
    protected void onResume() {
        super.onResume();
        LogManager.d(TAG, "onResume");

        if (!Controller.getInstance().getDbManager().isCacheStored(geoCache.getId())) {
            LogManager.e(TAG, "Geocache is not in found in database. Finishing.");
            Toast.makeText(this, this.getString(R.string.search_geocache_error_geocache_not_in_db), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        compassView.setHelper(preferenceManager.getCompassAppearance());
        compassView.setKeepScreenOn(preferenceManager.getKeepScreenOnPreference());

        GeoCache currentSearchPoint = Controller.getInstance().getCurrentSearchPoint();
        if (currentSearchPoint.getType() == GeoCacheType.CHECKPOINT) {
            getSupportActionBar().setSubtitle(currentSearchPoint.getName());
        }
        cacheCoordinates.setText(CoordinateHelper.coordinateToString(currentSearchPoint.getLocationGeoPoint()));
        cacheIcon.setImageResource(Controller.getInstance().getResourceManager().getMarkerResId(currentSearchPoint.getType(), currentSearchPoint.getStatus()));
        updateOdometer();

        if (locationManager.hasLocation()) {
            LogManager.d(TAG, "runLogic: has location. Update location with last known location");
            locationListener.updateLocation(locationManager.getLastKnownLocation());
            userCoordinates.setText(CoordinateHelper.coordinateToString(CoordinateHelper.locationToGeoPoint(locationManager.getLastKnownLocation())));
        }
        if (Controller.getInstance().getLocationManager().hasPreciseLocation()) {
            progressBarCircle.setVisibility(View.GONE);
        } else {
            statusText.setText(R.string.gps_status_initialization);
            progressBarCircle.setVisibility(View.VISIBLE);
        }

        locationManager.addSubscriber(locationListener);
        startAnimation();
    }

    @Override
    protected void onPause() {
        LogManager.d(TAG, "onPause");
        locationManager.removeSubscriber(locationListener);
        stopAnimation();
        providerUnavailableToast.cancel();
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        locationManager.checkSubscribers();
    }

    private void startAnimation() {
        if (animationThread == null) {
            animationThread = new SmoothCompassThread(compassView);
            animationThread.setRunning(true);

            animationThread.setSpeed(CompassSpeed.valueOf(preferenceManager.getCompassSpeed()));
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
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.compass_menu, menu);
        return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavigationManager.startDashboardActivity(this);
                return true;
            case R.id.menuStartMap:
                NavigationManager.startSearchMapActivity(this, geoCache);
                return true;
            case R.id.menuGeoCacheInfo:
                NavigationManager.startInfoActivity(this, geoCache);
                return true;
            case R.id.stepByStep:
                NavigationManager.startCheckpointsFolder(this, geoCache);
                return true;
            case R.id.compassSettings:
                showCompassPreferences();
                return true;
            case R.id.compassOdometer:
                toggleOdometerVisibility();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (preferenceManager.isOdometerOnPreference()) {
            menu.findItem(R.id.compassOdometer).setTitle(R.string.menu_compass_odometer_hide);
        } else {
            menu.findItem(R.id.compassOdometer).setTitle(R.string.menu_compass_odometer_show);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void showCompassPreferences() {
        stopAnimation();
        Intent intent = new Intent(this, CompassPreferenceActivity.class);
        startActivity(intent);
    }

    private void toggleOdometerVisibility() {
        AccurateUserLocationManager.Odometer.refresh();
        boolean isOdometerOn = preferenceManager.isOdometerOnPreference();
        preferenceManager.setOdometerOnPreference(!isOdometerOn);
        AccurateUserLocationManager.Odometer.setEnabled(!isOdometerOn);
        updateOdometer();
        invalidateOptionsMenu();
    }

    private void updateOdometer() {
        if (preferenceManager.isOdometerOnPreference()) {
            odometerLayout.setVisibility(View.VISIBLE);
            tvOdometer.setText(CoordinateHelper.distanceToString(AccurateUserLocationManager.Odometer.getDistance()));
            toggleStartButton();
        } else {
            odometerLayout.setVisibility(View.GONE);
        }
    }

    private void toggleStartButton() {
        if (AccurateUserLocationManager.Odometer.isEnabled()) {
            startButton.setImageResource(R.drawable.ic_pause);
        } else {
            startButton.setImageResource(R.drawable.ic_play);
        }
    }

    public void onStartStopOdometerClick(View v) {
        AccurateUserLocationManager.Odometer.setEnabled(!AccurateUserLocationManager.Odometer.isEnabled());
        toggleStartButton();
    }

    public void onRefreshOdometerClick(View v) {
        AccurateUserLocationManager.Odometer.refresh();
        tvOdometer.setText(CoordinateHelper.distanceToString(0));
    }

    public void onCloseOdometerClick(View v) {
        toggleOdometerVisibility();
    }

    public void StartGpsStatusActivity(View v) {
        NavigationManager.startExternalGpsStatusActivity(v.getContext());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ID_TURN_ON_GPS:
                return NavigationManager.createTurnOnGpsDialog(this);
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_ID_TURN_ON_GPS:
                Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch("/EnableGpsDialog");
                break;
        }
        super.onPrepareDialog(id, dialog);
    }

    class LocationListener implements ILocationAware {
        private final static float CLOSE_DISTANCE_TO_GC_VALUE = 100; // if we nearly than this distance in meters to geoCache - gps will be work maximal often

        @Override
        public void updateLocation(Location location) {
            if (tvOdometer.isShown()) {
                tvOdometer.setText(CoordinateHelper.distanceToString(AccurateUserLocationManager.Odometer.getDistance()));
            }
            UiHelper.setGone(progressBarCircle);
            float distance = CoordinateHelper.getDistanceBetween(Controller.getInstance().getCurrentSearchPoint().getLocationGeoPoint(), location);
            if (distance < CLOSE_DISTANCE_TO_GC_VALUE || AccurateUserLocationManager.Odometer.isEnabled()) {
                Controller.getInstance().getLocationManager().updateFrequency(GpsUpdateFrequency.MAXIMAL);
            } else {
                Controller.getInstance().getLocationManager().updateFrequencyFromPreferences();
            }
            compassView.setCacheDirection(CoordinateHelper.getBearingBetween(location, Controller.getInstance().getCurrentSearchPoint().getLocationGeoPoint()));
            userCoordinates.setText(CoordinateHelper.coordinateToString(CoordinateHelper.locationToGeoPoint(location)));
            compassView.setDistance(distance);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case AccurateUserLocationManager.GPS_EVENT_SATELLITE_STATUS:
                    // just update status
                    statusText.setText(Controller.getInstance().getLocationManager().getSatellitesStatusString());
                    break;
                case AccurateUserLocationManager.OUT_OF_SERVICE:
                    // provider unavailable
                    UiHelper.setVisible(progressBarCircle);
                    statusText.setText(R.string.gps_status_unavailable);
                    providerUnavailableToast.show();
                    break;
                case AccurateUserLocationManager.TEMPORARILY_UNAVAILABLE:
                    // gps connection lost. just show progress bar
                    UiHelper.setVisible(progressBarCircle);
                    break;
                case AccurateUserLocationManager.EVENT_PROVIDER_DISABLED:
                    if (LocationManager.GPS_PROVIDER.equals(provider)) {
                        // gps has been turned off
                        showDialog(DIALOG_ID_TURN_ON_GPS);
                        UiHelper.setGone(progressBarCircle);
                        UiHelper.setGone(statusText);
                    }
                    break;
                case AccurateUserLocationManager.EVENT_PROVIDER_ENABLED:
                    if (LocationManager.GPS_PROVIDER.equals(provider)) {
                        try {
                            // gps has been turned on
                            dismissDialog(DIALOG_ID_TURN_ON_GPS);
                        } catch (Exception e) {
                            LogManager.w(TAG, "Can't dismiss dialog, probably it hasn't ever been shown", e);
                        }
                        UiHelper.setVisible(progressBarCircle);
                        UiHelper.setVisible(statusText);
                    }
                    break;
            }
        }
    }
}
