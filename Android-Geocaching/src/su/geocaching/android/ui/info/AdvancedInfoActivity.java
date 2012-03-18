package su.geocaching.android.ui.info;

/**
 * Activity to display cache's information, notebook and photos
 *
 * @author Nickolay Artamonov
 */

import java.util.ArrayList;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.GeocachingSuApiManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;


public class AdvancedInfoActivity extends FragmentActivity {
    ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;
    
    private InfoViewModel infoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        infoViewModel = Controller.getInstance().getInfoViewModel(); 
        GeoCache geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        infoViewModel.SetGeoCache(geoCache);

        setContentView(su.geocaching.android.ui.R.layout.advanced_info_activity);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.Tab infoTab = getSupportActionBar().newTab().setText(R.string.info_tab_name_info);
        ActionBar.Tab notebookTab = getSupportActionBar().newTab().setText(R.string.info_tab_name_notebook);
        ActionBar.Tab photoTab = getSupportActionBar().newTab().setText(R.string.info_tab_name_photo);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);
        mTabsAdapter.addTab(infoTab, AdvancedInfoActivity.InfoInfoFragment.class);
        mTabsAdapter.addTab(notebookTab, AdvancedInfoActivity.InfoInfoFragment.class);
        mTabsAdapter.addTab(photoTab, AdvancedInfoActivity.InfoInfoFragment.class);

        if (savedInstanceState != null) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("index"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index", getSupportActionBar().getSelectedNavigationIndex());
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, ActionBar.TabListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<String> mTabs = new ArrayList<String>();

        public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mActionBar = actionBar;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss) {
            mTabs.add(clss.getName());
            mActionBar.addTab(tab.setTabListener(this));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            return Fragment.instantiate(mContext, mTabs.get(position), null);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }    
    }
    
    public static class InfoInfoFragment extends Fragment {      
        private WebView webView;
        private InfoViewModel infoViewModel;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            infoViewModel = Controller.getInstance().getInfoViewModel();            
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.info_info_fragment, container, false);
            webView = (WebView) v.findViewById(R.id.info_web_brouse);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            return v;
        }        
        
        @Override 
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setWebViewData(infoViewModel.getInformation());            
        }      
        
        private void setWebViewData(String info) {
            webView.loadDataWithBaseURL(GeocachingSuApiManager.HTTP_PDA_GEOCACHING_SU, info, "text/html", GeocachingSuApiManager.UTF8_ENCODING, null);
        }        
    }
}
