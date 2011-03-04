package su.geocaching.android.model.datastorage;

import su.geocaching.android.model.datastorage.DbManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import android.text.Html;
import android.util.Log;
import android.webkit.WebView;
import su.geocaching.android.model.datatype.GeoCache;
import android.os.AsyncTask;

public class DownloadWebNotebookTask extends AsyncTask<String, Void, String> {
	private DbManager dbManager;
	private boolean isCacheStoredInDataBase;
	private int idCache;
	private String htmlNotebookTextGeoCache = "";

	public DownloadWebNotebookTask(DbManager db, boolean isCacheStoredInDataBase, int idCache) {
		this.dbManager = db;
		this.isCacheStoredInDataBase = isCacheStoredInDataBase;
		this.idCache = idCache;
	}

	@Override
	protected String doInBackground(String... params) {
		if (params[0] == null || params[0] == "") {
			if (isCacheStoredInDataBase) {
				dbManager.openDB();
				htmlNotebookTextGeoCache = dbManager.getWebNotebookTextById(idCache);
				dbManager.closeDB();
			}
			if (htmlNotebookTextGeoCache == null || htmlNotebookTextGeoCache == "") {
				try {
					htmlNotebookTextGeoCache = getWebText(idCache);
					if (isCacheStoredInDataBase){
						dbManager.openDB();
						GeoCache tempCache = dbManager.getCacheByID(idCache);
						String htmlInfo = dbManager.getWebTextById(idCache);
						dbManager.deleteCacheById(idCache);
						dbManager.addGeoCache(tempCache, htmlInfo, htmlNotebookTextGeoCache);
						dbManager.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			return htmlNotebookTextGeoCache;
		} else {
			return params[0];
		}

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
}
