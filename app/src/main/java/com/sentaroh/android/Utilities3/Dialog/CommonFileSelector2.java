package com.sentaroh.android.Utilities3.Dialog;
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
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sentaroh.android.Utilities3.MiscUtil;
import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.R;
import com.sentaroh.android.Utilities3.SafFile3;
import com.sentaroh.android.Utilities3.SafManager3;
import com.sentaroh.android.Utilities3.SafStorage3;
import com.sentaroh.android.Utilities3.StringUtil;
import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.Utilities3.ThemeUtil;
import com.sentaroh.android.Utilities3.TreeFilelist.TreeFilelistAdapter;
import com.sentaroh.android.Utilities3.TreeFilelist.TreeFilelistItem;
import com.sentaroh.android.Utilities3.Widget.CustomSpinnerAdapter;
import com.sentaroh.android.Utilities3.Widget.NonWordwrapTextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by sentaroh on 2018/03/04.
 */

public class CommonFileSelector2 extends DialogFragment {
    private boolean mScopedStorageMode =false;
    private final static String APPLICATION_TAG="FileSelectDialogFragment";

    private Dialog mDialog=null;
    private boolean mTerminateRequired=true;
    private Dialog mCreateDirDialog=null;
    private CommonFileSelector2 mFragment=null;
    private String mDialogLocalStorageId="";

    private String mDialogTitle="", mDialogLocalDir="", mDialogFileName="";
    private boolean mDialogEnableCreate=true;
    public final static int DIALOG_SELECT_CATEGORY_UNSPECIFIED=0;
    public final static int DIALOG_SELECT_CATEGORY_DIRECTORY=1;
    public final static int DIALOG_SELECT_CATEGORY_FILE=2;
    private int mDialogSelectCat=DIALOG_SELECT_CATEGORY_UNSPECIFIED;
    private boolean mDialogHideMp=false;

//    private boolean mDialogIncludeMp=false;

    private boolean mDialogSingleSelect=true;
    private boolean mDialogSelectedFilePathWithMountPoint=false;

    private boolean mDialogHideHiddenDirsFiles=false;
//    private CustomContextMenu mCcMenu=null;

    private boolean mDialogDisableInput=false;

    private NotifyEvent mNotifyEvent=null;

    private SafManager3 mSafFileMgr2 =null;
    private int mRestartStatus=0;

    private Activity mActivity=null;
    private Context mContext=null;

    private Handler mUiHandler=null;

    private static Logger log = LoggerFactory.getLogger(CommonFileSelector2.class);

    private boolean mDebug=false;

    public static CommonFileSelector2 newInstance(boolean scoped_storage_mode,
                                                  boolean enableCreate, boolean hideMp, int select_category,
                                                  boolean singleSelect, //boolean include_mp,
                                                  String lmp, String ldir, String file_name, String title) {
        log.debug("newInstance"+
                " scoped_storage_mode="+scoped_storage_mode+", enableCreate="+enableCreate+
                ", title="+title+", lmp="+lmp+", ldir="+ldir+", filename="+file_name+", singleSelect="+singleSelect);//+", include_mp="+include_mp);
        CommonFileSelector2 frag = new CommonFileSelector2();
        Bundle bundle = new Bundle();
        bundle.putBoolean("scoped_storage_mode", scoped_storage_mode);
        bundle.putString("title", title);
        bundle.putString("filename", file_name);
        bundle.putString("localStorageId", lmp);
        bundle.putString("ldir", ldir);
        bundle.putBoolean("enableCreate", enableCreate);
        bundle.putInt("selectCat", select_category);
        bundle.putBoolean("hideMp", hideMp);
        bundle.putBoolean("singleSelect", singleSelect);
        bundle.putBoolean("disableInput", false);
//        bundle.putBoolean("includeMp", include_mp);
        bundle.putBoolean("selectedFilePathWithMountPoint", false);
        bundle.putBoolean("hideHiddenDirsFiles", false);
        frag.setArguments(bundle);
        return frag;
    };

//Constructor for disable input field
    public static CommonFileSelector2 newInstance(boolean scoped_storage_mode,
                                                  boolean enableCreate, boolean hideMp, int select_category,
                                                  boolean singleSelect, boolean disableInput,
                                                  String lmp, String ldir, String file_name, String title) {
        log.debug("newInstance"+
                " scoped_storage_mode="+scoped_storage_mode+", enableCreate="+enableCreate+
                ", title="+title+", lmp="+lmp+", ldir="+ldir+", filename="+file_name+", singleSelect="+singleSelect);//+", include_mp="+include_mp);
        CommonFileSelector2 frag = new CommonFileSelector2();
        Bundle bundle = new Bundle();
        bundle.putBoolean("scoped_storage_mode", scoped_storage_mode);
        bundle.putString("title", title);
        bundle.putString("filename", file_name);
        bundle.putString("localStorageId", lmp);
        bundle.putString("ldir", ldir);
        bundle.putBoolean("enableCreate", enableCreate);
        bundle.putInt("selectCat", select_category);
        bundle.putBoolean("hideMp", hideMp);
        bundle.putBoolean("singleSelect", singleSelect);
        bundle.putBoolean("disableInput", disableInput);
//        bundle.putBoolean("includeMp", include_mp);
        bundle.putBoolean("selectedFilePathWithMountPoint", false);
        bundle.putBoolean("hideHiddenDirsFiles", false);
        frag.setArguments(bundle);
        return frag;
    };

    public void setNotifyEvent(NotifyEvent ntfy) {mNotifyEvent=ntfy;}

    public CommonFileSelector2() {
        if (mDebug) log.debug("Constructor(Default)");
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDebug) log.debug("onSaveInstanceState");
        if(outState.isEmpty()){
            outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
        }
        saveViewContents();
    };

    @Override
    final public void onConfigurationChanged(final Configuration newConfig) {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
        if (mDebug) log.debug("onConfigurationChanged");
        reInitViewWidget();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mDebug) log.debug("onCreateView");
        View view=super.onCreateView(inflater, container, savedInstanceState);
        CommonDialog.setDlgBoxSizeLimit(mDialog,true);
        return view;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity=getActivity();
        mContext=mActivity.getApplicationContext();

        mUiHandler=new Handler();
        Bundle bd=getArguments();
        mScopedStorageMode =bd.getBoolean("scoped_storage_mode");
        mDialogTitle=bd.getString("title");
        String w_fn=bd.getString("filename");
        if (w_fn.startsWith("/")) mDialogFileName=w_fn.substring(1);
        else mDialogFileName=w_fn;
        mDialogLocalStorageId=bd.getString("localStorageId");
        mDialogLocalDir="";//bd.getString("ldir");
        mDialogEnableCreate=bd.getBoolean("enableCreate");
        mDialogSelectCat=bd.getInt("selectCat");
        mDialogHideMp=bd.getBoolean("hideMp");
        mDialogSingleSelect=bd.getBoolean("singleSelect");
