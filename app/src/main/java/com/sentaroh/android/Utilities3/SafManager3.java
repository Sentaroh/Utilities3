package com.sentaroh.android.Utilities3;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SafManager3 {
//    public static final String SAF_UUID_KEY ="saf_manager2_uuid_key";

    private static final String APPLICATION_TAG="SafManager3";

    private Context mContext=null;

    final public static String SAF_FILE_PRIMARY_UUID="primary";

    private ArrayList<SafStorage3> mSafFileList =new ArrayList<SafStorage3>();

    private String baseMp=null;

    private static Logger log = LoggerFactory.getLogger(SafManager3.class);

    private boolean mScopedStorageMode=false;

    public final static String SAF_FILE_PRIMARY_STORAGE_PREFIX="/storage/emulated/0";
    public final static String SAF_FILE_EXTERNAL_STORAGE_PREFIX="/storage/";
    public final static String SAF_FILE_DOCUMENT_TREE_URI_PREFIX="content://com.android.externalstorage.documents/tree/";

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

    public void setDebugEnabled(boolean enabled) {
        //NOP
    }

    public static final int SCOPED_STORAGE_SDK=30;
    public SafManager3(Context c) {
        mContext=c;
        baseMp= Environment.getExternalStorageDirectory().getPath();

        if (Build.VERSION.SDK_INT>=SCOPED_STORAGE_SDK) {
            mScopedStorageMode=true;//!Environment.isExternalStorageLegacy();
//            Log.v("Utilities2","mScopedStorageMode="+mScopedStorageMode+", env="+Environment.isExternalStorageLegacy());
        }

        mSafFileList=buildSafFileList();
    }

    public void refreshSafList() {
        ArrayList<SafStorage3> saf_list=buildSafFileList();
        mSafFileList.clear();
        mSafFileList.addAll(saf_list);
    }

    static public boolean isRootTreeUri(Uri uri) {
        boolean result=false;
        String uuid=getUuidFromUri(uri.toString());
        if (uri.toString().endsWith("%3A") || uri.toString().endsWith(":")) result=true;
        return result;
    }

    public boolean isUuidMounted(String uuid) {
        return isUuidMounted(mContext, uuid);
    }

    static public boolean isUuidMounted(Context c, String uuid) {
        long b_time= System.currentTimeMillis();
        ArrayList<StorageVolumeInfo>svl=getStorageVolumeInfo(c);

        for(StorageVolumeInfo svi:svl) {
            if (!svi.isDuplicate) {
                if (svi.uuid.equals(uuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    static public ArrayList<SafStorage3> getDuplicateUuid(Context c) {
        long b_time= System.currentTimeMillis();
        ArrayList<StorageVolumeInfo>svl=getStorageVolumeInfo(c);
        ArrayList<SafStorage3>duplicate_list=new ArrayList<SafStorage3>();
        for(StorageVolumeInfo svi:svl) {
            if (svi.isDuplicate) {
                SafStorage3 sitem=new SafStorage3();
                sitem.uuid=svi.uuid;
                sitem.description=svi.description;
                duplicate_list.add(sitem);
            }
        }
        return duplicate_list;
    }

    public boolean isUuidRegistered(String uuid) {
        return isUuidRegistered(mContext, uuid);
    }

    static public boolean isUuidRegistered(Context c, String uuid) {
        long b_time= System.currentTimeMillis();
        SafFile3 rt=null;
        boolean result=false;
        if (uuid.equals(SafFile3.SAF_FILE_PRIMARY_UUID)) {
            if ( Build.VERSION.SDK_INT>=SafManager3.SCOPED_STORAGE_SDK) {
                rt=SafFile3.fromTreeUri(c, Uri.parse(SAF_FILE_DOCUMENT_TREE_URI_PREFIX+uuid+"%3A"));
                result=rt.exists();
            } else {
                result=true;
            }
        } else {
            rt=SafFile3.fromTreeUri(c, Uri.parse(SAF_FILE_DOCUMENT_TREE_URI_PREFIX+uuid+"%3A"));
            result=rt.exists();
        }
//        log.debug("isUuidRegistered elapsed time="+(System.currentTimeMillis()-b_time));
        return result;
    }

    public boolean isStoragePermissionRequired() {
        return isStoragePermissionRequired(mContext);
    }

    static public boolean isStoragePermissionRequired(Context c) {
        ArrayList<StorageVolumeInfo> rows=buildStoragePermissionRequiredList(c);
        if (rows.size()>0) return true;
        else return false;
    }

    static public ArrayList<StorageVolumeInfo> buildStoragePermissionRequiredList(Context c) {
        final ArrayList<StorageVolumeInfo> svi_list= getStorageVolumeInfo(c);
        final ArrayList<StorageVolumeInfo> rows=new ArrayList<StorageVolumeInfo>();

        for(SafManager3.StorageVolumeInfo ssi:svi_list) {
            if (!ssi.isDuplicate && ssi.state.equals(Environment.MEDIA_MOUNTED)) {
                if (!isUuidRegistered(c, ssi.uuid)) {
                    if (ssi.uuid.equals(SafManager3.SAF_FILE_PRIMARY_UUID)) {
                    } else {
                        rows.add(ssi);
                    }
                }
            }
        }
        log.debug("buildStoragePermissionRequiredList size="+rows.size());
        return rows;
    }


    public boolean isScopedStorageMode() {
        return mScopedStorageMode;
    }

    private ArrayList<StorageVolumeInfo> mStorageVolumeInfoList=null;

    public ArrayList<StorageVolumeInfo> getLastStorageVolumeInfo() {
        return mStorageVolumeInfoList;
    }

    private ArrayList<SafStorage3> buildSafFileList() {
        ArrayList<SafStorage3> saf_list=new ArrayList<SafStorage3>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        StorageManager sm = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        mStorageVolumeInfoList=getStorageVolumeInfo(mContext);
        for(StorageVolumeInfo item_svi:mStorageVolumeInfoList) {
            SafFile3 rt=null;
            if (!item_svi.isDuplicate) {
                if (isScopedStorageMode()) {
                    rt=SafFile3.fromTreeUri(mContext, Uri.parse(SAF_FILE_DOCUMENT_TREE_URI_PREFIX+item_svi.uuid+"%3A"));
                    if (rt.exists()) {//Internal storage, SDCARD or USB
                        SafStorage3 sli=new SafStorage3();
                        sli.description=item_svi.description;
                        sli.uuid=item_svi.uuid;
                        sli.saf_file =rt;
                        sli.isSafFile=true;
                        sli.appDirectory=getAppSpecificDirectory(mContext,  item_svi.uuid);
                        saf_list.add(sli);
                    }
                } else {
                    if (Build.VERSION.SDK_INT>=23) {
                        if (item_svi.uuid.equals(SAF_FILE_PRIMARY_UUID)) {//Internal storage
                            SafStorage3 sli=new SafStorage3();
                            sli.description=item_svi.description;
                            sli.uuid=item_svi.uuid;
                            sli.isSafFile=false;
                            sli.appDirectory=getAppSpecificDirectory(mContext,  item_svi.uuid);
                            sli.appMountpoint=baseMp;
                            String fp=mContext.getExternalFilesDirs(null)[0].getPath();
                            sli.saf_file=new SafFile3(mContext, fp.substring(0, fp.indexOf("/Android/data")));
                            saf_list.add(sli);
                        } else {//SDCARD or USB
                            rt=new SafFile3(mContext, SAF_FILE_EXTERNAL_STORAGE_PREFIX+ item_svi.uuid);
                            if (rt.exists()) {
                                SafStorage3 sli=new SafStorage3();
                                sli.description=item_svi.description;
                                sli.uuid=item_svi.uuid;
                                sli.saf_file=rt;
                                sli.appMountpoint=rt.getPath();
                                sli.isSafFile=true;
                                sli.appDirectory=getAppSpecificDirectory(mContext,  item_svi.uuid);
                                saf_list.add(sli);
                            }
                        }
                    } else {
                        if (item_svi.isPrimary && !item_svi.isRemovable) {
                            //Internal storage
                            SafStorage3 sli=new SafStorage3();
                            sli.description=item_svi.description;
                            sli.uuid=SAF_FILE_PRIMARY_UUID;
                            sli.isSafFile=false;
                            sli.appDirectory=mContext.getExternalFilesDirs(null)[0].getPath();
                            sli.appMountpoint=baseMp;
                            sli.saf_file=new SafFile3(mContext, SAF_FILE_PRIMARY_STORAGE_PREFIX);
                            saf_list.add(sli);
                        } else {
                            rt=SafFile3.fromTreeUri(mContext, Uri.parse(SAF_FILE_DOCUMENT_TREE_URI_PREFIX+item_svi.uuid+"%3A"));
                            if (rt.exists()) {
                                SafStorage3 sli=new SafStorage3();
                                sli.description=item_svi.description;
                                sli.uuid=item_svi.uuid;
                                sli.saf_file=rt;
                                sli.isSafFile=true;
                                sli.appDirectory=getAppSpecificDirectory(mContext,  item_svi.uuid);
                                saf_list.add(sli);
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(saf_list, new Comparator<SafStorage3>(){
            @Override
            public int compare(SafStorage3 l1, SafStorage3 r1) {
                String l_key, r_key;
                if (l1.uuid.equals(SAF_FILE_PRIMARY_UUID)) l_key="0"+l1.description;
                else l_key="1"+l1.description;
                if (r1.uuid.equals(SAF_FILE_PRIMARY_UUID)) r_key="0"+r1.description;
                else r_key="1"+r1.description;
                return l_key.compareToIgnoreCase(r_key);
            }
        });
        log.debug("SafStorage3List created size="+ saf_list.size());
        for(SafStorage3 ss: saf_list) {
            String f_path="";
            if (ss.saf_file !=null) f_path=ss.saf_file.getPath();
            log.debug("Descrition="+ss.description+", uuid="+ss.uuid+", isSafFile="+ss.isSafFile+", path="+f_path+", mp="+ss.appMountpoint+", appDirectory="+ss.appDirectory);
        }

        return saf_list;
    }

    static private String getAppSpecificDirectory(Context c, String uuid) {
        String app_dir="";
        File[] fl =c.getExternalFilesDirs(null);
        if (uuid.equals(SAF_FILE_PRIMARY_UUID)) {
            app_dir=fl[0].getPath();
        } else {
            for(File item_fl:fl) {
                if (item_fl!=null && item_fl.getPath().contains(uuid)) {
                    app_dir=item_fl.getPath();
                    break;
                }
            }
        }
        return app_dir;
    }

    static private boolean isUuidExists(ArrayList<StorageVolumeInfo> svl, String uuid) {
        boolean result=false;
        for(StorageVolumeInfo item:svl) {
            if (item.uuid.equals(uuid)) {
                result=true;
                break;
            }
        }
        return result;
    }

    static public ArrayList<StorageVolumeInfo> getStorageVolumeInfo(Context c) {
        ArrayList<StorageVolumeInfo> svl=new ArrayList<StorageVolumeInfo>();
        try {
            StorageManager sm = (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
            if (Build.VERSION.SDK_INT>=24) {
                List<StorageVolume> svs=sm.getStorageVolumes();
                for(StorageVolume item:svs) {
                    StorageVolumeInfo svi=new StorageVolumeInfo();
                    svi.description=item.getDescription(c);
                    svi.uuid=item.getUuid()==null?SAF_FILE_PRIMARY_UUID:item.getUuid();
                    svi.isPrimary=item.isPrimary();
                    svi.isRemovable=item.isRemovable();
                    svi.volume=item;
                    svi.state=item.getState();
                    if (isUuidExists(svl, svi.uuid)) {
                        if (log.isDebugEnabled()) log.debug("Duplicate UUID detected, UUID="+svi.uuid+", Description="+svi.description);
                        svi.isDuplicate=true;
                    }
                    svl.add(svi);
                }
            } else {
                Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
                Object[] volumeList = (Object[]) getVolumeList.invoke(sm);
                for (Object volume : volumeList) {
                    StorageVolumeInfo svi = new StorageVolumeInfo();
                    Method getPath = volume.getClass().getDeclaredMethod("getPath");
                    Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
                    Method isPrimary = volume.getClass().getDeclaredMethod("isPrimary");
                    Method getUuid = volume.getClass().getDeclaredMethod("getUuid");
                    Method getState = volume.getClass().getDeclaredMethod("getState");
//                    Method getDescription = volume.getClass().getDeclaredMethod("getDescription");
//                    Method getId = volume.getClass().getDeclaredMethod("getId");
                    Method toString = volume.getClass().getDeclaredMethod("toString");
//                    Method allowMassStorage = volume.getClass().getDeclaredMethod("allowMassStorage");
//                    Method getStorageId = volume.getClass().getDeclaredMethod("getStorageId");
                    svi.isPrimary = (boolean) isPrimary.invoke(volume);
                    svi.isRemovable = (boolean) isRemovable.invoke(volume);
                    svi.path = (String) getPath.invoke(volume);
                    svi.state=(String)getState.invoke(volume);
                    String f_uuid=(String) getUuid.invoke(volume);
                    if (Build.VERSION.SDK_INT>=23) {
                        svi.uuid =f_uuid==null?SAF_FILE_PRIMARY_UUID:f_uuid;
                    } else {
                        if (svi.isPrimary && !svi.isRemovable) svi.uuid=SAF_FILE_PRIMARY_UUID;
                    }
                    String dump=(String) toString.invoke(volume);
                    if (log.isDebugEnabled()) log.debug("toString="+dump);
                    if (Build.VERSION.SDK_INT==23) {
                        try {
                            svi.description =dump.substring(dump.indexOf("mDescription")+13, dump.indexOf("mPrimary")-1);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            svi.description =dump.substring(dump.indexOf("mUserLabel")+11, dump.indexOf("mState")-1);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (isUuidExists(svl, svi.uuid)) {
                        if (log.isDebugEnabled()) log.debug("Duplicate UUID detected, UUID="+svi.uuid+", Description="+svi.description);
                        svi.isDuplicate=true;
                    }
                    svl.add(svi);
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Collections.sort(svl, new Comparator<StorageVolumeInfo>(){
            @Override
            public int compare(StorageVolumeInfo l1, StorageVolumeInfo r1) {
                return l1.description.compareToIgnoreCase(r1.description);
            }
        });
        return svl;
    }

    public SafFile3 getRootSafFile(String uuid) {
        return getRootSafFile(mSafFileList, uuid);
    }

    static public SafFile3 getRootSafFile(ArrayList<SafStorage3> sl, String uuid) {
        for(SafStorage3 sli:sl) {
            if (sli.uuid.equals(uuid)) {
                return sli.saf_file;
            }
        }
        return null;
    }

    public ArrayList<SafStorage3> getSafStorageList() {
        return mSafFileList;
    }

    public static String getUuidFromUri(Uri uri) {
        return getUuidFromUri(uri.toString());
    }

    public static String getUuidFromUri(String uri) {
        String result="";
        try {
            int semicolon = uri.lastIndexOf("%3A");
            if (semicolon<0) semicolon = uri.lastIndexOf(":");
            if (semicolon>0) {
                int idx=uri.substring(0,semicolon).lastIndexOf("/");
                result=uri.substring(idx+1,semicolon);
            } else {
                int idx=uri.substring(0,semicolon).lastIndexOf("/");
                result=uri.substring(idx+1,uri.length()-3);
            }
        } catch(Exception e) {}
        return result;
    }

    public boolean addUuid(Uri uri) {
        boolean result=false;
        String uuid=getUuidFromUri(uri.toString());
        if (uuid.length()>0) result=addUuid(uuid);
        return result;
    }

    public boolean addUuid(final String uuid) {
        boolean result=true;
        putInfoMessage("addUuid uuif="+uuid);
        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
        for(UriPermission item:permissions) putInfoMessage(item.toString());
        try {
            mContext.getContentResolver().takePersistableUriPermission(
                    Uri.parse(SAF_FILE_DOCUMENT_TREE_URI_PREFIX+uuid+"%3A"),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            putInfoMessage("addUuid successfull");
            ArrayList<SafStorage3> saf_list=buildSafFileList();
            mSafFileList.clear();
            mSafFileList.addAll(saf_list);
        } catch(Exception e) {
            putErrorMessage("addUuid error, uuid="+uuid+", Error="+e.getMessage());
            result=false;
        }
        return result;
    }

//    public SafFile3 createSafItem(SafFile3 rf, String target_path, boolean isDirectory) {
//        return createItem(rf, target_path, isDirectory);
//    }
//
//    public SafFile3 createSafDirectory(SafFile3 rf, String target_path) {
//        return createItem(rf, target_path, true);
//    }
//
//    public SafFile3 createSafFile(SafFile3 rf, String target_path) {
//        return createItem(rf, target_path, false);
//    }
//
//    private SafFile3 createItem(SafFile3 rf, String target_path, boolean isDirectory) {
//        SafFile3 saf=null;
//        if (target_path.equals("")) return rf;
//        if (rf.isSafFile()) {
//            ContentProviderClient client =null;
//            try {
//                if (rf!=null) {
//                    client = mContext.getContentResolver().acquireContentProviderClient(rf.getUri().getAuthority());
//                    saf=createItem(client, rf, target_path, isDirectory);
//                } else {
//                    putErrorMessage("createItem SafRoot file is null.");
//                }
//            } finally {
//                if (client!=null) client.release();
//            }
//        } else {
//            saf=new SafFile3(mContext, target_path);
//            if (isDirectory) saf.mkdirs();
//            else {
//                try {
//                    saf.createNewFile();
//                } catch (IOException e) {
//                    saf=null;
//                    e.printStackTrace();
//                }
//            }
//        }
//        return saf;
//    }

//    private SafFile3 createItem(ContentProviderClient client, SafFile3 rf, String target_path, boolean isDirectory) {
//        SafFile3 parent=null;
//        if (log.isDebugEnabled()) putDebugMessage("createItem target_path="+target_path+", isDirectory="+isDirectory);
//
//        if (rf==null) {
//            putErrorMessage("createItem SafRoot file is null.");
//            return null;
//        }
//
//        long b_time= System.currentTimeMillis();
//        SafFile3 document=rf;
//
//        String relativePath=target_path;
//
//        if (log.isDebugEnabled()) putDebugMessage("rootUri="+rf.getUri()+", relativePath="+relativePath);
//
//        try {
//            if (!relativePath.equals("")) {
//                String[] parts = relativePath.split("\\/");
//                for (int i = 0; i < parts.length; i++) {
//                    if (log.isDebugEnabled()) putDebugMessage("parts="+parts[i]);
//                    if (!parts[i].equals("")) {
//                        SafFile3 nextDocument = document.findFile(client, parts[i]);
//                        if (log.isDebugEnabled()) putDebugMessage("findFile="+parts[i]+", result="+nextDocument);
//                        if (nextDocument == null) {
//                            if ((i < parts.length - 1) || isDirectory) {
//                                String c_dir=parts[i];
//                                nextDocument = document.createDirectory(c_dir);
//                                if (log.isDebugEnabled()) putDebugMessage("Directory was created name="+c_dir+", result="+nextDocument);
////                			Log.v("","saf="+document.getMsgArea());
//                            } else {
//                                nextDocument = document.createFile("", parts[i]);
//                                if (log.isDebugEnabled()) putDebugMessage("File was created name="+parts[i]+", result="+nextDocument);
//                            }
//                        }
//                        parent=document;
//                        document = nextDocument;
////                        if (document!=null) {
////                            document.setParentFile(parent);
////                        }
//                    }
//                }
//            }
//        } catch(Exception e) {
//            StackTraceElement[] st=e.getStackTrace();
//            String stm="";
//            for (int i=0;i<st.length;i++) {
//                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
//            }
//            putErrorMessage("createItem Error="+e.getMessage()+stm);
//        }
//        if (log.isDebugEnabled()) putDebugMessage("createItem elapsed="+(System.currentTimeMillis()-b_time));
//        return document;
//    }

//    public SafFile3 findSafItem(SafFile3 rf, String target_path) {
//        if (rf.isSafFile()) {
////            long b_time= System.currentTimeMillis();
////            String path=target_path.startsWith("/")?target_path.substring(1):"";
////            Uri uri=Uri.parse(rf.getUri().toString()+path);
////            log.debug("uri="+uri.getPath());
////            SafFile3 sf2=new SafFile3(rf.getRootSafFile(), mContext, uri);
////            log.debug("uri1="+sf2.getUri().getPath()+", exists="+sf2.exists());
//
////            if (log.isDebugEnabled()) putDebugMessage("findSafItem elapsed="+(System.currentTimeMillis()-b_time));
////            if (sf2!=null && sf2.getName()!=null) return sf2;
////            else return null;
//            SafFile3 fi=findItem(rf, target_path);
////            if (log.isDebugEnabled()) putDebugMessage("findSafItem elapsed="+(System.currentTimeMillis()-b_time));
//            return fi;
//        } else {
//            SafFile3 sf2=new SafFile3(mContext, target_path);
//            if (log.isDebugEnabled()) putDebugMessage("findItem target_path="+target_path+", root name="+rf.getName()+", exists="+sf2.exists());
//            if (sf2.exists()) return sf2;
//            else return null;
//        }
//    }

//    private SafFile3 findItem(SafFile3 rf, String target_path) {
//        SafFile3 parent=null;
//        if (log.isDebugEnabled()) putDebugMessage("findItem target_path="+target_path+", root name="+rf.getName());
//
//        long b_time= System.currentTimeMillis();
//        SafFile3 document=rf;
//
//        String relativePath=target_path;
//
//        if (log.isDebugEnabled()) putDebugMessage("rootUri="+rf.getUri()+", relativePath="+relativePath);
//
//        ContentProviderClient client =null;
//        try {
//            client = mContext.getContentResolver().acquireContentProviderClient(rf.getUri().getAuthority());
//            if (!relativePath.equals("")) {
//                String[] parts = relativePath.split("\\/");
//                for (int i = 0; i < parts.length; i++) {
//                    if (log.isDebugEnabled()) putDebugMessage("parts="+parts[i]);
//                    if (!parts[i].equals("")) {
//                        SafFile3 nextDocument = document.findFile(client, parts[i]);
//                        if (log.isDebugEnabled()) putDebugMessage("findFile="+parts[i]+", result="+nextDocument);
//                        if (nextDocument != null) {
//                            parent=document;
//                            document = nextDocument;
////                            document.setParentFile(parent);
//                        } else {
//                            document = null;
//                            break;
//                        }
//                    }
//                }
//            }
//        } catch(Exception e) {
//            StackTraceElement[] st=e.getStackTrace();
//            String stm="";
//            for (int i=0;i<st.length;i++) {
//                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
//            }
//            putErrorMessage("findItem Error="+e.getMessage()+stm);
//        } finally {
//            if (client!=null) client.release();
//        }
//
//        if (log.isDebugEnabled()) putDebugMessage("findItem elapsed="+(System.currentTimeMillis()-b_time));
//        return document;
//    };

    static public class StorageVolumeInfo {
        public String path="";
        public String uuid="";
        public String description="";
        public String state="";
        public boolean isRemovable=false;
        public boolean isPrimary=false;

        public boolean isDuplicate=false;

        public StorageVolume volume=null;
    }

}

