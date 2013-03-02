package su.geocaching.android.controller.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import java.util.Collection;

/**
 * @author Nikita Bumakov
 */
public class BaseArrayAdapter<T> extends ArrayAdapter<T> {
    protected final LayoutInflater inflater;


    public BaseArrayAdapter(final Context context) {
        super(context, 0);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Added in API level 11
    public void addAll(Collection<? extends T> collection) {
        for (T o : collection) {
            add(o);
        }
    }
}