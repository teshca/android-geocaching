package su.geocaching.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.utils.UiHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesFolder extends Activity implements OnItemClickListener {

    private ArrayList<GeoCache> mass = new ArrayList<GeoCache>();
    private ListView lvListShowCache;
    private DbManager dbm = null;
    private TextView tvTitle;
    private GoogleAnalyticsTracker tracker;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.favorites_activity_folder));
        tracker.dispatch();
        setContentView(R.layout.favorit_list);
        lvListShowCache = (ListView) findViewById(R.id.favorit_folder_listCach);
        tvTitle = (TextView) findViewById(R.id.favorit_foldet_title_text);
        dbm = new DbManager(getBaseContext());

    }

    private List<Map<String, ?>> createGeoCacheList(ArrayList<GeoCache> t) {
        GeoCache localGeoCache = new GeoCache();
        List<Map<String, ?>> ExitList = new ArrayList<Map<String, ?>>();

        for (int i = 0; i < t.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            localGeoCache = t.get(i);
            map.put("statusText", Controller.getInstance().getResourceManager().getGeoCacheStatus(localGeoCache));
            map.put("typeText", Controller.getInstance().getResourceManager().getGeoCacheType(localGeoCache));
            map.put("type", Controller.getInstance().getResourceManager().getMarkerResId(localGeoCache));
            map.put("name", localGeoCache.getName());
            ExitList.add(map);
        }

        return ExitList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvTitle.setKeepScreenOn(Controller.getInstance().getKeepScreenOnPreference(tvTitle.getContext()));
    }

    @Override
    protected void onStart() {
        dbm.openDB();
        mass = dbm.getArrayGeoCache();
        dbm.closeDB();
        if (mass != null) {
            SimpleAdapter ap = new SimpleAdapter(this, createGeoCacheList(mass), R.layout.row_in_favorit_rolder, new String[]{"type", "name", "typeText", "statusText"}, new int[]{
                R.id.favorit_list_imagebutton_type, R.id.favorit_list_textview_name, R.id.favorites_row_type_text, R.id.favorites_row_statys_text});
            lvListShowCache.setAdapter(ap);
            lvListShowCache.setOnItemClickListener(this);
        } else {
            lvListShowCache.setAdapter(null);
            tvTitle.setText(tvTitle.getText() + "\n" + getString(R.string.favorit_folder_In_DB_not_cache));
            Log.d("FavoritFolder", "DB empty");
        }
        super.onStart();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent intent = new Intent(this, ShowGeoCacheInfo.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), mass.get(arg2));
        startActivity(intent);
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }
}
