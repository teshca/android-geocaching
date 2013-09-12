package su.geocaching.android.ui.map.providers;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

import su.geocaching.android.controller.managers.LogManager;

public class MarshrutyRuUrlTileProvider extends UrlTileProvider {

    private static final String TAG = MarshrutyRuUrlTileProvider.class.getCanonicalName();

    public MarshrutyRuUrlTileProvider() {
        super(256, 256);
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        try {
            return new URL(String.format("http://maps.marshruty.ru/ml.ashx?al=1&i=1&x=%d&y=%d&z=%d", x, y, zoom));
        } catch (MalformedURLException e) {
            LogManager.e(TAG, e);
        }
        return null;
    }
}