package su.geocaching.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.searchmap.SearchGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * Main activity in application
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class DashboardActivity extends Activity {

    private static final String TAG = DashboardActivity.class.getCanonicalName();
    private static final String DASHBOARD_ACTIVITY_FOLDER = "/DashboardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.dashboard_menu);

        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(DASHBOARD_ACTIVITY_FOLDER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.preference:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starting activity to select GeoCache
     * 
     * @param v
     *            //TODO describe it
     */
    public void onSelectClick(View v) {
        Intent intent = new Intent(this, SelectGeoCacheMap.class);
        startActivity(intent);

    }

    /**
     * Starting activity to search GeoCache
     * 
     * @param v
     *            //TODO describe it
     */
    public void onSearchClick(View v) {
        if (Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache() == null) {
            Toast.makeText(this.getBaseContext(), getString(R.string.search_geocache_start_without_geocache), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, SearchGeoCacheMap.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache());
        startActivity(intent);

    }

    /**
     * Starting about activity
     * 
     * @param v
     *            //TODO describe it
     */
    public void onSettingsClick(View v) {
        startActivity(new Intent(this, DashboardPreferenceActivity.class));
    }

    /**
     * Starting activity with favorites geocaches
     * 
     * @param v
     *            //TODO describe it
     */
    public void onFavoriteClick(View v) {
        Intent intent = new Intent(this, FavoritesFolder.class);
        startActivity(intent);
    }
}
