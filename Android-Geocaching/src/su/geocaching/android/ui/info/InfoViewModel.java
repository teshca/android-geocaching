package su.geocaching.android.ui.info;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.DbManager;
import su.geocaching.android.model.GeoCache;

public class InfoViewModel {
    private static final String TAG = InfoViewModel.class.getCanonicalName();
    
    private GeoCache geoCache;
    private DbManager dbManager;
    private boolean isCacheStored;
    private String information;    
    
    public InfoViewModel() {
        dbManager = Controller.getInstance().getDbManager();
    }
    
    public synchronized void SetGeoCache(GeoCache geoCache) {
        if (this.geoCache != geoCache || this.geoCache.getId() != geoCache.getId()) {
            this.geoCache = geoCache;
            
            isCacheStored = dbManager.isCacheStored(geoCache.getId());
            if (isCacheStored) {
                information = dbManager.getCacheInfoById(geoCache.getId());
            }
        }
    }

    public String getInformation() {
        return information;
    }
}
