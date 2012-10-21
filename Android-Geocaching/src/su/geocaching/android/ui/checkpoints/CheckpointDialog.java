package su.geocaching.android.ui.checkpoints;

import android.widget.Button;
import com.actionbarsherlock.app.SherlockActivity;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CheckpointDialog extends SherlockActivity {
    private static final String CHECKPOINT_DIALOG_ACTIVITY_FOLDER = "/CheckpointDialog";
    private CheckpointManager checkpointManager;
    private GeoCache checkpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoint_dialog);

        checkpoint = (GeoCache) getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        // TODO: Pass geocache though intent extra
        int currentCacheId = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId();
        checkpointManager = Controller.getInstance().getCheckpointManager(currentCacheId);

        TextView coordinates = (TextView) findViewById(R.id.checkpointCoordinate);
        TextView status = (TextView) findViewById(R.id.tvCheckpointDialogStatus);

        Button checkpointDeleteButton = (Button) findViewById(R.id.checkpointDeleteButton);
        checkpointDeleteButton.setEnabled(checkpoint.getType() == GeoCacheType.CHECKPOINT);

        coordinates.setText(CoordinateHelper.coordinateToString(checkpoint.getLocationGeoPoint()));
        status.setText(Controller.getInstance().getResourceManager().getGeoCacheStatus(checkpoint));
        setTitle(checkpoint.getName());

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(CHECKPOINT_DIALOG_ACTIVITY_FOLDER);
    }

    public void onActiveClick(View v) {
        checkpointManager.activateCheckpoint(checkpoint);
        finish();
    }

    public void onDeleteClick(View v) {
        checkpointManager.removeCheckpoint(checkpoint.getId());
        finish();
    }
}
