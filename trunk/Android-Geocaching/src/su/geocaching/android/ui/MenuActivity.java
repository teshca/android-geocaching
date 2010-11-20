package su.geocaching.android.ui;

import su.geocaching.android.application.ApplicationMain;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheCompass;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.view.userstory.favoritwork.FavoritFolder;
import su.geocaching.android.view.userstory.incocach.Info_cach;
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
import android.widget.ImageButton;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 Main menu activity stub
 */
public class MenuActivity extends Activity implements OnClickListener {

    private static final String TAG = MenuActivity.class.getCanonicalName();

    private Button searchButton;
    private Button selectButton;
    private Button infoButton;
    private Button favoritButton;
    private ApplicationMain application;
    private ImageButton titleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(TAG, "onCreate");

	setContentView(R.layout.menu);
	initButtons();
    }

    private void initButtons() {
	searchButton = (Button) findViewById(R.id.SearchButton);
	selectButton = (Button) findViewById(R.id.SelectButton);
	infoButton = (Button) findViewById(R.id.InfoButton);
	favoritButton = (Button) findViewById(R.id.FavoritesButton);
	titleButton = (ImageButton) findViewById(R.id.title_button);

	searchButton.setOnClickListener(this);
	selectButton.setOnClickListener(this);
	infoButton.setOnClickListener(this);
	favoritButton.setOnClickListener(this);

	application = (ApplicationMain) getApplication();
	titleButton.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
	if (v.equals(searchButton)) {
	    startSearchGeoCache();
	} else if (v.equals(selectButton)) {
	    startSelectGeoCache();
	} else if (v.equals(infoButton)) {
	    startInfoGeoCache();
	} else if (v.equals(favoritButton)) {
	    startFavoritFolder();
	}else if (v.equals(titleButton)) {
	    Intent intent = new Intent(this, SearchGeoCacheCompass.class);
	    startActivity(intent);
	}
	
	
    }

    /**
     * Starting activity to search GeoCache
     */
    private void startSearchGeoCache() {
	if (application.getDesiredGeoCache() == null) {
	    Toast.makeText(this.getBaseContext(), getString(R.string.search_geocache_start_without_geocache), Toast.LENGTH_SHORT).show();
	    return;
	}
	Intent intent = new Intent(this, SearchGeoCacheMap.class);
	intent.putExtra(GeoCache.class.getCanonicalName(), application.getDesiredGeoCache());
	startActivity(intent);
    }

    /**
     * Starting activity to select GeoCache
     */
    private void startSelectGeoCache() {
	// TODO check internet
	Intent intent = new Intent(this, SelectGeoCacheMap.class);
	intent.putExtra("layout", R.layout.select_geocache_map);
	intent.putExtra("mapID", R.id.selectGeocacheMap);
	startActivity(intent);
    }

    private void startInfoGeoCache() {
	Intent intent = new Intent(this, Info_cach.class);
	startActivity(intent);
    }

    private void startFavoritFolder() {
	Intent intent = new Intent(this, FavoritFolder.class);
	startActivity(intent);
    }
}
