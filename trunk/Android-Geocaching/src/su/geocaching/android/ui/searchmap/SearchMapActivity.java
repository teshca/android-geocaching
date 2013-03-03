package su.geocaching.android.ui.searchmap;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.controller.compass.SmoothCompassThread;
import su.geocaching.android.controller.managers.*;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.utils.UiHelper;
import su.geocaching.android.model.*;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.map.GeocacheMarkerTapListener;
import su.geocaching.android.ui.map.LocationMarkerTapListener;
import su.geocaching.android.ui.map.MapLongClickListener;
import su.geocaching.android.ui.preferences.DashboardPreferenceActivity;

/**
 * Search GeoCache with the map
 *
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class SearchMapActivity extends SherlockFragmentActivity
        implements IConnectionAware, ILocationAware, android.os.Handler.Callback {
    private final static String TAG = SearchMapActivity.class.getCanonicalName();
    private final static float CLOSE_DISTANCE_TO_GC_VALUE = 100; // if we nearly than this distance in meters to geocache - gps will be work maximal often
    private final static String SEARCH_MAP_ACTIVITY_NAME = "/SearchMapActivity";

    private static final int DIALOG_ID_TURN_ON_GPS = 1000;

    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap googleMap;
    private ISearchMapWrapper mapWrapper;

    private TextView gpsStatusTextView;
    private TextView distanceStatusTextView;
    private ProgressBar progressBarCircle;
    private Toast providerUnavailableToast;
    private Toast connectionLostToast;
    private Toast statusNullLastLocationToast;

    private SmoothCompassThread animationThread;

    private GeoCache geoCache;

    // handler associated with this activity
    private Handler handler;

    /*
     * (non-Javadoc)
     *
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");

        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.search_map_activity);

        geoCache = (GeoCache) getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(geoCache.getName());

        providerUnavailableToast = Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG);
        connectionLostToast = Toast.makeText(this, getString(R.string.map_internet_lost), Toast.LENGTH_LONG);

        gpsStatusTextView = (TextView) findViewById(R.id.waitingLocationFixText);
        distanceStatusTextView = (TextView) findViewById(R.id.distanceToCacheText);
        progressBarCircle = (ProgressBar) findViewById(R.id.progressCircle);

        setUpMapIfNeeded();

        Controller.getInstance().getPreferencesManager().setLastSearchedGeoCache(geoCache);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(SEARCH_MAP_ACTIVITY_NAME);

        handler = new Handler(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.android.maps.MapActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        LogManager.d(TAG, "onPause");
        saveMapInfoToSettings();

        if (Controller.getInstance().getLocationManager().hasLocation()) {
            stopCompassAnimation();
        }

        Controller.getInstance().getConnectionManager().removeSubscriber(this);
        Controller.getInstance().getLocationManager().removeSubscriber(this);
        Controller.getInstance().getCallbackManager().removeSubscriber(handler);
        providerUnavailableToast.cancel();
        connectionLostToast.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManager.d(TAG, "onResume");

        if (!Controller.getInstance().getDbManager().isCacheStored(geoCache.getId())) {
            LogManager.e(TAG, "Geocache is not in found in database. Finishing.");
            Toast.makeText(this, getString(R.string.search_geocache_error_geocache_not_in_db), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Controller.getInstance().setCurrentSearchPoint(geoCache);
        getSupportActionBar().setSubtitle(null);

        setUpMapIfNeeded();

        boolean keepScreenOn = Controller.getInstance().getPreferencesManager().getKeepScreenOnPreference();
        this.mapFragment.getView().setKeepScreenOn(keepScreenOn);
        updateMapInfoFromSettings();
        mapWrapper.updateMapLayer();
        mapWrapper.clearGeocacheMarkers();
        mapWrapper.setSearchGeocache(geoCache);

        for (GeoCache checkpoint : Controller.getInstance().getCheckpointManager(geoCache.getId()).getCheckpoints()) {
            mapWrapper.addCheckpointMarker(checkpoint);
            if (checkpoint.getStatus() == GeoCacheStatus.ACTIVE_CHECKPOINT) {
                Controller.getInstance().setCurrentSearchPoint(checkpoint);
                getSupportActionBar().setSubtitle(checkpoint.getName());
            }
        }

        if (Controller.getInstance().getLocationManager().hasLocation()) {
            LogManager.d(TAG, "Update location with last known location");
            // this update will hide progressBarView
            updateLocation(Controller.getInstance().getLocationManager().getLastKnownLocation());
            startCompassAnimation();
        }

        if (!Controller.getInstance().getLocationManager().isBestProviderEnabled()) {
            showDialog(DIALOG_ID_TURN_ON_GPS);
            hideProgressBarCircle();
            UiHelper.hide(gpsStatusTextView);
        } else {
            if (Controller.getInstance().getLocationManager().hasPreciseLocation()) {
                hideProgressBarCircle();
            } else {
                gpsStatusTextView.setText(R.string.gps_status_initialization);
                showProgressBarCircle();
            }
        }

        Controller.getInstance().getLocationManager().addSubscriber(this);
        Controller.getInstance().getConnectionManager().addSubscriber(this);
        Controller.getInstance().getCallbackManager().addSubscriber(handler);

        if (!Controller.getInstance().getConnectionManager().isActiveNetworkConnected()) {
            onConnectionLost();
            LogManager.w(TAG, "internet not connected");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Controller.getInstance().getLocationManager().checkSubscribers();
    }

    /*
     * (non-Javadoc)
     *
     * @see su.geocaching.android.controller.ILocationAware#updateLocation(android.location.Location)
     */
    @Override
    public void updateLocation(Location userLocation) {
        LogManager.d(TAG, "update location");
        hideProgressBarCircle();
        final GeoPoint cachePosition = Controller.getInstance().getCurrentSearchPoint().getGeoPoint();
        final float distance = CoordinateHelper.getDistanceBetween(cachePosition, userLocation);
        if (distance < CLOSE_DISTANCE_TO_GC_VALUE) {
            Controller.getInstance().getLocationManager().updateFrequency(GpsUpdateFrequency.MAXIMAL);
        } else {
            Controller.getInstance().getLocationManager().updateFrequencyFromPreferences();
        }
        boolean isPrecise = Controller.getInstance().getLocationManager().hasPreciseLocation();
        distanceStatusTextView.setText(CoordinateHelper.distanceToString(distance, isPrecise));

        mapWrapper.updateLocationMarker(userLocation, isPrecise);
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.search_map_menu, menu);
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
            case R.id.menu_mylocation:
                onMyLocationClick();
                return true;
            case R.id.menuDefaultZoom:
                resetZoom(true);
                return true;
            case R.id.menuStartCompass:
                NavigationManager.startCompassActivity(this, geoCache);
                return true;
            case R.id.menuGeoCacheInfo:
                NavigationManager.startInfoActivity(this, geoCache);
                return true;
            case R.id.driving_directions:
                onDrivingDirectionsSelected();
                return true;
            case R.id.show_external_map:
                showExternalMap();
                return true;
            case R.id.stepByStep:
                NavigationManager.startCheckpointsFolder(this, geoCache);
                return true;
            case R.id.searchMapSettings:
                startActivity(new Intent(this, DashboardPreferenceActivity.class));
                return true;
            case R.id.menuStartGpsStatus:
                StartGpsStatusActivity(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetZoom(final boolean animate) {
        // Pan to see all markers in view.
        final View mapView = mapFragment.getView();
        final int mapWidth = mapView.getWidth();
        final int mapHeight = mapView.getHeight();
        // Cannot zoom to bounds until the map has a size.
        if (mapWidth == 0 || mapHeight == 0) {
            if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi") // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        mapWrapper.resetZoom(mapView.getWidth(), mapView.getHeight(), animate);
                    }
                });
            }
        } else {
            mapWrapper.resetZoom(mapWidth, mapHeight, animate);
        }
    }

    private void onDrivingDirectionsSelected() {
        final Location location = Controller.getInstance().getLocationManager().getLastKnownLocation();
        if (location != null) {
            final GeoPoint destination = Controller.getInstance().getCurrentSearchPoint().getGeoPoint();
            final double sourceLat = location.getLatitude();
            final double sourceLng = location.getLongitude();
            final double destinationLat = destination.getLatitude();
            final double destinationLng = destination.getLongitude();
            NavigationManager.startExternalDrivingDirrections(this, sourceLat, sourceLng, destinationLat, destinationLng);
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.status_null_last_location), Toast.LENGTH_LONG).show();
        }
    }

    private void showExternalMap() {
        final GeoPoint destination = Controller.getInstance().getCurrentSearchPoint().getGeoPoint();
        final double latitude = destination.getLatitude();
        final double longitude = destination.getLongitude();
        NavigationManager.startExternalMap(this, latitude, longitude, (int) mapWrapper.getMapState().getZoom());
    }

    /*
     * (non-Javadoc)
     *
     * @see su.geocaching.android.controller.IConnectionAware#onConnectionLost()
     */
    @Override
    public void onConnectionLost() {
        connectionLostToast.show();
    }

    /*
     * (non-Javadoc)
     *
     * @see su.geocaching.android.controller.ILocationAware#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case AccurateUserLocationManager.GPS_EVENT_SATELLITE_STATUS:
                // just update status
                gpsStatusTextView.setText(Controller.getInstance().getLocationManager().getSatellitesStatusString());
                break;
            case AccurateUserLocationManager.OUT_OF_SERVICE:
                // provider unavailable
                showProgressBarCircle();
                gpsStatusTextView.setText(R.string.gps_status_unavailable);
                providerUnavailableToast.show();
                break;
            case AccurateUserLocationManager.TEMPORARILY_UNAVAILABLE:
                // gps connection lost. just show progress bar
                showProgressBarCircle();
                break;
            case AccurateUserLocationManager.GPS_EVENT_FIRST_FIX:
                // location fixed. hide progress bar
                hideProgressBarCircle();
                break;
            case AccurateUserLocationManager.EVENT_PROVIDER_DISABLED:
                if (LocationManager.GPS_PROVIDER.equals(provider)) {
                    // gps has been turned off
                    showDialog(DIALOG_ID_TURN_ON_GPS);
                    hideProgressBarCircle();
                    UiHelper.hide(gpsStatusTextView);
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
                    showProgressBarCircle();
                    UiHelper.show(gpsStatusTextView);
                }
                break;
        }
    }

    private void showProgressBarCircle() {
        progressBarCircle.setVisibility(View.VISIBLE);
        //setSupportProgressBarIndeterminateVisibility(true);
    }

    private void hideProgressBarCircle() {
        progressBarCircle.setVisibility(View.GONE);
        //setSupportProgressBarIndeterminateVisibility(false);
    }

    /**
     * run animation for user location overlay
     */
    private void startCompassAnimation() {
        if (animationThread == null) {
            animationThread = new SmoothCompassThread(mapWrapper);
            animationThread.setRunning(true);
            animationThread.start();
        }
    }

    /**
     * Stop animation for user location overlay
     */
    private void stopCompassAnimation() {
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
     * Set map center and zoom level from last using search geocache map
     */
    private void updateMapInfoFromSettings() {
        SearchMapInfo lastMapInfo = Controller.getInstance().getPreferencesManager().getLastSearchMapInfo();
        // TODO: also resetZoom if user location and all markers are out of the current view port

        if (lastMapInfo.getGeoCacheId() != geoCache.getId()) {
            resetZoom(false);
        } else {
            updateMap(lastMapInfo);
        }
    }

    private void updateMap(SearchMapInfo lastMapInfo) {
        mapWrapper.restoreMapSate(lastMapInfo);
    }

    /**
     * Save map center and zoom level to shared preferences
     */
    private void saveMapInfoToSettings() {
        SearchMapInfo searchMapInfo = getMapInfo();
        Controller.getInstance().getPreferencesManager().setLastSearchMapInfo(searchMapInfo);
    }

    private SearchMapInfo getMapInfo() {
        MapInfo mapInfo = mapWrapper.getMapState();
        int geocacheId = geoCache.getId();
        return new SearchMapInfo(mapInfo.getCenterX(), mapInfo.getCenterY(), mapInfo.getZoom(), geocacheId);
    }

    @Override
    public void onConnectionFound() {
        connectionLostToast.cancel();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SearchMapInfo.class.getCanonicalName(), getMapInfo());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        SearchMapInfo mapInfo = (SearchMapInfo) savedInstanceState.getSerializable(SearchMapInfo.class.getCanonicalName());
        if (mapInfo != null) {
            updateMap(mapInfo);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CallbackManager.WHAT_LOCATION_DEPRECATED:
                // update distance text view
                final boolean isPrecise = Controller.getInstance().getLocationManager().hasPreciseLocation();
                mapWrapper.setLocationPrecise(isPrecise);
                updateDistanceTextView();
                return true;
        }
        return false;
    }

    private void updateDistanceTextView() {
        if (distanceStatusTextView != null && Controller.getInstance().getLocationManager().hasLocation()) {
            distanceStatusTextView.setText(
                    CoordinateHelper.distanceToString(
                            CoordinateHelper.getDistanceBetween(
                                    Controller.getInstance().getCurrentSearchPoint().getGeoPoint(),
                                    Controller.getInstance().getLocationManager().getLastKnownLocation()),
                            Controller.getInstance().getLocationManager().hasPreciseLocation()));
        }
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

    private void onMyLocationClick() {
        final Location lastLocation = Controller.getInstance().getLocationManager().getLastKnownLocation();
        if (lastLocation != null) {
            mapWrapper.animateToLocation(lastLocation);
        } else {
            if (statusNullLastLocationToast == null) {
                statusNullLastLocationToast = Toast.makeText(getBaseContext(), getString(R.string.status_null_last_location), Toast.LENGTH_SHORT);
            }
            statusNullLastLocationToast.show();
        }
    }

    private void setActiveItem(GeoCache activeItem) {
        if (activeItem.getType() == GeoCacheType.CHECKPOINT) {
            Controller.getInstance().getCheckpointManager(geoCache.getId()).setActiveItem(activeItem.getId());
            getSupportActionBar().setSubtitle(activeItem.getName());
        } else {
            Controller.getInstance().getCheckpointManager(geoCache.getId()).deactivateCheckpoints();
            Controller.getInstance().setCurrentSearchPoint(activeItem);
            getSupportActionBar().setSubtitle(null);
        }

        // update checkpoint markers
        // TODO: optimize
        mapWrapper.clearGeocacheMarkers();
        mapWrapper.setSearchGeocache(geoCache);
        for (GeoCache checkpoint : Controller.getInstance().getCheckpointManager(geoCache.getId()).getCheckpoints()) {
            mapWrapper.addCheckpointMarker(checkpoint);
        }

        // update cache direction
        mapWrapper.updateCacheDirection();

        // update distance
        updateDistanceTextView();
    }

    public GeoCache getGeoCache() {
        return geoCache;
    }

    public void StartGpsStatusActivity(View v) {
        NavigationManager.startExternalGpsStatusActivity(this);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #googleMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView
     * MapView}) will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p/>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            googleMap = mapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();
            } else {
                Toast.makeText(this,  this.getString(R.string.error_map_creation), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    SupportMapFragment mapFragment;

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #googleMap} is not null.
     */
    private void setUpMap() {
        mapWrapper = new SearchGoogleMapWrapper(googleMap);

        boolean isMultiTouchAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
        mapWrapper.setZoomControlsEnabled(!isMultiTouchAvailable);

        mapWrapper.setupMyLocationLayer();
        mapWrapper.setLocationMarkerTapListener(new LocationMarkerTapListener() {
            @Override
            public void OnMarkerTapped() {
                Controller.getInstance().Vibrate();
                NavigationManager.startCompassActivity(SearchMapActivity.this, geoCache);
            }
        });

        mapWrapper.setGeocacheTapListener(new GeocacheMarkerTapListener() {
            @Override
            public void OnMarkerTapped(final GeoCache geocache) {
                Controller.getInstance().Vibrate();
                if (geocache.getType() == GeoCacheType.CHECKPOINT) {
                    NavigationManager.startCheckpointDialog(SearchMapActivity.this, geocache);
                } else {
                    NavigationManager.startInfoActivity(SearchMapActivity.this, geocache);
                }
            }

            @Override
            public void OnMarkerLongTapped(final GeoCache geocache) {
                setActiveItem(geocache);
            }
        });

        mapWrapper.setMapLongClickListener(new MapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                GeoCache checkpoint = new GeoCache();
                checkpoint.setType(GeoCacheType.CHECKPOINT);
                checkpoint.setName(geoCache.getName());
                checkpoint.setId(geoCache.getId());
                checkpoint.setGeoPoint(new GeoPoint(latLng.latitude, latLng.longitude));
                NavigationManager.startCreateCheckpointActivity(SearchMapActivity.this, checkpoint);
            }
        });
    }
}