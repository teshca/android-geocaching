package su.geocaching.android.view.showgeocacheinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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

    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.info_geocach_activity);

	initialize();
	dbm.openDB();

	cbAddDelCache.setOnCheckedChangeListener(this);
	btGo.setOnClickListener(this);
	btGo.setEnabled(false);
	
	wvWebrouse.setWebViewClient(new WVclient());
	cache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

	if (dbm.getCacheByID(cache.getId()) != null) {
	    cbAddDelCache.setChecked(true);
	}
	tvNametext.setText(cache.getName());

	if (!wifi.isAvailable()) {
	    if (dbm.getCacheByID(cache.getId()) == null) {
		Log.d("Info Cache", "Internet - NOT + BD NOT have cache");
		wvWebrouse.loadData("<?xml version='1.0' encoding='UTF-8' ?><center>"
			+ "<p>Невозможно показать информацию по тайнику.</p><p>Тайника нет в базе.</p><p>Соединение с интернетом отсутствует.</p></center>", "text/html", "UTF-8");
	    } else {
		Log.d("Info Cache", "Internet - NOT + BD HAVE cache");
		wvWebrouse.loadData("<?xml version='1.0' encoding='UTF-8' ?>" + dbm.getWebTextById(cache.getId()), "html/text", "UTF-8");
	    }

	} else {
	    if (dbm.getCacheByID(cache.getId()) == null) {
		Log.d("Info Cache", "Internet - YES + BD NOT have cache");
		wvWebrouse.loadUrl("http://pda.geocaching.su/cache.php?cid=" + cache.getId());
	   
	    } else {
		//TODO: Case if Cache in BD
		Log.d("Info Cache", "Internet - YES + BD HAVE cache");
		//wvWebrouse.loadData("<?xml version='1.0' encoding='windows-1251' ?>" + dbm.getWebTextById(cache.getId()), "html/text", "windows-1251");
		wvWebrouse.loadUrl("http://pda.geocaching.su/cache.php?cid=" + cache.getId());
	    }

	}
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
		//TODO: record html in BD
		    dbm.addGeoCache(cache, "");
	    	btGo.setEnabled(true);
	} else {
	    dbm.deleteCacheById(cache.getId());
	    btGo.setEnabled(false);
	}
	
	
    }

    @Override
    public void onClick(View v) {
	Intent intent = new Intent(this, SearchGeoCacheMap.class);
	intent.putExtra(GeoCache.class.getCanonicalName(), cache);
	
	finish();
	
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
	wvWebrouse.setLayoutParams(new LayoutParams(dsDis.getWidth(), dsDis.getHeight() - dsDis.getHeight() / 3));
    }
    
    private String getWebText(int id) throws ClientProtocolException, IOException{
	//TODO: Поправить кодировки
	HttpClient client = new DefaultHttpClient();
	HttpGet request = new HttpGet("http://pda.geocaching.su/cache.php?cid="+id);
	HttpResponse response = client.execute(request);

	String html = "";
	InputStream in = response.getEntity().getContent();
	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	StringBuilder str = new StringBuilder();
	String line = null;
	while((line = reader.readLine()) != null)
	{
	    str.append(line);
	}
	in.close();
	html = str.toString();
	return html;
    }
}
