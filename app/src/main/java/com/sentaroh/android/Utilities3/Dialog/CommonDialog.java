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
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.preference.Preference;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.R;
import com.sentaroh.android.Utilities3.ThemeUtil;

public class CommonDialog {
	private FragmentManager mFragMgr =null;
	
	public CommonDialog(Context c, FragmentManager fm) {
		mFragMgr =fm;
	};
	
	public void showCommonDialog(
            final boolean negative, String type, String title, String msgtext,
            final NotifyEvent ntfy) {
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(negative, type, title, msgtext);
        cdf.showDialog(mFragMgr,cdf,ntfy);
	};

    static public Dialog showProgressSpinIndicator(Activity a) {
        final Dialog dialog=new Dialog(a, android.R.style.Theme_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_spin_indicator_dlg);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    final static public float toPixel(Resources res, int dip) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
        return px;
    }

    static public void setDlgBoxSizeCompact(Dialog dialog) {
		if (dialog==null) return;
        setDefaultDlgBoxSizeCompact(dialog);
        setDlgBoxPosition(dialog, (int)toPixel(dialog.getContext().getResources(), 80));
    };

    static private void setDefaultDlgBoxSizeCompact(Dialog dialog) {
        if (dialog==null) return;
        int w=dialog.getWindow().getWindowManager().getDefaultDisplay().getWidth();
        int h=dialog.getWindow().getWindowManager().getDefaultDisplay().getHeight();
        int nw=0;

        if (w>h) {//Landscape
            if (w>800) {
                if (w>=1200) nw=(w/3)*2;
                else nw=800;
            } else nw= LayoutParams.FILL_PARENT;
        } else {//Portrait
            nw= LayoutParams.FILL_PARENT;
        }
        dialog.getWindow().setLayout(nw, LayoutParams.WRAP_CONTENT);

    };

    static public void setDlgBoxSizeCompactWithInput(Dialog dialog) {
        if (dialog==null) return;
        setDlgBoxSizeCompactWithInput(dialog, (int)toPixel(dialog.getContext().getResources(), 80));

    };

    static public void setDlgBoxSizeCompactWithInput(Dialog dialog, int margin_pix) {
        if (dialog==null) return;
        setDefaultDlgBoxSizeCompact(dialog);

        setDlgBoxPosition(dialog, margin_pix);
    };

    static private void setDlgBoxPosition(Dialog dialog, int margin_pix) {
        WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
        lp.gravity= Gravity.TOP;
        lp.y=margin_pix;
        dialog.getWindow().setAttributes(lp);
    }

    static public void setDlgBoxSizeLimit(Dialog dlg, boolean set_max) {
		if (dlg==null) return;
		if (!set_max) {// W=fill_parent H=fill_parent
			setDlgBoxSizeCompact(dlg);
		} else {// W=fill_parent H=wrap_content
			dlg.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
	};

    static public void setDlgBoxSizeLimitWithInput(Dialog dlg, boolean set_max) {
        if (dlg==null) return;
        if (!set_max) {// W=fill_parent H=fill_parent
            setDlgBoxSizeCompactWithInput(dlg);
        } else {// W=fill_parent H=wrap_content
            dlg.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }
    };

    @SuppressWarnings("deprecation")
	static public void setDlgBoxSizeHeightMax(Dialog dlg) {
		if (dlg==null) return;
			dlg.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
	};

    static public void setMenuItemEnabled(Activity a, Menu menu, MenuItem menu_item, boolean enabled) {
        if (ThemeUtil.isLightThemeUsed(a)) {
            menu_item.setEnabled(enabled);
            SpannableString s = new SpannableString(menu_item.getTitle());
            if (enabled) s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
            else s.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s.length(), 0);
            menu_item.setTitle(s);
        } else {
            menu_item.setEnabled(enabled);
        }
    }