//        mDialogIncludeMp=bd.getBoolean("includeMp");
        mDialogSelectedFilePathWithMountPoint=bd.getBoolean("selectedFilePathWithMountPoint");
        mDialogHideHiddenDirsFiles=bd.getBoolean("hideHiddenDirsFiles");
        mDialogDisableInput=bd.getBoolean("disableInput");
        if (mDebug) log.debug("onCreate");

        if (savedInstanceState!=null) mRestartStatus=2;

        mSafFileMgr2 =new SafManager3(getActivity().getApplicationContext());
        mSafStorageList =mSafFileMgr2.getSafStorageList();

        mFragment=this;
        if (!mTerminateRequired) {
//            setRetainInstance(true);

            if (mDebug) log.debug("ScopedStorageMode="+mScopedStorageMode+
                    ", Create="+mDialogEnableCreate+
                    ", SelectCat="+mDialogSelectCat+
                    ", SingleSelect="+mDialogSingleSelect+
                    ", Title="+mDialogTitle+//", lurl="+ mDialogLocalMP +
//                    ", ldir="+mDialogLocalDir+
                    ", file name="+mDialogFileName);

//        	mCcMenu=new CustomContextMenu(getActivity().getResources(), this.getFragmentManager());
        }
    }

    @Override
    final public void onResume() {
        super.onResume();
        if (mDebug) log.debug("onResume restart="+mRestartStatus);
        if (mRestartStatus==1) {
        }
        mRestartStatus=1;
    };

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (mDebug) log.debug("onActivityResult entered");
//    }

    @Override
    final public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mDebug) log.debug("onActivityCreated");
    };
    @Override
    final public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mDebug) log.debug("onAttach");
    };
    @Override
    final public void onDetach() {
        super.onDetach();
        if (mDebug) log.debug("onDetach");
    };
    @Override
    final public void onStart() {
//    	CommonDialog.setDlgBoxSizeLimit(mDialog,true);
        super.onStart();
        if (mDebug) log.debug("onStart");
        if (mTerminateRequired) mDialog.cancel();
    };
    @Override
    final public void onStop() {
        super.onStop();
        if (mDebug) log.debug("onStop");
    };

    @Override
    public void onDestroyView() {
        if (mDebug) log.debug("onDestroyView");
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
//	    mCcMenu.cleanup();
        super.onDestroyView();
    }
    @Override
    public void onCancel(DialogInterface di) {
        if (mDebug) log.debug("onCancel");
//	    super.onCancel(di);
        if (!mTerminateRequired) {
            Button btnCancel = (Button) mDialog.findViewById(R.id.common_file_selector_btn_cancel);
            btnCancel.performClick();
        }
        super.onCancel(di);
    }
    @Override
    public void onDismiss(DialogInterface di) {
        if (mDebug) log.debug("onDismiss");
        super.onDismiss(di);
    }

    private ThemeColorList mThemeColorList=null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mDebug) log.debug("onCreateDialog");

        mDialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));

        mDialog.setCanceledOnTouchOutside(false);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mThemeColorList= ThemeUtil.getThemeColorList(getActivity());

        if (!mTerminateRequired) {
            initViewWidget(true);
            restoreViewContents();
            CommonDialog.setDlgBoxSizeLimit(mDialog,true);
        }
//    	setShowsDialog(true);

        return mDialog;
    };

    class SavedViewContentsValue {
        public String createDialogEditText=null;
        public int createDialogEditTextSelStart=0,createDialogEditTextSelEnd=0;
        public String mainDialogFilename=null;
        public String mainDialogDirName=null;
        public int mainDialogFilenameSelStart=0,mainDialogFilenameTextSelEnd=0;
        public int mainDialogDirNameSelStart=0,mainDialogDirNameTextSelEnd=0;

        public int[] mainDailogListViewPos=new int[]{-1,-1};
        public ArrayList<TreeFilelistItem> mainDailogListItems=null;
        public int mainDialogSpinnerPos=-1;
    }

    private SavedViewContentsValue mSavedViewContentsValue=null;
    private void resetSavedViewContents() {
        mSavedViewContentsValue=null;
    }

    private void saveViewContents() {
        if (mDebug) log.debug("saveViewContents");
        mSavedViewContentsValue=new SavedViewContentsValue();
        if (mCreateDirDialog!=null) {
            final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);
            mSavedViewContentsValue.createDialogEditText=etDir.getText().toString();
            mSavedViewContentsValue.createDialogEditTextSelStart=etDir.getSelectionStart();
            mSavedViewContentsValue.createDialogEditTextSelEnd=etDir.getSelectionEnd();
            mCreateDirDialog.dismiss();
        }
        EditText file_name = (EditText) mDialog.findViewById(R.id.common_file_selector_file_name);
        mSavedViewContentsValue.mainDialogFilename=file_name.getText().toString();
        mSavedViewContentsValue.mainDialogFilenameSelStart=file_name.getSelectionStart();
        mSavedViewContentsValue.mainDialogFilenameTextSelEnd=file_name.getSelectionEnd();

        final NonWordwrapTextView dir_name = (NonWordwrapTextView) mDialog.findViewById(R.id.common_file_selector_filepath);
        mSavedViewContentsValue.mainDialogDirName=dir_name.getText().toString();

        mSavedViewContentsValue.mainDailogListViewPos[0]=mTreeFileListView.getFirstVisiblePosition();
        if (mTreeFileListView.getChildAt(0)!=null)
            mSavedViewContentsValue.mainDailogListViewPos[1]=mTreeFileListView.getChildAt(0).getTop();
        mSavedViewContentsValue.mainDailogListItems=mTreeFilelistAdapter.getDataList();

        mSavedViewContentsValue.mainDialogSpinnerPos= mStorageSelectorSpinner.getSelectedItemPosition();
    };

    private void restoreViewContents() {
        if (mDebug) log.debug("restoreViewContents mSavedViewContentsValue="+mSavedViewContentsValue);
        if (mSavedViewContentsValue==null) return;
        final SavedViewContentsValue sv=mSavedViewContentsValue;
        Handler hndl=new Handler();
        hndl.postDelayed(new Runnable(){
            @Override
            public void run() {
                if (sv!=null && mCreateDirDialog!=null) {
                    Button btnCreate=(Button)mDialog.findViewById(R.id.common_file_selector_create_btn);
                    btnCreate.performClick();
                    final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);
                    etDir.setText(sv.createDialogEditText);
                    etDir.setSelection(sv.createDialogEditTextSelStart, sv.createDialogEditTextSelEnd);
                }
            }
        },10);
        if (mSavedViewContentsValue.mainDialogSpinnerPos!=-1)
            mStorageSelectorSpinner.setSelection(mSavedViewContentsValue.mainDialogSpinnerPos);

