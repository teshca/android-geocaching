package su.geocaching.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import su.geocaching.android.controller.Controller;

public class AboutDialog extends AlertDialog {

    protected AboutDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set icon
        setIcon(R.drawable.ic_launcher);
        // set title
        String versionName = Controller.getInstance().getApplicationVersionName();
        setTitle(getContext().getString(R.string.about_application_version, versionName));
        // set content
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View aboutContentView = inflater.inflate(R.layout.about_dialog, null);
        setView(aboutContentView);
        // add exit button
        setButton(getContext().getString(R.string.about_exit_button_text),
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        setCancelable(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttachedToWindow()
    {
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch("/AboutDialog");
    }
}
