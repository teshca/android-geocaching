package su.geocaching.android.controller.compass;

import android.content.Context;
import android.graphics.BitmapFactory;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

/**
 * Alternative appearance of compass. (With white compass rose)
 * 
 * @author Nikita Bumakov
 */
public class WhiteStandartCompassDrawning extends StandartCompassDrawning {

    public WhiteStandartCompassDrawning(Context context) {
        super(context);
        roseBitmap = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager(context.getApplicationContext()).getResources(), R.drawable.compass_rose_pale);
    }
}
