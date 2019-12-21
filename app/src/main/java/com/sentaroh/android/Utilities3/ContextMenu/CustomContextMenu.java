package com.sentaroh.android.Utilities3.ContextMenu;

import android.content.res.Resources;

import androidx.fragment.app.FragmentManager;

import com.sentaroh.android.Utilities3.ContextMenu.CustomContextMenuItem.CustomContextMenuOnCleanupListener;
import com.sentaroh.android.Utilities3.ContextMenu.CustomContextMenuItem.CustomContextMenuOnClickListener;

import java.util.ArrayList;

public class CustomContextMenu{

	private ArrayList<CustomContextMenuItem> mMenuList = null;
	private Resources mParentResource=null;
	private FragmentManager mFragmentManager=null;
	
	private ArrayList<CustomContextMenuOnClickListener> mClickHandler =
			new ArrayList<CustomContextMenuOnClickListener>();
	
	public CustomContextMenu(Resources r, FragmentManager fm) {
		mParentResource=r;
		mFragmentManager=fm;
		mMenuList = new ArrayList<CustomContextMenuItem>();
//				new CustomMenuAdapter(parentActivity,R.layout.custom_context_menu_list_item);
	};

	
	public CustomContextMenu addMenuItem(CharSequence title) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, title,
				-1));
		return this;
	};

	public CustomContextMenu addMenuItem(CharSequence title, int imageResourceId) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, title,
				imageResourceId));
		return this;
	};

	public CustomContextMenu addMenuItem(int textResourceId, int imageResourceId) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, textResourceId,
				imageResourceId));
		return this;
	};

	public CustomContextMenu addMenuItem(boolean enabled, CharSequence title) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, title,
				-1,enabled));
		return this;
	};

	public CustomContextMenu addMenuItem(boolean enabled, CharSequence title, int imageResourceId) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, title,
				imageResourceId,enabled));
		return this;
	};

	public CustomContextMenu addMenuItem(boolean enabled, int textResourceId, int imageResourceId) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, textResourceId,
				imageResourceId,enabled));
		return this;
	};

	
	public void setOnClickListener(CustomContextMenuOnClickListener listener) {
		mClickHandler.add( listener);
	};

	public void createMenu(String title) {
		CustomContextMenuFragment ccmf =
				CustomContextMenuFragment.newInstance(title);
		ccmf.showDialog(mFragmentManager,ccmf,mMenuList,mClickHandler,null);
	};

	public void createMenu(String title, CustomContextMenuOnCleanupListener cl) {
		CustomContextMenuFragment ccmf =
				CustomContextMenuFragment.newInstance(title);
		ccmf.showDialog(mFragmentManager,ccmf,mMenuList,mClickHandler, cl);
	};

	public void createMenu() {
		createMenu("");
	};

	public void createMenu(CustomContextMenuOnCleanupListener cl) {
		createMenu("",cl);
	};

}
