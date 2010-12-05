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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

    private GraphicCompassView compassView;
    private SearchGeoCacheManager manager;
    private TextView distanceToCache;
    private ImageView progressCircle;
    private TextView statusText;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(TAG, "on create");
	setContentView(R.layout.search_geocache_compass);
	compassView = (GraphicCompassView) findViewById(R.id.compassView);
	manager = new SearchGeoCacheManager(this);
	distanceToCache = (TextView) findViewById(R.id.DistanceValue);
	setDistance(0);
	progressCircle = (ImageView) findViewById(R.id.progressCircle);
	statusText = (TextView) findViewById(R.id.waitingLocationFixText);
    }

    private void setDistance(float distance) {
	if (!manager.isLocationFixed()) {
	    distanceToCache.setText(R.string.distance_unknown);
	    Toast.makeText(this, R.string.search_geocache_best_provider_lost, Toast.LENGTH_LONG).show();
	} else {
	    distanceToCache.setText(Helper.distanceToString(distance));
	}
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
    protected void onRestart() {
	super.onRestart();

    }

    /**
     * Run all activity logic
     */
    @Override
    public void runLogic() {
	manager.runLogic();
	if (manager.getGeoCache() == null) {
	    return;
	}
	if (!manager.isLocationFixed()) {
	    Log.d(TAG, "run logic: location not fixed. Show gps status");
	    progressCircle.setVisibility(View.VISIBLE);
	    Animation progressCircleAnim = AnimationUtils.loadAnimation(this, R.anim.progress_circle);
	    progressCircle.startAnimation(progressCircleAnim);
	} else {
	    progressCircle.setVisibility(View.GONE);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#updateBearing
     * (int)
     */
    public void updateBearing(int bearing) {
	compassView.setBearingToNorth(bearing);
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
	progressCircle.clearAnimation();
	progressCircle.setVisibility(View.GONE);
	compassView.setBearingToGeoCache(Helper.getBearingBetween(location, manager.getGeoCache().getLocationGeoPoint()));
	setDistance(Helper.getDistanceBetween(location, manager.getGeoCache().getLocationGeoPoint()));
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
	compassView.setLocationFix(manager.isLocationFixed());
	// TODO add status field
	if (type == ISearchActivity.STATUS_TYPE_GPS) {
	    statusText.setText(status);
	}
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

    @Override
    public void onBestProviderUnavailable() {
	// TODO tell user about this
    }
}
