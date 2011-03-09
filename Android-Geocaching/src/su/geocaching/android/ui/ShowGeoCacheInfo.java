package su.geocaching.android.ui;

import java.util.concurrent.ExecutionException;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datastorage.DownloadInfoCacheTask;
import su.geocaching.android.model.datastorage.DownloadWebNotebookTask;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.geocachemap.ConnectionManager;
import su.geocaching.android.ui.geocachemap.IInternetAware;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.utils.UiHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * 
 * 
 * @author Alekseenko Vladimir
 * 
 */
public class ShowGeoCacheInfo extends Activity implements OnCheckedChangeListener, OnClickListener, IInternetAware {
	private static final String TAG = ShowGeoCacheInfo.class.getCanonicalName();
	private static final String HTTP_PDA_GEOCACHING_SU = "http://pda.geocaching.su/";
	private WebView webView;
	private TextView tvNameText;
	private TextView tvTypeGeoCacheText;
	private TextView tvStatusGeoCacheText;
	private ImageView btGoToSearchGeoCache;
	private CheckBox cbAddDelCache;
	private DbManager dbm;
	private Menu menuInfo;
	private GeoCache GeoCacheForShowInfo;
	private String htmlTextGeoCache = null;
	private String htmlTextNotebookGeoCache = null;
	private boolean isCacheStoredInDataBase;
	private ConnectionManager connectManager;
	private GoogleAnalyticsTracker tracker;
	private boolean isPageNoteBook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_geocach_activity);
		dbm = new DbManager(getApplicationContext());

		webView = (WebView) findViewById(R.id.info_web_brouse);
		btGoToSearchGeoCache = (ImageView) findViewById(R.id.info_geocach_Go_button);
		cbAddDelCache = (CheckBox) findViewById(R.id.info_geocache_add_del);
		tvNameText = (TextView) findViewById(R.id.info_text_name);
		tvTypeGeoCacheText = (TextView) findViewById(R.id.info_GeoCache_type);
		tvStatusGeoCacheText = (TextView) findViewById(R.id.info_GeoCache_status);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());
		isPageNoteBook = false;
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(getString(R.string.id_Google_Analytics), this);
		tracker.trackPageView(getString(R.string.geocache_info_activity_folder));
		tracker.dispatch();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			isCacheStoredInDataBase = true;
			if (htmlTextNotebookGeoCache == null || htmlTextNotebookGeoCache == "") {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(this.getString(R.string.ask_download_notebook)).setCancelable(false).setPositiveButton(this.getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						htmlTextNotebookGeoCache = getHtmlString(!isPageNoteBook);
						dialog.cancel();
					}
				}).setNegativeButton(this.getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog askDownloadNotebook = builder.create();
				askDownloadNotebook.show();
			}
			dbm.openDB();
			dbm.addGeoCache(GeoCacheForShowInfo, htmlTextGeoCache, htmlTextNotebookGeoCache);
			dbm.closeDB();

		} else {
			dbm.openDB();
			dbm.deleteCacheById(GeoCacheForShowInfo.getId());
			dbm.closeDB();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		webView.setKeepScreenOn(Controller.getInstance().getKeepScreenOnPreference(webView.getContext()));
	}

	@Override
	protected void onStop() {
		connectManager.removeSubscriber(this);
		tracker.stop();
		super.onStop();
	}

	@Override
	protected void onStart() {
		GeoCacheForShowInfo = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

		connectManager = Controller.getInstance().getConnectionManager();
		connectManager.addSubscriber(this);

		dbm.openDB();
		isCacheStoredInDataBase = (dbm.getCacheByID(GeoCacheForShowInfo.getId()) != null);
		if (isCacheStoredInDataBase) {
			cbAddDelCache.setChecked(true);
			htmlTextGeoCache = dbm.getWebTextById(GeoCacheForShowInfo.getId());
			htmlTextNotebookGeoCache = dbm.getWebNotebookTextById(GeoCacheForShowInfo.getId());
		}
		dbm.close();

		tvNameText.setText(GeoCacheForShowInfo.getName());
		tvStatusGeoCacheText.setText(Controller.getInstance().getResourceManager().getGeoCacheStatus(GeoCacheForShowInfo));
		tvTypeGeoCacheText.setText(Controller.getInstance().getResourceManager().getGeoCacheType(GeoCacheForShowInfo));

		cbAddDelCache.setOnCheckedChangeListener(this);
		btGoToSearchGeoCache.setOnClickListener(this);

		if (!connectManager.isInternetConnected() && !isCacheStoredInDataBase)
			webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
		else
			htmlTextGeoCache = getHtmlString(isPageNoteBook);
		webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, htmlTextGeoCache, "text/html", "utf-8", "");
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		if (!isCacheStoredInDataBase) {
			cbAddDelCache.setChecked(true);
		}
		Intent intent = new Intent(this, SearchGeoCacheMap.class);
		intent.putExtra(GeoCache.class.getCanonicalName(), GeoCacheForShowInfo);
		startActivity(intent);
	}

	public void onHomeClick(View v) {
		UiHelper.goHome(this);
	}

	@Override
	public void onInternetLost() {
		btGoToSearchGeoCache.setOnClickListener(null);
		Toast.makeText(getBaseContext(), getString(R.string.select_geocache_status_without_internet), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInternetFound() {
		btGoToSearchGeoCache.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.info_about_cache, menu);
		menuInfo = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.show_web_notebook_cache: {
			changeMenuItem(item);
			return true;
		}
		case R.id.show_web_add_delete_cache: {

			if (cbAddDelCache.isChecked()) {
				cbAddDelCache.setChecked(false);
			} else {
				cbAddDelCache.setChecked(true);
			}

			return true;
		}
		case R.id.show_web_search_cache: {
			if (!isCacheStoredInDataBase) {
				cbAddDelCache.setChecked(true);
			}
			Intent intent = new Intent(this, SearchGeoCacheMap.class);
			intent.putExtra(GeoCache.class.getCanonicalName(), GeoCacheForShowInfo);
			startActivity(intent);
			return true;
		}

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean onPrepareOptionsMenu(Menu menu) {

		if (cbAddDelCache.isChecked()) {

			menu.getItem(1).setTitle(R.string.menu_show_web_delete_cache);
		} else {
			menu.getItem(1).setTitle(R.string.menu_show_web_add_cache);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	public void changeMenuItem(MenuItem item) {
		if (!connectManager.isInternetConnected() && !isCacheStoredInDataBase)
			webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
		else {
			if (!isPageNoteBook) {
				item.setTitle(R.string.menu_show_info_cache);
				isPageNoteBook = true;
				htmlTextNotebookGeoCache = getHtmlString(isPageNoteBook);
				if (htmlTextNotebookGeoCache == null)
					webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.notebook_geocache_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
				else
					webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, htmlTextNotebookGeoCache, "text/html", "utf-8", "");

			} else {
				isPageNoteBook = false;
				item.setTitle(R.string.menu_show_web_notebook_cache);
				htmlTextGeoCache = getHtmlString(isPageNoteBook);
				webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, htmlTextGeoCache, "text/html", "utf-8", "");
			}
		}
	}

	private String getHtmlString(boolean isPageNoteBook) {
		String exString = "";
		if (!isPageNoteBook)
			try {
				exString = new DownloadInfoCacheTask(dbm, isCacheStoredInDataBase, GeoCacheForShowInfo.getId()).execute(htmlTextGeoCache).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else {
			try {
				exString = new DownloadWebNotebookTask(dbm, isCacheStoredInDataBase, GeoCacheForShowInfo.getId()).execute(htmlTextNotebookGeoCache).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return exString;

	}
}