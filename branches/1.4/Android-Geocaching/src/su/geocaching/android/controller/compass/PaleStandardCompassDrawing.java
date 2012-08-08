package su.geocaching.android.controller.compass;

import android.graphics.BitmapFactory;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

/**
 * Alternative appearance of compass. (With white compass rose)
 *
 * @author Nikita Bumakov
 */
public class PaleStandardCompassDrawing extends DefaultCompassDrawing {

    public PaleStandardCompassDrawing() {
        super();
        roseBitmap = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.compass_rose_pale);
    }

    @Override
    public String getType() {
        return TYPE_PALE;
    }
}
