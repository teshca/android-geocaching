package su.geocaching.android.ui;

import su.geocaching.android.ui.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutActivity extends Activity implements OnClickListener {

    private Button exitButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d("about", "on create");
	setContentView(R.layout.about);
	exitButton = (Button) findViewById(R.id.about_exit_button);
	exitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
	if (v.equals(exitButton)) {
	    super.finish();
	    Log.d("aboutActivity", "exit");
	}
    }
}