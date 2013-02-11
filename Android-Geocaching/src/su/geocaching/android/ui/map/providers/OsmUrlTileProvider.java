package su.geocaching.android.ui.map.providers;

import com.google.android.gms.maps.model.UrlTileProvider;
import su.geocaching.android.controller.managers.LogManager;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class OsmUrlTileProvider extends UrlTileProvider {

    private static final String TAG = MapnikOsmUrlTileProvider.class.getCanonicalName();

    public OsmUrlTileProvider() {
        super(256, 256);
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        try {
            return new URL(String.format(getUrlTemplate(), zoom, x, y));
        } catch (MalformedURLException e) {
            LogManager.e(TAG, e);
        }
        return null;
    }

    protected abstract String getUrlTemplate();
}