package su.geocaching.android.ui.selectmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

public class EnableConnectionDialog extends AlertDialog {

    private Activity parent;

    protected EnableConnectionDialog(Activity parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch("/EnableConnectionDialog");
        setIcon(android.R.drawable.ic_dialog_alert);
        setTitle(getContext().getString(R.string.connection_problem_dialog_title));
        setMessage(getContext().getString(R.string.connection_problem_dialog_message));
        setCancelable(false);
        setButton(getContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                getContext().startActivity(intent);
                dialog.dismiss();
            }
        });
        setButton2(getContext().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                parent.finish();
                dialog.cancel();
            }
        });

        super.onCreate(savedInstanceState);
    }
}