package com.sentaroh.android.Utilities3;
/*
The MIT License (MIT)
Copyright (c) 2018 Sentaroh

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
                String strace = getStackTrace(ex);
                invokeActivity(strace);
                appUniqueProcess(ex, strace);
            } else {
                Log.v("AppUncaughtExceptionHandler", "Second crash detected.");
            }
        } catch(Exception e) {
            e.printStackTrace();
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
