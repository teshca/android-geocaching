package su.geocaching.android.ui.compass;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationProvider;
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
    private UserLocationManager locationManager;
    private LocationListener locationListener;
    private PreferencesManager preferenceManager;

    private CompassView compassView;
    private TextView tvOdometer, statusText, cacheCoordinates, userCoordinates;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnimation;
    private RelativeLayout odometerLayout;
    private Toast providerUnavailableToast;
    private ImageView startButton;

    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "on create");
        setContentView(R.layout.compass_activity);

        compassView = (CompassView) findViewById(R.id.compassView);
        tvOdometer = (TextView) findViewById(R.id.tvOdometer);
        cacheCoordinates = (TextView) findViewById(R.id.cacheCoordinates);
        userCoordinates = (TextView) findViewById(R.id.userCoordinates);
        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationManager.startExternalGpsStatusActivity(v.getContext());
            }
        });
        progressBarAnimation = (AnimationDrawable) progressBarView.getBackground();
        statusText = (TextView) findViewById(R.id.waitingLocationFixText);
        odometerLayout = (RelativeLayout) findViewById(R.id.odometer_layout);

        controller = Controller.getInstance();
        locationManager = controller.getLocationManager();
        preferenceManager = controller.getPreferencesManager();
        locationListener = new LocationListener(this);

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
        controller.getCompassManager().setUsingGpsCompass(preferenceManager.getCompassSensorPreference().endsWith("GPS"));

        GeoCache gc = controller.getSearchingGeoCache();
        cacheCoordinates.setText(CoordinateHelper.coordinateToString(gc.getLocationGeoPoint()));
        ((ImageView) findViewById(R.id.ivCacheCoordinate)).setImageResource(controller.getResourceManager().getMarkerResId(gc.getType(), gc.getStatus()));
        updateOdometer();


        providerUnavailableToast = Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG);
        runLogic();
        startAnimation();
    }

    /**
     * Run activity logic
     */
    private void runLogic() {
        if (locationManager.hasLocation()) {
            LogManager.d(TAG, "runLogic: has location. Update location with last known location");
            locationListener.updateLocation(locationManager.getLastKnownLocation());
            userCoordinates.setText(CoordinateHelper.coordinateToString(CoordinateHelper.locationToGeoPoint(locationManager.getLastKnownLocation())));
        }
        if (Controller.getInstance().getLocationManager().hasPreciseLocation()) {
            statusText.setVisibility(View.GONE);
            progressBarView.setVisibility(View.GONE);
        } else {
            statusText.setText(R.string.gps_status_initialization);
            progressBarView.setVisibility(View.VISIBLE);
        }

        locationManager.addSubscriber(locationListener, true);
        locationManager.enableBestProviderUpdates();
    }

    @Override
    protected void onPause() {
        LogManager.d(TAG, "onPause");
        locationManager.removeSubscriber(locationListener);
        stopAnimation();
        providerUnavailableToast.cancel();
        super.onPause();
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
        if (preferenceManager.getOdometerOnPreference()) {
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
        UserLocationManager lm = Controller.getInstance().getLocationManager();
        lm.refreshOdometer();
        boolean isOdometerOn = preferenceManager.getOdometerOnPreference();
        preferenceManager.setOdometerOnPreference(!isOdometerOn);
        lm.setUpdatingOdometer(!isOdometerOn);
        updateOdometer();
    }

    private void updateOdometer() {
        if (preferenceManager.getOdometerOnPreference()) {
            startButton = (ImageView) findViewById(R.id.startButton);
            odometerLayout.setVisibility(View.VISIBLE);
            tvOdometer.setText(CoordinateHelper.distanceToString(locationManager.getOdometerDistance()));
            toggleStartButton();
        } else {
            odometerLayout.setVisibility(View.GONE);
        }
    }

    private void toggleStartButton() {
        if (locationManager.isUpdatingOdometer()) {
            startButton.setImageResource(R.drawable.ic_pause);
        } else {
            startButton.setImageResource(R.drawable.ic_play);
        }
    }

    private void onBestProviderUnavailable() {
        UiHelper.setVisible(progressBarView);
        UiHelper.setVisible(statusText);
        statusText.setText(R.string.gps_status_unavailable);
        providerUnavailableToast.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (progressBarAnimation.isRunning()) {
            progressBarAnimation.stop();
        } else {
            progressBarAnimation.start();
        }
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActivity(this);
    }

    public void onStartStopOdometerClick(View v) {
        locationManager.setUpdatingOdometer(!locationManager.isUpdatingOdometer());
        toggleStartButton();
    }

    public void onRefreshOdometerClick(View v) {
        locationManager.refreshOdometer();
        tvOdometer.setText(CoordinateHelper.distanceToString(0));
    }

    public void onCloseOdometerClick(View v) {
        showHideOdometer();
    }

    class LocationListener implements ILocationAware {
        private final static float CLOSE_DISTANCE_TO_GC_VALUE = 100; // if we nearly than this distance in meters to geoCache - gps will be work maximal often

        private Activity activity;

        LocationListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void updateLocation(Location location) {
            locationManager.removeStatusListening(this);

            if (tvOdometer.isShown()) {
                tvOdometer.setText(CoordinateHelper.distanceToString(locationManager.getOdometerDistance()));
            }
            UiHelper.setGone(progressBarView);
            UiHelper.setGone(statusText);
            float distance = CoordinateHelper.getDistanceBetween(controller.getSearchingGeoCache().getLocationGeoPoint(), location);
            if (distance < CLOSE_DISTANCE_TO_GC_VALUE || locationManager.isUpdatingOdometer()) {
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
                case LocationProvider.OUT_OF_SERVICE:
                    onBestProviderUnavailable();
                    LogManager.d(TAG, "GpsStatus: out of service.");
                    break;
                case LocationProvider.AVAILABLE:
                    LogManager.d(TAG, "GpsStatus: available.");
                    String statusString = Controller.getInstance().getLocationManager().getSatellitesStatusString();
                    if (statusString != null) {
                        statusText.setText(statusString);
                        UiHelper.setVisible(progressBarView);
                        UiHelper.setVisible(statusText);
                    }
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            LogManager.d(TAG, "onProviderDisabled provider: " + provider);
            if (!locationManager.isBestProviderEnabled()) {
                LogManager.d(TAG, "onStatusChanged: best provider (" + locationManager.getBestProvider(false) + ") disabled. Ask turn on.");
                onBestProviderUnavailable();
                NavigationManager.displayTurnOnGpsDialog(activity);
            }
        }
    }
}
