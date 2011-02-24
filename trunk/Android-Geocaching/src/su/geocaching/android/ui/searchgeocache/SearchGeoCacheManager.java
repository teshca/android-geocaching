package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.controller.CompassManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GeoCacheLocationManager;
import su.geocaching.android.controller.GpsStatusManager;
import su.geocaching.android.controller.ICompassAware;
import su.geocaching.android.controller.IGpsStatusAware;
import su.geocaching.android.controller.ILocationAware;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.UiHelper;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * This manager handle many common situation of search geocache activities
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 18, 2010
 */
public class SearchGeoCacheManager implements ILocationAware, ICompassAware, IGpsStatusAware {
	private static final String TAG = SearchGeoCacheManager.class.getCanonicalName();

	private CompassManager compass;
	private GeoCacheLocationManager locationManager;
	private ISearchActivity activity;
	private Activity context; // it's activity casted to Activity. Cast is
	// bad...
	//private GeoCache geoCache;
	private GpsStatusManager gpsStatusManager;

	/**
	 * @param activity
	 *            activity which used this manager
	 */
	public SearchGeoCacheManager(ISearchActivity activity) {
		this.activity = activity;
		this.context = (Activity) activity;
		locationManager = Controller.getInstance().getLocationManager(context);
		compass = Controller.getInstance().getCompassManager(context);
		gpsStatusManager = Controller.getInstance().getGpsStatusManager(context);
//		Intent intent = ((Activity) activity).getIntent();
//		geoCache = intent.getParcelableExtra(GeoCache.class.getCanonicalName());
		Log.d(TAG, "Init");
	}

	/**
	 * Called when activity pausing
	 */
	public void onPause() {
		locationManager.removeSubsriber(this);
		compass.removeSubscriber(this);
		gpsStatusManager.removeSubsriber(this);
		Log.d(TAG, "pause: remove updates of location, compass and GPS status");
	}

	/**
	 * Called when activity resuming
	 */
	public void onResume() {
		if (!locationManager.isBestProviderEnabled()) {
			if (!locationManager.isBestProviderGps()) {
				Log.w(TAG, "resume: device without gps");
				// Toast.makeText(activity.getContext(),
				// activity.getContext().getString(R.string.device_without_gps_alert),
				// Toast.LENGTH_LONG).show();
			}
			UiHelper.askTurnOnGps(context);
			Log.d(TAG, "resume: best provider (" + locationManager.getBestProvider() + ") disabled. Current provider is " + locationManager.getCurrentProvider());
		} else {
			activity.runLogic();
			Log.d(TAG, "resume: best provider (" + locationManager.getBestProvider() + ") enabled. Run logic of manager");
		}
	}

	// /**
	// * Ask user turn on GPS, if this disabled
	// */
	// private void askTurnOnGps() {
	// if (locationManager.isBestProviderEnabled()) {
	// Log.w(TAG, "ask turn on best provider (" + locationManager.getBestProvider() + "): already done");
	// return;
	// }
	// AlertDialog.Builder builder = new AlertDialog.Builder(context);
	// builder.setMessage(context.getString(R.string.ask_enable_gps_text)).setCancelable(false)
	// .setPositiveButton(context.getString(R.string.ask_enable_gps_yes), new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// Intent startGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	// context.startActivity(startGPS);
	// dialog.cancel();
	// }
	// }).setNegativeButton(context.getString(R.string.ask_enable_gps_no), new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// dialog.cancel();
	// // activity is MapActivity or Activity
	// context.finish();
	// }
	// });
	// AlertDialog turnOnGpsAlert = builder.create();
	// turnOnGpsAlert.show();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.geocachemap.ILocationAware#updateLocation(android .location.Location)
	 */
	@Override
	public void updateLocation(Location location) {
		Log.d(TAG, "update location: send it to activity");
		activity.updateLocation(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.geocachemap.ILocationAware#onStatusChanged(java .lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO: send code of event to activity
		Log.d(TAG, "onStatusChanged:");
		String statusString = "Location fixed: " + Boolean.toString(isLocationFixed()) + ". Provider: " + provider + ". ";
		Log.d(TAG, "     " + statusString);
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			statusString += "Status: out of service. ";
			activity.onBestProviderUnavailable();
			Log.d(TAG, "     Status: out of service.");
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			statusString += "Status: temporarily unavailable. ";
			activity.onBestProviderUnavailable();
			Log.d(TAG, "     Status: temporarily unavailable.");
			break;
		case LocationProvider.AVAILABLE:
			statusString += "Status: available. ";
			Log.d(TAG, "     Status: available.");
			break;
		}
		if (provider.equals(LocationManager.GPS_PROVIDER)) {
			statusString += "Satellites: " + Integer.toString(extras.getInt("satellites"));
			Log.d(TAG, "     Satellites: " + Integer.toString(extras.getInt("satellites")));
		}
		// activity.updateStatus(statusString, ISearchActivity.STATUS_TYPE_GPS);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "onProviderEnabled: do nothing");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.geocachemap.ILocationAware#onProviderDisabled (java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "onProviderDisabled");
		if (!locationManager.isBestProviderEnabled()) {
			Log.d(TAG, "onStatusChanged: best provider (" + locationManager.getBestProvider() + ") disabled. Ask turn on.");
			activity.onBestProviderUnavailable();
			UiHelper.askTurnOnGps(context);
		}
	}

	/**
	 * @return true if user location has been fixed
	 */
	public boolean isLocationFixed() {
		return locationManager.hasLocation();
	}

	/**
	 * Common part of init and run search geocache activities
	 */
	public void runLogic() {
		if (Controller.getInstance().getSearchingGeoCache() == null) {
			Log.e(TAG, "runLogic: null geocache. Finishing.");
			Toast.makeText(context, context.getString(R.string.search_geocache_error_no_geocache), Toast.LENGTH_LONG).show();
			((Activity) activity).finish();
			return;
		}

		// Save last searched geocache
		//TODO unnecessary parameter Controller.getInstance().getSearchedGeoCache()
		Controller.getInstance().setLastSearchedGeoCache(Controller.getInstance().getSearchingGeoCache(), context);

		if (!isLocationFixed()) {
			activity.onBestProviderUnavailable();
			Log.d(TAG, "runLogic: location not fixed. Send msg.");
		} else {
			updateLocation(getCurrentLocation());
			Log.d(TAG, "runLogic: location fixed. Update location with last known location");
		}
		locationManager.addSubscriber(this);
		locationManager.enableBestProviderUpdates();
		compass.addSubscriber(this);
		gpsStatusManager.addSubscriber(this);
	}

//	/**
//	 * @return geocache which we search
//	 */
//	public GeoCache getGeoCache() {
//		return geoCache;
//	}

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
		return compass.getLastDirection();
	}

	/**
	 * Open GeoCache info activity
	 */
	// public void showGeoCacheInfo() {
	// Log.d(TAG, "Go to show geo cache activity");
	// Intent intent = new Intent(context, ShowGeoCacheInfo.class);
	// intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
	// context.startActivity(intent);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.searchgeocache.ICompassAware#updateBearing(int)
	 */
	@Override
	public void updateBearing(float bearing) {
		// Log.d(TAG, "updateBearing: new bearing " +
		// Integer.toString(bearing));
		activity.updateBearing(bearing);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.searchgeocache.IGpsStatusAware#updateStatus( java.lang.String)
	 */
	@Override
	public void updateStatus(String status) {
		activity.updateStatus(status, StatusType.GPS);
	}

}
