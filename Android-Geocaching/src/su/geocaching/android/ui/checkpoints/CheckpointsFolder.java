package su.geocaching.android.ui.checkpoints;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.adapters.FavoritesArrayAdapter;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.DbManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.FavoritesFolderActivity;
import su.geocaching.android.ui.R;

import java.util.ArrayList;

/**
 * Class for create ListActivity with checkpoints caches
 *
 * @author Nikita Bumakov
 */
public class CheckpointsFolder extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = FavoritesFolderActivity.class.getCanonicalName();

    private CheckpointManager checkpointManager;
    private DbManager dbm;
    private FavoritesArrayAdapter checkpointsAdapter;
    private AlertDialog removeAlert;
    private TextView tvNoCache;
    private ListView lvListShowCache;
    private int cacheId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_folder_activity);

        cacheId = getIntent().getIntExtra(NavigationManager.CACHE_ID, 0);
        checkpointManager = Controller.getInstance().getCheckpointManager(cacheId);
        dbm = Controller.getInstance().getDbManager();
        checkpointsAdapter = new FavoritesArrayAdapter(this);

        lvListShowCache = (ListView) findViewById(R.id.favorite_folder_listCache);
        tvNoCache = (TextView) findViewById(R.id.favorite_folder_title_text);

        lvListShowCache.setOnItemClickListener(this);
        lvListShowCache.setAdapter(checkpointsAdapter);
        initRemoveDialog();
    }

    private void initRemoveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeAllCheckpoints();
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        removeAlert = builder.create();
    }

    @Override
    protected void onResume() {
        ArrayList<GeoCache> checkpointList = dbm.getCheckpointsArrayById(cacheId);
        checkpointsAdapter.clear();
        if (checkpointList.isEmpty()) {
            tvNoCache.setVisibility(View.VISIBLE);
            LogManager.d(TAG, "checkpoints DB empty");
        } else {
            checkpointList.add(0, Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache());
            tvNoCache.setVisibility(View.GONE);
            for (GeoCache gc : checkpointList) {
                checkpointsAdapter.add(gc);
            }
        }
        checkpointsAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (checkpointsAdapter.isEmpty()) {
            menu.getItem(1).setEnabled(false);
        } else {
            menu.getItem(1).setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.checkpoints_folder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addCheckpointMenu:
                NavigationManager.startCreateCheckpointActivity(this, Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache());
                break;
            case R.id.removeAllCheckpointMenu:
                removeAlert.show();
                break;
        }
        return true;
    }

    private void removeAllCheckpoints() {
        checkpointsAdapter.clear();
        checkpointManager.clear();
        tvNoCache.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View cv, int position, long id) {
        NavigationManager.startCheckpointDialog(this, ((GeoCache) adapter.getItemAtPosition(position)).getId());
    }
}
