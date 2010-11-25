package su.geocaching.android.view.favoriteswork;


import java.util.ArrayList;


import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;

import su.geocaching.android.view.showgeocacheinfo.ShowGeoCacheInfo;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FavoritesFolder extends Activity implements OnClickListener, OnItemClickListener {

    private Efficient baForListView;
    private ArrayList<GeoCache> mass = new ArrayList<GeoCache>();
    private ListView lvListShowCach;
    private ImageView ivSearch;
    private ImageView ivDelete;
    private DbManager dbm = null;
    private TextView tvTitle;
    private View lvNowSelect = null;
    private int numClickItem;
    private int[] massTypeCache;
    private String[] massNameCache;
    private Button addBut;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.favorit_list);

	lvListShowCach = (ListView) findViewById(R.id.favorit_folder_listCach);
	ivSearch = (ImageView) findViewById(R.id.favorits_list_search_cache);
	ivDelete = (ImageView) findViewById(R.id.favorits_list_delete_cache);
	tvTitle = (TextView) findViewById(R.id.favorit_foldet_title_text);
	//addBut=(Button)findViewById(R.id.addBut);
	
	//addBut.setOnClickListener(this);
	ivSearch.setOnClickListener(this);
	ivDelete.setOnClickListener(this);
	
	if (dbm == null) {
	    dbm = new DbManager(getBaseContext());
	}

	dbm.openDB();

	 
	mass = dbm.getArrayGeoCache();
	// dbm.closeDB();

	ivSearch.setEnabled(false);
	ivDelete.setEnabled(false);
	
	if (mass != null) {
	//    addBut.setEnabled(false);
	    massNameCache = new String[mass.size()];
	    massTypeCache = new int[mass.size()];
	    for (int i = 0; i < mass.size(); i++) {
		massNameCache[i] = mass.get(i).getName();
		massTypeCache[i] = mass.get(i).getType().ordinal();
	    }

	    baForListView = new Efficient(massTypeCache, massNameCache, this);
	    lvListShowCach.setAdapter(baForListView);

	    lvListShowCach.setOnItemClickListener(this);
	} else {
	    tvTitle.setText(tvTitle.getText()+"\n"+getString(R.string.favorit_folder_In_DB_not_cache) );

	    Log.d("FavoritFolder", "DB empty");
	}

    }

    @Override
    public void onClick(View v) {
	if ((v.equals(ivSearch))&&(lvNowSelect != null)) {
	    Intent intent = new Intent(this, ShowGeoCacheInfo.class);
	    intent.putExtra(GeoCache.class.getCanonicalName(), mass.get(numClickItem));
	    startActivity(intent);
	}

	if ((v.equals(ivDelete))&&(lvNowSelect != null)) {
	    // TODO: Very very bad code. write better.
	    
		dbm.deleteCachById(mass.get(numClickItem).getId());
		mass.remove(numClickItem);
		
		massNameCache = new String[mass.size()];
		massTypeCache = new int[mass.size()];
		
		for (int i = 0; i < mass.size(); i++) {
		    massNameCache[i] = mass.get(i).getName();
		    massTypeCache[i] = mass.get(i).getType().ordinal();
		}
		 baForListView = new Efficient(massTypeCache, massNameCache, this);
		lvListShowCach.setAdapter(baForListView);
		lvNowSelect=null;
	    }
	if(v.equals(addBut)){
		 dbm.insert2_for_testing(234, "Name1", 1, 2,  5442387, -2030774, "My Web text-1");
		 dbm.insert2_for_testing(239, "Name2", 2, 2, 5626802, -3454912, "My Web text-2");
		 dbm.insert2_for_testing(232, "Name3", 1, 2,  556918, -3738894, "My Web text 3");
		 dbm.insert2_for_testing(223, "Name4", 1, 2, 5745883, -2933664, "My Web text 4");
		 dbm.insert2_for_testing(124, "Name5", 3, 2, 4850787,-4336517, "My Web text 5");
		 dbm.insert2_for_testing(322, "Name6", 1, 2, 5531899, -3659884, "My Web text 6");
		 dbm.insert2_for_testing(17, "Name7", 4, 2, 5619838, -3757061, "My Web text 7");
		 dbm.insert2_for_testing(43, "Name8", 2, 2, 573722, -2854325, "My Web text 8");
		 dbm.insert2_for_testing(412, "Name9", 2, 2, 4425389, -3355556, "My Web text 9");
		 dbm.insert2_for_testing(430, "Name10", 2, 2, 5716796, -3647061, "My Web text 10");
		 dbm.insert2_for_testing(432, "Name11", 3, 2, 5434437, -8321288, "My Web text 11");
		 dbm.insert2_for_testing(439, "Name12", 1, 2, 5524446, -3353614, "My Web text 12");
	    
		 mass = dbm.getArrayGeoCache();
		 massNameCache = new String[mass.size()];
		    massTypeCache = new int[mass.size()];
		    for (int i = 0; i < mass.size(); i++) {
			massNameCache[i] = mass.get(i).getName();
			massTypeCache[i] = mass.get(i).getType().ordinal();
		    }

		    baForListView = new Efficient(massTypeCache, massNameCache, this);
		    lvListShowCach.setAdapter(baForListView);
		    lvListShowCach.setOnItemClickListener(this);
		    addBut.setEnabled(false);
	}
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	ivSearch.setEnabled(true);
	ivDelete.setEnabled(true);
	if (lvNowSelect == null) {
	    this.lvNowSelect = arg1;
	    this.lvNowSelect.setBackgroundColor(Color.GRAY);
	} else {
	    this.lvNowSelect.setBackgroundColor(Color.BLACK);
	    this.lvNowSelect = arg1;
	    this.lvNowSelect.setBackgroundColor(Color.GRAY);
	}
	
	numClickItem = arg2;
    }

}
