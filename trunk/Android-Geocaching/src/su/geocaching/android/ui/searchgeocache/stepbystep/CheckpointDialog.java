package su.geocaching.android.ui.searchgeocache.stepbystep;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.SearchCacheOverlay;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.maps.MapView;

public class CheckpointDialog extends Dialog {

	private Button active, delete;

	private SearchCacheOverlay searchOverlay;
	private MapView map;
	private int index;

	public CheckpointDialog(Context context, int index, SearchCacheOverlay searchOverlay, MapView map) {
		super(context);

		String title = Controller.getInstance().getResourceManager().getString(R.string.checkpoint_dialog_title) + " " + index;
		setTitle(title);
		// this.context = context;
		this.index = index;
		this.searchOverlay = searchOverlay;
		this.map = map;		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkpoint_dialog);
		
		active = (Button) findViewById(R.id.checkpointActiveButton);
		delete = (Button) findViewById(R.id.checkpointDeleteButton);
		ButtonClickListener clickListener = new ButtonClickListener();
		active.setOnClickListener(clickListener);
		delete.setOnClickListener(clickListener);
	}

	class ButtonClickListener implements android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.equals(active)) {
				Controller.getInstance().setSearchingGeoCache(searchOverlay.getGeoCache(index));
			} else if (v.equals(delete)) {
				searchOverlay.removeOverlayItem(index);
			}
			map.invalidate();
			dismiss();
		}
	}

}
