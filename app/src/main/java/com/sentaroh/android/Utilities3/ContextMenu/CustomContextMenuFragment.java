package com.sentaroh.android.Utilities3.ContextMenu;

/*
The MIT License (MIT)
Copyright (c) 2012 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to deal 
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to 
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or 
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.sentaroh.android.Utilities3.ContextMenu.CustomContextMenuItem.CustomContextMenuOnCleanupListener;
import com.sentaroh.android.Utilities3.ContextMenu.CustomContextMenuItem.CustomContextMenuOnClickListener;
import com.sentaroh.android.Utilities3.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class CustomContextMenuFragment extends DialogFragment {
    private static final Logger log=LoggerFactory.getLogger(CustomContextMenuFragment.class);
	private final static boolean DEBUG_ENABLE=false;
	private final static String APPLICATION_TAG="CustomContextMenuFragment";

	private Dialog mDialog=null;
	
	private CustomContextMenuFragment mFragment=null;
	private boolean terminateRequired=true;

    private String mDialogTitle="";
    
    private CustomContextMenuAdapter mMenuAdapter = null;
	private ArrayList<CustomContextMenuItem> mMenuItemList=null;
	private ArrayList<CustomContextMenuOnClickListener> mClickHandler =null;
	private CustomContextMenuOnCleanupListener mCleanupHandler=null;
	
	public static CustomContextMenuFragment newInstance(String title) {
		if (DEBUG_ENABLE) log.debug("newInstance entered");
        CustomContextMenuFragment frag = new CustomContextMenuFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        frag.setArguments(bundle);
        return frag;
    }

	public CustomContextMenuFragment() {
        if (DEBUG_ENABLE) log.debug("Constructor(Default) entered");
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        if (DEBUG_ENABLE) log.debug("onSaveInstanceState entered");
		if(outState.isEmpty()){
	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
	    }
	};  
	
	@Override
	final public void onConfigurationChanged(final Configuration newConfig) {
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
        if (DEBUG_ENABLE) log.debug("onConfigurationChanged entered");

	    reInitViewWidget();
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        if (DEBUG_ENABLE) log.debug("onCreate entered");

        Bundle bd=getArguments();
        setRetainInstance(true);

        mFragment=this;
//        mMenuAdapter=new CustomContextMenuAdapter(this.getLayoutInflater(savedInstanceState));
		if (!terminateRequired) {
            mMenuAdapter=new CustomContextMenuAdapter(getActivity());
        	mMenuAdapter.setMenuItemList(mMenuItemList);
        	mDialogTitle=bd.getString("title");
        }
        
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG_ENABLE) log.debug("onCreateView entered");
    	View view=super.onCreateView(inflater, container, savedInstanceState);
//    	CommonDialog.setDlgBoxSizeCompact(mDialog);
    	return view;
    };

	@Override
	final public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
        if (DEBUG_ENABLE) log.debug("onActivityCreated entered");
	};
	@Override
	final public void onAttach(Activity activity) {
	    super.onAttach(activity);
        if (DEBUG_ENABLE) log.debug("onAttach entered");
	};
	@Override
	final public void onDetach() {
	    super.onDetach();
        if (DEBUG_ENABLE) log.debug("onDetach entered");
	};
	@Override
	final public void onStart() {
//    	CommonDialog.setDlgBoxSizeCompact(mDialog);
	    super.onStart();
        if (DEBUG_ENABLE) log.debug("onStart entered");
	    if (terminateRequired) mFragment.mDialog.cancel();
	    else mDialog.show();
	};
	@Override
	final public void onStop() {
	    super.onStop();
        if (DEBUG_ENABLE) log.debug("onStop entered");
	    mDialog.hide();
	};

	@Override
	public void onDestroyView() {
        if (DEBUG_ENABLE) log.debug("onDestroyView entered");
	    if (getDialog() != null && getRetainInstance())
	        getDialog().setDismissMessage(null);
	    super.onDestroyView();
	}
	@Override
	public void onCancel(DialogInterface di) {
        if (DEBUG_ENABLE) log.debug("onCancel entered");
		mFragment.dismiss();
//		mMenuAdapter.clear();
//		mClickHandler.clear();
		super.onCancel(di);
	}
	@Override
	public void onDismiss(DialogInterface di) {
        if (DEBUG_ENABLE) log.debug("onDismiss entered");
		if (!terminateRequired) {
			mMenuAdapter.clear();
			mClickHandler.clear();
			if (mCleanupHandler!=null) mCleanupHandler.onCleanup();
		}
		super.onDismiss(di);
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (DEBUG_ENABLE) log.debug("onCreateDialog entered");
    	mDialog=new Dialog(getActivity());//, ThemeUtil.getAppTheme(getActivity()));
//		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		mDialog.setCanceledOnTouchOutside(true);
//        CommonDialog.setDlgBoxSizeCompact(mDialog);

		if (!terminateRequired) {
		    initViewWidget();

		    // Workaround for Huawei MediPad M5
            Handler hndl=new Handler();
            hndl.post(new Runnable(){
                @Override
                public void run() {
                    mDialog.hide();
                    mDialog.show();
                }
            });
        }

        return mDialog;
    };

    private void reInitViewWidget() {
        if (DEBUG_ENABLE) log.debug("reInitViewWidget enterd");
    	if (!terminateRequired) {
    		Handler hndl=new Handler();
    		hndl.post(new Runnable(){
				@Override
				public void run() {
					mDialog.hide();
//					mDialog.getWindow().getDecorView().invalidate();
		        	if (mDialog.getWindow()!=null && mDialog.getWindow().getCurrentFocus()!=null) {
		        	    mDialog.getWindow().getCurrentFocus().invalidate();
                        mDialog.getWindow().getCurrentFocus().requestLayout();
                    }
		    		initViewWidget();
//		        	CommonDialog.setDlgBoxSizeCompact(mDialog);
		        	mDialog.show();
				}
    		});
    	}
    };

//    private void initViewWidgetX() {
//        log.debug("initViewWidget entered");
//
////		CommonDialog.setDlgBoxSizeCompact(mDialog);
//
//        LinearLayout dlg_ll = new LinearLayout(getActivity());
//        dlg_ll.setOrientation(LinearLayout.VERTICAL);
//        TextView dlg_tv =new TextView(getActivity());
//        dlg_tv.setBackgroundColor(Color.WHITE);
//        dlg_tv.setTextColor(Color.BLACK);
////        dlg_tv.setTextSize(32);
//        dlg_tv.setGravity(
//                android.view.Gravity.CENTER_VERTICAL|android.view.Gravity.CENTER_HORIZONTAL);
//
//        ListView dlg_lv =new ListView(getActivity());
//        dlg_lv.setBackgroundColor(Color.WHITE);
//
//        dlg_ll.addView(dlg_tv);
//        dlg_ll.addView(dlg_lv);
//
//        mDialog.setContentView(dlg_ll);
//
//        if (mDialogTitle.length()!=0) {
//            dlg_tv.setText(mDialogTitle);
//            dlg_tv.setVisibility(TextView.VISIBLE);
//        } else dlg_tv.setVisibility(TextView.GONE);
//
//        dlg_lv.setAdapter(mMenuAdapter);
//        dlg_lv.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> items, View view, int idx,long id) {
//                CustomContextMenuItem item = (CustomContextMenuItem) mMenuAdapter
//                        .getItem(idx);
//                if (item.menu_enabled) {
//                    if (idx<mClickHandler.size() ) {
//                        mClickHandler.get(idx).onClick(item.text);
//                    }
//                    mFragment.dismiss();
//                }
//            }
//        });
//        dlg_lv.setScrollingCacheEnabled(false);
//        dlg_lv.setScrollbarFadingEnabled(false);
////		int[] colors = {0, 0xFFFF0000, 0}; // red for the example
////		lv.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
//        dlg_lv.setDividerHeight(0);
//        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////        CommonDialog.setDlgBoxSizeCompact(mDialog);
//
//    }

    private void initViewWidget() {
        if (DEBUG_ENABLE) log.debug("initViewWidget entered");
        mDialog.setContentView(R.layout.custom_context_menu_dlg);
        LinearLayout dlg_ll=(LinearLayout)mDialog.findViewById(R.id.custom_context_enu_dlg_view);
        dlg_ll.setBackgroundColor(Color.LTGRAY);
		ListView dlg_lv=(ListView)mDialog.findViewById(R.id.custom_context_enu_dlg_list);
        dlg_lv.setBackgroundColor(Color.WHITE);

		dlg_lv.setAdapter(mMenuAdapter);
		dlg_lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
				CustomContextMenuItem item = (CustomContextMenuItem) mMenuAdapter.getItem(idx);
				if (item.menu_enabled) {
					if (idx<mClickHandler.size() ) {
						mClickHandler.get(idx).onClick(item.text);
					}
					mFragment.dismiss();
				}
			}
		});
//        CommonDialog.setDlgBoxSizeCompact(mDialog);
//        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setDlgBoxSizeCompact(mDialog);
        dlg_lv.setDividerHeight(0);

    };

    static private void setDlgBoxSizeCompact(Dialog dialog) {
        if (dialog==null) return;
        int w=dialog.getWindow().getWindowManager().getDefaultDisplay().getWidth();
        int h=dialog.getWindow().getWindowManager().getDefaultDisplay().getHeight();
        int nw=0;
//        log.info("w="+w+", h="+h);
        if (w>h) {//Landscape
            if (w>800) {
                if (w>=1200) nw=(w/3)*2;
                else nw=800;
            } else nw= ViewGroup.LayoutParams.FILL_PARENT;
        } else {//Portrait
            nw= ViewGroup.LayoutParams.FILL_PARENT;
            if (w>=1600) {
                nw=(int)((float)w*0.8f);
            }
        }
//        log.info("nw="+nw);
        dialog.getWindow().setLayout(nw, ViewGroup.LayoutParams.WRAP_CONTENT);

    };


    public void showDialog(FragmentManager fm, CustomContextMenuFragment frag,
                           final ArrayList<CustomContextMenuItem> ma,
                           final ArrayList<CustomContextMenuOnClickListener> ch,
                           CustomContextMenuOnCleanupListener cl) {
        if (DEBUG_ENABLE) log.debug("showDialog entered");
    	terminateRequired=false;
//	    FragmentTransaction ft = fm.beginTransaction();
//	    ft.add(frag,null);
//	    ft.commitAllowingStateLoss();
    	mMenuItemList=ma;
    	mClickHandler=ch;
    	mCleanupHandler=cl;
    	show(fm, APPLICATION_TAG);
    };
    
}
