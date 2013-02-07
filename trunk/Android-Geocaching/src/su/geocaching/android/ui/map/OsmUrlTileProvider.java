package su.geocaching.android.ui.map;

import com.google.android.gms.maps.model.UrlTileProvider;
import su.geocaching.android.controller.managers.LogManager;

import java.net.MalformedURLException;
import java.net.URL;

public class OsmUrlTileProvider extends UrlTileProvider {
    private static final String TAG = OsmUrlTileProvider.class.getCanonicalName();

    private static final String baseUrl = "http://tile.openstreetmap.org/%d/%d/%d.png";

    public OsmUrlTileProvider() {
        super(256, 256);
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        try {
            return new URL(String.format(baseUrl, zoom, x, y));
        } catch (MalformedURLException e) {
            LogManager.e(TAG, e);
        }
        return null;
    }
}