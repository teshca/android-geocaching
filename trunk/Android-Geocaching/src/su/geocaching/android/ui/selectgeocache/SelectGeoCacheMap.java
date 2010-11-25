package su.geocaching.android.ui.selectgeocache;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.maps.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheItemizedOverlay;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.geocachemap.IMapAware;
import su.geocaching.android.utils.Helper;
import su.geocaching.android.view.showgeocacheinfo.ShowGeoCacheInfo;

import java.util.List;

/**
 * @author Yuri Denison
 * @date 04.11.2010
 */
public class SelectGeoCacheMap extends MapActivity implements IMapAware {
    private static final String TAG = SelectGeoCacheMap.class.getCanonicalName();
    private final static int DEFAULT_ZOOM_VALUE = 13;

    private Controller controller;
    private UserLocationOverlay userOverlay;
    private MapView map;
    private List<Overlay> mapOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_geocache_map);
        map = (MapView) findViewById(R.id.selectGeocacheMap);
        mapOverlays = map.getOverlays();

        userOverlay = new UserLocationOverlay(this, map);
        userOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                map.getController().animateTo(Helper.locationToGeoPoint(userOverlay.getLastFix()));
                map.getController().setCenter(Helper.locationToGeoPoint(userOverlay.getLastFix()));
                userOverlay.onLocationChanged(userOverlay.getLastFix());
            }
        });

        controller = Controller.getInstance();

        map.setBuiltInZoomControls(true);
        map.getOverlays().clear();
        map.getOverlays().add(userOverlay);
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
        // TODO default zoom to 10km
        map.getController().setZoom(DEFAULT_ZOOM_VALUE);
    }

    @Override
    protected void onPause() {
        userOverlay.disableMyLocation();
        super.onPause();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /* Handles item selections */

    public void updateCacheOverlay(GeoPoint upperLeftCorner, GeoPoint lowerRightCorner) {
        Log.d(TAG, "updateCacheOverlay");
        // TODO add real visible area bounds
        double maxLatitude = (double) upperLeftCorner.getLatitudeE6() / 1e6;
        double minLatitude = (double) lowerRightCorner.getLatitudeE6() / 1e6;
        double maxLongitude = (double) lowerRightCorner.getLongitudeE6() / 1e6;
        double minLongitude = (double) upperLeftCorner.getLongitudeE6() / 1e6;

        controller.updateSelectedGeoCaches(this, maxLatitude, minLatitude, maxLongitude, minLongitude);
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
        GeoCacheItemizedOverlay gOverlay = new GeoCacheItemizedOverlay(
                controller.getMarker(geoCacheList.get(0), this), this);
        for (GeoCache geoCache : geoCacheList) {
            gOverlay.addOverlayItem(new GeoCacheOverlayItem(geoCache, "", "", this));
        }
        mapOverlays.add(gOverlay);
        map.invalidate();
    }
}
