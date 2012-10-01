package su.geocaching.android.ui.compass;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.controller.compass.CompassSpeed;
import su.geocaching.android.controller.compass.SmoothCompassThread;
import su.geocaching.android.controller.managers.*;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.utils.UiHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.ProgressBarView;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.preferences.CompassPreferenceActivity;

/**
 * Search GeoCache with the compass.
 *
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class CompassActivity extends Activity {
    private static final String TAG = CompassActivity.class.getCanonicalName();
    private static final String COMPASS_ACTIVITY = "/CompassActivity";

    private SmoothCompassThread animationThread;
    private AccurateUserLocationManager locationManager;
    private LocationListener locationListener;
    private PreferencesManager preferenceManager;

    private CompassView compassView;
    private TextView tvOdometer, statusText, cacheCoordinates, userCoordinates;
    private ProgressBarView progressBarView;
    private RelativeLayout odometerLayout;
    private Toast providerUnavailableToast;
    private ImageView startButton;

    private Controller controller;
    private static final int DIALOG_ID_TURN_ON_GPS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "on create");
        setContentView(R.layout.compass_activity);

        compassView = (CompassView) findViewById(R.id.compassView);
        tvOdometer = (TextView) findViewById(R.id.tvOdometer);
        cacheCoordinates = (TextView) findViewById(R.id.cacheCoordinates);
        userCoordinates = (TextView) findViewById(R.id.userCoordinates);
        progressBarView = (ProgressBarView) findViewById(R.id.progressCircle);
        progressBarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationManager.startExternalGpsStatusActivity(v.getContext());
            }
        });
        statusText = (TextView) findViewById(R.id.waitingLocationFixText);
        odometerLayout = (RelativeLayout) findViewById(R.id.odometer_layout);
        providerUnavailableToast = Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG);

        controller = Controller.getInstance();
        locationManager = controller.getLocationManager();
        preferenceManager = controller.getPreferencesManager();
        locationListener = new LocationListener();

        controller.getGoogleAnalyticsManager().trackActivityLaunch(COMPASS_ACTIVITY);
    }


    @Override
    protected void onResume() {
        super.onResume();
        LogManager.d(TAG, "onResume");
        if (controller.getSearchingGeoCache() == null) {
            LogManager.e(TAG, "Geocache is null. Finishing.");
            Toast.makeText(this, this.getString(R.string.search_geocache_error_no_geocache), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        compassView.setHelper(preferenceManager.getCompassAppearance());
        compassView.setKeepScreenOn(preferenceManager.getKeepScreenOnPreference());

        GeoCache gc = controller.getSearchingGeoCache();
        cacheCoordinates.setText(CoordinateHelper.coordinateToString(gc.getLocationGeoPoint()));
        ((ImageView) findViewById(R.id.ivCacheCoordinate)).setImageResource(controller.getResourceManager().getMarkerResId(gc.getType(), gc.getStatus()));
        updateOdometer();

        if (locationManager.hasLocation()) {
            LogManager.d(TAG, "runLogic: has location. Update location with last known location");
            locationListener.updateLocation(locationManager.getLastKnownLocation());
            userCoordinates.setText(CoordinateHelper.coordinateToString(CoordinateHelper.locationToGeoPoint(locationManager.getLastKnownLocation())));
        }
        if (Controller.getInstance().getLocationManager().hasPreciseLocation()) {
            progressBarView.setVisibility(View.GONE);
        } else {
            statusText.setText(R.string.gps_status_initialization);
            progressBarView.setVisibility(View.VISIBLE);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compass_menu, menu);
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
                NavigationManager.startSearchMapActivity(this, searchingGC);
                return true;
            case R.id.menuGeoCacheInfo:
                NavigationManager.startInfoActivity(this, searchingGC);
                return true;
            case R.id.stepByStep:
                NavigationManager.startCheckpointsFolder(this, searchingGC.getId());
                return true;
            case R.id.compassSettings:
                showCompassPreferences();
                return true;
            case R.id.compassOdometer:
                showHideOdometer();
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

    private void showHideOdometer() {
        AccurateUserLocationManager.Odometer.refresh();
        boolean isOdometerOn = preferenceManager.isOdometerOnPreference();
        preferenceManager.setOdometerOnPreference(!isOdometerOn);
        AccurateUserLocationManager.Odometer.setEnabled(!isOdometerOn);
        updateOdometer();
    }

    private void updateOdometer() {
        if (preferenceManager.isOdometerOnPreference()) {
            startButton = (ImageView) findViewById(R.id.startButton);
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

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActivity(this);
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
        showHideOdometer();
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
            UiHelper.setGone(progressBarView);
            float distance = CoordinateHelper.getDistanceBetween(controller.getSearchingGeoCache().getLocationGeoPoint(), location);
            if (distance < CLOSE_DISTANCE_TO_GC_VALUE || AccurateUserLocationManager.Odometer.isEnabled()) {
                controller.getLocationManager().updateFrequency(GpsUpdateFrequency.MAXIMAL);
            } else {
                controller.getLocationManager().updateFrequencyFromPreferences();
            }
            compassView.setCacheDirection(CoordinateHelper.getBearingBetween(location, controller.getSearchingGeoCache().getLocationGeoPoint()));
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
                    UiHelper.setVisible(progressBarView);
                    statusText.setText(R.string.gps_status_unavailable);
                    providerUnavailableToast.show();
                    break;
                case AccurateUserLocationManager.TEMPORARILY_UNAVAILABLE:
                    // gps connection lost. just show progress bar
                    UiHelper.setVisible(progressBarView);
                    break;
                case AccurateUserLocationManager.EVENT_PROVIDER_DISABLED:
                    if (LocationManager.GPS_PROVIDER.equals(provider)) {
                        // gps has been turned off
                        showDialog(DIALOG_ID_TURN_ON_GPS);
                        UiHelper.setGone(progressBarView);
                        UiHelper.setGone(statusText);
                    }
                    break;
                case AccurateUserLocationManager.EVENT_PROVIDER_ENABLED:
                    if (LocationManager.GPS_PROVIDER.equals(provider)) {
                        // gps has been turned on
                        dismissDialog(DIALOG_ID_TURN_ON_GPS);
                        UiHelper.setVisible(progressBarView);
                        UiHelper.setVisible(statusText);
                    }
                    break;
            }
        }
    }
}