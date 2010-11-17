package su.geocaching.android.ui;

import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.view.userstory.favoritwork.FavoritFolder;
import su.geocaching.android.view.userstory.incocach.Info_cach;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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

	searchButton.setOnClickListener(this);
	selectButton.setOnClickListener(this);
	infoButton.setOnClickListener(this);
	favoritButton.setOnClickListener(this);
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
	}
    }

    /**
     * Starting activity to search GeoCache
     */
    private void startSearchGeoCache() {
	Intent intent = new Intent(this, SearchGeoCacheMap.class);
	// TODO fix this
	intent.putExtra("GeoCache id", 8984);
	intent.putExtra("layout", R.layout.search_geocache_map);
	intent.putExtra("mapID", R.id.searchGeocacheMap);
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

    // TODO Delete it
    public static final String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";

}
