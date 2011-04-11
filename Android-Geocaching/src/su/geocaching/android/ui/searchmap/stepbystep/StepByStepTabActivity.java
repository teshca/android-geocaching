package su.geocaching.android.ui.searchmap.stepbystep;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.ui.R;

public class StepByStepTabActivity extends TabActivity {
    
    private static final String STEP_BY_STEP_TAB_ACTIVITY_FOLDER = "/StepByStepTabActivity"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step_by_step);
        
        setTitle(R.string.converter);
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
        
        intent = new Intent(this, SexagestimalSecondsInputActivity.class);
        intent.putExtras(getIntent().getExtras());

        spec = tabHost.newTabSpec("tab2");
        spec.setIndicator(getString(R.string.sexagestimal_seconds__template));
        spec.setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, DecimalInputActivity.class);
        intent.putExtras(getIntent().getExtras());

        spec = tabHost.newTabSpec("tab3");
        spec.setIndicator(getString(R.string.decimal_template));
        spec.setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, AzimuthInputActivity.class);
        intent.putExtras(getIntent().getExtras());

        spec = tabHost.newTabSpec("tab4");
        spec.setIndicator(getString(R.string.azimuth_template));
        spec.setContent(intent);
        tabHost.addTab(spec);
        
        Controller.getInstance().getGoogleAnalyticsManager(this).trackPageView(STEP_BY_STEP_TAB_ACTIVITY_FOLDER);
    }
    
    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }
}
