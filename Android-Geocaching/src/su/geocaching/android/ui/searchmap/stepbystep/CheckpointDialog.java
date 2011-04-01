package su.geocaching.android.ui.searchmap.stepbystep;

import su.geocaching.android.controller.CheckpointManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsHelper;
import su.geocaching.android.controller.ResourceManager;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CheckpointDialog extends Activity {

    private CheckpointManager checkpointManager;
    private int checkpointId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoint_dialog);

        Intent intent = getIntent();
        checkpointId = intent.getIntExtra(UiHelper.CACHE_ID, 0);
        checkpointManager = Controller.getInstance().getCheckpointManager(Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId());
        ResourceManager rm = Controller.getInstance().getResourceManager();

        TextView coordinates = (TextView) findViewById(R.id.checkpointCoordinate);
        TextView status = (TextView) findViewById(R.id.tvCheckpointDialogStatus);

        coordinates.setText(GpsHelper.coordinateToString(checkpointManager.getGeoCache(checkpointId).getLocationGeoPoint()));
        status.setText(rm.getGeoCacheStatus(checkpointManager.getGeoCache(checkpointId)));

        setTitle(getString(R.string.checkpoint_dialog_title)+" " + checkpointId);
    }

    public void onActiveClick(View v) {
        checkpointManager.setActiveItem(checkpointId);
        finish();
    }

    public void onDeleteClick(View v) {
        checkpointManager.removeCheckpoint(checkpointId);
        finish();
    }
}
