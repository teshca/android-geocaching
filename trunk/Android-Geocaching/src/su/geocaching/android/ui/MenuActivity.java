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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * Main activity in application
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010 Main menu activity stub
 */
public class MenuActivity extends Activity implements OnClickListener {

    private static final String TAG = MenuActivity.class.getCanonicalName();

    private Button searchButton;
    private Button selectButton;
    private Button favoriteButton;
    private Button aboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(TAG, "onCreate");

	setContentView(R.layout.dashboard_menu);
	initButtons();
    }

    private void initButtons() {
	searchButton = (Button) findViewById(R.id.SearchButton);
	selectButton = (Button) findViewById(R.id.SelectButton);
	favoriteButton = (Button) findViewById(R.id.FavoritesButton);
	aboutButton = (Button) findViewById(R.id.AboutButton);

	searchButton.setOnClickListener(this);
	selectButton.setOnClickListener(this);
	favoriteButton.setOnClickListener(this);
	aboutButton.setOnClickListener(this);
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

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
	if (v.equals(searchButton)) {
	    startSearchGeoCache();
	} else if (v.equals(selectButton)) {
	    startSelectGeoCache();
	} else if (v.equals(favoriteButton)) {
	    startFavoriteFolder();
	} else if (v.equals(aboutButton)) {
	    startAboutActivity();
	}
    }

    /**
     * Starting activity to search GeoCache
     */
    private void startSearchGeoCache() {
	if (Controller.getInstance().getLastSearchedGeoCache(this) == null) {
	    Toast.makeText(this.getBaseContext(), getString(R.string.search_geocache_start_without_geocache), Toast.LENGTH_SHORT).show();
	    return;
	}
	Intent intent = new Intent(this, SearchGeoCacheMap.class);
	intent.putExtra(GeoCache.class.getCanonicalName(), Controller.getInstance().getLastSearchedGeoCache(this));
	startActivity(intent);
    }

    /**
     * Starting activity to select GeoCache
     */
    private void startSelectGeoCache() {
	Intent intent = new Intent(this, SelectGeoCacheMap.class);
        intent.putExtra("map_info", Controller.getInstance().getLastMapInfo(this));
	startActivity(intent);
    }

    private void startAboutActivity() {
	Intent intent = new Intent(this, su.geocaching.android.ui.AboutActivity.class);
	startActivity(intent);
    }

    private void startFavoriteFolder() {
	Intent intent = new Intent(this, su.geocaching.android.view.favoriteswork.FavoritesFolder.class);
	startActivity(intent);
    }
}
