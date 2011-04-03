package su.geocaching.android.ui.searchmap.stepbystep;

import su.geocaching.android.controller.CheckpointManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.ui.AbstractCacheFolder;
import su.geocaching.android.ui.FavoritesFolder;
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
public class CheckpointsFolder extends AbstractCacheFolder implements OnItemClickListener {

    public static final String CACHE_ID = "cacheId";

    private static final String TAG = FavoritesFolder.class.getCanonicalName();
    private CheckpointManager checkpointManager;
    private AlertDialog removeAlert;
    private int cacheid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cacheid = getIntent().getIntExtra(CACHE_ID, 0);
        checkpointManager = Controller.getInstance().getCheckpointManager(cacheid);
        tvNoCache.setText(getString(R.string.checkpoint_folder_not_cache_in_db));
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
            tvNoCache.setVisibility(View.GONE);
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, createGeoCacheList(favoritesList), R.layout.row_in_favorit_rolder, keys, new int[] { R.id.favorite_list_image_button_type,
                    R.id.favorite_list_text_view_name, R.id.favorites_row_type_text, R.id.favorites_row_status_text });
            lvListShowCache.setAdapter(simpleAdapter);
        }
        super.onResume();
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
            case R.id.searchMainCache:
                checkpointManager.deactivateCheckpoints();
                lvListShowCache.setAdapter(null);
                onResume();
                break;
            case R.id.removeAllCheckpointMenu:
                removeAlert.show();
                break;
        }
        return true;
    }

    private void removeAllCheckpoints() {
        checkpointManager.clear();
        lvListShowCache.setAdapter(null);
        tvNoCache.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        UiHelper.startCheckpointDialog(this, favoritesList.get(arg2).getId());
    }
}
