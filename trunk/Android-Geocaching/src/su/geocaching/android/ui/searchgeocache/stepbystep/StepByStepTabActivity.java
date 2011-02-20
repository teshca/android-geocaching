package su.geocaching.android.ui.searchgeocache.stepbystep;

import su.geocaching.android.ui.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class StepByStepTabActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.step_by_step);

		// Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent(this, SexagesimalInputActivity.class);

		spec = tabHost.newTabSpec("tab1");
		spec.setIndicator(getString(R.string.sexagesimal));
		spec.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent(this, DecimalInputActivity.class);

		spec = tabHost.newTabSpec("tab2");
		spec.setIndicator(getString(R.string.decimal));
		spec.setContent(intent);
		tabHost.addTab(spec);
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// setResult(resultCode, data);
	// LogHelper.d("", "DATA == null" +(data==null));
	// }
}
