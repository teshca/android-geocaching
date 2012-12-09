package su.geocaching.android.ui.map;

import su.geocaching.android.controller.apimanager.GeoRect;

public abstract class ViewPortChangedListener {
    public abstract void OnViewPortChanged(GeoRect viewPort);
}
