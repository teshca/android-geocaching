package su.geocaching.android.ui.map.providers;

public class MapQuestOsmUrlTileProvider extends OsmUrlTileProvider {

    private static final String baseUrl = "http://otile%d.mqcdn.com";
    private static final String tilePath = "/tiles/1.0.0/osm/%d/%d/%d.png";

    private final String tileUrl;

    public MapQuestOsmUrlTileProvider() {
        tileUrl = String.format(baseUrl, (int)(1 + 3 * Math.random())) + tilePath;
    }

    @Override
    protected String getUrlTemplate() {
        return tileUrl;
    }
}