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

public class DialogDownloadNotebook {
    private static final String TAG = DialogDownloadNotebook.class.getCanonicalName();
    
    private Dialog dialog;
    private CheckBox checkBox;
    private Context context;
    private AsyncTask<Void, Void, String> notebookTask;
    private AsyncTask<Void, Void, String> infoTask;
    private int  webViewScrollX;
    private int  webViewScrollY;
    private GeoCache cache;

    public DialogDownloadNotebook(Context context, AsyncTask<Void, Void, String> infoTask ,GeoCache cache, int webViewScrollX, int webViewScrollY){
        this.context = context;
        this.cache = cache;
        this.webViewScrollX = webViewScrollX;
        this.webViewScrollY = webViewScrollY;
        this.infoTask = infoTask;
        this.notebookTask = null;
         initViews();
       
    }
     
    private void initViews(){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog_in_info_activity);
        dialog.setCancelable(true);
        dialog.setTitle(R.string.ask_download_notebook_title);
        checkBox = (CheckBox) dialog.findViewById(R.id.downloadNoteBookAlways);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Controller.getInstance().getPreferencesManager().setDownloadNoteBookAlways(isChecked);
            }
        });
    }
       
    public void onYesClick(View v){
        notebookTask = new DownloadWebNotebookTask(context, cache.getId(), webViewScrollX, webViewScrollY, null,"").execute();
        try {
            Controller.getInstance().getDbManager().addGeoCache(cache, infoTask.get(), notebookTask.get());
        } catch (InterruptedException e) {
            LogManager.e(TAG, "InterruptedException", e);
        } catch (ExecutionException e) {
            LogManager.e(TAG, "ExecutionException", e);
        }
        dialog.dismiss();
    }
    
    public void onNoClick(){
        try {
            Controller.getInstance().getDbManager().addGeoCache(cache, infoTask.get(), "");
        } catch (InterruptedException e) {
            LogManager.e(TAG, "InterruptedException", e);
        } catch (ExecutionException e) {
            LogManager.e(TAG, "ExecutionException", e);
        }
        dialog.dismiss();
    }
    
    public void dialogShow(){
        dialog.show();
    }
}


