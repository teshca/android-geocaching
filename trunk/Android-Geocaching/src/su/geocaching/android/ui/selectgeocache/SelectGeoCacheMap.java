package su.geocaching.android.ui.selectgeocache;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.ShowGeoCacheInfo;
import su.geocaching.android.ui.geocachemap.*;
import su.geocaching.android.ui.selectgeocache.timer.MapUpdateTimer;
import su.geocaching.android.utils.GpsHelper;
import su.geocaching.android.utils.UiHelper;

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
    private GeoCacheItemizedOverlay gOverlay;
    private MapUpdateTimer mapTimer;
    private ConnectionManager internetManager;
    private Activity context;
    private Location currentLocation;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnimation;
    private int countDownloadTask;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_geocache_map);
        map = (MapView) findViewById(R.id.selectGeocacheMap);
        map.getOverlays().clear();

        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnimation = (AnimationDrawable) progressBarView.getBackground();
        progressBarView.setVisibility(View.GONE);
        countDownloadTask = 0;
        mHandler = new Handler();

        gOverlay = new GeoCacheItemizedOverlay(Controller.getInstance().getMarker(new GeoCache(), this), this);
        map.getOverlays().add(gOverlay);

        internetManager = Controller.getInstance().getConnectionManager(this);
        internetManager.addSubscriber(this);

        context = this;      
        askTurnOnInternet();
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
    }

    private synchronized void updateProgressStart() {
        if (countDownloadTask == 0) {
            mHandler.post(new Runnable() {
                public void run() {
                    progressBarView.setVisibility(View.VISIBLE);
                }
            });
        }
        countDownloadTask++;
    }

    private synchronized void updateProgressStop() {
        countDownloadTask--;
        if (countDownloadTask == 0) {
            Log.d(TAG, "set visible gone for progress");
            mHandler.post(new Runnable() {
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

        map.getController().setCenter(lastCenter);
        map.getController().animateTo(lastCenter);
        map.getController().setZoom(lastMapInfo[2]);
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
        map.getController().setZoom(savedInstanceState.getInt("zoom"));
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        internetManager.removeSubscriber(this);
        saveMapInfoToSettings();
        Log.d("mapInfo", "fucking save on pause");
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
                    map.getController().animateTo(center);
                    map.getController().setCenter(center);
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

        double maxLatitude = (double) upperLeftCorner.getLatitudeE6() / 1e6;
        double minLatitude = (double) lowerRightCorner.getLatitudeE6() / 1e6;
        double maxLongitude = (double) lowerRightCorner.getLongitudeE6() / 1e6;
        double minLongitude = (double) upperLeftCorner.getLongitudeE6() / 1e6;

        Controller.getInstance().updateSelectedGeoCaches(this, maxLatitude, minLatitude, maxLongitude, minLongitude);
        updateProgressStart();
    }

    private void startGeoCacheInfoView(GeoCache geoCache) {
        Intent intent = new Intent(this, ShowGeoCacheInfo.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        startActivity(intent);
    }

    @Override
    public void onGeoCacheItemTaped(GeoCacheOverlayItem item) {
        startGeoCacheInfoView(item.getGeoCache());
    }

    public void addGeoCacheList(List<GeoCache> geoCacheList) {
        if (geoCacheList == null) {
            return;
        }
        if (geoCacheList.size() > MAX_CACHE_NUMBER) {
     //       Toast.makeText(this.getBaseContext(), getString(R.string.too_small_zoom) + " " + geoCacheList.size(), Toast.LENGTH_LONG).show();
            geoCacheList = geoCacheList.subList(0, MAX_CACHE_NUMBER);
        }
        Log.d(TAG, "draw update cache overlay; count = " + countDownloadTask + "; size = " + geoCacheList.size());
        for (GeoCache geoCache : geoCacheList) {
            gOverlay.addOverlayItem(new GeoCacheOverlayItem(geoCache, "", "", this));
        }
        updateProgressStop();
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
        if (internetManager.isInternetConnected()) {
            Log.w(TAG, "Internet connected");
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
}
