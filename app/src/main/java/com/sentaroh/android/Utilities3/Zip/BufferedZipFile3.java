package com.sentaroh.android.Utilities3.Zip;
/*
The MIT License (MIT)
Copyright (c) 2019 Sentaroh

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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import com.sentaroh.android.Utilities3.CallBackListener;
import com.sentaroh.android.Utilities3.MiscUtil;
import com.sentaroh.android.Utilities3.SafFile3;
import com.sentaroh.android.Utilities3.SafManager3;
import com.sentaroh.android.Utilities3.StringUtil;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.headers.HeaderSignature;
import net.lingala.zip4j.headers.HeaderWriter;
import net.lingala.zip4j.io.outputstream.SplitOutputStream;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.EndOfCentralDirectoryRecord;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.CRC32;

public class BufferedZipFile3 {
    private static Logger log= LoggerFactory.getLogger(BufferedZipFile3.class);

    private Context mContext=null;

    private boolean closed =false;
    private boolean mInpuZipFileItemRemoved =false;
    private boolean mAddZipFileItemAdded =false;
    //    private ZipFile mInputZipFile =null;
//    private ZipFile mAddZipFile =null;
    //    private File mInputOsFile =null;
//    private File mOutputOsFile =null;
//    private File mTempOsFileX =null, mAddOsFile =null;
    private ZipModel mInputZipModel=null;

    private ZipModel mAddZipModel =null;
    private ArrayList<BzfFileHeaderItem> mInputZipFileHeaderList =null;
    private ArrayList<BzfFileHeaderItem> mAddZipFileHeaderList =null;
    private OutputStream mOutputOsFileStream =null;
    private long mOutputZipFilePosition =0;
    private BufferedOutputStream mOutputZipFileStream =null;

    private SafFile3 mInputSafFile=null;
    private SafFile3 mOutputSafFile=null;
    private SafFile3 mAddSafFile=null;
    private Uri mInputUri=null;
    private Uri mOutputUri=null;

    private static final int IO_AREA_SIZE=1024*1024;
    private ZipOutputStream mAddZipOutputStream =null;
//    private OutputStream mAddSplitOutputStream = null;

    private String mEncoding =DEFAULT_ZIP_FILENAME_ENCODING;
    private String mPassword = "";
    private static final String DEFAULT_ZIP_FILENAME_ENCODING="UTF-8";

    private String[] mNoCompressExtention=null;

    class BzfFileHeaderItem {
        public FileHeader file_header;
        public boolean isRemovedItem=false;
        public long start_pos=0;
        public long end_pos=0;
        public boolean removed_entry=false;
    }

    public void setNoCompressExtentionList(String list) {
        mNoCompressExtention=list.split(";");
    }

    private int mNoCompressFileLength=100;
    public void setNoCompressFileLength(int no_compress_file_length) {
        mNoCompressFileLength=no_compress_file_length;
    }

    public BufferedZipFile3(Context c, String input_path, String output_path, String encoding, String password) throws ZipException {
        mContext=c;
        SafFile3 in_uri=input_path!=null?new SafFile3(mContext, input_path):null;
        SafFile3 out_uri=new SafFile3(mContext, output_path);
        init(in_uri, out_uri, encoding, password);
    }

    //    public BufferedZipFile3(Context c, File input_file, File output_file, String encoding, String wfp) {
//        mContext=c;
//        Uri in_uri=Uri.fromFile(input_file);
//        Uri out_uri=Uri.fromFile(output_file);
//        init(in_uri, out_uri, encoding, wfp);
//    }
//
    public BufferedZipFile3(Context c, SafFile3 input_file, SafFile3 output_file, String encoding, String password) throws ZipException {
        mContext=c;
        SafFile3 add_wrk_uri=new SafFile3(mContext, output_file.getPath()+".add_work");
        init(input_file, output_file, encoding, password);
    }

    public SafFile3 getInputZipFile() {
        return mInputSafFile;
    }

    public SafFile3 getOutputZipFile() {
        return mOutputSafFile;
    }

    private boolean mEmptyInputZipFile=true;
    private void init(SafFile3 in_uri, SafFile3 out_uri, String encoding, String password) throws ZipException {
        log.debug("<init> Input="+in_uri+", Output="+out_uri+", Encoding="+encoding);
        if (in_uri!=null && out_uri!=null) {
            if (in_uri.getPath().equals(out_uri.getPath())) throw new ZipException("BufferedZipFile3 create failed.(Same path)");
        } else if (in_uri==null && out_uri==null) {
            throw new ZipException("BufferedZipFile3 create failed.(Input and output is null)");
        }
        mInputSafFile=in_uri;
        mOutputSafFile=out_uri;
        mInputUri=mInputSafFile!=null?mInputSafFile.getUri():null;
        mEncoding =encoding;
        mPassword =password;
//        mTempOsFile =new File(work_file_path+"/ziputility.tmp");
//        mAddOsFile =new File(work_file_path+"/ziputility.add");
        mInputZipFileHeaderList =new ArrayList<BzfFileHeaderItem>();
        try {
            HeaderReader header_reader=new HeaderReader();
            SeekableInputStream sis=null;
            if (mInputSafFile!=null && mInputSafFile.exists()) {
                if (mInputSafFile.isSafFile()) sis=new SeekableInputStream(mContext, mInputUri, mInputSafFile.length());
                else sis=new SeekableInputStream(mContext, mInputSafFile.getFile());
            }
            try {
                if (sis!=null) {
                    mInputZipModel =header_reader.readAllHeaders(sis, Charset.forName(encoding));
                    ArrayList<FileHeader> file_header_list=(ArrayList<FileHeader>) mInputZipModel.getCentralDirectory().getFileHeaders();
                    for(FileHeader fh:file_header_list) {
                        BzfFileHeaderItem rfhli=new BzfFileHeaderItem();
                        rfhli.file_header=fh;
                        mInputZipFileHeaderList.add(rfhli);
                        mEmptyInputZipFile=false;
                    }
                } else {
                    mInputZipModel =new ZipModel();
                    mInputZipModel.setEndOfCentralDirectoryRecord(createEndOfCentralDirectoryRecord());
                    mInputZipModel.setCentralDirectory(new CentralDirectory());
                    mInputZipModel.getCentralDirectory().setFileHeaders(new ArrayList<FileHeader>());
                }
            } catch (ZipException e) {
                mInputZipModel =new ZipModel();
                mInputZipModel.setEndOfCentralDirectoryRecord(createEndOfCentralDirectoryRecord());
                mInputZipModel.setCentralDirectory(new CentralDirectory());
                mInputZipModel.getCentralDirectory().setFileHeaders(new ArrayList<FileHeader>());
            }
            try {sis.close();} catch(Exception e) {}
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dumpZipModel("Init", mInputZipModel);
    }

//    private char[] mPassword=null;
//    public void setPassword(String password) {
//        if (password!=null) mPassword=password.toCharArray();
//    }

    public boolean addItem(String input, ZipParameters zp) throws IOException {
        return addItem(new SafFile3(mContext, input), zp, null);
    }

    public boolean addItem(String input, ZipParameters zp, CallBackListener cbl) throws IOException {
        return addItem(new SafFile3(mContext, input), zp, cbl);
    }

    public boolean addItem(File input, ZipParameters zp) throws IOException {
        return addItem(new SafFile3(mContext, input.getPath()), zp, null);
    }

    public boolean addItem(File input, ZipParameters zp, CallBackListener cbl) throws IOException {
        return addItem(new SafFile3(mContext, input.getPath()), zp, cbl);
    }

    public boolean addItem(SafFile3 in_uri, ZipParameters zp) throws IOException {
        return addItem(in_uri, zp, null);
    }

    public boolean addItem(SafFile3 in_uri, ZipParameters zp, CallBackListener cbl) throws IOException {
        checkClosed();
        if (mAddZipModel ==null) {
            OutputStream os=null;
            try {
                mAddSafFile=new SafFile3(mContext, mOutputSafFile.getParent()+"/"+System.currentTimeMillis()+".add_work");
                mAddSafFile.deleteIfExists();
                mAddSafFile.createNewFile();
                mAddZipFileHeaderList =new ArrayList<BzfFileHeaderItem>();
                mAddZipModel = new ZipModel();
                mAddZipModel.setEndOfCentralDirectoryRecord(createEndOfCentralDirectoryRecord());
                mAddZipModel.setSplitArchive(false);
                mAddZipModel.setSplitLength(-1);
//                mAddSplitOutputStream = new SplitOutputStream(new File(mAddZipModel.getZipFile().getPath()), mAddZipModel.getSplitLength());
                os = mAddSafFile.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("addItem OutputStream was not created",e);
                return false;
            }
            //mAddZipOutputStream =new ZipOutputStream(os, null, Charset.forName(mEncoding), mAddZipModel);
            mAddZipOutputStream =new ZipOutputStream(os, mPassword.toCharArray(), Charset.forName(mEncoding));
        }
        return addItemInternal(in_uri, zp, cbl);
    };

    private EndOfCentralDirectoryRecord createEndOfCentralDirectoryRecord() {
        EndOfCentralDirectoryRecord endCentralDirRecord = new EndOfCentralDirectoryRecord();
        endCentralDirRecord.setSignature(HeaderSignature.END_OF_CENTRAL_DIRECTORY);
        endCentralDirRecord.setNumberOfThisDisk(0);
        endCentralDirRecord.setTotalNumberOfEntriesInCentralDirectory(0);
        endCentralDirRecord.setTotalNumberOfEntriesInCentralDirectoryOnThisDisk(0);
        endCentralDirRecord.setOffsetOfStartOfCentralDirectory(0);
        return endCentralDirRecord;
    }

//    private ZipModel readZipInfo(Uri uri, String encoding) throws ZipException {
//        ZipModel zipModel=null;
//        SeekableInputStream ss=null;
//        try {
//            ss=new SeekableInputStream(mContext, uri);
//            HeaderReader headerReader = new HeaderReader();
//            zipModel = headerReader.readAllHeaders(ss, Charset.forName(encoding));
//        } catch (FileNotFoundException e) {
//            throw new ZipException(e);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (ss != null) {
//                try {
//                    ss.close();
//                } catch (IOException e) {
//                    //ignore
//                }
//            }
//        }
//        return zipModel;
//    }

    private boolean isAlreadyAdded(String fp) {
        boolean result=false;
        if (mAddZipFileHeaderList!=null) {
            for(BzfFileHeaderItem added: mAddZipFileHeaderList) {
                if (!added.isRemovedItem && added.file_header.getFileName().equals(fp)) {
                    result=true;
                    break;
                }
            }
        }
        return result;
    }

    public static long computeFileCrc(SafFile3 inputFile) {

        byte[] buff = new byte[1024*1024*2];
        CRC32 crc32 = new CRC32();

        try(InputStream inputStream = inputFile.getInputStream()) {
            int readLen;
            while ((readLen = inputStream.read(buff)) != -1) {
                crc32.update(buff, 0, readLen);
            }
            return crc32.getValue();
        } catch (Exception e) {
            crc32.update(buff, 0, 0);
            return crc32.getValue();
        }
    }

    private boolean isNoCompressExtention(String fp) {
        boolean result=false;
        if (mNoCompressExtention!=null) {
            for (String item:mNoCompressExtention) {
                if (fp.toLowerCase().endsWith("."+item.toLowerCase())) {
                    result=true;
                    break;
                }
            }
        }
        return result;
    }

    public void mkDir(ZipParameters zp) throws IOException {
        addItemInputStream(null, zp, true, null);
    }

    public boolean addItem(InputStream is, ZipParameters zp, CallBackListener cbl) throws IOException {
        return addItemInputStream(is, zp, false, cbl);
    }

    private boolean addItemInputStream(InputStream is, ZipParameters zp, boolean directory, CallBackListener cbl) throws IOException {
        checkClosed();
        if (mAddZipModel ==null) {
            OutputStream os=null;
            try {
                mAddSafFile=new SafFile3(mContext, mOutputSafFile.getParent()+"/"+System.currentTimeMillis()+".add_work");
                mAddSafFile.deleteIfExists();
                mAddSafFile.createNewFile();
                mAddZipFileHeaderList =new ArrayList<BzfFileHeaderItem>();
                mAddZipModel = new ZipModel();
                mAddZipModel.setEndOfCentralDirectoryRecord(createEndOfCentralDirectoryRecord());
                mAddZipModel.setSplitArchive(false);
                mAddZipModel.setSplitLength(-1);
                os = mAddSafFile.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("addItemInputStream OutputStream was not created",e);
                return false;
            }
            //mAddZipOutputStream =new ZipOutputStream(os, null, Charset.forName(mEncoding), mAddZipModel);
            mAddZipOutputStream =new ZipOutputStream(os, mPassword.toCharArray(), Charset.forName(mEncoding));
        }
        return addItemInternalInputStream(is, zp, directory, cbl);
    };

    private boolean addItemInternalInputStream(InputStream is, ZipParameters parameters, boolean directory, CallBackListener p_cbl) throws ZipException {
        boolean result=false;
        if (isAlreadyAdded(parameters.getFileNameInZip())) throw new ZipException("BufferedZipFile3 Already added, name="+parameters.getFileNameInZip());
        BufferedInputStream inputStream =null;
        try {
            byte[] readBuff = new byte[IO_AREA_SIZE];
            int readLen = -1;
            ZipParameters fileParameters = new ZipParameters(parameters);
            String fp_prefix="";
            fileParameters.setIncludeRootFolder(false);

            if (directory) {
                fileParameters.setEntrySize(0);
                fileParameters.setCompressionMethod(CompressionMethod.STORE);
                fileParameters.setEncryptFiles(false);
                fileParameters.setEncryptionMethod(EncryptionMethod.NONE);
                fileParameters.setEntryCRC(0);
                fileParameters.setWriteExtendedLocalFileHeader(true);
            } else {
                if ((parameters.getEntrySize()<=mNoCompressFileLength) || isNoCompressExtention(parameters.getFileNameInZip())) {
                    fileParameters.setCompressionMethod(CompressionMethod.STORE);
                    fileParameters.setEntrySize(parameters.getEntrySize());
                } else {
                    fileParameters.setCompressionMethod(parameters.getCompressionMethod());
                }
            }

            fileParameters.setLastModifiedFileTime(parameters.getLastModifiedFileTime());
            mAddZipOutputStream.putNextEntry(fileParameters);
            FileHeader fh_close=null;

            if (directory) {//for directory
                fh_close=mAddZipOutputStream.closeEntry();
            } else {
                inputStream = new BufferedInputStream(is, IO_AREA_SIZE*4);
                long fsz=inputStream.available();
                long read_count=0L;
                int progress=0;
                if (p_cbl!=null) p_cbl.onCallBack(mContext, true, new Object[]{progress});
                while ((readLen = inputStream.read(readBuff)) != -1) {
                    if (isAborted()) break;
                    mAddZipOutputStream.write(readBuff, 0, readLen);
                    read_count+=readLen;
                    notifyProgress(p_cbl, read_count, fsz);
                }
                mAddZipOutputStream.closeEntry();

                inputStream.close();
            }

            if (!isAborted()) {
                List<FileHeader> fhl= mAddZipModel.getCentralDirectory().getFileHeaders();
                List<LocalFileHeader> lfhl= mAddZipModel.getLocalFileHeaders();
                log.debug("addItemInternalInputStream Central FileHeader size="+fhl.size()+", Local Fileheader size="+lfhl.size());
                for(int i = mAddZipFileHeaderList.size(); i<fhl.size(); i++) {
                    FileHeader fh=fhl.get(i);
                    BzfFileHeaderItem bfhi=new BzfFileHeaderItem();
                    bfhi.file_header=fh;
                    mAddZipFileHeaderList.add(bfhi);
                    byte[] gpflags=fh.getGeneralPurposeFlag();
                    log.debug("addItemInternalInputStream added name="+fh.getFileName()+", GeneralPurposeFlag="+ StringUtil.getHexString(gpflags, 0, gpflags.length));
                }
                mAddZipFileItemAdded=true;
                result=true;
            }
        } catch (ZipException e) {
            if (inputStream != null) try {inputStream.close();} catch (IOException ex) {}
            if (mAddZipOutputStream != null) try {mAddZipOutputStream.close();} catch (IOException ex) {}
            throw e;
        } catch (Exception e) {
            if (inputStream != null) try {inputStream.close();} catch (IOException ex) {}
            if (mAddZipOutputStream != null) try {mAddZipOutputStream.close();} catch (IOException ex) {}
            throw new ZipException(e);
        }
        return result;
    }

    private boolean addItemInternal(SafFile3 input, ZipParameters parameters, CallBackListener p_cbl) throws ZipException {
        boolean result=false;
        if (isAlreadyAdded(input.getPath())) throw new ZipException("BufferedZipFile3 Already added, name="+input.getPath());
        BufferedInputStream inputStream =null;
        try {
            byte[] readBuff = new byte[IO_AREA_SIZE];
            int readLen = -1;
//            ZipParameters fileParameters = (ZipParameters) parameters.clone();
            ZipParameters fileParameters = new ZipParameters(parameters);
            String fp_prefix="";
            if (input.getUuid().equals(SafFile3.SAF_FILE_PRIMARY_UUID)) {
                fp_prefix= SafManager3.getPrimaryStoragePath(); // "/storage/emulated/0"
            } else {
                fp_prefix="/storage/"+input.getUuid()+"/";
            }
//            fileParameters.setDefaultFolderPath(input.getParentFile().getPath().replace(fp_prefix,""));
            fileParameters.setIncludeRootFolder(false);

//            String def_path=fileParameters.getDefaultFolderPath().endsWith("/")?fileParameters.getDefaultFolderPath():fileParameters.getDefaultFolderPath()+"/";
            if (!input.isDirectory()) {
//                fileParameters.setFileNameInZip(input.getPath().replace(fp_prefix,""));
//                fileParameters.setFileNameInZip(input.getPath().replace(def_path,""));
                if (fileParameters.isEncryptFiles() && fileParameters.getEncryptionMethod() == EncryptionMethod.ZIP_STANDARD) {
                    fileParameters.setEntryCRC((int) computeFileCrc(input));
                }
                //Add no compress function 2016/07/22 F.Hoshino
//                if (Zip4jUtil.getFileLengh(input)<100 || !fileParameters.isCompressFileExtention(input.getName())) {
                if ((input.length()<=mNoCompressFileLength) || isNoCompressExtention(input.getPath())) {
                    fileParameters.setCompressionMethod(CompressionMethod.STORE);
                    fileParameters.setEntrySize(input.length());
                } else {
                    fileParameters.setCompressionMethod(parameters.getCompressionMethod());
                }
            } else {
//                fileParameters.setFileNameInZip(input.getPath().replace(fp_prefix,"")+"/");
//                fileParameters.setFileNameInZip(input.getPath().replace(def_path,""));
                fileParameters.setEntrySize(0);
                fileParameters.setCompressionMethod(CompressionMethod.STORE);
                fileParameters.setEncryptFiles(false);
                fileParameters.setEncryptionMethod(EncryptionMethod.NONE);
                fileParameters.setEntryCRC(0);
                fileParameters.setWriteExtendedLocalFileHeader(true);
            }
            fileParameters.setLastModifiedFileTime(input.lastModified());
            mAddZipOutputStream.putNextEntry(fileParameters);
            FileHeader fh_close=null;
            if (input.isDirectory()) {
                fh_close=mAddZipOutputStream.closeEntry();
            } else {
                inputStream = new BufferedInputStream(input.getInputStream(), IO_AREA_SIZE*4);
                long fsz=inputStream.available();
                long read_count=0L;
                int progress=0;
                if (p_cbl!=null) p_cbl.onCallBack(mContext, true, new Object[]{progress});
                while ((readLen = inputStream.read(readBuff)) != -1) {
                    if (isAborted()) break;
                    mAddZipOutputStream.write(readBuff, 0, readLen);
                    read_count+=readLen;
                    notifyProgress(p_cbl, read_count, fsz);
                }
                mAddZipOutputStream.closeEntry();

                inputStream.close();
            }
            if (!isAborted()) {
                List<FileHeader> fhl= mAddZipModel.getCentralDirectory().getFileHeaders();
                List<LocalFileHeader> lfhl= mAddZipModel.getLocalFileHeaders();
                log.debug("addItemInternal Central FileHeader size="+fhl.size()+", Local Fileheader size="+lfhl.size());
                for(int i = mAddZipFileHeaderList.size(); i<fhl.size(); i++) {
                    FileHeader fh=fhl.get(i);
                    BzfFileHeaderItem bfhi=new BzfFileHeaderItem();
                    bfhi.file_header=fh;
                    mAddZipFileHeaderList.add(bfhi);
                    byte[] gpflags=fh.getGeneralPurposeFlag();
                    log.debug("addItemInternal added name="+fh.getFileName()+", GeneralPurposeFlag="+ StringUtil.getHexString(gpflags, 0, gpflags.length));
                }
                mAddZipFileItemAdded=true;
                result=true;
            }
        } catch (ZipException e) {
            if (inputStream != null) try {inputStream.close();} catch (IOException ex) {}
            if (mAddZipOutputStream != null) try {mAddZipOutputStream.close();} catch (IOException ex) {}
            throw e;
        } catch (Exception e) {
            if (inputStream != null) try {inputStream.close();} catch (IOException ex) {}
            if (mAddZipOutputStream != null) try {mAddZipOutputStream.close();} catch (IOException ex) {}
            throw new ZipException(e);
        }
        return result;
    }

    private void checkClosed() throws ZipException {
        if (closed) throw new ZipException("BufferedZipFile3 is closed.");
    }

    private void removeItemIfExistst() {
        if (mAddZipFileHeaderList !=null && mAddZipFileHeaderList.size()>0) {
            ArrayList<BzfFileHeaderItem> sort_list=new ArrayList<BzfFileHeaderItem>();
            sort_list.addAll(mAddZipFileHeaderList);
            Collections.sort(sort_list, new Comparator<BzfFileHeaderItem>(){
                @Override
                public int compare(BzfFileHeaderItem lhs, BzfFileHeaderItem rhs) {
                    if (!lhs.file_header.getFileName().equalsIgnoreCase(rhs.file_header.getFileName()))
                        return lhs.file_header.getFileName().compareToIgnoreCase(rhs.file_header.getFileName());
                    return (int) (rhs.file_header.getOffsetLocalHeader()-lhs.file_header.getOffsetLocalHeader());
                }
            });

            //Check duplicate entry
            String prev_name="";
            ArrayList<BzfFileHeaderItem> removed_list_for_add=new ArrayList<BzfFileHeaderItem>();
            for(BzfFileHeaderItem item:sort_list) {
                if (!prev_name.equals(item.file_header.getFileName())) {
                    prev_name=item.file_header.getFileName();
                } else {
                    removed_list_for_add.add(item);
                }
            }

            for(BzfFileHeaderItem added_item: mAddZipFileHeaderList) {
                if (!added_item.isRemovedItem) {
                    for(BzfFileHeaderItem removed_item:removed_list_for_add) {
                        if (added_item.file_header.getFileName().equals(removed_item.file_header.getFileName()) &&
                                added_item.file_header.getOffsetLocalHeader()==removed_item.file_header.getOffsetLocalHeader()) {
                            added_item.isRemovedItem=true;
                            break;
                        }
                    }
                }
            }
            for(BzfFileHeaderItem primary_item: mInputZipFileHeaderList) {
                if (!primary_item.isRemovedItem) {
                    for(BzfFileHeaderItem removed_item: mAddZipFileHeaderList) {
                        if (primary_item.file_header.getFileName().equals(removed_item.file_header.getFileName())) {
                            primary_item.isRemovedItem=true;
                            mInpuZipFileItemRemoved =true;
                            break;
                        }
                    }
                }
            }
        }
    };

    public void destroy() throws ZipException, IOException {
        checkClosed();
        closed =true;
        if (mOutputZipFileStream !=null) mOutputZipFileStream.close();
        if (mAddZipOutputStream !=null) mAddZipOutputStream.close();
        if (mOutputSafFile != null) mOutputSafFile.deleteIfExists();

//        if (mTempOsFile !=null && mTempOsFile.exists()) mTempOsFile.delete();
        if (mAddSafFile !=null && mAddSafFile.exists()) mAddSafFile.delete();

    }

    private int getInputFileSize() {
        try {
            if (mInputSafFile.isSafFile()) {
                if (mInputUri==null) return -1;
                InputStream is=mContext.getContentResolver().openInputStream(mInputUri);
                return is.available();
            } else {
                return (int)mInputSafFile.length();
            }
        } catch(Exception e) {
            return -1;
        }
    }

    public boolean close() throws ZipException, Exception {
        return close(null);
    }

    public boolean close(CallBackListener cbl) throws ZipException, Exception {
        checkClosed();
        closed =true;
        boolean updated=false;
//        closeFull();
        log.debug("close entered, added="+mAddZipFileItemAdded+", removed="+mInpuZipFileItemRemoved);
        if (mAddZipFileItemAdded || mInpuZipFileItemRemoved) {
            if (cbl!=null) cbl.onCallBack(mContext, true, new Object[]{0});
//            if (getInputFileSize()>0) closeUpdate(cbl);
            if (!mEmptyInputZipFile) closeUpdate(cbl);
            else closeAddOnly(cbl);
            updated=true;
        }
        return updated;
    }

    private void closeAddOnly(CallBackListener cbl) throws ZipException, Exception {
        log.debug("closeAddOnly entered");
        long b_time= System.currentTimeMillis();
        try {
            mOutputZipFilePosition =0;
            if (mAddSafFile !=null) {
                if (mAddZipModel !=null && mAddZipModel.getEndOfCentralDirectoryRecord()!=null) {
                    dumpFileHeaderList("WriteHeader", mAddZipFileHeaderList);

                    mAddZipOutputStream.flush();;
                    mAddZipOutputStream.close();

                    String out_dir=mOutputSafFile.getParent();
                    String add_dir=mAddSafFile.getParent();
                    if (out_dir.equals(add_dir)) {
                        mOutputSafFile.deleteIfExists();
                        boolean rc=mAddSafFile.renameTo(mOutputSafFile);
                        if (!rc && !mAddSafFile.exists() && mOutputSafFile.exists()) rc=true;
                        if (!rc) throw new ZipException("Rename failed at closeAddOnly()");
                    } else {
                        mOutputSafFile.deleteIfExists();
                        mOutputSafFile.createNewFile();
                        mOutputOsFileStream=mOutputSafFile.getOutputStream();

                        InputStream fis=mAddSafFile.getInputStream();
                        byte[] buff=new byte[IO_AREA_SIZE*4];
                        int rc=0;
                        long read_byte=0;
                        long fsz=fis.available();
                        while((rc=fis.read(buff))>0) {
                            if (isAborted()) {
                                log.debug("closeAddOnly aborted");
                                break;
                            }
                            mOutputOsFileStream.write(buff,0,rc);
                            read_byte+=rc;
                            notifyProgress(cbl, read_byte, fsz);
                        }
                        mOutputOsFileStream.flush();
                        mOutputOsFileStream.close();
                        mAddSafFile.delete();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
        log.debug("closeAddOnly elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private void notifyProgress(CallBackListener cbl, long read_byte, long file_size) {
        if (cbl!=null && file_size>0) {
            int progress=(int)((read_byte*100)/file_size);
            cbl.onCallBack(mContext, true, new Object[]{progress>100?100:progress});
//            log.info("progress="+progress);
        }
    }

    private boolean mAbort=false;
    synchronized public boolean isAborted() {return mAbort;}
    synchronized public void abort() {mAbort=true;}

    private boolean mZipOutputFinalyzeRequired=false;
    private long mToBeProcessByteCount =0, mProcessedByteCount =0;
    private void closeUpdate(CallBackListener cbl) throws ZipException, Exception {
        log.debug("closeUpdate entered");
        long b_time= System.currentTimeMillis();
        try {
            removeItemIfExistst();

            mOutputZipFilePosition =0;

            mOutputSafFile.deleteIfExists();
            mOutputSafFile.createNewFile();
            mOutputOsFileStream=mOutputSafFile.getOutputStream();
            mOutputZipFileStream=new BufferedOutputStream(mOutputOsFileStream,IO_AREA_SIZE*4);

            mToBeProcessByteCount = getZipItemCount();
//            log.info("mToBeProcessByteCount="+mToBeProcessByteCount);

            if (!mEmptyInputZipFile) copyInputZipFile(cbl);

            if (mAddZipModel !=null) {
                mAddZipOutputStream.flush();;
                mAddZipOutputStream.close();

                appendAddZipFile(cbl);
            }

            if (mZipOutputFinalyzeRequired) {
                mInputZipModel.getEndOfCentralDirectoryRecord().setOffsetOfStartOfCentralDirectory(mOutputZipFilePosition);

                dumpFileHeaderList("WriteHeader", mInputZipFileHeaderList);
                dumpFileHeaderList("WriteHeader", mAddZipFileHeaderList);

                HeaderWriter hw=new HeaderWriter();
                hw.finalizeZipFile(mInputZipModel, mOutputZipFileStream, Charset.forName(mEncoding));

                mOutputZipFileStream.flush();
                mOutputZipFileStream.close();
            } else {
                ZipUtil.writeEmptyZipHeader(mOutputZipFileStream);
                mOutputZipFileStream.flush();
                mOutputZipFileStream.close();
            }
            if (mAddSafFile !=null) mAddSafFile.deleteIfExists();

        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
        log.debug("closeUpdate elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private long getZipItemCount() {
        long size=0;
        if (mInpuZipFileItemRemoved) {
            for(int i = 0; i< mInputZipFileHeaderList.size(); i++) {
                BzfFileHeaderItem rfhli= mInputZipFileHeaderList.get(i);
                if (!rfhli.isRemovedItem) {
                    size+=rfhli.file_header.getCompressedSize();
//                    log.info("remove size="+rfhli.file_header.getCompressedSize()+", name="+rfhli.file_header.getFileName());
                }
            }
        } else {
            size=mInputSafFile.length();
        }

        if (mAddZipFileHeaderList!=null) {
            size+=mAddSafFile.length();
        }
        return size;
    }

    private void copyInputZipFile(CallBackListener cbl) throws IOException, Exception {
        log.debug("copyInputZipFile entered");
        long b_time= System.currentTimeMillis();
        if (mEmptyInputZipFile) return;

        Collections.sort(mInputZipFileHeaderList, new Comparator<BzfFileHeaderItem>(){
            @Override
            public int compare(BzfFileHeaderItem o1, BzfFileHeaderItem o2) {
                long diff=(o1.file_header.getOffsetLocalHeader()-o2.file_header.getOffsetLocalHeader());
                int result=0;
                if (diff>0) result=1;
                else if (diff==0) result=0;
                else result=-1;
                return result;
            }
        });

        dumpZipModel("WriteRemoveFile", mInputZipModel);
        dumpFileHeaderList("WriteRemoveFile", mInputZipFileHeaderList);
        SeekableInputStream input_file_stream =null;
        try {
            if (mInputSafFile.isSafFile()) input_file_stream=new SeekableInputStream(mContext, mInputUri, mInputSafFile.length());
            else input_file_stream=new SeekableInputStream(mContext, mInputSafFile.getFile());
            if (mInpuZipFileItemRemoved) {
                for(int i = 0; i< mInputZipFileHeaderList.size(); i++) {
                    if (isAborted()) {
                        log.debug("copyInputZipFile aborted");
                        break;
                    } else {
                        BzfFileHeaderItem rfhli= mInputZipFileHeaderList.get(i);
                        if (!rfhli.isRemovedItem) {
                            long primary_file_start_pos=rfhli.file_header.getOffsetLocalHeader();
                            rfhli.file_header.setOffsetLocalHeader(mOutputZipFilePosition);
                            long end_pos=0;
                            if (i==(mInputZipFileHeaderList.size()-1)) {//end pos=startCentralRecord-1
                                long offsetStartCentralDir = mInputZipModel.getEndOfCentralDirectoryRecord().getOffsetOfStartOfCentralDirectory();
                                if (mInputZipModel.isZip64Format()) {
                                    if (mInputZipModel.getZip64EndOfCentralDirectoryRecord() != null) {
                                        offsetStartCentralDir = mInputZipModel.getZip64EndOfCentralDirectoryRecord().getOffsetStartCentralDirectoryWRTStartDiskNumber();
                                    }
                                }
                                end_pos=offsetStartCentralDir-1;
                            } else {
                                end_pos= mInputZipFileHeaderList.get(i+1).file_header.getOffsetLocalHeader()-1;
                            }
//                            long end_pos_x=primary_file_start_pos+30+rfhli.file_header.getExtraFieldLength()+
//                                    rfhli.file_header.getFileNameLength()+rfhli.file_header.getCompressedSize();
//                            log.info("end_pos="+end_pos+", end_pos_x="+end_pos_x+", diff="+(end_pos-end_pos_x));
                            mOutputZipFilePosition +=copyZipFile(rfhli.file_header.getFileName(),
                                    mOutputZipFileStream, input_file_stream, primary_file_start_pos, end_pos, cbl);
                            mZipOutputFinalyzeRequired=true;
                        } else {
                            mInputZipModel.getCentralDirectory().getFileHeaders().remove(rfhli.file_header);
                        }
                    }
                }
            } else {
                long end_pos=0;
                long offsetStartCentralDir = mInputZipModel.getEndOfCentralDirectoryRecord().getOffsetOfStartOfCentralDirectory();
                if (mInputZipModel.isZip64Format()) {
                    if (mInputZipModel.getZip64EndOfCentralDirectoryRecord() != null) {
                        offsetStartCentralDir = mInputZipModel.getZip64EndOfCentralDirectoryRecord().getOffsetStartCentralDirectoryWRTStartDiskNumber();
                    }
                }
                if (offsetStartCentralDir>1) {
                    end_pos=offsetStartCentralDir-1;
                    mOutputZipFilePosition +=copyZipFile("**copy_all_local_record", mOutputZipFileStream, input_file_stream, 0, end_pos, cbl);
                }
            }
        } finally {
            if (input_file_stream!=null) try {input_file_stream.close();} catch(Exception e){};
        }
        log.debug("copyInputZipFile elapsed time="+(System.currentTimeMillis()-b_time));
    };

    private void appendAddZipFile(CallBackListener cbl) throws ZipException, Exception {
        log.debug("appendAddZipFile entered");
        long b_time= System.currentTimeMillis();

        long offsetStartCentralDir= mAddSafFile.length();
        if (mEmptyInputZipFile) {
//            mTempOsFile.delete();
//            mAddOsFile.renameTo(mTempOsFile);
            mOutputZipFilePosition++;
        } else {
            dumpZipModel("WriteAddZipFile", mAddZipModel);
            SeekableInputStream input_file_stream =null;
            try {
//                input_file_stream=new SeekableInputStream(mContext, new SafFile3(mContext, mAddOsFile.getPath()).getUri());
                if (mAddSafFile.getUri()==null) input_file_stream=new SeekableInputStream(mContext, mAddSafFile.getFile());
                else input_file_stream=new SeekableInputStream(mContext, mAddSafFile.getUri(), mAddSafFile.length());
                long base_pointer= mOutputZipFilePosition;
                for(int i = 0; i< mAddZipFileHeaderList.size(); i++) {
                    if (isAborted()) {
                        log.debug("appendAddZipFile aborted");
                        break;
                    } else {
                        BzfFileHeaderItem fh= mAddZipFileHeaderList.get(i);
                        fh.file_header.setOffsetLocalHeader(mOutputZipFilePosition);
                        long end_pos=0;
                        if (i==(mAddZipFileHeaderList.size()-1)) {//end pos=startCentralRecord-1
                            end_pos=offsetStartCentralDir;
                        } else {
                            end_pos= mAddZipFileHeaderList.get(i+1).file_header.getOffsetLocalHeader()-1;
                        }
                        mOutputZipFilePosition +=copyZipFile(fh.file_header.getFileName(),
                                mOutputZipFileStream, input_file_stream, fh.file_header.getOffsetLocalHeader()-base_pointer, end_pos, cbl);
                        mInputZipModel.getCentralDirectory().getFileHeaders().add(fh.file_header);

                        mZipOutputFinalyzeRequired=true;
                    }
                }
            } finally {
                if (input_file_stream!=null) try {input_file_stream.close();} catch(Exception e){};
            }
        }
        log.debug("appendAddZipFile elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private long copyZipFile(String name, BufferedOutputStream bos, SeekableInputStream input_file, long start_pos, long end_pos, CallBackListener cbl)
            throws IOException, Exception {
        long item_size=(end_pos-start_pos)+1;
        if (log.isTraceEnabled())
            log.trace("CopyZipFile output="+ String.format("%#010x", mOutputZipFilePosition)+
                    ", start="+ String.format("%#010x",start_pos)+", end="+ String.format("%#010x",end_pos)+", length="+item_size+", Name="+name);
        byte[] buff=null;
        if (item_size>IO_AREA_SIZE) buff=new byte[IO_AREA_SIZE];
        else {
            if (item_size<1) throw new Exception("Buffer size error. size="+item_size);
            buff=new byte[(int)item_size];
        }
        int bufsz=buff.length;

        long output_size=0;
        int read_size=buff.length;
        try {
            input_file.seek(start_pos);
            int rc=input_file.read(buff,0,bufsz);
            int progress=0;
            while(rc>0) {
                if (isAborted()) {
                    log.debug("copyZip aborted");
                    break;
                } else {
                    bos.write(buff, 0, rc);
                    if (cbl!=null) {
                        mProcessedByteCount +=rc;
                        notifyProgress(cbl, mProcessedByteCount, mToBeProcessByteCount);
                    }
                    output_size+=rc;
                    if (item_size>output_size) {
                        if ((item_size-output_size)>0) {
                            read_size=(int) ((item_size-output_size)>bufsz?bufsz:(item_size-output_size));
                            rc=input_file.read(buff,0,read_size);
                        } else break;
                    } else break;
                }
            }
        } catch (IOException e) {
            throw new IOException(e);
        }

        return output_size;
    };

    private FileHeader getInputZipFileFileHeader(String item_name) throws ZipException {
        if (mInputZipModel==null) return null;

        for(FileHeader item:mInputZipModel.getCentralDirectory().getFileHeaders()) {
            if (item.getFileName().equals(item_name)) {
                return item;
            }
        }
        return null;
    }

    public void removeItem(String[] remove_list) throws ZipException {
        checkClosed();
        ArrayList<FileHeader> fhl=new ArrayList<FileHeader>();
        for(String item:remove_list) {
            FileHeader fh=null;
            try {
                fh= getInputZipFileFileHeader(item);
            } catch (ZipException ze) {
            }
            if (fh!=null) fhl.add(fh);
        }
        if (fhl.size()>0) removeItem(fhl);
    }

    public void removeItem(FileHeader del_fh)
            throws ZipException {
        checkClosed();
        ArrayList<FileHeader> fhl=new ArrayList<FileHeader>();
        fhl.add(del_fh);
        removeItemInternal(fhl, mInputZipFileHeaderList);
        if (mAddZipFileHeaderList !=null && mAddZipFileHeaderList.size()>0) removeItemInternal(fhl, mAddZipFileHeaderList);
    }

    public void removeItem(ArrayList<FileHeader> remove_list)
            throws ZipException {
        checkClosed();
        removeItemInternal(remove_list, mInputZipFileHeaderList);
        if (mAddZipFileHeaderList !=null && mAddZipFileHeaderList.size()>0) removeItemInternal(remove_list, mAddZipFileHeaderList);
    }

    @SuppressLint("NewApi")
    private void removeItemInternal(ArrayList<FileHeader> remove_item_list,
                                    ArrayList<BzfFileHeaderItem> bzf_file_header_list) throws ZipException {
        checkClosed();
        for(FileHeader fh:remove_item_list) if (log.isDebugEnabled()) log.debug("removeItemInternal selected name="+fh.getFileName());
        for(int i=0;i<bzf_file_header_list.size();i++) {
            BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
            if (!rfhli.isRemovedItem) {
                for(FileHeader remove_item:remove_item_list) {
                    if (rfhli.file_header.getFileName().equals(remove_item.getFileName())) {
                        log.debug("removeItemInternal remove item="+rfhli.file_header.getFileName());
                        rfhli.isRemovedItem=true;
                        mInpuZipFileItemRemoved =true;
                    }
                }
            }
        }
        dumpFileHeaderList("AfterDeleted", bzf_file_header_list);
    }

    public boolean exists(String fp) {
        boolean result=false;
        for(BzfFileHeaderItem rfhli:mInputZipFileHeaderList) {
            if (!rfhli.isRemovedItem) {
//                log.debug("exists 0 fp="+fp+", list="+rfhli.file_header.getFileName());
                if (rfhli.file_header.getFileName().equals(fp)) {
                    result=true;
                    break;
                }
            }
        }
//        log.debug("exists 1 result="+result);
        if (!result) {
            result= isAlreadyAdded(fp);
//            log.debug("exists 2 result="+result);
        }
        return result;
    }

    public ArrayList<FileHeader> getFileHeaderList() {
        ArrayList<FileHeader> fhl=new ArrayList<FileHeader>();

        if (mInputZipFileHeaderList!=null) {
            for(BzfFileHeaderItem rfhli:mInputZipFileHeaderList) {
                if (!rfhli.isRemovedItem) {
                    fhl.add(rfhli.file_header);
                }
            }
        }

        if (mAddZipFileHeaderList!=null) {
            for(BzfFileHeaderItem added: mAddZipFileHeaderList) {
                if (!added.isRemovedItem) {
                    fhl.add(added.file_header);
                }
            }
        }

        return fhl;
    }

    public FileHeader getFileHeader(FileHeader fh) {
        return getFileHeader(fh.getFileName());
    }

    public FileHeader getFileHeader(String fp) {
        FileHeader result=null;
        if (mInputZipFileHeaderList!=null) {
            for(BzfFileHeaderItem rfhli:mInputZipFileHeaderList) {
                if (!rfhli.isRemovedItem) {
                    if (rfhli.file_header.getFileName().equals(fp)) {
                        result=rfhli.file_header;
                        break;
                    }
                }
            }
        }
        if (result==null && mAddZipFileHeaderList!=null) {
            for(BzfFileHeaderItem added: mAddZipFileHeaderList) {
                if (!added.isRemovedItem && added.file_header.getFileName().equals(fp)) {
                    result=added.file_header;
                    break;
                }
            }
        }
        return result;
    }

    private void dumpZipModel(String id, ZipModel zm) {
        if (!log.isTraceEnabled() ||zm==null || zm.getEndOfCentralDirectoryRecord()==null) return;
        long offsetStartCentralDir = zm.getEndOfCentralDirectoryRecord().getOffsetOfStartOfCentralDirectory();
        if (zm.isZip64Format()) {
            if (zm.getZip64EndOfCentralDirectoryRecord() != null) {
                offsetStartCentralDir = zm.getZip64EndOfCentralDirectoryRecord().getOffsetStartCentralDirectoryWRTStartDiskNumber();
            }
        }

        log.trace(id+" offsetStartCentralDir="+String.format("%#010x", offsetStartCentralDir));
        List<FileHeader> fhl=zm.getCentralDirectory().getFileHeaders();
        for(FileHeader fh:fhl) {
            log.trace(id+" FileHeader comp size="+fh.getCompressedSize()+
                    ", header offset="+String.format("%#010x",fh.getOffsetLocalHeader())+
                    ", crc="+String.format("%#010x",fh.getCrc())+
                    ", GPF=0x"+ StringUtil.getHexString(fh.getGeneralPurposeFlag(), 0,2)+
                    ", extra field length="+fh.getExtraFieldLength()+
                    ", name="+fh.getFileName());
        }
    }

    private void dumpFileHeaderList(String id, ArrayList<BzfFileHeaderItem> bzf_file_header_list) {
        if (!log.isTraceEnabled() || bzf_file_header_list==null) return;
        for(int i=0;i<bzf_file_header_list.size();i++) {
            BufferedZipFile3.BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
            log.trace(id+" BzFileHeader comp size="+rfhli.file_header.getCompressedSize()+
                    ", header offset="+String.format("%#010x",rfhli.file_header.getOffsetLocalHeader())+
                    ", crc="+String.format("%#010x",rfhli.file_header.getCrc())+
                    ", extra field length="+rfhli.file_header.getExtraFieldLength()+
                    ", removed="+rfhli.isRemovedItem+
                    ", name="+rfhli.file_header.getFileName());
        }
    }

}
