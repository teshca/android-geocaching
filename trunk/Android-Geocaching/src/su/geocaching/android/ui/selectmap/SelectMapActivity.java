package su.geocaching.android.ui.selectmap;

import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.managers.ConnectionManager;
import su.geocaching.android.controller.managers.IConnectionAware;
import su.geocaching.android.controller.managers.ILocationAware;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.controller.managers.UserLocationManager;
import su.geocaching.android.controller.selectmap.geocachegroup.GroupGeoCacheTask;
import su.geocaching.android.controller.selectmap.mapupdatetimer.MapUpdateTimer;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.preferences.MapPreferenceActivity;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class SelectMapActivity extends MapActivity implements IConnectionAware, ILocationAware {
    private static final String TAG = SelectMapActivity.class.getCanonicalName();
    private static final int MAX_CACHE_NUMBER = 300;
    private static final String SELECT_ACTIVITY_FOLDER = "/SelectActivity";
    private static final int ENABLE_CONNECTION_DIALOG_ID = 0;
    private Controller controller;
    private MapView map;
    private SelectGeoCacheOverlay selectGeoCacheOverlay;
    private StaticUserLocationOverlay locationOverlay;
    private UserLocationManager locationManager;
    private MapUpdateTimer mapTimer;
    private ConnectionManager connectionManager;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnimation;
    private int countDownloadTask;
    private Handler handler;
    private GroupGeoCacheTask groupTask = null;
    private boolean firstRun = true;
    private TextView connectionInfoTextView;
    private TextView downloadingInfoTextView;
    private TextView groupingInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.select_map_activity);

        controller = Controller.getInstance();
        map = (MapView) findViewById(R.id.selectGeocacheMap);
        map.getOverlays().clear();

        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnimation = (AnimationDrawable) progressBarView.getBackground();
        progressBarView.setVisibility(View.GONE);
        countDownloadTask = 0;
        handler = new Handler();

        connectionInfoTextView = (TextView) findViewById(R.id.connectionInfoTextView);
        groupingInfoTextView = (TextView) findViewById(R.id.groupingInfoTextView);
        downloadingInfoTextView = (TextView) findViewById(R.id.downloadingInfoTextView);

        locationOverlay = new StaticUserLocationOverlay(controller.getResourceManager().getUserLocationMarker());
        selectGeoCacheOverlay = new SelectGeoCacheOverlay(controller.getResourceManager().getCacheMarker(GeoCacheType.TRADITIONAL, GeoCacheStatus.VALID), this, map);
        map.getOverlays().add(selectGeoCacheOverlay);
        map.getOverlays().add(locationOverlay);

        locationManager = controller.getLocationManager();
        connectionManager = controller.getConnectionManager();

        map.setBuiltInZoomControls(true);
        map.invalidate();
        LogManager.d(TAG, "onCreate Done");

        controller.getGoogleAnalyticsManager().trackActivityLaunch(SELECT_ACTIVITY_FOLDER);
    }

    private synchronized void updateProgressStart() {
        if (countDownloadTask == 0) {
            LogManager.d(TAG, "set visible Visible for progress");
            handler.post(new Runnable() {
                public void run() {
                    progressBarView.setVisibility(View.VISIBLE);
                    downloadingInfoTextView.setText("Загрузка");
                }
            });
        }
        LogManager.d(TAG, "count plus. count = " + countDownloadTask);
        countDownloadTask++;
    }

    private synchronized void updateProgressStop() {
        countDownloadTask--;
        LogManager.d(TAG, "count minus. count = " + countDownloadTask);
        if (countDownloadTask == 0) {
            LogManager.d(TAG, "set visible gone for progress");
            handler.post(new Runnable() {
                public void run() {
                    progressBarView.setVisibility(View.GONE);
                    downloadingInfoTextView.setText("");
                }
            });
        }
    }

    private void updateMapInfoFromSettings() {
        MapInfo lastMapInfo = controller.getPreferencesManager().getLastSelectMapInfo();
        GeoPoint lastCenter = new GeoPoint(lastMapInfo.getCenterX(), lastMapInfo.getCenterY());
        map.getController().setCenter(lastCenter);
        map.getController().animateTo(lastCenter);
        map.getController().setZoom(lastMapInfo.getZoom());
    }

    private void saveMapInfoToSettings() {
        controller.getPreferencesManager().setLastSelectMapInfo(new MapInfo(map.getMapCenter().getLatitudeE6(), map.getMapCenter().getLongitudeE6(), map.getZoomLevel()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update map setting in case they were changed in preferences
        map.setSatellite(controller.getPreferencesManager().useSatelliteMap());
        // add subscriber to connection manager
        connectionManager.addSubscriber(this);
        // add subscriber to location manager
        if (!connectionManager.isActiveNetworkConnected()) {
            showDialog(ENABLE_CONNECTION_DIALOG_ID);
        }
        if (locationManager.getBestProvider(true) == null) {
            //NavigationManager.askTurnOnLocationService(this);
        } else {
            locationManager.addSubscriber(this, false);
        }
        // reset map center and zoom level
        updateMapInfoFromSettings();

        mapTimer = new MapUpdateTimer(this);

        selectGeoCacheOverlay.clear();
        updateCacheOverlay();

        updateLocationOverlay(locationManager.getLastKnownLocation());
        map.invalidate();
    }

    @Override
    protected void onPause() {
        mapTimer.cancel();
        cancelGroupTask();
        locationManager.removeSubscriber(this);
        connectionManager.removeSubscriber(this);
        saveMapInfoToSettings();
        super.onPause();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_map_menu, menu);
        return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapSettings:
                startActivity(new Intent(this, MapPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateLocationOverlay(Location location) {
        LogManager.d(TAG, "updateLocationOverlay");
        if (location != null) {
            locationOverlay.updateLocation(location);
            map.invalidate();
        }
    }

    /* Handles item selections */
    public void updateCacheOverlay() {
        LogManager.d(TAG, "updateCacheOverlay; count = " + countDownloadTask);
        GeoPoint upperLeftCorner = map.getProjection().fromPixels(0, 0);
        GeoPoint lowerRightCorner = map.getProjection().fromPixels(map.getWidth(), map.getHeight());
        controller.updateSelectedGeoCaches(this, upperLeftCorner, lowerRightCorner);
        cancelGroupTask();
        updateProgressStart();
    }

    private void cancelGroupTask()
    {
        if (groupTask != null) {
            LogManager.d(TAG, "cancel group task");
            groupTask.cancel(true);
        }
    }

    public synchronized void simpleAddGeoCacheList(List<GeoCache> geoCacheList) {
        if (geoCacheList == null || geoCacheList.size() == 0) {
            updateProgressStop();
            return;
        }
        if (geoCacheList.size() > MAX_CACHE_NUMBER) {
            Toast.makeText(this, R.string.too_many_caches, Toast.LENGTH_LONG).show();
        } else {
            selectGeoCacheOverlay.clear();
            for (GeoCache geoCache : geoCacheList) {
                selectGeoCacheOverlay.addOverlayItem(new GeoCacheOverlayItem(geoCache, "", ""));
            }
            updateProgressStop();
            map.invalidate();
        }
    }

    public synchronized void groupUseAddGeoCacheList(List<GeoCache> geoCacheList) {
        if (geoCacheList == null || geoCacheList.size() == 0) {
            updateProgressStop();
            return;
        }
        cancelGroupTask();
        groupTask = new GroupGeoCacheTask(this, geoCacheList);
        groupTask.execute();
        groupingInfoTextView.setText("Группировка");
        updateProgressStop();
    }

    public synchronized void addOverlayItemList(List<GeoCacheOverlayItem> overlayItemList) {
        groupingInfoTextView.setText("-");
        selectGeoCacheOverlay.clear();
        for (GeoCacheOverlayItem item : overlayItemList) {
            selectGeoCacheOverlay.addOverlayItem(item);
        }
        map.invalidate();
    }

    public int getZoom() {
        return map.getZoomLevel();
    }

    public GeoPoint getCenter() {
        return map.getMapCenter();
    }

    @Override
    public void onConnectionLost() {
        connectionInfoTextView.setText(R.string.map_internet_lost);
    }

    @Override
    public void onConnectionFound() {
        connectionInfoTextView.setText("");
    }

    protected Dialog onCreateDialog(int id) {
        return (id == ENABLE_CONNECTION_DIALOG_ID) ? new EnableConnectionDialog(this) : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onWindowFocusChanged(boolean)
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && firstRun) {
            updateCacheOverlay();
            firstRun = false;
        }
        if (!progressBarAnimation.isRunning()) {
            progressBarAnimation.start();
        } else {
            progressBarAnimation.stop();
        }
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActivity(this);
    }

    public void onMyLocationClick(View v) {
        Location lastLocation = locationManager.getLastKnownLocation();
        if (lastLocation != null) {
            GeoPoint center = CoordinateHelper.locationToGeoPoint(lastLocation);
            map.getController().animateTo(center);
            map.invalidate();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.status_null_last_location), Toast.LENGTH_SHORT).show();
        }
    }

    public MapView getMapView() {
        return map;
    }

    @Override
    public void updateLocation(Location location) {
        updateLocationOverlay(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        locationManager.addSubscriber(this, false);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
