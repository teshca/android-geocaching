/**
 * 
 */
package su.geocaching.android.utils;

import su.geocaching.android.ui.MenuActivity;
import android.content.Context;
import android.content.Intent;

/**
 * @author Nikita Bumakov
 * 
 */
public class UiUtils {

    /**
     * Invoke "home" action, returning to DashBoardActivity
     */
    public static void goHome(Context context) {
	final Intent intent = new Intent(context, MenuActivity.class);
	context.startActivity(intent);
    }
}
