package su.geocaching.android.ui.selectmap;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
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
import su.geocaching.android.controller.managers.*;
import su.geocaching.android.controller.selectmap.mapupdatetimer.MapUpdateTimer;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.ProgressBarView;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.preferences.MapPreferenceActivity;

import java.util.List;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class SelectMapActivity extends MapActivity implements IConnectionAware, ILocationAware {
    private static final String TAG = SelectMapActivity.class.getCanonicalName();
    private static final String SELECT_ACTIVITY_FOLDER = "/SelectActivity";
    private static final int ENABLE_CONNECTION_DIALOG_ID = 0;
    private static final int MAX_CACHE_NUMBER = 100;
    private MapView mapView;
    private SelectGeoCacheOverlay selectGeoCacheOverlay;
    private StaticUserLocationOverlay locationOverlay;
    private UserLocationManager locationManager;
    private ConnectionManager connectionManager;
    private ProgressBarView progressBarView;
    private MapUpdateTimer mapTimer;
    private TextView connectionInfoTextView;
    private TextView downloadingInfoTextView;
    private TextView groupingInfoTextView;

    private SelectMapViewModel selectMapViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.select_map_activity);

        mapView = (MapView) findViewById(R.id.selectGeocacheMap);

        progressBarView = (ProgressBarView) findViewById(R.id.progressCircle);
        progressBarView.setVisibility(View.GONE);

        connectionInfoTextView = (TextView) findViewById(R.id.connectionInfoTextView);
        groupingInfoTextView = (TextView) findViewById(R.id.groupingInfoTextView);
        downloadingInfoTextView = (TextView) findViewById(R.id.downloadingInfoTextView);

        locationOverlay = new StaticUserLocationOverlay(Controller.getInstance().getResourceManager().getUserLocationMarker());
        selectGeoCacheOverlay = new SelectGeoCacheOverlay(Controller.getInstance().getResourceManager().getCacheMarker(GeoCacheType.TRADITIONAL, GeoCacheStatus.VALID), this, mapView);
        mapView.getOverlays().add(selectGeoCacheOverlay);
        mapView.getOverlays().add(locationOverlay);

        locationManager = Controller.getInstance().getLocationManager();
        connectionManager = Controller.getInstance().getConnectionManager();

        mapView.setBuiltInZoomControls(true);
        mapView.invalidate();

        selectMapViewModel = Controller.getInstance().getSelectMapViewModel();
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(SELECT_ACTIVITY_FOLDER);
    }

    private void updateMapInfoFromSettings() {
        MapInfo lastMapInfo = Controller.getInstance().getPreferencesManager().getLastSelectMapInfo();
        GeoPoint lastCenter = new GeoPoint(lastMapInfo.getCenterX(), lastMapInfo.getCenterY());
        mapView.getController().setCenter(lastCenter);
        mapView.getController().animateTo(lastCenter);
        mapView.getController().setZoom(lastMapInfo.getZoom());
    }

    private void saveMapInfoToSettings() {
        MapInfo mapInfo = new MapInfo(mapView.getMapCenter().getLatitudeE6(), mapView.getMapCenter().getLongitudeE6(), mapView.getZoomLevel());
        Controller.getInstance().getPreferencesManager().setLastSelectMapInfo(mapInfo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManager.d(TAG, "onResume");
        // update mapView setting in case they were changed in preferences
        mapView.setSatellite(Controller.getInstance().getPreferencesManager().useSatelliteMap());
        // add subscriber to connection manager
        connectionManager.addSubscriber(this);
        // ask to enable if disabled
        if (!connectionManager.isActiveNetworkConnected()) {
            showDialog(ENABLE_CONNECTION_DIALOG_ID);
        }
        // add subscriber to location manager
        if (locationManager.getBestProvider(true) == null) {
            //NavigationManager.askTurnOnLocationService(this);
        } else {
            locationManager.addSubscriber(this);
        }
        // set user location
        updateLocationOverlay(locationManager.getLastKnownLocation());
        // update mapView center and zoom level
        updateMapInfoFromSettings();
        // register activity against view model
        selectMapViewModel.registerActivity(this);
        // schedule map update timer tasks
        mapTimer = new MapUpdateTimer(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogManager.d(TAG, "onPause");
        // cancel current map update timer and all it's tasks
        mapTimer.cancel();
        // unsubscribe  form location and connection manager
        locationManager.removeSubscriber(this);
        connectionManager.removeSubscriber(this);
        saveMapInfoToSettings();
        // don't keep reference to this activity in view model
        selectMapViewModel.unregisterActivity(this);
        progressBarView.stopAnimation();
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
            mapView.invalidate();
        }
        // TODO: if not
    }

    public void beginUpdateGeoCacheOverlay() {
        LogManager.d(TAG, "beginUpdateGeoCacheOverlay");
        final GeoPoint upperLeftCorner = mapView.getProjection().fromPixels(0, 0);
        final GeoPoint lowerRightCorner = mapView.getProjection().fromPixels(mapView.getWidth(), mapView.getHeight());
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        selectMapViewModel.beginUpdateGeocacheOverlay(upperLeftCorner, lowerRightCorner);
                    }
                }
        );
    }

    public synchronized void updateGeoCacheOverlay(List<GeoCacheOverlayItem> overlayItemList) {
        LogManager.d(TAG, "overlayItemList updated");
        selectGeoCacheOverlay.clear();
        if (overlayItemList.size() > MAX_CACHE_NUMBER) {
            Toast.makeText(this, R.string.too_many_caches, Toast.LENGTH_LONG).show();
        } else {
            selectGeoCacheOverlay.AddOverlayItems(overlayItemList);
        }
        mapView.invalidate();
    }

    public void hideGroupingInfo() {
        groupingInfoTextView.setVisibility(View.GONE);
        updateAnimation();
    }

    public void showGroupingInfo() {
        groupingInfoTextView.setVisibility(View.VISIBLE);
        updateAnimation();
    }

    public void hideDownloadingInfo() {
        downloadingInfoTextView.setVisibility(View.GONE);
        updateAnimation();
    }

    public void showDownloadingInfo() {
        downloadingInfoTextView.setVisibility(View.VISIBLE);
        updateAnimation();
    }

    private void updateAnimation() {
        if (downloadingInfoTextView.getVisibility() == View.VISIBLE || groupingInfoTextView.getVisibility() == View.VISIBLE) {
            progressBarView.setVisibility(View.VISIBLE);
        } else {
            progressBarView.setVisibility(View.GONE);
        }
    }

    public int getZoom() {
        return mapView.getZoomLevel();
    }

    public GeoPoint getCenter() {
        return mapView.getMapCenter();
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
        progressBarView.startAnimation();
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActivity(this);
    }

    public void onMyLocationClick(View v) {
        Location lastLocation = locationManager.getLastKnownLocation();
        if (lastLocation != null) {
            GeoPoint center = CoordinateHelper.locationToGeoPoint(lastLocation);
            mapView.getController().animateTo(center);
            mapView.invalidate();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.status_null_last_location), Toast.LENGTH_SHORT).show();
        }
    }

    public MapView getMapView() {
        return mapView;
    }

    @Override
    public void updateLocation(Location location) {
        updateLocationOverlay(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
