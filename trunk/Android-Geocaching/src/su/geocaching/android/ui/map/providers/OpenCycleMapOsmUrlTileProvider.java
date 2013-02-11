package su.geocaching.android.ui.map.providers;

public class OpenCycleMapOsmUrlTileProvider extends OsmUrlTileProvider {

    private static final String baseUrl = "http://tile.opencyclemap.org/cycle/%d/%d/%d.png";

    @Override
    protected String getUrlTemplate() {
        return baseUrl;
    }
}