package su.geocaching.android.ui;

import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OverlayUtils {

    public static boolean isMultiTouch(MotionEvent event)
    {
        try {
            Method getPointer = MotionEvent.class.getMethod("getPointerCount");
            if (Integer.parseInt(getPointer.invoke(event).toString()) > 1) {
                // prevent tap on geocache icon on multitouch
                return true;
            }
            /* success, this is a newer device */
        } catch (NoSuchMethodException e) {
            /* failure, must be older device */
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

}
