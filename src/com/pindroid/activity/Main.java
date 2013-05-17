/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.pindroid.activity;

import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.fragment.AddBookmarkFragment.OnBookmarkSaveListener;
import com.pindroid.fragment.BookmarkBrowser;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.BrowseNotesFragment;
import com.pindroid.fragment.BrowseNotesFragment.OnNoteSelectedListener;
import com.pindroid.fragment.BrowseTagsFragment;
import com.pindroid.fragment.BrowseTagsFragment.OnTagSelectedListener;
import com.pindroid.fragment.MainFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.fragment.ViewNoteFragment;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;
import com.pindroid.fragment.PindroidFragment;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Main extends FragmentBaseActivity implements MainFragment.OnMainActionListener, OnBookmarkSelectedListener, 
		OnTagSelectedListener, OnNoteSelectedListener, OnBookmarkActionListener, OnBookmarkSaveListener {
	
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private int lastSelected = 0;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedState);
		setContentView(R.layout.main);

		
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mTitle = mDrawerTitle = getTitle();
		
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		String[] MENU_ITEMS = new String[] {getString(R.string.main_menu_my_bookmarks),
				getString(R.string.main_menu_my_unread_bookmarks),
				getString(R.string.main_menu_my_tags),
				getString(R.string.main_menu_my_notes),
				getString(R.string.main_menu_recent_bookmarks),
				getString(R.string.main_menu_popular_bookmarks),
				getString(R.string.main_menu_network_bookmarks),
				"Search",
				"Add Bookmark",
				"Change Account",
				"Settings"};
		
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.main_view, MENU_ITEMS));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		if (savedInstanceState == null) {
            selectItem(0);
        }
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		switch(position){
			case 0:
				onMyBookmarksSelected();
				break;
			case 1:
				onMyUnreadSelected();
				break;
			case 2:
				onMyTagsSelected();
				break;
			case 3:
				onMyNotesSelected();
				break;
			case 4:
				onRecentSelected();
				break;
			case 5:
				onPopularSelected();
				break;
			case 6:
				onMyNetworkSelected();
				break;
			case 7:
				onSearchSelected();
				break;
			case 8:
				onBookmarkAdd(null);
				mDrawerList.setItemChecked(8, false);
				mDrawerList.setItemChecked(lastSelected, true);
				mDrawerLayout.closeDrawer(mDrawerList);
				break;
			case 9:
				Intent i = AccountManager.newChooseAccountIntent(getAccount(), null, new String[]{Constants.ACCOUNT_TYPE}, true, null, null, null, null);
				startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
				mDrawerList.setItemChecked(9, false);
				mDrawerList.setItemChecked(lastSelected, true);
				mDrawerLayout.closeDrawer(mDrawerList);
				break;
			case 10:
				Intent prefs = new Intent(this, Preferences.class);
				startActivity(prefs);
				mDrawerList.setItemChecked(10, false);
				mDrawerList.setItemChecked(lastSelected, true);
				mDrawerLayout.closeDrawer(mDrawerList);
				break;
		}

		if(position < 7)
			lastSelected = position;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
	
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }
	
	private void replaceLeftFragment(Fragment frag, boolean backstack){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    FragmentTransaction t = fragmentManager.beginTransaction();
	    t.replace(R.id.list_frame, frag, "left");
	    if(backstack)
	    	t.addToBackStack(null);
	    t.commit();
	    
	    clearRightFragment();
	}
	
	private void clearRightFragment(){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    Fragment f = fragmentManager.findFragmentByTag("right");
	    
	    if(f != null){
		    FragmentTransaction t = fragmentManager.beginTransaction();
		    t.remove(f);
		    t.commit();
	    }
	}
	
	private void replaceRightFragment(Fragment frag, boolean backstack){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    FragmentTransaction t = fragmentManager.beginTransaction();
	    t.replace(R.id.content_frame, frag, "right");
	    if(backstack)
	    	t.addToBackStack(null);
	    t.commit();
	}
	
	private void clearDrawer(int position){		
		// Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	private void clearBackStack(){
		if(getSupportFragmentManager().getBackStackEntryCount() > 0){
			int id = getSupportFragmentManager().getBackStackEntryAt(0).getId();
			getSupportFragmentManager().popBackStackImmediate(id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}
	
	private boolean isTwoPane(){
		return getResources().getBoolean(R.bool.has_two_panes);
	}

	public void onMyBookmarksSelected() {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), null, null);
		
		clearBackStack();
		
		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(0);
	}

	public void onMyUnreadSelected() {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), null, "unread");
		
		clearBackStack();
		
		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(1);
	}

	public void onMyTagsSelected() {
		BrowseTagsFragment frag = new BrowseTagsFragment();
		frag.setUsername(app.getUsername());
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(2);
	}
	
	public void onMyNotesSelected() {
		BrowseNotesFragment frag = new BrowseNotesFragment();
		frag.setUsername(app.getUsername());
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(3);
	}

	public void onRecentSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "recent");
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(4);
	}
	
	public void onPopularSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "popular");
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(5);
	}
	
	public void onMyNetworkSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "network");
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(6);
	}
	
	public void onSearchSelected() {
		onSearchRequested();
		mDrawerList.setItemChecked(7, false);
		mDrawerList.setItemChecked(lastSelected, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	@Override
	protected void changeAccount(){
		Fragment cf = getSupportFragmentManager().findFragmentById(R.id.content_frame);
		Fragment lf = getSupportFragmentManager().findFragmentById(R.id.list_frame);
		
		if(cf != null){
			((PindroidFragment)cf).setUsername(app.getUsername());
			((PindroidFragment)cf).refresh();
		}
		
		if(lf != null){
			((PindroidFragment)lf).setUsername(app.getUsername());
			((PindroidFragment)lf).refresh();
		}
	}
	
	public void onBookmarkSelected(Bookmark b, BookmarkViewType viewType){
		ViewBookmarkFragment frag = new ViewBookmarkFragment();
		frag.setBookmark(b, viewType);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		
		
		if(isTwoPane()){
			FragmentTransaction t = fragmentManager.beginTransaction();

			if(fragmentManager.findFragmentByTag("left") instanceof BrowseTagsFragment){
				Fragment right = fragmentManager.findFragmentByTag("right");
				Fragment newRight = duplicateFragment(right);
				
				t.replace(R.id.list_frame, newRight, "left");
				t.addToBackStack(null);
			}
			
			t.replace(R.id.content_frame, frag, "right");
			t.commit();
		} else {
			replaceRightFragment(frag, true);
		}
	}

	public void onBookmarkView(Bookmark b) {
		onBookmarkSelected(b, BookmarkViewType.VIEW);
	}

	public void onBookmarkRead(Bookmark b) {
		onBookmarkSelected(b, BookmarkViewType.READ);
	}

	public void onBookmarkOpen(Bookmark b) {
		onBookmarkSelected(b, BookmarkViewType.WEB);
	}

	public void onBookmarkAdd(Bookmark b) {
		AddBookmarkFragment frag = new AddBookmarkFragment();
		frag.loadBookmark(b, null);
		frag.setUsername(app.getUsername());
		replaceRightFragment(frag, true);
	}

	public void onBookmarkShare(Bookmark b) {
		if(b != null){
			Intent sendIntent = IntentHelper.SendBookmark(b.getUrl(), b.getDescription());
			startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));
		}
	}

	public void onBookmarkMark(Bookmark b) {
		if(b != null && isMyself() && b.getToRead()) {
    		b.setToRead(false);
			BookmarkManager.UpdateBookmark(b, app.getUsername(), this);
    	}
	}

	public void onBookmarkEdit(Bookmark b) {
		AddBookmarkFragment frag = new AddBookmarkFragment();
		frag.loadBookmark(b, b);
		frag.setUsername(app.getUsername());
		replaceRightFragment(frag, true);
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, app.getUsername(), this);
	}

	public void onTagSelected(String tag) {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), tag, null);
		
		if(isTwoPane()){
			replaceRightFragment(frag, false);		
		} else {
			replaceRightFragment(frag, true);
		}
	}

	public void onNoteView(Note n) {
		ViewNoteFragment frag = new ViewNoteFragment();
		frag.setNote(n);
		replaceRightFragment(frag, true);
	}

	public void onViewTagSelected(String tag, String user) {
		Fragment frag = null;
		
		if(user.equals(app.getUsername())){
			frag = new BrowseBookmarksFragment();
		} else frag = new BrowseBookmarkFeedFragment();
		
		((BookmarkBrowser)frag).setQuery(app.getUsername(), tag, user);

		if(isTwoPane()){
			replaceLeftFragment(frag, true);		    
		} else {
			replaceRightFragment(frag, true);
		}
	}

	public void onAccountSelected(String account) {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, account);

		if(isTwoPane()){
			replaceLeftFragment(frag, true);		    
		} else {
			replaceRightFragment(frag, true);
		}
	}

	public void onBookmarkSave(Bookmark b) {
		getSupportFragmentManager().popBackStack();
	}

	public void onBookmarkCancel(Bookmark b) {
		getSupportFragmentManager().popBackStack();
	}
	
	@Override
	public void setTitle(CharSequence title){
		super.setTitle(title);
		mTitle = title;

		if(this.findViewById(R.id.action_bar_title) != null) {
			((TextView)this.findViewById(R.id.action_bar_title)).setText(title);
		}
	}
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    private Fragment duplicateFragment(Fragment f)
    {
        try {
            Fragment.SavedState oldState = getSupportFragmentManager().saveFragmentInstanceState(f);

            Fragment newInstance = f.getClass().newInstance();
            newInstance.setInitialSavedState(oldState);

            return newInstance;
        }
        catch (Exception e) // InstantiationException, IllegalAccessException
        {

        }
        
        return null;
    }
}