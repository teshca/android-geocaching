package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.Helper;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        <p>
 *        Search GeoCache with the compass.
 *        </p>
 */
public class SearchGeoCacheCompass extends Activity implements ISearchActivity {
    private static final String TAG = SearchGeoCacheCompass.class.getCanonicalName();

    private GraphicCompassView compassView;
    private SearchGeoCacheManager manager;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.search_geocache_compass);
	compassView = (GraphicCompassView) findViewById(R.id.compassView);
	manager = new SearchGeoCacheManager(this);
	Log.d(TAG, "on create");
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	manager.onResume();
	Log.d(TAG, "on resume");
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
	super.onPause();
	manager.onPause();
	Log.d(TAG, "on pause");
    }
    
    @Override
    protected void onDestroy() {
	super.onDestroy();
	manager.onDestroy();
    }

    /**
     * Run all activity logic
     */
    @Override
    public void runLogic() {
	manager.runLogic();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#updateAzimuth
     * (int)
     */
    public void updateBearing(int azimuth) {
	compassView.setAzimuthToNorth(azimuth);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#updateLocation
     * (android.location.Location)
     */
    @Override
    public void updateLocation(Location location) {
	if (!manager.isLocationFixed()) {
	    return;
	}
	compassView.setAzimuthToGeoCache(Helper.getBearingBetween(location, manager.getGeoCache().getLocationGeoPoint()));
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
	    this.startMapView();
	    return true;
	case R.id.menuGeoCacheInfo:
	    manager.showGeoCacheInfo();
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
	intent.putExtra(GeoCache.class.getCanonicalName(), manager.getGeoCache());
	startActivity(intent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#updateStatus(
     * java.lang.String, int)
     */
    @Override
    public void updateStatus(String status, int type) {
	// TODO add status field

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#getLastKnownLocation
     * ()
     */
    @Override
    public Location getLastKnownLocation() {
	return manager.getCurrentLocation();
    }
}