//        if (mDebug) log.debug("restoreViewContent mainDailogListItems="+mSavedViewContentsValue.mainDailogListItems);
        if (mSavedViewContentsValue.mainDailogListItems!=null){
            final TextView tv_empty = (TextView) mDialog.findViewById(R.id.common_file_selector_empty);
            if (mDebug) log.debug("restoreViewContent mainDailogListItems size="+mSavedViewContentsValue.mainDailogListItems.size());
            if (mSavedViewContentsValue.mainDailogListItems.size()==0) {
                tv_empty.setVisibility(TextView.VISIBLE);
                mTreeFileListView.setVisibility(TextView.GONE);
            } else {
                tv_empty.setVisibility(TextView.GONE);
                mTreeFileListView.setVisibility(TextView.VISIBLE);
                mTreeFilelistAdapter.setDataList(mSavedViewContentsValue.mainDailogListItems);
            }
            mTreeFileListView.setScrollingCacheEnabled(false);
            mTreeFileListView.setScrollbarFadingEnabled(false);


            mTreeFileListView.setSelectionFromTop(mSavedViewContentsValue.mainDailogListViewPos[0],
                    mSavedViewContentsValue.mainDailogListViewPos[1]);
            mTreeFilelistAdapter.notifyDataSetChanged();
        }
        resetSavedViewContents();
    };

    private void reInitViewWidget() {
        if (mDebug) log.debug("reInitViewWidget");
        if (!mTerminateRequired) {
            Handler hndl=new Handler();
            hndl.post(new Runnable(){
                @Override
                public void run() {
                    mDialog.hide();
                    saveViewContents();
                    if (mDialog.getWindow()!=null && mDialog.getWindow().getCurrentFocus()!=null) mDialog.getWindow().getCurrentFocus().invalidate();
                    initViewWidget(false);
                    restoreViewContents();
                    CommonDialog.setDlgBoxSizeLimit(mDialog,true);
                    mDialog.onContentChanged();
                    mDialog.show();
                }
            });
        }
    };

    @SuppressWarnings("deprecation")
    public static void setSpinnerBackground(Context c, Spinner spinner, boolean theme_is_light) {
        if (theme_is_light) spinner.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.spinner_color_background_light));
        else spinner.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.spinner_color_background));
    };

    private ListView mTreeFileListView=null;
    private TreeFilelistAdapter mTreeFilelistAdapter=null;
    private Spinner mStorageSelectorSpinner =null;

    private void setStorageSelectorSpinner(Spinner spinner, CustomSpinnerAdapter adapter) {
        if (!mScopedStorageMode) {
            setStorageSelectorSpinnerLegacyStorageModel(spinner, adapter);
        } else {
            setStorageSelectorSpinnerScopedStorage(spinner, adapter);
        }
    }

    private void setStorageSelectorSpinnerLegacyStorageModel(Spinner spinner, CustomSpinnerAdapter adapter) {
        int sel_no=0;
        int i=0;
        for(SafStorage3 item: mSafStorageList) {
            if (item.uuid.equals(mDialogLocalStorageId)) sel_no=i;
            adapter.add(item.description);
            i++;
        }
        spinner.setSelection(sel_no, false);
    }

    private ArrayList<SafStorage3> mSafStorageList =null;
    private void setStorageSelectorSpinnerScopedStorage(Spinner spinner, CustomSpinnerAdapter adapter) {
        int sel_no=0;
        int i=0;
        for(SafStorage3 item: mSafStorageList) {
            if (item.uuid.equals(mDialogLocalStorageId)) sel_no=i;
            adapter.add(item.description);
            i++;
        }
        spinner.setSelection(sel_no, false);
    }

    private void initViewWidget(final boolean init_file_list_required) {
        if (mDebug) log.debug("initViewWidget");

        if (mDebug) log.debug("Create="+mDialogEnableCreate+
                ", Title="+mDialogTitle+//", lurl="+ mDialogLocalMP +
                ", ldir="+mDialogLocalDir+", file name="+mDialogFileName);


        mDialog.setContentView(R.layout.common_file_selector_dlg);
        LinearLayout title_view=(LinearLayout)mDialog.findViewById(R.id.common_file_selector_dlg_title_view);
        title_view.setBackgroundColor(mThemeColorList.title_background_color);
        TextView title=(TextView)mDialog.findViewById(R.id.common_file_selector_dlg_title);
        title.setTextColor(mThemeColorList.title_text_color);
        title.setText(mDialogTitle);
        final TextView dlg_msg = (TextView) mDialog.findViewById(R.id.common_file_selector_dlg_msg);
//        dlg_msg.setVisibility(TextView.GONE);
//        final Button btnHome = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_home_dir_btn);
//        btnHome.setTextColor(mThemeColorList.text_color_primary);
//        btnHome.setVisibility(Button.VISIBLE);
        final Button btnCreate = (Button) mDialog.findViewById(R.id.common_file_selector_create_btn);
//        btnCreate.setTextColor(mThemeColorList.text_color_primary);
        final Button btnOk = (Button) mDialog.findViewById(R.id.common_file_selector_btn_ok);
//		btnOk.setTextColor(mThemeColorList.text_color_primary);
        final Button btnCancel = (Button)mDialog.findViewById(R.id.common_file_selector_btn_cancel);
//        btnCancel.setTextColor(mThemeColorList.text_color_primary);
        final Button btnRefresh = (Button) mDialog.findViewById(R.id.common_file_selector_refresh_btn);
//        btnRefresh.setTextColor(mThemeColorList.text_color_primary);
        final TextView tv_empty = (TextView) mDialog.findViewById(R.id.common_file_selector_empty);

        final Button btnTop = (Button)mDialog.findViewById(R.id.common_file_selector_top_btn);
//        btnTop.setTextColor(mThemeColorList.text_color_primary);
        if (ThemeUtil.isLightThemeUsed(getActivity())) btnTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_dark, 0, 0, 0);
        else btnTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_light, 0, 0, 0);

        final Button btnUp = (Button)mDialog.findViewById(R.id.common_file_selector_up_btn);
//        btnUp.setTextColor(mThemeColorList.text_color_primary);
        if (ThemeUtil.isLightThemeUsed(getActivity())) btnUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_dark, 0, 0, 0);
        else btnUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_light, 0, 0, 0);


        LinearLayout ll_dlg_view=(LinearLayout) mDialog.findViewById(R.id.common_file_selector_dlg_view);
//        ll_dlg_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);


        if (mDialogEnableCreate) {
            btnCreate.setVisibility(TextView.VISIBLE);
        } else {
            btnCreate.setVisibility(TextView.GONE);
        }

        mStorageSelectorSpinner =(Spinner) mDialog.findViewById(R.id.common_file_selector_storage_spinner);
        setSpinnerBackground(mContext, mStorageSelectorSpinner, ThemeUtil.isLightThemeUsed(getActivity()));
        mStorageSelectorSpinner.setVisibility(Spinner.VISIBLE);
        //	Root directory spinner
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        mStorageSelectorSpinner.setPrompt(mContext.getString(R.string.msgs_file_select_edit_local_mount_point));
        mStorageSelectorSpinner.setAdapter(adapter);

        mStorageSelectorSpinner.setOnItemSelectedListener(null);
        setStorageSelectorSpinner(mStorageSelectorSpinner, adapter);

        if (mDialogHideMp) mStorageSelectorSpinner.setVisibility(LinearLayout.GONE);
        else mStorageSelectorSpinner.setVisibility(LinearLayout.VISIBLE);
//		ll_mp.setVisibility(LinearLayout.GONE);

        //	final TextView v_spacer=(TextView)mDialog.findViewById(R.id.file_select_edit_dlg_spacer);
        mTreeFileListView = (ListView) mDialog.findViewById(R.id.common_file_selector_list);
        final NonWordwrapTextView dir_path = (NonWordwrapTextView) mDialog.findViewById(R.id.common_file_selector_filepath);
