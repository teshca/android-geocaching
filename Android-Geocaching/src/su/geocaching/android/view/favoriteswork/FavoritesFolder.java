package su.geocaching.android.view.favoriteswork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;

import su.geocaching.android.view.showgeocacheinfo.ShowGeoCacheInfo;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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


    private List<? extends Map<String, ?>> createGeoCacheList(ArrayList<GeoCache> t) {
	int type[] = new int[t.size()];
	String name[] = new String[t.size()];
	List<Map<String, ?>> te = new ArrayList<Map<String, ?>>();

	for (int i = 0; i < t.size(); i++) {
	    Map<String, Object> map = new HashMap<String, Object>();
	    type[i] = t.get(i).getType().ordinal();
	    name[i] = t.get(i).getName();
	    if (type[i] == 1) {
		map.put("type", R.drawable.icon_favorit_folder_traditional_cach);
	    } else {
		map.put("type", R.drawable.icon_favorit_folder_extrime_cach);
	    }
	    map.put("name", name[i]);
	    te.add(map);
	}

	return te;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.favorites_folder_menu, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	if (item.getItemId() == R.id.favorit_folder_Add_Cache_Button) {
	    addRandomCache();
	    return true;
	}
	return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
       dbm.closeDB();
        super.onStop();
    }
    
    @Override
    protected void onStart() {
	dbm.openDB();
	mass = dbm.getArrayGeoCache();
	if (mass != null) {
	    SimpleAdapter ap = new SimpleAdapter(this, createGeoCacheList(mass), R.layout.row_in_favorit_rolder, new String[] { "type", "name" }, new int[] { R.id.favorit_list_imagebutton_type,
		    R.id.favorit_list_textview_name });
	   lvListShowCache.setAdapter(ap);
	    lvListShowCache.setOnItemClickListener(this);
	} else {
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

    private void addRandomCache() {
	dbm.insert2_for_testing(234, "Name1", 1, 2, 5442387, -2030774, "My Web text-1");
	dbm.insert2_for_testing(239, "Name2", 2, 2, 5626802, -3454912, "My Web text-2");
	dbm.insert2_for_testing(232, "Name3", 1, 2, 556918, -3738894, "My Web text 3");
	dbm.insert2_for_testing(223, "Name4", 1, 2, 5745883, -2933664, "My Web text 4");
	dbm.insert2_for_testing(124, "Name5", 3, 2, 4850787, -4336517, "My Web text 5");
	dbm.insert2_for_testing(322, "Name6", 1, 2, 5531899, -3659884, "My Web text 6");
	dbm.insert2_for_testing(17, "Name7", 4, 2, 5619838, -3757061, "My Web text 7");
	dbm.insert2_for_testing(43, "Name8", 2, 2, 573722, -2854325, "My Web text 8");
	dbm.insert2_for_testing(412, "Name9", 2, 2, 4425389, -3355556, "My Web text 9");
	dbm.insert2_for_testing(430, "Name10", 2, 2, 5716796, -3647061, "My Web text 10");
	dbm.insert2_for_testing(432, "Name11", 3, 2, 5434437, -8321288, "My Web text 11");
	dbm.insert2_for_testing(439, "Name12", 1, 2, 5524446, -3353614, "My Web text 12");

	mass = dbm.getArrayGeoCache();

	SimpleAdapter ap = new SimpleAdapter(this, createGeoCacheList(mass), R.layout.row_in_favorit_rolder, new String[] { "type", "name" }, new int[] { R.id.favorit_list_imagebutton_type,
		R.id.favorit_list_textview_name });
	lvListShowCache.setAdapter(ap);
	lvListShowCache.setOnItemClickListener(this);

    }
}
