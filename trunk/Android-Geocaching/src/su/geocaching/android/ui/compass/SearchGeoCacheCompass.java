package su.geocaching.android.ui.compass;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GeoCacheLocationManager;
import su.geocaching.android.controller.GpsStatusManager;
import su.geocaching.android.controller.IGpsStatusAware;
import su.geocaching.android.controller.ILocationAware;
import su.geocaching.android.controller.compass.CompassPreferenceManager;
import su.geocaching.android.controller.compass.CompassSpeed;
import su.geocaching.android.controller.compass.SmoothCompassThread;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.GpsHelper;
import su.geocaching.android.utils.UiHelper;
import su.geocaching.android.utils.log.LogHelper;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Search GeoCache with the compass.
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class SearchGeoCacheCompass extends Activity {
	private static final String TAG = SearchGeoCacheCompass.class.getCanonicalName();

	private SmoothCompassThread animationThread;
	private GeoCacheLocationManager locationManager;
	private LocationListener locationListener;
	private GpsStatusManager gpsManager;
	private GpsStatusListener gpsListener;

	private CompassView compassView;
	private TextView distanceToCache;
	private TextView statusText;
	private ImageView progressBarView;
	private AnimationDrawable progressBarAnim;

	private GeoCache geoCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogHelper.d(TAG, "on create");
		setContentView(R.layout.search_geocache_compass);

		compassView = (CompassView) findViewById(R.id.compassView);
		distanceToCache = (TextView) findViewById(R.id.DistanceValue);
		progressBarView = (ImageView) findViewById(R.id.progressCircle);
		progressBarView.setBackgroundResource(R.anim.earth_anim);
		progressBarAnim = (AnimationDrawable) progressBarView.getBackground();
		statusText = (TextView) findViewById(R.id.waitingLocationFixText);

		locationManager = Controller.getInstance().getLocationManager(this);
		gpsManager = Controller.getInstance().getGpsStatusManager(this);
		locationListener = new LocationListener(this);
		gpsListener = new GpsStatusListener();
		geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogHelper.d(TAG, "onResume");
		runLogic();
		startAnimation();
	}

	/**
	 * Run activity logic
	 */
	private void runLogic() {
		if (geoCache == null) {
			Log.e(TAG, "runLogic: null geocache. Finishing.");
			Toast.makeText(this, this.getString(R.string.search_geocache_error_no_geocache), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// Save last searched geocache
		// Controller.getInstance().setLastSearchedGeoCache(geoCache, this);

		if (locationManager.hasLocation()) {
			Log.d(TAG, "runLogic: location fixed. Update location with last known location");
			locationListener.updateLocation(locationManager.getLastKnownLocation());
			progressBarView.setVisibility(View.GONE);
		} else {
			LogHelper.d(TAG, "run logic: location not fixed. Show gps status");
			onBestProviderUnavailable();
			progressBarView.setVisibility(View.VISIBLE);
		}

		locationManager.addSubscriber(locationListener);
		locationManager.enableBestProviderUpdates();
		gpsManager.addSubscriber(gpsListener);
	}

	@Override
	protected void onPause() {
		LogHelper.d(TAG, "onPause");
		locationManager.removeSubsriber(locationListener);
		gpsManager.removeSubsriber(gpsListener);
		stopAnimation();
		super.onPause();
	}

	private void startAnimation() {
		if (animationThread == null) {
			animationThread = new SmoothCompassThread(compassView, this);
			animationThread.setRunning(true);

			CompassPreferenceManager preferManager = CompassPreferenceManager.getPreference(this);
			String speed = preferManager.getString(CompassPreferenceManager.PREFS_COMPASS_SPEED_KEY, CompassSpeed.NORMAL.name());
			animationThread.setSpeed(CompassSpeed.valueOf(speed));

			animationThread.start();
		}
	}

	private void stopAnimation() {
		if (animationThread != null) {
			animationThread.setRunning(false);
			try {
				animationThread.join(150); // TODO Is it need?
			} catch (InterruptedException e) {
			}
			animationThread = null;
		}
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
			UiHelper.startMapView(this, geoCache);
			return true;
		case R.id.menuGeoCacheInfo:
			UiHelper.showGeoCacheInfo(this, geoCache);
			return true;
		case R.id.menuKeepScreen:
			keepScreenOn(item);
			return true;
		case R.id.compassSettings:
			showCompassPreferences();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void keepScreenOn(MenuItem item) {
		if (compassView.getKeepScreenOn()) {
			compassView.setKeepScreenOn(false);
			item.setIcon(R.drawable.ic_menu_screen_off);
		} else {
			compassView.setKeepScreenOn(true);
			item.setIcon(R.drawable.ic_menu_screen_on);
		}
	}

	private void showCompassPreferences() {
		stopAnimation();
		Intent intent = new Intent(this, CompassPreferenceActivity.class);
		startActivityForResult(intent, 1);
	}

	private void onBestProviderUnavailable() {
		if (progressBarView.getVisibility() == View.GONE) {
			progressBarView.setVisibility(View.VISIBLE);
		}
		gpsListener.updateStatus(getString(R.string.waiting_location_fix_message));
		Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG).show();
	}

	// TODO check it
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		progressBarAnim.start();
	}

	public void onHomeClick(View v) {
		UiHelper.goHome(this);
	}

	/**
	 * 
	 */
	class LocationListener implements ILocationAware {
		Activity activity;

		LocationListener(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void updateLocation(Location location) {
			// TODO is it need?
			// if (!locationManager.hasLocation()) {
			// return;
			// }
			if (progressBarView.getVisibility() == View.VISIBLE) {
				progressBarView.setVisibility(View.GONE);
			}
			compassView.setCacheDirection(GpsHelper.getBearingBetween(location, geoCache.getLocationGeoPoint()));
			setDistance(GpsHelper.getDistanceBetween(location, geoCache.getLocationGeoPoint()));
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(TAG, "onStatusChanged:");
			switch (status) {
			case LocationProvider.OUT_OF_SERVICE:
				onBestProviderUnavailable();
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				onBestProviderUnavailable();
				break;
			case LocationProvider.AVAILABLE:
				break;
			}
			if (provider.equals(LocationManager.GPS_PROVIDER)) {

			}
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "onProviderDisabled provider: " + provider);
			if (!locationManager.isBestProviderEnabled()) {
				Log.d(TAG, "onStatusChanged: best provider (" + locationManager.getBestProvider() + ") disabled. Ask turn on.");
				onBestProviderUnavailable();
				UiHelper.askTurnOnGps(activity);
			}
		}

		private void setDistance(float distance) {
			if (!locationManager.hasLocation()) {
				distanceToCache.setText(R.string.distance_unknown);
				Toast.makeText(activity, R.string.search_geocache_best_provider_lost, Toast.LENGTH_LONG).show();
			} else {
				distanceToCache.setText(GpsHelper.distanceToString(distance));
			}
		}
	}

	/**
	 * 
	 */
	class GpsStatusListener implements IGpsStatusAware {

		@Override
		public void updateStatus(String status) {
			compassView.setLocationFix(locationManager.hasLocation());

			statusText.setText(status);
		}

	}
}
