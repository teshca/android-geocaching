package su.geocaching.android.view.userStory.selectGeoCache;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.dataType.GeoCache;
import su.geocaching.android.view.geoCacheMap.GeoCacheItemizedOverlay;
import su.geocaching.android.view.geoCacheMap.GeoCacheMap;
import su.geocaching.android.view.userStory.showGeoCacheInfo.ShowGeoCacheInfo;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Author: Yuri Denison
 * Date: 04.11.2010 18:26:39
 */
public class SelectGeoCacheMap extends GeoCacheMap {
    public static final int DEFAULT_SEARCH_RADIUS = 10000; //in meters
    private static final int MENU_FILTER = 1;

    public static final int DEFAULT_ZOOM = 15;
    //TODO: set default zoom to radius 10km
    private Controller controller;
    private LinkedList<GeoCache> geoCacheList;
    private HashMap<Drawable, GeoCacheItemizedOverlay> cacheItemizedOverlays;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controller = Controller.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager.getCurrentLocation() != null) {
            GeoPoint locationPoint = new GeoPoint(
                    (int) (locationManager.getCurrentLocation().getLatitude() * 1E6),
                    (int) (locationManager.getCurrentLocation().getLongitude() * 1E6));
            mcMapController.animateTo(locationPoint);
            mcMapController.setCenter(locationPoint);
            updateCacheOverlay(locationPoint);
        }
        mvMap.invalidate();
    }

    /* Handles item selections */

    @Override
    public boolean onOptionsItemSelected(final android.view.MenuItem item) {
        switch (item.getItemId()) {
            case MENU_FILTER:
                showFilterMenu();
                return true;
        }
        return false;
    }

    private void showFilterMenu() {
        //TODO: implement filter menu
    }

    private void updateCacheOverlay(GeoPoint locationPoint) {
        geoCacheList = controller.getGeoCacheList(locationPoint.getLatitudeE6(), locationPoint.getLongitudeE6(),
                DEFAULT_SEARCH_RADIUS, controller.getFilterList());
        for (GeoCache geoCache : geoCacheList) {
            Drawable marker = controller.getMarker(geoCache, this);
            if (cacheItemizedOverlays.get(marker) == null) {
                cacheItemizedOverlays.put(marker, new GeoCacheItemizedOverlay(marker));
                //TODO: make markers on map clickable
            }
            cacheItemizedOverlays.get(marker).addOverlay(
                    new OverlayItem(geoCache.getLocation(), "", ""));
        }
        for (GeoCacheItemizedOverlay overlay : cacheItemizedOverlays.values()) {
            mapOverlays.add(overlay);
        }
        mvMap.invalidate();
    }

    private void startGeoCacheInfoView() {
        Intent intent = new Intent(this, ShowGeoCacheInfo.class);
        startActivity(intent);
        this.finish();
    }
}
