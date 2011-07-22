package su.geocaching.android.ui.compass;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.CheckBox;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.UserLocationManager;
import su.geocaching.android.ui.R;

public class CompassPreferenceActivity extends PreferenceActivity {
    private static final String COMPASS_PREFERENCE_ACTIVITY = "/CompassPreferenceActivity"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(COMPASS_PREFERENCE_ACTIVITY);
        addPreferencesFromResource(R.xml.compass_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void onOdometerClick(View v){
        UserLocationManager lm = Controller.getInstance().getLocationManager();
        lm.refreshOdometer();
        if(((CheckBox) v).isChecked()){
            lm.setUpdatingOdometer(true);
        }
        else{
            lm.setUpdatingOdometer(false);
        }
    }
}
