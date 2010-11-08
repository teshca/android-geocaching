package su.geocaching.android.view.userstory.searchgeocache;

import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.view.MainMenu;
import su.geocaching.android.view.R;
import su.geocaching.android.view.geocachemap.*;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * @description Search GeoCache with the compass.
 */
public class SearchGeoCacheCompass extends Activity implements IActivityWithLocation, IActivityWithCompass {

	private CompassView compassView;
	private GeoCache geoCache;
	private SearchGeoCacheLocationManager locManager;
	private SearchGeoCacheCompassManager compass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_geocache_compass);
		compassView = (CompassView) findViewById(R.id.compassView);
		Intent intent = this.getIntent();
		geoCache = new GeoCache(intent.getIntExtra(MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
		locManager = new SearchGeoCacheLocationManager(this, (LocationManager) this.getSystemService(LOCATION_SERVICE));
		compass = new SearchGeoCacheCompassManager(this, (SensorManager) this.getSystemService(SENSOR_SERVICE));
	}

	@Override
	protected void onResume() {
		super.onResume();
		locManager.resume();
		compass.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		locManager.pause();
		compass.pause();
	}

	@Override
	public void updateAzimuth(float azimuth) {
		compassView.setAzimuthToNorth(azimuth);
	}

	@Override
	public void updateLocation(Location location) {
		compassView.setAzimuthToGeoCache(getBearingToGeoCache(location));
		compassView.setDistanceToGeoCache(getDistanceToGeoCache(location));
	}
	
	
    /**
     * @param location 
     * @return distance in meters of shortest way from location to geoPoint location
     */
    protected float getDistanceToGeoCache(Location location) {
        float[] results = new float[3];
        double endLatitude = geoCache.getLocationGeoPoint().getLatitudeE6() / 1E6;
        double endLongitude = geoCache.getLocationGeoPoint().getLongitudeE6() / 1E6;
        Location.distanceBetween(location.getLatitude(),
                location.getLongitude(), endLatitude, endLongitude, results);
        return results[0];
    }
    
    
    /**
     * @param location
     * @return bearing in degrees of shortest way from location to geoPoint location
     */
    protected float getBearingToGeoCache(Location location) {
        float[] results = new float[3];
        double endLatitude = geoCache.getLocationGeoPoint().getLatitudeE6() / 1E6;
        double endLongitude = geoCache.getLocationGeoPoint().getLongitudeE6() / 1E6;
        Location.distanceBetween(location.getLatitude(),
                location.getLongitude(), endLatitude, endLongitude, results);
        return results[1];
    }
    
    /**
	 * Creating menu object
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_geocache_compass, menu);
		return true;
	}

	/**
	 * Called when menu element selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuStartMap:
			this.startMapView();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/** 
	 *  Starts SearchGeoCacheMap activity and finish this
	 */
	private void startMapView() {
        Intent intent = new Intent(this, SearchGeoCacheMap.class);
        intent.putExtra(MainMenu.DEFAULT_GEOCACHE_ID_NAME, geoCache.getId());
		intent.putExtra("layout", R.layout.search_geocache_map);
		intent.putExtra("mapID", R.id.searchGeocacheMap);
        startActivity(intent);
        this.finish();
	}
}
