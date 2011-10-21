package su.geocaching.android.controller.adapters;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.ResourceManager;
import su.geocaching.android.controller.managers.UserLocationManager;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.compass.OneThreadCompassView;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author Nikita Bumakov
 */
public class FavoritesArrayAdapter extends BaseArrayAdapter<GeoCache> implements Filterable {

    private static final String TAG = FavoritesArrayAdapter.class.getCanonicalName();

    private final Object mLock = new Object();
    private ItemsFilter mFilter;
    private Comparator distanceComparator = new DistanceComparator();
    private Comparator nameComparator = new NameComparator();
    public ArrayList<GeoCache> gcItems;

    private UserLocationManager locationManager;
    private ResourceManager rm;
    private Location lastLocation;

    public enum SortType {
        BY_DIST, BY_NAME
    }

    private SortType sortType;


    public FavoritesArrayAdapter(final Context context) {
        super(context);
        locationManager = Controller.getInstance().getLocationManager();
        lastLocation = locationManager.getLastKnownLocation();
        rm = Controller.getInstance().getResourceManager();
        sortType = SortType.BY_DIST;
    }

//    @Override
//    public void add(GeoCache object) {
//        super.add(object);
//        sortByDistance();
//    }

    @Override
    public View getView(int position, View cv, ViewGroup parent) {

        LogManager.d(TAG, "getView");
        long time = System.currentTimeMillis();

        if (cv == null) {
            long time2 = System.currentTimeMillis();
            cv = inflater.inflate(R.layout.row_favorites, null);
            cv.setTag(new Holder(cv.findViewById(R.id.tvName), cv.findViewById(R.id.tvType), cv.findViewById(R.id.tvStatus), cv.findViewById(R.id.ivIcon), cv.findViewById(R.id.compassView), cv.findViewById(R.id.tvDistance)));
            LogManager.d(TAG, "inflater.inflate done for " + (System.currentTimeMillis() - time2) + " ms.");
        }

        final GeoCache geoCache = getItem(position);
        final Holder holder = (Holder) cv.getTag();
        holder.textViewName.setText(geoCache.getName());
        holder.textViewType.setText(rm.getGeoCacheType(geoCache));
        holder.textViewStatus.setText(rm.getGeoCacheStatus(geoCache));
        holder.imageViewIcon.setImageResource(rm.getMarkerResId(geoCache.getType(), geoCache.getStatus()));

        //TODO check/improve it;
        if (lastLocation != null) {
            holder.compassView.setCacheDirection(CoordinateHelper.getBearingBetween(lastLocation, geoCache.getLocationGeoPoint()));
            boolean hasPreciseLocation = Controller.getInstance().getLocationManager().hasPreciseLocation();
            float distance = CoordinateHelper.getDistanceBetween(geoCache.getLocationGeoPoint(), lastLocation);
            holder.textViewDistance.setText(CoordinateHelper.distanceToString(distance, hasPreciseLocation));
        }

        holder.compassView.invalidate();

        LogManager.d(TAG, "getView done for " + (System.currentTimeMillis() - time) + " ms. gc.name " + geoCache.getName() + " position " + position);
        return cv;
    }

    /**
     * Implementing the Filterable interface.
     */
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ItemsFilter();
        }
        return mFilter;
    }

    private void sortByDistance() {
        sort(distanceComparator);
    }

    private void sortByName() {
        sort(nameComparator);
    }

    public void sort() {
        switch (sortType) {
            case BY_DIST:
                sortByDistance();
                break;
            case BY_NAME:
                sortByName();
                break;
        }
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    private class Holder {
        final TextView textViewName;
        final TextView textViewType;
        final TextView textViewStatus;
        final TextView textViewDistance;
        final ImageView imageViewIcon;
        final OneThreadCompassView compassView;


        public Holder(final View textViewName, final View textViewType, final View textViewStatus, final View imageViewIcon, final View compassView, final View textViewDistance) {
            this.textViewName = (TextView) textViewName;
            this.textViewType = (TextView) textViewType;
            this.textViewStatus = (TextView) textViewStatus;
            this.imageViewIcon = (ImageView) imageViewIcon;
            this.compassView = (OneThreadCompassView) compassView;
            this.textViewDistance = (TextView) textViewDistance;
            this.compassView.setHelper("PREVIEW");
        }
    }

    class DistanceComparator implements Comparator<GeoCache> {

        @Override
        public int compare(GeoCache geoCache1, GeoCache geoCache2) {
            if (lastLocation == null) {
                return 0;
            }
            float distance1 = CoordinateHelper.getDistanceBetween(geoCache1.getLocationGeoPoint(), lastLocation);
            float distance2 = CoordinateHelper.getDistanceBetween(geoCache2.getLocationGeoPoint(), lastLocation);
            return Float.compare(distance1, distance2);
        }
    }

    class NameComparator implements Comparator<GeoCache> {

        @Override
        public int compare(GeoCache geoCache1, GeoCache geoCache2) {
            return geoCache1.getName().toLowerCase().compareTo(geoCache2.getName().toLowerCase());
        }
    }

    private class ItemsFilter extends Filter {

        protected FilterResults performFiltering(CharSequence query) {
            // Initiate our results object
            FilterResults results = new FilterResults();
            // If the adapter array is empty, check the actual items array and use it
            if (gcItems == null) {
                synchronized (mLock) {
                    gcItems = new ArrayList<GeoCache>(gcItems);
                }
            }
            // No prefix is sent to filter by so we're going to send back the original array
            if (query == null || query.length() == 0) {
                synchronized (mLock) {
                    results.values = gcItems;
                    results.count = gcItems.size();
                }
            } else {
                // Local to here so we're not changing actual array
                final ArrayList<GeoCache> items = gcItems;
                final int count = items.size();
                final ArrayList<GeoCache> newItems = new ArrayList<GeoCache>(count);
                for (int i = 0; i < count; i++) {
                    final GeoCache item = items.get(i);
                    final String itemName = item.getName().toString().toLowerCase();
                    // First match against the whole, non-splitted value
                    if (itemName.contains(query.toString().toLowerCase())) {
                        newItems.add(item);
                    }
                }
                // Set and return
                results.values = newItems;
                results.count = newItems.size();
            }
            gcItems = (ArrayList<GeoCache>) results.values;
            return results;
        }

        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence prefix, FilterResults results) {
            //noinspection unchecked

            clear();
            for (GeoCache gc : gcItems) {
                add(gc);
            }
            sort();
            // Let the adapter know about the updated list
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
