package su.geocaching.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AboutActivity extends Activity {

	private GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("about", "on create");
		setContentView(R.layout.about);

		tracker = GoogleAnalyticsTracker.getInstance();

		tracker.start(getString(R.string.id_Google_Analytics), this);
		tracker.trackPageView(getString(R.string.about_activity_folder));
		tracker.dispatch();
	}

	public void onExitClick(View v) {
		tracker.stop();
		super.finish();
		Log.d("aboutActivity", "exit");
	}
}