//        dir_path.setTextColor(mThemeColorList.text_color_primary);
        final LinearLayout ll_dir_name = (LinearLayout) mDialog.findViewById(R.id.common_file_selector_dir_name_view);
        final LinearLayout ll_file_name = (LinearLayout) mDialog.findViewById(R.id.common_file_selector_file_name_view);
        final TextView hdr_file_name = (TextView) mDialog.findViewById(R.id.common_file_selector_hdr_file_name);
        final EditText et_dir_name = (EditText) mDialog.findViewById(R.id.common_file_selector_dir_name);
        final EditText et_file_name = (EditText) mDialog.findViewById(R.id.common_file_selector_file_name);
        hdr_file_name.setVisibility(TextView.GONE);
        ll_dir_name.setVisibility(LinearLayout.GONE);
        ll_file_name.setVisibility(LinearLayout.VISIBLE);
        if (!mDialogSingleSelect) {
        } else {
            if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_UNSPECIFIED) {
            } else if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_DIRECTORY) {
            } else if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) {
            }
        }
        et_file_name.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent event) {
                if (//event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
                return false;
            }
        });

        mTreeFilelistAdapter= new TreeFilelistAdapter(mActivity, mDialogSingleSelect, true);
        mTreeFilelistAdapter.setDirectorySelectable(!(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE));
//        mTreeFilelistAdapter.setDirectorySelectable(false);
        mTreeFilelistAdapter.setSelectable(!(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE));
//        mTreeFilelistAdapter.setSelectable(false);
        mTreeFileListView.setAdapter(mTreeFilelistAdapter);


//        if (mDialogLocalMP.equals("")) mDialogLocalMP =adapter.getItem(0);

        NotifyEvent ntfy_file_list=new NotifyEvent(mContext);
        ntfy_file_list.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                if (tfl.size()==0) {
                    tv_empty.setVisibility(TextView.VISIBLE);
                    mTreeFileListView.setVisibility(TextView.GONE);
                } else {
                    tv_empty.setVisibility(TextView.GONE);
                    mTreeFileListView.setVisibility(TextView.VISIBLE);
                    mTreeFilelistAdapter.setDataList(tfl);
                }
                mTreeFileListView.setScrollingCacheEnabled(false);
                mTreeFileListView.setScrollbarFadingEnabled(false);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                tv_empty.setVisibility(TextView.VISIBLE);
                mTreeFileListView.setVisibility(TextView.GONE);

                mTreeFileListView.setScrollingCacheEnabled(false);
                mTreeFileListView.setScrollbarFadingEnabled(false);
            }
        });
        if (init_file_list_required) {
            String stg_name=mStorageSelectorSpinner.getSelectedItem().toString();
            SafStorage3 ss= getSafStorageFromName(stg_name);
            mDialogLocalDir=ss.saf_file.getPath();
            createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalDir, ntfy_file_list, true);
        }
        mTreeFileListView.setVisibility(TextView.INVISIBLE);
        tv_empty.setVisibility(TextView.GONE);

        setTopUpButtonEnabled(false);

        if (mSavedViewContentsValue!=null && mSavedViewContentsValue.mainDialogFilename!=null) {
            et_file_name.setText(mSavedViewContentsValue.mainDialogFilename);
            et_file_name.setSelection(
                    mSavedViewContentsValue.mainDialogFilenameSelStart,
                    mSavedViewContentsValue.mainDialogFilenameTextSelEnd);
            dir_path.setText(mSavedViewContentsValue.mainDialogDirName);
        } else {
            dir_path.setText(mDialogLocalDir);
            if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) {
                et_file_name.setText(mDialogFileName);
            } else {
                et_file_name.setVisibility(EditText.GONE);
//                mTreeFilelistAdapter.setDirectorySelectable(false);
//                if (mDialogLocalDir.length()>1) et_file_name.setText(mDialogLocalDir.substring(1));
//                else et_file_name.setText("");
            }
        }

        if (!mDialogSingleSelect) setButtonEnabled(mActivity, btnOk, false);
        else setButtonEnabled(mActivity, btnOk, true);

        final NotifyEvent cb_ntfy=new NotifyEvent(mContext);
        // set file list thread response listener
        cb_ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                int p=(Integer) o[0];
                boolean p_chk=(Boolean) o[1];
                String turl=(String) mStorageSelectorSpinner.getSelectedItem();
                if (mDialogSingleSelect) {
                    if (mTreeFilelistAdapter.getDataItem(p).isChecked() && !p_chk) {
                        if (p!=-1) {
                            if (mTreeFilelistAdapter.getDataItem(p).isChecked()) {
                                et_file_name.setText((mTreeFilelistAdapter.getDataItem(p).getName()));
                            }
                        }
                    }
                    if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) {
                        if(et_file_name.getText().length()>0) {
                            setButtonEnabled(mActivity, btnOk, true);
                            putDlgMsg(dlg_msg,"");
                        } else {
                            setButtonEnabled(mActivity, btnOk, false);
                            putDlgMsg(dlg_msg, mContext.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
                        }
                    } else {
                        if (mTreeFilelistAdapter.isDataItemIsSelected() || et_file_name.getText().length()>0) {
                            setButtonEnabled(mActivity, btnOk, true);
                            putDlgMsg(dlg_msg,"");
                        } else {
                            putDlgMsg(dlg_msg,mContext.getString(R.string.msgs_file_select_edit_dlg_directory_not_selected));
                            setButtonEnabled(mActivity, btnOk, false);
                        }
                    }
                } else {
//                    if (mTreeFilelistAdapter.getDataItem(p).isDir()) {
//                        dir_path.setText(turl+mTreeFilelistAdapter.getDataItem(p).getPath()+
//                                mTreeFilelistAdapter.getDataItem(p).getName()+"/");
//                    } else {
//                        dir_path.setText(turl+mTreeFilelistAdapter.getDataItem(p).getPath());
//                        et_file_name.setText(mTreeFilelistAdapter.getDataItem(p).getName());
//                    }
                    putDlgMsg(dlg_msg,"");
                    setButtonEnabled(mActivity, btnOk, true);
                }
            }
            @Override
            public void negativeResponse(Context c, Object[] o) {
                boolean checked=false;
                //			int p=(Integer) o[0];
                boolean p_chk=(Boolean) o[1];
                if (mDialogSingleSelect) {
                    if (p_chk) {
                        for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
                            if (mTreeFilelistAdapter.getDataItem(i).isChecked()) {
                                checked=true;
                                break;
                            }
                        }
                        if (checked) setButtonEnabled(mActivity, btnOk, true);
                        else setButtonEnabled(mActivity, btnOk, false);
                    }
                } else {
//					Log.v("","sel="+p_chk);
                    setButtonEnabled(mActivity, btnOk, false);
                    for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
                        if (mTreeFilelistAdapter.getDataItem(i).isChecked()) {
                            setButtonEnabled(mActivity, btnOk, true);
                            break;
                        }
                    }
                }
            }
        });
        mTreeFilelistAdapter.setCbCheckListener(cb_ntfy);

