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

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SafFile3 {
    private Context mContext;
    private Uri mUri;
    private String mPath="";
    private String mDocName;

    private SafFile3 mParentFile=null;

    private ArrayList<String> msg_array =new ArrayList<String>();

    private static Logger log = LoggerFactory.getLogger(SafFile3.class);

    private File mFile=null;
    private boolean mSafFile =true;

    private String mAppDirectoryFiles=null;
    private String mAppDirectoryCache=null;

    private String mUuid="";

    private boolean mBuildError=false;

    public final static String SAF_FILE_PRIMARY_UUID="primary";
    public final static String SAF_FILE_UNKNOWN_UUID="unknown";

    //private String mSafPrimaryStoragePrefix="/storage/emulated/0";
    private String mSafPrimaryStoragePrefix=null;
    public String getSafPrimaryStoragePrefix() {return mSafPrimaryStoragePrefix;}

//    public final static String SAF_FILE_PRIMARY_STORAGE_PREFIX="/storage/emulated/0";
    public final static String SAF_FILE_EXTERNAL_STORAGE_PREFIX="/storage/";
//    public final static String SAF_FILE_PRIMARY_STORAGE_ANDROID_APP_DIRECTORY="/storage/emulated/0/Android/data/%s";
//    public final static String SAF_FILE_PRIMARY_STORAGE_ANDROID_APP_DIRECTORY="%1$s/Android/data/%2$s";
//    public final static String SAF_FILE_EXTERNAL_STORAGE_ANDROID_APP_DIRECTORY="/storage/%1$s/Android/data/%2$s";
    public final static String SAF_FILE_DOCUMENT_TREE_URI_PREFIX="content://com.android.externalstorage.documents/tree/";

    static public boolean isAllFileAccessAvailable() {
        if (Build.VERSION.SDK_INT>=30) return true;
        else return false;
    }

    public SafFile3(Context context, String fpath) {
        if (fpath==null || fpath.length()==0) {
            putErrorMessage("SafFile3 build error, File path was empty");
            mBuildError=true;
            return;
        }
        if (context==null) {
            putErrorMessage("SafFile3 build error, context was empty");
            mBuildError=true;
            return;
        }
//        long b_time=System.currentTimeMillis();
        mSafPrimaryStoragePrefix=Environment.getExternalStorageDirectory().getPath(); // /storage/emulated/0
        boolean all_file_access=isAllFileAccessAvailable();
        mContext = context;
        String remove_redundant_separator=fpath;
        while(remove_redundant_separator.indexOf("//")>=0) {
            remove_redundant_separator=remove_redundant_separator.replaceAll("//","/");
        }
        String reformed_fp="";
        if (remove_redundant_separator.endsWith("/")) {
            reformed_fp=remove_redundant_separator.substring(0, remove_redundant_separator.length()-1);
        } else {
            reformed_fp=remove_redundant_separator;
        }
        String user_path_seg="";
        String rebuild_path="";

        if (reformed_fp.startsWith("/data")) {
            try {
                String file_path=context.getFilesDir().getCanonicalPath();
                String cache_path=context.getCacheDir().getCanonicalPath();
                File lf=new File(reformed_fp);
                String lf_path=lf.getCanonicalPath();
                mPath=lf_path;
                mFile=lf;
                mUuid="data";
                mSafFile =false;
                mDocName=lf.getName();
                mAppDirectoryFiles=file_path;
                mAppDirectoryCache=cache_path;
                if (!lf_path.startsWith(file_path) &&! lf_path.startsWith(cache_path)) {
                    putErrorMessage("SafFile3 build error, invalid File path. Path="+reformed_fp);
                    mBuildError=true;
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                putErrorMessage("SafFile3 build error, File path obtain error. error="+e.getMessage());
                mBuildError=true;
                return;
            }
        } else {
            boolean saf_mp_exists=isSafFileMountPointExists(reformed_fp);
            if (all_file_access && saf_mp_exists) {
                if (reformed_fp.startsWith(mSafPrimaryStoragePrefix)) {
                    user_path_seg=reformed_fp.replace(mSafPrimaryStoragePrefix,"");
                    rebuild_path=reformed_fp;
                    buildOsFile(rebuild_path, user_path_seg);
                } else {
                    String[] path_array=reformed_fp.split("/");
                    String t_uuid=path_array.length>=3?path_array[2]:"";
                    String t_user_path_seg=reformed_fp.replace("/"+path_array[1]+"/"+t_uuid,"");
                    user_path_seg=t_user_path_seg.startsWith("/")?t_user_path_seg.substring(1):t_user_path_seg;
                    rebuild_path=reformed_fp;
                    buildOsFile(rebuild_path, user_path_seg);
                }
            } else {
                if (reformed_fp.startsWith(mSafPrimaryStoragePrefix)) {
                    user_path_seg=reformed_fp.replace(mSafPrimaryStoragePrefix,"");
                    rebuild_path=reformed_fp;
                    buildOsFile(rebuild_path, user_path_seg);
                } else {
                    String[] path_array=reformed_fp.split("/");
                    String t_uuid=path_array.length>=3?path_array[2]:"";
                    String t_user_path_seg=reformed_fp.replace("/"+path_array[1]+"/"+t_uuid,"");
                    user_path_seg=t_user_path_seg.startsWith("/")?t_user_path_seg.substring(1):t_user_path_seg;
                    rebuild_path=reformed_fp;
                    buildSafStorage(rebuild_path, user_path_seg);
                }
            }
        }
    }

    private boolean isSafFileMountPointExists(String fp) {
        boolean saf_mp_exists=true;
        if (!fp.startsWith(mSafPrimaryStoragePrefix)) {
            String[] path_array=fp.split("/");
            String mp_path="/"+path_array[1]+"/"+path_array[2];
            File lf=new File(mp_path);
            saf_mp_exists=lf.exists();
        }
        return saf_mp_exists;
    }

    private void buildOsFile(String reduced_fpath, String user_path_seg) {
//        long b_time=System.currentTimeMillis();
        if (reduced_fpath.endsWith("/")) mPath=reduced_fpath.substring(0, reduced_fpath.length()-1);
        else mPath=reduced_fpath;

        mSafFile =false;

        if (mPath.startsWith(mSafPrimaryStoragePrefix)) {
            mUuid=SAF_FILE_PRIMARY_UUID;
        } else {
            String[] path_array=mPath.split("/");
            mUuid=path_array[2];
        }
//        File[] app_file_list=mContext.getExternalFilesDirs(null);
//        File app_specfic=getAppDirectoryFile(app_file_list, mUuid);
        File app_specfic=getAppDirectoryFile(mUuid);
        if (app_specfic!=null) {
            String p_path=app_specfic.getParent();
            mAppDirectoryFiles=p_path+"/files";
            mAppDirectoryCache=p_path+"/cache";
        }
        mFile=new File(mPath);

        if (user_path_seg.lastIndexOf("/")>=0) {
            mDocName=user_path_seg.substring(user_path_seg.lastIndexOf("/")+1);
        } else {
            mDocName=user_path_seg;
        }
//        log.info("elapsed4="+(System.currentTimeMillis()-b_time));
    }

    private void buildSafStorage(String reduced_fpath, String user_path_seg) {
        if (reduced_fpath.endsWith("/")) mPath=reduced_fpath.substring(0, reduced_fpath.length()-1);
        else mPath=reduced_fpath;

        if (mPath.startsWith(mSafPrimaryStoragePrefix)) {
            mUuid=SAF_FILE_PRIMARY_UUID;
        } else {
            String[] path_array=mPath.split("/");
            mUuid=path_array[2];
        }
        mSafFile =true;

        if (user_path_seg.lastIndexOf("/")>=0) {
            mDocName=user_path_seg.substring(user_path_seg.lastIndexOf("/")+1);
        } else {
            mDocName=user_path_seg;
        }
        String u_path=user_path_seg.startsWith("/")?user_path_seg.substring(1):user_path_seg;

        mUri=buildSafUri(mUuid, u_path);

//        File[] app_file_list=mContext.getExternalFilesDirs(null);
//        File app_specfic=getAppDirectoryFile(app_file_list, mUuid);
        File app_specfic=getAppDirectoryFile(mUuid);
        if (app_specfic!=null) {
            String p_path=app_specfic.getParent();
            mAppDirectoryFiles=p_path+"/files";
            mAppDirectoryCache=p_path+"/cache";
        }

    }

    public String getAppDirectoryFiles() {
        if (isBuildError()) return null;
        return mAppDirectoryFiles;
    }

    public String getAppDirectoryCache() {
        if (isBuildError()) return null;
        return mAppDirectoryCache;
    }

    public InputStream getInputStream() throws Exception {
        if (isBuildError()) throw new Exception("Build error detected");
        if (mSafFile) {
            return mContext.getContentResolver().openInputStream(getUri());
        } else {
            FileInputStream fis=new FileInputStream(mFile);
            return fis;
        }
    }

    public InputStream getInputStreamByUri() throws Exception {
        if (isBuildError()) throw new Exception("Build error detected");
        return mContext.getContentResolver().openInputStream(getUri());
    }

    public OutputStream getOutputStream() throws Exception {
        if (isBuildError()) throw new Exception("Build error detected");
        if (mSafFile) {
            return mContext.getContentResolver().openOutputStream(getUri());
        } else {
            FileOutputStream fos=new FileOutputStream(mFile);
            return fos;
        }
    }

    public OutputStream getOutputStream(String mode) throws Exception {
        if (isBuildError()) throw new Exception("Build error detected");
        if (mSafFile) {
            return mContext.getContentResolver().openOutputStream(getUri(), mode);
        } else {
            putErrorMessage("File object was getOutputStream(String mode) not supported");
            return null;
        }
    }

    public SafFile3(Context c, Uri uri) {
        // For SafFile
//        String uuid= SafManager3.getUuidFromTreeUriPath(uri);
        if (uri==null) {
            putErrorMessage("SafFile3 build error, uri was empty");
            mBuildError=true;
            return;
        }
        if (c==null) {
            putErrorMessage("SafFile3 build error, context was empty");
            mBuildError=true;
            return;
        }
        mSafPrimaryStoragePrefix=Environment.getExternalStorageDirectory().getPath();
        boolean all_file_access=isAllFileAccessAvailable();

        if (all_file_access) {
            buildSafFileLegacyStorage(c, uri, null, all_file_access);
        } else {
            buildSafFileLegacyStorage(c, uri, null, all_file_access);
        }
    }

    public SafFile3(Context c, Uri uri, String name) {
        if (uri==null) {
            putErrorMessage("SafFile3 build error, uri was empty");
            mBuildError=true;
            return;
        }
        if (c==null) {
            putErrorMessage("SafFile3 build error, context was empty");
            mBuildError=true;
            return;
        }
        mSafPrimaryStoragePrefix=Environment.getExternalStorageDirectory().getPath();
        boolean all_file_access=isAllFileAccessAvailable();

        if (all_file_access) {
            buildSafFileLegacyStorage(c, uri, null, all_file_access);
        } else {
            buildSafFileLegacyStorage(c, uri, name, all_file_access);
        }
    }

    public SafFile3(Context c, boolean force_saf, Uri uri, String name) {
        if (uri==null) {
            putErrorMessage("SafFile3 build error, uri was empty");
            mBuildError=true;
            return;
        }
        if (c==null) {
            putErrorMessage("SafFile3 build error, context was empty");
            mBuildError=true;
            return;
        }
        mSafPrimaryStoragePrefix=Environment.getExternalStorageDirectory().getPath();
        boolean all_file_access=isAllFileAccessAvailable();

        if (!force_saf && all_file_access) {
            buildSafFileLegacyStorage(c, uri, null, all_file_access);
        } else {
            buildSafFileLegacyStorage(c, uri, name, false);
        }
    }

    private boolean isBuildError() {
        return mBuildError;
    }

    private void buildSafFileScopedStorage(Context c, Uri uri, String name) {
        mContext = c;
        mUri = uri;
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            mSafFile =true;
            mDocName=getLastPathSegementFromUri(uri.toString());

            mPath=uri.getPath();
            if (mPath.endsWith("/")) mPath=mPath.substring(0, mPath.length()-1);

            String u_path="";
            if (mPath.startsWith(mSafPrimaryStoragePrefix)) {
                mUuid=SAF_FILE_PRIMARY_UUID;
                String t_path=mPath.replace(mSafPrimaryStoragePrefix, "");
                u_path=t_path.startsWith("/")?t_path.substring(1):t_path;
            } else {
                String[] path_seg=mPath.split("/");
                mUuid=path_seg[2];
                String t_path=mPath.replace(SAF_FILE_EXTERNAL_STORAGE_PREFIX+mUuid, "");
                u_path=t_path.startsWith("/")?t_path.substring(1):t_path;
            }

            mUri=buildSafUri(mUuid, u_path);

//            File[] app_file_list=mContext.getExternalFilesDirs(null);
//            File app_specfic=getAppDirectoryFile(app_file_list, mUuid);
            File app_specfic=getAppDirectoryFile(mUuid);
            if (app_specfic!=null) {
                String p_path=app_specfic.getParent();
                mAppDirectoryFiles=p_path+"/files";
                mAppDirectoryCache=p_path+"/cache";
            }
        } else {
            mSafFile =true;
            mUuid= getUuidFromTreeUriPath(mUri);
            if (name!=null) mDocName=name;
            else mDocName=queryForString(mContext, mUri, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);

            if (uri.toString().startsWith(SAF_FILE_DOCUMENT_TREE_URI_PREFIX)) {
                if (mUuid.equals(SAF_FILE_PRIMARY_UUID)) mPath=mSafPrimaryStoragePrefix+"/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                else if (mUuid.equals(SAF_FILE_UNKNOWN_UUID)) mPath=uri.getPath();
                else mPath=SAF_FILE_EXTERNAL_STORAGE_PREFIX+mUuid+"/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
            } else {
                if (mUuid.equals(SAF_FILE_PRIMARY_UUID)) mPath=uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                else if (mUuid.equals(SAF_FILE_UNKNOWN_UUID)) mPath=uri.getPath();
                else mPath=uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
            }
            if (mPath.endsWith("/")) mPath=mPath.substring(0, mPath.length()-1);

            if (mDocName==null) mDocName=getLastPathSegementFromUri(uri.toString());

//            File[] app_file_list=mContext.getExternalFilesDirs(null);
//            File app_specfic=getAppDirectoryFile(app_file_list, mUuid);
            File app_specfic=getAppDirectoryFile(mUuid);
            if (app_specfic!=null) {
                String p_path=app_specfic.getParent();
                mAppDirectoryFiles=p_path+"/files";
                mAppDirectoryCache=p_path+"/cache";
            }
        }
    }

    private void buildSafFileLegacyStorage(Context c, Uri uri, String name, boolean all_file_access) {
        // For SafFile
        mContext = c;
        mUri = uri;
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            mSafFile =false;
            mDocName=getLastPathSegementFromUri(uri.toString());

            mPath=uri.getPath();
            if (mPath.endsWith("/")) mPath=mPath.substring(0, mPath.length()-1);
            mFile=new File(mPath);

            if (mPath.startsWith(mSafPrimaryStoragePrefix)) mUuid=SAF_FILE_PRIMARY_UUID;
            else {
                String[] path_seg=mPath.split("/");
                mUuid=path_seg[2];
            }
//            File[] app_file_list=mContext.getExternalFilesDirs(null);
//            File app_specfic=getAppDirectoryFile(app_file_list, mUuid);
            File app_specfic=getAppDirectoryFile(mUuid);
            if (app_specfic!=null) {
                String p_path=app_specfic.getParent();
                mAppDirectoryFiles=p_path+"/files";
                mAppDirectoryCache=p_path+"/cache";
            }
        } else {
            mUuid= getUuidFromTreeUriPath(mUri);
            boolean saf_mp_exists=false;
            if (!mUuid.equals(SafFile3.SAF_FILE_PRIMARY_UUID)) {
                File lf=new File(SafFile3.SAF_FILE_EXTERNAL_STORAGE_PREFIX+mUuid);
                if (lf.exists()) saf_mp_exists=true;
            } else {
                saf_mp_exists=true;
            }
            if (all_file_access && saf_mp_exists) {
                mSafFile =false;
                if (name!=null) mDocName=name;
                else mDocName=queryForString(mContext, mUri, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);

                if (uri.toString().startsWith(SAF_FILE_DOCUMENT_TREE_URI_PREFIX)) {
                    if (mUuid.equals(SAF_FILE_PRIMARY_UUID)) mPath=mSafPrimaryStoragePrefix+"/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                    else mPath=SAF_FILE_EXTERNAL_STORAGE_PREFIX+mUuid+"/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                    mFile=new File(mPath);
                } else {
                    if (mUuid.equals(SAF_FILE_PRIMARY_UUID)) mPath=uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                    else mPath=uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                    mFile=new File(mPath);
                }
                if (mPath.endsWith("/")) mPath=mPath.substring(0, mPath.length()-1);

                File app_specfic=getAppDirectoryFile(mUuid);
                if (app_specfic!=null) {
                    String p_path=app_specfic.getParent();
                    mAppDirectoryFiles=p_path+"/files";
                    mAppDirectoryCache=p_path+"/cache";
                }
            } else {
                mSafFile =true;
                if (name!=null) mDocName=name;
                else mDocName=queryForString(mContext, mUri, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);

                if (uri.toString().startsWith(SAF_FILE_DOCUMENT_TREE_URI_PREFIX)) {
                    if (mUuid.equals(SAF_FILE_PRIMARY_UUID)) mPath=mSafPrimaryStoragePrefix+"/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                    else if (mUuid.equals(SAF_FILE_UNKNOWN_UUID)) mPath=uri.getPath();
                    else mPath=SAF_FILE_EXTERNAL_STORAGE_PREFIX+mUuid+"/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                } else {
                    if (mUuid.equals(SAF_FILE_PRIMARY_UUID)) mPath=uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                    else if (mUuid.equals(SAF_FILE_UNKNOWN_UUID)) mPath=uri.getPath();
                    else mPath=uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
                }
                if (mPath.endsWith("/")) mPath=mPath.substring(0, mPath.length()-1);

                File app_specfic=getAppDirectoryFile(mUuid);
                if (app_specfic!=null) {
                    String p_path=app_specfic.getParent();
                    mAppDirectoryFiles=p_path+"/files";
                    mAppDirectoryCache=p_path+"/cache";
                }
            }
        }
    }

    public static Uri buildSafUri(String uuid, String file_path) {
        return Uri.parse(SAF_FILE_DOCUMENT_TREE_URI_PREFIX+uuid+"%3A/document/"+uuid+"%3A"+Uri.encode(file_path));
    }

    public static String getUuidFromFilePath(String fp) {
        if (fp.startsWith(SafManager3.getPrimaryStoragePath())) return SafFile3.SAF_FILE_PRIMARY_UUID;
        else {
            String[] fp_parts=fp.split("/");
            if (fp_parts[1].equals("storage")) {
                return fp_parts[2];
            } else {
                return SafFile3.SAF_FILE_UNKNOWN_UUID;
            }
        }
    }

    public static String getUuidFromTreeUriPath(Uri uri) {
//        log.debug("getUuidFromTreeUriPath uri="+uri.toString()+", path="+uri.getPath()+", encoded="+uri.getEncodedPath());
        String path=uri.getPath();
        String p_prefix=Environment.getExternalStorageDirectory().getPath();
        if (path.startsWith(p_prefix)) return SAF_FILE_PRIMARY_UUID;
        else {
            if (path.startsWith(SAF_FILE_EXTERNAL_STORAGE_PREFIX)) {
                String[] path_array=path.split("/");
                if (path.startsWith("/")) {
                    return path_array[2];
                } else {
                    return path_array[1];
                }
            } else if (path.startsWith("/tree/")) {
                return getUuidFromTreeUriPath(path);
            } else {
                return SAF_FILE_UNKNOWN_UUID;
            }
        }
    }

    private static String getUuidFromTreeUriPath(String uri_path) {
        String result=SAF_FILE_UNKNOWN_UUID;
        try {
            int semicolon = uri_path.lastIndexOf("%3A");
            if (semicolon<0) semicolon = uri_path.lastIndexOf(":");
            if (semicolon>0) {
                int idx=uri_path.substring(0,semicolon).lastIndexOf("/");
                result=uri_path.substring(idx+1,semicolon);
            } else {
                int idx=uri_path.substring(0,semicolon).lastIndexOf("/");
                result=uri_path.substring(idx+1,uri_path.length()-3);
            }
        } catch(Exception e) {}
        return result;
    }

    private File getAppDirectoryFile(File[] fl, String uuid) {
        File result=null;
        if (uuid.equals(SafManager3.SAF_FILE_PRIMARY_UUID)) {
            result=fl[0];
        } else {
            for(File file_item:fl) {
                if (file_item!=null && file_item.getPath()!=null && file_item.getPath().contains(uuid)) {
                    result=file_item;
                }
            }
        }
        return result;
    }

    private File getAppDirectoryFile(String uuid) {
        File result = null;
/*
        if (uuid.equals(SafManager3.SAF_FILE_PRIMARY_UUID)) {
            result=new File(mSafPrimaryStoragePrefix+"/Android/data/"+mContext.getApplicationInfo().packageName+"/files");
        } else {
            result=new File("/storage/"+uuid+"/Android/data/"+mContext.getApplicationInfo().packageName+"/files");
        }
*/
        String path = SafManager3.getAppSpecificDirectory(mContext, uuid);
        if (path != null) result = new File(path);
        return result != null && result.canRead() ? result:null;
    }

    private void putDebugMessage(String msg) {
        log.debug(msg);
    }

    private void putInfoMessage(String msg) {
        log.info(msg);
    }

    private String mLastErrorMessage="";
    private void putErrorMessage(String msg) {
        mLastErrorMessage=msg;
        log.error(msg);
    }

    public String getLastErrorMessage() {
        String em=mLastErrorMessage;
        mLastErrorMessage="";
        return em;
    }

    public boolean isSafFile() {
        return mSafFile;
    }

