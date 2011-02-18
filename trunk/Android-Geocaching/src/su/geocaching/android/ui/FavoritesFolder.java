package su.geocaching.android.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.UiHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class FavoritesFolder extends Activity implements OnItemClickListener {

    private ArrayList<GeoCache> mass = new ArrayList<GeoCache>();
    private ListView lvListShowCache;
    private DbManager dbm = null;
    private TextView tvTitle;
    private GoogleAnalyticsTracker tracker; 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	tracker = GoogleAnalyticsTracker.getInstance();
	tracker.start("UA-20327116-1", this);
	tracker.trackPageView("/favoriteActivity");
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

	    switch (localGeoCache.getStatus()) {
	    case VALID:
		map.put("statusText", getString(R.string.status_geocache_valid));
		break;
	    case NOT_VALID:
		map.put("statusText", getString(R.string.status_geocache_no_valid));
		break;
	    case NOT_CONFIRMED:
		map.put("statusText", getString(R.string.status_geocache_no_confirmed));
		break;
	    default:
		map.put("statusText", "???");
		break;
	    }

	    switch (localGeoCache.getType()) {
	    case TRADITIONAL:
		map.put("typeText", getString(R.string.type_geocache_traditional));

		switch (localGeoCache.getStatus()) {
		case VALID:
		    map.put("type", R.drawable.ic_cache_traditional_valid);
		    break;
		case NOT_VALID:
		    map.put("type", R.drawable.ic_cache_traditional_not_valid);
		    break;
		case NOT_CONFIRMED:
		    map.put("type", R.drawable.ic_cache_traditional_not_confirmed);
		    break;
		default:
		    map.put("type", R.drawable.ic_cache_traditional_not_confirmed);
		    break;
		}

		break;
	    case VIRTUAL:
		map.put("typeText", getString(R.string.type_geocache_virtua));

		switch (localGeoCache.getStatus()) {
		case VALID:
		    map.put("type", R.drawable.ic_cache_virtual_valid);
		    break;
		case NOT_VALID:
		    map.put("type", R.drawable.ic_cache_virtual_not_valid);
		    break;
		case NOT_CONFIRMED:
		    map.put("type", R.drawable.ic_cache_virtual_not_confirmed);
		    break;
		default:
		    map.put("type", R.drawable.ic_cache_virtual_not_confirmed);
		    break;
		}
		
		break;
	    case STEP_BY_STEP:
		map.put("typeText", getString(R.string.type_geocache_step_by_step));

		switch (localGeoCache.getStatus()) {
		case VALID:
		    map.put("type", R.drawable.ic_cache_stepbystep_valid);
		    break;
		case NOT_VALID:
		    map.put("type", R.drawable.ic_cache_stepbystep_not_valid);
		    break;
		case NOT_CONFIRMED:
		    map.put("type", R.drawable.ic_cache_stepbystep_not_confirmed);
		    break;
		default:
		    map.put("type", R.drawable.ic_cache_stepbystep_not_confirmed);
		    break;
		}

		break;
	    case EVENT:
		map.put("typeText", getString(R.string.type_geocache_event));

		switch (localGeoCache.getStatus()) {
		case VALID:
		    map.put("type", R.drawable.ic_cache_event_valid);
		    break;
		case NOT_VALID:
		    map.put("type", R.drawable.ic_cache_event_not_valid);
		    break;
		case NOT_CONFIRMED:
		    map.put("type", R.drawable.ic_cache_event_not_confirmed);
		    break;
		default:
		    map.put("type", R.drawable.ic_cache_event_not_confirmed);
		    break;
		}

		break;
	    case EXTREME:
		map.put("typeText", getString(R.string.type_geocache_extreme));

		switch (localGeoCache.getStatus()) {
		case VALID:
		    map.put("type", R.drawable.ic_cache_extreme_valid);
		    break;
		case NOT_VALID:
		    map.put("type", R.drawable.ic_cache_extreme_not_valid);
		    break;
		case NOT_CONFIRMED:
		    map.put("type", R.drawable.ic_cache_extreme_not_confirmed);
		    break;
		default:
		    map.put("type", R.drawable.ic_cache_extreme_not_confirmed);
		    break;
		}

		break;
	    default:
		map.put("typeText", "???");

		switch (localGeoCache.getStatus()) {
		case VALID:
		    map.put("type", R.drawable.ic_cache_traditional_valid);
		    break;
		case NOT_VALID:
		    map.put("type", R.drawable.ic_cache_traditional_not_valid);
		    break;
		case NOT_CONFIRMED:
		    map.put("type", R.drawable.ic_cache_traditional_not_confirmed);
		    break;
		default:
		    map.put("type", R.drawable.ic_cache_traditional_not_confirmed);
		    break;
		}

		break;
	    }

	    map.put("name", localGeoCache.getName());
	    ExitList.add(map);
	}

	return ExitList;
    }

    @Override
    protected void onStart() {
	dbm.openDB();
	mass = dbm.getArrayGeoCache();
	dbm.closeDB();
	tracker.start("UA-20327116-1", this);
	tracker.trackPageView("/favoriteActivity");
	if (mass != null) {
	    SimpleAdapter ap = new SimpleAdapter(this, createGeoCacheList(mass), R.layout.row_in_favorit_rolder, new String[] { "type", "name", "typeText", "statusText" }, new int[] {
		    R.id.favorit_list_imagebutton_type, R.id.favorit_list_textview_name, R.id.favorites_row_type_text, R.id.favorites_row_statys_text });
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
