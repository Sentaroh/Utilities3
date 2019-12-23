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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

public class CommonLogParameters {
    static private Logger slf4jLog= LoggerFactory.getLogger(CommonLogParameters.class);

    static private int debug_level=1;
    static private boolean log_enabled=true;
    static private String log_dir_name="";
    static private String log_file_name="log_file";

    static private String log_file_provider_auth="";

    static public PrintWriter logWriter=null;

    static final private String LOG_OPTION_KEY_LOG_LEVEL="log_option_key_log_level";
    static final private String LOG_OPTION_KEY_LOG_ENABLE="log_option_key_log_enable";

    public void init(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        if (!prefs.contains(LOG_OPTION_KEY_LOG_LEVEL)) {
            SharedPreferences.Editor edit=prefs.edit();
            edit.putInt(LOG_OPTION_KEY_LOG_LEVEL, 1);
            edit.putBoolean(LOG_OPTION_KEY_LOG_ENABLE, false);
            edit.apply();
        }
        log_dir_name=c.getExternalFilesDirs(null)[0].getPath()+"/log";
        log_file_provider_auth=c.getPackageName()+".provider";
        appl_tag=c.getPackageName().substring(c.getPackageName().lastIndexOf(".")+1);
//        Log.v("CommonLogParameters","App tag="+appl_tag);
    }

    static public void loadLogParameters(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        debug_level=prefs.getInt(LOG_OPTION_KEY_LOG_LEVEL, 1);
        log_enabled=prefs.getBoolean(LOG_OPTION_KEY_LOG_ENABLE, false);
        setSlf4jLogOption();
    }

    static public void setLogOptionLogEnabled(Context c, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(LOG_OPTION_KEY_LOG_ENABLE, enabled).apply();
        log_enabled=enabled;
//        Log.v("CommonLogParameters","new log enabled="+log_enabled);
    }

    static public void setLogOptionLogLevel(Context c, int log_level) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putInt(LOG_OPTION_KEY_LOG_LEVEL, log_level).apply();
        debug_level=log_level;
        setSlf4jLogOption();
//        Log.v("CommonLogParameters","new log level="+log_level);
    }

    static public Logger getSlf4jLog() {
        return slf4jLog;
    }

    static public void setSlf4jLogOption() {
        boolean debug=false, error=false, warn=false, info=false, trace=false;
        if (debug_level==0) {
        } else if (debug_level==1) {
            error=warn=info=true;
        } else if (debug_level==2) {
            debug=error=warn=info=true;
        } else if (debug_level==3) {
            debug=error=warn=info=trace=true;
        }
        slf4jLog.setLogOption(debug, error, info, trace, warn);
    }

    static public void setLogFileProviderAuth(String auth) {log_file_provider_auth=auth;}
    static public String getLogFileProviderAuth() {return log_file_provider_auth;}

    static public int getLogLevel() {return debug_level;}

    static private long log_limit_size=10L*1024L*1024L;
    static public void setLogLimitSize(long size) {log_limit_size=size;}
    static public long getLogLimitSize() {return log_limit_size;}

    static private int log_max_file_count=10;
    static public void setLogMaxFileCount(int size) {log_max_file_count=size;}
    static public int getLogMaxFileCount() {return log_max_file_count;}

    static public boolean isLogEnabled() {return log_enabled;}

    static public String getLogDirName() {return log_dir_name;}

    static public String getLogFileName() {return log_file_name;}

    static private String appl_tag="CommonLogParameters";
    static public void setApplicationTag(String p) {appl_tag=p;}
    static public String getApplicationTag() {return appl_tag;}

    static private String log_reset="RESET", log_delete="DELETE", log_flush="FLUSH", log_rotate="ROTATE", log_send="SEND", log_close="CLOSE";
    static public String getLogIntentReset() {return log_reset;}
    static public String getLogIntentDelete() {return log_delete;}
    static public String getLogIntentSend() {return log_send;}
    static public String getLogIntentFlush() {return log_flush;}
    static public String getLogIntentRotate() {return log_rotate;}
    static public String getLogIntentClose() {return log_close;}
}