//    public void setParentFile(SafFile3 parent) {mParentFile=parent;}
//    public SafFile3 getParentFile() {return mParentFile;}

    public String getPath() {
        if (isBuildError()) return null;
        return mPath;
    }

    public static SafFile3 fromTreeUri(Context context, Uri treeUri) {
        return new SafFile3(context, prepareTreeUri(treeUri));
    }

    public static Uri prepareTreeUri(Uri treeUri) {
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));
    }

    public String toString() {
        if (isBuildError()) return "SafFile3 build error occured";
        if (mSafFile) {
            String result="Name="+mDocName+", Uri="+mUri.toString();
            if (mParentFile!=null) result+=", ParentUri="+mParentFile.getUri().toString();
            return result;
        } else {
            return "Path="+mFile.getPath();
        }
    }

//    public SafFile3 createFile(String mimeType, String displayName) {
//        if (mSafFile) {
//            Uri result=null;
//            try {
//                result= DocumentsContract.createDocument(mContext.getContentResolver(), mUri, mimeType, displayName);
//                if (log.isDebugEnabled()) putDebugMessage("saf_file#createFile result="+result);
//            } catch (Exception e) {
//                StackTraceElement[] st=e.getStackTrace();
//                String stm="";
//                for (int i=0;i<st.length;i++) {
//                    stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
//                }
//                putErrorMessage("createFile Failed to create file, Error="+e.getMessage()+stm);
//            }
//            SafFile3 saf=null;
//            if (result != null) saf=new SafFile3(mContext, result);
//            return saf;
//        } else {
//            String fp=mFile.getPath()+"/"+displayName;
//            File lf=new File(fp);
//            try {
//                lf.createNewFile();
//            } catch (IOException e) {
//                lf=null;
//                e.printStackTrace();
//            }
//            return new SafFile3(mContext, fp);
//        }
//    }

