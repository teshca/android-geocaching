package su.geocaching.android.ui.selectgeocache;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.maps.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.*;
import su.geocaching.android.ui.selectgeocache.timer.MapUpdateTimer;
import su.geocaching.android.utils.Helper;
import su.geocaching.android.view.showgeocacheinfo.ShowGeoCacheInfo;

import java.util.List;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class SelectGeoCacheMap extends MapActivity implements IMapAware, IInternetAware {
    private static final String TAG = SelectGeoCacheMap.class.getCanonicalName();

    private Controller controller;
    private MyLocationOverlay userOverlay;
    private MapView map;
    private List<Overlay> mapOverlays;
    private boolean touchHappened;
    private MapUpdateTimer mapTimer;
    private ConnectionManager internetManager;
    private Activity context;
    private ImageView progressCircle;
    private Location currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_geocache_map);
        map = (MapView) findViewById(R.id.selectGeocacheMap);
        progressCircle = (ImageView) findViewById(R.id.progressCircle);
        mapOverlays = map.getOverlays();
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

            @Override
            public boolean onTouchEvent(MotionEvent event, MapView mapView) {
                touchHappened = true;
                return false;
            }
        };
        userOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                updateMapInfoFromSettings();
                updateCacheOverlay();
            }
        });

        controller = Controller.getInstance();

        map.setBuiltInZoomControls(true);
        map.getOverlays().clear();
        map.getOverlays().add(userOverlay);
        // initProgressCircle();
        map.invalidate();
    }

    private void initProgressCircle() {
        Animation progressCircleAnim = AnimationUtils.loadAnimation(this, R.anim.progress_circle);
        progressCircle.startAnimation(progressCircleAnim);
    }



    private void showProgressCircle() {
        progressCircle.setVisibility(View.VISIBLE);
    }

    private void stopProgressCircle() {
        progressCircle.setVisibility(View.GONE);
    }

    private void updateMapInfoFromSettings() {
        int[] lastMapInfo = this.getIntent().getExtras().getIntArray("map_info");
        GeoPoint lastCenter = new GeoPoint(lastMapInfo[0], lastMapInfo[1]);
        map.getController().setCenter(lastCenter);
        map.getController().animateTo(lastCenter);
        map.getController().setZoom(lastMapInfo[2]);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("zoom", map.getZoomLevel());
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

        touchHappened = false;
        mapTimer = new MapUpdateTimer(this);
        updateCacheOverlay();
    }

    @Override
    protected void onPause() {
        userOverlay.disableMyLocation();
        mapTimer.cancel();
        internetManager.removeSubscriber(this);
        controller.setLastMapInfo(map.getMapCenter(), map.getZoomLevel(), this);
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
                    GeoPoint center = Helper.locationToGeoPoint(currentLocation);
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
        Log.d(TAG, "updateCacheOverlay");
        GeoPoint upperLeftCorner = map.getProjection().fromPixels(0, 0);
        GeoPoint lowerRightCorner = map.getProjection().fromPixels(map.getWidth(), map.getHeight());

        double maxLatitude = (double) upperLeftCorner.getLatitudeE6() / 1e6;
        double minLatitude = (double) lowerRightCorner.getLatitudeE6() / 1e6;
        double maxLongitude = (double) lowerRightCorner.getLongitudeE6() / 1e6;
        double minLongitude = (double) upperLeftCorner.getLongitudeE6() / 1e6;

        controller.updateSelectedGeoCaches(this, maxLatitude, minLatitude, maxLongitude, minLongitude);
        // showProgressCircle();
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
        if (geoCacheList == null || geoCacheList.size() == 0) {
            return;
        }
        GeoCacheItemizedOverlay gOverlay = new GeoCacheItemizedOverlay(
                controller.getMarker(geoCacheList.get(0), this), this);
        for (GeoCache geoCache : geoCacheList) {
            gOverlay.addOverlayItem(new GeoCacheOverlayItem(geoCache, "", "", this));
        }
        mapOverlays.add(gOverlay);
        // stopProgressCircle();
        map.invalidate();
    }

    public int getZoom() {
        return map.getZoomLevel();
    }

    public GeoPoint getCenter() {
        return map.getMapCenter();
    }

    public boolean touchHappened() {
        return touchHappened;
    }

    public void setTouchHappened(boolean touchHappened) {
        this.touchHappened = touchHappened;
    }

    @Override
    public void onInternetLost() {
        Toast.makeText(this, getString(R.string.search_geocache_internet_lost), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInternetFound() {
        // TODO: do smth?
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
                context.finish();
            }
        });
        AlertDialog turnOnInternetAlert = builder.create();
        turnOnInternetAlert.show();
    }
}