//        btnOk.setEnabled(false);
        if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE && mDialogFileName.equals("")) {
            setButtonEnabled(mActivity, btnOk, false);
            putDlgMsg(dlg_msg, mContext.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
        }
        if (mDialogDisableInput) {
            et_file_name.setEnabled(false);
            et_file_name.setVisibility(EditText.GONE);
            if (ThemeUtil.isLightThemeUsed(mActivity)) et_file_name.setTextColor(Color.BLACK) ;
            else et_file_name.setTextColor(Color.LTGRAY) ;
        } else {
            et_file_name.setEnabled(true);
            et_file_name.setVisibility(EditText.VISIBLE);
            if (ThemeUtil.isLightThemeUsed(mActivity)) et_file_name.setTextColor(Color.BLACK) ;
            else et_file_name.setTextColor(Color.LTGRAY) ;
        }
        et_file_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) {
                    if (s.length()>0) {
                        for(int i = s.length()-1; i >= 0; i--){
                            if(s.charAt(i) == '\n'){
                                s.delete(i, i + 1);
                                return;
                            }
                        }
                        if (s.charAt(s.length()-1)=='/' || s.charAt(s.length()-1)=='"'
                                || s.charAt(s.length()-1)==':'
                                || s.charAt(s.length()-1)=='\\'
                                || s.charAt(s.length()-1)=='*'
                                || s.charAt(s.length()-1)=='<'
                                || s.charAt(s.length()-1)=='>'
                                || s.charAt(s.length()-1)=='|') {
                            s.delete(s.length()-1, s.length());
                            Toast.makeText(mContext, mContext.getString(R.string.msgs_file_select_edit_remove_invalid_character_for_file_name), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (mDialogSingleSelect) {
                    if (s.length()!=0) {
                        setButtonEnabled(mActivity, btnOk, true);
                        putDlgMsg(dlg_msg, "");
                    } else {
                        if (mDialogDisableInput) {
                            setButtonEnabled(mActivity, btnOk, true);
                        } else {
                            setButtonEnabled(mActivity, btnOk, false);
                            putDlgMsg(dlg_msg, mContext.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
                        }
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        mTreeFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                final int pos=mTreeFilelistAdapter.getItem(idx);
                final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(pos);
                final String stg_name=(String) mStorageSelectorSpinner.getSelectedItem();
                SafStorage3 ss= getSafStorageFromName(stg_name);
                final String turl=ss.saf_file.getPath();
                if (mDebug) log.debug("TreeFileListView clicked pos="+pos+", name="+tfi.getName());
                if (tfi.isDir()) {
                    if (tfi.getSubDirItemCount()>=0) {
                        NotifyEvent ntfy=new NotifyEvent(mContext);
                        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context c, Object[] o) {
                                ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                                dir_path.setText(mDialogLocalDir);
                                if (tfl.size()==0) {
                                    tv_empty.setVisibility(TextView.VISIBLE);
                                    mTreeFileListView.setVisibility(TextView.GONE);
                                } else {
                                    tv_empty.setVisibility(TextView.GONE);
                                    mTreeFileListView.setVisibility(TextView.VISIBLE);
                                    mTreeFilelistAdapter.setDataList(tfl);
                                }
                                if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE && mDialogFileName.equals("")) {
                                    setButtonEnabled(mActivity, btnOk, false);
                                    putDlgMsg(dlg_msg, mContext.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
                                } else {
                                    setTopUpButtonEnabled(true);
//                                    et_file_name.setText("");
                                }
                                setButtonEnabled(mActivity, btnUp, true);
                                setButtonEnabled(mActivity, btnTop, true);
                            }

                            @Override
                            public void negativeResponse(Context c, Object[] o) {
                            }
                        });
                        String sep=tfi.getPath().endsWith("/")?"":"/";
                        mDialogLocalDir=tfi.getPath()+sep+tfi.getName();
                        createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalDir, ntfy, true);
                    }
                } else {
                    mTreeFilelistAdapter.setDataItemIsSelected(pos);

                    et_file_name.setText(mTreeFilelistAdapter.getDataItem(pos).getName());
                    if (mTreeFilelistAdapter.getDataItem(pos).isDir() && mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) setButtonEnabled(mActivity, btnOk, false);
                    else setButtonEnabled(mActivity, btnOk, true);
                }
            }
        });

        mTreeFileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick(AdapterView<?> items, View view, int idx, long id) {
                if (mDebug) log.debug("TreeFileListView long clicked idx="+idx);
                if (mTreeFilelistAdapter.isDirectorySelectable()) {
                    final int pos=mTreeFilelistAdapter.getItem(idx);
                    final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(pos);
                    mTreeFilelistAdapter.setAllItemUnchecked();
                    tfi.setChecked(true);
                    mTreeFilelistAdapter.notifyDataSetChanged();
                    cb_ntfy.notifyToListener(tfi.isChecked(), new Object[]{pos, !tfi.isChecked()});
                }
                return true;
            }
        });

        btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDebug) log.debug("TreeFileListView top button clicked");
                NotifyEvent ntfy_file_list=new NotifyEvent(mContext);
                ntfy_file_list.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
//                        mDialogLocalDir="";
                        dir_path.setText(mDialogLocalDir);
                        if (tfl.size()==0) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            mTreeFileListView.setVisibility(TextView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            mTreeFileListView.setVisibility(TextView.VISIBLE);
                            mTreeFilelistAdapter.setDataList(tfl);
                        }
                        setTopUpButtonEnabled(false);
//                        et_file_name.setText("");
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        tv_empty.setVisibility(TextView.VISIBLE);
                        mTreeFileListView.setVisibility(TextView.GONE);

                        mTreeFileListView.setScrollingCacheEnabled(false);
                        mTreeFileListView.setScrollbarFadingEnabled(false);
//                        et_file_name.setText("");
                    }
                });
                String stg_name=mStorageSelectorSpinner.getSelectedItem().toString();
                SafStorage3 ss=getSafStorageFromName(stg_name);
                mDialogLocalDir=ss.saf_file.getPath();
                createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalDir, ntfy_file_list, false);
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDebug) log.debug("TreeFileListView up button clicked");
                String stg_name=mStorageSelectorSpinner.getSelectedItem().toString();
                SafStorage3 ss= getSafStorageFromName(stg_name);
                String mp=ss.saf_file.getPath();
                String w_dir=dir_path.getText().toString().replace(mp,"");
                String c_dir=w_dir.startsWith("/")?w_dir.substring(1):w_dir;
                String new_dir="";
                if (c_dir.lastIndexOf("/")>=0) {
                    new_dir=c_dir.substring(0, c_dir.lastIndexOf("/"));
                } else {
                    new_dir="";
                }
                if (new_dir.equals("")) {
                    mDialogLocalDir=mp;
                    dir_path.setText(mDialogLocalDir);
                    setTopUpButtonEnabled(false);
                } else {
                    mDialogLocalDir=mp+"/"+new_dir;
                    dir_path.setText(mDialogLocalDir);
                    setTopUpButtonEnabled(true);
                }
                NotifyEvent ntfy_file_list=new NotifyEvent(mContext);
                ntfy_file_list.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
