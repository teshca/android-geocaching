package su.geocaching.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.NavigationManager;

public class AskForRatingDialog extends AlertDialog {

    private static final String ASK_FOR_RATING_DIALOG_NAME = "/AskForRatingDialog";   
    
    public AskForRatingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set icon
        setIcon(R.drawable.ic_launcher);
        // set title
        setTitle(getContext().getString(R.string.ask_for_rating_title_text));
        setMessage(getContext().getString(R.string.ask_for_rating_message_text));
        // add exit button
        setButton(BUTTON_POSITIVE, getContext().getString(R.string.ask_for_rating_yes_button_text),
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(ASK_FOR_RATING_DIALOG_NAME + "/yes");
                        NavigationManager.startAndroidMarketActivity(getContext());
                        dialog.dismiss();
                    }
                });
        setButton(BUTTON_NEGATIVE, getContext().getString(R.string.ask_for_rating_no_button_text),
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(ASK_FOR_RATING_DIALOG_NAME + "/no");
                        dialog.dismiss();
                    }
                });

        setCancelable(true);

        super.onCreate(savedInstanceState);
    }
}
