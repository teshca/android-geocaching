package su.geocaching.android.model.datastorage;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import su.geocaching.android.ui.R;

public class DownloadWebNotebookTask extends AsyncTask<String, Void, String> {
    private static final String HTTP_PDA_GEOCACHING_SU = "http://pda.geocaching.su/";
    private DbManager dbManager;
    private boolean isCacheStoredInDataBase;
    private int idCache;
    private Context context;
    private ProgressDialog progressDialog;
    private WebView webView;
    public DownloadWebNotebookTask(DbManager db, boolean isCacheStoredInDataBase, int idCache, Context context, WebView web) {
        this.dbManager = db;
        this.isCacheStoredInDataBase = isCacheStoredInDataBase;
        this.idCache = idCache;
        this.context = context;
        this.webView = web;
    }
    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Скачивание дневника");
        progressDialog.show();
    };
    @Override
    protected String doInBackground(String... params) {
        if (params[0] == null || params[0] == "") {
            if (isCacheStoredInDataBase) {
                dbManager.openDB();
                params[0] = dbManager.getWebNotebookTextById(idCache);
                dbManager.closeDB();
            }
            if (params[0] == null || params[0] == "") {
                try {
                    params[0] = getWebText(idCache);
                    if (isCacheStoredInDataBase) {
                        dbManager.openDB();
                        dbManager.ubdateNotebookText(idCache, params[0]);
                        dbManager.closeDB();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
        if (params[0] == null) {
            webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + context.getString(R.string.notebook_geocache_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
        } else {
            webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, params[0], "text/html", "utf-8", "");
        }
            return params[0];
        

    }

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        String html2 = "";
        URL url = new URL("http://pda.geocaching.su/note.php?cid=" + id + "&mode=0");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "windows-1251"));

        html.append(in.readLine());
        html.append(in.readLine());
        html.append(in.readLine().replace("windows-1251", "utf-8"));

        while ((html2 = in.readLine()) != null) {
            html.append(html2);
        }

        return html.toString();

    }
    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        progressDialog.dismiss();
        super.onPostExecute(result);
    }
}
