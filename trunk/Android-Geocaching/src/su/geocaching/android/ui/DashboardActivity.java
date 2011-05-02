package su.geocaching.android.ui;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.selectmap.SelectMapActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
        setContentView(R.layout.dashboard_view);

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
     * @param v //TODO describe it
     */
    public void onSelectClick(View v) {
        Intent intent = new Intent(this, SelectMapActivity.class);
        startActivity(intent);

    }

    /**
     * Starting activity to search GeoCache
     *
     * @param v //TODO describe it
     */
    public void onSearchClick(View v) {
        if (Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache() == null) {
            Toast.makeText(this.getBaseContext(), getString(R.string.search_geocache_start_without_geocache), Toast.LENGTH_SHORT).show();
            return;
        }
        UiHelper.startSearchMapActivity(this,  Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache());
    }

    /**
     * Starting about activity
     *
     * @param v //TODO describe it
     */
    public void onSettingsClick(View v) {
        startActivity(new Intent(this, DashboardPreferenceActivity.class));
    }

    /**
     * Starting activity with favorites geocaches
     *
     * @param v //TODO describe it
     */
    public void onFavoriteClick(View v) {
        Intent intent = new Intent(this, FavoritesFolderActivity.class);
        startActivity(intent);
    }
}
