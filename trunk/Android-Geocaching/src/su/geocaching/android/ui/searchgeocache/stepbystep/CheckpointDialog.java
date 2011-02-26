package su.geocaching.android.ui.searchgeocache.stepbystep;

import com.google.android.maps.MapView;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.SearchCacheOverlay;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CheckpointDialog extends Dialog {

	private Context context;
	private SearchCacheOverlay searchOverlay;
	private MapView map;
	private int index;

	public CheckpointDialog(Context context, int index, SearchCacheOverlay searchOverlay, MapView map) {
		super(context);

		String title = context.getString(R.string.checkpoint_dialog_title) + " " + index;
		setTitle(title);
		this.context = context;
		this.index = index;
		this.searchOverlay = searchOverlay;
		this.map = map;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] items = context.getResources().getStringArray(R.array.checkpoints_menu);
		ListView list = new ListView(context);
		list.setOnItemClickListener(new ClickListener());

		list.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items));
		setContentView(list);
	}

	class ClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
			switch (position) {
			case 0:
				Controller.getInstance().setSearchingGeoCache(searchOverlay.getGeoCache(index));
				break;
			case 1:
				searchOverlay.removeOverlayItem(index);
				break;
			case 2:
				searchOverlay.clear();
				break;

			default:
				break;
			}
			map.invalidate();
			dismiss();
		}
	}

}
