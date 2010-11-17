package su.geocaching.android.view.searchgeocache;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.MenuActivity;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.Helper;
import su.geocaching.android.view.geocachemap.GeoCacheCompassManager;
import su.geocaching.android.view.geocachemap.GeoCacheLocationManager;
import su.geocaching.android.view.geocachemap.ICompassAware;
import su.geocaching.android.view.geocachemap.ILocationAware;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 * @description Search GeoCache with the compass.
 */
public class SearchGeoCacheCompass extends Activity implements ILocationAware, ICompassAware {

    private GraphicCompassView compassView;
    private GeoCache geoCache;
    private GeoCacheLocationManager locManager;
    private GeoCacheCompassManager compass;
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
    protected void onResume() {
	super.onResume();
	if (!isGPSEnabled()) {
	    askTurnOnGPS();
	} else {
	    runLogic();
	}
    }

    @Override
    protected void onPause() {
	super.onPause();

	if (wasInitialized) {
	    locManager.pause();
	    compass.pause();
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
     * Run all activity logic
     */
    private void runLogic() {
	// from onCreate
	setContentView(R.layout.search_geocache_compass);
	compassView = (GraphicCompassView) findViewById(R.id.compassView);
	thisIntent = this.getIntent();
	geoCache = new GeoCache(thisIntent.getIntExtra(MenuActivity.DEFAULT_GEOCACHE_ID_NAME, -1));
	locManager = new GeoCacheLocationManager(this, (LocationManager) this.getSystemService(LOCATION_SERVICE));
	compass = new GeoCacheCompassManager(this, (SensorManager) this.getSystemService(SENSOR_SERVICE));
	isLocationFixed = thisIntent.getBooleanExtra("location fixed", false);
	if (!isLocationFixed) {
	    showWaitingLocationFix();
	}

	// from onResume
	locManager.resume();
	compass.resume();
    }

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

    @Override
    public void updateAzimuth(float azimuth) {
	compassView.setAzimuthToNorth(azimuth);
    }

    @Override
    public void updateLocation(Location location) {
	if (locManager.isLocationFixed()) {
	    if (!isLocationFixed) {
		waitingLocationFixAlert.dismiss();
	    }
	    isLocationFixed = true;
	} else {
	    if (isLocationFixed) {
		locManager.setLocationFixed();
	    } else {
		return;
	    }
	}

	if (locManager.isLocationFixed()) {
	}
	compassView.setAzimuthToGeoCache(Helper.getBearingBetween(location, geoCache.getLocationGeoPoint()));
	compassView.setDistanceToGeoCache(Helper.getDistanceBetween(location, geoCache.getLocationGeoPoint()));
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
     * Starts SearchGeoCacheMap activity and finish this
     */
    private void startMapView() {
	Intent intent = new Intent(this, SearchGeoCacheMap.class);
	intent.putExtra(MenuActivity.DEFAULT_GEOCACHE_ID_NAME, geoCache.getId());
	intent.putExtra("layout", R.layout.search_geocache_map);
	intent.putExtra("mapID", R.id.searchGeocacheMap);
	intent.putExtra("location fixed", locManager.isLocationFixed());
	startActivity(intent);
	this.finish();
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
