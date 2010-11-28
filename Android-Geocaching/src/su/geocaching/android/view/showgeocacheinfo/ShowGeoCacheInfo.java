package su.geocaching.android.view.showgeocacheinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;

import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ShowGeoCacheInfo extends Activity implements OnCheckedChangeListener, OnClickListener {
    private WebView wvWebrouse;
    private TextView tvNametext;
    private Display dsDis;
    private ImageButton btGo;
    private CheckBox cbAddDelCache;
    private DbManager dbm = null;
    private NetworkInfo wifi;
    private GeoCache cache;
    private String htmlTextGeoCache = "default text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.info_geocach_activity);

	initialize();

	dbm.openDB();

	cbAddDelCache.setOnCheckedChangeListener(this);
	btGo.setOnClickListener(this);

	wvWebrouse.setWebViewClient(new WVclient());
	    }

    private class WVclient extends WebViewClient {
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    view.loadUrl(url);
	    return true;
	}
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	if (isChecked) {
	    if (dbm.getCacheByID(cache.getId()) == null)
		dbm.addGeoCache(cache, htmlTextGeoCache);
	} else {
	    dbm.deleteCacheById(cache.getId());
	}

    }

    @Override
    protected void onStop() {
	dbm.closeDB();
	super.onStop();
    }

    @Override
    protected void onStart() {
       dbm.openDB();
       cache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

	if (dbm.getCacheByID(cache.getId()) != null) {
	    Log.d("ShowGeoCacheInfo", "Cache in BD");
	    cbAddDelCache.setChecked(true);
	    htmlTextGeoCache = dbm.getWebTextById(cache.getId());
	    Log.d("Cache in BD html text:", htmlTextGeoCache);
	} else {
	    Log.d("ShowGeoCacheInfo", "Cache NOT BD");
	    try {
		htmlTextGeoCache = getWebText(cache.getId());
		  Log.d("ShowGeoCacheInfo", htmlTextGeoCache);
	    } catch (IOException e) {
		Log.d("getWebText", "IOExeption :" + e.toString());
		e.printStackTrace();
	    }
	}

	tvNametext.setText(cache.getName());

	if (!wifi.isAvailable()) {
	    if (dbm.getCacheByID(cache.getId()) == null) {
		Log.d("Info Cache", "Internet - NOT + BD NOT have cache");
		wvWebrouse.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
	    } else {
		Log.d("Info Cache", "Internet - NOT + BD HAVE cache");
		wvWebrouse.loadData("<?xml version='1.0' encoding='UTF-8'?>" +htmlTextGeoCache, "text/html", "utf-8");
	    }

	} else {
	    if (dbm.getCacheByID(cache.getId()) == null) {
		Log.d("Info Cache", "Internet - YES + BD NOT have cache");
		wvWebrouse.loadData("<?xml version='1.0' encoding='windows-1251'?>" +htmlTextGeoCache, "text/html", "UTF-8");
	    } else {
		Log.d("Info Cache", "Internet - YES + BD HAVE cache");
		wvWebrouse.loadData(htmlTextGeoCache, "text/html", "cp1251");
		
	    }

	}

        super.onStart();
    }
    @Override
    public void onClick(View v) {
	Intent intent = new Intent(this, SearchGeoCacheMap.class);
	intent.putExtra(GeoCache.class.getCanonicalName(), cache);

	startActivity(intent);
    }

    private void initialize() {
	ConnectivityManager network = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	wifi = network.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

	if (dbm == null) {
	    dbm = new DbManager(getBaseContext());
	}

	wvWebrouse = (WebView) findViewById(R.id.info_web_brouse);
	btGo = (ImageButton) findViewById(R.id.info_geocach_Go_button);
	cbAddDelCache = (CheckBox) findViewById(R.id.info_geocache_add_del);

	tvNametext = (TextView) findViewById(R.id.info_text_name);
	dsDis = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	wvWebrouse.setLayoutParams(new LayoutParams(dsDis.getWidth(), dsDis.getHeight()));
    }

    private String getWebText(int id) throws IOException {
	String html = "";
	String html2 = "";
	URL url = new URL("http://pda.geocaching.su/cache.php?cid=" + id);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	while ((html2 = in.readLine()) != null) {
	   html += html2;
	   
	}
	
	return html;

    }
}