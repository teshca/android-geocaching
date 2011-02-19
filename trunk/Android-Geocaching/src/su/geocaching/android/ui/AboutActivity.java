package su.geocaching.android.ui;

import su.geocaching.android.ui.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
public class AboutActivity extends Activity implements OnClickListener {

    private Button exitButton;
    private GoogleAnalyticsTracker tracker;
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d("about", "on create");
	setContentView(R.layout.about);
	exitButton = (Button) findViewById(R.id.about_exit_button);
	exitButton.setOnClickListener(this);
	tracker = GoogleAnalyticsTracker.getInstance();
	tracker.start("UA-20327116-3", this);
	tracker.trackPageView("/aboutActivity");
    }

    @Override
    public void onClick(View v) {
	if (v.equals(exitButton)) {
		tracker.stop();
	    super.finish();
	    Log.d("aboutActivity", "exit");
	}
    }
}