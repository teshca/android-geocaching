package su.geocaching.android.ui.checkpoints;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.adapters.CheckpointsArrayAdapter;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for create ListActivity with checkpoints caches
 *
 * @author Nikita Bumakov
 */
public class CheckpointsFolderActivity extends SherlockListActivity {

    private static final String TAG = CheckpointsFolderActivity.class.getCanonicalName();
    private static final String CHECKPOINT_FOLDER_ACTIVITY_NAME = "/CheckpointFolderActivity";
    private static final int DELETE_CHECKPOINTS_DIALOG_ID = 1;

    private CheckpointManager checkpointManager;
    private CheckpointsArrayAdapter checkpointsAdapter;
    private TextView tvNoCache;
    private GeoCache geoCache;

    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoints_folder_activity);

        geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        checkpointManager = Controller.getInstance().getCheckpointManager(geoCache.getId());
        checkpointsAdapter = new CheckpointsArrayAdapter(this);

        tvNoCache = (TextView) findViewById(R.id.tvNoCheckpoints);

        getSupportActionBar().setTitle(geoCache.getName());
        getSupportActionBar().setHomeButtonEnabled(true);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListAdapter(checkpointsAdapter);

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(CHECKPOINT_FOLDER_ACTIVITY_NAME);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        if (getListView().getCheckedItemPosition() != -1) {
            mActionMode = startActionMode(mActionModeCallback);
        }
    }

    @Override
    protected void onResume() {
        refreshListData();
        super.onResume();
    }

    private void refreshListData() {
        List<GeoCache> checkpointList = new ArrayList<GeoCache>(checkpointManager.getCheckpoints());
        checkpointsAdapter.clear();
        if (!checkpointList.isEmpty()) {
            checkpointList.add(0, geoCache);
            for (GeoCache geoCache : checkpointList) {
                checkpointsAdapter.add(geoCache);
            }
        }
        updateNoCacheVisibility();
        checkpointsAdapter.notifyDataSetChanged();
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
        if (mActionMode == null) {
            mActionMode = startActionMode(mActionModeCallback);
        } else {
            mActionMode.invalidate();
        }
    }

    private void onDeleteCheckpoint(int position) {
        GeoCache checkpoint = checkpointsAdapter.getItem(position);
        checkpointManager.removeCheckpoint(checkpoint.getId());
        refreshListData();
    }

    private void updateNoCacheVisibility() {
        if (checkpointManager.getCheckpoints().isEmpty()) {
            tvNoCache.setVisibility(View.VISIBLE);
        } else {
            tvNoCache.setVisibility(View.GONE);
        }
    }

    private void onActivateCheckpoint(int position) {
        GeoCache checkpoint = checkpointsAdapter.getItem(position);
        checkpointManager.activateCheckpoint(checkpoint);
        finish();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.checkpoint_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.findItem(R.id.menu_checkpoint_delete).setVisible(getListView().getCheckedItemPosition() != 0);
            return true;
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_checkpoint_delete:
                    onDeleteCheckpoint(getListView().getCheckedItemPosition());
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_checkpoint_activate:
                    onActivateCheckpoint(getListView().getCheckedItemPosition());
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getListView().setItemChecked(getListView().getCheckedItemPosition(), false);
            mActionMode = null;
        }
    };
}
