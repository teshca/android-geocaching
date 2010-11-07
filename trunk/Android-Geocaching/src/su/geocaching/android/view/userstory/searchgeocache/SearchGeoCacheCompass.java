package su.geocaching.android.view.userstory.searchgeocache;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import su.geocaching.android.view.R;
import su.geocaching.android.view.geocachemap.*;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        Search GeoCache with the compass.
 */
public class SearchGeoCacheCompass extends Activity implements IActivityWithLocation,IActivityWithCompass {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_geocache_compass);
    }
    
	@Override
	public void updateAzimuth(float azimuth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLocation(Location location) {
		// TODO Auto-generated method stub
		
	}
}
