package su.geocaching.android.ui.info;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import android.content.Context;
import android.widget.CheckBox;
import su.geocaching.android.ui.R;

public class DownloadNotebookDialog extends AlertDialog {

    private static final String TAG = DownloadNotebookDialog.class.getCanonicalName();

    private ConfirmDialogResultListener resultListener;

    protected DownloadNotebookDialog(final Context context, final ConfirmDialogResultListener resultListener) {
        super(context);
        this.resultListener = resultListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set icon
        //setIcon(R.drawable.ic_launcher);
        // set title
        setTitle(R.string.ask_download_notebook_title);
        // set content
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View aboutContentView = inflater.inflate(R.layout.save_notebook_dialog, null);
        setView(aboutContentView);
        // add yes button
        setButton(BUTTON_POSITIVE, getContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final CheckBox downloadNoteBookAlways = (CheckBox) findViewById(R.id.downloadNoteBookAlways);
                Controller.getInstance().getPreferencesManager().setDownloadNoteBookAlways(downloadNoteBookAlways.isChecked());
                resultListener.onConfirm();
                dialog.dismiss();
            }
        });
        // add no button
        setButton(BUTTON_NEGATIVE, getContext().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        setCancelable(true);

        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "OnCreate");
    }
}