package su.geocaching.android.view.userstory.favoritwork;

import su.geocaching.android.ui.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FavoritFolder extends Activity {
   
    private Display  dsDis;
    public class Efficient extends BaseAdapter {
	private LayoutInflater mInter;
	private int type[];
	private String name[];
	
	public Efficient(int[] type, String[] name, Context context) {
	    this.type = type;
	    this.name = name;
	    this.mInter = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
	    return name.length;
	}

	@Override
	public Object getItem(int position) {
	    return position;
	}

	@Override
	public long getItemId(int position) {
	    return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    Views mt;
	    if (convertView == null) {
		convertView=mInter.inflate(R.layout.row_in_favorit_rolder, null);
		mt=new Views();
		mt.t1=(ImageView)convertView.findViewById(R.id.favorit_list_imagebutton_type);
		mt.t2=(TextView)convertView.findViewById(R.id.favorit_list_textview_name);
		convertView.setTag(mt);
	    }else{
		mt=(Views)convertView.getTag();
	    }
	 
	    if(position<type.length){
	    if(type[position]==1){
		mt.t1.setImageResource(R.drawable.icon_favorit_folder_traditional_cach);
	    }else if(type[position]==2){
		mt.t1.setImageResource(R.drawable.icon_favorit_folder_virtual_cach);
	    }
	    }
	   
	    mt.t2.setText(name[position]);
	    return convertView;
	}
	
    }

    static class Views {
	ImageView t1;
	TextView t2;
    }

    private ListView lvListShowCach;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	dsDis=((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	setContentView(R.layout.favorit_list);

	lvListShowCach = (ListView) findViewById(R.id.favorit_folder_listCach);	
	lvListShowCach.setLayoutParams(new LinearLayout.LayoutParams(dsDis.getWidth(),dsDis.getHeight()-dsDis.getHeight()/3));
	/**
	 * Exemple massib type and name GeoCach
	 * These data come from the database
	 * */
	int tb1[] = {1,1,2,1,2,2,2,1,2,2,2,1,1,1,1,2,2,2};
	String tb2[] = { "Ope", "Четыре грани ","vnjd2039r","932fjffjlj","vj0rfjefsjdl","fjdsl;vnsad","vnjd2039r","932fjffjlj","vj0rfjefsjdl","vnjd2039r","932fjffjlj","vj0rfjefsjdl", "Возрождение христианства", "fsdav", "Close2", "Big2", "Bad2","342"};

	lvListShowCach.setAdapter(new Efficient(tb1, tb2, this));
	
    }

}
