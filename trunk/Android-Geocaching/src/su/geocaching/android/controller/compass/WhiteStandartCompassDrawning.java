package su.geocaching.android.controller.compass;

import android.graphics.BitmapFactory;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

/**
 * Alternative appearance of compass. (With white compass rose)
 * 
 * @author Nikita Bumakov
 */
public class WhiteStandartCompassDrawning extends StandartCompassDrawning {

    public WhiteStandartCompassDrawning() {
        super();
        roseBitmap = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.compass_rose_pale);
    }
}
