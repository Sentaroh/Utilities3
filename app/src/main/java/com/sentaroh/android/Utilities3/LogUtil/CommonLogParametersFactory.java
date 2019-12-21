package com.sentaroh.android.Utilities3.LogUtil;

import android.content.Context;

public class CommonLogParametersFactory {
    private static CommonLogParameters clog=null;
    static public CommonLogParameters getLogParms(Context c) {
        if (clog==null) {
            clog=new CommonLogParameters();
            clog.init(c);
            clog.loadLogParameters(c);
        }
        return clog;
    }
}
