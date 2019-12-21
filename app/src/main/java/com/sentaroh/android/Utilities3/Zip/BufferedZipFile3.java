package com.sentaroh.android.Utilities3.Zip;
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import com.sentaroh.android.Utilities3.SafFile3;
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
    //    private ZipFile mInputZipFile =null;
    private ZipFile mAddZipFile =null;
    //    private File mInputOsFile =null;
//    private File mOutputOsFile =null;
    private File mTempOsFile =null, mAddOsFile =null;
    private ZipModel mInputZipModel=null;

    private ZipModel mAddZipModel =null;
    private ArrayList<BzfFileHeaderItem> mInputZipFileHeaderList =null;
    private ArrayList<BzfFileHeaderItem> mAddZipFileHeaderList =null;
    private OutputStream mOutputOsFileStream =null;
    private long mOutputZipFilePosition =0;
    private BufferedOutputStream mOutputZipFileStream =null;

    private SafFile3 mInputSafFile=null;
    private SafFile3 mOutputSafFile=null;
    private Uri mInputUri=null;
    private Uri mOutputUri=null;

    private static final int IO_AREA_SIZE=1024*1024;
    private ZipOutputStream mAddZipOutputStream =null;
    private SplitOutputStream mAddSplitOutputStream = null;

    private String mEncoding =DEFAULT_ZIP_FILENAME_ENCODING;
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

    public BufferedZipFile3(Context c, String input_path, String output_path, String encoding, String wfp) {
        mContext=c;
        SafFile3 in_uri=input_path!=null?new SafFile3(mContext, input_path):null;
        SafFile3 out_uri=new SafFile3(mContext, output_path);
        init(in_uri, out_uri, encoding, wfp);
    }

    //    public BufferedZipFile3(Context c, File input_file, File output_file, String encoding, String wfp) {
