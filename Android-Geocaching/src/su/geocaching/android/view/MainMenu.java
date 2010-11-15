package su.geocaching.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import su.geocaching.android.view.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.view.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.view.userstory.favoritwork.FavoritFolder;
import su.geocaching.android.view.userstory.incocach.Info_cach;
/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 Main menu activity stub
 */
public class MainMenu extends Activity implements OnClickListener {

    public static final String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";
    private static final int DEFAULT_GEOCACHE_ID_VALUE = 8984;
    private static final String TAG = MainMenu.class.getCanonicalName();

    private Button btSearchGeoCache;
    private Button btSelectGeoCache;
    private Button btInfoGeoCache;
    private Button btFavoritFild;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main_menu);

	btSearchGeoCache = (Button) findViewById(R.id.btSearchGeocache);
	btSelectGeoCache = (Button) findViewById(R.id.btSelectGeocache);
	btInfoGeoCache = (Button)findViewById(R.id.btInfoGeocache);
	btFavoritFild=(Button)findViewById(R.id.btFavoritFolder);
	
	btSearchGeoCache.setOnClickListener(this);
	btSelectGeoCache.setOnClickListener(this);
	btInfoGeoCache.setOnClickListener(this);
	btFavoritFild.setOnClickListener(this);
    }

    /**
     * Method which handle onClick.
     */
    @Override
    public void onClick(View v) {
	if (v.equals(btSearchGeoCache)) {
	    startSearchGeoCache();
	} else if (v.equals(btSelectGeoCache)) {
	    startSelectGeoCache();
	} else  if(v.equals(btInfoGeoCache)){
	    startInfoGeoCache();
	}else if (v.equals(btFavoritFild)){
	    startFavoritFolder();
	}else {
	    Log.d(TAG, "unknown view was clicked");
	    // not implemented yet
	}
	
    }

	private void startInfoGeoCache(){
	    Intent intent = new Intent(this, Info_cach.class);
	    startActivity(intent);
	}
	
    /**
     * Starting activity to search GeoCache
     */
    private void startSearchGeoCache() {
	Intent intent = new Intent(this, SearchGeoCacheMap.class);
	intent.putExtra(DEFAULT_GEOCACHE_ID_NAME, DEFAULT_GEOCACHE_ID_VALUE);
	intent.putExtra("layout", R.layout.search_geocache_map);
	intent.putExtra("mapID", R.id.searchGeocacheMap);
	startActivity(intent);
    }
    private void startFavoritFolder() {
	Intent intent = new Intent(this, FavoritFolder.class);
	startActivity(intent);
    }
    /**
     * Starting activity to select GeoCache
     */
    private void startSelectGeoCache() {
	// this code does not work. it is meaningless
	// ConnectivityManager cm = (ConnectivityManager)
	// getSystemService(Context.CONNECTIVITY_SERVICE);
	// if (cm.getActiveNetworkInfo() == null ||
	// !cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
	// Toast.makeText(getApplicationContext(),
	// "You must be connected to the Internet!", Toast.LENGTH_SHORT);
	// } else {
	Intent intent = new Intent(this, SelectGeoCacheMap.class);
	intent.putExtra("layout", R.layout.select_geocache_map);
	intent.putExtra("mapID", R.id.selectGeocacheMap);
	startActivity(intent);
	// this.finish();
	// }
    }
}