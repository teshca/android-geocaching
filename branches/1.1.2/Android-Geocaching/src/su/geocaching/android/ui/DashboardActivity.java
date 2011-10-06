package su.geocaching.android.ui;

import android.app.Dialog;
import android.widget.Toast;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.NavigationManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import su.geocaching.android.model.GeoCache;

/**
 * Main activity of the application
 *
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class DashboardActivity extends Activity {

    private static final String DASHBOARD_ACTIVITY_NAME = "/DashboardActivity";
    private static final int ABOUT_DIALOG_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(DASHBOARD_ACTIVITY_NAME);
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
            case R.id.about:
                this.showDialog(ABOUT_DIALOG_ID);
                return true;
            case R.id.give_feedback:
                NavigationManager.startAndroidMarketActivity(this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        return (id == ABOUT_DIALOG_ID) ? new AboutDialog(this) : null;
    }

    /**
     * Starting activity to select GeoCache
     */
    public void onSelectClick(View v) {
        NavigationManager.startSelectMapActivity(this);
    }

    /**
     * Starting activity to search GeoCache
     */
    public void onSearchClick(View v) {
        GeoCache geoCache = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache();
        if (geoCache == null) {
            Toast.makeText(this, this.getString(R.string.search_geocache_start_without_geocache), Toast.LENGTH_SHORT).show();
            return;
        }
        NavigationManager.startSearchMapActivity(this, geoCache);
    }

    /**
     * Start preference activity
     */
    public void onSettingsClick(View v) {
        NavigationManager.startPreferencesActivity(this);
    }

    /**
     * Start favorites GeoCaches list
     */
    public void onFavoritesClick(View v) {
        NavigationManager.startFavoritesActivity(this);
    }

    /**
     * User clicked on Geocaching.su logo
     */
    public void onHomeClick(View v) {
        this.showDialog(ABOUT_DIALOG_ID);
    }
}
