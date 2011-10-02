package su.geocaching.android.controller.managers;

import java.util.List;

import android.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.AndroidCharacter;
import android.view.LayoutInflater;
import android.view.View;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.*;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.checkpoints.CheckpointDialog;
import su.geocaching.android.ui.checkpoints.CheckpointsFolder;
import su.geocaching.android.ui.checkpoints.CreateCheckpointActivity;
import su.geocaching.android.ui.compass.CompassActivity;
import su.geocaching.android.ui.info.CacheNotesActivity;
import su.geocaching.android.ui.info.InfoActivity;
import su.geocaching.android.ui.preferences.DashboardPreferenceActivity;
import su.geocaching.android.ui.searchmap.SearchMapActivity;
import su.geocaching.android.ui.selectmap.SelectMapActivity;

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

    public static void startSelectMapActivity(Context context)
    {
        Intent intent = new Intent(context, SelectMapActivity.class);
        context.startActivity(intent);
    }

    public static void startPreferencesActivity(Context context)
    {
        Intent intent = new Intent(context, DashboardPreferenceActivity.class);
        context.startActivity(intent);
    }

    public static void startFavoritesActivity(Context context)
    {
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
     * Open SearchMapActivity activity and finish this
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
        Intent intent = new Intent(context, CheckpointsFolder.class);
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

    public static void displayTurnOnGpsDialog(final Activity context) {
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch("/EnableGpsDialog");

        new AlertDialog.Builder(context)
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
            .create()
            .show();
    }

     public static void displayTurnOnConnectionDialog(final Activity context) {
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch("/EnableConnectionDialog");

        new AlertDialog.Builder(context)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(context.getString(R.string.connection_problem_dialog_title))
            .setMessage(context.getString(R.string.connection_problem_dialog_message))
            .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    context.startActivity(intent);
                    dialog.cancel();
                }
            })
            .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    context.finish();
                    dialog.cancel();
                }
            })
            .create()
            .show();
    }

    /**
     * Run external GpsStatus & toolbox application
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
}