//                        et_file_name.setText("");
                        if (tfl.size()==0) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            mTreeFileListView.setVisibility(TextView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            mTreeFileListView.setVisibility(TextView.VISIBLE);
                            mTreeFilelistAdapter.setDataList(tfl);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        tv_empty.setVisibility(TextView.VISIBLE);
                        mTreeFileListView.setVisibility(TextView.GONE);

                        mTreeFileListView.setScrollingCacheEnabled(false);
                        mTreeFileListView.setScrollbarFadingEnabled(false);
//                        et_file_name.setText("");
                    }
                });
                createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalDir, ntfy_file_list, false);

            }
        });

        //Create button
        btnCreate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mDebug) log.debug("TreeFileListView create button clicked");
                NotifyEvent ntfy=new NotifyEvent(mContext);
                // set file list thread response listener
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        btnRefresh.performClick();
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {}

                });
                fileSelectEditDialogCreateBtn(mActivity, mContext,
                        mDialogLocalDir,
                        mTreeFilelistAdapter, ntfy,mTreeFileListView);

            }
        });
        //Refresh button
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mDebug) log.debug("TreeFileListView refresh button clicked");
                final String stg_name=(String) mStorageSelectorSpinner.getSelectedItem();
                SafStorage3 ss= getSafStorageFromName(stg_name);
                final String turl=ss.saf_file.getPath();
                NotifyEvent ntfy_file_list=new NotifyEvent(mContext);
                ntfy_file_list.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                        if (tfl.size()<1) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            mTreeFileListView.setVisibility(TextView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            mTreeFileListView.setVisibility(TextView.VISIBLE);
                        }
                        mTreeFilelistAdapter.setDataList(tfl);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        tv_empty.setVisibility(TextView.VISIBLE);
                        mTreeFileListView.setVisibility(TextView.GONE);

                        mTreeFileListView.setScrollingCacheEnabled(false);
                        mTreeFileListView.setScrollbarFadingEnabled(false);
                    }
                });
                createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalDir, ntfy_file_list, false);
            }
        });
        //OK button
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String stg_name=(String) mStorageSelectorSpinner.getSelectedItem();
                SafStorage3 ss= getSafStorageFromName(stg_name);
                if (mDialogSingleSelect) {
                    Uri file_uri=null;
                    String fp="", fd="", fn="";
                    TreeFilelistItem sel_fi=null;
                    for(TreeFilelistItem fi:mTreeFilelistAdapter.getDataList()) if (fi.isChecked()) sel_fi=fi;
                    String sel_dir=sel_fi!=null?sel_fi.getPath()+"/"+sel_fi.getName():mDialogLocalDir;
                    if (ss.isSafFile) {
                        if (sel_fi!=null && et_file_name.getText().length()==0) {
                            if (sel_fi.isDir()) {
                                sel_dir=sel_fi.getPath()+"/"+sel_fi.getName();
                                fp=sel_dir;
                                fd=sel_dir;
                            } else {
                                sel_dir=sel_fi.getPath();
                                fn=sel_fi.getName();
                                fp=sel_dir+"/"+fn;
                                fd=sel_dir;
                            }
                        } else {
                            sel_dir=mDialogLocalDir;
                            fn=et_file_name.getText().toString();
                            fp=sel_dir+"/"+fn;
                            fd=sel_dir;
                        }
                        if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_DIRECTORY) {
                            SafFile3 sf=new SafFile3(mContext, sel_dir);
                            file_uri=sf.getUri();
                        } else {
                            SafFile3 sf=new SafFile3(mContext, fp);
                            file_uri=sf.getUri();
                        }
                    } else {
                        if (sel_fi!=null && et_file_name.getText().length()==0) {
                            if (sel_fi.isDir()) {
                                sel_dir=sel_fi.getPath()+"/"+sel_fi.getName();
                                fp=sel_dir;
                                fd=sel_dir;
                            } else {
                                sel_dir=sel_fi.getPath();
                                fn=sel_fi.getName();
                                fp=sel_dir+"/"+fn;
                                fd=sel_dir;
                            }
                        } else {
                            sel_dir=mDialogLocalDir;
                            fn=et_file_name.getText().toString();
                            fp=sel_dir+"/"+fn;
                            fd=sel_dir;
                        }
                        if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_DIRECTORY) {
                            file_uri= Uri.parse("file://"+sel_dir);
                        } else {
                            file_uri= Uri.parse("file://"+sel_dir+"/"+et_file_name.getText().toString());
                        }
                    }
                    if (mDebug) log.debug("TreeFileListView ok button clicked, name="+file_uri.getPath());
                    if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(true, new Object[]{file_uri, fp, fd, fn, ss.saf_file.getPath()});
                } else {
                    int selected_item_count=0;
                    for(TreeFilelistItem fi:mTreeFilelistAdapter.getDataList()) if (fi.isChecked()) selected_item_count++;
                    Uri[] file_uri=new Uri[selected_item_count];
                    String[] fp=new String[selected_item_count];
                    String[] fd=new String[selected_item_count];
                    String[] fn=new String[selected_item_count];
                    if (ss.isSafFile) {
                        int pos=-1;
                        for(TreeFilelistItem fi:mTreeFilelistAdapter.getDataList()) {
                            if (fi.isChecked()) {
                                pos++;
                                if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_DIRECTORY) {
                                    SafFile3 sf=new SafFile3(mContext, mDialogLocalDir+"/"+fi.getName());
                                    file_uri[pos]=sf.getUri();
                                    fp[pos]=fi.getPath()+"/"+fi.getName();
                                    fd[pos]=fi.getPath();
                                } else {
                                    fd[pos]=fi.getPath()+"/"+fi.getName();;
                                    fn[pos]=et_file_name.getText().toString();
                                    fp[pos]=fd+"/"+fn;
                                    SafFile3 sf=new SafFile3(mContext, mDialogLocalDir+"/"+fi.getName()+"/"+fn);
                                    file_uri[pos]=sf.getUri();
                                }
                            }
                        }
                    } else {
                        int pos=-1;
                        for(TreeFilelistItem fi:mTreeFilelistAdapter.getDataList()) {
                            if (fi.isChecked()) {
                                pos++;
                                if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_DIRECTORY) {
                                    file_uri[pos]=Uri.parse("file://"+mDialogLocalDir+"/"+fi.getName());
                                    fp[pos]=fi.getPath()+"/"+fi.getName();
                                    fd[pos]=fi.getPath();
                                } else {
                                    fd[pos]=fi.getPath();
                                    fn[pos]=et_file_name.getText().toString();
                                    fp[pos]=fd+"/"+fn;
                                    file_uri[pos]=Uri.parse("file://"+mDialogLocalDir+"/"+fi.getName()+"/"+et_file_name.getText().toString());
                                }
                            }
                        }
                    }

                    String fp_list="";
                    if (mDebug) log.debug("TreeFileListView ok button clicked, name="+fp_list);
                    if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(true, new Object[]{file_uri, fp, fd, fn, ss.saf_file.getPath()});
                }
                mFragment.dismiss();
            }
        });
        // CANCELボタンの指定
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mDebug) log.debug("TreeFileListView cancel button clicked");
                mFragment.dismiss();
                if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(false, null);
            }
        });

        Handler hndl=new Handler();
        hndl.postDelayed(new Runnable(){
            @Override
            public void run() {
                mStorageSelectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Spinner spinner = (Spinner) parent;
                        String stg_name=(String) spinner.getSelectedItem();
                        SafStorage3 ss= getSafStorageFromName(stg_name);
                        mDialogLocalDir=ss.saf_file.getPath();
                        
                        NotifyEvent ntfy=new NotifyEvent(mContext);
                        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context c, Object[] o) {
                                ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                                if (tfl.size()<1) tfl.add(new TreeFilelistItem(mContext.getString(R.string.msgs_file_select_edit_dir_empty)));
                                mTreeFilelistAdapter.setDataList(tfl);
                                mTreeFilelistAdapter.notifyDataSetChanged();
                                setButtonEnabled(mActivity, btnCreate, true);
                                dir_path.setText(mDialogLocalDir);
                                Handler hndl_sel=new Handler();
                                hndl_sel.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        mTreeFileListView.setSelection(0);
                                    }
                                });
                            }

                            @Override
                            public void negativeResponse(Context c, Object[] o) {}
                        });
                        createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalDir, ntfy, false);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {}
                });
            }
        }, 100);
    };

    private void setButtonEnabled(Activity mActivity, Button btnOk, boolean enabled) {
//        if (mDebug) log.debug("button enabled="+enabled);
//        Thread.dumpStack();
        CommonDialog.setButtonEnabled(mActivity, btnOk, enabled);
    }

    private boolean isFileExists(Uri uri) {
        boolean result=false;
        try {
            InputStream is=mContext.getContentResolver().openInputStream(uri);
            result=true;
        } catch(Exception e) {

        }
        return result;
    }

    private SafStorage3 getSafStorageFromName(String name) {
        SafStorage3 ss=null;
        for(SafStorage3 item: mSafStorageList) {
            if (item.description.equals(name)) {
                ss=item;
                break;
            }
        }
        return ss;
    }

    private void setTopUpButtonEnabled(boolean p) {
        final Button btnTop = (Button)mDialog.findViewById(R.id.common_file_selector_top_btn);
        final Button btnUp = (Button)mDialog.findViewById(R.id.common_file_selector_up_btn);

        btnUp.setEnabled(p);
        btnTop.setEnabled(p);
        if (p) {
            btnUp.setAlpha(1);
            btnTop.setAlpha(1);
        } else {
            btnUp.setAlpha(0.4f);
            btnTop.setAlpha(0.4f);
        }
    };

    private void putDlgMsg(TextView msg, String txt) {
        if (txt.equals("")) {
//            msg.setVisibility(TextView.GONE);
            msg.setText("");
        } else {
//            msg.setVisibility(TextView.VISIBLE);
            msg.setText(txt);
        }
    };

    public void showDialog(boolean debug, FragmentManager fm, Fragment frag, NotifyEvent ntfy) {
        mDebug=debug;
        if (mDebug) log.debug("showDialog");
        mTerminateRequired=false;
        mNotifyEvent=ntfy;
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(frag,null);
        ft.commitAllowingStateLoss();
    };

    private void createLocalFilelist(final boolean fileOnly, final String dir, final NotifyEvent ntfy, final boolean show_pd_circle_delay) {
        SafFile3 sf= new SafFile3(mContext, dir);
        if (sf.isSafFile()) {
            File lf=new File(sf.getPath());
            if (lf.canRead()) createFileApiFilelist(fileOnly, lf, ntfy, show_pd_circle_delay);
            else createSafApiFilelist(fileOnly, sf, ntfy, show_pd_circle_delay);
        } else {
            createFileApiFilelist(fileOnly, sf.getFile(), ntfy, show_pd_circle_delay);
        }
    }

    private void  createFileApiFilelist(final boolean fileOnly, final File target_dir, final NotifyEvent ntfy, final boolean show_pd_circle_delay) {
        final Dialog pd= CommonDialog.showProgressSpinIndicator(getActivity());
        if (show_pd_circle_delay) {
            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    pd.show();
                }
            });
        } else {
            pd.show();
        }
        Thread th=new Thread(){
            @Override
            public void run() {
                if (mDebug) log.debug("createFileApiFilelist Thread started");
                final ArrayList<TreeFilelistItem> tfl = new ArrayList<TreeFilelistItem>(); ;
                String tdir,fp;
                File lf=target_dir;
                final File[]  ff = lf.listFiles();
                if (ff!=null) {
                    if (mDebug) log.debug("createFileApiFilelistNonThread list file size="+ff.length);
                    for (int i=0;i<ff.length;i++){
                        if (!ff[i].isHidden() || (ff[i].isHidden() && !mDialogHideHiddenDirsFiles)) {
                            if (ff[i].canRead()) {
                                int dirct=0;
                                if (ff[i].isDirectory()) {
                                    File[] sdc_list=ff[i].listFiles();
                                    if (sdc_list!=null) {
                                        if (mDebug) log.debug("createFileApiFilelistNonThread sub dir="+ff[i].getPath()+", count="+sdc_list.length);
                                        for (int j=0;j<sdc_list.length;j++) {
                                            if (!fileOnly) {
                                                if (sdc_list[j].isDirectory()) dirct++;
                                            } else dirct++;
//									dirct++;
                                        }
                                    }
                                }
                                TreeFilelistItem tfi= buildFileApiTreeFileListItem(ff[i], lf.getPath());
                                tfi.setSubDirItemCount(dirct);
                                if (!fileOnly) {
                                    if (ff[i].isDirectory()) tfl.add(tfi);
                                } else tfl.add(tfi);
                            }
                        }
                    }
                    Collections.sort(tfl);
                }
                if (mDebug) log.debug("createFileApiFilelistNonThread ended, file list size="+tfl.size());

                if (mDebug) log.debug("createFileApiFilelist Thread ended");
                mUiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        ntfy.notifyToListener(true, new Object[]{tfl});
                        pd.dismiss();
                    }
                });
            }
        };
        th.start();
    }

    private void  createSafApiFilelist(final boolean fileOnly, final SafFile3 target_dir, final NotifyEvent ntfy, final boolean show_pd_circle_delay) {
        final Dialog pd= CommonDialog.showProgressSpinIndicator(getActivity());
        if (show_pd_circle_delay) {
            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    pd.show();
                }
            });
        } else {
            pd.show();
        }
        Thread th=new Thread(){
            @Override
            public void run() {
                if (mDebug) log.debug("createSafApiFilelist Thread started");
                final ArrayList<TreeFilelistItem> tfl = new ArrayList<TreeFilelistItem>(); ;
                final ContentProviderClient cpc=target_dir.getContentProviderClient();
                try {
                    final SafFile3[]  ff = target_dir.listFiles(cpc);
                    if (ff!=null && ff.length>0) {
                        if (mDebug) log.debug("createSafApiFilelistNonThread list file size="+ff.length);
                        boolean first_notify_issued=false;
                        int count_notified=0;
                        for (int i=0;i<ff.length;i++){
                            if (!ff[i].isHidden() || (ff[i].isHidden() && !mDialogHideHiddenDirsFiles)) {
                                int dirct=0;
                                if (ff[i].isDirectory(cpc)) {
                                    if (!fileOnly) {
                                        SafFile3[] sdc_list=ff[i].listFiles(cpc);
                                        if (mDebug) log.debug("createSafApiFilelistNonThread sub dir="+ff[i].getPath()+", count="+sdc_list.length);
                                        if (sdc_list!=null)  for(SafFile3 sf:sdc_list) if (sf.isDirectory(cpc)) dirct++;
                                    } else {
                                        dirct=ff[i].getCount(cpc);
                                    }
                                }
                                final TreeFilelistItem tfi= buildSafApiTreeFileListItem(ff[i], target_dir.getPath(), cpc);
                                tfi.setSubDirItemCount(dirct);
                                mUiHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        if (!fileOnly) {
                                            if (tfi.isDir()) tfl.add(tfi);
                                        } else {
                                            tfl.add(tfi);
                                        }
                                    }
                                });
                                if (!first_notify_issued) {
                                    if (tfl.size()>0) {
                                        first_notify_issued=true;
                                        mUiHandler.post(new Runnable(){
                                            @Override
                                            public void run() {
                                                ntfy.notifyToListener(true, new Object[]{tfl});
                                            }
                                        });
                                    }
                                } else {
//                                    count_notified++;
//                                    if (count_notified>10) {
//                                        count_notified=0;
//                                    }
                                    mUiHandler.post(new Runnable(){
                                        @Override
                                        public void run() {
//                                            Collections.sort(tfl);
//                                            ntfy.notifyToListener(true, new Object[]{tfl});
                                            mTreeFilelistAdapter.createShowList();
//                                            mTreeFilelistAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                        if (!first_notify_issued) {
                            mUiHandler.post(new Runnable(){
                                @Override
                                public void run() {
                                    ntfy.notifyToListener(true, new Object[]{tfl});
                                }
                            });
                        }
//                        Collections.sort(tfl);
                    } else {
                        mUiHandler.post(new Runnable(){
                            @Override
                            public void run() {
                                ntfy.notifyToListener(true, new Object[]{tfl});
                            }
                        });
                    }
                    if (mDebug) log.debug("createSafApiFilelistNonThread ended, file list size="+tfl.size());

                    if (mDebug) log.debug("createSafApiFilelist Thread ended");
                    mUiHandler.post(new Runnable(){
                        @Override
                        public void run() {
//                            ntfy.notifyToListener(true, new Object[]{tfl});
                            Collections.sort(tfl);
                            mTreeFilelistAdapter.createShowList();
//                            mTreeFilelistAdapter.notifyDataSetChanged();
                            pd.dismiss();
                        }
                    });
                } finally {
                    cpc.release();
                }
            }
        };
        th.start();
    }

    private TreeFilelistItem buildFileApiTreeFileListItem(File fl, String fp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String tfs= MiscUtil.convertFileSize(fl.length());
        TreeFilelistItem tfi=null;
        String n_fp="/";
        if (fp.equals("")) n_fp="/";
        else if (!fp.startsWith("/")) n_fp="/"+fp;
        else n_fp=fp;
        if (fl.isDirectory()) {
            tfi=new TreeFilelistItem(fl.getName(),
                    sdf.format(fl.lastModified())+", ", true, 0,0,false,
                    fl.canRead(),fl.canWrite(),
                    fl.isHidden(), n_fp,0);
        } else {
            tfi=new TreeFilelistItem(fl.getName(), sdf.format(fl
                    .lastModified())+","+tfs, false, fl.length(), fl
                    .lastModified(),false,
                    fl.canRead(),fl.canWrite(),
                    fl.isHidden(), n_fp,0);
        }
        return tfi;
    };

    private TreeFilelistItem buildSafApiTreeFileListItem(SafFile3 fl, String fp, ContentProviderClient cpc) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        TreeFilelistItem tfi=null;
        String n_fp="/";
        if (fp.equals("")) n_fp="/";
        else if (!fp.startsWith("/")) n_fp="/"+fp;
        else n_fp=fp;
        if (fl.isDirectory()) {
            long last_mod=fl.lastModified(cpc);
            tfi=new TreeFilelistItem(fl.getName(),
                    StringUtil.convDateTimeTo_YearMonthDayHourMinSec(last_mod) +", ", true, 0,0,false,
                    true,true,
                    fl.isHidden(), n_fp,0);
        } else {
            long[] lm=fl.getLastModifiedAndLength(cpc);
            long last_mod=lm[0];
            long fsz=lm[1];
            String tfs= MiscUtil.convertFileSize(fsz);
            tfi=new TreeFilelistItem(fl.getName(), StringUtil.convDateTimeTo_YearMonthDayHourMinSec(last_mod)+","+tfs, false, fsz, last_mod, false,
                    true,true,
                    fl.isHidden(), n_fp,0);
        }
        return tfi;
    };

    //	static private void setCheckedTextView(final CheckedTextView ctv) {
