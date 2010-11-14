package su.geocaching.android.view.userstory.selectgeocache;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.view.geocachemap.GeoCacheItemizedOverlay;
import su.geocaching.android.view.geocachemap.GeoCacheMap;
import su.geocaching.android.view.userstory.showgeocacheinfo.ShowGeoCacheInfo;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Author: Yuri Denison Date: 04.11.2010 18:26:39
 */
public class SelectGeoCacheMap extends GeoCacheMap {

    private static final String TAG = SelectGeoCacheMap.class.getCanonicalName();

    public static final int DEFAULT_SEARCH_RADIUS = 10000; // in meters
    private static final int MENU_FILTER = 1;

    public static final int DEFAULT_ZOOM = 5;
    // TODO: set default zoom to radius 10km
    private Controller controller;
    private LinkedList<GeoCache> geoCacheList;
    private HashMap<GeoCacheType, GeoCacheItemizedOverlay> cacheItemizedOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	controller = Controller.getInstance();
	cacheItemizedOverlays = new HashMap<GeoCacheType, GeoCacheItemizedOverlay>();
	mapController.setZoom(DEFAULT_ZOOM);
    }

    @Override
    protected void onResume() {
	super.onResume();
	locationManager.resume();

	// TODO: Deprecated. it's already doing in updateLocation.
	if (locationManager.getCurrentLocation() != null) {
	    GeoPoint locationPoint = new GeoPoint((int) (locationManager.getCurrentLocation().getLatitude() * 1E6), (int) (locationManager.getCurrentLocation().getLongitude() * 1E6));
	    mapController.animateTo(locationPoint);
	    mapController.setCenter(locationPoint);
	    updateCacheOverlay(map.getProjection().fromPixels(0, 0), map.getProjection().fromPixels(map.getRight(), map.getBottom()));
	}
	map.invalidate();
    }

    @Override
    protected void onPause() {
	super.onPause();
	locationManager.pause();
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
	// TODO: implement filter menu
    }

    private void updateCacheOverlay(GeoPoint upperLeftCorner, GeoPoint lowerRightCorner) {
	Log.d(TAG, "updateCacheOverlay");
	// TODO add real visible area bounds
	double maxLatitude = (double) upperLeftCorner.getLatitudeE6() / 1e6;
	double minLatitude = (double) lowerRightCorner.getLatitudeE6() / 1e6;
	double maxLongitude = (double) lowerRightCorner.getLongitudeE6() / 1e6;
	double minLongitude = (double) upperLeftCorner.getLongitudeE6() / 1e6;
	geoCacheList = controller.getGeoCacheList(maxLatitude, minLatitude, maxLongitude, minLongitude, controller.getFilterList());

	// TODO:
	Drawable marker = controller.getMarker(new GeoCache(), this);
	if (geoCacheList != null) {
	    marker = controller.getMarker(geoCacheList.get(0), this);
	}
	// -------------

	for (GeoCache geoCache : geoCacheList) {
	    // This code creates the N overlay with N overlayItem
	    // Drawable marker = controller.getMarker(geoCache, this);
	    // if (cacheItemizedOverlays.get(marker) == null) {
	    // cacheItemizedOverlays.put(marker, new
	    // GeoCacheItemizedOverlay(marker));
	    // // TODO: make markers on map clickable
	    // }

	    if (cacheItemizedOverlays.get(geoCache.getType()) == null) {
		cacheItemizedOverlays.put(geoCache.getType(), new GeoCacheItemizedOverlay(marker));
		// TODO: make markers on map clickable
	    }
	    cacheItemizedOverlays.get(geoCache.getType()).addOverlayItem(new OverlayItem(geoCache.getLocationGeoPoint(), "", ""));
	}
	for (GeoCacheItemizedOverlay overlay : cacheItemizedOverlays.values()) {
	    mapOverlays.add(overlay);
	}
	map.invalidate();
    }

    private void startGeoCacheInfoView() {
	Intent intent = new Intent(this, ShowGeoCacheInfo.class);
	startActivity(intent);
	this.finish();
    }

    @Override
    public void updateLocation(Location location) {
	// TODO Auto-generated method stub

    }

    @Override
    public Location getLastLocation() {
	return locationManager.getCurrentLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
	// TODO Auto-generated method stub

    }
}
