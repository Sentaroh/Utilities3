package com.sentaroh.android.Utilities3;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AppUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private boolean mCrashing=false;
    private Thread.UncaughtExceptionHandler defaultUEH;
    private Context mContext=null;
    public void init(Context c, AppUncaughtExceptionHandler aueh) {
        mContext=c;
        defaultUEH = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(aueh);
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            if (!mCrashing) {
                mCrashing = true;
                String strace=getStackTrace(ex);
                invokeActivity(strace);
                appUniqueProcess(ex, strace);
            } else {
                Log.v("AppUncaughtExceptionHandler","Second crash detected.");
            }
        } finally {
            defaultUEH.uncaughtException(thread, ex);
        }
    }
    public void appUniqueProcess(Throwable ex, String strace) {
    }

    private String getStackTrace(Throwable ex) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

    private void invokeActivity(String strace) {
        Intent in=new Intent(mContext, CrashReport.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |   Intent.FLAG_ACTIVITY_CLEAR_TOP);//Intent.FLAG_ACTIVITY_NEW_TASK);
        CrashReport.setCrashInfo(mContext, in, strace);
        mContext.startActivity(in);
    }

};
