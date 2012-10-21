package su.geocaching.android.ui.selectmap;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.GeoRect;
import su.geocaching.android.controller.managers.*;
import su.geocaching.android.controller.selectmap.mapupdatetimer.MapUpdateTimer;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.preferences.MapPreferenceActivity;

import java.util.List;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class SelectMapActivity extends SherlockMapActivity implements IConnectionAware, ILocationAware {
    private static final String TAG = SelectMapActivity.class.getCanonicalName();
    private static final String SELECT_ACTIVITY_FOLDER = "/SelectActivity";
    private static final int ENABLE_CONNECTION_DIALOG_ID = 0;
    private MapView mapView;
    private SelectGeoCacheOverlay selectGeoCacheOverlay;
    private StaticUserLocationOverlay locationOverlay;
    private LowPowerUserLocationManager locationManager;
    private ConnectionManager connectionManager;
    private ProgressBar progressCircle;
    private MapUpdateTimer mapTimer;
    private TextView connectionInfoTextView;
    private TextView downloadingInfoTextView;
    private TextView groupingInfoTextView;

    private Toast tooManyCachesToast;
    private Toast statusNullLastLocationToast;

    private SelectMapViewModel selectMapViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");

        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.select_map_activity);
        getSupportActionBar().setHomeButtonEnabled(true);

        mapView = (MapView) findViewById(R.id.selectGeocacheMap);

        progressCircle = (ProgressBar) findViewById(R.id.progressCircle);

        connectionInfoTextView = (TextView) findViewById(R.id.connectionInfoTextView);
        groupingInfoTextView = (TextView) findViewById(R.id.groupingInfoTextView);
        downloadingInfoTextView = (TextView) findViewById(R.id.downloadingInfoTextView);

        locationOverlay = new StaticUserLocationOverlay(Controller.getInstance().getResourceManager().getUserLocationMarker());
        selectGeoCacheOverlay = new SelectGeoCacheOverlay(Controller.getInstance().getResourceManager().getCacheMarker(GeoCacheType.TRADITIONAL, GeoCacheStatus.VALID), this, mapView);
        mapView.getOverlays().add(selectGeoCacheOverlay);
        mapView.getOverlays().add(locationOverlay);

        locationManager = Controller.getInstance().getLowPowerLocationManager();
        connectionManager = Controller.getInstance().getConnectionManager();

        boolean isMultiTouchAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
        mapView.setBuiltInZoomControls(!isMultiTouchAvailable);
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
        locationManager.addSubscriber(this);
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
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.select_map_menu, menu);
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
            case R.id.menu_settings:
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
    }

    public void beginUpdateGeoCacheOverlay() {
        LogManager.d(TAG, "beginUpdateGeoCacheOverlay");
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        GeoRect viewPort = getCurrentGeoRect();
                        selectMapViewModel.beginUpdateGeocacheOverlay(viewPort, mapView.getProjection(), mapView.getWidth(), mapView.getHeight());
                    }
                }
        );
    }

    private GeoRect getCurrentGeoRect()
    {
        final int MAX_LONG = (int) 180E6;
        GeoPoint tl = mapView.getProjection().fromPixels(0, 0);
        GeoPoint br = mapView.getProjection().fromPixels(mapView.getWidth(), mapView.getHeight());
        if (mapView.getLongitudeSpan() >= 2 * MAX_LONG)
        {
            tl = new GeoPoint(tl.getLatitudeE6(), -MAX_LONG + 1);
            br = new GeoPoint(br.getLatitudeE6(), MAX_LONG);
        }
        return new GeoRect(tl, br);
    }

    public synchronized void updateGeoCacheOverlay(List<GeoCacheOverlayItem> overlayItemList) {
        LogManager.d(TAG, "overlayItemList updated; size: %d", overlayItemList.size());
        selectGeoCacheOverlay.clear();
        hideTooMayCachesToast();
        selectGeoCacheOverlay.AddOverlayItems(overlayItemList);
        mapView.invalidate();
    }

    public void tooManyOverlayItems() {
        showTooMayCachesToast();
        selectGeoCacheOverlay.clear();
        mapView.invalidate();
    }

    private void showTooMayCachesToast() {
        if (tooManyCachesToast == null) {
            tooManyCachesToast = Toast.makeText(this, R.string.too_many_caches, Toast.LENGTH_LONG);
        }
        tooManyCachesToast.show();
    }

    private void hideTooMayCachesToast() {
        if (tooManyCachesToast != null) {
            tooManyCachesToast.cancel();
        }
    }

    public void hideGroupingInfo() {
        groupingInfoTextView.setVisibility(View.INVISIBLE);
        updateProgressCircleVisibility();
    }

    public void showGroupingInfo() {
        groupingInfoTextView.setVisibility(View.VISIBLE);
        updateProgressCircleVisibility();
    }

    public void hideDownloadingInfo() {
        downloadingInfoTextView.setVisibility(View.GONE);
        updateProgressCircleVisibility();
    }

    public void showDownloadingInfo() {
        downloadingInfoTextView.setVisibility(View.VISIBLE);
        updateProgressCircleVisibility();
    }

    private void updateProgressCircleVisibility() {
        if (downloadingInfoTextView.getVisibility() == View.VISIBLE || groupingInfoTextView.getVisibility() == View.VISIBLE) {
            //setSupportProgressBarIndeterminateVisibility(true);
            progressCircle.setVisibility(View.VISIBLE);
        } else {
            //setSupportProgressBarIndeterminateVisibility(false);
            progressCircle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionLost() {
        connectionInfoTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionFound() {
        // TODO: make text sorter and use INVISIBLE instead of GONE
        connectionInfoTextView.setVisibility(View.GONE);
    }

    protected Dialog onCreateDialog(int id) {
        return (id == ENABLE_CONNECTION_DIALOG_ID) ? new EnableConnectionDialog(this) : null;
    }

    private void onMyLocationClick() {
        final Location lastLocation = locationManager.getLastKnownLocation();
        if (lastLocation != null) {
            GeoPoint center = CoordinateHelper.locationToGeoPoint(lastLocation);
            mapView.getController().animateTo(center);
            mapView.invalidate();
        } else {
            if (statusNullLastLocationToast == null) {
                statusNullLastLocationToast = Toast.makeText(getBaseContext(), getString(R.string.status_null_last_location), Toast.LENGTH_SHORT);
            }
            statusNullLastLocationToast.show();
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