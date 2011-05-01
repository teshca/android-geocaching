package su.geocaching.android.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.managers.DbManager;
import su.geocaching.android.controller.managers.ResourceManager;
import su.geocaching.android.model.GeoCache;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Abstract class for create ListActivity with caches
 */
public abstract class AbstractGeoCacheFolderActivity extends Activity implements OnItemClickListener {

    protected ArrayList<GeoCache> favoritesList = new ArrayList<GeoCache>();
    protected DbManager dbm;
    protected ListView lvListShowCache;
    protected TextView tvNoCache;

    protected String[] keys = new String[] { "type", "name", "typeText", "statusText" };

    public AbstractGeoCacheFolderActivity() {
        super();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_list);
        lvListShowCache = (ListView) findViewById(R.id.favorite_folder_listCache);
        tvNoCache = (TextView) findViewById(R.id.favorite_folder_title_text);
        dbm = Controller.getInstance().getDbManager();
        lvListShowCache.setOnItemClickListener(this);
    }

    protected List<Map<String, ?>> createGeoCacheList(ArrayList<GeoCache> cacheList) {
        List<Map<String, ?>> exitList = new ArrayList<Map<String, ?>>();

        for (GeoCache localGeoCache : cacheList) {
            Map<String, Object> map = new HashMap<String, Object>();
            ResourceManager rm = Controller.getInstance().getResourceManager();
            map.put(keys[0], rm.getMarkerResId(localGeoCache.getType(), localGeoCache.getStatus()));
            map.put(keys[1], localGeoCache.getName());
            map.put(keys[2], rm.getGeoCacheType(localGeoCache));
            map.put(keys[3], rm.getGeoCacheStatus(localGeoCache));
            exitList.add(map);
        }
        return exitList;
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }
}