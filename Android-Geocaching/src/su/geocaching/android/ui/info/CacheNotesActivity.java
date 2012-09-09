package su.geocaching.android.ui.info;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import android.os.Bundle;
import android.widget.EditText;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;

/**
 * Activity for show and write notes about geocache
 *
 * @author Nikita Bumakov
 */
public class CacheNotesActivity extends SherlockActivity {

    private static String TAG = CacheNotesActivity.class.getCanonicalName();
    private static final String CACHE_NOTES_ACTIVITY_NAME = "/CacheNotesActivity";
    private EditText cacheNotesText;
    private GeoCache geoCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        setContentView(su.geocaching.android.ui.R.layout.cache_notes_activity);

        cacheNotesText = (EditText) findViewById(su.geocaching.android.ui.R.id.cache_notes_text);

        getSupportActionBar().setTitle(geoCache.getName());
        getSupportActionBar().setHomeButtonEnabled(true);

        LogManager.d(TAG, "CacheNotesActivity created");

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(CACHE_NOTES_ACTIVITY_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.cache_notes_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavigationManager.startDashboardActivity(this);
                return true;
            case R.id.menu_cache_notes_save:
                Controller.getInstance().getDbManager().updateNotes(geoCache.getId(), cacheNotesText.getText().toString());
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}