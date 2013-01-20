package su.geocaching.android.ui.map;

import su.geocaching.android.controller.apimanager.GeoRect;

public abstract class ViewPortChangeListener {
    public abstract void OnViewPortChanged(GeoRect viewPort);
}
