package su.geocaching.android.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.DashboardActivity;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.InfoActivity;
import su.geocaching.android.ui.compass.CompassActivity;
import su.geocaching.android.ui.searchmap.SearchMapActivity;
import su.geocaching.android.ui.searchmap.stepbystep.CheckpointDialog;
import su.geocaching.android.ui.searchmap.stepbystep.CheckpointsFolder;
import su.geocaching.android.ui.searchmap.stepbystep.CreateCheckpointActivity;

/**
 * @author Nikita Bumakov
 */
public class UiHelper {

    public static final String CACHE_ID = "cache_id";

    /**
     * Invoke "home" action, returning to DashBoardActivity
     * 
     * @param context
     *            //TODO describe it
     */
    public static void goHome(Context context) {
        final Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
    }

    public static void askTurnOnGps(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.ask_enable_gps_text)).setCancelable(false).setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent startGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(startGPS);
                dialog.cancel();
            }
        }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
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
     * 
     * @param context
     *            //TODO describe it
     * @param geoCache
     *            //TODO describe it
     */
    public static void startGeoCacheInfo(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }

    /**
     * F Starts SearchMapActivity activity and finish this
     * 
     * @param context
     *            //TODO describe it
     * @param geoCache
     *            //TODO describe it
     */
    public static void startSearchMapActivity(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, SearchMapActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }
    
    public static void startCompassActivity(Context context) {
        Intent intent = new Intent(context, CompassActivity.class);      
        context.startActivity(intent);
    }
    
    public static void startStepByStep(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, CreateCheckpointActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }

    public static void startCheckpointsFolder(Context context, int cacheId) {
        Intent intent = new Intent(context, CheckpointsFolder.class);
        intent.putExtra(CheckpointsFolder.CACHE_ID, cacheId);
        context.startActivity(intent);
    }

    public static void startCheckpointDialog(Context context, int id) {
        Intent intent = new Intent(context, CheckpointDialog.class);
        intent.putExtra(CACHE_ID, id);
        context.startActivity(intent);
    }
}
