package su.geocaching.android.view.userstory.searchgeocache;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.view.MainMenu;
import su.geocaching.android.view.R;
import su.geocaching.android.view.geocachemap.GeoCacheItemizedOverlay;
import su.geocaching.android.view.geocachemap.GeoCacheMap;

/**
 * @author Android-Geocaching.su student project team
 * @description Search GeoCache with the map.
 * @since October 2010
 */
public class SearchGeoCacheMap extends GeoCacheMap {
    public final static String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";

    private GeoCache geoCache;
    private OverlayItem cacheOverlayItem;
    private GeoCacheItemizedOverlay cacheItemizedOverlay;
    private DistanceToGeoCacheOverlay distanceOverlay;
    private MyLocationOverlay userOverlay;
    private AlertDialog waitingLocationFixAlert;
    private boolean isLocationFixed;
    private Intent thisIntent;
    private boolean wasInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	thisIntent = this.getIntent();
	wasInitialized = false;
    }

    @Override
    protected void onPause() {
	super.onPause();

	if (wasInitialized) {
	    userOverlay.disableCompass();
	    userOverlay.disableMyLocation();
	    locationManager.pause();
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	if (!isGPSEnabled()) {
	    askTurnOnGPS();
	} else {
	    runLogic();
	}
    }

    /**
     * Ask user turn on GPS, if this disabled
     */
    private void askTurnOnGPS() {
	if (isGPSEnabled()) {
	    return;
	}
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setMessage(getString(R.string.ask_enable_gps_text)).setCancelable(false).setPositiveButton(getString(R.string.ask_enable_gps_yes), new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int id) {
		Intent startGPS = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		startActivity(startGPS);
		dialog.cancel();
	    }
	}).setNegativeButton(getString(R.string.ask_enable_gps_no), new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int id) {
		dialog.cancel();
		finish();
	    }
	});
	AlertDialog turnOnGPSAlert = builder.create();
	turnOnGPSAlert.show();
    }

    /**
     * @return true if GPS enabled
     */
    private boolean isGPSEnabled() {
	LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Init and run all activity content
     */
    private void runLogic() {
	// from onCreate
	// Controller controller = Controller.getInstance();

	// not working yet
	// geoCache = controller.getGeoCacheByID(intent.getIntExtra(
	// MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));

	geoCache = new GeoCache(thisIntent.getIntExtra(MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
	isLocationFixed = thisIntent.getBooleanExtra("location fixed", false);
	if (!isLocationFixed) {
	    showWaitingLocationFix();
	}

	userOverlay = new MyLocationOverlay(this, map);
	userOverlay.enableCompass();
	userOverlay.enableMyLocation();

	// from onResume
	locationManager.resume();
	Drawable cacheMarker = this.getResources().getDrawable(R.drawable.orangecache);
	cacheMarker.setBounds(0, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth(), 0);

	cacheItemizedOverlay = new GeoCacheItemizedOverlay(cacheMarker);
	cacheOverlayItem = new OverlayItem(geoCache.getLocationGeoPoint(), "", "");
	cacheItemizedOverlay.addOverlay(cacheOverlayItem);
	mapOverlays.add(cacheItemizedOverlay);

	map.invalidate();

	wasInitialized = true;
    }

    /**
     * Show cancelable alert which tell user what location fixing
     */
    private void showWaitingLocationFix() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setMessage(getString(R.string.waiting_location_fix_message)).setCancelable(false)
		.setNegativeButton(getString(R.string.waiting_location_fix_cancel), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
			finish();
		    }
		});
	waitingLocationFixAlert = builder.create();
	waitingLocationFixAlert.show();
    }

    /**
     * Start SearchGeoCacheCompass activity
     */
    private void startCompassView() {
	Intent intent = new Intent(this, SearchGeoCacheCompass.class);
	intent.putExtra(DEFAULT_GEOCACHE_ID_NAME, geoCache.getId());
	intent.putExtra("location fixed", locationManager.isLocationFixed());
	startActivity(intent);
	this.finish();
    }

    @Override
    public void updateLocation(Location location) {
	if (locationManager.isLocationFixed()) {
	    if (!isLocationFixed) {
		waitingLocationFixAlert.dismiss();
	    }
	    isLocationFixed = true;
	} else {
	    if (isLocationFixed) {
		locationManager.setLocationFixed();
		userOverlay.onLocationChanged(location);
	    } else {
		return;
	    }
	}
	Location loc = locationManager.getCurrentLocation();
	GeoPoint currentGeoPoint = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));

	if (distanceOverlay == null) {
	    // It's really first run of update location
	    resetZoom();
	    distanceOverlay = new DistanceToGeoCacheOverlay(currentGeoPoint, geoCache.getLocationGeoPoint());
	    mapOverlays.add(distanceOverlay);
	    mapOverlays.add(userOverlay);
	    return;
	}
	distanceOverlay.setCachePoint(geoCache.getLocationGeoPoint());
	distanceOverlay.setUserPoint(currentGeoPoint);
	map.invalidate();
    }

    @Override
    public Location getLastLocation() {
	return locationManager.getCurrentLocation();
    }

    /**
     * Set map zoom which can show userPoint and GeoCachePoint
     */
    private void resetZoom() {
	Location loc = locationManager.getCurrentLocation();
	GeoPoint currentGeoPoint = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
	mapController.zoomToSpan(Math.abs(geoCache.getLocationGeoPoint().getLatitudeE6() - currentGeoPoint.getLatitudeE6()),
		Math.abs(geoCache.getLocationGeoPoint().getLongitudeE6() - currentGeoPoint.getLongitudeE6()));

	GeoPoint center = new GeoPoint((geoCache.getLocationGeoPoint().getLatitudeE6() + currentGeoPoint.getLatitudeE6()) / 2,
		(geoCache.getLocationGeoPoint().getLongitudeE6() + currentGeoPoint.getLongitudeE6()) / 2);
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
	    if (locationManager.isLocationFixed()) {
		resetZoom();
	    }
	    return true;
	case R.id.menuStartCompass:
	    this.startCompassView();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }
}