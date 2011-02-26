package su.geocaching.android.ui.geocachemap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.utils.UiHelper;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
public class SearchCacheOverlay extends ItemizedOverlay<OverlayItem> {

	private GestureDetector gestureDetector;
	private boolean longClick;

	private List<GeoCacheOverlayItem> items;
	private Context context;

	public SearchCacheOverlay(Drawable defaultMarker, Context context) {
		super(defaultMarker);
		items = Collections.synchronizedList(new LinkedList<GeoCacheOverlayItem>());
		this.context = context;
		gestureDetector = new GestureDetector(context, new GestureScanner());
		populate();
	}

	public synchronized void addOverlayItem(GeoCacheOverlayItem overlay) {
		if (!contains(overlay.getGeoCache()) || overlay.getTitle().equals("Group")) {
			items.add(overlay);
			setLastFocusedIndex(-1);
			populate();
		}
	}

	private boolean contains(GeoCache geoCache) {
		for (GeoCacheOverlayItem item : items) {
			if (item.getGeoCache().equals(geoCache)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return items.get(i);
	}

	@Override
	public int size() {
		return items.size();
	}

	public synchronized void clear() {
		items.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	@Override
	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
	}

	@Override
	public boolean onTap(int index) {
		if (!longClick) {
			UiHelper.showGeoCacheInfo(context, items.get(index).getGeoCache());
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		return gestureDetector.onTouchEvent(event);
	}

	class GestureScanner implements OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {			
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			longClick = true;
			}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			longClick = false;
			return false;
		}

	}

}
