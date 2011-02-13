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

/**
 * Main activity in application
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010 Main menu activity stub
 */
public class MenuActivity extends Activity {
	private static final String TAG = MenuActivity.class.getCanonicalName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.dashboard_menu);
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
		Intent intent = new Intent(this, SearchGeoCacheMap.class);
		intent.putExtra(GeoCache.class.getCanonicalName(), Controller.getInstance().getLastSearchedGeoCache(this));
		startActivity(intent);
	}

	/**
	 * Starting about activity
	 */
	public void onAboutClick(View v) {
		Intent intent = new Intent(this, su.geocaching.android.ui.AboutActivity.class);
		startActivity(intent);
	}

	/**
	 * Starting activity with favorites geocaches
	 */
	public void onFavoriteClick(View v) {
		Intent intent = new Intent(this, su.geocaching.android.ui.FavoritesFolder.class);
		startActivity(intent);
	}
}
