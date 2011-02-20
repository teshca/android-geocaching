package su.geocaching.android.ui.searchgeocache.stepbystep;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GeoCacheLocationManager;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.GpsHelper;
import su.geocaching.android.utils.log.LogHelper;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;

public class SexagesimalInputActivity extends Activity {

	public static String LATITUDE = "latitude";
	public static String LONGITUDE = "longitude";

	private GeoCacheLocationManager locationManager;
	private EditText latDegrees, latMinutes, latmMinutes;
	private EditText lngDegrees, lngMinutes, lngmMinutes;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sexagestimal_input);

		latDegrees = (EditText) findViewById(R.id.sLatDegrees);
		latMinutes = (EditText) findViewById(R.id.sLatMinutes);
		latmMinutes = (EditText) findViewById(R.id.sLatmMinutes);
		lngDegrees = (EditText) findViewById(R.id.sLngDegrees);
		lngMinutes = (EditText) findViewById(R.id.sLngMinutes);
		lngmMinutes = (EditText) findViewById(R.id.sLngmMinutes);

		locationManager = Controller.getInstance().getLocationManager(this);

		if (locationManager.hasLocation()) {
			int[] sexagesimal;
			Location location = locationManager.getLastKnownLocation();
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			try {
				sexagesimal = GpsHelper.decimalToSexagesimal(lat);
				latDegrees.setText("" + sexagesimal[0], BufferType.EDITABLE);
				latMinutes.setText("" + sexagesimal[1], BufferType.EDITABLE);
				latmMinutes.setText("" + sexagesimal[2], BufferType.EDITABLE);

				sexagesimal = GpsHelper.decimalToSexagesimal(lng);
				lngDegrees.setText("" + sexagesimal[0], BufferType.EDITABLE);
				lngMinutes.setText("" + sexagesimal[1], BufferType.EDITABLE);
				lngmMinutes.setText("" + sexagesimal[2], BufferType.EDITABLE);
			} catch (Exception e) {
				Log.e("Geocaching.su", e.getMessage());
			}
		}
	}

	// TODO
	public void onEnterClick(View v) {
		int latitudeE6 = GpsHelper.sexagesimalToCoordinateE6(Integer.parseInt(latDegrees.getText().toString()), Integer.parseInt(latMinutes.getText().toString()),
				Integer.parseInt(latmMinutes.getText().toString()));
		int longitudeE6 = GpsHelper.sexagesimalToCoordinateE6(Integer.parseInt(lngDegrees.getText().toString()), Integer.parseInt(lngMinutes.getText().toString()),
				Integer.parseInt(lngmMinutes.getText().toString()));
		Intent intent = new Intent();
		LogHelper.d("", "" + latitudeE6);
		LogHelper.d("", "" + longitudeE6);
		intent.putExtra(LATITUDE, latitudeE6);
		intent.putExtra(LONGITUDE, longitudeE6);

		getParent().setResult(RESULT_OK, intent);
		finish();
	}
}