//        mContext=c;
//        Uri in_uri=Uri.fromFile(input_file);
//        Uri out_uri=Uri.fromFile(output_file);
//        init(in_uri, out_uri, encoding, wfp);
//    }
//
    public BufferedZipFile3(Context c, SafFile3 input_file, SafFile3 output_file, String encoding, String wfp) {
        mContext=c;
        init(input_file, output_file, encoding, wfp);
    }

    private boolean mEmptyInputZipFile=true;
    private void init(SafFile3 in_uri, SafFile3 out_uri, String encoding, String work_file_path) {
        log.debug("<init> Input="+in_uri+", Output="+out_uri+", Encoding="+encoding+", wfp="+work_file_path);
        mInputSafFile=in_uri;
        mOutputSafFile=out_uri;
        mInputUri=mInputSafFile!=null?mInputSafFile.getUri():null;
        mEncoding =encoding;
        mTempOsFile =new File(work_file_path+"/ziputility.tmp");
        mAddOsFile =new File(work_file_path+"/ziputility.add");
        mInputZipFileHeaderList =new ArrayList<BzfFileHeaderItem>();
        try {
            HeaderReader header_reader=new HeaderReader();
            SeekableInputStream sis=null;
            if (mInputSafFile!=null && mInputSafFile.exists()) {
                if (mInputSafFile.isSafFile()) sis=new SeekableInputStream(mContext, mInputUri);
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

    private char[] mPassword=null;
    public void setPassword(String password) {
        if (password!=null) mPassword=password.toCharArray();
    }

    public boolean addItem(String input, ZipParameters zp) throws IOException {
        return addItem(new SafFile3(mContext, input), zp);
    }

    public boolean addItem(File input, ZipParameters zp) throws IOException {
        return addItem(new SafFile3(mContext, input.getPath()), zp);
    }

    public boolean addItem(SafFile3 in_uri, ZipParameters zp) throws IOException {
        checkClosed();
        if (mAddZipFile ==null) {
            mAddOsFile.delete();
            mAddZipFile =new ZipFile(mAddOsFile);
            mAddZipFile.setCharset(Charset.forName(mEncoding));
            mAddZipFileHeaderList =new ArrayList<BzfFileHeaderItem>();
            mAddZipModel = new ZipModel();
            mAddZipModel.setZipFile(mAddOsFile);
            mAddZipModel.setEndOfCentralDirectoryRecord(createEndOfCentralDirectoryRecord());
            mAddZipModel.setSplitArchive(false);
            mAddZipModel.setSplitLength(-1);

            try {
                mAddSplitOutputStream = new SplitOutputStream(new File(mAddZipModel.getZipFile().getPath()), mAddZipModel.getSplitLength());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("addItem addSplitOutputStream was not created",e);
                return false;
            }
            EncryptionMethod em=zp.getEncryptionMethod();
            if (em.name().equals(EncryptionMethod.NONE.name())) mAddZipOutputStream =new ZipOutputStream(mAddSplitOutputStream, null, Charset.forName(mEncoding), mAddZipModel);
            else  mAddZipOutputStream =new ZipOutputStream(mAddSplitOutputStream, mPassword, Charset.forName(mEncoding), mAddZipModel);
        }
        return addItemInternal(in_uri, zp);
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

    private ZipModel readZipInfo(Uri uri, String encoding) throws ZipException {
        ZipModel zipModel=null;
        SeekableInputStream ss=null;
        try {
            ss=new SeekableInputStream(mContext, uri);
            HeaderReader headerReader = new HeaderReader();
            zipModel = headerReader.readAllHeaders(ss, Charset.forName(encoding));
        } catch (FileNotFoundException e) {
            throw new ZipException(e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        return zipModel;
    }

    private boolean isDuplicateEntry(String fp) {
        boolean result=false;
        for(BzfFileHeaderItem added: mAddZipFileHeaderList) {
            if (!added.isRemovedItem && added.file_header.getFileName().equals(fp)) {
                result=true;
                break;
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
                if (fp.endsWith("."+item)) {
                    result=true;
                    break;
                }
            }
        }
        return result;
    }

    private boolean addItemInternal(SafFile3 input, ZipParameters parameters) throws ZipException {
        boolean result=false;
        if (isDuplicateEntry(input.getPath())) throw new ZipException("BufferedZipFile3 Already added, name="+input.getPath());
        BufferedInputStream inputStream =null;
        try {
            byte[] readBuff = new byte[IO_AREA_SIZE];
            int readLen = -1;
//            ZipParameters fileParameters = (ZipParameters) parameters.clone();
            ZipParameters fileParameters = new ZipParameters(parameters);
            String fp_prefix="";
            if (input.getUuid().equals(SafFile3.SAF_FILE_PRIMARY_UUID)) {
                fp_prefix="/storage/emulated/0/";
            } else {
                fp_prefix="/storage/"+input.getUuid()+"/";
            }
            fileParameters.setDefaultFolderPath(input.getParent().getPath().replace(fp_prefix,""));
            fileParameters.setIncludeRootFolder(false);

            if (!input.isDirectory()) {
                fileParameters.setFileNameInZip(input.getPath().replace(fp_prefix,""));
                if (fileParameters.isEncryptFiles() && fileParameters.getEncryptionMethod() == EncryptionMethod.ZIP_STANDARD) {
                    fileParameters.setEntryCRC((int) computeFileCrc(input));
                }
                //Add no compress function 2016/07/22 F.Hoshino
//                if (Zip4jUtil.getFileLengh(input)<100 || !fileParameters.isCompressFileExtention(input.getName())) {
                if ((input.length()<100) || isNoCompressExtention(input.getPath())) {
                    fileParameters.setCompressionMethod(CompressionMethod.STORE);
                    fileParameters.setEntrySize(input.length());
                } else {
                    fileParameters.setCompressionMethod(parameters.getCompressionMethod());
                }
            } else {
                fileParameters.setFileNameInZip(input.getPath().replace(fp_prefix,"")+"/");
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
                while ((readLen = inputStream.read(readBuff)) != -1) {
                    mAddZipOutputStream.write(readBuff, 0, readLen);
                }
                mAddZipOutputStream.closeEntry();

                inputStream.close();
            }

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
            result=true;
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

        if (mTempOsFile !=null && mTempOsFile.exists()) mTempOsFile.delete();
        if (mAddOsFile !=null && mAddOsFile.exists()) mAddOsFile.delete();

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
    public void close() throws ZipException, Exception {
        checkClosed();
        closed =true;
//        closeFull();
        if (getInputFileSize()>0) closeUpdate();
        else closeAddOnly();
    }

    private void closeAddOnly() throws ZipException, Exception {
        log.debug("closeAddOnly entered");
        long b_time= System.currentTimeMillis();
        try {
            mOutputZipFilePosition =0;
            if (mAddZipFile !=null) {
                if (mAddZipModel !=null && mAddZipModel.getEndOfCentralDirectoryRecord()!=null) {
                    dumpFileHeaderList("WriteHeader", mAddZipFileHeaderList);

                    mAddZipOutputStream.flush();;
                    mAddZipOutputStream.close();

                    mOutputSafFile.deleteIfExists();
                    mOutputSafFile.createNewFile();
                    mOutputOsFileStream=mOutputSafFile.getOutputStream();

                    FileInputStream fis=new FileInputStream(mAddOsFile);
                    byte[] buff=new byte[IO_AREA_SIZE*4];
                    int rc=0;
                    while((rc=fis.read(buff))>0) {
                        mOutputOsFileStream.write(buff,0,rc);
                    }
                    mOutputOsFileStream.flush();
                    mOutputOsFileStream.close();
                    mAddOsFile.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
        log.debug("closeAddOnly elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private boolean mZipOutputFinalyzeRequired=false;
    private void closeUpdate() throws ZipException, Exception {
        log.debug("closeUpdate entered");
        long b_time= System.currentTimeMillis();
        try {
            removeItemIfExistst();

            mOutputZipFilePosition =0;

            mOutputSafFile.deleteIfExists();
            mOutputSafFile.createNewFile();
            mOutputOsFileStream=mOutputSafFile.getOutputStream();
            mOutputZipFileStream=new BufferedOutputStream(mOutputOsFileStream,IO_AREA_SIZE*4);

            if (!mEmptyInputZipFile) copyInputZipFile();

            if (mAddZipFile !=null) {
                mAddZipOutputStream.flush();;
                mAddZipOutputStream.close();

                appendAddZipFile();
            }

            if (mZipOutputFinalyzeRequired) {
                mInputZipModel.getEndOfCentralDirectoryRecord().setOffsetOfStartOfCentralDirectory(mOutputZipFilePosition);

                dumpFileHeaderList("WriteHeader", mInputZipFileHeaderList);
                dumpFileHeaderList("WriteHeader", mAddZipFileHeaderList);

                HeaderWriter hw=new HeaderWriter();
                hw.finalizeZipFile(mInputZipModel, mOutputZipFileStream, Charset.forName(mEncoding));

                mOutputZipFileStream.flush();
                mOutputZipFileStream.close();
            }
            if (mAddZipFile !=null) mAddZipFile.getFile().delete();

        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
        log.debug("closeUpdate elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private void copyInputZipFile() throws IOException, Exception {
        log.debug("copyInputZipFile entered");
        long b_time= System.currentTimeMillis();
        if (mEmptyInputZipFile) return;

        dumpZipModel("WriteRemoveFile", mInputZipModel);
        dumpFileHeaderList("WriteRemoveFile", mInputZipFileHeaderList);
        SeekableInputStream input_file_stream =null;
        try {
            if (mInputSafFile.isSafFile()) input_file_stream=new SeekableInputStream(mContext, mInputUri);
            else input_file_stream=new SeekableInputStream(mContext, mInputSafFile.getFile());
            if (mInpuZipFileItemRemoved) {
                for(int i = 0; i< mInputZipFileHeaderList.size(); i++) {
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
                        mOutputZipFilePosition +=copyZipFile(rfhli.file_header.getFileName(),
                                mOutputZipFileStream, input_file_stream, primary_file_start_pos, end_pos);
                        mZipOutputFinalyzeRequired=true;
                    } else {
                        mInputZipModel.getCentralDirectory().getFileHeaders().remove(rfhli.file_header);
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
                    mOutputZipFilePosition +=copyZipFile("**copy_all_local_record", mOutputZipFileStream, input_file_stream, 0, end_pos);
                }
            }
        } finally {
            if (input_file_stream!=null) try {input_file_stream.close();} catch(Exception e){};
        }
        log.debug("copyInputZipFile elapsed time="+(System.currentTimeMillis()-b_time));
    };

    private void appendAddZipFile() throws ZipException, Exception {
        log.debug("appendAddZipFile entered");
        long b_time= System.currentTimeMillis();

        long offsetStartCentralDir= mAddZipFile.getFile().length();
        if (mEmptyInputZipFile) {
            mTempOsFile.delete();
            mAddOsFile.renameTo(mTempOsFile);
            mOutputZipFilePosition++;
        } else {
            dumpZipModel("WriteAddZipFile", mAddZipModel);
            SeekableInputStream input_file_stream =null;
            try {
//                input_file_stream=new SeekableInputStream(mContext, new SafFile3(mContext, mAddOsFile.getPath()).getUri());
                input_file_stream=new SeekableInputStream(mContext, mAddOsFile);
                long base_pointer= mOutputZipFilePosition;
                for(int i = 0; i< mAddZipFileHeaderList.size(); i++) {
                    BzfFileHeaderItem fh= mAddZipFileHeaderList.get(i);
                    fh.file_header.setOffsetLocalHeader(mOutputZipFilePosition);
                    long end_pos=0;
                    if (i==(mAddZipFileHeaderList.size()-1)) {//end pos=startCentralRecord-1
                        end_pos=offsetStartCentralDir;
                    } else {
                        end_pos= mAddZipFileHeaderList.get(i+1).file_header.getOffsetLocalHeader()-1;
                    }
                    mOutputZipFilePosition +=copyZipFile(fh.file_header.getFileName(),
                            mOutputZipFileStream, input_file_stream, fh.file_header.getOffsetLocalHeader()-base_pointer, end_pos);
                    mInputZipModel.getCentralDirectory().getFileHeaders().add(fh.file_header);

                    mZipOutputFinalyzeRequired=true;
                }
            } finally {
                if (input_file_stream!=null) try {input_file_stream.close();} catch(Exception e){};
            }
        }
        log.debug("appendAddZipFile elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private long copyZipFile(String name, BufferedOutputStream bos, SeekableInputStream input_file, long start_pos, long end_pos)
            throws IOException, Exception {
        if (log.isTraceEnabled())
            log.trace("CopyZipFile output="+ String.format("%#010x", mOutputZipFilePosition)+
                    ", start="+ String.format("%#010x",start_pos)+", end="+ String.format("%#010x",end_pos)+", Name="+name);
        int item_size=(int) (end_pos-start_pos)+1;
        byte[] buff=null;
        if (item_size>(IO_AREA_SIZE)) buff=new byte[IO_AREA_SIZE];
        else {
            if (item_size<1) throw(new Exception("Buffer size error. size="+item_size));
            buff=new byte[item_size];
        }
        int bufsz=buff.length;

        long output_size=0;
        int read_size=buff.length;
        try {
            input_file.seek(start_pos);
            int rc=input_file.read(buff,0,bufsz);
            while(rc>0) {
                bos.write(buff, 0, rc);
                output_size+=rc;
                if (item_size>output_size) {
                    if ((item_size-output_size)>0) {
                        read_size=(int) ((item_size-output_size)>bufsz?bufsz:(item_size-output_size));
                        rc=input_file.read(buff,0,read_size);
                    } else break;
                } else break;
            }
        } catch (IOException e) {
            throw new IOException(e);
        }

        return output_size;
    };

    private FileHeader getFileHeader(String item_name) throws ZipException {
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
                fh= getFileHeader(item);
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
        for(FileHeader fh:remove_item_list) if (log.isDebugEnabled()) log.debug("removeItem selected name="+fh.getFileName());
        for(int i=0;i<bzf_file_header_list.size();i++) {
            BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
            if (!rfhli.isRemovedItem) {
                for(FileHeader remove_item:remove_item_list) {
                    if (rfhli.file_header.getFileName().equals(remove_item.getFileName())) {
                        rfhli.isRemovedItem=true;
                        mInpuZipFileItemRemoved =true;
                    }
                }
            }
        }
        dumpFileHeaderList("AfterDeleted", bzf_file_header_list);
    }

    private void dumpZipModel(String id, ZipModel zm) {
        if (!log.isTraceEnabled() ||zm==null || zm.getEndOfCentralDirectoryRecord()==null) return;
//        long offsetStartCentralDir = zm.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
//        if (zm.isZip64Format()) {
//            if (zm.getZip64EndCentralDirRecord() != null) {
//                offsetStartCentralDir = zm.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
//            }
//        }
//
//        log.trace(id+" offsetStartCentralDir="+String.format("%#010x", offsetStartCentralDir));
//        ArrayList<FileHeader> fhl=zm.getCentralDirectory().getFileHeaders();
//        for(FileHeader fh:fhl) {
//            log.trace(id+" FileHeader comp size="+fh.getCompressedSize()+
//                    ", header offset="+String.format("%#010x",fh.getOffsetLocalHeader())+
//                    ", crc32="+String.format("%#010x",fh.getCrc32())+
//                    ", name="+fh.getFileName());
//        }
    }

    private void dumpFileHeaderList(String id, ArrayList<BzfFileHeaderItem> bzf_file_header_list) {
        if (!log.isTraceEnabled() || bzf_file_header_list==null) return;
//        for(int i=0;i<bzf_file_header_list.size();i++) {
//            BufferedZipFile3.BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
//            log.trace(id+" BzFileHeader comp size="+rfhli.file_header.getCompressedSize()+
//                    ", header offset="+String.format("%#010x",rfhli.file_header.getOffsetLocalHeader())+
//                    ", crc32="+String.format("%#010x",rfhli.file_header.getCrc32())+
//                    ", removed="+rfhli.isRemovedItem+
//                    ", name="+rfhli.file_header.getFileName());
//        }
    }

}
