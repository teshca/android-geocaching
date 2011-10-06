package su.geocaching.android.ui.info;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.DownloadInfoTask.DownloadInfoState;
import su.geocaching.android.controller.managers.LogManager;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import su.geocaching.android.ui.R;

public class DownloadNotebookDialog extends Dialog {

    private static final String TAG = DownloadNotebookDialog.class.getCanonicalName();

    public DownloadNotebookDialog(final Context context, final InfoActivity infoActivity, final int cacheId) {
        super(context);
        LogManager.d(TAG, "New DownloadNotebookDialog created");

        setContentView(R.layout.save_notebook_dialog);
        setTitle(R.string.ask_download_notebook_title);
        setCancelable(true);

        CheckBox cbDownloadAlways = (CheckBox) findViewById(R.id.downloadNoteBookAlways);
        cbDownloadAlways.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Controller.getInstance().getPreferencesManager().setDownloadNoteBookAlways(isChecked);
            }
        });

        Button buttonYes = (Button) findViewById(R.id.ButtonYes);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller.getInstance().getApiManager().getInfo(context, DownloadInfoState.SAVE_CACHE_NOTEBOOK, infoActivity, cacheId);
                dismiss();
            }
        });

        Button buttonNo = (Button) findViewById(R.id.ButtonNo);
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
