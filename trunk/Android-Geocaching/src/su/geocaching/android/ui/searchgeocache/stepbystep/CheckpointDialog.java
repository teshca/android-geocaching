package su.geocaching.android.ui.searchgeocache.stepbystep;

import su.geocaching.android.ui.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CheckpointDialog extends Dialog {

	private Context context;

	public CheckpointDialog(Context context) {
		super(context);
		this.context = context;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] countries = context.getResources().getStringArray(R.array.checkpoints_menu);
		ListView list = new ListView(context);
		list.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, countries));
		setContentView(list);
	}

}
