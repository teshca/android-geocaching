package su.geocaching.android.ui.searchgeocache.stepbystep;

import su.geocaching.android.controller.CheckpointManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.GpsHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CheckpointDialog extends Activity {

    private Button active, delete;
    private TextView coordinates;

    private CheckpointManager checkpointManager;
    private int checkpointId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoint_dialog);      

        coordinates = (TextView) findViewById(R.id.checkpointCoordinate);       
        active = (Button) findViewById(R.id.checkpointActiveButton);
        delete = (Button) findViewById(R.id.checkpointDeleteButton);
        ButtonClickListener clickListener = new ButtonClickListener();
        active.setOnClickListener(clickListener);
        delete.setOnClickListener(clickListener);
        
        Intent intent = getIntent();
        checkpointId = intent.getIntExtra(UiHelper.CACHE_ID, 0);
        checkpointManager = Controller.getInstance().getCheckpointManager(Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId());
        coordinates.setText(GpsHelper.coordinateToString(checkpointManager.getGeoCache(checkpointId).getLocationGeoPoint()));
    }

    private class ButtonClickListener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.equals(active)) {
                checkpointManager.setActiveItem(checkpointId);
            } else if (v.equals(delete)) {
                checkpointManager.removeCheckpoint(checkpointId);           
            }          
          finish();
        }
    }
}
