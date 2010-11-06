package su.geocaching.android.view.geoCacheMap;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.*;

import java.util.List;

import su.geocaching.android.view.R;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        Search GeoCache with the map.
 */
public abstract class GeoCacheMap extends MapActivity implements IActivityWithLocation {
    protected MapView map;
    protected MapController mapController;
    protected SearchGeoCacheLocationManager locationManager;
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
        
        //TODO: deal with the transfer of resources
        // Log.d("albama", Integer.toString(id));
        // Log.d("albama", Boolean.toString(mvMap == null));
	locationManager = new SearchGeoCacheLocationManager(
		this, (LocationManager) this.getSystemService(LOCATION_SERVICE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        map.setBuiltInZoomControls(true);
        mapOverlays = map.getOverlays();
        locationManager.resume();
//        compassManager = new SearchGeoCacheCompassManager(this);
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

    //public abstract float getLastAzimuth();
    public abstract Location getLastLocation();
    
}