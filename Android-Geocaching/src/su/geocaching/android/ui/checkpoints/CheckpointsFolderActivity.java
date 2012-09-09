package su.geocaching.android.ui.checkpoints;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.adapters.FavoritesArrayAdapter;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.FavoritesFolderActivity;
import su.geocaching.android.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for create ListActivity with checkpoints caches
 *
 * @author Nikita Bumakov
 */
public class CheckpointsFolderActivity extends SherlockListActivity {

    private static final String TAG = FavoritesFolderActivity.class.getCanonicalName();
    private static final String CHECKPOINT_FOLDER_ACTIVITY_NAME = "/CheckpointFolderActivity";
    private static final int DELETE_CHECKPOINTS_DIALOG_ID = 1;

    private CheckpointManager checkpointManager;
    private FavoritesArrayAdapter checkpointsAdapter;
    private TextView tvNoCache;
    private GeoCache geoCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoints_folder_activity);

        geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        checkpointManager = Controller.getInstance().getCheckpointManager(geoCache.getId());
        checkpointsAdapter = new FavoritesArrayAdapter(this);

        tvNoCache = (TextView) findViewById(R.id.tvNoCheckpoints);

        getSupportActionBar().setTitle(geoCache.getName());
        getSupportActionBar().setHomeButtonEnabled(true);

        setListAdapter(checkpointsAdapter);

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(CHECKPOINT_FOLDER_ACTIVITY_NAME);
    }

    @Override
    protected void onResume() {
        List<GeoCache> checkpointList = new ArrayList<GeoCache>(checkpointManager.getCheckpoints());
        checkpointsAdapter.clear();
        if (checkpointList.isEmpty()) {
            tvNoCache.setVisibility(View.VISIBLE);
            LogManager.d(TAG, "checkpoints DB empty");
        } else {
            checkpointList.add(0, geoCache);
            tvNoCache.setVisibility(View.GONE);
            for (GeoCache gc : checkpointList) {
                checkpointsAdapter.add(gc);
            }
        }
       // checkpointsAdapter.sort();
        checkpointsAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.removeAllCheckpointMenu).setEnabled(!checkpointsAdapter.isEmpty());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.checkpoints_folder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavigationManager.startDashboardActivity(this);
                break;
            case R.id.addCheckpointMenu:
                NavigationManager.startCreateCheckpointActivity(this, geoCache);
                break;
            case R.id.removeAllCheckpointMenu:
                showDialog(DELETE_CHECKPOINTS_DIALOG_ID);
                break;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DELETE_CHECKPOINTS_DIALOG_ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.checkpoints_folder_confirm_delete_title);
                builder.setMessage(getString(R.string.checkpoints_folder_confirm_delete_message));
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
                return builder.create();
            }
            default: {
                return null;
            }
        }
    }

    private void removeAllCheckpoints() {
        checkpointsAdapter.clear();
        checkpointManager.clear();
        tvNoCache.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        NavigationManager.startCheckpointDialog(this, ((GeoCache) this.getListAdapter().getItem(position)).getId());
    }
}
