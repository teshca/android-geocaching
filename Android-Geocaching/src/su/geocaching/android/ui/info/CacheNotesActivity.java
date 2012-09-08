package su.geocaching.android.ui.info;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import su.geocaching.android.model.GeoCache;

/**
 * Activity for show and write notes about geocache
 *
 * @author Nikita Bumakov
 */
public class CacheNotesActivity extends SherlockActivity {

    private static String TAG = CacheNotesActivity.class.getCanonicalName();
    private EditText cacheNotesText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final GeoCache geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        setContentView(su.geocaching.android.ui.R.layout.cache_notes_activity);
        cacheNotesText = (EditText) findViewById(su.geocaching.android.ui.R.id.cache_notes_text);
        Button saveButton = (Button) findViewById(su.geocaching.android.ui.R.id.text_save_button);

        cacheNotesText.setText(Controller.getInstance().getDbManager().getNoteById(geoCache.getId()));
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Controller.getInstance().getDbManager().updateNotes(geoCache.getId(), cacheNotesText.getText().toString());
                finish();
            }
        });

        getSupportActionBar().setTitle(geoCache.getName());
        getSupportActionBar().setHomeButtonEnabled(true);

        LogManager.d(TAG, "CacheNotesActivity created");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavigationManager.startDashboardActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}