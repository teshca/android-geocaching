package su.geocaching.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	Main menu activity stub
 */
public class MainMenu extends Activity implements OnClickListener {
    protected final static String TAG = "su.geocaching.android";

    private Button btSearchGeocache;
    private Button btSelectGeocache;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(TAG, "MainMenu Activity creating:");
	setContentView(R.layout.main_menu);

	btSearchGeocache = (Button) findViewById(R.id.btSearchGeocache);
	btSelectGeocache = (Button) findViewById(R.id.btSelectGeocache);
	
	btSearchGeocache.setOnClickListener(this);
	btSelectGeocache.setOnClickListener(this);
	Log.d(TAG, "MainMenu Activity was created.");
    }

    /**
     * Method which handle onClick.
     */
    @Override
    public void onClick(View v) {
	Log.d(TAG, "on click:");
	if (v.equals(btSearchGeocache)) {
	    Log.d(TAG, "	btSearchGeocache");
	    // TODO: connect with Search Geocache Activity
	} else if (v.equals(btSelectGeocache)) {
	    Log.d(TAG, "	btSelectGeocache");
	    // TODO: connect with Select Geocache Activity
	} else {
	    Log.d(TAG, "	unknown view was clicked");
	    // not implemented yet
	}
    }
}