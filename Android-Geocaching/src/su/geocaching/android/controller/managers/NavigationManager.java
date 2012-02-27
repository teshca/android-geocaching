package su.geocaching.android.controller.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.DashboardActivity;
import su.geocaching.android.ui.FavoritesFolderActivity;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.checkpoints.CheckpointDialog;
import su.geocaching.android.ui.checkpoints.CheckpointsFolderActivity;
import su.geocaching.android.ui.checkpoints.CreateCheckpointActivity;
import su.geocaching.android.ui.compass.CompassActivity;
import su.geocaching.android.ui.info.CacheNotesActivity;
import su.geocaching.android.ui.info.InfoActivity;
import su.geocaching.android.ui.preferences.DashboardPreferenceActivity;
import su.geocaching.android.ui.searchmap.SearchMapActivity;
import su.geocaching.android.ui.selectmap.SelectMapActivity;

import java.util.List;

/**
 * @author Nikita Bumakov
 */
public class NavigationManager {
    public static final String CACHE_ID = "cache_id";

    /**
     * Invoke "home" action, returning to DashBoardActivity
     */
    public static void startDashboardActivity(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
    }

    public static void startSelectMapActivity(Context context){
        Intent intent = new Intent(context, SelectMapActivity.class);
        context.startActivity(intent);
    }

    public static void startPreferencesActivity(Context context){
        Intent intent = new Intent(context, DashboardPreferenceActivity.class);
        context.startActivity(intent);
    }

    public static void startFavoritesActivity(Context context){
        Intent intent = new Intent(context, FavoritesFolderActivity.class);
        context.startActivity(intent);
    }

    /**
     * Open GeoCache info activity
     */
    public static void startInfoActivity(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }

    /**
     * Open SearchMapActivity activity
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

    public static void startCreateCheckpointActivity(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, CreateCheckpointActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }

    public static void startCheckpointsFolder(Context context, int cacheId) {
        Intent intent = new Intent(context, CheckpointsFolderActivity.class);
        intent.putExtra(CACHE_ID, cacheId);
        context.startActivity(intent);
    }

    public static void startCheckpointDialog(Context context, int cacheId) {
        Intent intent = new Intent(context, CheckpointDialog.class);
        intent.putExtra(CACHE_ID, cacheId);
        context.startActivity(intent);
    }

    public static void startPictureViewer(Context context, Uri photoUri) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(photoUri, "image/*");
        context.startActivity(intent);
    }

    /**
     * Open CacheNotesActivity activity
     */
    public static void startNotesActivity(Context context, int cacheId) {
        Intent intent = new Intent(context, CacheNotesActivity.class);
        intent.putExtra(CACHE_ID, cacheId);
        context.startActivity(intent);
    }

    public static Dialog createTurnOnGpsDialog(final Activity context) {
        return new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.ask_enable_gps_text))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        context.finish();
                    }
                })
                .create();
    }

    /**
     * Run external GpsStatus & toolbox application
     * @param context parent context
     */
    public static void startExternalGpsStatusActivity(Context context) {
        Intent intent = new Intent("com.eclipsim.gpsstatus.VIEW");

        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        if (list.size() > 0) {
            // Application installed
            Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/GpsStatus/1");
            context.startActivity(intent);
        } else {
            // Application isn't installed
            Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/GpsStatus/0");
        }
    }


    private static Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=su.geocaching.android.ui"));
    /**
     * Run Android Market application
     * @param context parent context
     */
    public static void startAndroidMarketActivity(Context context) {
        if (isAndroidMarketAvailable(context)) {
            Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/AndroidMarket");
            context.startActivity(marketIntent);
        }
    }
    /**
     * Check if Android Market application is available
     * @param context parent context
     */
    public static boolean isAndroidMarketAvailable(Context context) {
        return context.getPackageManager().queryIntentActivities(marketIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).size() > 0;
    }
}
