package su.geocaching.android.controller.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;

/**
 * @author Nikita Bumakov
 */
public class CheckpointsArrayAdapter extends FavoritesArrayAdapter {

    public CheckpointsArrayAdapter(final Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final GeoCache geoCache = getItem(position);
        if (geoCache.getType() == GeoCacheType.CHECKPOINT) {
            final TextView textViewType = (TextView) view.findViewById(R.id.tvType);
            textViewType.setText(CoordinateHelper.coordinateToString(geoCache.getGeoPoint()));
        }
        return view;
    }
}