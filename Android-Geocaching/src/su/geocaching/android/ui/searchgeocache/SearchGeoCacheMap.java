package su.geocaching.android.ui.searchgeocache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import su.geocaching.android.controller.CompassManager;
import su.geocaching.android.controller.ConnectionManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GeoCacheLocationManager;
import su.geocaching.android.controller.GpsStatusManager;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.controller.ICompassAware;
import su.geocaching.android.controller.IGpsStatusAware;
import su.geocaching.android.controller.IInternetAware;
import su.geocaching.android.controller.ILocationAware;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.compass.CompassPreferenceManager;
import su.geocaching.android.controller.compass.CompassSpeed;
import su.geocaching.android.controller.compass.SmoothCompassThread;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.CheckpointsFolder;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.compass.SearchGeoCacheCompass;
import su.geocaching.android.ui.geocachemap.CheckpointCacheOverlay;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.geocachemap.SearchCacheOverlay;
import su.geocaching.android.ui.searchgeocache.stepbystep.CheckpointDialog;
import su.geocaching.android.ui.searchgeocache.stepbystep.StepByStepTabActivity;
import su.geocaching.android.utils.GpsHelper;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * Search GeoCache with the map
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class SearchGeoCacheMap extends MapActivity implements IInternetAware, ILocationAware, ICompassAware, IGpsStatusAware {
    private final static String TAG = SearchGeoCacheMap.class.getCanonicalName();
    private final static float CLOSE_DISTANCE_TO_GC_VALUE = 30; // if we nearly than this distance in meters to geocache - gps will be work maximal often

    private CheckpointCacheOverlay checkpointCacheOverlay;
    private Drawable cacheMarker;
    private DistanceToGeoCacheOverlay distanceOverlay;
    private UserLocationOverlay userOverlay;
    private MapView map;
    private MapController mapController;
    private List<Overlay> mapOverlays;

    private TextView waitingLocationFixText;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnimation;

    private ConnectionManager internetManager;
    private CompassManager mCompassManager;
    private GeoCacheLocationManager mLocationManager;
    private GpsStatusManager mGpsStatusManager;
    private Controller mController;
    private GoogleAnalyticsTracker tracker;
    private SmoothCompassThread animationThread;

    private DbManager dbm;

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.search_geocache_map);

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.search_activity_folder));
        tracker.dispatch();

        waitingLocationFixText = (TextView) findViewById(R.id.waitingLocationFixText);
        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnimation = (AnimationDrawable) progressBarView.getBackground();

        map = (MapView) findViewById(R.id.searchGeocacheMap);
        mapOverlays = map.getOverlays();
        mapController = map.getController();
        userOverlay = new UserLocationOverlay(this);
        map.setBuiltInZoomControls(true);

        GeoCache geoCache = (GeoCache) getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        mController = Controller.getInstance();
        mController.setSearchingGeoCache(geoCache);

        internetManager = mController.getConnectionManager();
        mLocationManager = mController.getLocationManager();
        mCompassManager = mController.getCompassManager();
        mGpsStatusManager = mController.getGpsStatusManager(getApplicationContext());

        if (geoCache != null) {
            cacheMarker = mController.getResourceManager().getMarker(mController.getSearchingGeoCache());
            SearchCacheOverlay searchCacheOverlay = new SearchCacheOverlay(cacheMarker, this);
            GeoCacheOverlayItem cacheOverlayItem = new GeoCacheOverlayItem(mController.getSearchingGeoCache(), "", "");
            searchCacheOverlay.addOverlayItem(cacheOverlayItem);
            mapOverlays.add(searchCacheOverlay);
        }

        GeoCache gc = new GeoCache();
        gc.setType(GeoCacheType.CHECKPOINT);
        cacheMarker = Controller.getInstance().getResourceManager().getMarker(gc);

        dbm = Controller.getInstance().getDbManager();
        checkpointCacheOverlay = new CheckpointCacheOverlay(cacheMarker, this, map);
        ArrayList<GeoCache> checkpoints = dbm.getCheckpointsArrayById(mController.getSearchingGeoCache().getId());
        for (GeoCache checkpoint : checkpoints) {
            checkpointCacheOverlay.addOverlayItem(new GeoCacheOverlayItem(checkpoint, "", ""));
            if (checkpoint.getStatus() == GeoCacheStatus.ACTIVE_CHECKPOINT) {
                mController.setSearchingGeoCache(checkpoint);
            }
        }
        mapOverlays.add(checkpointCacheOverlay);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        LogManager.d(TAG, "on pause");

        if (mLocationManager.hasLocation()) {
            stopAnimation();
        }

        internetManager.removeSubscriber(this);
        mLocationManager.removeSubscriber(this);
        mCompassManager.removeSubscriber(this);
        mGpsStatusManager.removeSubscriber(this);

        tracker.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManager.d(TAG, "on resume");

        if (checkpointCacheOverlay.size() == 0) {
            dbm = Controller.getInstance().getDbManager();
            ArrayList<GeoCache> checkpoints = dbm.getCheckpointsArrayById(mController.getSearchingGeoCache().getId());
            LogManager.d(TAG, "checkpoints " + checkpoints.size());
            LogManager.d(TAG, "mController.getSearchingGeoCache().getId() " + mController.getSearchingGeoCache().getId());
            for (GeoCache item : checkpoints) {
                checkpointCacheOverlay.addOverlayItem(new GeoCacheOverlayItem(item, "", ""));
            }
        }

        map.setKeepScreenOn(Controller.getInstance().getPreferencesManager().getKeepScreenOnPreference());

        if (!mLocationManager.isBestProviderEnabled()) {
            if (!mLocationManager.isBestProviderGps()) {
                LogManager.w(TAG, "resume: device without gps");
            }
            UiHelper.askTurnOnGps(this);
            LogManager.d(TAG, "resume: best provider (" + mLocationManager.getBestProvider() + ") disabled. Current provider is " + mLocationManager.getCurrentProvider());
        } else {
            LogManager.d(TAG, "resume: best provider (" + mLocationManager.getBestProvider() + ") enabled. Run logic");
            if (mController.getSearchingGeoCache() == null) {
                LogManager.e(TAG, "null geocache. Finishing.");
                Toast.makeText(this, getString(R.string.search_geocache_error_no_geocache), Toast.LENGTH_LONG).show();
                this.finish();
                return;
            }

            // Save last searched geocache
            if (mController.getSearchingGeoCache().getType() != GeoCacheType.CHECKPOINT) {
                Controller.getInstance().getPreferencesManager().setLastSearchedGeoCache(mController.getSearchingGeoCache());
            }

            if (!mLocationManager.hasLocation()) {
                onBestProviderUnavailable();
                mapController.animateTo(mController.getSearchingGeoCache().getLocationGeoPoint());
                progressBarView.setVisibility(View.VISIBLE);
                LogManager.d(TAG, "runLogic: location not fixed. Send msg.");
            } else {
                updateLocation(mLocationManager.getLastKnownLocation());
                progressBarView.setVisibility(View.GONE);
                startAnimation();
                LogManager.d(TAG, "runLogic: location fixed. Update location with last known location");
            }

            mLocationManager.addSubscriber(this);
            mLocationManager.enableBestProviderUpdates();
            mCompassManager.addSubscriber(this);
            mGpsStatusManager.addSubscriber(this);
            internetManager.addSubscriber(this);

            map.invalidate();
        }

        if (!internetManager.isInternetConnected()) {
            onInternetLost();
            LogManager.w(TAG, "internet not connected");
        }

    }

    /**
     * Start SearchGeoCacheCompass activity
     */
    public void startCompassView() {
        LogManager.d(TAG, "start compass activity");

        Intent intent = new Intent(this, SearchGeoCacheCompass.class);
        if (mController.getSearchingGeoCache() != null) {
            intent.putExtra(GeoCache.class.getCanonicalName(), mController.getSearchingGeoCache());
        }
        startActivity(intent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.controller.ILocationAware#updateLocation(android.location.Location)
     */
    @Override
    public void updateLocation(Location location) {
        userOverlay.setPoint(GpsHelper.locationToGeoPoint(location));
        userOverlay.setAccuracy(location.getAccuracy());
        LogManager.d(TAG, "update location");
        if (progressBarView.getVisibility() == View.VISIBLE) {
            progressBarView.setVisibility(View.GONE);
        }
        if (GpsHelper.getDistanceBetween(location, Controller.getInstance().getSearchingGeoCache().getLocationGeoPoint()) < CLOSE_DISTANCE_TO_GC_VALUE) {
            // TODO: may be need make special preference?
            mLocationManager.updateFrequency(GpsUpdateFrequency.MAXIMAL);
        } else {
            mLocationManager.updateFrequencyFromPreferences();
        }
        waitingLocationFixText.setText(GpsHelper.distanceToString(GpsHelper.getDistanceBetween(mController.getSearchingGeoCache().getLocationGeoPoint(), location)));
        if (distanceOverlay == null) {
            // It's really first run of update location
            LogManager.d(TAG, "update location: first run of this activity");
            waitingLocationFixText.setGravity(Gravity.CENTER);
            distanceOverlay = new DistanceToGeoCacheOverlay(GpsHelper.locationToGeoPoint(location), mController.getSearchingGeoCache().getLocationGeoPoint());
            mapOverlays.add(distanceOverlay);
            mapOverlays.add(userOverlay);
            resetZoom();

            startAnimation();

            return;
        }
        distanceOverlay.setUserPoint(GpsHelper.locationToGeoPoint(location));

        map.invalidate();
    }

    @Override
    protected Dialog onCreateDialog(int index) {
        return new CheckpointDialog(this, index, checkpointCacheOverlay, map);
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.controller.ICompassAware#updateBearing(float)
     */
    @Override
    public void updateBearing(float bearing) {
        userOverlay.setDirection(bearing);
    }

    /**
     * Set map zoom which can show userPoint and GeoCachePoint
     */
    private void resetZoom() {
        GeoPoint currentGeoPoint = GpsHelper.locationToGeoPoint(mLocationManager.getLastKnownLocation());
        // Calculating max and min lat and lon
        int minLat = Math.min(mController.getSearchingGeoCache().getLocationGeoPoint().getLatitudeE6(), currentGeoPoint.getLatitudeE6());
        int maxLat = Math.max(mController.getSearchingGeoCache().getLocationGeoPoint().getLatitudeE6(), currentGeoPoint.getLatitudeE6());
        int minLon = Math.min(mController.getSearchingGeoCache().getLocationGeoPoint().getLongitudeE6(), currentGeoPoint.getLongitudeE6());
        int maxLon = Math.max(mController.getSearchingGeoCache().getLocationGeoPoint().getLongitudeE6(), currentGeoPoint.getLongitudeE6());

        // Calculate span
        int latSpan = maxLat - minLat;
        int lonSpan = maxLon - minLon;

        // Set zoom
        mapController.zoomToSpan(latSpan, lonSpan);

        // Calculate new center of map
        GeoPoint center = new GeoPoint((mController.getSearchingGeoCache().getLocationGeoPoint().getLatitudeE6() + currentGeoPoint.getLatitudeE6()) / 2, (mController.getSearchingGeoCache()
                .getLocationGeoPoint().getLongitudeE6() + currentGeoPoint.getLongitudeE6()) / 2);

        // Set new center of map
        mapController.setCenter(center);
        map.invalidate();
        Projection proj = map.getProjection();
        // calculate padding
        int userPadding = (int) proj.metersToEquatorPixels(mLocationManager.getLastKnownLocation().getAccuracy());
        Rect cacheBounds = cacheMarker.getBounds();
        // Get points of user and cache on screen
        Point userPoint = new Point();
        Point cachePoint = new Point();
        proj.toPixels(currentGeoPoint, userPoint);
        proj.toPixels(mController.getSearchingGeoCache().getLocationGeoPoint(), cachePoint);
        // Get map boundaries
        int mapRight = map.getRight();
        int mapBottom = map.getBottom();
        int mapLeft = map.getLeft();
        int mapTop = map.getTop();
        // Check contains markers in visible part of map
        boolean isCacheMarkerNotInMapX = (cachePoint.x + cacheBounds.left < mapLeft) || (cachePoint.x + cacheBounds.right > mapRight);
        boolean isCacheMarkerNotInMapY = (cachePoint.y + cacheBounds.top < mapTop) || (cachePoint.y + cacheBounds.bottom > mapBottom);
        boolean isUserMarkerNotInMapX = (userPoint.x - userPadding < mapLeft) || (userPoint.x + userPadding > mapRight);
        boolean isUserMarkerNotInMapY = (userPoint.y - userPadding < mapTop) || (userPoint.y + userPadding > mapBottom);
        boolean isMapDimensionsZeroes = mapRight == 0 && mapLeft == 0 && mapTop == 0 && mapBottom == 0;
        // if markers are not visible then zoomOut
        if ((isCacheMarkerNotInMapX || isCacheMarkerNotInMapY || isUserMarkerNotInMapX || isUserMarkerNotInMapY) && (!isMapDimensionsZeroes)) {
            LogManager.d(TAG, "markers not in the visible part of map. Zoom out.");
            mapController.zoomOut();
        }
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_geocache_map, menu);
        return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menuDefaultZoom:
                if (mLocationManager.hasLocation()) {
                    resetZoom();
                } else {
                    mapController.animateTo(mController.getSearchingGeoCache().getLocationGeoPoint());
                    Toast.makeText(getBaseContext(), R.string.status_null_last_location, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menuStartCompass:
                this.startCompassView();
                return true;
            case R.id.menuGeoCacheInfo:
                UiHelper.showGeoCacheInfo(this, mController.getPreferencesManager().getLastSearchedGeoCache());
                return true;
            case R.id.driving_directions:
                onDrivingDirectionsSelected();
                return true;
            case R.id.stepByStep:
                UiHelper.startCheckpointsFolderForResult(this, mController.getPreferencesManager().getLastSearchedGeoCache().getId());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDrivingDirectionsSelected() {
        if (mLocationManager.getLastKnownLocation() != null) {
            GeoPoint second = mController.getSearchingGeoCache().getLocationGeoPoint();
            double firstLat = mLocationManager.getLastKnownLocation().getLatitude();
            double firstLng = mLocationManager.getLastKnownLocation().getLongitude();
            double secondLat = second.getLatitudeE6() / 1E6;
            double secondLng = second.getLongitudeE6() / 1E6;
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f&ie=UTF8&om=0&output=kml",
                    firstLat, firstLng, secondLat, secondLng)));
            startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.dislocation), Toast.LENGTH_LONG).show();
        }
    }

    // TODO
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case UiHelper.STEP_BY_STEP_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    int latitude = data.getIntExtra(StepByStepTabActivity.LATITUDE, 0);
                    int longitude = data.getIntExtra(StepByStepTabActivity.LONGITUDE, 0);
                    checkpointCacheOverlay.addCheckpoint(latitude, longitude);
                    if (distanceOverlay != null) {
                        distanceOverlay.setCachePoint(mController.getSearchingGeoCache().getLocationGeoPoint());
                        // distanceOverlay.
                    }
                    map.invalidate();
                }
                break;
            case UiHelper.CHECKPOINT_FOLDER_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    int id = data.getIntExtra(CheckpointsFolder.CACHE_ID, 0);
                    int action = data.getIntExtra(CheckpointsFolder.ACTION_KEY, 0);
                    if (action == 1) {
                        checkpointCacheOverlay.setActiveItemById(id);
                    }
                    if (action == 2) {
                        checkpointCacheOverlay.removeOverlayItemById(id);
                    }

                    if (distanceOverlay != null) {
                        distanceOverlay.setCachePoint(mController.getSearchingGeoCache().getLocationGeoPoint());
                        // distanceOverlay.
                    }
                    map.invalidate();
                }
                break;
        }

    }

    /**
     * Show message string to user
     * 
     * @param status
     *            string with information about device status
     * @param type
     *            type of message(GPS, Internet, etc)
     */
    public void updateStatus(String status, StatusType type) {
        if (!mLocationManager.hasLocation()) {
            if (type == StatusType.GPS) {
                waitingLocationFixText.setText(status);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#isRouteDisplayed()
     */
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.ui.geocachemap.IInternetAware#onInternetLost()
     */
    @Override
    public void onInternetLost() {
        Toast.makeText(this, getString(R.string.search_geocache_internet_lost), Toast.LENGTH_LONG).show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.ui.geocachemap.IInternetAware#onInternetFound()
     */
    @Override
    public void onInternetFound() {
        // TODO: do smthng?
    }

    public void onBestProviderUnavailable() {
        if (progressBarView.getVisibility() == View.GONE) {
            progressBarView.setVisibility(View.VISIBLE);
        }
        updateStatus(getString(R.string.waiting_location_fix_message), StatusType.GPS);
        Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG).show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onWindowFocusChanged(boolean)
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        progressBarAnimation.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.controller.IGpsStatusAware#updateStatus(java.lang.String)
     */
    @Override
    public void updateStatus(String status) {
        updateStatus(status, StatusType.GPS);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO: send code of event to activity
        LogManager.d(TAG, "onStatusChanged:");
        String statusString = "Location fixed: " + Boolean.toString(mLocationManager.hasLocation()) + ". Provider: " + provider + ". ";
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

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.controller.ILocationAware#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider) {
        LogManager.d(TAG, "onProviderEnabled: do nothing");
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.controller.ILocationAware#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String provider) {
        LogManager.d(TAG, "onProviderDisabled");
        if (!mLocationManager.isBestProviderEnabled()) {
            LogManager.d(TAG, "onStatusChanged: best provider (" + mLocationManager.getBestProvider() + ") disabled. Ask turn on.");
            onBestProviderUnavailable();
            UiHelper.askTurnOnGps(this);
        }
    }

    /**
     * run animation for user location overlay
     */
    private void startAnimation() {
        if (animationThread == null) {
            animationThread = new SmoothCompassThread(userOverlay);
            animationThread.setRunning(true);

            CompassPreferenceManager preferManager = CompassPreferenceManager.getPreference(this);
            String speed = preferManager.getString(CompassPreferenceManager.PREFS_COMPASS_SPEED_KEY, CompassSpeed.NORMAL.name());
            animationThread.setSpeed(CompassSpeed.valueOf(speed));

            animationThread.start();
        }
    }

    /**
     * Stop animation for user location overlay
     */
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

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }
}