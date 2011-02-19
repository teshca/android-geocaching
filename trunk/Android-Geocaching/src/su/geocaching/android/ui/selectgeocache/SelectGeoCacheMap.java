package su.geocaching.android.ui.selectgeocache;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.ShowGeoCacheInfo;
import su.geocaching.android.ui.geocachemap.*;
import su.geocaching.android.ui.selectgeocache.geocachegroup.GeoCacheListAnalyzer;
import su.geocaching.android.ui.selectgeocache.geocachegroup.GroupCacheTask;
import su.geocaching.android.ui.selectgeocache.timer.MapUpdateTimer;
import su.geocaching.android.utils.GpsHelper;
import su.geocaching.android.utils.UiHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class SelectGeoCacheMap extends MapActivity implements IMapAware, IInternetAware {
    private static final String TAG = SelectGeoCacheMap.class.getCanonicalName();
    private static final int MAX_CACHE_NUMBER = 100;

    private MyLocationOverlay userOverlay;
    private MapView map;
    private MapController mapController;
    private GeoCacheItemizedOverlay gOverlay;
    private MapUpdateTimer mapTimer;
    private ConnectionManager connectionManager;
    private Activity context;
    private Location currentLocation;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnimation;
    private int countDownloadTask;
    private Handler handler;
    private GoogleAnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_geocache_map);
        map = (MapView) findViewById(R.id.selectGeocacheMap);
        map.getOverlays().clear();
        mapController = map.getController();

        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnimation = (AnimationDrawable) progressBarView.getBackground();
        progressBarView.setVisibility(View.GONE);
        countDownloadTask = 0;
        handler = new Handler();

        gOverlay = new GeoCacheItemizedOverlay(Controller.getInstance().getMarker(new GeoCache(), this), this);
        map.getOverlays().add(gOverlay);

        connectionManager = Controller.getInstance().getConnectionManager(this);
        connectionManager.addSubscriber(this);

        context = this;
        askTurnOnInternet();
        userOverlay = new MyLocationOverlay(this, map) {
            @Override
            public void onLocationChanged(Location location) {
                super.onLocationChanged(location);
                currentLocation = location;
            }
        };

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start("UA-20327116-3", this);
        tracker.trackPageView("/selectActivity");

        map.setBuiltInZoomControls(true);
        map.getOverlays().add(userOverlay);
        map.invalidate();
    }

    private synchronized void updateProgressStart() {
        if (countDownloadTask == 0) {
            Log.d(TAG, "set visible Visible for progress");
            handler.post(new Runnable() {
                public void run() {
                    progressBarView.setVisibility(View.VISIBLE);
                }
            });
        }
        Log.d(TAG, "count plus. count = " + countDownloadTask);
        countDownloadTask++;
    }

    private synchronized void updateProgressStop() {
        countDownloadTask--;
        Log.d(TAG, "count minus. count = " + countDownloadTask);
        if (countDownloadTask == 0) {
            Log.d(TAG, "set visible gone for progress");
            handler.post(new Runnable() {
                public void run() {
                    progressBarView.setVisibility(View.GONE);
                }
            });
        }
    }

    private void updateMapInfoFromSettings() {
        int[] lastMapInfo = Controller.getInstance().getLastMapInfo(this);
        GeoPoint lastCenter = new GeoPoint(lastMapInfo[0], lastMapInfo[1]);
        Log.d("mapInfo", "X = " + lastMapInfo[0]);
        Log.d("mapInfo", "Y = " + lastMapInfo[1]);
        Log.d("mapInfo", "zoom = " + lastMapInfo[2]);

        mapController.setCenter(lastCenter);
        mapController.animateTo(lastCenter);
        mapController.setZoom(lastMapInfo[2]);
        map.invalidate();
    }

    private void saveMapInfoToSettings() {
        Controller.getInstance().setLastMapInfo(map.getMapCenter(), map.getZoomLevel(), this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mapController.setZoom(savedInstanceState.getInt("zoom"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.start("UA-20327116-3", this);
        tracker.trackPageView("/selectActivity");

        userOverlay.enableMyLocation();
        updateMapInfoFromSettings();
        mapTimer = new MapUpdateTimer(this);
        updateCacheOverlay();
        map.invalidate();
    }

    @Override
    protected void onPause() {
        userOverlay.disableMyLocation();
        mapTimer.cancel();
        connectionManager.removeSubscriber(this);
        saveMapInfoToSettings();
        tracker.stop();
        Log.d("mapInfo", "save on pause");
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
        inflater.inflate(R.menu.select_geocache_map, menu);
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
                    GeoPoint center = GpsHelper.locationToGeoPoint(currentLocation);
                    mapController.animateTo(center);
                    mapController.setCenter(center);
                } else {
                    Toast.makeText(getBaseContext(), R.string.status_null_last_location, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Handles item selections */

    public void updateCacheOverlay() {
        Log.d(TAG, "updateCacheOverlay; count = " + countDownloadTask);
        GeoPoint upperLeftCorner = map.getProjection().fromPixels(0, 0);
        GeoPoint lowerRightCorner = map.getProjection().fromPixels(map.getWidth(), map.getHeight());
        Controller.getInstance().updateSelectedGeoCaches(this, upperLeftCorner, lowerRightCorner);
        updateProgressStart();
    }

    private void startGeoCacheInfoView(GeoCache geoCache) {
        Intent intent = new Intent(this, ShowGeoCacheInfo.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        startActivity(intent);
    }

    @Override
    public void onGeoCacheItemTaped(GeoCacheOverlayItem item) {
        if (!item.getTitle().equals("Group")) {
            startGeoCacheInfoView(item.getGeoCache());
        } else {
            gOverlay.remove(item);
            mapController.animateTo(item.getGeoCache().getLocationGeoPoint());
            mapController.zoomIn();
            map.invalidate();
            testAddGeoCacheList(item.getGeoCacheList());
            map.invalidate();
        }
    }

    public void addGeoCacheList(List<GeoCache> geoCacheList) {
        if (geoCacheList == null || geoCacheList.size() == 0) {
            updateProgressStop();
            return;
        }
        if (geoCacheList.size() > MAX_CACHE_NUMBER) {
            geoCacheList = geoCacheList.subList(0, MAX_CACHE_NUMBER);
        }

        Log.d(TAG, "draw update cache overlay; count = " + countDownloadTask + "; size = " + geoCacheList.size());
        for (GeoCache geoCache : geoCacheList) {
            gOverlay.addOverlayItem(new GeoCacheOverlayItem(geoCache, "", "", this));
        }
        updateProgressStop();
        map.invalidate();
    }

    public void testAddGeoCacheList(List<GeoCache> geoCacheList) {
        if (geoCacheList == null || geoCacheList.size() == 0) {
            updateProgressStop();
            return;
        }
        Log.d(TAG, "draw update cache overlay; count = " + countDownloadTask + "; size = " + geoCacheList.size());
        Log.d("GroupCacheTask", "execute task, size = " + geoCacheList.size());
        new GroupCacheTask(this, geoCacheList).execute();
        Log.d(TAG, "Adding completed.");
        updateProgressStop();
        Log.d(TAG, "progress stopped.");
    }

    public void addOverlayItemList(List<GeoCacheOverlayItem> overlayItemList) {
        gOverlay.clear();
        for (GeoCacheOverlayItem item : overlayItemList) {
            gOverlay.addOverlayItem(item);
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
            Log.d(TAG, "Internet connected");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.ask_enable_internet_text)).setCancelable(false)
            .setPositiveButton(context.getString(R.string.ask_enable_internet_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent startGPS = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    context.startActivity(startGPS);
                    dialog.cancel();
                }
            }).setNegativeButton(context.getString(R.string.ask_enable_internet_no), new DialogInterface.OnClickListener() {
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
        if (hasFocus) {
            updateCacheOverlay();
        }
        progressBarAnimation.start();
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }

    public MapView getMapView() {
        return map;
    }
}
