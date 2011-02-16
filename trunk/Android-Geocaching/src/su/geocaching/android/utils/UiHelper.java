package su.geocaching.android.utils;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.DashboardActivity;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.ShowGeoCacheInfo;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

/**
 * @author Nikita Bumakov
 */
public class UiHelper {

	/**
	 * Invoke "home" action, returning to DashBoardActivity
	 */
	public static void goHome(Context context) {
		final Intent intent = new Intent(context, DashboardActivity.class);
		context.startActivity(intent);
	}

	public static void askTurnOnGps(final Activity context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(context.getString(R.string.ask_enable_gps_text)).setCancelable(false)
				.setPositiveButton(context.getString(R.string.ask_enable_gps_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent startGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						context.startActivity(startGPS);
						dialog.cancel();
					}
				}).setNegativeButton(context.getString(R.string.ask_enable_gps_no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// activity is MapActivity or Activity
						context.finish();
					}
				});
		AlertDialog turnOnGpsAlert = builder.create();
		turnOnGpsAlert.show();
	}

	/**
	 * Open GeoCache info activity
	 */
	public static void showGeoCacheInfo(Context context, GeoCache geoCache) {
		// Log.d(TAG, "Go to show geo cache activity");
		Intent intent = new Intent(context, ShowGeoCacheInfo.class);
		intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
		context.startActivity(intent);
	}

	/**
	 * Starts SearchGeoCacheMap activity and finish this
	 */
	public static void startMapView(Context context, GeoCache geoCache) {
		Intent intent = new Intent(context, SearchGeoCacheMap.class);
		intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
		context.startActivity(intent);
	}
}
