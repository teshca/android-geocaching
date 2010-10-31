package su.geocaching.android.view;

import java.util.List;

import su.geocaching.android.model.GeoCache;
import su.geocaching.android.searchGeoCache.GeoCacheItemizedOverlay;
import su.geocaching.android.searchGeoCache.SearchGeoCacheCompasManager;
import su.geocaching.android.searchGeoCache.SearchGeoCacheLocationManager;
import su.geocaching.android.searchGeoCache.UserLocationOverlay;
import su.geocaching.android.view.R;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	Search GeoCache with the map.
 */
public class SearchGeoCacheMap extends MapActivity {
    
    public final static String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";

    private MapView mvMap;
    private MapController mcMapController;
    private GeoCache geoCache;
    private GeoCacheItemizedOverlay cacheItemizedOverlay;
    private OverlayItem cacheOverlayItem;
    private UserLocationOverlay userOverlay;
    private SearchGeoCacheCompasManager compas;
    private SearchGeoCacheLocationManager locationManager;
    private List<Overlay> mapOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.search_geocache_map);
	Intent intent = this.getIntent();
	geoCache = new GeoCache(intent.getIntExtra(
		MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
	mvMap = (MapView) findViewById(R.id.searchGeocacheMap);
	mcMapController = mvMap.getController();
	mvMap.setBuiltInZoomControls(true);
	mapOverlays = mvMap.getOverlays();
    }

    @Override
    protected void onResume() {
	super.onResume();
	Drawable cacheMarker = this.getResources().getDrawable(R.drawable.orangecache);
	cacheMarker.setBounds(0,
		-cacheMarker.getMinimumHeight(),
		cacheMarker.getMinimumWidth(), 0);
	cacheItemizedOverlay = new GeoCacheItemizedOverlay(cacheMarker);
	cacheOverlayItem = new OverlayItem(geoCache.getLocation(), "", "");
	cacheItemizedOverlay.addOverlay(cacheOverlayItem);
	mapOverlays.add(cacheItemizedOverlay);
	mvMap.invalidate();
	mcMapController.animateTo(geoCache.getLocation());
	compas = new SearchGeoCacheCompasManager(this);
	locationManager = new SearchGeoCacheLocationManager(this);
    }

    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

    public void updateUserOverlay(Location location, float angle) {
	//TODO: if (distanceToGeoCache<Accuracy) then startCompasView
	GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6),
		(int) (location.getLongitude() * 1E6));
	if (userOverlay==null) {
	    userOverlay = new UserLocationOverlay(this, point, angle, location.getAccuracy());
	}else{
	    mapOverlays.remove(userOverlay);
	    userOverlay.setPoint(point);
	    userOverlay.setAngle(angle);
	    userOverlay.setRadius(location.getAccuracy());
	}
	mapOverlays.add(userOverlay);
	mvMap.invalidate();
    }
    
    public float getLastAzimuth() {
	return compas.getLastAzimuth();
    }
    
    public Location getLastLocation() {
	return locationManager.getCurrentLocation();
    }
    
    private void startCompasView() {
	    Intent intent = new Intent(this, SearchGeoCacheCompas.class);
	    intent.putExtra(DEFAULT_GEOCACHE_ID_NAME, geoCache.getId());
	    startActivity(intent);
	    this.finish();
    }
    
    protected float getDistanceToGeoCache(Location location) {
	//TODO: implement using Location.distanceBetween
	return 0;
    }
}
