package su.geocaching.android.view.userstory.incocach;

import su.geocaching.android.ui.R;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Info_cach extends Activity implements OnClickListener {
    private WebView wvWebrouse;
    private TextView tvNametext;
    private Display dsDis;
    private Button btGo;
    private ImageButton ibAddDelCach;
    
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.info_geocach_activity);
	
	wvWebrouse=(WebView)findViewById(R.id.info_web_brouse);
	btGo=(Button)findViewById(R.id.info_geocach_Go_button);
//	ibAddDelCach=(ImageButton)findViewById(R.id.td);
	
	tvNametext=(TextView)findViewById(R.id.info_text_name);
	dsDis=((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay(); 
	wvWebrouse.setLayoutParams(new LayoutParams(dsDis.getWidth(),dsDis.getHeight()-dsDis.getHeight()/3));
	
	btGo.setOnClickListener(this);
	ibAddDelCach.setOnClickListener(this);
	
	wvWebrouse.setWebViewClient(new WVclient());
	wvWebrouse.loadUrl("http://pda.geocaching.su/cache.php?cid=112");
    }

    private class WVclient extends WebViewClient{
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    view.loadUrl(url);
	    return true;
	}
    }

    @Override
    public void onClick(View v) {
	Log.d("Begin Click", v.toString());
	if(v.equals(btGo)){
	    Intent intent = new Intent(this, SearchGeoCacheMap.class);
		//intent.putExtra(DEFAULT_GEOCACHE_ID_NAME, DEFAULT_GEOCACHE_ID_VALUE);
		intent.putExtra("layout", R.layout.search_geocache_map);
		intent.putExtra("mapID", R.id.searchGeocacheMap);
		
		
		
		startActivity(intent);
		this.finish();
	}
	if(v.equals(ibAddDelCach)){
	    Log.d("OnClick","++Begin");
	    ibAddDelCach.setImageResource(R.drawable.jean_victor_balin_icon_star);
	    //TODO: Write add\remove cach in DB
	     
	     
	    
	}
	
    }
}
