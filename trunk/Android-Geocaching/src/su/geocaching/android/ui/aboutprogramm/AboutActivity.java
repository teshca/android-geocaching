package su.geocaching.android.ui.aboutprogramm;

import su.geocaching.android.ui.R;
import su.geocaching.android.ui.R.layout;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class AboutActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d("about", "on create");
	setContentView(R.layout.about);
    }

}