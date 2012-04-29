package su.geocaching.android.ui.info;

/**
 * Activity to display cache's information, notebook and photos
 *
 * @author Nickolay Artamonov
 */

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;

import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

public class AdvancedInfoActivity extends FragmentActivity {
    ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;
    
    private InfoViewModel infoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        infoViewModel = Controller.getInstance().getInfoViewModel(); 
        GeoCache geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        infoViewModel.SetGeoCache(geoCache.getId());

        setContentView(su.geocaching.android.ui.R.layout.advanced_info_activity);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.Tab infoTab = getSupportActionBar().newTab().setText(R.string.info_tab_name_info);
        ActionBar.Tab notebookTab = getSupportActionBar().newTab().setText(R.string.info_tab_name_notebook);
        ActionBar.Tab photoTab = getSupportActionBar().newTab().setText(R.string.info_tab_name_photo);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2); // always keep all 3 fragments available for performance reason
        mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);
        mTabsAdapter.addTab(infoTab, InfoFragment.class);
        mTabsAdapter.addTab(notebookTab, NotebookFragment.class);
        mTabsAdapter.addTab(photoTab, PhotoFragment.class);
    }
    
    public void navigateToInfoTab() {
        getSupportActionBar().setSelectedNavigationItem(infoViewModel.getInfoState().getIndex());       
    }

    public void naviagteToNotebookTab() {
        getSupportActionBar().setSelectedNavigationItem(infoViewModel.getNotebookState().getIndex());        
    }

    public void naviagteToPhotosTab() {
        getSupportActionBar().setSelectedNavigationItem(infoViewModel.getPhotosState().getIndex());        
    }    
    
    public void openCheckpointDialog(GeoPoint geoPoint) {
        
        if (!infoViewModel.isCacheStored()) {
            Toast.makeText(this, R.string.ask_add_cache_in_db, Toast.LENGTH_LONG).show();
            return;
        }

        GeoCache checkpoint = new GeoCache();
        checkpoint.setId(infoViewModel.getGeoCachceId());
        checkpoint.setType(GeoCacheType.CHECKPOINT);
        checkpoint.setLocationGeoPoint(geoPoint);
        NavigationManager.startCreateCheckpointActivity(this, checkpoint);      
    }
    
    @Override
    public void onResume() {
        super.onResume();
        infoViewModel.registerActivity(this);
        getSupportActionBar().setSelectedNavigationItem(infoViewModel.getSelectedTabIndex());
    }
    
    @Override
    public void onPause() {
        super.onPause();
        infoViewModel.unregisterActivity(this);
        infoViewModel.setSelectedTabIndex(getSupportActionBar().getSelectedNavigationIndex());
    }
    
    private AbstractWebViewFragment getInfoFragment() {
        return (AbstractWebViewFragment) mTabsAdapter.getFragment(infoViewModel.getInfoState().getIndex());        
    }
        
    public void showInfoProgressBar() {
        AbstractWebViewFragment webViewFragment = getInfoFragment();
        if (webViewFragment != null) {
            webViewFragment.showProgressBar();            
        }        
    }
    
    public void hideInfoProgressBar() {
        AbstractWebViewFragment webViewFragment = getInfoFragment();
        if (webViewFragment != null) {
            webViewFragment.hideProgressBar();
        }
    }
    
    public void showInfoErrorMessage() {
        AbstractWebViewFragment webViewFragment = getInfoFragment();
        if (webViewFragment != null) {
            webViewFragment.showErrorMessage();            
        }
    }
    
    public void hideInfoErrorMessage() {
        AbstractWebViewFragment webViewFragment = getInfoFragment();
        if (webViewFragment != null) {        
            webViewFragment.hideErrorMessage();
        }
    }    
    
    public void setInfoText(String text) {
        AbstractWebViewFragment webViewFragment = getInfoFragment();
        if (webViewFragment != null) {
            webViewFragment.setWebViewData(text);
        }
    }    

    private AbstractWebViewFragment getNotebookFragment() {
        return (AbstractWebViewFragment) mTabsAdapter.getFragment(infoViewModel.getNotebookState().getIndex());        
    }
        
    public void showNotebookProgressBar() {
        AbstractWebViewFragment webViewFragment = getNotebookFragment();
        if (webViewFragment != null) {
            webViewFragment.showProgressBar();            
        }        
    }
    
    public void hideNotebookProgressBar() {
        AbstractWebViewFragment webViewFragment = getNotebookFragment();
        if (webViewFragment != null) {        
            webViewFragment.hideProgressBar();
        }
    }
    
    public void showNotebookErrorMessage() {
        AbstractWebViewFragment webViewFragment = getNotebookFragment();
        if (webViewFragment != null) {
            webViewFragment.showErrorMessage();            
        }
    }
    
    public void hideNotebookErrorMessage() {
        AbstractWebViewFragment webViewFragment = getNotebookFragment();
        if (webViewFragment != null) {        
            webViewFragment.hideErrorMessage();
        }
    }     
    
    public void setNotebookText(String text) {
        AbstractWebViewFragment webViewFragment = getNotebookFragment();
        if (webViewFragment != null) {
            webViewFragment.setWebViewData(text);
        }
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
        private final FragmentActivity mActivity;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<String> mTabs = new ArrayList<String>();

        public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mActivity = activity;
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
        
        private String getFragmentName(int position) {
            return makeFragmentName(mViewPager.getId(), position);
        }
        
        public Fragment getFragment(int position) {
            return mActivity.getSupportFragmentManager().findFragmentByTag(getFragmentName(position));    
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            return Fragment.instantiate(mActivity, mTabs.get(position), null);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
            Fragment fragment = getFragment(position);
            if (fragment instanceof IInfoFragment) {
                ((IInfoFragment) fragment).onNavigatedTo();
            }             
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
}
