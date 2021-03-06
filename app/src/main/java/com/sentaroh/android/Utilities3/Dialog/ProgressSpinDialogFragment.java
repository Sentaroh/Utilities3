package com.sentaroh.android.Utilities3.Dialog;

/*
The MIT License (MIT)
Copyright (c) 2013 Sentaroh

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sentaroh.android.Utilities3.CallBackListener;
import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.R;
import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.Utilities3.ThemeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressSpinDialogFragment extends DialogFragment {
	final private static Logger log=LoggerFactory.getLogger(ProgressSpinDialogFragment.class);
	private final static String APPLICATION_TAG="ProgressSpinDialogFragment";

    private Dialog mDialog=null;
    private boolean mTerminateRequired=true;
    private ProgressSpinDialogFragment mFragment=null;

    private boolean mDialogCancellable=true;

    private String mDialogTitle="",mDialogMsgText="",
            mDialogCanTitleInit,mDialogCanTitlePressed;
    private int mDialogProgress=0;
    private boolean mDialogCancelBtnEnabled=true;
    private Handler mUiHandler=new Handler();

    private NotifyEvent mNotifyEvent=null;

    public static ProgressSpinDialogFragment newInstance(
            String title, String msgtext, String cancelTitleInit, String cancelTitlePressed) {
        log.info("newInstance");
        ProgressSpinDialogFragment frag = new ProgressSpinDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("msgtext", msgtext);
        bundle.putString("cacelTitleInit", cancelTitleInit);
        bundle.putString("cacelTitlePressed", cancelTitlePressed);
        frag.setArguments(bundle);
        return frag;
    }
    public void setNotifyEvent(NotifyEvent ntfy) {mNotifyEvent=ntfy;}

    public ProgressSpinDialogFragment() {
        log.info("Constructor(Default)");
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        log.info("onSaveInstanceState");
        if(outState.isEmpty()){
            outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
        }
    };

    @Override
    final public void onConfigurationChanged(final Configuration newConfig) {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
        log.info("onConfigurationChanged");

//	    reInitViewWidget();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log.info("onCreateView");
        View view=super.onCreateView(inflater, container, savedInstanceState);
        CommonDialog.setDlgBoxSizeCompact(mDialog);
        return view;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.info("onCreate");

        mFragment=this;
        if (!mTerminateRequired) {
            Bundle bd=getArguments();
            setRetainInstance(true);
            mDialogTitle=bd.getString("title");
            mDialogMsgText=bd.getString("msgtext");
            mDialogCanTitleInit=bd.getString("cacelTitleInit");
            mDialogCanTitlePressed=bd.getString("cacelTitlePressed");
        }
    };

    @Override
    final public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        log.info("onActivityCreated");
    };
    @Override
    final public void onAttach(Activity activity) {
        super.onAttach(activity);
        log.info("onAttach");
    };
    @Override
    final public void onDetach() {
        super.onDetach();
        log.info("onDetach");
    };
    @Override
    final public void onStart() {
//    	CommonDialog.setDlgBoxSizeCompact(mDialog);
        super.onStart();
        log.info("onStart");
        if (mTerminateRequired) mDialog.cancel();
    };
    @Override
    final public void onStop() {
        super.onStop();
        log.info("onStop");
    };

    @Override
    public void onDestroyView() {
        log.info("onDestroyView");
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
    @Override
    public void onCancel(DialogInterface di) {
        log.info("onCancel");
//	    super.onCancel(di);
        if (!mTerminateRequired) {
            Button btnCancel = (Button) mDialog.findViewById(R.id.progress_spin_dlg_fragment_btn_cancel);
            btnCancel.performClick();
        }
        super.onCancel(di);
    }
    @Override
    public void onDismiss(DialogInterface di) {
        log.info("onDismiss");
        super.onDismiss(di);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log.info("onCreateDialog");
        mDialog=new Dialog(getActivity());//, ThemeUtil.getAppTheme(getActivity()));//, MiscUtil.getAppTheme(getActivity()));
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(mDialogCancellable);
        mDialog.setCanceledOnTouchOutside(false);

        if (!mTerminateRequired) {
            initViewWidget();
        }
        return mDialog;
    };

    @SuppressWarnings("unused")
    private void reInitViewWidget() {
        log.info("reInitViewWidget");
        if (!mTerminateRequired) {
            Handler hndl=new Handler();
            hndl.post(new Runnable(){
                @Override
                public void run() {
                    mDialog.hide();
                    mDialog.getWindow().getCurrentFocus().invalidate();
                    initViewWidget();
                    Button btnCancel = (Button) mDialog.findViewById(R.id.progress_spin_dlg_fragment_btn_cancel);
                    if (mDialogCancelBtnEnabled) btnCancel.setEnabled(true);
                    else btnCancel.setEnabled(true);
                    CommonDialog.setDlgBoxSizeCompact(mDialog);
                    mDialog.show();
                }
            });
        }
    };

    private ThemeColorList mThemeColorList;
    private void initViewWidget() {
        log.info("initViewWidget");
        mThemeColorList=ThemeUtil.getThemeColorList(getActivity());
        mDialog.setContentView(R.layout.progress_spin_dlg_fragment);

        LinearLayout title_view=(LinearLayout)mDialog.findViewById(R.id.progress_spin_dlg_fragment_ll_title);
        title_view.setBackgroundColor(mThemeColorList.title_background_color);

        LinearLayout dlg_view=(LinearLayout)mDialog.findViewById(R.id.progress_spin_dlg_fragment);
//		dlg_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);

        TextView tv_title=(TextView)mDialog.findViewById(R.id.progress_spin_dlg_fragment_title);
        tv_title.setTextColor(mThemeColorList.title_text_color);
        tv_title.setText(mDialogTitle);

        TextView tv_msg=(TextView)mDialog.findViewById(R.id.progress_spin_dlg_fragment_msg);
        if (mDialogMsgText.equals("")) {
            tv_msg.setVisibility(TextView.GONE);
        } else {
            tv_msg.setVisibility(TextView.VISIBLE);
            tv_msg.setText(mDialogMsgText);
        }

        final Button btnCancel = (Button) mDialog.findViewById(R.id.progress_spin_dlg_fragment_btn_cancel);

        btnCancel.setText(mDialogCanTitleInit);

//		CommonDialog.setDlgBoxSizeCompact(mDialog);

        // CANCELボタンの指定
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDialogCancelBtnEnabled=false;
                btnCancel.setEnabled(false);
                btnCancel.setText(mDialogCanTitlePressed);
                if (mNotifyEvent!=null)
                    mNotifyEvent.notifyToListener(false,new Object[]{mFragment});
            }
        });
    }

//    public void showDialog(FragmentManager fm, NotifyEvent ntfy) {
//    	log.info("showDialog");
//    	mNotifyEvent=ntfy;
//    	FragmentTransaction ft=fm.beginTransaction();
//        show(ft, "ProgressSpinDialogFragment");
//    };

    public void showDialog(FragmentManager fm, Fragment frag, @Nullable NotifyEvent ntfy, boolean cancellable) {
        log.info("showDialog");
        mTerminateRequired=false;
        mNotifyEvent=ntfy;

        mDialogCancellable=cancellable;
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(frag,null);
        ft.commitAllowingStateLoss();
//    	show(fm, APPLICATION_TAG);
    };

    public void showDialog(Context c, FragmentManager fm, Fragment frag, boolean cancellable, @Nullable final CallBackListener cbl) {
        NotifyEvent ntfy=new NotifyEvent(c);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                cbl.onCallBack(c, true, o);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                cbl.onCallBack(c, false, o);
            }
        });
        showDialog(fm, frag, ntfy, cancellable);
    };

    public void updateMsgText(final String msgtext) {
        if (mUiHandler!=null) {
            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    mDialogMsgText=msgtext;
                    TextView tv_msg=(TextView)mDialog.findViewById(R.id.progress_spin_dlg_fragment_msg);
                    tv_msg.setVisibility(TextView.VISIBLE);
                    tv_msg.setText(mDialogMsgText);
                    tv_msg.requestLayout();
                }
            });
        }
    };

}