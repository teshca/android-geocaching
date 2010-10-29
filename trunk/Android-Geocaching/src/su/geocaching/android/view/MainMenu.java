package su.geocaching.android.view;

import su.geocaching.android.view.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	Main menu activity stub
 */
public class MainMenu extends Activity implements OnClickListener {
    
    public final static String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";
    protected final static String TAG = "su.geocaching.android";
    private final static int DEFAULT_GEOCACHE_ID_VALUE = 8984;
   

    private Button btSearchGeocache;
    private Button btSelectGeocache;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main_menu);

	btSearchGeocache = (Button) findViewById(R.id.btSearchGeocache);
	btSelectGeocache = (Button) findViewById(R.id.btSelectGeocache);
	
	btSearchGeocache.setOnClickListener(this);
	btSelectGeocache.setOnClickListener(this);
    }

    /**
     * Method which handle onClick.
     */
    @Override
    public void onClick(View v) {
	if (v.equals(btSearchGeocache)) {
	    startSearchGeoCache();
	} else if (v.equals(btSelectGeocache)) {
	    Log.d(TAG, "Not implemented or nor connected yet");
	    // TODO: connect with Select Geocache Activity
	} else {
	    Log.d(TAG, "unknown view was clicked");
	    // not implemented yet
	}
    }

    /**
     * Starting activity to search GeoCache
     */
    private void startSearchGeoCache() {
	    Intent intent = new Intent(this, SearchGeoCacheMap.class);
	    intent.putExtra(DEFAULT_GEOCACHE_ID_NAME, DEFAULT_GEOCACHE_ID_VALUE);
	    startActivity(intent);
	    this.finish();
    }
}