package su.geocaching.android.controller;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Manager which can get access to application resources
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Mar 8, 2011
 */
public class ResourceManager {
	private Context context;

	public ResourceManager(Context context) {
		this.context = context;
	}

	public Drawable getDrawable(int id) {
		return context.getResources().getDrawable(id);
	}

	public String getString(int id) {
		return context.getString(id);
	}

	public String getString(int id, Object... formatArgs) {
		return context.getString(id, formatArgs);
	}

	public CharSequence getText(int id) {
		return context.getText(id);
	}

	public Resources getResources() {
		return context.getResources();
	}
}
