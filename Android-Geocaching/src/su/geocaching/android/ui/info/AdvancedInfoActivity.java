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
import android.support.v4.view.MenuItem;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.view.Menu;
import android.support.v4.view.ViewPager;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.MenuInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AdvancedInfoActivity extends FragmentActivity {
    ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;
    
    private InfoViewModel infoViewModel;
    
    private static final int REMOVE_CACHE_ALERT_DIALOG_ID = 2;  
    
    private TextView infoTabTextView;
    private TextView notebookTabTextView;
    private TextView photoTabTextView;
    
    private TextView getTabTextView() {
        TextView textView = new TextView(this, null, R.style.GeocachingActionBarTabTextStyle);
        textView.setTextSize(16);
        textView.setLines(1);
        textView.setEllipsize(TruncateAt.MARQUEE);
        textView.setMarqueeRepeatLimit(0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_VERTICAL;
        textView.setLayoutParams(lp);
        return textView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        infoViewModel = Controller.getInstance().getInfoViewModel(); 
        GeoCache geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        infoViewModel.setGeoCache(geoCache);
        
        getSupportActionBar().setTitle(geoCache.getName());
        setContentView(R.layout.advanced_info_activity);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                   
        ActionBar.Tab infoTab = getSupportActionBar().newTab();
        infoTabTextView = getTabTextView();
        infoTabTextView.setText(R.string.info_tab_name_info);
        //infoTab.setText(R.string.info_tab_name_info);
        infoTab.setCustomView(infoTabTextView);
        updateTabTextView(infoTabTextView, infoViewModel.getInfoState().getText() != null);
        
        ActionBar.Tab notebookTab = getSupportActionBar().newTab();      
        notebookTabTextView = getTabTextView();
        notebookTabTextView.setText(R.string.info_tab_name_notebook);
        //notebookTab.setText(R.string.info_tab_name_notebook);
        notebookTab.setCustomView(notebookTabTextView);
        updateTabTextView(notebookTabTextView, infoViewModel.getNotebookState().getText() != null);
        
        ActionBar.Tab photoTab = getSupportActionBar().newTab();
        photoTabTextView = getTabTextView();
        photoTabTextView.setText(R.string.info_tab_name_photo);
        //photoTab.setText(R.string.info_tab_name_photo);
        photoTab.setCustomView(photoTabTextView);
        updateTabTextView(photoTabTextView, infoViewModel.getPhotosState().getPhotos() != null);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2); // always keep all 3 fragments available for performance reason
        mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);
        mTabsAdapter.addTab(infoTab, InfoFragment.class);
        mTabsAdapter.addTab(notebookTab, NotebookFragment.class);
        mTabsAdapter.addTab(photoTab, PhotoFragment.class);
    }
    
    private void updateTabTextView(TextView tabTextView, boolean isDownloaded) {
        if (isDownloaded) {
            tabTextView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            tabTextView.setTextColor(getResources().getColor(R.color.dashboard_text_color));
        } else {
            tabTextView.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            tabTextView.setTextColor(getResources().getColor(R.color.disabled_text_color));                             
        }
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info_menu, menu);       
        return true;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case REMOVE_CACHE_ALERT_DIALOG_ID:
                return new RemoveFavoriteCacheDialog(this, removeCacheListener);
            default:
                return null;
        }
    }
    
    private ConfirmDialogResultListener removeCacheListener = new ConfirmDialogResultListener() {
        public void onConfirm() {
            performDeleteCache();
        }
    };
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       
        if (menu != null) {
            if (infoViewModel.isCacheStored()) {
                menu.findItem(R.id.menu_info_save).setVisible(false);
                menu.findItem(R.id.menu_info_delete).setVisible(true);
            } else {
                menu.findItem(R.id.menu_info_save).setVisible(true);
                menu.findItem(R.id.menu_info_delete).setVisible(false);            
            }
        }
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onHome();
                return true;
            case R.id.menu_info_save:
                onSaveCache();
                return true;               
            case R.id.menu_info_delete:
                onDeleteCache();
                return true;
            case R.id.menu_info_search:
                onSearchCache();
                return true;                
            case R.id.menu_info_notes:
                onEditNotes();
                return true;                
            case R.id.menu_info_refresh:
                onRefresh();
                return true;                
            default:
                return super.onOptionsItemSelected(item);                
        }
    }
    
    private void onHome() {
        NavigationManager.startDashboardActivity(this);        
    }

    private void onRefresh() {
        infoViewModel.beginRefresh(getSupportActionBar().getSelectedNavigationIndex());       
    }

    private void onEditNotes() {
        NavigationManager.startNotesActivity(this, infoViewModel.getGeoCachceId());        
    }

    private void onSaveCache() {
        infoViewModel.saveCache();
        invalidateOptionsMenu();        
    }

    private void onDeleteCache() {
        showDialog(REMOVE_CACHE_ALERT_DIALOG_ID);
        /*
        boolean forceDelete = Controller.getInstance().getPreferencesManager().getRemoveFavoriteWithoutConfirm();
        if (forceDelete) {
            performDeleteCache();
        } else {
            showDialog(REMOVE_CACHE_ALERT_DIALOG_ID);
        }
        */        
    }
    
    private void performDeleteCache() {
        infoViewModel.deleteCache();
        invalidateOptionsMenu();        
    }

    private void onSearchCache() {
        //TODO:
        /*
        if (!isCacheStored) {
            cbFavoriteCache.setChecked(true);
            if ((notebook == null) && (controller.getPreferencesManager().getDownloadNoteBookAlways() || controller.getConnectionManager().isWifiConnected())) {
                controller.getApiManager().getInfo(DownloadInfoState.SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP, this, geoCache.getId());
                return;
            }
        }
        */
        if (!infoViewModel.isCacheStored()) {
            infoViewModel.saveCache();
            invalidateOptionsMenu();            
        }
        NavigationManager.startSearchMapActivity(this, infoViewModel.getGeoCachce());
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
    
    public void updateInfoText() {
        AbstractWebViewFragment webViewFragment = getInfoFragment();
        if (webViewFragment != null) {
            webViewFragment.updateText();
        }
        updateTabTextView(infoTabTextView, infoViewModel.getInfoState().getText() != null);
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
    
    public void updateNotebookText() {
        AbstractWebViewFragment webViewFragment = getNotebookFragment();
        if (webViewFragment != null) {
            webViewFragment.updateText();
        }
        updateTabTextView(notebookTabTextView, infoViewModel.getNotebookState().getText() != null);
    }
    
    private PhotoFragment getPhotoFragment() {
        return (PhotoFragment) mTabsAdapter.getFragment(infoViewModel.getPhotosState().getIndex());        
    }
    
    public void showPhotoListProgressBar() {
        PhotoFragment photoFragment = getPhotoFragment();
        if (photoFragment != null) {
            photoFragment.showProgressBar();            
        }        
    }
    
    public void hidePhotoListProgressBar() {
        PhotoFragment photoFragment = getPhotoFragment();
        if (photoFragment != null) {
            photoFragment.hideProgressBar();            
        }
    }

    public void showPhotoListErrorMessage() {
        PhotoFragment photoFragment = getPhotoFragment();
        if (photoFragment != null) {
            photoFragment.showErrorMessage();            
        }       
    }

    public void hidePhotoListErrorMessage() {
        PhotoFragment photoFragment = getPhotoFragment();
        if (photoFragment != null) {        
            photoFragment.hideErrorMessage();
        }        
    }
    
    public void updatePhotosList() {
        PhotoFragment photoFragment = getPhotoFragment();
        if (photoFragment != null) {        
            photoFragment.updatePhotosList();
        }
        updateTabTextView(photoTabTextView, infoViewModel.getPhotosState().getPhotos() != null);        
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
