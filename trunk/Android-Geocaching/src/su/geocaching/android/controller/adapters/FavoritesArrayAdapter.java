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
import su.geocaching.android.controller.compass.AbstractCompassDrawing;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.FavoritesFolderRow;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.compass.UiThreadCompassView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Nikita Bumakov
 */
public class FavoritesArrayAdapter extends BaseArrayAdapter<GeoCache> implements Filterable {

    private static final String TAG = FavoritesArrayAdapter.class.getCanonicalName();

    private Comparator<GeoCache> distanceComparator = new DistanceComparator();
    private Comparator<GeoCache> nameComparator = new NameComparator();

    private List<GeoCache> allItemsArray = new ArrayList<GeoCache>();
    private ModelFilter filter;

    private Location lastLocation;

    public enum GeoCacheSortType {
        BY_DIST, BY_NAME
    }

    private GeoCacheSortType sortType = GeoCacheSortType.BY_DIST;


    public FavoritesArrayAdapter(final Context context) {
        super(context);
        lastLocation = Controller.getInstance().getLocationManager().getLastKnownLocation();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LogManager.d(TAG, "getView");
        long time = System.currentTimeMillis();

        if (convertView == null) {
            long time2 = System.currentTimeMillis();
            convertView = new FavoritesFolderRow(getContext());
            LogManager.d(TAG, "inflater.inflate done for " + (System.currentTimeMillis() - time2) + " ms.");
        }

        final TextView textViewName = (TextView) convertView.findViewById(R.id.tvName);
        final TextView textViewType = (TextView) convertView.findViewById(R.id.tvType);
        final TextView textViewStatus = (TextView) convertView.findViewById(R.id.tvStatus);
        final TextView textViewDistance =  (TextView) convertView.findViewById(R.id.tvDistance);
        final ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
        final UiThreadCompassView compassView = (UiThreadCompassView) convertView.findViewById(R.id.compassView);        

        final GeoCache geoCache = getItem(position);
        textViewName.setText(geoCache.getName());
        textViewType.setText(Controller.getInstance().getResourceManager().getGeoCacheType(geoCache));
        textViewStatus.setText(Controller.getInstance().getResourceManager().getGeoCacheStatus(geoCache));
        imageViewIcon.setImageResource(Controller.getInstance().getResourceManager().getMarkerResId(geoCache.getType(), geoCache.getStatus()));

        if (lastLocation != null) {
            boolean hasPreciseLocation = Controller.getInstance().getLocationManager().hasPreciseLocation();
            float distance = CoordinateHelper.getDistanceBetween(geoCache.getLocationGeoPoint(), lastLocation);
            textViewDistance.setText(CoordinateHelper.distanceToString(distance, hasPreciseLocation));
        } else {
            textViewDistance.setVisibility(View.GONE);
        }

        if (lastLocation != null && Controller.getInstance().getCompassManager().IsCompassAvailable()) {
        	compassView.setHelper(AbstractCompassDrawing.TYPE_PREVIEW);
            compassView.setCacheDirection(CoordinateHelper.getBearingBetween(lastLocation, geoCache.getLocationGeoPoint()));
        } else {
            compassView.setVisibility(View.GONE);
        }

        compassView.invalidate();

        LogManager.d(TAG, "getView done for " + (System.currentTimeMillis() - time) + " ms. gc.name " + geoCache.getName() + " position " + position);
        return convertView;
    }

    /**
     * Implementing the Filterable interface.
     */
    public Filter getFilter() {
        if (filter == null) {
            filter = new ModelFilter();
        }
        return filter;
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

    public void setSortType(GeoCacheSortType sortType) {
        this.sortType = sortType;
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

    private class ModelFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                ArrayList<GeoCache> filteredItems = new ArrayList<GeoCache>();

                for (GeoCache m : allItemsArray) {
                    if (m.getName().toLowerCase().contains(constraint))
                        filteredItems.add(m);
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = allItemsArray;
                    result.count = allItemsArray.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((ArrayList<GeoCache>) results.values);
            sort();
            notifyDataSetInvalidated();
            notifyDataSetChanged();
        }
    }

    public void setAllItemsArray(List<GeoCache> allItemsArray) {
        this.allItemsArray = allItemsArray;
    }
    
    public int getAllItemsCount()
    {
        return this.allItemsArray.size();
    }
}
