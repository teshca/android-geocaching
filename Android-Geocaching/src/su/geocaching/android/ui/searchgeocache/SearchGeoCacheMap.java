package su.geocaching.android.ui.searchgeocache;

import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheItemizedOverlay;
import su.geocaching.android.utils.Helper;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @description Search GeoCache with the map.
 * @since October 2010
 */
public class SearchGeoCacheMap extends MapActivity implements ISearchActivity {
    private OverlayItem cacheOverlayItem;
    private GeoCacheItemizedOverlay cacheItemizedOverlay;
    private DistanceToGeoCacheOverlay distanceOverlay;
    private UserLocationOverlay userOverlay;
    private TextView statusTextView;
    private MapView map;
    private MapController mapController;
    private List<Overlay> mapOverlays;
    private SearchGeoCacheManager manager;

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.search_geocache_map);
	statusTextView = (TextView) findViewById(R.id.statusTextView);
	map = (MapView) findViewById(R.id.searchGeocacheMap);
	mapOverlays = map.getOverlays();
	mapController = map.getController();
	userOverlay = new UserLocationOverlay(this, map);
	manager = new SearchGeoCacheManager(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#onPause()
     */
    @Override
    protected void onPause() {
	super.onPause();
	manager.onPause();
	if (manager.isLocationFixed()) {
	    userOverlay.disableCompass();
	    userOverlay.disableMyLocation();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	manager.onResume();
    }

    /**
     * Init and run all activity content
     */
    @Override
    public void runLogic() {
	manager.runLogic();
	if (manager.getGeoCache() == null) {
	    return;
	}
	userOverlay.enableCompass();
	userOverlay.enableMyLocation();

	Drawable cacheMarker = this.getResources().getDrawable(R.drawable.orangecache);
	cacheMarker.setBounds(0, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth(), 0);

	cacheItemizedOverlay = new GeoCacheItemizedOverlay(cacheMarker);
	cacheOverlayItem = new OverlayItem(manager.getGeoCache().getLocationGeoPoint(), "", "");
	cacheItemizedOverlay.addOverlayItem(cacheOverlayItem);
	mapOverlays.add(cacheItemizedOverlay);
	if (!manager.isLocationFixed()) {
	    mapController.animateTo(manager.getGeoCache().getLocationGeoPoint());
	}

	map.invalidate();
    }

    /**
     * Start SearchGeoCacheCompass activity
     */
    public void startCompassView() {
	Intent intent = new Intent(this, SearchGeoCacheCompass.class);
	if ((manager != null) && (manager.getGeoCache() != null)) {
	    intent.putExtra(GeoCache.class.getCanonicalName(), manager.getGeoCache());
	}
	if (manager != null) {
	    intent.putExtra("location fixed", manager.isLocationFixed());
	}
	startActivity(intent);
	this.finish();
    }

    @Override
    public void updateLocation(Location location) {
	userOverlay.onLocationChanged(location);

	if (distanceOverlay == null) {
	    // It's really first run of update location
	    resetZoom();
	    distanceOverlay = new DistanceToGeoCacheOverlay(Helper.locationToGeoPoint(location), manager.getGeoCache().getLocationGeoPoint());
	    distanceOverlay.setCachePoint(manager.getGeoCache().getLocationGeoPoint());
	    mapOverlays.add(distanceOverlay);
	    mapOverlays.add(userOverlay);
	    return;
	}
	distanceOverlay.setUserPoint(Helper.locationToGeoPoint(location));
	map.invalidate();
    }

    /**
     * Set map zoom which can show userPoint and GeoCachePoint
     */
    private void resetZoom() {
	GeoPoint currentGeoPoint = Helper.locationToGeoPoint(manager.getCurrentLocation());
	mapController.zoomToSpan(Math.abs(manager.getGeoCache().getLocationGeoPoint().getLatitudeE6() - currentGeoPoint.getLatitudeE6()),
		Math.abs(manager.getGeoCache().getLocationGeoPoint().getLongitudeE6() - currentGeoPoint.getLongitudeE6()));

	GeoPoint center = new GeoPoint((manager.getGeoCache().getLocationGeoPoint().getLatitudeE6() + currentGeoPoint.getLatitudeE6()) / 2, (manager.getGeoCache().getLocationGeoPoint()
		.getLongitudeE6() + currentGeoPoint.getLongitudeE6()) / 2);
	mapController.animateTo(center);
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.search_geocache_map, menu);
	return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menuDefaultZoom:
	    if (manager.isLocationFixed()) {
		resetZoom();
	    }
	    return true;
	case R.id.menuStartCompass:
	    this.startCompassView();
	    return true;
	case R.id.menuToggleShortestWay:
	    distanceOverlay.toggleShorteshtWayVisible();
	    return true;
	case R.id.menuGeoCacheInfo:
	    manager.showGeoCacheInfo();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#updateStatus(
     * java.lang.String)
     */
    @Override
    public void updateStatus(String status) {
	statusTextView.setText(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#isRouteDisplayed()
     */
    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#getLocationManager
     * ()
     */
    @Override
    public LocationManager getLocationManager() {
	return (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.ui.searchgeocache.ISearchActivity#getContext()
     */
    @Override
    public Context getContext() {
	return this.getBaseContext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#getLastKnownLocation
     * ()
     */
    @Override
    public Location getLastKnownLocation() {
	if (!manager.isLocationFixed()) {
	    return null;
	}
	return manager.getCurrentLocation();
    }
}