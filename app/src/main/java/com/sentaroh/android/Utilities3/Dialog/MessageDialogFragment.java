package com.sentaroh.android.Utilities3.Dialog;

/*
The MIT License (MIT)
Copyright (c) 2015 Sentaroh

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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sentaroh.android.Utilities3.CallBackListener;
import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.R;
import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.Utilities3.ThemeUtil;
import com.sentaroh.android.Utilities3.Widget.NonWordwrapTextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDialogFragment extends DialogFragment {
    private final static Logger log= LoggerFactory.getLogger(MessageDialogFragment.class);
	private boolean mDebugEnabled=false;
	private final static String APPLICATION_TAG="MessageDialogFragment";

	private Dialog mDialog=null;
	private MessageDialogFragment mFragment=null;
	private boolean terminateRequired=true;

    private String mDialogTitleType="", mDialogTitle="",mDialogMsgText="", mDialogButtonOkText="",
            mDialogButtonCancelText="", mDialogButtonExtraText="";
    private Spannable mDialogMsgSpannable=null;
	private boolean mDialogTypeNegative=false;
	
	private NotifyEvent mNotifyEvent=null;
	
	private ThemeColorList mThemeColorList;

	private Activity mActivity=null;

	public static MessageDialogFragment newInstance(
            boolean negative, String type, String title, String msgtext) {
        MessageDialogFragment frag = new MessageDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putString("button_ok_text", "");
        bundle.putString("button_cancel_text", "");

        if (title!=null) bundle.putString("title", title);
        else bundle.putString("title", "");
        
        if (msgtext!=null) bundle.putString("msgtext", msgtext);
        else bundle.putString("msgtext", "");
        
        bundle.putString("type", type);
        bundle.putBoolean("negative", negative);
        frag.setArguments(bundle);
        return frag;
    }

    public static MessageDialogFragment newInstance(boolean negative, String type, String title, String msgtext,
                                                    String btn_ok_text, String btn_cancel_text) {
//		log.debug("newInstance sub="+title+", msg="+msgtext);
        MessageDialogFragment frag = new MessageDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putString("button_ok_text", btn_ok_text);
        bundle.putString("button_cancel_text", btn_cancel_text);

        if (title!=null) bundle.putString("title", title);
        else bundle.putString("title", "");

        if (msgtext!=null) bundle.putString("msgtext", msgtext);
        else bundle.putString("msgtext", "");

        bundle.putString("type", type);
        bundle.putBoolean("negative", negative);
        frag.setArguments(bundle);
        return frag;
    }

    public static MessageDialogFragment newInstance(boolean negative, String type, String title, String msgtext,
                                                    String btn_ok_text, String btn_cancel_text, String btn_extra_text) {
//		log.debug("newInstance sub="+title+", msg="+msgtext);
        MessageDialogFragment frag = new MessageDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putString("button_ok_text", btn_ok_text);
        bundle.putString("button_cancel_text", btn_cancel_text);
        bundle.putString("button_extra_text", btn_extra_text);

        if (title!=null) bundle.putString("title", title);
        else bundle.putString("title", "");

        if (msgtext!=null) bundle.putString("msgtext", msgtext);
        else bundle.putString("msgtext", "");

        bundle.putString("type", type);
        bundle.putBoolean("negative", negative);
        frag.setArguments(bundle);
        return frag;
    }

    public void setNotifyEvent(NotifyEvent ntfy) {mNotifyEvent=ntfy;}

	public MessageDialogFragment() {
        if (mDebugEnabled) log.debug("Constructor(Default) terminateRequired="+terminateRequired);
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        if (mDebugEnabled) log.debug("onSaveInstanceState terminateRequired="+terminateRequired);
		if(outState.isEmpty()){
	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
	    }
	};  
	
	@Override
	final public void onConfigurationChanged(final Configuration newConfig) {
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
        if (mDebugEnabled) log.debug("onConfigurationChanged terminateRequired="+terminateRequired);

	    reInitViewWidget();
//	    CommonDialog.setDlgBoxSizeCompact(mDialog);
	};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mDebugEnabled) log.debug("onCreateView terminateRequired="+terminateRequired);
    	View view=super.onCreateView(inflater, container, savedInstanceState);
//    	CommonDialog.setDlgBoxSizeCompact(mDialog);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (mMaxDialogWindowSize) mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        else mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return view;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        if (mDebugEnabled) log.debug("onCreate terminateRequired="+terminateRequired);
        if (mActivity==null) mActivity=getActivity();
        if (!terminateRequired) {
            Bundle bd=getArguments();
            setRetainInstance(true);
        	mDialogTitleType=bd.getString("type");
        	mDialogTitle=bd.getString("title");
        	mDialogMsgText=bd.getString("msgtext");
        	mDialogTypeNegative=bd.getBoolean("negative");

            mDialogButtonOkText=bd.getString("button_ok_text", "");
            mDialogButtonCancelText=bd.getString("button_cancel_text", "");
            mDialogButtonExtraText=bd.getString("button_extra_text", "");

        }
    	mFragment=this;
    }

	@Override
	final public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
        if (mDebugEnabled) log.debug("onActivityCreated terminateRequired="+terminateRequired);
	};

	@Override
	final public void onAttach(Activity activity) {
	    super.onAttach(activity);
        if (mDebugEnabled) log.debug("onAttach terminateRequired="+terminateRequired);
        if (mActivity==null) mActivity=getActivity();
	};

	@Override
	final public void onDetach() {
	    super.onDetach();
        if (mDebugEnabled) log.debug("onDetach terminateRequired="+terminateRequired);
	};

	@Override
	final public void onStart() {
//		CommonDialog.setDlgBoxSizeCompact(mDialog);
	    super.onStart();
        if (mDebugEnabled) log.debug("onStart terminateRequired="+terminateRequired);
	    if (terminateRequired) mDialog.cancel(); 
	};
	
	@Override
	final public void onStop() {
	    super.onStop();
        if (mDebugEnabled) log.debug("onStop terminateRequired="+terminateRequired);
	};

	@Override
	public void onDestroyView() {
        if (mDebugEnabled) log.debug("onDestroyView terminateRequired="+terminateRequired);
	    if (getDialog() != null && getRetainInstance())
	        getDialog().setDismissMessage(null);
	    super.onDestroyView();
	};
	
	@Override
	public void onCancel(DialogInterface di) {
        if (mDebugEnabled) log.debug("onCancel terminateRequired="+terminateRequired);
//	    super.onCancel(di);
		if (!terminateRequired) {
			Button btnOk = (Button) mDialog.findViewById(R.id.common_dialog_btn_ok);
			Button btnCancel = (Button) mDialog.findViewById(R.id.common_dialog_btn_cancel);
			if (mDialogTypeNegative) btnCancel.performClick();
			else btnOk.performClick();
		}
		super.onCancel(di);
	};
	
	@Override
	public void onDismiss(DialogInterface di) {
        if (mDebugEnabled) log.debug("onDismiss terminateRequired="+terminateRequired);
		super.onDismiss(di);
	};

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mDebugEnabled) log.debug("onCreateDialog terminateRequired="+terminateRequired);

        mDialog=new Dialog(mActivity);//, ThemeUtil.getAppTheme(mActivity));

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setCanceledOnTouchOutside(false);

		if (!terminateRequired) {
			mThemeColorList=ThemeUtil.getThemeColorList(mActivity);
			initViewWidget();
		}
		
        return mDialog;
    };

    private void reInitViewWidget() {
        if (mDebugEnabled) log.debug("reInitViewWidget enterd");
		if (!terminateRequired) {
    		Handler hndl=new Handler();
    		hndl.post(new Runnable(){
				@Override
				public void run() {
					mDialog.hide();
//		    		mDialog.getWindow().getCurrentFocus().invalidate();
					initViewWidget();
					CommonDialog.setDlgBoxSizeCompact(mDialog);
					mDialog.onContentChanged();
					mDialog.show();
				}
    		});
		}
    };

    private NonWordwrapTextView mCustomTextView=null;
    private int mMessageTextColor=-1;
    private boolean mWordWrapSetRequired =false;
    private boolean mWordWrap =true;

    final static public String CATEGORY_INFO="I";
    final static public String CATEGORY_WARN="W";
    final static public String CATEGORY_ERROR="E";
    final static public String CATEGORY_DANGER="D";

    private void initViewWidget() {
        if (mDebugEnabled) log.debug("initViewWidget enterd");

    	mDialog.setContentView(R.layout.common_dialog);
        LinearLayout dlg_view=(LinearLayout)mDialog.findViewById(R.id.common_dialog_view);
        dlg_view.setBackgroundColor(mThemeColorList.text_background_color);
//        if (mThemeColorList.theme_is_light) dlg_view.setBackgroundColor(Color.parseColor("#ffffff"));
//        else dlg_view.setBackgroundColor(Color.parseColor("#444444"));
    	ImageView title_icon=(ImageView)mDialog.findViewById(R.id.common_dialog_icon);
        NonWordwrapTextView title=(NonWordwrapTextView)mDialog.findViewById(R.id.common_dialog_title);
//        title.setWordWrapEnabled(true);
    	LinearLayout title_view=(LinearLayout)mDialog.findViewById(R.id.common_dialog_title_view);
    	title_view.setBackgroundColor(mThemeColorList.title_background_color);
    	ScrollView msg_view=(ScrollView)mDialog.findViewById(R.id.common_dialog_msg_view);
//    	msg_view.setBackgroundColor(mThemeColorList.text_background_color);

    	LinearLayout btn_view=(LinearLayout)mDialog.findViewById(R.id.common_dialog_btn_view);
//        btn_view.setBackgroundColor(mThemeColorList.text_background_color);

        TextView msg_text=(TextView)mDialog.findViewById(R.id.common_dialog_msg);
        msg_text.setVisibility(TextView.GONE);
        mCustomTextView=(NonWordwrapTextView)mDialog.findViewById(R.id.common_dialog_custom_text_view);
        if (mWordWrapSetRequired) mCustomTextView.setWordWrapEnabled(mWordWrap);
//        mCustomTextView.setBackgroundColor(Color.DKGRAY);

//        mCustomTextView.setTextColor(Color.LTGRAY);
		if (mDialogTitleType.equalsIgnoreCase(CATEGORY_WARN)) {
			title_icon.setImageResource(R.drawable.dialog_warning);
            title.setTextColor(Color.WHITE);
//			title.setTextColor(Color.YELLOW);
            mCustomTextView.setTextColor(mThemeColorList.text_color_primary);
		} else if (mDialogTitleType.equalsIgnoreCase(CATEGORY_ERROR)) {
			title_icon.setImageResource(R.drawable.dialog_error);
            title.setTextColor(Color.RED);
            mCustomTextView.setTextColor(mThemeColorList.text_color_error);
        } else if (mDialogTitleType.equalsIgnoreCase(CATEGORY_DANGER)) {
            title_icon.setImageResource(R.drawable.dialog_error);
            title.setTextColor(Color.RED);
            msg_view.setBackgroundColor(mThemeColorList.title_background_color);
            btn_view.setBackgroundColor(mThemeColorList.title_background_color);
            msg_text.setTextColor(mThemeColorList.text_color_error);
            mCustomTextView.setTextColor(mThemeColorList.text_color_error);
		} else {
            title_icon.setImageResource(R.drawable.dialog_information);
            title.setTextColor(Color.WHITE);
        }
		title.setText(mDialogTitle);
//		title.setDebugEnable(true);
        if (mMessageTextColor!=-1) mCustomTextView.setTextColor(mMessageTextColor);
//        cust_msg_text.setDebugEnable(true);
        mCustomTextView.setVisibility(TextView.VISIBLE);
//        cust_msg_text.setWordWrapEnabled(true);
		if (mDialogMsgText.equals("") && mDialogMsgSpannable==null) mCustomTextView.setVisibility(View.GONE);
		else {
		    if (mDialogMsgSpannable!=null) mCustomTextView.setText(mDialogMsgSpannable);
            else mCustomTextView.setText(mDialogMsgText);
            mCustomTextView.invalidate();
            mCustomTextView.requestLayout();
//            msg_view.invalidate();
//            msg_view.requestLayout();
//			msg_text.setTextColor(mThemeColorList.text_color_primary);
//			msg_text.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
		}

        final Button btnExtra = (Button) mDialog.findViewById(R.id.common_dialog_extra_button);
		if (mExtraButtonListener!=null) {
            if (!mDialogButtonExtraText.equals("")) {
                btnExtra.setText(mDialogButtonExtraText);
                btnExtra.setVisibility(Button.VISIBLE);

                btnExtra.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mExtraButtonListener.onCallBack(mActivity, true, null);
                    }
                });

            }
        }

        final Button btnOk = (Button) mDialog.findViewById(R.id.common_dialog_btn_ok);
		if (!mDialogButtonOkText.equals("")) btnOk.setText(mDialogButtonOkText);
		final Button btnCancel = (Button) mDialog.findViewById(R.id.common_dialog_btn_cancel);
        if (!mDialogButtonCancelText.equals("")) btnCancel.setText(mDialogButtonCancelText);
		if (mDialogTypeNegative) btnCancel.setVisibility(View.VISIBLE);
			else  btnCancel.setVisibility(View.GONE);
		
//		if (Build.VERSION.SDK_INT<=10 && mThemeColorList.theme_is_light) {
//			btnOk.setTextColor(mThemeColorList.text_color_info);
//			btnCancel.setTextColor(mThemeColorList.text_color_info);
//		}
		
//		CommonDialog.setDlgBoxSizeCompact(mDialog);
		
		// OKボタンの指定
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				mDialog.dismiss();
                mFragment.dismissAllowingStateLoss();//.dismiss();
                if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(true,null);
			}
		});
		// CANCELボタンの指定
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				mDialog.dismiss();
                mFragment.dismissAllowingStateLoss();//.dismiss();
                if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(false,null);
			}
		});
    }
    
//    public void showDialog(FragmentManager fm, NotifyEvent ntfy) {
//    	log.debug("showDialog");
//    	mNotifyEvent=ntfy;
//	    FragmentTransaction ft = fm.beginTransaction();
//	    ft.add(mFragment,null);
//	    ft.commitAllowingStateLoss();
////      show(fm, "MessageDialogFragment");
//    };

    public void setTextColor(int color) {
        mMessageTextColor=color;
    }

    public void setMessageText(Spannable msg_text) {
        mDialogMsgSpannable=msg_text;
    }

    public void setMessageText(String msg_text) {
        mDialogMsgText=msg_text;
    }

    public void setWordWrapEanbled(boolean enabled) {
        mWordWrap =enabled;
        mWordWrapSetRequired=true;
    }

    public void setDebugEanbled(boolean enabled) {
        mDebugEnabled =enabled;
    }

    private void showDialogInternal(FragmentManager fm, Fragment frag, NotifyEvent ntfy) {
        if (mDebugEnabled) log.debug("showDialog");

        terminateRequired=false;
        mNotifyEvent=ntfy;
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(frag,null);
        ft.commitAllowingStateLoss();
    };

//    public void showDialog(FragmentManager fm, Fragment frag, final NotifyEvent ntfy) {
//        mDebugEnabled=false;
//        showDialog(false, fm, frag, ntfy);
//    };

    public void showDialog(FragmentManager fm, Fragment frag, final Object listener) {
        showDialog(fm, frag, listener, null, false);
    };

    private CallBackListener mExtraButtonListener=null;
    private boolean mMaxDialogWindowSize=false;
    public void showDialog(FragmentManager fm, Fragment frag, final Object listener, final CallBackListener cbl, boolean max_size) {
        Context c=frag.getContext();
        mExtraButtonListener=cbl;
        mMaxDialogWindowSize=max_size;
        if (listener==null || listener instanceof NotifyEvent) {
            showDialogInternal(fm, frag, (NotifyEvent)listener);
        } else if (listener instanceof CallBackListener){
            NotifyEvent ntfy=new NotifyEvent(c);
            ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                @Override
                public void positiveResponse(Context c, Object[] o) {
                    if (listener!=null) ((CallBackListener)listener).onCallBack(mActivity, true, o);
                }

                @Override
                public void negativeResponse(Context c, Object[] o) {
                    if (listener!=null) ((CallBackListener)listener).onCallBack(mActivity, false, o);
                }
            });
            showDialogInternal(fm, frag, ntfy);
        }
    };

}
