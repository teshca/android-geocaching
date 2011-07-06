package su.geocaching.android.ui.selectmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.maps.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.CoordinateHelper;
import su.geocaching.android.controller.managers.ConnectionManager;
import su.geocaching.android.controller.managers.IInternetAware;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.controller.selectmap.geocachegroup.GroupGeoCacheTask;
import su.geocaching.android.controller.selectmap.mapupdatetimer.MapUpdateTimer;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.geocachemap.MapPreferenceActivity;

import java.util.List;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class SelectMapActivity extends MapActivity implements IInternetAware {
    private static final String TAG = SelectMapActivity.class.getCanonicalName();
    private static final int MAX_CACHE_NUMBER = 300;
    private static final String SELECT_ACTIVITY_FOLDER = "/SelectActivity";
    private MyLocationOverlay userOverlay;
    private MapView map;
    private MapController mapController;
    private SelectGeoCacheOverlay selectGeoCacheOverlay;
    private MapUpdateTimer mapTimer;
    private ConnectionManager connectionManager;
    private Activity context;
    private Location currentLocation;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnimation;
    private int countDownloadTask;
    private Handler handler;
    private GroupGeoCacheTask groupTask = null;
    private boolean firstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.select_map_activity);
        map = (MapView) findViewById(R.id.selectGeocacheMap);
        map.getOverlays().clear();
        mapController = map.getController();

        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnimation = (AnimationDrawable) progressBarView.getBackground();
        progressBarView.setVisibility(View.GONE);
        countDownloadTask = 0;
        handler = new Handler();

        selectGeoCacheOverlay = new SelectGeoCacheOverlay(Controller.getInstance().getResourceManager().getMarker(GeoCacheType.TRADITIONAL, GeoCacheStatus.VALID), this, map);
        map.getOverlays().add(selectGeoCacheOverlay);

        connectionManager = Controller.getInstance().getConnectionManager();

        context = this;
        userOverlay = new MyLocationOverlay(this, map) {
            @Override
            public void onLocationChanged(Location location) {
                super.onLocationChanged(location);
                currentLocation = location;
            }
        };

        map.setBuiltInZoomControls(true);
        map.getOverlays().add(userOverlay);
        map.invalidate();
        LogManager.d(TAG, "onCreate Done");

        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(SELECT_ACTIVITY_FOLDER);
    }

    private synchronized void updateProgressStart() {
        if (countDownloadTask == 0) {
            LogManager.d(TAG, "set visible Visible for progress");
            handler.post(new Runnable() {
                public void run() {
                    progressBarView.setVisibility(View.VISIBLE);
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
                }
            });
        }
    }

    private void updateMapInfoFromSettings() {
        MapInfo lastMapInfo = Controller.getInstance().getPreferencesManager().getLastSelectMapInfo();
        GeoPoint lastCenter = new GeoPoint(lastMapInfo.getCenterX(), lastMapInfo.getCenterY());
        mapController.setCenter(lastCenter);
        mapController.animateTo(lastCenter);
        mapController.setZoom(lastMapInfo.getZoom());
        map.invalidate();
    }

    private void saveMapInfoToSettings() {
        Controller.getInstance().getPreferencesManager().setLastSelectMapInfo(new MapInfo(map.getMapCenter().getLatitudeE6(), map.getMapCenter().getLongitudeE6(), map.getZoomLevel()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        askTurnOnInternet();
        map.setSatellite(Controller.getInstance().getPreferencesManager().useSatelliteMap());
        connectionManager.addSubscriber(this);
        userOverlay.enableMyLocation();
        selectGeoCacheOverlay.clear();
        map.invalidate();
        updateMapInfoFromSettings();
        mapTimer = new MapUpdateTimer(this);

        selectGeoCacheOverlay.clear();
        updateCacheOverlay();

        map.invalidate();
    }

    @Override
    protected void onPause() {
        userOverlay.disableMyLocation();
        mapTimer.cancel();
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
            case R.id.revertCenterToLocation:
                if (currentLocation != null) {
                    GeoPoint center = CoordinateHelper.locationToGeoPoint(currentLocation);
                    mapController.animateTo(center);
                } else {
                    Toast.makeText(getBaseContext(), R.string.status_null_last_location, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.mapSettings:
                startActivity(new Intent(this, MapPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Handles item selections */

    public void updateCacheOverlay() {
        LogManager.d(TAG, "updateCacheOverlay; count = " + countDownloadTask);
        GeoPoint upperLeftCorner = map.getProjection().fromPixels(0, 0);
        GeoPoint lowerRightCorner = map.getProjection().fromPixels(map.getWidth(), map.getHeight());
        Controller.getInstance().updateSelectedGeoCaches(this, upperLeftCorner, lowerRightCorner);
        updateProgressStart();
    }

    public void simpleAddGeoCacheList(List<GeoCache> geoCacheList) {
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

    public void groupUseAddGeoCacheList(List<GeoCache> geoCacheList) {
        if (geoCacheList == null || geoCacheList.size() == 0) {
            updateProgressStop();
            return;
        }
        if (groupTask != null && groupTask.getStatus() != AsyncTask.Status.FINISHED) {
            LogManager.d(TAG, "task canceled");
            groupTask.cancel(false);
        }
        groupTask = new GroupGeoCacheTask(this, geoCacheList);
        groupTask.execute();
        updateProgressStop();
    }

    public void addOverlayItemList(List<GeoCacheOverlayItem> overlayItemList) {
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
    public void onInternetLost() {
        Toast.makeText(this, getString(R.string.search_geocache_internet_lost), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInternetFound() {
    }

    /**
     * Ask user turn on Internet, if this disabled
     */
    private void askTurnOnInternet() {
        if (connectionManager.isInternetConnected()) {
            LogManager.d(TAG, "Internet connected");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.ask_enable_internet_text)).setCancelable(false).setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent startGPS = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                context.startActivity(startGPS);
                dialog.cancel();
            }
        }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog turnOnInternetAlert = builder.create();
        turnOnInternetAlert.show();
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
        NavigationManager.goHome(this);
    }

    public MapView getMapView() {
        return map;
    }
}
