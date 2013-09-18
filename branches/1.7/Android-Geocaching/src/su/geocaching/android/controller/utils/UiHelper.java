package su.geocaching.android.controller.utils;

import android.view.View;

/**
 * @author: Grigory Kalabin
 * @since: 11.10.2011
 */
public class UiHelper {

    /**
     * Set visibility 'visible' if not visible yet
     *
     * @param v
     *         view
     */
    public static void show(View v) {
        if (v.getVisibility() != View.VISIBLE) {
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set visibility 'gone' if not visible yet
     *
     * @param v
     *         view
     */
    public static void hide(View v) {
        if (v.getVisibility() != View.GONE) {
            v.setVisibility(View.GONE);
        }
    }
}
