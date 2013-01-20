package su.geocaching.android.ui.map;

import su.geocaching.android.model.GeoCache;

abstract public class GeocacheMarkerTapListener {
    public abstract void OnMarkerTapped(GeoCache geocache);
    public abstract void OnMarkerLongTapped(GeoCache geocache);
}