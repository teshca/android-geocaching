package su.geocaching.android.ui.searchmap.stepbystep;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import su.geocaching.android.ui.R;

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
        intent.putExtras(getIntent().getExtras());

        spec = tabHost.newTabSpec("tab1");
        spec.setIndicator(getString(R.string.sexagestimal_template));
        spec.setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, DecimalInputActivity.class);
        intent.putExtras(getIntent().getExtras());

        spec = tabHost.newTabSpec("tab2");
        spec.setIndicator(getString(R.string.decimal_template));
        spec.setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, AzimuthInputActivity.class);
        intent.putExtras(getIntent().getExtras());

        spec = tabHost.newTabSpec("tab3");
        spec.setIndicator(getString(R.string.azimuth_template));
        spec.setContent(intent);
        tabHost.addTab(spec);
    }
}
