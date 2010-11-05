package su.geocaching.android.view.userStory.searchGeoCache;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.model.dataType.GeoCache;
import su.geocaching.android.view.MainMenu;
import su.geocaching.android.view.R;
import su.geocaching.android.view.geoCacheMap.GeoCacheItemizedOverlay;
import su.geocaching.android.view.geoCacheMap.GeoCacheMap;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        Search GeoCache with the map.
 */
public class SearchGeoCacheMap extends GeoCacheMap {
    public final static String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";

    private GeoCache geoCache;
    private OverlayItem cacheOverlayItem;
    private GeoCacheItemizedOverlay cacheItemizedOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        geoCache = new GeoCache(intent.getIntExtra(
                MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Drawable cacheMarker = this.getResources().getDrawable(R.drawable.orangecache);
        cacheMarker.setBounds(0,
                -cacheMarker.getMinimumHeight(),
                cacheMarker.getMinimumWidth(), 0);
        cacheItemizedOverlay = new GeoCacheItemizedOverlay(cacheMarker);
        cacheOverlayItem = new OverlayItem(geoCache.getLocation(), "", "");
        cacheItemizedOverlay.addOverlay(cacheOverlayItem);
        mapOverlays.add(cacheItemizedOverlay);
        mvMap.invalidate();
        mcMapController.animateTo(geoCache.getLocation());
    }

    private void startCompassView() {
        Intent intent = new Intent(this, SearchGeoCacheCompass.class);
        intent.putExtra(DEFAULT_GEOCACHE_ID_NAME, geoCache.getId());
        startActivity(intent);
        this.finish();
    }

    protected float getDistanceToGeoCache(Location location) {
        //TODO: implement using Location.distanceBetween
        return 0;
    }
}
