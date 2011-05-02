package su.geocaching.android.ui.checkpoints;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.CoordinateHelper;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.ResourceManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CheckpointDialog extends Activity {
    private static final String CHECKPOINT_DIALOG_ACTIVITY_FOLDER ="/CheckpointDialog";
    private CheckpointManager checkpointManager;
    private int checkpointId, cacheId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoint_dialog);

        Intent intent = getIntent();
        checkpointId = intent.getIntExtra(UiHelper.CACHE_ID, 0);
        cacheId = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId();
        checkpointManager = Controller.getInstance().getCheckpointManager(cacheId);
        ResourceManager rm = Controller.getInstance().getResourceManager();

        TextView coordinates = (TextView) findViewById(R.id.checkpointCoordinate);
        TextView status = (TextView) findViewById(R.id.tvCheckpointDialogStatus);

        if (checkpointId == cacheId) {
            findViewById(R.id.checkpointDeleteButton).setEnabled(false);
            GeoCache cache = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache();
            coordinates.setText(CoordinateHelper.coordinateToString(cache.getLocationGeoPoint()));
            status.setText(rm.getGeoCacheStatus(cache));
            setTitle(cache.getName());
        }
        else {
            coordinates.setText(CoordinateHelper.coordinateToString(checkpointManager.getGeoCache(checkpointId).getLocationGeoPoint()));
            status.setText(rm.getGeoCacheStatus(checkpointManager.getGeoCache(checkpointId)));
            setTitle(String.format("%s %d", getString(R.string.checkpoint_dialog_title), checkpointId));
        }
        
        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(CHECKPOINT_DIALOG_ACTIVITY_FOLDER);
    }

    public void onActiveClick(View v) {
        if (cacheId == checkpointId) {
            checkpointManager.deactivateCheckpoints();
        } else {
            checkpointManager.setActiveItem(checkpointId);
        }
        finish();
    }

    public void onDeleteClick(View v) {
        checkpointManager.removeCheckpoint(checkpointId);
        finish();
    }
}
