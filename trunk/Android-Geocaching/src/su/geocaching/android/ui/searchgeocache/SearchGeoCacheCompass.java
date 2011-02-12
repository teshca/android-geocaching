package su.geocaching.android.ui.searchgeocache;

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
import android.os.Bundle;
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
public class SearchGeoCacheCompass extends Activity implements ISearchActivity {
	private static final String TAG = SearchGeoCacheCompass.class.getCanonicalName();

	private SmoothCompassThread animThread;
	private SearchGeoCacheManager searchManager;

	private CompassView compassView;
	private TextView distanceToCache;
	private TextView statusText;
	private ImageView progressBarView;
	private AnimationDrawable progressBarAnim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogHelper.d(TAG, "on create");
		setContentView(R.layout.search_geocache_compass);

		compassView = (CompassView) findViewById(R.id.compassView);
		searchManager = new SearchGeoCacheManager(this);
		distanceToCache = (TextView) findViewById(R.id.DistanceValue);
		progressBarView = (ImageView) findViewById(R.id.progressCircle);
		progressBarView.setBackgroundResource(R.anim.earth_anim);
		progressBarAnim = (AnimationDrawable) progressBarView.getBackground();
		statusText = (TextView) findViewById(R.id.waitingLocationFixText);
	}

	private void setDistance(float distance) {
		if (!searchManager.isLocationFixed()) {
			distanceToCache.setText(R.string.distance_unknown);
			Toast.makeText(this, R.string.search_geocache_best_provider_lost, Toast.LENGTH_LONG).show();
		} else {
			distanceToCache.setText(GpsHelper.distanceToString(distance));
		}
	}

	@Override
	protected void onResume() {
		LogHelper.d(TAG, "onResume");
		super.onResume();
		searchManager.onResume();
		startAnim();
	}

	@Override
	protected void onPause() {
		LogHelper.d(TAG, "onPause");
		searchManager.onPause();
		stopAnim();
		super.onPause();
	}

	private void startAnim() {
		if (animThread == null) {
			animThread = new SmoothCompassThread(compassView, this);
			animThread.setRunning(true);
			animThread.start();
		}
	}

	private void stopAnim() {
		if (animThread != null) {
			animThread.setRunning(false);
			try {
				animThread.join(150); // TODO Is it need?
			} catch (InterruptedException e) {
			}
			animThread = null;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	/**
	 * Run all activity logic
	 */
	@Override
	public void runLogic() {
		searchManager.runLogic();
		if (searchManager.getGeoCache() == null) {
			return;
		}
		if (!searchManager.isLocationFixed()) {
			LogHelper.d(TAG, "run logic: location not fixed. Show gps status");
			progressBarView.setVisibility(View.VISIBLE);
		} else {
			progressBarView.setVisibility(View.GONE);
		}
	}

	@Override
	public void updateBearing(float bearing) {
		// compassView.setBearingToNorth(bearing);
	}

	@Override
	public void updateLocation(Location location) {
		if (!searchManager.isLocationFixed()) {
			return;
		}
		if (progressBarView.getVisibility() == View.VISIBLE) {
			progressBarView.setVisibility(View.GONE);
		}
		compassView.setCacheDirection(GpsHelper.getBearingBetween(location, searchManager.getGeoCache().getLocationGeoPoint()));
		setDistance(GpsHelper.getDistanceBetween(location, searchManager.getGeoCache().getLocationGeoPoint()));
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
			startMapView();
			return true;
		case R.id.menuGeoCacheInfo:
			searchManager.showGeoCacheInfo();
			return true;
		case R.id.menuKeepScreen:
			if (compassView.getKeepScreenOn()) {
				compassView.setKeepScreenOn(false);
				item.setIcon(R.drawable.ic_menu_screen_off);
			} else {
				compassView.setKeepScreenOn(true);
				item.setIcon(R.drawable.ic_menu_screen_on);
			}
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
		intent.putExtra(GeoCache.class.getCanonicalName(), searchManager.getGeoCache());
		startActivity(intent);
	}

	@Override
	public void updateStatus(String status, StatusType type) {
		compassView.setLocationFix(searchManager.isLocationFixed());
		// TODO add status field
		if (type == StatusType.GPS) {
			statusText.setText(status);
		}
	}

	@Override
	public Location getLastKnownLocation() {
		return searchManager.getCurrentLocation();
	}

	@Override
	public void onBestProviderUnavailable() {
		if (progressBarView.getVisibility() == View.GONE) {
			progressBarView.setVisibility(View.VISIBLE);
		}
		updateStatus(getString(R.string.waiting_location_fix_message), StatusType.GPS);
		Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG).show();
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		progressBarAnim.start();
	}

	public void onHomeClick(View v) {
		UiHelper.goHome(this);
	}
}
