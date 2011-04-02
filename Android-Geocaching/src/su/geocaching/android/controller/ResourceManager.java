package su.geocaching.android.controller;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;

/**
 * Manager which can get access to application resources
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since March 2011
 */
public class ResourceManager {
    private final Context context;

    public ResourceManager(Context context) {
        this.context = context;

    }

    public Drawable getDrawable(int id) {
        return context.getResources().getDrawable(id);
    }

    public String getString(int id) {
        return context.getString(id);
    }

    public String getString(int id, Object... formatArgs) {
        return context.getString(id, formatArgs);
    }

    public CharSequence getText(int id) {
        return context.getText(id);
    }

    public Resources getResources() {
        return context.getResources();
    }

    /**
     * Return marker for map of input geoCache
     * 
     * @return Drawable for this geoCache depends on it's parameters
     */
    public Drawable getMarker(GeoCacheType type, GeoCacheStatus status) {
        int resId;
        if ((resId = getMarkerResId(type, status)) == -1) {
            return null;
        }
        return getMarker(resId);
    }

    /**
     * Return marker resource id of input geoCache
     * 
     * @param geoCache
     *            we want to draw on the map
     * @return Drawable for this geoCache depends on it's parameters
     */
    public int getMarkerResId(GeoCacheType type, GeoCacheStatus status) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.default_marker), true)) {

            switch (type) {
                case TRADITIONAL:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_cache_traditional_valid;
                        case NOT_VALID:
                            return R.drawable.ic_cache_traditional_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_cache_traditional_not_confirmed;
                    }
                    break;
                case VIRTUAL:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_cache_virtual_valid;
                        case NOT_VALID:
                            return R.drawable.ic_cache_virtual_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_cache_virtual_not_confirmed;
                    }
                    break;
                case STEP_BY_STEP:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_cache_stepbystep_valid;
                        case NOT_VALID:
                            return R.drawable.ic_cache_stepbystep_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_cache_stepbystep_not_confirmed;
                    }
                    break;
                case EXTREME:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_cache_extreme_valid;
                        case NOT_VALID:
                            return R.drawable.ic_cache_extreme_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_cache_extreme_not_confirmed;
                    }
                    break;
                case EVENT:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_cache_event_valid;
                        case NOT_VALID:
                            return R.drawable.ic_cache_event_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_cache_event_not_confirmed;
                    }
                    break;
                case GROUP:
                    return R.drawable.ic_cache_group;
                case CHECKPOINT:
                    return R.drawable.ic_cache_checkpoint;

            }

        } else {
            switch (type) {
                case TRADITIONAL:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_traditional_valid;
                        case NOT_VALID:
                            return R.drawable.ic_traditional_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_traditional_not_confirmed;
                    }
                    break;
                case VIRTUAL:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_virtual_valid;
                        case NOT_VALID:
                            return R.drawable.ic_virtual_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_virtual_not_confirmed;
                    }
                    break;
                case STEP_BY_STEP:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_step_by_step_valid;
                        case NOT_VALID:
                            return R.drawable.ic_step_by_step_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_step_by_step_not_confirmed;
                    }
                    break;
                case EXTREME:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_extrem_valid;
                        case NOT_VALID:
                            return R.drawable.ic_extrem_not_valid;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_extrem_not_confirmed;
                    }
                    break;
                case EVENT:
                    switch (status) {
                        case VALID:
                            return R.drawable.ic_cache_event_valid_second;
                        case NOT_VALID:
                            return R.drawable.ic_cache_event_not_valid_second;
                        case NOT_CONFIRMED:
                            return R.drawable.ic_cache_event_not_confirmed_second;
                    }
                    break;
                case GROUP:
                    return R.drawable.ic_cache_group_second;
                case CHECKPOINT:
                    return R.drawable.ic_cache_checkpoint;
            }
        }
        return -1;
    }

    /**
     * Set bounds to marker
     * 
     * @param resource
     *            id of marker
     * @return marker with set bounds
     */
    private Drawable getMarker(int resource) {
        Drawable cacheMarker = context.getResources().getDrawable(resource);
        cacheMarker.setBounds(-cacheMarker.getMinimumWidth() / 2, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth() / 2, 0);
        return cacheMarker;
    }

    /**
     * @param geoCache
     *            input geo cache
     * @return localized name of geocache status
     */
    public String getGeoCacheStatus(GeoCache geoCache) {
        switch (geoCache.getStatus()) {
            case VALID:
                return getString(R.string.status_geocache_valid);
            case NOT_VALID:
                return getString(R.string.status_geocache_no_valid);
            case NOT_CONFIRMED:
                return getString(R.string.status_geocache_no_confirmed);
            case ACTIVE_CHECKPOINT:
                return getString(R.string.status_geocache_active_checkpoint);
            case NOT_ACTIVE_CHECKPOINT:
                return getString(R.string.status_geocache_not_active_checkpoint);
            default:
                return getString(R.string.status_geocache_unknown);
                // what a terrible failure?
        }
    }

    /**
     * @param geoCache
     *            input geo cache
     * @return localized name of geocache type
     */
    public String getGeoCacheType(GeoCache geoCache) {
        switch (geoCache.getType()) {
            case TRADITIONAL:
                return getString(R.string.type_geocache_traditional);
            case VIRTUAL:
                return getString(R.string.type_geocache_virtua);
            case EVENT:
                return getString(R.string.type_geocache_event);
            case EXTREME:
                return getString(R.string.type_geocache_extreme);
            case STEP_BY_STEP:
                return getString(R.string.type_geocache_step_by_step);
            case CHECKPOINT:
                return getString(R.string.type_geocache_checkpoint);
            default:
                return getString(R.string.status_geocache_unknown);
                // what a terrible failure?
        }
    }
}
