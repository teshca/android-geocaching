package su.geocaching.android.ui;

import android.app.Dialog;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;

/**
 * Main activity of the application
 *
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class DashboardActivity extends SherlockActivity {

    private static final String DASHBOARD_ACTIVITY_NAME = "/DashboardActivity";
    private static final int ABOUT_DIALOG_ID = 0;
    private static final int ASK_FOR_RATING_DIALOG_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_background));
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(DASHBOARD_ACTIVITY_NAME);        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        //  check if we want to display dialog asking for rating and feedback
        if (!Controller.getInstance().getPreferencesManager().isAskForRatingShown()
            && Controller.getInstance().getPreferencesManager().getNumberOfRuns() > 8
            && Controller.getInstance().getConnectionManager().isWifiConnected()) {
            this.showDialog(ASK_FOR_RATING_DIALOG_ID);
            Controller.getInstance().getPreferencesManager().setAskForRatingShown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.dashboard_option_menu, menu);
        return true;
    }
     @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.give_feedback).setVisible(NavigationManager.isAndroidMarketAvailable(this));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                //this.openOptionsMenu();
                //return true;
            case R.id.about:
                this.showDialog(ABOUT_DIALOG_ID);
                return true;
            case R.id.give_feedback:
                NavigationManager.startGeocachingGooglePlayActivity(this);
                return true;
            case R.id.developers_email:
                NavigationManager.SendEmailToDevelopers(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ABOUT_DIALOG_ID: 
                return new AboutDialog(this);
            case ASK_FOR_RATING_DIALOG_ID:
                return new AskForRatingDialog(this);
        }
        return null;
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
}
