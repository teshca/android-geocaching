package su.geocaching.android.ui.selectgeocache;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.maps.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheItemizedOverlay;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.geocachemap.IMapAware;
import su.geocaching.android.view.userstory.incocach.Info_cach;

import java.util.HashMap;
import java.util.List;

/**
 * Author: Yuri Denison Date: 04.11.2010 18:26:39
 */
public class SelectGeoCacheMap extends MapActivity implements IMapAware {
    private static SelectGeoCacheMap instance;
    private static final String TAG = SelectGeoCacheMap.class.getCanonicalName();
    private final static int DEFAULT_ZOOM_VALUE = 13;

    private Controller controller;
    private HashMap<GeoCacheType, GeoCacheItemizedOverlay> cacheItemizedOverlays;
    private UserLocationOverlay userOverlay;
    private MapView map;
    private MapController mapController;
    private List<Overlay> mapOverlays;

    public static SelectGeoCacheMap getInstance(){
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_geocache_map);
        map = (MapView) findViewById(R.id.selectGeocacheMap);
        mapController = map.getController();
        mapOverlays = map.getOverlays();
        cacheItemizedOverlays = new HashMap<GeoCacheType, GeoCacheItemizedOverlay>();

        userOverlay = new UserLocationOverlay(this, map);
        userOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                userOverlay.onLocationChanged(userOverlay.getLastFix());
            }
        });

        controller = Controller.getInstance();

        map.setBuiltInZoomControls(true);
        mapController = map.getController();
        map.getOverlays().clear();
        map.getOverlays().add(userOverlay);

        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        userOverlay.enableMyLocation();
        // TODO default zoom to 10km
        mapController.setZoom(DEFAULT_ZOOM_VALUE);
        GeoPoint center = userOverlay.getMyLocation();
        if (center != null) {
            mapController.animateTo(center);
        }
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
        List<GeoCache> geoCacheList = controller.getGeoCacheList(maxLatitude, minLatitude,
                maxLongitude, minLongitude);

        addGeoCacheList(geoCacheList);
    }

    private void startGeoCacheInfoView(GeoCache geoCache) {
        Intent intent = new Intent(this, Info_cach.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        startActivity(intent);
    }

    @Override
    public void onGeoCacheItemTaped(GeoCacheOverlayItem item) {
        // Toast.makeText(this, "Активити с информацией", Toast.LENGTH_SHORT).show();
        startGeoCacheInfoView(item.getGeoCache());
    }

    public void addGeoCacheList(List<GeoCache> geoCacheList) {
        for (GeoCache geoCache : geoCacheList) {
            if (cacheItemizedOverlays.get(geoCache.getType()) == null) {
                cacheItemizedOverlays.put(geoCache.getType(),
                        new GeoCacheItemizedOverlay(Controller.getInstance().getMarker(geoCache, this), this));
            }
            cacheItemizedOverlays.get(geoCache.getType()).addOverlayItem(new GeoCacheOverlayItem(geoCache, "", ""));
        }
        for (GeoCacheItemizedOverlay overlay : cacheItemizedOverlays.values()) {
            mapOverlays.add(overlay);
        }
        map.invalidate();
    }
}
