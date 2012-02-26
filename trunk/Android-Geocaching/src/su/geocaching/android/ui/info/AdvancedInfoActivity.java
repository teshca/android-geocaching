package su.geocaching.android.ui.info;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Activity to display cache's information, notebook and photos
 *
 * @author Nickolay Artamonov
 */
public class AdvancedInfoActivity extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(su.geocaching.android.ui.R.layout.advanced_info_activity);
    }
}
