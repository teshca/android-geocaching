package su.geocaching.android.controller.managers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;

/**
 * Manager which can get access to application resources
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since March 2011
 */
public class ResourceManager {
    private final Context context;

    private enum IconType {
        DEFAULT, CUSTOM
    }

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
    public Drawable getCacheMarker(GeoCacheType type, GeoCacheStatus status) {
        int resId;
        if ((resId = getMarkerResId(type, status)) == -1) {
            return null;
        }
        return getMarker(resId);
    }

    public Drawable getUserLocationMarker()
    {
        Drawable marker = getDrawable(R.drawable.ic_my_location);
        marker.setBounds(-marker.getMinimumWidth() / 2, -marker.getMinimumHeight() + 20, marker.getMinimumWidth() / 2, 20);
        return marker;
    }

    /**
     * Return marker resource id of input geoCache
     * 
     * @return Drawable for this geoCache depends on it's parameters
     */
    public int getMarkerResId(GeoCacheType type, GeoCacheStatus status) {
        PreferencesManager manager = Controller.getInstance().getPreferencesManager();
        IconType iconType = IconType.valueOf(manager.getIconType());

        switch (iconType) {
            case CUSTOM:
                switch (type) {
                    case TRADITIONAL:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_custom_traditional_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_custom_traditional_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_custom_traditional_not_confirmed;
                        }
                        break;
                    case STEP_BY_STEP_TRADITIONAL:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_custom_step_by_step_traditional_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_custom_step_by_step_traditional_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_custom_step_by_step_traditional_not_confirmed;
                        }
                    case VIRTUAL:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_custom_virtual_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_custom_virtual_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_custom_virtual_not_confirmed;
                        }
                        break;
                    case EVENT:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_custom_event_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_custom_event_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_custom_event_not_confirmed;
                        }
                        break;
                    case WEBCAM:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_custom_virtual_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_custom_virtual_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_custom_virtual_not_confirmed;
                        }
                        break;
                    case EXTREME:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_custom_extreme_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_custom_extreme_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_custom_extreme_not_confirmed;
                        }
                        break;
                    case STEP_BY_STEP_VIRTUAL:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_custom_step_by_step_virtual_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_custom_step_by_step_virtual_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_custom_step_by_step_virtual_not_confirmed;
                        }
                        break;
                    case CONTEST:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_custom_competition_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_custom_competition_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_custom_competition_not_confirmed;
                        }
                        break;
                    case GROUP:
                        return R.drawable.ic_cache_custom_group;
                    case CHECKPOINT:
                        switch (status) {
                            case ACTIVE_CHECKPOINT:
                                return R.drawable.ic_cache_checkpoint_active;
                            case NOT_ACTIVE_CHECKPOINT:
                                return R.drawable.ic_cache_checkpoint;
                            default:
                                return R.drawable.ic_cache_checkpoint;
                        }
                }
                break;

            default:
                switch (type) {
                    case TRADITIONAL:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_default_traditional_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_default_traditional_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_default_traditional_not_confirmed;
                        }
                        break;

                    case STEP_BY_STEP_TRADITIONAL:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_default_stepbystep_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_default_stepbystep_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_default_stepbystep_not_confirmed;
                        }
                        break;
                    case VIRTUAL:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_default_virtual_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_default_virtual_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_default_virtual_not_confirmed;
                        }
                        break;
                    case EVENT:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_default_event_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_default_event_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_default_event_not_confirmed;
                        }
                        break;
                    case WEBCAM:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_default_virtual_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_default_virtual_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_default_virtual_not_confirmed;
                        }
                        break;
                    case EXTREME:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_default_extreme_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_default_extreme_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_default_extreme_not_confirmed;
                        }
                        break;
                    case STEP_BY_STEP_VIRTUAL:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_default_virtual_stepbystep_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_default_virtual_stepbystep_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_default_virtual_stepbystep_not_confirmed;
                        }
                        break;
                    case CONTEST:
                        switch (status) {
                            case VALID:
                                return R.drawable.ic_cache_default_competition_valid;
                            case NOT_VALID:
                                return R.drawable.ic_cache_default_competition_not_valid;
                            case NOT_CONFIRMED:
                                return R.drawable.ic_cache_default_competition_not_confirmed;
                        }
                        break;
                    case GROUP:
                        return R.drawable.ic_cache_default_group;
                    case CHECKPOINT:
                        switch (status) {
                            case ACTIVE_CHECKPOINT:
                                return R.drawable.ic_cache_checkpoint_active;
                            case NOT_ACTIVE_CHECKPOINT:
                                return R.drawable.ic_cache_checkpoint;
                            default:
                                return R.drawable.ic_cache_checkpoint;
                        }
                }
                break;
        }
        return -1;
    }

    /**
     * Set bounds to marker
     * 
     * @param resourceId
     *            id of marker
     * @return marker with set bounds
     */
    private Drawable getMarker(int resourceId) {
        Drawable cacheMarker = getDrawable(resourceId);
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
            case STEP_BY_STEP_TRADITIONAL:
                return getString(R.string.type_geocache_step_by_step);
            case STEP_BY_STEP_VIRTUAL:
                return getString(R.string.type_geocache_step_by_step_virtual);
            case VIRTUAL:
                return getString(R.string.type_geocache_virtua);
            case EVENT:
                return getString(R.string.type_geocache_event);
            case WEBCAM:
                return getString(R.string.type_geocache_webcam);
            case EXTREME:
                return getString(R.string.type_geocache_extreme);
            case CONTEST:
                return getString(R.string.type_geocache_contest);
            case CHECKPOINT:
                return getString(R.string.type_geocache_checkpoint);
            default:
                return getString(R.string.status_geocache_unknown);
                // what a terrible failure?
        }
    }
}
