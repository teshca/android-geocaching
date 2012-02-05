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
import su.geocaching.android.controller.managers.ResourceManager;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.compass.UiThreadCompassView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Nikita Bumakov
 */
public class FavoritesArrayAdapter extends BaseArrayAdapter<GeoCache> implements Filterable {

    private static final String TAG = FavoritesArrayAdapter.class.getCanonicalName();

    private Comparator<GeoCache> distanceComparator = new DistanceComparator();
    private Comparator<GeoCache> nameComparator = new NameComparator();

    private List<GeoCache> allItemsArray;
    private List<GeoCache> filteredItemsArray;
    private ModelFilter filter;

    private ResourceManager rm;
    private Location lastLocation;

    public enum GeoCacheSortType {
        BY_DIST, BY_NAME
    }

    private GeoCacheSortType sortType;


    public FavoritesArrayAdapter(final Context context) {
        super(context);
        lastLocation = Controller.getInstance().getLocationManager().getLastKnownLocation();
        rm = Controller.getInstance().getResourceManager();
        sortType = GeoCacheSortType.BY_DIST;

        this.allItemsArray = new ArrayList<GeoCache>();
//        allItemsArray.addAll(list);
//        this.add(list);
        this.filteredItemsArray = new ArrayList<GeoCache>();
        filteredItemsArray.addAll(allItemsArray);
        getFilter();
    }

    public void add(Collection<GeoCache> collection) {
        for (GeoCache geoCache : collection) {
            add(geoCache);
        }
    }

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
        //TODO also check if sensor compass is available
        if (lastLocation != null) {
            holder.compassView.setCacheDirection(CoordinateHelper.getBearingBetween(lastLocation, geoCache.getLocationGeoPoint()));
            boolean hasPreciseLocation = Controller.getInstance().getLocationManager().hasPreciseLocation();
            float distance = CoordinateHelper.getDistanceBetween(geoCache.getLocationGeoPoint(), lastLocation);
            holder.textViewDistance.setText(CoordinateHelper.distanceToString(distance, hasPreciseLocation));
        } else {
            holder.compassView.setVisibility(View.GONE);
            holder.textViewDistance.setVisibility(View.GONE);
        }

        holder.compassView.invalidate();

        LogManager.d(TAG, "getView done for " + (System.currentTimeMillis() - time) + " ms. gc.name " + geoCache.getName() + " position " + position);
        return cv;
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

    private class Holder {
        final TextView textViewName;
        final TextView textViewType;
        final TextView textViewStatus;
        final TextView textViewDistance;
        final ImageView imageViewIcon;
        final UiThreadCompassView compassView;


        public Holder(final View textViewName, final View textViewType, final View textViewStatus, final View imageViewIcon, final View compassView, final View textViewDistance) {
            this.textViewName = (TextView) textViewName;
            this.textViewType = (TextView) textViewType;
            this.textViewStatus = (TextView) textViewStatus;
            this.imageViewIcon = (ImageView) imageViewIcon;
            this.compassView = (UiThreadCompassView) compassView;
            this.textViewDistance = (TextView) textViewDistance;
            this.compassView.setHelper(AbstractCompassDrawing.TYPE_PREVIEW);
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
            filteredItemsArray = (ArrayList<GeoCache>) results.values;
            notifyDataSetChanged();
            clear();
            for (GeoCache aFilteredItemsArray : filteredItemsArray) add(aFilteredItemsArray);
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
