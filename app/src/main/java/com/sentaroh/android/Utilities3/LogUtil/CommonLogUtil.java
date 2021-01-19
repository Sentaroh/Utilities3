package com.sentaroh.android.Utilities3.LogUtil;

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
import android.util.Log;

import com.sentaroh.android.Utilities3.MiscUtil;
import com.sentaroh.android.Utilities3.StringUtil;

import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CommonLogUtil {
	private Context mContext=null;
	
	private CommonLogParameters mClog =null;

	private String mLogIdent="";
	
	public CommonLogUtil(Context c, String li) {
		mContext=c;
		setLogId(li);
		mClog = CommonLogParametersFactory.getLogParms(c);
	};

	public CommonLogParameters getClog() {
	    return mClog;
    }

	public Logger getSlf4jLog() {
	    return mClog.getSlf4jLog();
    }

	final public void setLogId(String li) {
		mLogIdent=(li+"                 ").substring(0,16)+" ";
	};

    final public String getLogId() {
        return mLogIdent;
    };

    final public void closeLog() {
		closeLog(mContext);
	};

	final public static void closeLog(Context c) {
        CommonLogParameters clog= CommonLogParametersFactory.getLogParms(c);
        if (clog.isLogActivated()) CommonLogWriter.enqueue(clog, c, clog.getLogIntentClose(), "", true);
	};

	final public void resetLogReceiver() {
		resetLogReceiver(mContext);
	};

	final public static void resetLogReceiver(Context c) {
        CommonLogParameters clog= CommonLogParametersFactory.getLogParms(c);
        if (clog.isLogActivated()) CommonLogWriter.enqueue(clog, c, clog.getLogIntentReset(), "",  true);
	};

	final public void flushLog() {
        if (mClog.isLogActivated()) flushLog(mContext);
	};

	final public static void flushLog(Context c) {
        CommonLogParameters clog= CommonLogParametersFactory.getLogParms(c);
        if (clog.isLogActivated()) CommonLogWriter.enqueue(clog, c, clog.getLogIntentFlush(), "", true);
	};

	final public void rotateLogFile() {
        if (mClog.isLogActivated()) rotateLogFile(mContext);
	};

	final public static void rotateLogFile(Context c) {
        CommonLogParameters clog= CommonLogParametersFactory.getLogParms(c);
        if (clog.isLogActivated()) CommonLogWriter.enqueue(clog, c, clog.getLogIntentRotate(), "", true);
	};

    final public void deleteLogFile() {
        if (mClog.isLogActivated()) deleteLogFile(mContext);
	};

    final public static void deleteLogFile(Context c) {
        CommonLogParameters clog= CommonLogParametersFactory.getLogParms(c);
        if (clog.isLogActivated()) CommonLogWriter.enqueue(clog, c, clog.getLogIntentDelete(), "", true);
    };

    final public void addLogMsg(String cat, String... msg) {
//		Log.v("","lvl="+mClog.getDebugLevel()+", ena="+mClog.isLogEnabled());
		if (mClog.getLogLevel()>0 || mClog.isLogEnabled() || cat.equals("E")) {
            if (mClog.isLogActivated()) addLogMsg(mClog, mContext, mLogIdent, cat, msg);
		}
	};

	final public void addDebugMsg(int lvl, String cat, String... msg) {
		if (mClog.isLogEnabled() && mClog.getLogLevel()>=lvl ) {
            if (mClog.isLogActivated()) addDebugMsg(mClog, mContext, mLogIdent, lvl, cat, msg);
		}
	};

	final public String buildLogCatMsg(String log_id, String cat, String... msg) {
		StringBuilder log_msg=new StringBuilder(512);
		for (int i=0;i<msg.length;i++) log_msg.append(msg[i]);
		return cat.concat(" ").concat(log_id).concat(log_msg.toString());
	};

	final public String buildPrintLogMsg(String cat, String... msg) {
		return buildPrintLogMsg(mLogIdent, cat, msg);
	};

	final public String buildPrintLogMsg(String log_id, String cat, String... msg) {
		StringBuilder log_msg=new StringBuilder(512);
		for (int i=0;i<msg.length;i++) log_msg.append(msg[i]);
		StringBuilder print_msg=new StringBuilder(512);
		print_msg
		.append("M ")
		.append(cat)
		.append(" ")
		.append(StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis()))
		.append(" ")
		.append(log_id)
		.append(log_msg.toString());
		return print_msg.toString();
	};

	final static private void addLogMsg(CommonLogParameters clog, Context context, String log_id, String cat, String... msg) {
		StringBuilder log_msg=new StringBuilder(512);
		for (int i=0;i<msg.length;i++) log_msg.append(msg[i]);
		if (clog.isLogEnabled()) {
			StringBuilder print_msg=new StringBuilder(512);
			print_msg
			.append("M ")
			.append(cat)
			.append(" ")
			.append(StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis()))
			.append(" ")
			.append(log_id)
			.append(log_msg.toString());
            CommonLogWriter.enqueue(clog, context, clog.getLogIntentSend(), print_msg.toString(), false);
		}
		Log.v(clog.getApplicationTag(), cat.concat(" ").concat(log_id).concat(log_msg.toString()));
	};

	final static private void addDebugMsg(CommonLogParameters clog, Context context, String log_id, int lvl, String cat, String... msg) {
		StringBuilder print_msg=new StringBuilder(512);
			print_msg.append("D ");
			print_msg.append(cat);
		StringBuilder log_msg=new StringBuilder(512);
		for (int i=0;i<msg.length;i++) log_msg.append(msg[i]);
		if (clog.isLogEnabled()) {
			print_msg.append(" ")
			.append(StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis()))
			.append(" ")
			.append(log_id)
			.append(log_msg.toString());
            CommonLogWriter.enqueue(clog, context, clog.getLogIntentSend(), print_msg.toString(), false);
		}
		Log.v(clog.getApplicationTag(), cat.concat(" ").concat(log_id).concat(log_msg.toString()));
	};

	final public boolean isLogFileExists() {
		boolean result = false;
		result=isLogFileExists(mContext);
		if (mClog.isLogEnabled() && mClog.getLogLevel()>=2) addDebugMsg(2,"I","Log file exists="+result);
		return result;
	};

	final static public boolean isLogFileExists(Context c) {
        CommonLogParameters clog= CommonLogParametersFactory.getLogParms(c);
        if (!clog.isLogActivated()) return false;
        boolean result = false;
		File lf = new File(getLogFilePath(c));
		result=lf.exists();
		return result;
	};

    final public int getLogLevel() {
        return mClog.getLogLevel();
    }

	final public String getLogFilePath() {
		return getLogFilePath(mContext);
	};
	final static public String getLogFilePath(Context c) {
        CommonLogParameters clog= CommonLogParametersFactory.getLogParms(c);
		return clog.getLogDirName()+"/"+clog.getLogFileName()+".txt";
	};
	
    final static public ArrayList<CommonLogFileListItem> createLogFileList(Context c) {
    	ArrayList<CommonLogFileListItem> lfm_fl=new ArrayList<CommonLogFileListItem>();
        CommonLogParameters clog= CommonLogParametersFactory.getLogParms(c);
    	File lf=new File(clog.getLogDirName());
    	File[] file_list=lf.listFiles();
    	if (file_list!=null) {
    		for (int i=0;i<file_list.length;i++) {
    			if (file_list[i].getName().startsWith(clog.getLogFileName())) {
    				if (file_list[i].getName().startsWith(clog.getLogFileName()+"_20")) {
        		    	CommonLogFileListItem t=new CommonLogFileListItem();
        		    	t.log_file_name=file_list[i].getName();
        		    	t.log_file_path=file_list[i].getPath();
        		    	t.log_file_size=MiscUtil.convertFileSize(file_list[i].length());
        		    	t.log_file_last_modified=file_list[i].lastModified();
        		    	String lm_date=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(file_list[i].lastModified());
        		    	t.log_file_last_modified_date=lm_date.substring(0,10);
        		    	t.log_file_last_modified_time=lm_date.substring(11);
        		    	lfm_fl.add(t);
    				}
    			}
    		}
    		Collections.sort(lfm_fl,new Comparator<CommonLogFileListItem>(){
				@Override
				public int compare(CommonLogFileListItem arg0,
						CommonLogFileListItem arg1) {
					int result=0;
					long comp=arg1.log_file_last_modified-arg0.log_file_last_modified;
					if (comp==0) result=0;
					else if(comp<0) result=-1;
					else if(comp>0) result=1;
					return result;
				}
    			
    		});
    	}
    	if (lfm_fl.size()==0) {
    		CommonLogFileListItem t=new CommonLogFileListItem();
    		lfm_fl.add(t);
    	}
    	return lfm_fl;
    };

}
