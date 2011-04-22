package su.geocaching.android.ui.searchmap;

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
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.*;
import su.geocaching.android.controller.*;
import su.geocaching.android.controller.compass.SmoothCompassThread;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.IGpsStatusAware;
import su.geocaching.android.controller.managers.IInternetAware;
import su.geocaching.android.controller.managers.ILocationAware;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.FavoritesFolderActivity;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

import java.util.List;
import java.util.Locale;

/**
 * Search GeoCache with the map
 *
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class SearchMapActivity extends MapActivity implements IInternetAware, ILocationAware, IGpsStatusAware {
    private final static String TAG = SearchMapActivity.class.getCanonicalName();
    private final static float CLOSE_DISTANCE_TO_GC_VALUE = 100; // if we nearly than this distance in meters to geocache - gps will be work maximal often
    private final static String SEARCH_MAP_ACTIVITY_FOLDER = "/SearchMapActivity";

    private CheckpointOverlay checkpointOverlay;
    private SearchGeoCacheOverlay searchGeoCacheOverlay;
    private Drawable cacheMarker;
    private DistanceToGeoCacheOverlay distanceOverlay;
    private UserLocationOverlay userOverlay;
    private MapView map;
    private MapController mapController;
    private List<Overlay> mapOverlays;

    private TextView waitingLocationFixText;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnimation;

    private SmoothCompassThread animationThread;
    private CheckpointManager checkpointManager;

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

        waitingLocationFixText = (TextView) findViewById(R.id.waitingLocationFixText);
        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnimation = (AnimationDrawable) progressBarView.getBackground();

        map = (MapView) findViewById(R.id.searchGeocacheMap);
        mapOverlays = map.getOverlays();
        mapController = map.getController();
        userOverlay = new UserLocationOverlay(this, map);
        map.setBuiltInZoomControls(true);
        GeoCache geoCache = (GeoCache) getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        Controller.getInstance().setSearchingGeoCache(geoCache);
        Controller.getInstance().getPreferencesManager().setLastSearchedGeoCache(geoCache);
        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(SEARCH_MAP_ACTIVITY_FOLDER);

        checkpointManager = Controller.getInstance().getCheckpointManager(geoCache.getId());
        checkpointOverlay = new CheckpointOverlay(Controller.getInstance().getResourceManager().getMarker(GeoCacheType.CHECKPOINT, null), this, map);
        for (GeoCache checkpoint : checkpointManager.getCheckpoints()) {
            checkpointOverlay.addOverlayItem(new GeoCacheOverlayItem(checkpoint, "", ""));
            if (checkpoint.getStatus() == GeoCacheStatus.ACTIVE_CHECKPOINT) {
                Controller.getInstance().setSearchingGeoCache(checkpoint);
            }
        }
        mapOverlays.add(checkpointOverlay);
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
        saveMapInfoToSettings();

        if (Controller.getInstance().getLocationManager().hasLocation()) {
            stopAnimation();
        }

        Controller.getInstance().getConnectionManager().removeSubscriber(this);
        Controller.getInstance().getLocationManager().removeSubscriber(this);
        Controller.getInstance().getGpsStatusManager().removeSubscriber(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManager.d(TAG, "on resume");
        GeoCache geoCache = (GeoCache) getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        if (geoCache == null) {
            LogManager.e(TAG, "null geocache. Finishing.");
            Toast.makeText(this, getString(R.string.search_geocache_error_no_geocache), Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }

        if (distanceOverlay != null) {
            distanceOverlay.setCachePoint(Controller.getInstance().getSearchingGeoCache().getLocationGeoPoint());
        }

        if (!Controller.getInstance().getDbManager().isCacheStored(geoCache.getId())) {
            LogManager.e(TAG, "geocache not in db. Finishing.");
            // mController.getDbManager().addGeoCache(geoCache, webText, webNotebookText);
            Toast.makeText(this, getString(R.string.search_geocache_error_geocache_not_in_db), Toast.LENGTH_LONG).show();
            this.finish();
            startActivity(new Intent(this, FavoritesFolderActivity.class));
            return;
        }

        checkpointManager = Controller.getInstance().getCheckpointManager(Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId());
        checkpointOverlay.clear();
        for (GeoCache checkpoint : checkpointManager.getCheckpoints()) {
            checkpointOverlay.addOverlayItem(new GeoCacheOverlayItem(checkpoint, "", ""));
        }

        map.setKeepScreenOn(Controller.getInstance().getPreferencesManager().getKeepScreenOnPreference());
        updateMapInfoFromSettings();
        map.setSatellite(Controller.getInstance().getPreferencesManager().useSatelliteMap());
        mapOverlays.remove(searchGeoCacheOverlay);
        if (geoCache != null) {
            cacheMarker = Controller.getInstance().getResourceManager().getMarker(geoCache.getType(), geoCache.getStatus());
            searchGeoCacheOverlay = new SearchGeoCacheOverlay(cacheMarker, this, map);
            GeoCacheOverlayItem cacheOverlayItem = new GeoCacheOverlayItem(geoCache, "", "");
            searchGeoCacheOverlay.addOverlayItem(cacheOverlayItem);
            mapOverlays.add(searchGeoCacheOverlay);
        }

        if (!Controller.getInstance().getLocationManager().isBestProviderEnabled()) {
            if (!Controller.getInstance().getLocationManager().isBestProviderGps()) {
                LogManager.w(TAG, "resume: device without gps");
            }
            UiHelper.askTurnOnGps(this);
            LogManager.d(TAG, "resume: best provider (" + Controller.getInstance().getLocationManager().getBestProvider() + ") disabled. Current provider is "
                    + Controller.getInstance().getLocationManager().getCurrentProvider());
        } else {
            LogManager.d(TAG, "resume: best provider (" + Controller.getInstance().getLocationManager().getBestProvider() + ") enabled. Run logic");

            if (!Controller.getInstance().getLocationManager().hasLocation()) {
                onBestProviderUnavailable();
                resetZoom();
                progressBarView.setVisibility(View.VISIBLE);
                LogManager.d(TAG, "runLogic: location not fixed. Send msg.");
            } else {
                updateLocation(Controller.getInstance().getLocationManager().getLastKnownLocation());
                progressBarView.setVisibility(View.GONE);
                startAnimation();
                LogManager.d(TAG, "runLogic: location fixed. Update location with last known location");
            }

            Controller.getInstance().getLocationManager().addSubscriber(this);
            Controller.getInstance().getLocationManager().enableBestProviderUpdates();
            Controller.getInstance().getGpsStatusManager().addSubscriber(this);
            Controller.getInstance().getConnectionManager().addSubscriber(this);

            map.invalidate();
        }

        if (!Controller.getInstance().getConnectionManager().isInternetConnected()) {
            onInternetLost();
            LogManager.w(TAG, "internet not connected");
        }

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
            Controller.getInstance().getLocationManager().updateFrequency(GpsUpdateFrequency.MAXIMAL);
        } else {
            Controller.getInstance().getLocationManager().updateFrequencyFromPreferences();
        }
        waitingLocationFixText.setText(GpsHelper.distanceToString(GpsHelper.getDistanceBetween(Controller.getInstance().getSearchingGeoCache().getLocationGeoPoint(), location)));
        if (distanceOverlay == null) {
            // It's really first run of update location
            LogManager.d(TAG, "update location: first run of this activity");
            waitingLocationFixText.setGravity(Gravity.CENTER);
            waitingLocationFixText.setTextSize(getResources().getDimension(R.dimen.text_size_big));
            distanceOverlay = new DistanceToGeoCacheOverlay(GpsHelper.locationToGeoPoint(location), Controller.getInstance().getSearchingGeoCache().getLocationGeoPoint());
            mapOverlays.add(distanceOverlay);
            mapOverlays.add(userOverlay);
            resetZoom();

            startAnimation();

            return;
        }
        distanceOverlay.setUserPoint(GpsHelper.locationToGeoPoint(location));

        map.invalidate();
    }

    /**
     * Set map zoom which can show userPoint and GeoCachePoint
     */
    private void resetZoom() {
        // Calculate min/max latitude & longitude
        int minLat = Integer.MAX_VALUE;
        int maxLat = Integer.MIN_VALUE;
        int minLon = Integer.MAX_VALUE;
        int maxLon = Integer.MIN_VALUE;
        GeoCache gc = (GeoCache) getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        if (Controller.getInstance().getLocationManager().hasLocation()) {
            minLat = GpsHelper.locationToGeoPoint(Controller.getInstance().getLocationManager().getLastKnownLocation()).getLatitudeE6();
            maxLat = GpsHelper.locationToGeoPoint(Controller.getInstance().getLocationManager().getLastKnownLocation()).getLatitudeE6();
            minLon = GpsHelper.locationToGeoPoint(Controller.getInstance().getLocationManager().getLastKnownLocation()).getLongitudeE6();
            maxLon = GpsHelper.locationToGeoPoint(Controller.getInstance().getLocationManager().getLastKnownLocation()).getLongitudeE6();
        }
        minLat = Math.min(gc.getLocationGeoPoint().getLatitudeE6(), minLat);
        maxLat = Math.max(gc.getLocationGeoPoint().getLatitudeE6(), maxLat);
        minLon = Math.min(gc.getLocationGeoPoint().getLongitudeE6(), minLon);
        maxLon = Math.max(gc.getLocationGeoPoint().getLongitudeE6(), maxLon);

        for (GeoCache i : Controller.getInstance().getCheckpointManager(gc.getId()).getCheckpoints()) {
            minLat = Math.min(i.getLocationGeoPoint().getLatitudeE6(), minLat);
            maxLat = Math.max(i.getLocationGeoPoint().getLatitudeE6(), maxLat);
            minLon = Math.min(i.getLocationGeoPoint().getLongitudeE6(), minLon);
            maxLon = Math.max(i.getLocationGeoPoint().getLongitudeE6(), maxLon);
        }

        // Calculate span
        int latSpan = maxLat - minLat;
        int lonSpan = maxLon - minLon;

        // Set zoom
        if (latSpan != 0 && lonSpan != 0) {
            mapController.zoomToSpan(latSpan, lonSpan);
        }

        // Calculate new center of map
        GeoPoint center = new GeoPoint((minLat + maxLat) / 2, (minLon + maxLon) / 2);

        // Set new center of map
        mapController.setCenter(center);

        // if markers not in map - zoom out. logic below

        // Get map boundaries
        int mapRight = map.getRight();
        int mapBottom = map.getBottom();
        int mapLeft = map.getLeft();
        int mapTop = map.getTop();
        // if map not init - go out of here!
        if (mapRight == 0 && mapLeft == 0 && mapTop == 0 && mapBottom == 0) {
            map.invalidate();
            return;
        }

        boolean needZoomOut = false;
        Projection proj = map.getProjection();
        Rect rect = new Rect();
        Point point = new Point();

        if (Controller.getInstance().getLocationManager().hasLocation()) {
            // is user marker in visible map
            GeoPoint currentGeoPoint = GpsHelper.locationToGeoPoint(Controller.getInstance().getLocationManager().getLastKnownLocation());
            int userPadding = (int) proj.metersToEquatorPixels(Controller.getInstance().getLocationManager().getLastKnownLocation().getAccuracy());
            proj.toPixels(currentGeoPoint, point);
            needZoomOut = needZoomOut || (point.x - userPadding < mapLeft) || (point.x + userPadding > mapRight) || (point.y - userPadding < mapTop) || (point.y + userPadding > mapBottom);

        }
        if (!needZoomOut) {
            // still not need zoom out
            // calculate padding
            rect = cacheMarker.getBounds();
            // Get points on screen
            proj.toPixels(gc.getLocationGeoPoint(), point);
            // Check contains markers in visible part of map
            needZoomOut = needZoomOut || (point.x + rect.left < mapLeft) || (point.x + rect.right > mapRight) || (point.y + rect.top < mapTop) || (point.y + rect.bottom > mapBottom);
        }

        // check contains checkpoints markers in visible part of map if still not need zoom out
        if (!needZoomOut) {
            // Get marker of checkpoint
            // gc.setType(GeoCacheType.CHECKPOINT); //No!, never set checkpoint to Intent
            rect = Controller.getInstance().getResourceManager().getMarker(GeoCacheType.CHECKPOINT, null).getBounds();

            for (GeoCache i : Controller.getInstance().getCheckpointManager(gc.getId()).getCheckpoints()) {
                proj.toPixels(i.getLocationGeoPoint(), point);
                needZoomOut = needZoomOut || (point.x + rect.left < mapLeft) || (point.x + rect.right > mapRight) || (point.y + rect.top < mapTop) || (point.y + rect.bottom > mapBottom);
                if (needZoomOut) {
                    // already need zoom out
                    break;
                }
            }
        }

        // if markers are not visible then zoomOut
        if (needZoomOut) {
            LogManager.d(TAG, "markers not in the visible part of map. Zoom out.");
            mapController.setZoom(map.getZoomLevel() - 1);
        }
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_geocache_map_menu, menu);
        return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDefaultZoom:
                resetZoom();
                return true;
            case R.id.menuStartCompass:
                UiHelper.startCompassActivity(this);
                return true;
            case R.id.menuGeoCacheInfo:
                UiHelper.startGeoCacheInfo(this, Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache());
                return true;
            case R.id.driving_directions:
                onDrivingDirectionsSelected();
                return true;
            case R.id.stepByStep:
                UiHelper.startCheckpointsFolder(this, Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId());
                return true;
            case R.id.searchMapSettings:
                startActivity(new Intent(this, SearchMapPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDrivingDirectionsSelected() {
        if (Controller.getInstance().getLocationManager().getLastKnownLocation() != null) {
            GeoPoint second = Controller.getInstance().getSearchingGeoCache().getLocationGeoPoint();
            double firstLat = Controller.getInstance().getLocationManager().getLastKnownLocation().getLatitude();
            double firstLng = Controller.getInstance().getLocationManager().getLastKnownLocation().getLongitude();
            double secondLat = second.getLatitudeE6() / 1E6;
            double secondLng = second.getLongitudeE6() / 1E6;
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f&ie=UTF8&om=0&output=kml",
                    firstLat, firstLng, secondLat, secondLng)));
            startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.dislocation), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show message string to user
     *
     * @param status string with information about device status
     * @param type   type of message(GPS, Internet, etc)
     */
    public void updateStatus(String status, StatusType type) {
        if (!Controller.getInstance().getLocationManager().hasLocation()) {
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
    }

    /**
     * Show progressbar and send message when location updates from provider unavailable
     */
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
        if (!progressBarAnimation.isRunning()) {
            progressBarAnimation.start();
        } else {
            progressBarAnimation.stop();
        }
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
        if (!Controller.getInstance().getLocationManager().isBestProviderEnabled()) {
            LogManager.d(TAG, "onStatusChanged: best provider (" + Controller.getInstance().getLocationManager().getBestProvider() + ") disabled. Ask turn on.");
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
                animationThread.join(150);
            } catch (InterruptedException ignored) {
            }
            animationThread = null;
        }
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }

    private void updateMapInfoFromSettings() {
        MapInfo lastMapInfo = Controller.getInstance().getPreferencesManager().getLastSearchMapInfo();
        GeoPoint lastCenter = new GeoPoint(lastMapInfo.getCenterX(), lastMapInfo.getCenterY());

        mapController.setCenter(lastCenter);
        mapController.animateTo(lastCenter);
        mapController.setZoom(lastMapInfo.getZoom());
        map.invalidate();
    }

    private void saveMapInfoToSettings() {
        Controller.getInstance().getPreferencesManager().setLastSearchMapInfo(new MapInfo(map.getMapCenter().getLatitudeE6(), map.getMapCenter().getLongitudeE6(), map.getZoomLevel()));
    }
}