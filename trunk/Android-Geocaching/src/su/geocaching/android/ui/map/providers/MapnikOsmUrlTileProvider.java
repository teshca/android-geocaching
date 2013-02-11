package su.geocaching.android.ui.map.providers;

public class MapnikOsmUrlTileProvider extends OsmUrlTileProvider {

    private static final String baseUrl = "http://tile.openstreetmap.org/%d/%d/%d.png";

    @Override
    protected String getUrlTemplate() {
        return baseUrl;
    }
}