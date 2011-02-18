package su.geocaching.android.ui.searchgeocache;

import java.util.List;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.ShowGeoCacheInfo;
import su.geocaching.android.ui.compass.SearchGeoCacheCompass;
import su.geocaching.android.ui.geocachemap.*;
import su.geocaching.android.ui.searchgeocache.drivingDirections.IRoute;
import su.geocaching.android.ui.searchgeocache.drivingDirections.DrivingDirections.IDirectionsListener;
import su.geocaching.android.ui.searchgeocache.drivingDirections.DrivingDirections.Mode;
import su.geocaching.android.utils.GpsHelper;
import su.geocaching.android.utils.UiHelper;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.location.Location;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
/**
 * Search GeoCache with the map
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class SearchGeoCacheMap extends MapActivity implements ISearchActivity, IMapAware, IInternetAware, IDirectionsListener {
	private final static String TAG = SearchGeoCacheMap.class.getCanonicalName();

	private GeoCacheOverlayItem cacheOverlayItem;
	private GeoCacheItemizedOverlay cacheItemizedOverlay;
	private Drawable cacheMarker;
	private DistanceToGeoCacheOverlay distanceOverlay;
	private UserLocationOverlay userOverlay;
	private TextView waitingLocationFixText;
	private MapView map;
	private MapController mapController;
	private List<Overlay> mapOverlays;
	private SearchGeoCacheManager manager;
	private ConnectionManager internetManager;
	private ImageView progressBarView;
	private AnimationDrawable progressBarAnim;
	private GoogleAnalyticsTracker tracker; 
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_geocache_map);
		
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start("UA-20327116-1", this);
		tracker.trackPageView("/searchActivity");
		
		waitingLocationFixText = (TextView) findViewById(R.id.waitingLocationFixText);
		progressBarView = (ImageView) findViewById(R.id.progressCircle);
		progressBarView.setBackgroundResource(R.anim.earth_anim);
		progressBarAnim = (AnimationDrawable) progressBarView.getBackground();
		map = (MapView) findViewById(R.id.searchGeocacheMap);
		mapOverlays = map.getOverlays();
		mapController = map.getController();
		userOverlay = new UserLocationOverlay(this, map);
		manager = new SearchGeoCacheManager(this);
		map.setBuiltInZoomControls(true);
		internetManager = Controller.getInstance().getConnectionManager(this);
		internetManager.addSubscriber(this);
		if (manager.getGeoCache() != null) {
			cacheMarker = Controller.getInstance().getMarker(manager.getGeoCache(), this);
			cacheItemizedOverlay = new GeoCacheItemizedOverlay(cacheMarker, this);
			cacheOverlayItem = new GeoCacheOverlayItem(manager.getGeoCache(), "", "");
			cacheItemizedOverlay.addOverlayItem(cacheOverlayItem);
			mapOverlays.add(cacheItemizedOverlay);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "on pause");
		manager.onPause();
		userOverlay.disableCompass();
		userOverlay.disableMyLocation();
		internetManager.removeSubscriber(this);
		tracker.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		userOverlay.enableCompass();
		userOverlay.enableMyLocation();
		manager.onResume();
		Log.d(TAG, "on pause");
		if (!internetManager.isInternetConnected()) {
			onInternetLost();
			Log.w(TAG, "internet not connected");
		}
		tracker.start("UA-20327116-1", this);
		tracker.trackPageView("/searchActivity");
	}

	/**
	 * Init and run all activity content
	 */
	@Override
	public void runLogic() {
		Log.d(TAG, "run logic");
		manager.runLogic();
		if (manager.getGeoCache() == null) {
			return;
		}
		if (!manager.isLocationFixed()) {
			Log.d(TAG, "run logic: location not fixed. Show gps status");
			mapController.animateTo(manager.getGeoCache().getLocationGeoPoint());
			// we need to run progress bar, but it will be done in
			// onFocusChanged
			progressBarView.setVisibility(View.VISIBLE);
		} else {
			progressBarView.setVisibility(View.GONE);
		}

		map.invalidate();
	}

	/**
	 * Start SearchGeoCacheCompass activity
	 */
	public void startCompassView() {
		Log.d(TAG, "start compass activity");
		
		Intent intent = new Intent(this, SearchGeoCacheCompass.class);
		if ((manager != null) && (manager.getGeoCache() != null)) {
			intent.putExtra(GeoCache.class.getCanonicalName(), manager.getGeoCache());
		}
		tracker.trackPageView("/compassActivity");
		startActivity(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.searchgeocache.ISearchActivity#updateLocation (android.location.Location)
	 */
	@Override
	public void updateLocation(Location location) {
		userOverlay.onLocationChanged(location);
		Log.d(TAG, "update location");
		if (progressBarView.getVisibility() == View.VISIBLE) {
			progressBarView.setVisibility(View.GONE);
		}
		if (distanceOverlay == null) {
			// It's really first run of update location
			Log.d(TAG, "update location: first run of this activity");
			distanceOverlay = new DistanceToGeoCacheOverlay(GpsHelper.locationToGeoPoint(location), manager.getGeoCache().getLocationGeoPoint());
			distanceOverlay.setCachePoint(manager.getGeoCache().getLocationGeoPoint());
			mapOverlays.add(distanceOverlay);
			mapOverlays.add(userOverlay);
			resetZoom();

			// DrivingDirections.Mode mode = Mode.WALKING;
			// DrivingDirections directions =
			// DrivingDirectionsFactory.createDrivingDirections();
			// directions.driveTo(Helper.locationToGeoPoint(location),manager.getGeoCache().getLocationGeoPoint()
			// , mode, this);

			return;
		}
		distanceOverlay.setUserPoint(GpsHelper.locationToGeoPoint(location));

		map.invalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.searchgeocache.ISearchActivity#updateBearing (int)
	 */
	@Override
	public void updateBearing(float bearing) {
		float[] values = new float[1];
		values[0] = bearing;
		// Log.d(TAG, "update bearing. New bearing=" +
		// Integer.toString(bearing));
		// FIXME: using deprecated constant
		userOverlay.onSensorChanged(Sensor.TYPE_ORIENTATION, values);
	}

	/**
	 * Set map zoom which can show userPoint and GeoCachePoint
	 */
	private void resetZoom() {
		GeoPoint currentGeoPoint = GpsHelper.locationToGeoPoint(manager.getCurrentLocation());
		// Calculating max and min lat and lon
		int minLat = Math.min(manager.getGeoCache().getLocationGeoPoint().getLatitudeE6(), currentGeoPoint.getLatitudeE6());
		int maxLat = Math.max(manager.getGeoCache().getLocationGeoPoint().getLatitudeE6(), currentGeoPoint.getLatitudeE6());
		int minLon = Math.min(manager.getGeoCache().getLocationGeoPoint().getLongitudeE6(), currentGeoPoint.getLongitudeE6());
		int maxLon = Math.max(manager.getGeoCache().getLocationGeoPoint().getLongitudeE6(), currentGeoPoint.getLongitudeE6());

		// Calculate span
		int latSpan = maxLat - minLat;
		int lonSpan = maxLon - minLon;

		// Set zoom
		mapController.zoomToSpan(latSpan, lonSpan);

		// Calculate new center of map
		GeoPoint center = new GeoPoint((manager.getGeoCache().getLocationGeoPoint().getLatitudeE6() + currentGeoPoint.getLatitudeE6()) / 2, (manager.getGeoCache().getLocationGeoPoint()
				.getLongitudeE6() + currentGeoPoint.getLongitudeE6()) / 2);

		// Set new center of map
		mapController.setCenter(center);
		map.invalidate();
		Projection proj = map.getProjection();
		// calculate padding
		int userPadding = (int) proj.metersToEquatorPixels(manager.getCurrentLocation().getAccuracy());
		Rect cacheBounds = cacheMarker.getBounds();
		// Get points of user and cache on screen
		Point userPoint = new Point();
		Point cachePoint = new Point();
		proj.toPixels(currentGeoPoint, userPoint);
		proj.toPixels(manager.getGeoCache().getLocationGeoPoint(), cachePoint);
		// Get map boundaries
		int mapRight = map.getRight();
		int mapBottom = map.getBottom();
		int mapLeft = map.getLeft();
		int mapTop = map.getTop();
		// Check contains markers in visible part of map
		boolean isCacheMarkerNotInMapX = (cachePoint.x + cacheBounds.left < mapLeft) || (cachePoint.x + cacheBounds.right > mapRight);
		boolean isCacheMarkerNotInMapY = (cachePoint.y + cacheBounds.top < mapTop) || (cachePoint.y + cacheBounds.bottom > mapBottom);
		boolean isUserMarkerNotInMapX = (userPoint.x - userPadding < mapLeft) || (userPoint.x + userPadding > mapRight);
		boolean isUserMarkerNotInMapY = (userPoint.y - userPadding < mapTop) || (userPoint.y + userPadding > mapBottom);
		boolean isMapDimensionsZeroes = mapRight == 0 && mapLeft == 0 && mapTop == 0 && mapBottom == 0;
		// if markers are not visible then zoomOut
		if ((isCacheMarkerNotInMapX || isCacheMarkerNotInMapY || isUserMarkerNotInMapX || isUserMarkerNotInMapY) && (!isMapDimensionsZeroes)) {
			Log.d(TAG, "markers not in the visible part of map. Zoom out.");
			mapController.zoomOut();
		}
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
			} else {
				mapController.animateTo(manager.getGeoCache().getLocationGeoPoint());
				Toast.makeText(getBaseContext(), R.string.status_null_last_location, Toast.LENGTH_SHORT).show();
			}
			return true;
		case R.id.menuStartCompass:
			this.startCompassView();
			return true;
		case R.id.menuGeoCacheInfo:
			UiHelper.showGeoCacheInfo(this, manager.getGeoCache());
			return true;
			// case R.id.DrawDirectionPath:
			// directionControlller.setVisibleWay();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.searchgeocache.ISearchActivity#updateStatus( java.lang.String, su.geocaching.android.ui.searchgeocache.StatusType)
	 */
	@Override
	public void updateStatus(String status, StatusType type) {
		if (type == StatusType.GPS) {
			waitingLocationFixText.setText(status);
		}
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
	 * @see su.geocaching.android.ui.searchgeocache.ISearchActivity#getLastKnownLocation ()
	 */
	@Override
	public Location getLastKnownLocation() {
		if (!manager.isLocationFixed()) {
			return null;
		}
		return manager.getCurrentLocation();
	}

	/**
	 * @return last known bearing
	 */
	public int getLastKnownBearing() {
		return manager.getCurrentBearing();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.geocachemap.IMapAware#onGeoCacheItemTaped(su .geocaching.android.ui.geocachemap.GeoCacheOverlayItem)
	 */
	@Override
	public void onGeoCacheItemTaped(GeoCacheOverlayItem item) {
		Intent intent = new Intent(this, ShowGeoCacheInfo.class);
		intent.putExtra(GeoCache.class.getCanonicalName(), manager.getGeoCache());
		this.startActivity(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.geocachemap.IInternetAware#onInternetLost()
	 */
	@Override
	public void onInternetLost() {
		Toast.makeText(this, getString(R.string.search_geocache_internet_lost), Toast.LENGTH_LONG).show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.geocachemap.IInternetAware#onInternetFound()
	 */
	@Override
	public void onInternetFound() {
		// TODO: do smthng?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see su.geocaching.android.ui.searchgeocache.ISearchActivity# onBestProviderUnavailable()
	 */
	@Override
	public void onBestProviderUnavailable() {
		if (progressBarView.getVisibility() == View.GONE) {
			progressBarView.setVisibility(View.VISIBLE);
		}
		updateStatus(getString(R.string.waiting_location_fix_message), StatusType.GPS);
		Toast.makeText(this, getString(R.string.search_geocache_best_provider_lost), Toast.LENGTH_LONG).show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		progressBarAnim.start();
	}

	@Override
	public void onDirectionsAvailable(IRoute route, Mode mode) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDirectionsNotAvailable() {
		// TODO Auto-generated method stub
	}

	public void onHomeClick(View v) {
		UiHelper.goHome(this);
	}
}