package su.geocaching.android.ui.searchgeocache.stepbystep;

import su.geocaching.android.ui.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class StepByStepTabActivity extends TabActivity {

	public static String LATITUDE = "latitude";
	public static String LONGITUDE = "longitude";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.step_by_step);		
		// Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent(this, SexagesimalInputActivity.class);
		intent.putExtras(getIntent().getExtras());

		spec = tabHost.newTabSpec("tab1");
		spec.setIndicator(getString(R.string.sexagesimal));
		spec.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent(this, DecimalInputActivity.class);
		intent.putExtras(getIntent().getExtras());

		spec = tabHost.newTabSpec("tab2");
		spec.setIndicator(getString(R.string.decimal));
		spec.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent(this, AzimutInputActivity.class);
		intent.putExtras(getIntent().getExtras());

		spec = tabHost.newTabSpec("tab3");
		spec.setIndicator(getString(R.string.on_azimuth));
		spec.setContent(intent);
		tabHost.addTab(spec);
	}
}
