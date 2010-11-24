package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.application.ApplicationMain;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.view.showgeocacheinfo.Info_cache;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 18, 2010
 *      <p>
 *      This manager handle many common situation of search geocache activities
 *      </p>
 */
public class SearchGeoCacheManager implements ILocationAware, ICompassAware {
    private GeoCacheCompassManager compass;
    private GeoCacheLocationManager locationManager;
    private ISearchActivity activity;
    private GeoCache geoCache;
    private GpsStatusListener gpsStatusListener;

    /**
     * @param context
     *            activity which used this manager
     */
    public SearchGeoCacheManager(ISearchActivity context) {
	this.activity = context;
	locationManager = ((ApplicationMain) ((Activity) context).getApplication()).getLocationManager();
	compass = ((ApplicationMain) ((Activity) context).getApplication()).getCompassManager();
	gpsStatusListener = new GpsStatusListener(activity);
    }

    /**
     * Called when activity pausing
     */
    public void onPause() {
	if (locationManager.isLocationFixed()) {
	    locationManager.removeSubsriber(this);
	}
	compass.removeSubsriber(this);
	gpsStatusListener.pause();
    }
    
    /**
     * Call this then activity destroying
     */
    public void onDestroy() {
	locationManager.removeSubsriber(this);
    }

    /**
     * Called when activity resuming
     */
    public void onResume() {
	if (!locationManager.isBestProviderEnabled()) {
	    if (!locationManager.isBestProviderGps()) {
		Toast.makeText(activity.getContext(), activity.getContext().getString(R.string.device_without_gps_alert), Toast.LENGTH_LONG).show();
	    }
	    askTurnOnGps();
	} else {
	    activity.runLogic();
	}
    }

    /**
     * Ask user turn on GPS, if this disabled
     */
    private void askTurnOnGps() {
	if (locationManager.isBestProviderEnabled()) {
	    return;
	}
	AlertDialog.Builder builder = new AlertDialog.Builder((Activity) activity);
	builder.setMessage(activity.getContext().getString(R.string.ask_enable_gps_text)).setCancelable(false)
		.setPositiveButton(activity.getContext().getString(R.string.ask_enable_gps_yes), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			Intent startGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			((Activity) activity).startActivity(startGPS);
			dialog.cancel();
		    }
		}).setNegativeButton(activity.getContext().getString(R.string.ask_enable_gps_no), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
			// activity is MapActivity or Activity
			((Activity) activity).finish();
		    }
		});
	AlertDialog turnOnGpsAlert = builder.create();
	turnOnGpsAlert.show();
    }

    /**
     * Show cancelable alert which tell user what location fixing
     */
    private void showWaitingLocationFix() {
	activity.updateStatus(activity.getContext().getString(R.string.waiting_location_fix_message), ISearchActivity.STATUS_TYPE_GPS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.geocachemap.ILocationAware#updateLocation(android
     * .location.Location)
     */
    @Override
    public void updateLocation(Location location) {
	activity.updateLocation(location);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.geocachemap.ILocationAware#onStatusChanged(java
     * .lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO: send code of event to activity
	String statusString = "";
	if (!isLocationFixed()) {
	    statusString = "Fixing location:\n\t";
	}
	statusString += "Provider: " + provider + "\n\t";
	switch (status) {
	case LocationProvider.OUT_OF_SERVICE:
	    statusString += "Status: out of service";
	case LocationProvider.TEMPORARILY_UNAVAILABLE:
	    statusString += "Status: temporarily unavailable";
	case LocationProvider.AVAILABLE:
	    statusString += "Status: available";
	}
	if (provider.equals(LocationManager.GPS_PROVIDER)) {
	    statusString += "\n\t" + "Satellites: " + extras.getString("satellites");
	}
	activity.updateStatus(statusString, ISearchActivity.STATUS_TYPE_GPS);
    }

    @Override
    public void onProviderEnabled(String provider) {
	// its really need? can we call this?
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.geocachemap.ILocationAware#onProviderDisabled
     * (java.lang.String)
     */
    @Override
    public void onProviderDisabled(String provider) {
	if (!locationManager.isBestProviderEnabled()) {
	    askTurnOnGps();
	}
    }

    /**
     * @return true if user location has been fixed
     */
    public boolean isLocationFixed() {
	return locationManager.isLocationFixed();
    }

    /**
     * Common part of init and run search geocache activities
     */
    public void runLogic() {
	Intent intent = ((Activity) activity).getIntent();
	geoCache = intent.getParcelableExtra(GeoCache.class.getCanonicalName());
	if (geoCache == null) {
	    Toast.makeText(activity.getContext(), activity.getContext().getString(R.string.search_geocache_error_no_geocache), Toast.LENGTH_LONG).show();
	    ((Activity) activity).finish();
	    return;
	}

	if (!isLocationFixed()) {
	    showWaitingLocationFix();
	}else{
	    updateLocation(getCurrentLocation());
	}
	locationManager.addSubscriber(this);
	compass.addSubscriber(this);
	gpsStatusListener.resume();
    }

    /**
     * @return geocache which we search
     */
    public GeoCache getGeoCache() {
	return geoCache;
    }

    /**
     * @return current user location
     */
    public Location getCurrentLocation() {
	return locationManager.getLastKnownLocation();
    }

    /**
     * @return current bearing known to compass
     */
    public int getCurrentBearing() {
	return compass.getLastBearing();
    }

    /**
     * Open GeoCache info activity
     */
    public void showGeoCacheInfo() {
	Intent intent = new Intent(activity.getContext(), Info_cache.class);
	intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
	((Activity) activity).startActivity(intent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.geocachemap.ICompassAware#updateAzimuth(float)
     */
    @Override
    public void updateBearing(int azimuth) {
	activity.updateAzimuth(azimuth);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.geocachemap.ICompassAware#isCompassAvailable()
     */
    @Override
    public boolean isCompassAvailable() {
	return compass.isCompassAvailable();
    }
}
