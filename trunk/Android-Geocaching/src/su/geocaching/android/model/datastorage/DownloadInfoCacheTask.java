package su.geocaching.android.model.datastorage;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import su.geocaching.android.ui.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class DownloadInfoCacheTask extends AsyncTask<String, Integer, String> {

    private DbManager dbManager;
    private boolean isCacheStoredInDataBase;
    private ProgressDialog prDialog;
    private int idCache;
    private Context context;

    public DownloadInfoCacheTask(DbManager db, boolean isCacheStoredInDataBase, int idCache, Context context) {
        this.dbManager = db;
        this.isCacheStoredInDataBase = isCacheStoredInDataBase;
        this.idCache = idCache;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        prDialog = new ProgressDialog(context);
        prDialog.setMessage(context.getString(R.string.download_info));
        prDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        if (params[0] == null || params[0].equals("")) {
            if (isCacheStoredInDataBase) {
                params[0] = dbManager.getWebTextById(idCache);
            } else
                try {
                    params[0] = getWebText(idCache);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return params[0];
    }

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        String html2;
        URL url = new URL("http://pda.geocaching.su/cache.php?cid=" + id + "&mode=0");
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
        prDialog.dismiss();
        super.onPostExecute(result);
    }
}