    static public void setPreferenceItemEnabled(Activity a, Preference menu_item, boolean enabled) {
        if (ThemeUtil.isLightThemeUsed(a)) {
            menu_item.setEnabled(enabled);
            SpannableString title = new SpannableString(menu_item.getTitle());
            if (enabled) title.setSpan(new ForegroundColorSpan(Color.BLACK), 0, title.length(), 0);
            else title.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, title.length(), 0);
            menu_item.setTitle(title);
        } else {
            menu_item.setEnabled(enabled);
        }
    }

    private final static float mEnableAlpha=1.0f;
    private final static float mDisableAlphaLight=0.6f;
    private final static float mDisableAlphaSpinner=0.4f;
    private final static float mDisableAlpha=0.7f;
    private final static float mDisableAlphaButton=0.4f;
    private final static float mDisableAlphaEditTextLight=1.0f;
    private final static float mDisableAlphaEditText=0.6f;
    public static void setButtonEnabled(Activity a, Button btn, boolean enabled) {
        setViewEnabled(a, btn, enabled);
    }

    public static void setViewEnabled(Activity a, View v, boolean enabled) {
        boolean isLight=ThemeUtil.isLightThemeUsed(a);
        if (v instanceof Spinner) {
            int cc=((Spinner)v).getChildCount();
            for(int i=0;i<cc;i++) {
                View cv=(View)((Spinner)v).getChildAt(i);
                if (cv!=null) {
                    if (isLight) cv.setAlpha((enabled?mEnableAlpha:mDisableAlphaLight));
                    else cv.setAlpha((enabled?mEnableAlpha:mDisableAlphaSpinner));
                }
            }
            v.setEnabled(enabled);
        } else if (v instanceof EditText) {
            if (isLight) v.setAlpha((enabled?mEnableAlpha:mDisableAlphaEditTextLight));
            else v.setAlpha((enabled?mEnableAlpha:mDisableAlphaEditText));
            v.setEnabled(enabled);
        } else if (v instanceof Button) {
            if (isLight) v.setAlpha((enabled?mEnableAlpha:mDisableAlphaButton));
            else v.setAlpha((enabled?mEnableAlpha:mDisableAlpha));
            v.setEnabled(enabled);
        } else {
            if (isLight) v.setAlpha((enabled?mEnableAlpha:mDisableAlphaLight));
            else v.setAlpha((enabled?mEnableAlpha:mDisableAlpha));
            v.setEnabled(enabled);
        }
    }

    public static void setViewEnabled(Context a, View v, boolean enabled) {
        boolean isLight=ThemeUtil.isLightThemeUsed(a);
        if (v instanceof Spinner) {
            int cc=((Spinner)v).getChildCount();
            for(int i=0;i<cc;i++) {
                View cv=(View)((Spinner)v).getChildAt(i);
                if (cv!=null) {
                    if (isLight) cv.setAlpha((enabled?mEnableAlpha:mDisableAlphaLight));
                    else cv.setAlpha((enabled?mEnableAlpha:mDisableAlphaSpinner));
                }
            }
            v.setEnabled(enabled);
        } else if (v instanceof EditText) {
            if (isLight) v.setAlpha((enabled?mEnableAlpha:mDisableAlphaEditTextLight));
            else v.setAlpha((enabled?mEnableAlpha:mDisableAlphaEditText));
            v.setEnabled(enabled);
        } else if (v instanceof Button) {
            if (isLight) v.setAlpha((enabled?mEnableAlpha:mDisableAlphaButton));
            else v.setAlpha((enabled?mEnableAlpha:mDisableAlpha));
            v.setEnabled(enabled);
        } else {
            if (isLight) v.setAlpha((enabled?mEnableAlpha:mDisableAlphaLight));
            else v.setAlpha((enabled?mEnableAlpha:mDisableAlpha));
            v.setEnabled(enabled);
        }
    }

    public static void setSpinnerBackground(Activity a, Spinner spinner) {
        if (ThemeUtil.isLightThemeUsed(a)) spinner.setBackground(a.getApplicationContext().getDrawable(R.drawable.spinner_color_background_light));
        else spinner.setBackground(a.getApplicationContext().getDrawable(R.drawable.spinner_color_background));
    }


    static public void showToastLong(Activity a, String msg) {
        Toast toast=showToast(a, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    static public void showToastShort(Activity a, String msg) {
        Toast toast=showToast(a, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    static public Toast getToastShort(Activity a, String msg) {
        Toast toast=showToast(a, msg, Toast.LENGTH_SHORT);
        return toast;
    }

    static public Toast getToastLong(Activity a, String msg) {
        Toast toast=showToast(a, msg, Toast.LENGTH_LONG);
        return toast;
    }

    static private Toast showToast(Activity a, String msg, int duration) {
        Toast toast=Toast.makeText(a, msg, duration);
        if (Build.VERSION.SDK_INT<30) {
            View tv=toast.getView();
            int fg_color= Color.DKGRAY, bg_color=Color.LTGRAY;
            if (ThemeUtil.isLightThemeUsed(a)) {
                fg_color=Color.WHITE;
                bg_color=0xff666666;//<-Color.DKGRAY 0xff444444;
            }
            GradientDrawable drawable = new GradientDrawable();
            drawable.setStroke(3, bg_color);
            drawable.setCornerRadius(22);
            drawable.setColor(bg_color);
            tv.setBackground(drawable);
//        tv.setBackgroundColor(bg_color);
            if (tv instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup)tv;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View cv = vg.getChildAt(i);
                    if (cv instanceof TextView) {
                        ((TextView) cv).setBackgroundColor(bg_color);
                        ((TextView) cv).setTextColor(fg_color);
                    }
                }
            }
        }
        return toast;
    }


//	public void fileOnlySelectWithCreate(String lurl, String ldir,
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		fileSelect(true,true,false,lurl,ldir,file_name,dlg_title,ntfy);
//	};
//	public void fileOnlySelectWithCreateLimitMP(String lurl, String ldir,
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		FileSelectDialogFragment fsdf=
//				FileSelectDialogFragment.newInstance(false, true, true, false, false, true, true, lurl, ldir,
//					file_name, dlg_title);
//		fsdf.showDialog(mFragMgr, fsdf, ntfy);
//	};
//	public void fileOnlySelectWithCreateHideMP(String lurl, String ldir,
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		fileSelect(true,true,true,lurl,ldir,file_name,dlg_title,ntfy);
//	};
//	public void fileOnlySelectWithoutCreate(String lurl, String ldir,
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		fileSelect(false,true,false,lurl,ldir,file_name,dlg_title,ntfy);
//	};
//	public void fileOnlySelectWithoutCreateHideMP(String lurl, String ldir,
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		fileSelect(false,true,true,lurl,ldir,file_name,dlg_title,ntfy);
//	};
//	public void fileSelect(boolean enableCreate,boolean fileOnly, boolean hideMp, final String lurl,
//			final String ldir, String file_name,String dlg_title, final NotifyEvent ntfy) {
//
//		boolean include_root=false;
//		FileSelectDialogFragment fsdf=
//				FileSelectDialogFragment.newInstance(false, enableCreate, fileOnly, hideMp, include_root,
//						true, lurl, ldir, file_name, dlg_title);
//		fsdf.showDialog(mFragMgr, fsdf, ntfy);
//	}

//    public void fileSelectorFileOnly(boolean inc_mp, String mount_point, String dir_name, String file_name, String title, NotifyEvent ntfy) {
//        boolean include_root=false;
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, false, false, CommonFileSelector.DIALOG_SELECT_CATEGORY_FILE,
//                        true, inc_mp, mount_point, dir_name, file_name, title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorFileOnlyWithCreate(boolean inc_mp, String mount_point, String dir_name, String file_name, String title, NotifyEvent ntfy) {
//        boolean include_root=false;
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, true, false, CommonFileSelector.DIALOG_SELECT_CATEGORY_FILE,
//                        true, inc_mp, mount_point, dir_name, file_name, title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorDirOnly(Boolean inc_mp, String mount_point, String dir_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, false, false, CommonFileSelector.DIALOG_SELECT_CATEGORY_DIRECTORY,
//                        true, inc_mp, mount_point, dir_name, "", title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorDirOnlyWithCreate(Boolean inc_mp, String mount_point, String dir_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, true, false, CommonFileSelector.DIALOG_SELECT_CATEGORY_DIRECTORY,
//                        true, inc_mp, mount_point, dir_name, "", title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorFileOnlyHideMP(Boolean inc_mp, String mount_point, String dir_name, String file_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, false, true, CommonFileSelector.DIALOG_SELECT_CATEGORY_FILE,
//                        true, inc_mp, mount_point, dir_name, file_name, title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorFileOnlyWithCreateHideMP(boolean inc_mp, String mount_point, String dir_name, String file_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, true, true, CommonFileSelector.DIALOG_SELECT_CATEGORY_FILE,
//                        true, inc_mp, mount_point, dir_name, file_name, title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorDirOnlyHideMP(boolean inc_mp, String mount_point, String dir_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, false, true, CommonFileSelector.DIALOG_SELECT_CATEGORY_DIRECTORY,
//                        true, inc_mp, mount_point, dir_name, "", title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorDirOnlyWithCreateHideMP(boolean inc_mp, String mount_point, String dir_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, true, true, CommonFileSelector.DIALOG_SELECT_CATEGORY_DIRECTORY,
//                        true, inc_mp, mount_point, dir_name, "", title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };

}
