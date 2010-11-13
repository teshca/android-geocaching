package su.geocaching.android.view.userstory.selectgeocache;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.view.geocachemap.GeoCacheItemizedOverlay;
import su.geocaching.android.view.geocachemap.GeoCacheMap;
import su.geocaching.android.view.userstory.showgeocacheinfo.ShowGeoCacheInfo;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Author: Yuri Denison Date: 04.11.2010 18:26:39
 */
public class SelectGeoCacheMap extends GeoCacheMap {
    public static final int DEFAULT_SEARCH_RADIUS = 10000; // in meters
    private static final int MENU_FILTER = 1;

    public static final int DEFAULT_ZOOM = 15;
    // TODO: set default zoom to radius 10km
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

	// TODO: Deprecated. it's already doing in updateLocation.
	if (locationManager.getCurrentLocation() != null) {
	    GeoPoint locationPoint = new GeoPoint((int) (locationManager.getCurrentLocation().getLatitude() * 1E6), (int) (locationManager.getCurrentLocation().getLongitude() * 1E6));
	    mapController.animateTo(locationPoint);
	    mapController.setCenter(locationPoint);
	    updateCacheOverlay(locationPoint);
	}
	map.invalidate();
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

    private void updateCacheOverlay(GeoPoint locationPoint) {
	// TODO add real visible area bounds
	double maxLatitude = (double) locationPoint.getLatitudeE6() / 1e6 + 2;
	double minLatitude = (double) locationPoint.getLatitudeE6() / 1e6 - 2;
	double maxLongitude = (double) locationPoint.getLongitudeE6() / 1e6 + 2;
	double minLongitude = (double) locationPoint.getLongitudeE6() / 1e6 - 2;
	geoCacheList = controller.getGeoCacheList(maxLatitude, minLatitude, maxLongitude, minLongitude, controller.getFilterList());
	for (GeoCache geoCache : geoCacheList) {
	    Drawable marker = controller.getMarker(geoCache, this);
	    if (cacheItemizedOverlays.get(marker) == null) {
		cacheItemizedOverlays.put(marker, new GeoCacheItemizedOverlay(marker));
		// TODO: make markers on map clickable
	    }
	    cacheItemizedOverlays.get(marker).addOverlay(new OverlayItem(geoCache.getLocationGeoPoint(), "", ""));
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
