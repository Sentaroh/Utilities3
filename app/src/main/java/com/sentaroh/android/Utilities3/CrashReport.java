package com.sentaroh.android.Utilities3;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sentaroh.android.Utilities3.Dialog.CommonDialog;
import com.sentaroh.android.Utilities3.Widget.NonWordwrapTextView;

public class CrashReport extends AppCompatActivity {
    private Context mContext=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("CrashReport","onCreate entered");
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.crash_report);
        mContext=getApplicationContext();
        Intent in=getIntent();
        showCrashReport(in);
    }

    private void showCrashReport(Intent in) {
        ThemeColorList tcl= ThemeUtil.getThemeColorList(this);
        TextView tv_title=(TextView)findViewById(R.id.crash_report_title);
        tv_title.setBackgroundColor(tcl.title_background_color);
        final NonWordwrapTextView tv_info=(NonWordwrapTextView)findViewById(R.id.crash_report_crash_info);
        tv_info.setBackgroundColor(tcl.text_background_color);

        tv_info.setWordWrapEnabled(false);
        tv_title.setText("App crash");
//        tv_info.setVisibility(TextView.GONE);
        if (in!=null) tv_info.setText(in.getStringExtra(CRASH_REPORT_KEY_APPINFO)+"\n\n"+in.getStringExtra(CRASH_REPORT_KEY_CRASHINFO));
        else tv_info.setText("No intent data");
        tv_info.invalidate();
        tv_info.requestLayout();

        final Button btn_cancel=(Button)findViewById(R.id.crash_report_cancel);
        final Button btn_share=(Button)findViewById(R.id.crash_report_share);
        final Button btn_copy=(Button)findViewById(R.id.crash_report_copy_to_clipboard);

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    String npe=null;
//                    npe.length();
                    Intent intent=new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
//                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"gm.developer.fhoshino@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Crash Report");
                    intent.putExtra(Intent.EXTRA_TEXT, tv_info.getText().toString());
                    mContext.startActivity(intent);
                } catch(Exception e) {
                    CommonDialog cd=new CommonDialog(mContext, getSupportFragmentManager());
                    cd.showCommonDialog(false, "E", "Share failed", e.getMessage(), null);
                }
            }
        });

        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.content.ClipboardManager cm=(android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cd=cm.getPrimaryClip();
                cm.setPrimaryClip(ClipData.newPlainText("CrashReport", tv_info.getText().toString()));
                Toast.makeText(mContext, "Copy to clipboard", Toast.LENGTH_LONG).show();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    final private static String CRASH_REPORT_KEY_APPINFO="appinfo";
    final private static String CRASH_REPORT_KEY_CRASHINFO="crashinfo";

    static public String getApplVersionNameCode(Context c) {
        String vn = "Unknown", pn="Unknown";
        int vc=-1;
        try {
            pn = c.getPackageName();
            PackageInfo packageInfo = c.getPackageManager().getPackageInfo(pn, PackageManager.GET_META_DATA);
            vn = packageInfo.versionName;
            vc = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //
        }
        return pn+" "+vn+"("+vc+")";
    }

    public static void setCrashInfo(Context c, Intent in, String info) {
        String app=getApplVersionNameCode(c);
        String sdk="SDK="+Build.VERSION.SDK_INT;
        String device="Manufacturer="+ Build.MANUFACTURER+", Model="+ Build.MODEL;
        in.putExtra(CRASH_REPORT_KEY_APPINFO, app+", "+sdk+", "+device);
        in.putExtra(CRASH_REPORT_KEY_CRASHINFO, info);
    }


}
