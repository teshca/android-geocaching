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
import su.geocaching.android.ui.info.AdvancedInfoActivity;
import su.geocaching.android.ui.info.CacheNotesActivity;
import su.geocaching.android.ui.preferences.DashboardPreferenceActivity;
import su.geocaching.android.ui.searchmap.SearchMapActivity;
import su.geocaching.android.ui.selectmap.SelectMapActivity;

import java.util.List;
import java.util.Locale;

/**
 * @author Nikita Bumakov
 */
public class NavigationManager {

    /**
     * Invoke "home" action, returning to DashBoardActivity
     */
    public static void startDashboardActivity(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        Intent intent = new Intent(context, AdvancedInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }    

    /**
     * Open SearchMapActivity activity
     */
    public static void startSearchMapActivity(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, SearchMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }

    public static void startCompassActivity(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, CompassActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }

    public static void startCreateCheckpointActivity(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, CreateCheckpointActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }

    public static void startCheckpointsFolder(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, CheckpointsFolderActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
        context.startActivity(intent);
    }

    public static void startCheckpointDialog(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, CheckpointDialog.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
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
    public static void startNotesActivity(Context context, GeoCache geoCache) {
        Intent intent = new Intent(context, CacheNotesActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), geoCache);
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
            startAndroidMarketActivity(context, gpsStatusApplicationId);
            Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/GpsStatus/0");
        }
    }
    
    /**
     * Run external Map application
     * @param context parent context
     */
    public static void startExternalMap(Context context, double latitude, double longitude, int zoom) {
        final String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=%d", latitude, longitude, zoom);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));

        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        if (list.size() > 0) {
            // Application installed
            Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/ExtternalMap/1");
            context.startActivity(intent);
        } else {
            // Application isn't installed
            startAndroidMarketActivity(context, yandexMapsApplicationId);
            Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/ExtternalMap/0");
        }
    }

    private static String geocachingApplicationId = "su.geocaching.android.ui";
    private static String gpsStatusApplicationId = "com.eclipsim.gpsstatus2";
    private static String yandexMapsApplicationId = "ru.yandex.yandexmaps";

    /**
     * Run Android Market application
     * @param context parent context
     */
    public static void startGeocachingGooglePlayActivity(Context context) {
        startAndroidMarketActivity(context, geocachingApplicationId);
    }

    /**
     * Run Android Market application
     * @param context parent context
     */
    private static void startAndroidMarketActivity(Context context, String ApplicationId) {
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%s", ApplicationId)));
        if (isApplicationAvailable(context, marketIntent)) {
            Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch(String.format("/AndroidMarket/%s", ApplicationId));
            context.startActivity(marketIntent);
        }
    }
    /**
     * Check if Android Market application is available
     * @param context parent context
     */
    private static boolean isApplicationAvailable(Context context, Intent marketIntent) {
        return context.getPackageManager().queryIntentActivities(marketIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).size() > 0;
    }

    /**
     * Check if Android Market application is available
     * @param context parent context
     */
    public static boolean isAndroidMarketAvailable(Context context) {
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%s", geocachingApplicationId)));
        return isApplicationAvailable(context, marketIntent);
    }
    
    /**
     * Run driving directions application.
     * Prefer Yandex maps if exists
     * @param context parent context
     */
    public static void startExternalDrivingDirrections(Context context, double sourceLat, double sourceLng, double destinationLat, double destinationLng) {
        Intent intent = new Intent("ru.yandex.yandexmaps.action.BUILD_ROUTE_ON_MAP");
        intent.putExtra("lat_from", sourceLat);
        intent.putExtra("lon_from", sourceLng);
        intent.putExtra("lat_to", destinationLat);
        intent.putExtra("lon_to", destinationLng);       
        
        if (isApplicationAvailable(context, intent)) {
            // Application installed
            Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/ExternalDrivingDirrections/Yandex");
            context.startActivity(intent);
        } else {
            // Yandex maps is not found
            final String uri = String.format(
                    Locale.ENGLISH,
                    "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f&ie=UTF8&om=0&output=kml",
                    sourceLat, sourceLng, destinationLat, destinationLng);
            intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));

            if (isApplicationAvailable(context, intent)) {
                // Application installed
                Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/ExternalDrivingDirrections/1");
                context.startActivity(intent);
            } else {
                // Application isn't installed
                startAndroidMarketActivity(context, yandexMapsApplicationId);
                Controller.getInstance().getGoogleAnalyticsManager().trackExternalActivityLaunch("/ExternalDrivingDirrections/0");
            }                    
        }                
    }
}
