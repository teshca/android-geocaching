package su.geocaching.android.ui;

import java.util.concurrent.ExecutionException;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datastorage.DownloadWebNotebookTask;
import su.geocaching.android.model.datatype.GeoCache;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DialogDownloadNotebook extends Dialog {

    private static final String TAG = DialogDownloadNotebook.class.getCanonicalName();

    private CheckBox checkBox;
    private Context context;
    private AsyncTask<Void, Void, String> infoTask;
    private GeoCache cache;

    public DialogDownloadNotebook(Context context) {
        super(context);
        setContentView(R.layout.custom_dialog_in_info_activity);
        setCancelable(true);
        setTitle(R.string.ask_download_notebook_title);
        checkBox = (CheckBox) findViewById(R.id.downloadNoteBookAlways);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Controller.getInstance().getPreferencesManager().setDownloadNoteBookAlways(isChecked);
            }
        });
    }

    public DialogDownloadNotebook(Context context, AsyncTask<Void, Void, String> infoTask, GeoCache cache) {
        this(context);
        this.context = context;
        this.cache = cache;
        this.infoTask = infoTask;
    }


    public void onYesClick(View v) {
        AsyncTask<Void, Void, String> notebookTask = new DownloadWebNotebookTask(context, cache.getId(), 0, 0, null, "").execute();
        try {
            Controller.getInstance().getDbManager().addGeoCache(cache, infoTask.get(), notebookTask.get());
        } catch (InterruptedException e) {
            LogManager.e(TAG, "InterruptedException", e);
        } catch (ExecutionException e) {
            LogManager.e(TAG, "ExecutionException", e);
        }
        dismiss();
    }

    public void onNoClick() {
        try {
            Controller.getInstance().getDbManager().addGeoCache(cache, infoTask.get(), "");
        } catch (InterruptedException e) {
            LogManager.e(TAG, "InterruptedException", e);
        } catch (ExecutionException e) {
            LogManager.e(TAG, "ExecutionException", e);
        }
        dismiss();
    }
}
