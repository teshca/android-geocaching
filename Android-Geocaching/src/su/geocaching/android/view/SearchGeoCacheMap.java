package su.geocaching.android.view;

import su.geocaching.android.model.GeoCache;
import su.geocaching.android.view.R;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	Search GeoCache with map.
 */
public class SearchGeoCacheMap extends MapActivity {

    private MapView mvMap;
    private MapController mcMapController;
    private GeoCache geoCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.search_geocache_map);
	Intent intent = this.getIntent();
	geoCache = new GeoCache(intent.getIntExtra(
		MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
	mvMap = (MapView) findViewById(R.id.searchGeocacheMap);
	mcMapController = mvMap.getController();
	mvMap.setBuiltInZoomControls(true);
    }

    @Override
    protected void onResume() {
	super.onResume();
	// TODO: request updates from location manager, show user location and
	// geocache location on the map etc...
    }

    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

}
