package su.geocaching.android.view.favoritwork;

import su.geocaching.android.ui.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
		convertView = mInter.inflate(R.layout.row_in_favorit_rolder, null);
		mt = new Views();
		mt.t1 = (ImageView) convertView.findViewById(R.id.favorit_list_imagebutton_type);
		mt.t2 = (TextView) convertView.findViewById(R.id.favorit_list_textview_name);
		convertView.setTag(mt);
	    } else {
		mt = (Views) convertView.getTag();
	    }

	    if (position < type.length) {
		if (type[position] == 1) {
		    mt.t1.setImageResource(R.drawable.icon_favorit_folder_traditional_cach);
		} else if (type[position] == 2) {
		    mt.t1.setImageResource(R.drawable.icon_favorit_folder_virtual_cach);
		} else if (type[position] == 3) {
		    mt.t1.setImageResource(R.drawable.icon_favorit_folder_step_by_step_cach);
		} else if (type[position] == 4) {
		    mt.t1.setImageResource(R.drawable.icon_favorit_folder_extrime_cach);
		}
	    }

	    mt.t2.setText(name[position]);
	    return convertView;
	}

	static class Views {
	    ImageView t1;
	    TextView t2;
	}
}
