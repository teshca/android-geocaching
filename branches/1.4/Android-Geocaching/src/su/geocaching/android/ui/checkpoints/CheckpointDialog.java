package su.geocaching.android.ui.checkpoints;

import android.widget.Button;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.controller.managers.ResourceManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CheckpointDialog extends Activity {
    private static final String CHECKPOINT_DIALOG_ACTIVITY_FOLDER = "/CheckpointDialog";
    private CheckpointManager checkpointManager;
    private int checkpointId, cacheId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoint_dialog);

        Intent intent = getIntent();
        checkpointId = intent.getIntExtra(NavigationManager.CACHE_ID, 0);
        cacheId = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId();
        checkpointManager = Controller.getInstance().getCheckpointManager(cacheId);
        ResourceManager rm = Controller.getInstance().getResourceManager();

        TextView coordinates = (TextView) findViewById(R.id.checkpointCoordinate);
        TextView status = (TextView) findViewById(R.id.tvCheckpointDialogStatus);

        Button checkpointDeleteButton = (Button) findViewById(R.id.checkpointDeleteButton);
        checkpointDeleteButton.setEnabled(checkpointId != cacheId);

        GeoCache cache;
        if (checkpointId == cacheId) {
            findViewById(R.id.checkpointDeleteButton).setEnabled(false);
            cache = Controller.getInstance().getDbManager().getCacheByID(cacheId);
        } else {
            cache = checkpointManager.getGeoCache(checkpointId);
        }
        coordinates.setText(CoordinateHelper.coordinateToString(cache.getLocationGeoPoint()));
        status.setText(rm.getGeoCacheStatus(cache));
        setTitle(cache.getName());

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(CHECKPOINT_DIALOG_ACTIVITY_FOLDER);
    }

    public void onActiveClick(View v) {
        if (cacheId == checkpointId) {
            checkpointManager.deactivateCheckpoints();
            Controller.getInstance().setSearchingGeoCache(Controller.getInstance().getDbManager().getCacheByID(cacheId));
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
