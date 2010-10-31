package su.geocaching.android.view;

import su.geocaching.android.model.GeoCache;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	Search GeoCache with the compas.
 */
public class SearchGeoCacheCompas extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.search_geocache_compas);
    }
}
