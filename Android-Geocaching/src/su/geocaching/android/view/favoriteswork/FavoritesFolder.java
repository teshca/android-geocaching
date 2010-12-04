package su.geocaching.android.view.favoriteswork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;

import su.geocaching.android.view.showgeocacheinfo.ShowGeoCacheInfo;
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

public class FavoritesFolder extends Activity implements OnItemClickListener {

    private ArrayList<GeoCache> mass = new ArrayList<GeoCache>();
    private ListView lvListShowCache;
    private DbManager dbm = null;
    private TextView tvTitle;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.favorit_list);
	lvListShowCache = (ListView) findViewById(R.id.favorit_folder_listCach);
	tvTitle = (TextView) findViewById(R.id.favorit_foldet_title_text);
	dbm = new DbManager(getBaseContext());
	
    }

    private List<Map<String, ?>> createGeoCacheList(ArrayList<GeoCache> t) {
	int type[] = new int[t.size()];
	String name[] = new String[t.size()];
	List<Map<String, ?>> ExitList = new ArrayList<Map<String, ?>>();

	for (int i = 0; i < t.size(); i++) {
	    Map<String, Object> map = new HashMap<String, Object>();
	    type[i] = t.get(i).getType().ordinal();
	    name[i] = t.get(i).getName();

	    switch (t.get(i).getStatus().ordinal()){
	    case 0:
		map.put("statusText", getString(R.string.status_geocache_valid));
		break;
	    case 1:
		map.put("statusText", getString(R.string.status_geocache_no_valid));
		break;
	    case 2:
		map.put("statusText",getString(R.string.status_geocache_no_confirmed));
		break;
	   default:
	       map.put("statusText", "???");
	       break;
	    }
	    
	    switch (type[i]) {
	    case 0:
		map.put("typeText", getString(R.string.type_geocache_traditional));
		map.put("type", R.drawable.icon_favorit_folder_traditional_cach);
		break;
	    case 1:
		map.put("typeText", getString(R.string.type_geocache_virtua));
		map.put("type", R.drawable.icon_favorit_folder_virtual_cach);
		break;
	    case 2:
		map.put("typeText", getString(R.string.type_geocache_step_by_step));
		map.put("type", R.drawable.icon_favorit_folder_step_by_step_cach);
		break;
	    case 3:
		map.put("typeText",getString(R.string.type_geocache_event));
		map.put("type", R.drawable.icon_favorit_folder_extrime_cach);
		break;
	    case 4:
		map.put("typeText", getString(R.string.type_geocache_extreme));
		map.put("type", R.drawable.icon_favorites_folder_event);
		break;
	    default:
		map.put("typeText", "???");
		map.put("type", R.drawable.icon_favorit_folder_traditional_cach);
		break;
	    }

	    map.put("name", name[i]);
	    ExitList.add(map);
	}

	return ExitList;
    }


    @Override
    protected void onStart() {
	dbm.openDB();
	mass = dbm.getArrayGeoCache();
	dbm.closeDB();
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

}
