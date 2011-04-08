package su.geocaching.android.ui;

import java.util.concurrent.ExecutionException;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datastorage.DownloadInfoOrNotebookCacheTask;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.GeoCacheInfoActivity.PageType;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DialogDownloadNotebook extends Dialog {

    private static final String TAG = DialogDownloadNotebook.class.getCanonicalName();

    private CheckBox checkBox;
    private Context thisContext;
    private AsyncTask<Void, Void, String> infoTask;
    private GeoCache cache;
    private Button buttonYes;
    private Button buttonNo;

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
        buttonYes = (Button) findViewById(R.id.ButtonYes);
        setClickListenerButtonYes();
        buttonNo = (Button) findViewById(R.id.ButtonNo);
        setClickListenerButtonNo();

    }

    public DialogDownloadNotebook(Context context, AsyncTask<Void, Void, String> infoTask, GeoCache cache) {
        this(context);
        this.thisContext = context;
        this.cache = cache;
        this.infoTask = infoTask;
    }

    public void setClickListenerButtonNo() {
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Controller.getInstance().getDbManager().addGeoCache(cache, infoTask.get(), "");
                } catch (InterruptedException e) {
                    LogManager.e(TAG, "InterruptedException", e);
                } catch (ExecutionException e) {
                    LogManager.e(TAG, "ExecutionException", e);
                }
                dismiss();
            }
        });
    }

    public void setClickListenerButtonYes() {
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask<Void, Void, String> notebookTask = new DownloadInfoOrNotebookCacheTask(thisContext, cache.getId(), 0, 0, null, "", PageType.NOTEBOOK).execute();
                try {
                    Controller.getInstance().getDbManager().addGeoCache(cache, infoTask.get(), notebookTask.get());
                } catch (InterruptedException e) {
                    LogManager.e(TAG, "InterruptedException", e);
                } catch (ExecutionException e) {
                    LogManager.e(TAG, "ExecutionException", e);
                }

                dismiss();
            }
        });
    }
}
