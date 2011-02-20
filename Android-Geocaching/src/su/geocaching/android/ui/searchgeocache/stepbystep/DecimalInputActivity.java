package su.geocaching.android.ui.searchgeocache.stepbystep;

import su.geocaching.android.ui.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class DecimalInputActivity extends Activity {


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decimal_input);		
	}
	
	public void onEnterClick(View v){
		Log.d("Geocaching.su", "onEnterClick");
	}
}
