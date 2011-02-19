package su.geocaching.android.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import su.geocaching.android.ui.geocachemap.ConnectionManager;
import su.geocaching.android.ui.geocachemap.IInternetAware;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.utils.UiHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
/**
 * 
 * 
 * @author Alekseenko Vladimir
 * 
 */
public class ShowGeoCacheInfo extends Activity implements OnCheckedChangeListener, OnClickListener, IInternetAware {
    private static final String TAG = ShowGeoCacheInfo.class.getCanonicalName();
    private WebView webView;
    private TextView tvNameText;
    private TextView tvTypeGeoCacheText;
    private TextView tvStatusGeoCacheText;
    private ImageView btGoToSearchGeoCache;
    private CheckBox cbAddDelCache;
    private ImageView ivImageTypeGeoCache;
    private DbManager dbm = null;
    private GeoCache GeoCacheForShowInfo;
    private String htmlTextGeoCache;
    private boolean isCacheStoredInDataBase;
    private ConnectionManager connectManag;
    private GoogleAnalyticsTracker tracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.info_geocach_activity);

	if (dbm == null) {
	    dbm = new DbManager(getApplicationContext());
	}

	webView = (WebView) findViewById(R.id.info_web_brouse);
	btGoToSearchGeoCache = (ImageView) findViewById(R.id.info_geocach_Go_button);
	cbAddDelCache = (CheckBox) findViewById(R.id.info_geocache_add_del);
	tvNameText = (TextView) findViewById(R.id.info_text_name);
	tvTypeGeoCacheText = (TextView) findViewById(R.id.info_GeoCache_type);
	tvStatusGeoCacheText = (TextView) findViewById(R.id.info_GeoCache_status);

	webView.getSettings().setJavaScriptEnabled(true);
	webView.setWebViewClient(new MyWebClient());

    tracker = GoogleAnalyticsTracker.getInstance();
    tracker.start("UA-20327116-3", this);
    tracker.trackPageView("/showGeoGacheActivity");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	if (isChecked) {
	    dbm.openDB();
	    dbm.addGeoCache(GeoCacheForShowInfo, htmlTextGeoCache);
	    dbm.closeDB();
	} else {
	    dbm.openDB();
	    dbm.deleteCacheById(GeoCacheForShowInfo.getId());
	    dbm.closeDB();
	}

    }

    @Override
    protected void onStop() {
	connectManag.removeSubscriber(this);
	tracker.stop();
	super.onStop();
    }

    @Override
    protected void onStart() {
	GeoCacheForShowInfo = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

	connectManag = Controller.getInstance().getConnectionManager(this);
	connectManag.addSubscriber(this);

	dbm.openDB();
	isCacheStoredInDataBase = (dbm.getCacheByID(GeoCacheForShowInfo.getId()) != null);
	dbm.closeDB();

	tracker.start("UA-20327116-3", this);
	tracker.trackPageView("/ShowGeoCacheActivity");
	
	if (isCacheStoredInDataBase) {
	    cbAddDelCache.setChecked(true);
	    dbm.openDB();
	    htmlTextGeoCache = dbm.getWebTextById(GeoCacheForShowInfo.getId());
	    dbm.closeDB();
	} else {
	    try {
		htmlTextGeoCache = getWebText(GeoCacheForShowInfo.getId());
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    
	}

	

	tvNameText.setText(GeoCacheForShowInfo.getName());

	switch (GeoCacheForShowInfo.getStatus()) {
	case VALID:
	    tvStatusGeoCacheText.setText(getString(R.string.status_geocache_valid));
	    break;
	case NOT_VALID:
	    tvStatusGeoCacheText.setText(getString(R.string.status_geocache_no_valid));
	    break;
	case NOT_CONFIRMED:
	    tvStatusGeoCacheText.setText(getString(R.string.status_geocache_no_confirmed));
	    break;
	default:
	    tvStatusGeoCacheText.setText(getString(R.string.status_geocache_no_confirmed));
	    break;
	}

	
	
	switch (GeoCacheForShowInfo.getType()) {
	case TRADITIONAL:
	    tvTypeGeoCacheText.setText(getString(R.string.type_geocache_traditional));
	    break;
	case VIRTUAL:
	    tvTypeGeoCacheText.setText(getString(R.string.type_geocache_virtua));
	    break;
	case STEP_BY_STEP:
	    tvTypeGeoCacheText.setText(getString(R.string.type_geocache_step_by_step));
	    break;
	case EVENT:
	    tvTypeGeoCacheText.setText(getString(R.string.type_geocache_event));
	    break;
	case EXTREME:
	    tvTypeGeoCacheText.setText(getString(R.string.type_geocache_extreme));
	    break;
	default:
	    tvTypeGeoCacheText.setText("???");
	    break;
	}

	cbAddDelCache.setOnCheckedChangeListener(this);
	btGoToSearchGeoCache.setOnClickListener(this);

	if (!connectManag.isInternetConnected()) {
	    if (!isCacheStoredInDataBase) {
		webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
	    } else {
		webView.loadDataWithBaseURL("http://pda.geocaching.su/", htmlTextGeoCache, "text/html", "utf-8", "");// (htmlTextGeoCache,
														     // "text/html",
														     // "utf-8");
	    }

	} else {
	    if (!isCacheStoredInDataBase) {
		webView.loadDataWithBaseURL("http://pda.geocaching.su/", htmlTextGeoCache, "text/html", "utf-8", "");// loadData(htmlTextGeoCache,
														     // "text/html",
														     // "utf-8");
	    } else {
		webView.loadDataWithBaseURL("http://pda.geocaching.su/", htmlTextGeoCache, "text/html", "utf-8", "");// loadData(htmlTextGeoCache,
														     // "text/html",
														     // "utf-8");
	    }

	}

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

    private String getWebText(int id) throws IOException {
	StringBuilder html = new StringBuilder();
	String html2 = "";
	URL url = new URL("http://pda.geocaching.su/cache.php?cid=" + id);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "windows-1251"));

	html.append(in.readLine());
	html.append(in.readLine());
	html.append(in.readLine().replace("windows-1251", "utf-8"));

	while ((html2 = in.readLine()) != null) {
	    html.append(html2);
	}

	return html.toString();

    }
    
    public void onHomeClick(View v) {
	UiHelper.goHome(this);
    }
    

    /**
     * This class need for
     * 
     * 
     */
    private class MyWebClient extends WebViewClient {
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    view.loadUrl(url);
	    return true;
	}
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
    
}