//		ctv.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				ctv.toggle();
//			}
//		});
//	};
//
    private void fileSelectEditDialogCreateBtn(final Activity activity,
                                               final Context context,
                                               final String dir,
                                               final TreeFilelistAdapter tfa,
                                               final NotifyEvent p_ntfy, final ListView lv) {
        final String stg_name=(String) mStorageSelectorSpinner.getSelectedItem();
        final SafStorage3 ss= getSafStorageFromName(stg_name);

        mCreateDirDialog = new Dialog(activity);
        mCreateDirDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCreateDirDialog.setContentView(R.layout.single_item_input_dlg);
        final TextView dlg_title = (TextView) mCreateDirDialog.findViewById(R.id.single_item_input_title);
        dlg_title.setText(context.getString(R.string.msgs_file_select_edit_dlg_create));
        final TextView dlg_msg = (TextView) mCreateDirDialog.findViewById(R.id.single_item_input_msg);
        dlg_msg.setVisibility(TextView.VISIBLE);
        final TextView dlg_cmp = (TextView) mCreateDirDialog.findViewById(R.id.single_item_input_name);
        final Button btnOk = (Button) mCreateDirDialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btnCancel = (Button) mCreateDirDialog.findViewById(R.id.single_item_input_cancel_btn);
        final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);

        dlg_cmp.setText(context.getString(R.string.msgs_file_select_edit_parent_directory)+":"+dir);
        CommonDialog.setDlgBoxSizeCompact(mCreateDirDialog);
        btnOk.setEnabled(false);
        etDir.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0) {
                    File lf=new File(dir+"/"+s.toString());
//					Log.v("","fp="+lf.getPath());
                    if (lf.exists()) {
                        btnOk.setEnabled(false);
//                        dlg_msg.setVisibility(TextView.VISIBLE);
                        dlg_msg.setText(context.getString(
                                R.string.msgs_single_item_input_dlg_duplicate_dir));
                    } else {
                        btnOk.setEnabled(true);
//                        dlg_msg.setVisibility(TextView.GONE);
                        dlg_msg.setText("");
                    }
                }
            }

        });

        //OK button
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//				NotifyEvent
                final String creat_dir=etDir.getText().toString();
                final String n_path=dir+"/"+creat_dir;
                NotifyEvent ntfy=new NotifyEvent(context);
                ntfy.setListener(new NotifyEvent.NotifyEventListener(){
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        boolean rc_create=false;
                        SafFile3 sf= null;
                        if (ss.isSafFile) {
                            sf=new SafFile3(mContext, n_path);
                            sf.mkdirs();
                        } else {
                            sf=new SafFile3(mContext, n_path);
                            sf.mkdirs();
                        }
                        if (!sf.exists()) {
                            dlg_msg.setText(String.format(
                                    context.getString(R.string.msgs_file_select_edit_dlg_dir_not_created),
                                    etDir.getText()));
                            return;
                        } else {
                            if (mDebug) log.debug("fileSelectEditDialogCreateBtn Directory cretaed name="+n_path);
                            mCreateDirDialog.dismiss();
                            mCreateDirDialog=null;
                            p_ntfy.notifyToListener(true,
                                    new Object[]{etDir.getText().toString()});
                        }
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                CommonDialog cd=new CommonDialog(context, getFragmentManager());
                cd.showCommonDialog(true, "W", context.getString(R.string.msgs_file_select_edit_confirm_create_directory), n_path, ntfy);
            }
        });
        // CANCELボタンの指定
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCreateDirDialog.dismiss();
//				Log.v("","cancel create");
                mCreateDirDialog=null;
                p_ntfy.notifyToListener(false, null);
            }
        });
        mCreateDirDialog.show();
    };


}
