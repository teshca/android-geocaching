package su.geocaching.android.ui.compass;

import su.geocaching.android.ui.R;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CompassPreference extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.compass_preference);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // for portrait
	}
}
