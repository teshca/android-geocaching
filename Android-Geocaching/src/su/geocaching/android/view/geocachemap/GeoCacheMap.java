package su.geocaching.android.view.geocachemap;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import su.geocaching.android.view.R;

import java.util.List;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 * @description Search GeoCache with the map.
 */
public abstract class GeoCacheMap extends MapActivity implements ILocationAware {
    protected MapView map;
    protected MapController mapController;
    protected GeoCacheLocationManager locationManager;
    protected List<Overlay> mapOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Intent intent = this.getIntent();
	int layout = intent.getIntExtra("layout", R.layout.select_geocache_map);
	int mapID = intent.getIntExtra("mapID", R.id.selectGeocacheMap);
	setContentView(layout);
	map = (MapView) findViewById(mapID);
	mapController = map.getController();

	// TODO: deal with the transfer of resources
	locationManager = new GeoCacheLocationManager(this, (LocationManager) this.getSystemService(LOCATION_SERVICE));
    }

    @Override
    protected void onResume() {
	super.onResume();

	map.setBuiltInZoomControls(true);
	mapOverlays = map.getOverlays();
	locationManager.resume();
    }

    @Override
    protected void onPause() {
	super.onPause();
	locationManager.pause();
    }

    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

    /**
     * @return last known location of location manager
     */
    public abstract Location getLastLocation();

}