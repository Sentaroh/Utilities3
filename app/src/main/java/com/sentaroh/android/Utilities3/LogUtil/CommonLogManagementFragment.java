package com.sentaroh.android.Utilities3.LogUtil;

/*
The MIT License (MIT)
Copyright (c) 2011-2019 Sentaroh

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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sentaroh.android.Utilities3.ContextButton.ContextButtonUtil;
import com.sentaroh.android.Utilities3.Dialog.CommonDialog;
import com.sentaroh.android.Utilities3.Dialog.MessageDialogFragment;
import com.sentaroh.android.Utilities3.Dialog.ProgressBarDialogFragment;
import com.sentaroh.android.Utilities3.LocalMountPoint;
import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities3.R;
import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.Utilities3.ThemeUtil;
import com.sentaroh.android.Utilities3.ThreadCtrl;
import com.sentaroh.android.Utilities3.Widget.CustomSpinnerAdapter;
import com.sentaroh.android.Utilities3.Zip.ZipUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class CommonLogManagementFragment extends DialogFragment {
    private final static Logger log= LoggerFactory.getLogger(CommonLogManagementFragment.class);
    private static CommonLogParameters mClog=null;
//	private final static boolean log.isInfoEnabled()=false;
	private final static String APPLICATION_TAG="LogFileManagement";

	private final static String MAIL_TO="gm.developer.fhoshino@gmail.com";

	private Dialog mDialog=null;
	private boolean mTerminateRequired=true;
	private CommonLogManagementFragment mFragment=null;
	private String mDialogTitle=null;
	
	private CommonLogFileListAdapter mLogFileManagementAdapter=null;
	
	private Activity mContext=null;
	
	private Handler mUiHandler=null;

	private NotifyEvent mNotifyUpdateLogOption=null;
	
	private ArrayList<CommonLogFileListItem> mLogFileList=null;

	private String mEnableMessage="";
    private String mSendMessage="";
    private String mSendSubject="";
    private String mSendHint="";

	public static CommonLogManagementFragment newInstance(boolean retainInstance, String title,
                                                          String send_msg, String enable_msg, String send_subject) {
		CommonLogManagementFragment frag = new CommonLogManagementFragment();
        Bundle bundle = new Bundle();
        bundle.putString("theme_id", "");
        bundle.putBoolean("retainInstance", retainInstance);
        bundle.putBoolean("showSaveButton", true);
        bundle.putString("title", title);
        bundle.putString("msgtext", send_msg);
        bundle.putString("enableMsg", enable_msg);
        bundle.putString("subject", send_subject);
        bundle.putString("hint", "");
        frag.setArguments(bundle);
        return frag;
    }

    public static CommonLogManagementFragment newInstance(boolean retainInstance, String title,
                                                          String send_msg, String enable_msg, String send_subject, String send_hint) {
        CommonLogManagementFragment frag = new CommonLogManagementFragment();
        Bundle bundle = new Bundle();
        bundle.putString("theme_id", "");
        bundle.putBoolean("retainInstance", retainInstance);
        bundle.putBoolean("showSaveButton", true);
        bundle.putString("title", title);
        bundle.putString("msgtext", send_msg);
        bundle.putString("enableMsg", enable_msg);
        bundle.putString("subject", send_subject);
        bundle.putString("hint", send_hint);
        frag.setArguments(bundle);
        return frag;
    }

//    public static CommonLogManagementFragment newInstance(String theme_id, boolean retainInstance, String title,
//                                                              String send_msg, String enable_msg, String send_subject) {
//        if (log.isInfoEnabled()) log.info("newInstance");
//        CommonLogManagementFragment frag = new CommonLogManagementFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("theme_id", theme_id);
//        bundle.putBoolean("retainInstance", retainInstance);
//        bundle.putBoolean("showSaveButton", true);
//        bundle.putString("title", title);
//        bundle.putString("msgtext", send_msg);
//        bundle.putString("enableMsg", enable_msg);
//        bundle.putString("subject", send_subject);
//        frag.setArguments(bundle);
//        return frag;
//    }

    public CommonLogManagementFragment() {
	};
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(outState.isEmpty()){
	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
	    }
    	saveViewContents();
	};  
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	    reInitViewWidget();
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	};
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view=super.onCreateView(inflater, container, savedInstanceState);
    	return view;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        mContext=getActivity();
        mClog= CommonLogParametersFactory.getLogParms(mContext);
        mUiHandler=new Handler();
    	mFragment=this;
        if (!mTerminateRequired) {
//            mClog=(CommonGlobalParms)getActivity().getApplication();
            Bundle bd=getArguments();
            setRetainInstance(bd.getBoolean("retainInstance"));
            mDialogTitle=bd.getString("title");
            mEnableMessage=bd.getString("enableMsg");
            mSendMessage=bd.getString("msgtext");
            mSendSubject=bd.getString("subject");
            String hint=bd.getString("hint");
            if (hint!=null && hint.equals("")) mSendHint=mContext.getString(R.string.msgs_log_file_prob_question_desc_hint);
            else mSendHint=hint;
        	mLogFileList=CommonLogUtil.createLogFileList(mContext);
        }
    };
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setCanceledOnTouchOutside(false);

		if (!mTerminateRequired) {
			initViewWidget();
		}
		
        return mDialog;
    };
    
	@Override
	public void onStart() {
    	CommonDialog.setDlgBoxSizeLimit(mDialog,true);
	    super.onStart();
	    if (mTerminateRequired) mDialog.cancel();
	    else {
	    	mDialog.setOnKeyListener(new OnKeyListener(){
    	        @Override
	    	    public boolean onKey (DialogInterface dialog , int keyCode , KeyEvent event ){
	    	        // disable search button action
	    	        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction()== KeyEvent.ACTION_DOWN){
	    	        	if (mLogFileManagementAdapter.isShowCheckBox()) {
		    	        	for(int i=0;i<mLogFileManagementAdapter.getCount();i++) {
		    	        		mLogFileManagementAdapter.getItem(i).isChecked=false;
		    	        	}
		    	        	mLogFileManagementAdapter.setShowCheckBox(false);
		    	        	mLogFileManagementAdapter.notifyDataSetChanged();
		    	        	setContextButtonNormalMode();
		    	        	return true;
	    	        	}
	    	        }
	    	        return false;
	    	    }
	    	});
	    }
	};
	
	@Override
	public void onCancel(DialogInterface di) {
		mFragment.dismiss();
		super.onCancel(di);
	};
	
	@Override
	public void onDismiss(DialogInterface di) {
		super.onDismiss(di);
	};

	@Override
	public void onStop() {
	    super.onStop();
	};
	
	@Override
	public void onDestroyView() {
	    if (getDialog() != null && getRetainInstance())
	        getDialog().setDismissMessage(null);
	    super.onDestroyView();
        deleteTempLogFile();
//        deleteZipLogFile();
	};
	
	@Override
	public void onDetach() {
	    super.onDetach();
	};


    private void reInitViewWidget() {
    	if (!mTerminateRequired) {
    		Handler hndl=new Handler();
    		hndl.post(new Runnable(){
				@Override
				public void run() {
					boolean scb=mLogFileManagementAdapter.isShowCheckBox();
			    	ViewSaveValue sv=saveViewContents();
			    	initViewWidget();
			    	restoreViewContents(sv);
		        	if (scb) {
		        		mLogFileManagementAdapter.setShowCheckBox(true);
		        		setContextButtonSelecteMode();
		        	} else {
		        		setContextButtonNormalMode();
		        	}
				}
    		});
    	}
    };

    class ViewSaveValue {
        public boolean log_enabled=false;
        public int log_level=0;
    }
    private ViewSaveValue saveViewContents() {
        ViewSaveValue sv=new ViewSaveValue();
        final Spinner sp_log_level=(Spinner)mDialog.findViewById(R.id.log_file_list_dlg_log_level);
        final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
        sv.log_enabled=cb_log_enabled.isChecked();
        sv.log_level=sp_log_level.getSelectedItemPosition();
    	return sv;
    };
    
    private void restoreViewContents(ViewSaveValue sv) {
        final Spinner sp_log_level=(Spinner)mDialog.findViewById(R.id.log_file_list_dlg_log_level);
        final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
    	sp_log_level.setSelection(sv.log_level);
    	cb_log_enabled.setChecked(sv.log_enabled);
    };
    
    private void initViewWidget() {
    	mDialog.setContentView(R.layout.log_file_list_dlg);
    	
    	ThemeColorList tcl=ThemeUtil.getThemeColorList(getActivity());
    	
		final LinearLayout title_view=(LinearLayout) mDialog.findViewById(R.id.log_file_list_dlg_title_view);
		title_view.setBackgroundColor(tcl.title_background_color);
    	final TextView dlg_title=(TextView)mDialog.findViewById(R.id.log_file_list_dlg_title);
		dlg_title.setTextColor(tcl.title_text_color);

    	dlg_title.setText(mDialogTitle);
    	final ImageButton dlg_done=(ImageButton)mDialog.findViewById(R.id.log_file_list_dlg_done);
    	dlg_done.setVisibility(ImageButton.GONE);
    	
    	final ListView lv_log_file=(ListView)mDialog.findViewById(R.id.log_file_list_dlg_log_listview);
    	final Button btn_close=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_close);

    	NotifyEvent ntfy_cb_listener=new NotifyEvent(mContext);
    	ntfy_cb_listener.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				if (mLogFileManagementAdapter.isShowCheckBox()) {
					setContextButtonSelecteMode();
				}
			};

			@Override
			public void negativeResponse(Context c, Object[] o) {}
    	});
    	
    	mLogFileManagementAdapter=
    				new CommonLogFileListAdapter(getActivity(), R.layout.log_file_list_item,mLogFileList, ntfy_cb_listener);
    	lv_log_file.setAdapter(mLogFileManagementAdapter);
    	
    	setContextButtonListener();
    	setContextButtonNormalMode();

        final Spinner sp_log_level=(Spinner)mDialog.findViewById(R.id.log_file_list_dlg_log_level);
        final LinearLayout ll_log_level_view=(LinearLayout) mDialog.findViewById(R.id.log_file_list_dlg_log_level_view);
    	final Button btn_browse=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_browse_active_log);
    	final Button btn_send_dev=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_send);
    	final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
        CommonDialog.setButtonEnabled(getActivity(), btn_browse, mClog.isLogEnabled());
        CommonDialog.setButtonEnabled(getActivity(), btn_send_dev, mClog.isLogEnabled());
        CommonDialog.setSpinnerBackground(getActivity(), sp_log_level);
    	cb_log_enabled.setChecked(mClog.isLogEnabled());
        mDisableChangeLogEnabled=true;
    	cb_log_enabled.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CommonDialog.setViewEnabled(getActivity(), sp_log_level, cb_log_enabled.isChecked());
                if (cb_log_enabled.isChecked()) ll_log_level_view.setAlpha(1.0f);
                else ll_log_level_view.setAlpha(0.4f);
                if (!mDisableChangeLogEnabled) confirmSettingsLogOption(isChecked);
				mDisableChangeLogEnabled=false;
            }
    	});
    	mUiHandler.post(new Runnable(){
            @Override
            public void run() {
                mDisableChangeLogEnabled=false;
            }
        });

    	btn_browse.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				browseLogFile();
			}
    	});

    	btn_send_dev.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				confirmSendLog();
			}
    	});

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        adapter.add(mContext.getString(R.string.msgs_log_file_log_level_no_debug));
        adapter.add(mContext.getString(R.string.msgs_log_file_log_level_minimum_level));
        adapter.add(mContext.getString(R.string.msgs_log_file_log_level_verbose_level));
        adapter.add(mContext.getString(R.string.msgs_log_file_log_level_trace_level));
        sp_log_level.setPrompt("Select Log level");
        sp_log_level.setAdapter(adapter);
        if (mClog.getLogLevel()>3) sp_log_level.setSelection(3, false);
        else sp_log_level.setSelection(mClog.getLogLevel(), false);

        sp_log_level.setEnabled(cb_log_enabled.isChecked());
        if (cb_log_enabled.isChecked()) ll_log_level_view.setAlpha(1.0f);
        else ll_log_level_view.setAlpha(0.4f);
        sp_log_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mClog.setLogOptionLogLevel(mContext, sp_log_level.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        lv_log_file.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if (mLogFileManagementAdapter.getItem(0).log_file_name==null) return;
				if (mLogFileManagementAdapter.isShowCheckBox()) {
					mLogFileManagementAdapter.getItem(pos).isChecked=
							!mLogFileManagementAdapter.getItem(pos).isChecked;
					mLogFileManagementAdapter.notifyDataSetChanged();
		        	setContextButtonSelecteMode();
				} else {
					showLogFile(mLogFileManagementAdapter,pos);
				}
			}
    	});
    	
    	lv_log_file.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if (mLogFileManagementAdapter.isEmptyAdapter()) return true;
				if (!mLogFileManagementAdapter.getItem(pos).isChecked) {
					if (mLogFileManagementAdapter.isAnyItemSelected()) {
						int down_sel_pos=-1, up_sel_pos=-1;
						int tot_cnt=mLogFileManagementAdapter.getCount();
						if (pos+1<=tot_cnt) {
							for(int i=pos+1;i<tot_cnt;i++) {
								if (mLogFileManagementAdapter.getItem(i).isChecked) {
									up_sel_pos=i;
									break;
								}
							}
						}
						if (pos>0) {
							for(int i=pos;i>=0;i--) {
								if (mLogFileManagementAdapter.getItem(i).isChecked) {
									down_sel_pos=i;
									break;
								}
							}
						}
						if (up_sel_pos!=-1 && down_sel_pos==-1) {
							for (int i=pos;i<up_sel_pos;i++) 
								mLogFileManagementAdapter.getItem(i).isChecked=true;
						} else if (up_sel_pos!=-1 && down_sel_pos!=-1) {
							for (int i=down_sel_pos+1;i<up_sel_pos;i++) 
								mLogFileManagementAdapter.getItem(i).isChecked=true;
						} else if (up_sel_pos==-1 && down_sel_pos!=-1) {
							for (int i=down_sel_pos+1;i<=pos;i++) 
								mLogFileManagementAdapter.getItem(i).isChecked=true;
						}
						mLogFileManagementAdapter.notifyDataSetChanged();
					} else {
						mLogFileManagementAdapter.setShowCheckBox(true);
						mLogFileManagementAdapter.getItem(pos).isChecked=true;
						mLogFileManagementAdapter.notifyDataSetChanged();
					}
					setContextButtonSelecteMode();
				}
				return true;
			}
    	});

    	btn_close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mFragment.dismiss();
			}
    	});
    	
//    	CommonDialog.setDlgBoxSizeLimit(mDialog, true);
    };
    
    private ThemeColorList mThemeColorList=null;
	private void confirmSendLog() {
		CommonLogUtil.flushLog(mContext);

        File ilf=new File(mClog.getLogDirName()+"/"+mClog.getLogFileName()+".txt");
        if (ilf.length()==0) {
            MessageDialogFragment mdf =MessageDialogFragment.newInstance(false, "W",
                    mContext.getString(R.string.msgs_log_file_list_empty_can_not_send), "");
            mdf.showDialog(mFragment.getFragmentManager(), mdf, null);
            return ;
        }

		mThemeColorList=ThemeUtil.getThemeColorList(getActivity());
		createTempLogFile();


        final Dialog dialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.confirm_send_log_dlg);
		dialog.setCanceledOnTouchOutside(false);
		
		LinearLayout title_view=(LinearLayout)dialog.findViewById(R.id.confirm_send_log_dlg_title_view);
		title_view.setBackgroundColor(mThemeColorList.title_background_color);
		TextView title=(TextView)dialog.findViewById(R.id.confirm_send_log_dlg_title);
		title.setTextColor(mThemeColorList.title_text_color);
		final TextView msg=(TextView)dialog.findViewById(R.id.confirm_send_log_dlg_msg);
//		msg.setTextColor(mThemeColorList.text_color_primary);
//		msg.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
		msg.setText(mSendMessage);

		final Button btn_ok=(Button)dialog.findViewById(R.id.confirm_send_log_dlg_ok_btn);
		final Button btn_cancel=(Button)dialog.findViewById(R.id.confirm_send_log_dlg_cancel_btn);
		final Button btn_preview=(Button)dialog.findViewById(R.id.confirm_send_log_dlg_preview);
		
		CommonDialog.setDlgBoxSizeLimit(dialog, false);
		
		btn_preview.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
                try {
                    startLogfileViewerIntent(mContext, getTempLogFilePath());
                } catch (ActivityNotFoundException e) {
                    CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
                    mCommonDlg.showCommonDialog(false, "E",
                            mContext.getString(R.string.msgs_log_file_browse_app_can_not_found), e.getMessage(), null);
                } catch (Exception e) {
                    String ste=printStackTrace(e);
                    CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
                    mCommonDlg.showCommonDialog(false, "E",
                            mContext.getString(R.string.msgs_log_file_browse_app_error), e.getMessage()+"\n"+ste, null);
                }
            }
		});

		btn_ok.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
                getProblemDescription(getTempLogFilePath());
//				sendLogFileToDeveloper(getTempLogFilePath());
				dialog.dismiss();
			}
		});

		btn_cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
                deleteTempLogFile();
			}
		});

		dialog.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface arg0) {
				btn_cancel.performClick();
			}
		});

		dialog.show();

	};

	public void startLogfileViewerIntent(Context c, String fpath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (Build.VERSION.SDK_INT>=24) {
            Uri uri= FileProvider.getUriForFile(mContext, mClog.getLogFileProviderAuth(), new File(getTempLogFilePath()));
            intent.setDataAndType(uri, "text/plain");
        } else {
            intent.setDataAndType(Uri.parse("file://"+getTempLogFilePath()), "text/plain");
        }
        startActivity(intent);
    }

    private void getProblemDescription(final String fp) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.single_item_input_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_dlg_view);
//        CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_title_view);
        final TextView tv_title = (TextView) dialog.findViewById(R.id.single_item_input_title);
        title_view.setBackgroundColor(mThemeColorList.title_background_color);
        tv_title.setTextColor(mThemeColorList.title_text_color);
        tv_title.setText(mContext.getString(R.string.msgs_log_file_prob_question_desc_title));

        final TextView tv_msg=(TextView)dialog.findViewById(R.id.single_item_input_msg);
        tv_msg.setVisibility(TextView.GONE);
        final TextView tv_desc=(TextView)dialog.findViewById(R.id.single_item_input_name);
        tv_desc.setText(mSendHint);//mContext.getString(R.string.msgs_log_file_prob_question_desc_hint));
        final EditText et_msg=(EditText)dialog.findViewById(R.id.single_item_input_dir);
        et_msg.setHint(mSendHint);
        et_msg.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        final Button btn_ok=(Button)dialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btn_cancel=(Button)dialog.findViewById(R.id.single_item_input_cancel_btn);

        CommonDialog.setDlgBoxSizeLimit(dialog,true);

        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy_desc=new NotifyEvent(mContext);
                ntfy_desc.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        sendLogFileToDeveloper(getTempLogFilePath(), et_msg.getText().toString());
                        dialog.dismiss();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                if (et_msg.getText().length()<=10) {
                    MessageDialogFragment mdf =MessageDialogFragment.newInstance(false, "W",
                            mContext.getString(R.string.msgs_log_file_prob_question_desc_no_desc), "");
                    mdf.showDialog(mFragment.getFragmentManager(), mdf, null);
                } else {
                    ntfy_desc.notifyToListener(true, null);
                }
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                btn_cancel.performClick();
            }
        });

        dialog.show();
    }

    private void deleteTempLogFile() {
        String fp=getTempLogFilePath();
        if (fp!=null) {
            File lf=new File(fp);
            lf.delete();
        }
    }

    private String getTempLogFilePath() {
        if (mClog!=null) {
            return mContext.getExternalCacheDirs()[0].getPath()+"/"+mClog.getApplicationTag()+"_temp_log.txt";
        }
        return null;
    }

    private void deleteZipLogFile() {
        File lf=new File(getZipLogFilePath());
        lf.delete();
    }

    private String getZipLogFilePath() {
        return mContext.getExternalCacheDirs()[0].getPath()+"/"+mClog.getApplicationTag()+"_log.zip";
    }

    private void createTempLogFile() {
        File olf=new File(getTempLogFilePath());
		ArrayList<File> in_log_file_list=new ArrayList<File>();
        for(int i=mLogFileList.size()-1;i>=0;i--) {
            if (mLogFileList.get(i).log_file_path!=null) {
                in_log_file_list.add(new File(mLogFileList.get(i).log_file_path));
                log.debug("log file appended, path="+mLogFileList.get(i).log_file_path);
            }
        }
        in_log_file_list.add(new File(mClog.getLogDirName()+"/"+mClog.getLogFileName()+".txt"));
		try {
			FileOutputStream fos=new FileOutputStream(olf);
            for(File in_file:in_log_file_list) {
                FileInputStream fis=new FileInputStream(in_file);
                byte[] buff=new byte[1024*256];
                int rc=0;
                fos.write((new String(in_file.getPath()+"\n").getBytes()));
                while((rc=fis.read(buff))>0) {
                    fos.write(buff, 0, rc);
                }
                fis.close();
                fos.write((new String("\n")).getBytes());
            }
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	final private void sendLogFileToDeveloper(String log_file_path, String msg_text) {
		CommonLogUtil.resetLogReceiver(mContext);

		String zip_file_name=getZipLogFilePath();

		File lf=new File(zip_file_name);
		lf.delete();

//		createZipFile(zip_file_name,log_file_path);
//		String[] lmp=LocalMountPoint.convertFilePathToMountpointFormat(mContext, log_file_path);
//		ZipUtil.createZipFile(mContext, null, null, zip_file_name, lmp[0], log_file_path);
        String lfd=new File(log_file_path).getParent();
        ZipUtil.createZipFile(mContext, null, null, zip_file_name, lfd, log_file_path);

	    Intent intent=new Intent();
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(Intent.ACTION_SEND);
//	    intent.setType("message/rfc822");
//	    intent.setType("text/plain");
	    intent.setType("application/zip");

	    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{MAIL_TO});
//		    intent.putExtra(Intent.EXTRA_CC, new String[]{"cc@example.com"});
//		    intent.putExtra(Intent.EXTRA_BCC, new String[]{"bcc@example.com"});
	    intent.putExtra(Intent.EXTRA_SUBJECT, mSendSubject);
	    intent.putExtra(Intent.EXTRA_TEXT, msg_text);//mContext.getString(R.string.msgs_log_file_list_confirm_send_log_description));
	    Uri uri= FileProvider.getUriForFile(mContext, mClog.getLogFileProviderAuth(), lf);
	    intent.putExtra(Intent.EXTRA_STREAM, uri);
	    mContext.startActivity(intent);
	};

    private boolean mDisableChangeLogEnabled=false;
	final private void confirmSettingsLogOption(final boolean enabled) {
        final Button btn_browse=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_browse_active_log);
    	final Button btn_send_dev=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_send);
    	final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
//        final Spinner sp_log_level=(Spinner)mDialog.findViewById(R.id.log_file_list_dlg_log_level);
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
                final Button btn_browse=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_browse_active_log);
                final Button btn_send_dev=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_send);
                final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
			    mClog.setLogOptionLogEnabled(mContext, cb_log_enabled.isChecked());
                if (!cb_log_enabled.isChecked()) performRotateLog();
                CommonDialog.setButtonEnabled(getActivity(), btn_browse, enabled);
                CommonDialog.setButtonEnabled(getActivity(), btn_send_dev, enabled);
		    	Handler hndl=new Handler();
		    	hndl.postDelayed(new Runnable(){
					@Override
					public void run() {
					    int prev_count=mLogFileManagementAdapter.getCount();
						mLogFileList=CommonLogUtil.createLogFileList(mContext);
						mLogFileManagementAdapter.replaceDataList(mLogFileList);
						mLogFileManagementAdapter.notifyDataSetChanged();
						if (mLogFileManagementAdapter.getCount()!=0) setContextButtonNormalMode();
					}
		    	}, 200);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
                final Button btn_browse=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_browse_active_log);
                final Button btn_send_dev=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_send);
                final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
				mDisableChangeLogEnabled=true;
				cb_log_enabled.setChecked(!cb_log_enabled.isChecked());
			}
		});
		String msg="";
		if (enabled) msg=mEnableMessage;
        else  msg=getString(R.string.msgs_log_file_list_confirm_log_disable);
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(true, "W", msg, "");
        cdf.showDialog(getFragmentManager(),cdf,ntfy);
	};


	private void setContextButtonListener() {
		LinearLayout ll_prof=(LinearLayout) mDialog.findViewById(R.id.log_context_view);
        ImageButton ib_delete=(ImageButton)ll_prof.findViewById(R.id.log_context_button_delete);
        ImageButton ib_share=(ImageButton)ll_prof.findViewById(R.id.log_context_button_share);
        ImageButton ib_select_all=(ImageButton)ll_prof.findViewById(R.id.log_context_button_select_all);
        ImageButton ib_unselect_all=(ImageButton)ll_prof.findViewById(R.id.log_context_button_unselect_all);
    	final ImageButton dlg_done=(ImageButton)mDialog.findViewById(R.id.log_file_list_dlg_done);

    	if (ThemeUtil.isLightThemeUsed(getActivity())) ib_share.setImageResource(R.drawable.context_button_share_dark);
    	else ib_share.setImageResource(R.drawable.context_button_share);

    	dlg_done.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mLogFileManagementAdapter.setAllItemChecked(false);
				mLogFileManagementAdapter.setShowCheckBox(false);
				mLogFileManagementAdapter.notifyDataSetChanged();
				setContextButtonNormalMode();
			}
        });

        ib_delete.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				confirmDeleteLogFile();
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, ib_delete,
        		mContext.getString(R.string.msgs_log_file_list_label_delete));

        ib_share.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				sendLogFile();
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, ib_share,
        		mContext.getString(R.string.msgs_log_file_list_label_share));

        ib_select_all.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mLogFileManagementAdapter.setAllItemChecked(true);
				mLogFileManagementAdapter.setShowCheckBox(true);
				setContextButtonSelecteMode();
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, ib_select_all,
        		mContext.getString(R.string.msgs_log_file_list_label_select_all));

        ib_unselect_all.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mLogFileManagementAdapter.setAllItemChecked(false);
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, ib_unselect_all,
        		mContext.getString(R.string.msgs_log_file_list_label_unselect_all));

	};

	private void setContextButtonSelecteMode() {
		final TextView dlg_title=(TextView)mDialog.findViewById(R.id.log_file_list_dlg_title);
    	String sel=""+mLogFileManagementAdapter.getItemSelectedCount()+"/"+mLogFileManagementAdapter.getCount();
    	dlg_title.setText(sel);

    	final ImageButton dlg_done=(ImageButton)mDialog.findViewById(R.id.log_file_list_dlg_done);
    	dlg_done.setVisibility(ImageButton.VISIBLE);

		LinearLayout ll_prof=(LinearLayout) mDialog.findViewById(R.id.log_context_view);
		LinearLayout ll_delete=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_delete_view);
		LinearLayout ll_share=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_share_view);
		LinearLayout ll_select_all=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_select_all_view);
		LinearLayout ll_unselect_all=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_unselect_all_view);

		boolean deletable_log_selected=false;
		for(int i=0;i<mLogFileManagementAdapter.getCount();i++) {
			if (mLogFileManagementAdapter.getItem(i).isChecked && !mLogFileManagementAdapter.getItem(i).isCurrentLogFile) {
				deletable_log_selected=true;
				break;
			}
		}
		if (deletable_log_selected) ll_delete.setVisibility(LinearLayout.VISIBLE);
		else ll_delete.setVisibility(LinearLayout.GONE);

		if (mLogFileManagementAdapter.getItemSelectedCount()>0) ll_share.setVisibility(LinearLayout.VISIBLE);
		else ll_share.setVisibility(LinearLayout.GONE);

        ll_select_all.setVisibility(LinearLayout.VISIBLE);
        if (mLogFileManagementAdapter.isAnyItemSelected()) ll_unselect_all.setVisibility(LinearLayout.VISIBLE);
        else ll_unselect_all.setVisibility(LinearLayout.GONE);
	};

	private void setContextButtonNormalMode() {
		final TextView dlg_title=(TextView)mDialog.findViewById(R.id.log_file_list_dlg_title);
		dlg_title.setText(mDialogTitle);

    	final ImageButton dlg_done=(ImageButton)mDialog.findViewById(R.id.log_file_list_dlg_done);
    	dlg_done.setVisibility(ImageButton.GONE);

		LinearLayout ll_prof=(LinearLayout) mDialog.findViewById(R.id.log_context_view);
		LinearLayout ll_delete=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_delete_view);
		LinearLayout ll_share=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_share_view);
		LinearLayout ll_select_all=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_select_all_view);
		LinearLayout ll_unselect_all=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_unselect_all_view);

		ll_delete.setVisibility(LinearLayout.GONE);
		ll_share.setVisibility(LinearLayout.GONE);

    	if (mLogFileManagementAdapter.isEmptyAdapter()) {
            ll_select_all.setVisibility(LinearLayout.GONE);
            ll_unselect_all.setVisibility(LinearLayout.GONE);
    	} else {
            ll_select_all.setVisibility(LinearLayout.VISIBLE);
            ll_unselect_all.setVisibility(LinearLayout.GONE);
    	}
	};

    private void showLogFile(CommonLogFileListAdapter lfm_adapter, int pos) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		try {
            if (Build.VERSION.SDK_INT>=24) {
                Uri uri= FileProvider.getUriForFile(mContext, mClog.getLogFileProviderAuth(), new File(lfm_adapter.getItem(pos).log_file_path));
                intent.setDataAndType(uri, "text/plain");
            } else {
                intent.setDataAndType(Uri.parse("file://"+lfm_adapter.getItem(pos).log_file_path), "text/plain");
            }
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
			mCommonDlg.showCommonDialog(false, "E", 
					mContext.getString(R.string.msgs_log_file_browse_app_can_not_found), e.getMessage(), null);
        } catch (Exception e) {
		    String ste=printStackTrace(e);
            CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
            mCommonDlg.showCommonDialog(false, "E",
                    mContext.getString(R.string.msgs_log_file_browse_app_error), e.getMessage()+"\n"+ste, null);
		}
    };

    private static String printStackTrace(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

    private void sendLogFile() {
		final String zip_file_name=getZipLogFilePath();
		
		int no_of_files=0;
		for (int i=0;i<mLogFileManagementAdapter.getCount();i++) {
			if (mLogFileManagementAdapter.getItem(i).isChecked) no_of_files++;
		}
		final String[] file_name=new String[no_of_files];
		int files_pos=0;
		for (int i=0;i<mLogFileManagementAdapter.getCount();i++) {
			if (mLogFileManagementAdapter.getItem(i).isChecked) {
				file_name[files_pos]=mLogFileManagementAdapter.getItem(i).log_file_path;
				files_pos++;
			}
		}
		final ThreadCtrl tc=new ThreadCtrl();
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
				tc.setDisabled();
			}
		});

		final ProgressBarDialogFragment pbdf=ProgressBarDialogFragment.newInstance(
				mContext.getString(R.string.msgs_log_file_list_dlg_send_zip_file_creating), 
				"",
				mContext.getString(R.string.msgs_common_dialog_cancel),
				mContext.getString(R.string.msgs_common_dialog_cancel));
		pbdf.showDialog(getFragmentManager(), pbdf, ntfy,true);
		Thread th=new Thread() {
			@Override
			public void run() {
				File lf=new File(zip_file_name);
				lf.delete();
				String[] lmp=LocalMountPoint.convertFilePathToMountpointFormat(mContext, file_name[0]);
				ZipUtil.createZipFile(mContext, tc,pbdf,zip_file_name,lmp[0],file_name);
				if (tc.isEnabled()) {
				    Intent intent=new Intent();
				    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    intent.setAction(Intent.ACTION_SEND);
//				    intent.setType("message/rfc822");  
//				    intent.setType("text/plain");
				    intent.setType("application/zip");
                    Uri uri=createLogFileUri(mContext, mClog.getLogDirName()+"/"+mClog.getLogFileName()+".txt");
				    intent.putExtra(Intent.EXTRA_STREAM, uri);
				    mFragment.getActivity().startActivity(intent);

				    mUiHandler.post(new Runnable(){
						@Override
						public void run() {
                            mLogFileManagementAdapter.setAllItemChecked(false);
                            mLogFileManagementAdapter.setShowCheckBox(false);
                            mLogFileManagementAdapter.notifyDataSetChanged();
							setContextButtonNormalMode();
						}
				    });
				} else {
					lf.delete();

					MessageDialogFragment mdf =MessageDialogFragment.newInstance(false, "W",
							mContext.getString(R.string.msgs_log_file_list_dlg_send_zip_file_cancelled),
			        		"");
			        mdf.showDialog(mFragment.getFragmentManager(), mdf, null);

				}
				pbdf.dismiss();
			};
		};
		th.start();
    };

    public Uri createLogFileUri(Context c, String fpath) {
        Uri uri=null;
        if (Build.VERSION.SDK_INT>=24) {
            uri= FileProvider.getUriForFile(mContext, mClog.getLogFileProviderAuth(), new File(fpath));
        } else {
            uri=Uri.parse("file://"+fpath);
        }
        return uri;
    }

    private void confirmDeleteLogFile() {
    	String delete_list="",sep="";
    	final ArrayList<String> file_path_list=new ArrayList<String>();
    	for (int i=0;i<mLogFileManagementAdapter.getCount();i++) {
    		CommonLogFileListItem item=mLogFileManagementAdapter.getItem(i);
    		if (item.isChecked && !item.isCurrentLogFile) {
    			delete_list+=sep+item.log_file_name;
    			sep="\n";
    			file_path_list.add(item.log_file_path);
    		}
    	}
    	
    	NotifyEvent ntfy=new NotifyEvent(null);
    	ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				for (int i=0;i<file_path_list.size();i++) {
					File lf=new File(file_path_list.get(i));
					lf.delete();
				}

                mLogFileManagementAdapter.setAllItemChecked(false);
                mLogFileManagementAdapter.setShowCheckBox(false);
				mLogFileList=CommonLogUtil.createLogFileList(mContext);
                mLogFileManagementAdapter.replaceDataList(mLogFileList);
                mLogFileManagementAdapter.notifyDataSetChanged();
				setContextButtonNormalMode();

			}

			@Override
			public void negativeResponse(Context c, Object[] o) {}
    	});
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(true, "W",
        		mContext.getString(R.string.msgs_log_file_list_delete_confirm_msg),
        		delete_list);
        cdf.showDialog(mFragment.getFragmentManager(),cdf,ntfy);
    };

    public void performRotateLog() {
        CommonLogUtil.rotateLogFile(mContext);
    }

    public void browseLogFile() {

        CommonLogUtil.flushLog(mContext);

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    if (Build.VERSION.SDK_INT>=24) {
                        Uri uri= FileProvider.getUriForFile(mContext, mClog.getLogFileProviderAuth(), new File(mClog.getLogDirName()+"/"+mClog.getLogFileName()+".txt"));
                        intent.setDataAndType(uri, "text/plain");
                    } else {
                        intent.setDataAndType(Uri.parse("file://"+mClog.getLogDirName()+"/"+mClog.getLogFileName()+".txt"), "text/plain");
                    }
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
                    mCommonDlg.showCommonDialog(false, "E",
                            mContext.getString(R.string.msgs_log_file_browse_app_can_not_found), e.getMessage(), null);
                } catch (Exception e) {
                    String ste=printStackTrace(e);
                    CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
                    mCommonDlg.showCommonDialog(false, "E",
                            mContext.getString(R.string.msgs_log_file_browse_app_error), e.getMessage()+"\n"+ste, null);
                }
            }
        }, 100);
    };

    public void rotateLogFile() {
        performRotateLog();

    	mUiHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				mLogFileManagementAdapter.setAllItemChecked(false);
				mLogFileManagementAdapter.setShowCheckBox(false);
				mLogFileList=CommonLogUtil.createLogFileList(mContext);
				mLogFileManagementAdapter.replaceDataList(mLogFileList);
				mLogFileManagementAdapter.notifyDataSetChanged();
				setContextButtonNormalMode();
			}
    	},200);
    };

    public void showDialog(FragmentManager fm, Fragment frag, NotifyEvent ntfy) {
    	mTerminateRequired=false;
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.add(frag,null);
	    ft.commitAllowingStateLoss();
	    mNotifyUpdateLogOption=ntfy;
    };


}
