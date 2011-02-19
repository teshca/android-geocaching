package su.geocaching.android.ui;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Main activity in application
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010 Main menu activity stub
 */
public class DashboardActivity extends Activity {
	private static final String TAG = DashboardActivity.class.getCanonicalName();
	private GoogleAnalyticsTracker tracker;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.dashboard_menu);
		
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start("UA-20327116-3", this);
		tracker.trackPageView("/mainWindow");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.enableGps:
			startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
			return true;
		case R.id.enableInternet:
			startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Starting activity to select GeoCache
	 */
	public void onSelectClick(View v) {
		tracker.trackEvent(
				"Cliks", 
				"Button", 
				"select", 
				77);
		tracker.trackPageView("/SelectActivity");
		Intent intent = new Intent(this, SelectGeoCacheMap.class);
		startActivity(intent);
		
	}

	/**
	 * Starting activity to search GeoCache
	 */
	public void onSearchClick(View v) {
		if (Controller.getInstance().getLastSearchedGeoCache(this) == null) {
			Toast.makeText(this.getBaseContext(), getString(R.string.search_geocache_start_without_geocache), Toast.LENGTH_SHORT).show();
			return;
		}
		tracker.trackEvent(
				"Cliks", 
				"Button", 
				"search", 
				71);
		tracker.trackPageView("/SearchActivity");
		Intent intent = new Intent(this, SearchGeoCacheMap.class);
		intent.putExtra(GeoCache.class.getCanonicalName(), Controller.getInstance().getLastSearchedGeoCache(this));
		startActivity(intent);
		
	}

	/**
	 * Starting about activity
	 */
	public void onAboutClick(View v) {
		tracker.trackEvent(
				"Cliks", 
				"Button", 
				"about", 
				72);
		tracker.trackPageView("/AboutActivity");
		Intent intent = new Intent(this, su.geocaching.android.ui.AboutActivity.class);
		startActivity(intent);
		
	}

	/**
	 * Starting activity with favorites geocaches
	 */
	public void onFavoriteClick(View v) {
		tracker.trackEvent(
				"Cliks", 
				"Button", 
				"favorites", 
				73);
		tracker.trackPageView("/FavoriteActivity");
		Intent intent = new Intent(this, su.geocaching.android.ui.FavoritesFolder.class);
		startActivity(intent);
		
	}
}
