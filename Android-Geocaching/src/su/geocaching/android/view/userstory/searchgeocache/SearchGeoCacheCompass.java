package su.geocaching.android.view.userstory.searchgeocache;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
	private boolean locationFixed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_geocache_compass);
		compassView = (CompassView) findViewById(R.id.compassView);
		Intent intent = this.getIntent();
		geoCache = new GeoCache(intent.getIntExtra(MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
		locManager = new SearchGeoCacheLocationManager(this, (LocationManager) this.getSystemService(LOCATION_SERVICE));
		compass = new SearchGeoCacheCompassManager(this, (SensorManager) this.getSystemService(SENSOR_SERVICE));
		locationFixed = false;
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
		locationFixed = true;
		compassView.setAzimuthToGeoCache(getBearingToGeoCache(location));
		compassView.setDistanceToGeoCache(getDistanceToGeoCache(location));
	}
	
    protected float getDistanceToGeoCache(Location location) {
        float[] results = new float[3];
        double endLatitude = geoCache.getLocationGeoPoint().getLatitudeE6() / 1E6;
        double endLongitude = geoCache.getLocationGeoPoint().getLongitudeE6() / 1E6;
        Location.distanceBetween(location.getLatitude(),
                location.getLongitude(), endLatitude, endLongitude, results);
        return results[0];
    }
    
    protected float getBearingToGeoCache(Location location) {
        float[] results = new float[3];
        double endLatitude = geoCache.getLocationGeoPoint().getLatitudeE6() / 1E6;
        double endLongitude = geoCache.getLocationGeoPoint().getLongitudeE6() / 1E6;
        Location.distanceBetween(location.getLatitude(),
                location.getLongitude(), endLatitude, endLongitude, results);
    	DecimalFormat df = new DecimalFormat("0");
        //tv1.setText(df.format(180*results[1]/Math.PI));
        //tv2.setText(df.format(180*results[2]/Math.PI));
    	compassView.setTest(df.format(results[1])+"// "+df.format(results[2]));
        return results[1];
    }
}