//    public SafFile3 createDirectory(String displayName) {
//        if (mSafFile) {
//            Uri result=null;
//            try {
//                result= DocumentsContract.createDocument(mContext.getContentResolver(), mUri, DocumentsContract.Document.MIME_TYPE_DIR, displayName);
//                if (log.isDebugEnabled()) putDebugMessage("saf_file#createDirectory result="+result);
//            } catch (Exception e) {
//                StackTraceElement[] st=e.getStackTrace();
//                String stm="";
//                for (int i=0;i<st.length;i++) {
//                    stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
//                }
//                putErrorMessage("saf_file#createDirectory Failed to create directory, Error="+e.getMessage()+stm);
//            }
//            return (result != null) ? new SafFile3(mContext, result) : null;
//        } else {
//            String fp=mFile.getPath()+"/"+displayName;
//            File lf=new File(fp);
//            if (lf.mkdirs()) return new SafFile3(mContext, fp);
//            return null;
//        }
//    }

    public Uri getUri() {
        if (isBuildError()) return null;
        return mUri;
    }

    public String getUuid() {
        if (isBuildError()) return null;
        return mUuid;
    }

    public String getName() {
        if (isBuildError()) return null;
        if (mSafFile) return mDocName;
        else return mFile.getName();
    }

    public String getMimeType() {
        if (isBuildError()) return null;
        if (mSafFile) {
            if (mDocName==null) return null;
//            final String rawType = getRawType(mContext, mUri);
//            if (DocumentsContract.Document.MIME_TYPE_DIR.equals(rawType)) {
//                return null;
//            } else {
//                return rawType;
//            }
            if (getName().lastIndexOf(".")>=0) {
                String ft=getName().substring(getName().lastIndexOf(".")+1);
                String mt= MimeTypeMap.getSingleton().getMimeTypeFromExtension(ft);
                return mt;
            }
            return null;
        } else {
            if (getName().lastIndexOf(".")>=0) {
                String ft=getName().substring(getName().lastIndexOf(".")+1);
                String mt= MimeTypeMap.getSingleton().getMimeTypeFromExtension(ft);
                return mt;
            }
            return null;
        }

    }

    private String getRawType(Context context, Uri mUri) {
        return queryForString(context, mUri, DocumentsContract.Document.COLUMN_MIME_TYPE, null);
    }

    private String getRawType(ContentProviderClient cpc, Uri mUri) {
        return queryForString(cpc, mUri, DocumentsContract.Document.COLUMN_MIME_TYPE, null);
    }

    public boolean isDirectory() {
        if (isBuildError()) return false;
        if (mSafFile) {
            return DocumentsContract.Document.MIME_TYPE_DIR.equals(getRawType(mContext, mUri));
        } else {
            return mFile.isDirectory();
        }
    }

    public boolean isDirectory(ContentProviderClient cpc) {
        if (isBuildError()) return false;
        if (mSafFile) {
            return DocumentsContract.Document.MIME_TYPE_DIR.equals(getRawType(cpc, mUri));
        } else {
            return mFile.isDirectory();
        }
    }

    public boolean isFile() {
        if (isBuildError()) return false;
        if (mSafFile) {
            final String type = getRawType(mContext, mUri);
            if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type) || TextUtils.isEmpty(type)) {
                return false;
            } else {
                return true;
            }
        } else {
            return mFile.isFile();
        }
    }

    public boolean isHidden() {
        if (isBuildError()) return false;
        if (mSafFile) {
            if (getName().startsWith(".")) return true;
            else return false;
        } else {
            return mFile.isHidden();
        }
    }

    public long[] getLastModifiedAndLength() {
        if (isBuildError()) return null;
        long[] result=new long[]{0,0};
        if (isSafFile()) {
            final ContentResolver resolver = mContext.getContentResolver();
            Cursor c = null;
            try {
                c = resolver.query(mUri, new String[] { DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_SIZE }, null, null, null);
                if (c.moveToFirst() && !c.isNull(0)) {
                    result[0]=c.getLong(0);
                    result[1]=c.getLong(1);
                }
            } catch (Exception e) {
                putErrorMessage("getLastModifiedAndLength Failed to Query, Error="+e.getMessage());
                return result;
            } finally {
                closeQuietly(c);
            }
        } else {
            result[0]=mFile.lastModified();
            result[1]=mFile.length();
        }
        return result;
    }

    public long[] getLastModifiedAndLength(ContentProviderClient cpc) {
        if (isBuildError()) return null;
        long[] result=new long[]{0,0};
        if (isSafFile()) {
            Cursor c = null;
            try {
                c = cpc.query(mUri, new String[] { DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_SIZE }, null, null, null);
                if (c.moveToFirst() && !c.isNull(0)) {
                    result[0]=c.getLong(0);
                    result[1]=c.getLong(1);
                }
            } catch (Exception e) {
                putErrorMessage("getLastModifiedAndLength Failed to Query, Error="+e.getMessage());
                return result;
            } finally {
                closeQuietly(c);
            }
        } else {
            result[0]=mFile.lastModified();
            result[1]=mFile.length();
        }
        return result;
    }

    public long lastModified() {
        if (isBuildError()) return -1L;
        if (mSafFile) {
            return queryForLong(mContext, mUri, DocumentsContract.Document.COLUMN_LAST_MODIFIED, 0);
        } else {
            return mFile.lastModified();
        }
    }

    public long lastModified(ContentProviderClient cpc) {
        if (isBuildError()) return -1L;
        if (mSafFile) {
            return queryForLong(cpc, mUri, DocumentsContract.Document.COLUMN_LAST_MODIFIED, 0);
        } else {
            return mFile.lastModified();
        }
    }

    public boolean setLastModified(long last_modified) {
        if (isBuildError()) return false;
        if (mSafFile) {
            boolean result=false;
            if (mPath.startsWith(mAppDirectoryFiles) || mPath.startsWith(mAppDirectoryCache)) {
                File lf=new File(mPath);
                result=lf.setLastModified(last_modified);
            } else {
                putErrorMessage("setLastModified not supported with SafFile");
            }
            return result;
        } else {
            return mFile.setLastModified(last_modified);
        }
    }

    public long length() {
        if (isBuildError()) return -1;
        if (mSafFile) {
            return queryForLong(mContext, mUri, DocumentsContract.Document.COLUMN_SIZE, 0);
        } else {
            return mFile.length();
        }
    }

    public long length(ContentProviderClient cpc) {
        if (isBuildError()) return -1L;
        if (mSafFile) {
            return queryForLong(cpc, mUri, DocumentsContract.Document.COLUMN_SIZE, 0);
        } else {
            return mFile.length();
        }
    }

    public boolean mkdir() {
        if (isBuildError()) return false;
        if (mSafFile) {
            Uri result=null;
            try {
                Uri p_path=getParentUri(getUri().toString());
                result= DocumentsContract.createDocument(mContext.getContentResolver(), p_path,
                        DocumentsContract.Document.MIME_TYPE_DIR, getLastPathSegementFromUri(mUuid, getUri().toString()));
                if (result!=null) mDocName=queryForString(mContext, mUri, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);
                if (log.isDebugEnabled()) putDebugMessage("saf_file#mkdir result="+result);
            } catch (Exception e) {
                StackTraceElement[] st=e.getStackTrace();
                String stm="";
                for (int i=0;i<st.length;i++) {
                    stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
                }
                putErrorMessage("saf_file#mkdir Failed to create directory, Error="+e.getMessage()+stm);
            }
            return result!=null?true:false;
        } else {
            return mFile.mkdir();
        }
    }

