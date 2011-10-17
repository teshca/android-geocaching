package su.geocaching.android.controller.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

/**
 * @author Nikita Bumakov
 */
public class BaseArrayAdapter<T> extends ArrayAdapter<T> {
    protected final LayoutInflater inflater;


    public BaseArrayAdapter(final Context context) {
        super(context, 0);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /* @Override
    public void add(T object) {
        if (getPosition(object) == -1) {
            super.add(object);
        }
    }*/
}