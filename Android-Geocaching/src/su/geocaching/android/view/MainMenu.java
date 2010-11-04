package su.geocaching.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import su.geocaching.android.searchGeoCache.SearchGeoCacheMap;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        Main menu activity stub
 */
public class MainMenu extends Activity implements OnClickListener {

    public static final String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";
    protected static final String TAG = "su.geocaching.android";
    private static final int DEFAULT_GEOCACHE_ID_VALUE = 8984;

    private Button btSearchGeoCache;
    private Button btSelectGeoCache;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        btSearchGeoCache = (Button) findViewById(R.id.btSearchGeocache);
        btSelectGeoCache = (Button) findViewById(R.id.btSelectGeocache);

        btSearchGeoCache.setOnClickListener(this);
        btSelectGeoCache.setOnClickListener(this);
    }

    /**
     * Method which handle onClick.
     */
    @Override
    public void onClick(View v) {
        if (v.equals(btSearchGeoCache)) {
            startSearchGeoCache();
        } else if (v.equals(btSelectGeoCache)) {
            Log.d(TAG, "Not implemented or nor connected yet");
            // TODO: connect with Select GeoCache Activity
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