//    public boolean mkdirs() {
//        if (mSafFile) {
//            if (log.isDebugEnabled()) putDebugMessage("mkdirs target_path="+mPath);
//            SafFile3 rf=SafFile3.fromTreeUri(null, mContext, Uri.parse(SAF_FILE_DOCUMENT_TREE_URI_PREFIX+mUuid+"%3A"));
//            ContentProviderClient client =null;
//            boolean result=false;
//            try {
//                if (rf!=null) {
//                    if (!exists()) {
//                        client = mContext.getContentResolver().acquireContentProviderClient(rf.getUri().getAuthority());
//                        String t_path=mPath.replace("/saf/"+mUuid,"");
//                        if (!t_path.equals("") && !t_path.equals("/")) {
//                            SafFile3 saf=createItem(client, rf, mPath.replace("/saf/"+mUuid+"/",""));
//                            if (saf!=null && saf.getName()!=null) {
//                                mDocName=saf.getName();
//                                result=true;
//                            }
//                        }
//                    }
//                } else {
//                    putErrorMessage("createItem SafRoot file is null.");
//                }
//            } finally {
//                if (client!=null) client.release();
//            }
//            return result;
//        } else {
//            return mFile.mkdirs();
//        }
//    }

    public static String getUserPartFilePath(String uuid, String full_path) {
        String base_path="";
        if (uuid.equals(SAF_FILE_PRIMARY_UUID)) {
            String p_prefix=Environment.getExternalStorageDirectory().getPath();
            base_path=full_path.replace(p_prefix,"");
        } else {
            base_path=full_path.substring(full_path.indexOf(uuid)+uuid.length());
        }
        if (base_path.startsWith("/")) base_path=base_path.substring(1);
        return base_path;
    }

    public boolean mkdirs() {
        if (isBuildError()) return false;
        if (mSafFile) {
//            if (log.isDebugEnabled()) putDebugMessage("mkdirs target_path="+mPath);
            String base_path=getUserPartFilePath(mUuid, mPath);
            Uri base_uri=SafFile3.buildSafUri(mUuid, "");
            ContentProviderClient client =null;
            boolean result=false;
            try {
                client = mContext.getContentResolver().acquireContentProviderClient(base_uri.getAuthority());
                String[] path_seg=base_path.split("/");
                String path_accm="", sep="";
                for(String part:path_seg) {
                    if (part.length()>0) {
                        path_accm+=sep+part;
                        sep="/";
                        Uri uri_create=SafFile3.buildSafUri(mUuid, path_accm);
                        String doc_name=queryForString(client, uri_create, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);
                        if (doc_name==null) {
                            Uri next_base=createDocument(client, base_uri, DocumentsContract.Document.MIME_TYPE_DIR, part);
//                            Uri next_base=DocumentsContract.createDocument(mContext.getContentResolver(), base_uri,
//                                    DocumentsContract.Document.MIME_TYPE_DIR, part);
                            if (next_base==null) {
                                break;
                            }
                            base_uri=next_base;
                        } else {
                            base_uri=uri_create;
                        }
                    }
                }
                result=true;
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                if (client!=null) client.release();
            }
            return result;
        } else {
            return mFile.mkdirs();
        }
    }

    public boolean createNewFile() throws Exception {
        if (isBuildError()) throw new Exception("Build error detected");
        if (mSafFile) {
            Uri result=null;
            try {
                Uri p_path=getParentUri(getUri().toString());
                result= DocumentsContract.createDocument(mContext.getContentResolver(), p_path,
                        "", getLastPathSegementFromUri(mUuid, getUri().toString()));
                if (result!=null) mDocName=queryForString(mContext, mUri, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);
                if (log.isDebugEnabled()) putDebugMessage("createNewFile result="+result);
            } catch (Exception e) {
                StackTraceElement[] st=e.getStackTrace();
                String stm="";
                for (int i=0;i<st.length;i++) {
                    stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
                }
                putErrorMessage("createNewFile Failed to create file, Error="+e.getMessage()+stm);
            }
            return result!=null?true:false;
        } else {
            return mFile.createNewFile();
        }
    }

    public Context getContext() {return mContext;}

    public SafFile3 getParentFile() {
        if (isBuildError()) return null;
        if (isSafFile()) {
            if (mParentFile==null) mParentFile=new SafFile3(mContext, getParentUri(mUri.toString()));
        } else {
            if (mParentFile==null) mParentFile=new SafFile3(mContext, mFile.getParent());
        }
        return mParentFile;
    }

    public String getParent() {
        if (isBuildError()) return null;
        String ppath=getPath().substring(0, getPath().lastIndexOf("/"));
        return ppath;
    }

    private static Uri getParentUri(String uri) {
        String path="", hd="";
        if (uri.lastIndexOf("%3A")>=0) {
            path=uri.substring(uri.lastIndexOf("%3A")+3);
        } else if (uri.lastIndexOf(":")>=0) {
            path=uri.substring(uri.lastIndexOf(":")+1);
        }
        hd=uri.replace(path,"");
        String p_path="";
        if (path.lastIndexOf("%2F")>=0) {
            p_path=path.substring(0,path.lastIndexOf("%2F"));
        } else if (path.lastIndexOf("/")>=0) {
            p_path=path.substring(0,path.lastIndexOf("/"));
        }
        return Uri.parse(hd+p_path);
    }

    private static String getLastPathSegementFromUri(String uuid, String uri) {
        String fn="";
        String uri_path_key="/document/"+uuid+"%3A";
        if (uri.lastIndexOf(uri_path_key)>=0) {
            String t_path=uri.substring(uri.lastIndexOf(uri_path_key)+uri_path_key.length());
            if (t_path.lastIndexOf("%2F")>=0) fn=t_path.substring(t_path.lastIndexOf("%2F")+3);
            else fn=t_path;
        }
        return Uri.decode(fn);
    }

    private static String getLastPathSegementFromUri(String uri) {
        String lp="";
        String uri_path_key1="%3A";
        String uri_path_key2=":";
        String t_path="";
        if (uri.lastIndexOf(uri_path_key1)>=0) {
            t_path=uri.substring(uri.lastIndexOf(uri_path_key1)+uri_path_key1.length());
        } else {
            if (uri.lastIndexOf(uri_path_key2)>=0) {
                t_path=uri.substring(uri.lastIndexOf(uri_path_key2)+uri_path_key2.length());
            }
        }
        if (t_path.length()>0) {
            if (t_path.lastIndexOf("%2F")>=0) lp=t_path.substring(t_path.lastIndexOf("%2F")+3);
            else if (t_path.lastIndexOf("/")>=0) lp=t_path.substring(t_path.lastIndexOf("/")+1);
        }
        else lp=t_path;
        return Uri.decode(lp);
    }

    public boolean canRead() {
        if (isBuildError()) return false;
        if (mSafFile) {
            // Ignore if grant doesn't allow read
            if (mUri.getEncodedPath().endsWith(".android_secure")) return false;
            if (mContext.checkCallingOrSelfUriPermission(mUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            // Ignore documents without MIME
            if (TextUtils.isEmpty(getRawType(mContext, mUri))) {
                return false;
            }
            return true;
        } else {
            return mFile.canRead();
        }
    }

    public boolean canWrite() {
        if (isBuildError()) return false;
        if (mSafFile) {
            // Ignore if grant doesn't allow write
            if (mUri.getEncodedPath().endsWith(".android_secure")) return false;
            if (mContext.checkCallingOrSelfUriPermission(mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

            final String type = getRawType(mContext, mUri);
            final int flags = queryForInt(mContext, mUri, DocumentsContract.Document.COLUMN_FLAGS, 0);

            // Ignore documents without MIME
            if (TextUtils.isEmpty(type)) {
                return false;
            }

            // Deletable documents considered writable
            if ((flags & DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0) {
                return true;
            }

            if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type)
                    && (flags & DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE) != 0) {
                // Directories that allow create considered writable
                return true;
            } else if (!TextUtils.isEmpty(type)
                    && (flags & DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0) {
                // Writable normal files considered writable
                return true;
            }
            return false;
        } else {
            return mFile.canWrite();
        }
    }

    public boolean canWrite(ContentProviderClient cpc) {
        if (isBuildError()) return false;
        if (mSafFile) {
            // Ignore if grant doesn't allow write
            if (mUri.getEncodedPath().endsWith(".android_secure")) return false;
            if (mContext.checkCallingOrSelfUriPermission(mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

            final String type = getRawType(cpc, mUri);
            final int flags = queryForInt(cpc, mUri, DocumentsContract.Document.COLUMN_FLAGS, 0);

            // Ignore documents without MIME
            if (TextUtils.isEmpty(type)) {
                return false;
            }

            // Deletable documents considered writable
            if ((flags & DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0) {
                return true;
            }

            if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type)
                    && (flags & DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE) != 0) {
                // Directories that allow create considered writable
                return true;
            } else if (!TextUtils.isEmpty(type)
                    && (flags & DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0) {
                // Writable normal files considered writable
                return true;
            }
            return false;
        } else {
            return mFile.canWrite();
        }
    }

    public boolean deleteIfExists() {
        if (isBuildError()) return false;
        if (mSafFile) {
            boolean exists=false;
            boolean delete_success=false;
            ContentProviderClient client =null;
            try {
                client=mContext.getContentResolver().acquireContentProviderClient(getUri().getAuthority());
                delete_success=deleteIfExists(client);
            } finally {
                if (client!=null) client.release();
            }
            return delete_success;
        } else {
            if (mFile.exists()) return mFile.delete();
            return true;
        }
    }

    private boolean deleteIfExists(ContentProviderClient client) {
        if (isBuildError()) return false;
        boolean exists=false;
        boolean delete_success=false;
//        client=mContext.getContentResolver().acquireContentProviderClient(getUri().getAuthority());

        Cursor c = null;
        try {
            c = client.query(mUri, new String[] {DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
            exists=c.getCount() > 0;
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            closeQuietly(c);
        }
        if (exists) {
            try {
                final Bundle in = new Bundle();
                in.putParcelable(EXTRA_URI, getUri());
                client.call(METHOD_DELETE_DOCUMENT, null, in);
                delete_success=true;
            } catch (RemoteException e) {
//                e.printStackTrace();
            }
        } else {
            return true;
        }
        return delete_success;
    }


    public boolean delete() {
        if (isBuildError()) return false;
        if (mSafFile) {
            try {
                return DocumentsContract.deleteDocument(mContext.getContentResolver(), mUri);
            } catch (FileNotFoundException e) {
                return false;
            }
        } else {
            return mFile.delete();
        }
    }

    public boolean delete(ContentProviderClient cpc) {
        if (isBuildError()) return false;
        if (mSafFile) {
            try {
                deleteDocument(cpc, mUri);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return mFile.delete();
        }
    }

    public boolean exists() {
        if (isBuildError()) return false;
        if (mSafFile) {
            final ContentResolver resolver = mContext.getContentResolver();
            Cursor c = null;
            try {
                c = resolver.query(mUri, new String[] {DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
                return c.getCount() > 0;
            } catch (Exception e) {
                return false;
            } finally {
                closeQuietly(c);
            }
        } else {
            return mFile.exists();
        }
    }

    public boolean exists(Uri uri) {
        if (isBuildError()) return false;
        final ContentResolver resolver = mContext.getContentResolver();
        Cursor c = null;
        try {
            c = resolver.query(uri, new String[] {DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
            return c.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            closeQuietly(c);
        }
    }

    public boolean exists(ContentProviderClient cpc) {
        if (isBuildError()) return false;
        if (mSafFile) {
            Cursor c = null;
            try {
                c = cpc.query(mUri, new String[] {DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
                return c.getCount() > 0;
            } catch (Exception e) {
                return false;
            } finally {
                closeQuietly(c);
            }
        } else {
            return mFile.exists();
        }
    }

    public SafFile3[] listFiles() {
        if (isBuildError()) return null;
        if (mSafFile) {
            final ArrayList<FileListInfo> result = listDocUris(mContext, mUri);
            final SafFile3[] resultFiles = new SafFile3[result.size()];
            long b_time= System.currentTimeMillis();
            for (int i = 0; i < result.size(); i++) {
                Uri childlen_uri = DocumentsContract.buildDocumentUriUsingTree(mUri, result.get(i).doc_id);
                resultFiles[i] = new SafFile3(mContext, childlen_uri, result.get(i).doc_name);
            }
            return resultFiles;
        } else {
            File[] fl=mFile.listFiles();
            SafFile3[] resultFiles = null;
            if (fl!=null) {
                resultFiles = new SafFile3[fl.length];
                for (int i = 0; i < fl.length; i++) {
                    resultFiles[i] = new SafFile3(mContext, fl[i].getPath());
                }
            }
            return resultFiles;
        }
    }

    public ContentProviderClient getContentProviderClient() {
        if (isBuildError()) return null;
        return mUri==null?null:mContext.getContentResolver().acquireUnstableContentProviderClient(mUri);
    }

    public SafFile3[] listFiles(ContentProviderClient cpc) {
        if (isBuildError()) return null;
        if (mSafFile) {
            final ArrayList<FileListInfo> result = listDocUris(cpc, mUri);
            final SafFile3[] resultFiles = new SafFile3[result.size()];
            long b_time= System.currentTimeMillis();
            for (int i = 0; i < result.size(); i++) {
                Uri childlen_uri = DocumentsContract.buildDocumentUriUsingTree(mUri, result.get(i).doc_id);
                resultFiles[i] = new SafFile3(mContext, childlen_uri, result.get(i).doc_name);
            }
            return resultFiles;
        } else {
            File[] fl=mFile.listFiles();
            if (fl!=null) {
                final SafFile3[] resultFiles = new SafFile3[fl.length];
                for (int i = 0; i < fl.length; i++) {
                    resultFiles[i] = new SafFile3(mContext, fl[i].getPath());
                }
                return resultFiles;
            } else {
                return null;
            }
        }
    }

    public int getCount() {
        if (isBuildError()) return -1;
        if (mSafFile) {
            final ArrayList<FileListInfo> result = listDocUris(mContext, mUri);
            return result.size();
        } else {
            File[] fl=mFile.listFiles();
            return fl==null?0:fl.length;
        }
    }

    public int getCount(ContentProviderClient cpc) {
        if (isBuildError()) return -1;
        if (mSafFile) {
            final ArrayList<FileListInfo> result = listDocUris(cpc, mUri);
            return result.size();
        } else {
            File[] fl=mFile.listFiles();
            return fl==null?0:fl.length;
        }
    }

    public String[] list() {
        if (isBuildError()) return null;
        if (mSafFile) {
            final ArrayList<FileListInfo> result = listDocUris(mContext, mUri);
            final String[] resultFiles = new String[result.size()];
            for (int i = 0; i < result.size(); i++) {
                resultFiles[i] = this.getName()+"/"+result.get(i).doc_name;
            }
            return resultFiles;
        } else {
            File[] fl=mFile.listFiles();
            String[] resultFiles =null;
            if (fl!=null) {
                resultFiles = new String[fl.length];
                for (int i = 0; i < fl.length; i++) {
                    resultFiles[i] = fl[i].getPath();
                }
            }
            return resultFiles;
        }
    }

    public String[] list(ContentProviderClient cpc) {
        if (isBuildError()) return null;
        if (mSafFile) {
            final ArrayList<FileListInfo> result = listDocUris(cpc, mUri);
            final String[] resultFiles = new String[result.size()];
            for (int i = 0; i < result.size(); i++) {
                resultFiles[i] = this.getName()+"/"+result.get(i).doc_name;
            }
            return resultFiles;
        } else {
            File[] fl=mFile.listFiles();
            String[] resultFiles =null;
            if (fl!=null) {
                resultFiles = new String[fl.length];
                for (int i = 0; i < fl.length; i++) {
                    resultFiles[i] = fl[i].getPath();
                }
            }
            return resultFiles;
        }
    }

    public SafFile3 findFile(ContentProviderClient cpc, String name) {
        if (isBuildError()) return null;
        long b_time= System.currentTimeMillis();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mUri, DocumentsContract.getDocumentId(mUri));
        SafFile3 result=null;
        Cursor c = null;
        try {
            c = cpc.query(childrenUri, new String[] {
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME+"=?",
                    new String[]{name},
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME+" ASC");

//            putInfoMessage("saf_file#findFile name="+name+", count="+c.getCount());
            while (c.moveToNext()) {
                String doc_name=c.getString(1);
//                putInfoMessage("saf_file#findFile name="+doc_name+", key="+name);
                if (doc_name.equalsIgnoreCase(name)) {
                    String doc_id=c.getString(0);
                    Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(mUri, doc_id);
                    result=new SafFile3(mContext,  documentUri, doc_name);
//                    result.setParent(this.getUri());
                    break;
                }
            }
        } catch (Exception e) {
//            Log.w("saf_file", "saf_file#findFile Failed query: " + e);
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("saf_file#findFile Failed to Query, Error="+e.getMessage()+stm);
        } finally {
            closeQuietly(c);
        }
        if (log.isDebugEnabled()) putInfoMessage("saf_file#findFile elapased time="+(System.currentTimeMillis()-b_time));
        return result;
    }

    private ArrayList<FileListInfo> listDocUris(Context context, Uri uri) {
        long b_time= System.currentTimeMillis();
        final ContentResolver resolver = context.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
        final ArrayList<FileListInfo> results = new ArrayList<FileListInfo>();
        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null);
            while (c.moveToNext()) {
                FileListInfo info=new FileListInfo();
                info.doc_id=c.getString(0);
                info.doc_name=c.getString(1);
                results.add(info);
            }
        } catch (Exception e) {
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("saf_file#listDocUris Failed to Query, Error="+e.getMessage()+stm);
        } finally {
            closeQuietly(c);
        }
        if (log.isDebugEnabled()) log.trace("listDocUris elapsed time="+(System.currentTimeMillis()-b_time));
        return results;
    }

    private ArrayList<FileListInfo> listDocUris(ContentProviderClient cpc, Uri uri) {
        long b_time= System.currentTimeMillis();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
        final ArrayList<FileListInfo> results = new ArrayList<FileListInfo>();
        Cursor c = null;
        try {
            c = cpc.query(childrenUri, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null);
            while (c.moveToNext()) {
                FileListInfo info=new FileListInfo();
                info.doc_id=c.getString(0);
                info.doc_name=c.getString(1);
                results.add(info);
            }
        } catch (Exception e) {
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("saf_file#listDocUris Failed to Query, Error="+e.getMessage()+stm);
        } finally {
            closeQuietly(c);
        }
        if (log.isDebugEnabled()) log.trace("listDocUris elapsed time="+(System.currentTimeMillis()-b_time));
        return results;
    }

//    public boolean renameTo(String displayName) {
//        if (isBuildError()) return false;
//        if (mSafFile) {
//            Uri result=null;
//            try {
//                result = DocumentsContract.renameDocument(mContext.getContentResolver(), mUri, displayName);
////                log.info("result="+result);
//                mRenameToFle=null;
//                return true;
//            } catch (FileNotFoundException e) {
////                log.info("result="+result);
//                if (mRenameToFle==null || !mRenameToFle.exists()) {
//                    putErrorMessage("renameTo rename failed, msg="+e.getMessage());
//                    mRenameToFle=null;
//                    return false;
//                } else {
//                    mRenameToFle=null;
//                    return true;
//                }
//            }
//        } else {
//            putErrorMessage("renameTo(String displayName) was not supported non-SafFile");
//            return false;
//        }
//    }

    public File getFile() {
        return mFile;
    }

    public boolean renameTo(SafFile3 new_name) {
        if (isBuildError()) return false;
        if (mSafFile) {
            Uri result=null;
            try {
                result = DocumentsContract.renameDocument(mContext.getContentResolver(), mUri, new_name.getName());
                return true;
            } catch (Exception e) {
//                putErrorMessage("renameTo rename failed, msg="+e.getMessage()+"\n"+MiscUtil.getStackTraceString(e));
//                return false;
                if (!this.exists() && new_name.exists()) {
                    return true;
                } else {
                    putErrorMessage("renameTo rename failed, msg="+e.getMessage()+"\n"+MiscUtil.getStackTraceString(e));
                    return false;
                }
            }
        } else {
            return this.mFile.renameTo(new_name.getFile());
        }
    }

    public boolean moveTo(SafFile3 to_file) {
        if (isBuildError()) return false;
        if (mSafFile) {
            Uri move_result=null;
            try {
                if (log.isDebugEnabled()) putDebugMessage("moveTo mUri="+mUri.getPath()+", to_file="+to_file.getUri().getPath());
                Uri f_parent=getParentUri(mUri.toString());
                Uri t_parent=getParentUri(to_file.getUri().toString());
                move_result = DocumentsContract.moveDocument(mContext.getContentResolver(), mUri, f_parent, t_parent);
                mUri = move_result;
                if (mUri!=null) {
                    return true;
                } else {
                    putErrorMessage("moveTo move failed, to="+to_file);
                    return false;
                }
            } catch (FileNotFoundException e) {
                putErrorMessage("moveTo move failed, msg="+e.getMessage());
                return false;
            }
        } else {
            return mFile.renameTo(to_file.getFile());
        }
    }

    public boolean moveToWithRename(SafFile3 to_file) {
        if (isBuildError()) return false;
        if (mSafFile) {
            Uri move_result=null;
            try {
                if (log.isDebugEnabled()) putDebugMessage("moveTo mUri="+mUri.getPath()+", to_file="+to_file.getUri().getPath());
                if (to_file.exists()) {
                    putErrorMessage("moveTo failed. To file already exists.");
                    return false;
                }
                Uri f_parent=getParentUri(mUri.toString());
                Uri t_parent=getParentUri(to_file.getUri().toString());
                move_result = DocumentsContract.moveDocument(mContext.getContentResolver(), mUri, f_parent, t_parent);
                mUri = move_result;
                if (mUri!=null) {
                    if (!this.getName().toLowerCase().equals(to_file.getName().toLowerCase())) {
                        boolean result=this.renameTo(to_file);
                        return result;
                    } else {
                        return true;
                    }
                } else {
                    putErrorMessage("moveTo move failed, to="+to_file);
                    return false;
                }
            } catch (Exception e) {
                putErrorMessage("moveTo move failed, msg="+e.getMessage());
                return false;
            }
        } else {
            return mFile.renameTo(to_file.getFile());
        }
    }

    private static final String METHOD_CREATE_DOCUMENT = "android:createDocument";
    private static final String METHOD_RENAME_DOCUMENT = "android:renameDocument";
    private static final String METHOD_DELETE_DOCUMENT = "android:deleteDocument";
    private static final String METHOD_COPY_DOCUMENT = "android:copyDocument";
    private static final String METHOD_MOVE_DOCUMENT = "android:moveDocument";
    private static final String METHOD_IS_CHILD_DOCUMENT = "android:isChildDocument";
    private static final String METHOD_REMOVE_DOCUMENT = "android:removeDocument";
    private static final String METHOD_UPLOAD_DOCUMENT = "android:uploadDocument";

    private static final String METHOD_COMPRESS_DOCUMENT = "android:compressDocument";
    private static final String METHOD_UNCOMPRESS_DOCUMENT = "android:uncompressDocument";

    private static final String EXTRA_PARENT_URI = "parentUri";
    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_UPLOAD_URI = "upload_uri";
    private static final String EXTRA_THUMBNAIL_SIZE = "thumbnail_size";
    private static final String EXTRA_DOCUMENT_TO= "document_to";
    private static final String EXTRA_DELETE_AFTER = "delete_after";
    private static final String EXTRA_DOCUMENTS_COMPRESS = "documents_compress";
    private static final String EXTRA_DOCUMENTS_UNCOMPRESS = "documents_uncompress";

    private static final String EXTRA_TARGET_URI = "android.content.extra.TARGET_URI";

    private static final String PATH_ROOT = "root";
    private static final String PATH_RECENT = "recent";
    private static final String PATH_DOCUMENT = "document";
    private static final String PATH_CHILDREN = "children";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_TREE = "tree";

    private static final String PARAM_QUERY = "query";
    private static final String PARAM_MANAGE = "manage";

    public static void deleteDocument(ContentProviderClient client, Uri documentUri)
            throws RemoteException {
        final Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, documentUri);
        client.call(METHOD_DELETE_DOCUMENT, null, in);
    }

    private static Uri createDocument(ContentProviderClient client, Uri parentDocumentUri,
                                     String mimeType, String displayName) throws RemoteException {
        final Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, parentDocumentUri);
        in.putString(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType);
        in.putString(DocumentsContract.Document.COLUMN_DISPLAY_NAME, displayName);

        final Bundle out = client.call(METHOD_CREATE_DOCUMENT, null, in);
        return out.getParcelable(EXTRA_URI);
    }

    private static Uri moveDocument(ContentProviderClient client, Uri sourceDocumentUri,
                                    Uri sourceParentDocumentUri, Uri targetParentDocumentUri) throws RemoteException {
        final Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, sourceDocumentUri);
        in.putParcelable(EXTRA_PARENT_URI, sourceParentDocumentUri);
        in.putParcelable(EXTRA_TARGET_URI, targetParentDocumentUri);

        final Bundle out = client.call(METHOD_MOVE_DOCUMENT, null, in);
        return out.getParcelable(EXTRA_URI);
    }

    private static Uri renameDocument(ContentProviderClient client, Uri documentUri,
                                      String displayName) throws RemoteException {
        final Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, documentUri);
        in.putString(DocumentsContract.Document.COLUMN_DISPLAY_NAME, displayName);

        final Bundle out = client.call(METHOD_RENAME_DOCUMENT, null, in);
        final Uri outUri = out.getParcelable(EXTRA_URI);
        return (outUri != null) ? outUri : documentUri;
    }

    private String queryForString(Context context, Uri self, String column, String defaultValue) {
        final ContentResolver resolver = context.getContentResolver();

        Cursor c = null;
        try {
            c = resolver.query(self, new String[] { column }, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getString(0);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            if (log.isTraceEnabled()) putErrorMessage("saf_file#queryForString Failed to Query, Error="+e.getMessage());
            return defaultValue;
        } finally {
            closeQuietly(c);
        }
    }

    private String queryForString(ContentProviderClient client, Uri self, String column, String defaultValue) {
        Cursor c = null;
        try {
            c = client.query(self, new String[] { column }, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getString(0);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            if (log.isTraceEnabled()) putErrorMessage("saf_file#queryForString Failed to Query, Error="+e.getMessage());
            return defaultValue;
        }
    }

    private int queryForInt(Context context, Uri self, String column, int defaultValue) {
        return (int) queryForLong(context, self, column, defaultValue);
    }

    private int queryForInt(ContentProviderClient cpc, Uri self, String column, int defaultValue) {
        return (int) queryForLong(cpc, self, column, defaultValue);
    }

    private long queryForLong(Context context, Uri self, String column, long defaultValue) {
        final ContentResolver resolver = context.getContentResolver();

        Cursor c = null;
        try {
            c = resolver.query(self, new String[] { column }, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getLong(0);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
//            Log.w("saf_file", "saf_file#queryForLong Failed query: " + e);
//            StackTraceElement[] st=e.getStackTrace();
//            String stm="";
//            for (int i=0;i<st.length;i++) {
//                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
//            }
            if (log.isTraceEnabled()) putErrorMessage("saf_file#queryForLong Failed to Query, Error="+e.getMessage());
            return defaultValue;
        } finally {
            closeQuietly(c);
        }
    }

    private long queryForLong(ContentProviderClient cpc, Uri self, String column, long defaultValue) {
        Cursor c = null;
        try {
            c = cpc.query(self, new String[] { column }, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getLong(0);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            if (log.isTraceEnabled()) putErrorMessage("saf_file#queryForLong Failed to Query, Error="+e.getMessage());
            return defaultValue;
        } finally {
            closeQuietly(c);
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    static class FileListInfo {
        public String doc_id;
        public String doc_name, doc_type;
    }

}

