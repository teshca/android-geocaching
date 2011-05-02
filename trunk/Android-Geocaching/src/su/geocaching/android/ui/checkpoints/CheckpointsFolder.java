package su.geocaching.android.ui.checkpoints;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.AbstractGeoCacheFolderActivity;
import su.geocaching.android.ui.FavoritesFolderActivity;
import su.geocaching.android.ui.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

/**
 * Class for create ListActivity with checkpoints caches
 */
public class CheckpointsFolder extends AbstractGeoCacheFolderActivity implements OnItemClickListener {

    public static final String CACHE_ID = "cacheId";

    private static final String TAG = FavoritesFolderActivity.class.getCanonicalName();
    private CheckpointManager checkpointManager;
    private AlertDialog removeAlert;
    private int cacheid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cacheid = getIntent().getIntExtra(CACHE_ID, 0);
        checkpointManager = Controller.getInstance().getCheckpointManager(cacheid);
        tvNoCache.setText(getString(R.string.checkpoint_list_not_cache_in_db));
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
        favoritesList = dbm.getCheckpointsArrayById(cacheid);
        if (favoritesList.isEmpty()) {
            tvNoCache.setVisibility(View.VISIBLE);
            lvListShowCache.setAdapter(null);
            LogManager.d(TAG, "checkpoints DB empty");
        } else {
            favoritesList.add(0, Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache());
            tvNoCache.setVisibility(View.GONE);
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, createGeoCacheList(favoritesList), R.layout.favorites_row, keys, new int[] { R.id.favorite_list_image_button_type,
                    R.id.favorite_list_text_view_name, R.id.favorites_row_type_text, R.id.favorites_row_status_text });
            lvListShowCache.setAdapter(simpleAdapter);
        }
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (favoritesList.size() == 0) {
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
                UiHelper.startStepByStep(this, Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache());
                break;
            case R.id.removeAllCheckpointMenu:
                removeAlert.show();
                break;
        }
        return true;
    }

    private void removeAllCheckpoints() {
        favoritesList.clear();
        checkpointManager.clear();
        lvListShowCache.setAdapter(null);
        tvNoCache.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        UiHelper.startCheckpointDialog(this, favoritesList.get(arg2).getId());
    }
}
