package su.geocaching.android.view.userStory.selectGeoCache;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.model.dataType.GeoCache;
import su.geocaching.android.view.R;
import su.geocaching.android.view.geoCacheMap.GeoCacheItemizedOverlay;
import su.geocaching.android.view.geoCacheMap.GeoCacheMap;
import su.geocaching.android.view.userStory.showGeoCacheInfo.ShowGeoCacheInfo;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        Select GeoCache on the map.
 */
public class SelectGeoCacheMap extends GeoCacheMap {
    protected GeoCache geoCache;
    protected OverlayItem cacheOverlayItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.select_geocache_map);
        super.onCreate(savedInstanceState);
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

    private void startGeoCacheInfoView() {
        Intent intent = new Intent(this, ShowGeoCacheInfo.class);
        startActivity(intent);
        this.finish();
    }
}
