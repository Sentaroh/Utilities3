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

import android.content.Context;
import android.net.Uri;

import com.sentaroh.android.Utilities3.Dialog.ProgressBarDialogFragment;
import com.sentaroh.android.Utilities3.SafFile3;
import com.sentaroh.android.Utilities3.ThreadCtrl;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ZipUtil {

    public static final String DEFAULT_ZIP_FILENAME_ENCODING = "UTF-8";

    private static Logger log = LoggerFactory.getLogger(ZipUtil.class);

    static public String detectFileNameEncoding(Context c, String zip_path) throws IOException {
        String encoding = "";
        String result = null;
        UniversalDetector detector = new UniversalDetector(null);
//	    long b_time=System.currentTimeMillis();
        try {
            SafFile3 sf=new SafFile3(c, zip_path);
            List<FileHeader> fhl = sf.isSafFile()?getFileHeaders(c, sf.getUri(), "Cp850"):getFileHeaders(c, sf.getFile(), "Cp850");
            if (fhl.size() > 0) {
                StringBuilder enc_det = new StringBuilder(1024 * 1024 * 8);
                boolean fileNameUTF8Encoded = false;
                for (FileHeader fh : fhl) {
                    if (fh.isFileNameUTF8Encoded()) {
                        fileNameUTF8Encoded = true;
                        break;
                    }
                    enc_det.append(fh.getFileName());
                    if (enc_det.length() >= (1024 * 256)) {
//						Log.v("","length="+enc_det.length());
                        break;
                    }
                }
//				Log.v("","length="+enc_det.length()+", fileNameUTF8Encoded="+fileNameUTF8Encoded);
//				Log.v("","enc="+enc_det.toString());
                if (!fileNameUTF8Encoded) {
                    while (enc_det.length() < (1024 * 1024 * 1)) {
                        enc_det.append(enc_det.toString());
                    }
//					Log.v("","length="+enc_det.length());
                    byte[] det_buff = enc_det.toString().getBytes("Cp850");
                    detector.handleData(det_buff, 0, det_buff.length);
                    detector.dataEnd();
                    encoding = detector.getDetectedCharset();
                    detector.reset();
                    if (encoding != null) {
                        try {
                            if (!Charset.isSupported(encoding)) result = null;
                            else result = encoding;
                        } catch (IllegalCharsetNameException e) {
//				    		e.printStackTrace();
                            result = null;
                        }
                    }
                } else {
                    result = DEFAULT_ZIP_FILENAME_ENCODING;
                }
            }
        } catch (Exception e) {
            log.debug("detectFileNameEncoding", e);
            result = null;
            throw e;
        }
        log.trace("detectFileNameEncoding result=" + result);
        return result;
    }

    private static class DirectoryListItem {
        public String name = "";
        public String parent = "";
        public boolean added_directory = false;

        DirectoryListItem() {
        }

        DirectoryListItem(String entry, String parent, boolean added) {
            this.name = entry;
            this.parent = parent;
            added_directory = added;
        }
    }

    static private int findDirectoryList(ArrayList<DirectoryListItem> dir_list, String entry, String parent) {
        int idx = Collections.binarySearch(dir_list, new DirectoryListItem(entry, parent, false),
                new Comparator<DirectoryListItem>() {
                    @Override
                    public int compare(DirectoryListItem lhs, DirectoryListItem rhs) {
                        String l_key = lhs.parent + lhs.name;
                        String r_key = rhs.parent + rhs.name;
                        return l_key.compareToIgnoreCase(r_key);
                    }
                });
//		Log.v("","result="+idx+", name="+entry+", parent="+parent);
        return idx;
    }

    static private void addDirectory(StringBuilder sb, ArrayList<DirectoryListItem> dir_list,
                                     ArrayList<ZipFileListItem> zfl, String path, boolean isUtf8Enc) {
//		if (path.equals("")) return;
        String p_dir = "", dir_name = "";
        String[] dir_array = path.split("/");

        if (dir_array.length == 1) {
            int idx = findDirectoryList(dir_list, dir_array[0], "");
            if (idx < 0) {
                dir_list.add(new DirectoryListItem(dir_array[0], "", true));
                sortDirectoryList(dir_list);
            }
        } else {
            sb.setLength(0);
            for (int i = 0; i < (dir_array.length - 1); i++) {
                if (i == 0) sb.append(dir_array[i]);
                else sb.append("/").append(dir_array[i]);
            }
            p_dir = sb.toString();
            dir_name = dir_array[dir_array.length - 1];
            int idx = findDirectoryList(dir_list, dir_name, p_dir);
            if (idx < 0) {
                dir_list.add(new DirectoryListItem(dir_name, p_dir, true));
                sortDirectoryList(dir_list);
                addDirectory(sb, dir_list, zfl, p_dir, isUtf8Enc);
            }
        }
    }

    static public String isZipFile(Context c, String zip_path) {
        String result = null;
        try {
            List<FileHeader> fhl = getFileHeaders(c, new SafFile3(c, zip_path), "UTF-8");
        } catch (Exception e) {
            result=e.getMessage();
        }
        return result;
    }

    static public String isZipFile(Context c, SafFile3 sf) {
        String result = null;
        try {
            List<FileHeader> fhl = getFileHeaders(c, sf, "UTF-8");
        } catch (Exception e) {
            result=e.getMessage();
        }
        return result;
    }

    static public String isZipFile(Context c, File lf) {
        String result = null;
        try {
            List<FileHeader> fhl = getFileHeaders(c, lf, "UTF-8");
        } catch (Exception e) {
            result=e.getMessage();
        }
        return result;
    }

    static private void sortDirectoryList(ArrayList<DirectoryListItem> dir_list) {
        Collections.sort(dir_list, new Comparator<DirectoryListItem>() {
            @Override
            public int compare(DirectoryListItem lhs, DirectoryListItem rhs) {
                String l_key = lhs.parent + lhs.name;
                String r_key = rhs.parent + rhs.name;
                return l_key.compareToIgnoreCase(r_key);
            }
        });
    }

    static public ZipModel getZipModel(Context c, Uri zip_path, String password, String encoding) {
        try {
            SeekableInputStream sis = new SeekableInputStream(c, zip_path);
            HeaderReader header_reader = new HeaderReader();
            ZipModel zm = header_reader.readAllHeaders(sis, Charset.forName(encoding));
            return zm;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static public FileHeader getFileHeader(ZipModel zm, String item_name) throws ZipException {
        ArrayList<FileHeader>fh_list=(ArrayList<FileHeader>) zm.getCentralDirectory().getFileHeaders();
        return getFileHeader(fh_list, item_name);
    }

    static public FileHeader getFileHeader(ArrayList<FileHeader> fh_list, String item_name) throws ZipException {
        if (fh_list==null) return null;

        for(FileHeader item:fh_list) {
            if (item.getFileName().equals(item_name)) {
                return item;
            }
        }
        return null;
    }

    static public ArrayList<FileHeader> getFileHeaders(Context c, Uri zip_path, String encoding) throws IOException {
        try {
            SeekableInputStream sis = new SeekableInputStream(c, zip_path);
            HeaderReader header_reader = new HeaderReader();
            ZipModel zm = header_reader.readAllHeaders(sis, Charset.forName(encoding));
            ArrayList<FileHeader> file_header_list = (ArrayList<FileHeader>) zm.getCentralDirectory().getFileHeaders();
            return file_header_list;
        } catch (IOException e) {
//            log.info("error="+e.getMessage());
//            e.printStackTrace();
            throw e;
        }
//        return null;
    }

    static public ArrayList<FileHeader> getFileHeaders(Context c, File zip_file, String encoding) throws IOException {
        try {
            SeekableInputStream sis = new SeekableInputStream(c, zip_file);
            HeaderReader header_reader = new HeaderReader();
            ZipModel zm = header_reader.readAllHeaders(sis, Charset.forName(encoding));
            ArrayList<FileHeader> file_header_list = (ArrayList<FileHeader>) zm.getCentralDirectory().getFileHeaders();
            return file_header_list;
        } catch (IOException e) {
//            log.info("error="+e.getMessage());
//            e.printStackTrace();
            throw e;
        }
//        return null;
    }

    static public ArrayList<FileHeader> getFileHeaders(Context c, SafFile3 zip_file, String encoding) throws IOException {
        try {
            SeekableInputStream sis = null;
            if (zip_file.isSafFile()) sis=new SeekableInputStream(c, zip_file.getUri());
            else sis=new SeekableInputStream(c, zip_file.getFile());
            HeaderReader header_reader = new HeaderReader();
            ZipModel zm = header_reader.readAllHeaders(sis, Charset.forName(encoding));
            ArrayList<FileHeader> file_header_list = (ArrayList<FileHeader>) zm.getCentralDirectory().getFileHeaders();
            return file_header_list;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
//        return null;
    }

    static public ArrayList<FileHeader> getFileHeaders(ZipModel zm) {
        ArrayList<FileHeader> file_header_list = (ArrayList<FileHeader>) zm.getCentralDirectory().getFileHeaders();
        return file_header_list;
    }

    static public ArrayList<ZipFileListItem> buildZipFileList(Context c, String zip_path, String encoding) throws IOException {
        SafFile3 sf=new SafFile3(c, zip_path);
        return buildZipFileList(c, sf, encoding);
    }
    static public ArrayList<ZipFileListItem> buildZipFileList(Context c, Uri zip_path, String encoding) throws IOException {
        SafFile3 sf=new SafFile3(c, zip_path);
        return buildZipFileList(c, sf, encoding);
    }

    static private ArrayList<ZipFileListItem> buildZipFileList(Context c, SafFile3 zip_path, String encoding) throws IOException {
		ArrayList<ZipFileListItem> tfl=new ArrayList<ZipFileListItem>();
		try {
            List<FileHeader> fhl=getFileHeaders(c, zip_path, encoding);
			if (fhl.size()>0) {
				for(FileHeader fh:fhl) {
					String tfp=fh.getFileName();
					String t_path="", t_name="";//, w_t_name="";
					String w_path="";
					if (fh.isDirectory()) {
						w_path=tfp.endsWith("/")?tfp.substring(0,tfp.length()-1):tfp;
						t_name=w_path.lastIndexOf("/")>=0?w_path.substring(w_path.lastIndexOf("/")+1):w_path;
						t_path=w_path.substring(0,w_path.length()-t_name.length());
						if (t_path.endsWith("/")) t_path=t_path.substring(0,t_path.length()-1);
					} else {
						w_path=tfp;
						t_name=w_path.lastIndexOf("/")>=0?w_path.substring(w_path.lastIndexOf("/")+1):w_path; 
						t_path=w_path.substring(0,w_path.length()-t_name.length());
						if (t_path.endsWith("/")) t_path=t_path.substring(0,t_path.length()-1);
					}
					int zip_comp=fh.getCompressionMethod().getCode();
					if (zip_comp==CompressionMethod.AES_INTERNAL_ONLY.getCode()) {
					    zip_comp=fh.getAesExtraDataRecord().getCompressionMethod().getCode();
                    }
//					if (fh.getCompressionMethod()==CompressionMethod.STORE) zip_comp=ZipFileListItem.COMPRESSION_METHOD_STORE;
//					else if (fh.getCompressionMethod()==CompressionMethod.DEFLATE) zip_comp=ZipFileListItem.COMPRESSION_METHOD_DEFLATE;
					int zip_enc=0;
                    if (fh.getEncryptionMethod()==EncryptionMethod.AES) zip_enc=ZipFileListItem.ENCRPTION_METHOD_AES;
                    else if (fh.getEncryptionMethod()==EncryptionMethod.ZIP_STANDARD) zip_enc=ZipFileListItem.ENCRPTION_METHOD_ZIP;
					ZipFileListItem tfli=new ZipFileListItem(t_name, t_path,
							fh.isDirectory(), fh.isEncrypted(), fh.getUncompressedSize(), 
							dosToJavaTme((int) fh.getLastModifiedTime()), fh.getCompressedSize(),
							zip_comp, zip_enc,
							fh.isFileNameUTF8Encoded());
//					tfli.dump();
					tfl.add(tfli);
				}

				Collections.sort(tfl,new Comparator<ZipFileListItem>(){
					@Override
					public int compare(ZipFileListItem lhs, ZipFileListItem rhs) {
						return lhs.getParentDirectory().compareToIgnoreCase(rhs.getParentDirectory());
					}
				});

				ArrayList<DirectoryListItem> dir_list=new ArrayList<DirectoryListItem>();
				for(ZipFileListItem zfli:tfl) {
					if (zfli.isDirectory()) {
						DirectoryListItem dli=new DirectoryListItem();
						dli.name=zfli.getFileName();
						dli.parent=zfli.getParentDirectory();
						dir_list.add(dli);
					}
				}
				sortDirectoryList(dir_list);

				ArrayList<String> pdir_name_list=new ArrayList<String>();
				ArrayList<Boolean> pdir_utf8_list=new ArrayList<Boolean>();
				String prev_parent="";
				for(ZipFileListItem zfli:tfl) {
					if (!prev_parent.equalsIgnoreCase(zfli.getParentDirectory()) && !zfli.getParentDirectory().equals("")) {
						prev_parent=zfli.getParentDirectory();
						pdir_name_list.add(zfli.getParentDirectory());
						pdir_utf8_list.add(zfli.isUtf8Encoding());
					}
				}

				StringBuilder sb=new StringBuilder(1024);
				for(int i=0;i<pdir_name_list.size();i++) {
					addDirectory(sb, dir_list, tfl, pdir_name_list.get(i), pdir_utf8_list.get(i));
				}

				for(DirectoryListItem dli:dir_list) {
					if (dli.added_directory) {
						ZipFileListItem zfli=new ZipFileListItem(dli.name, dli.parent, true, false, 0, 0, 0, 0, 0, false);
						tfl.add(zfli);
					}
				}
				
				Collections.sort(tfl, new Comparator<ZipFileListItem>(){
					@Override
					public int compare(ZipFileListItem lhs, ZipFileListItem rhs) {
						String r_key=rhs.getParentDirectory();
						String l_key=lhs.getParentDirectory();
						if (l_key.equalsIgnoreCase(r_key)) {
							return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
						} else {
							return l_key.compareToIgnoreCase(r_key);
						}
					}
					
				});
			}
        }catch(Exception e) {
            log.debug("buildZipFileList",e);
            throw e;
		}
		return tfl;
	};

    /**
	 * Converts input time from Java to DOS format
	 * @param time
	 * @return time in DOS format 
	 */
	public static long javaToDosTime(long time) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		
		int year = cal.get(Calendar.YEAR);
		if (year < 1980) {
		    return (1 << 21) | (1 << 16);
		}
		return (year - 1980) << 25 | (cal.get(Calendar.MONTH) + 1) << 21 |
	               cal.get(Calendar.DATE) << 16 | cal.get(Calendar.HOUR_OF_DAY) << 11 | cal.get(Calendar.MINUTE) << 5 |
	               cal.get(Calendar.SECOND) >> 1;
	}
	
	/**
	 * Converts time in dos format to Java format
	 * @param dosTime
	 * @return time in java format
	 */
	public static long dosToJavaTme(int dosTime) {
		int sec = 2 * (dosTime & 0x1f);
	    int min = (dosTime >> 5) & 0x3f;
	    int hrs = (dosTime >> 11) & 0x1f;
	    int day = (dosTime >> 16) & 0x1f;
	    int mon = ((dosTime >> 21) & 0xf) - 1;
	    int year = ((dosTime >> 25) & 0x7f) + 1980;
	    
	    Calendar cal = Calendar.getInstance();
		cal.set(year, mon, day, hrs, min, sec);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime().getTime();
	}


	public static boolean createZipFile(Context c, ThreadCtrl tc,
                                        ProgressBarDialogFragment pbdf, String output_file_path,
                                        String default_root_folder, String... input_file_path) {
		return createEncZipFile(c, tc, pbdf, output_file_path, 
				"", EncryptionMethod.NONE, AesKeyStrength.KEY_STRENGTH_128,
				CompressionLevel.MAXIMUM, default_root_folder, input_file_path);
	};

	public static boolean createStandardEncZipFile(Context c, ThreadCtrl tc,
                                                   ProgressBarDialogFragment pbdf, String output_file_path,
                                                   String default_root_folder, String pswd, String... input_file_path) {
		return createEncZipFile(c, tc, pbdf, output_file_path,
				pswd, EncryptionMethod.ZIP_STANDARD, AesKeyStrength.KEY_STRENGTH_128,
				CompressionLevel.MAXIMUM, default_root_folder, input_file_path);
	};

	public static boolean createAes128EncZipFile(Context c, ThreadCtrl tc,
                                                 ProgressBarDialogFragment pbdf, String output_file_path,
                                                 String default_root_folder, String pswd, String... input_file_path) {
		return createEncZipFile(c, tc, pbdf, output_file_path, 
				pswd, EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_128,
                CompressionLevel.MAXIMUM, default_root_folder, input_file_path);

	};
	public static boolean createAes256EncZipFile(Context c, ThreadCtrl tc,
                                                 ProgressBarDialogFragment pbdf, String output_file_path,
                                                 String default_root_folder, String pswd, String... input_file_path) {
		return createEncZipFile(c, tc, pbdf, output_file_path,
				pswd, EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256,
				CompressionLevel.MAXIMUM, default_root_folder, input_file_path);

	};

	public static boolean createEncZipFile(Context c, ThreadCtrl tc,
                                           ProgressBarDialogFragment pbdf, String output_file_path,
                                           String pswd, EncryptionMethod enc_method, AesKeyStrength aes_strength, CompressionLevel comp_level,
                                           String default_root_folder, String... input_file_path) {
		InputStream is = null;
		ZipOutputStream zos = null;
        ZipParameters zp = new ZipParameters();
		zp.setDefaultFolderPath(default_root_folder);
		zp.setCompressionMethod(CompressionMethod.DEFLATE);
		zp.setCompressionLevel(comp_level);
		
		if (enc_method!=EncryptionMethod.NONE) {
			zp.setEncryptFiles(true);
			zp.setEncryptionMethod(enc_method);
			if (enc_method==EncryptionMethod.AES) zp.setAesKeyStrength(aes_strength);
            try {
                zos = new ZipOutputStream(new FileOutputStream(output_file_path), pswd.toCharArray());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
		} else {
            try {
                zos = new ZipOutputStream(new FileOutputStream(output_file_path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
		long total_size=0;
		for (int i=0; i<input_file_path.length; i++) {
			File lf=new File(input_file_path[i]);
			total_size+=lf.length();
		}
		try {
			long process_size=0;
			byte[] readBuff = new byte[1024*1024*2];
			for (int i=0; i<input_file_path.length; i++) {
				File file = new File(input_file_path[i]);
				zp.setFileNameInZip(file.getName());
				zp.setLastModifiedFileTime(file.lastModified());
				zp.setEntrySize(file.length());
				zos.putNextEntry(zp);
				
				if (file.isDirectory()) {
					zos.closeEntry();
					continue;
				}

				if (pbdf!=null) pbdf.updateMsgText(input_file_path[i]);
				
				is = new FileInputStream(file);
				int read_length = -1;
				
				while ((read_length = is.read(readBuff)) != -1) {
					if (tc!=null && !tc.isEnabled()) break; 
					else {
						zos.write(readBuff, 0, read_length);
						process_size+=read_length;
						if (pbdf!=null) {
							int progress=(int)((process_size*100)/total_size);
							pbdf.updateProgress(progress);
						}
					}
				}
				zos.closeEntry();
				is.close();
			}
			zos.flush();
		    zos.close();
		    
		} catch (IOException e) {
            log.debug("createEncZipFile",e);
//			e.printStackTrace();
			return false;
		}
		return true;
	};

    public static boolean isSupportedCompressionMethod(FileHeader fh) {
        boolean result=false;
        CompressionMethod cm=getCompressionMethod(fh);

        if (cm==CompressionMethod.STORE || cm==CompressionMethod.DEFLATE || cm==CompressionMethod.AES_INTERNAL_ONLY ||
                cm==CompressionMethod.BZIP2 || cm==CompressionMethod.LZMA
                || cm==CompressionMethod.DEFLATE64
        ) {
            result=true;
        }

        return result;
    }

    public static CompressionMethod getCompressionMethod(FileHeader fh) {
        CompressionMethod cm=fh.getCompressionMethod();
        if (fh.getCompressionMethod()==CompressionMethod.AES_INTERNAL_ONLY) cm=fh.getAesExtraDataRecord().getCompressionMethod();
        return cm;
    }

    public static String getCompressionMethodName(FileHeader fh) {
        CompressionMethod cm=getCompressionMethod(fh);
        return getCompressionMethodName(cm.getCode());
    }

    public static String getCompressionMethodName(int code) {
        String method_name="Unknown("+String.valueOf(code)+")";
        if (code==CompressionMethod.STORE.getCode()) method_name="STORED";
        else if (code==CompressionMethod.COMP_FACTOR1.getCode()) method_name="REDUCE1";
        else if (code==CompressionMethod.COMP_FACTOR2.getCode()) method_name="REDUCE2";
        else if (code==CompressionMethod.COMP_FACTOR3.getCode()) method_name="REDUCE3";
        else if (code==CompressionMethod.COMP_FACTOR4.getCode()) method_name="REDUCE4";
        else if (code==CompressionMethod.DEFLATE.getCode()) method_name="DEFLATED";
        else if (code==CompressionMethod.DEFLATE64.getCode()) method_name="DEFLATE64";
        else if (code==CompressionMethod.AES_INTERNAL_ONLY.getCode()) method_name="AE-x";
        else if (code==CompressionMethod.BZIP2.getCode()) method_name="BZIP2";
        else if (code==CompressionMethod.IBM_CMPSC.getCode()) method_name="IBM_CMPSC";
        else if (code==CompressionMethod.IBM_LZ77.getCode()) method_name="IBM_LZ77";
        else if (code==CompressionMethod.IBM_TERE.getCode()) method_name="IBM_TERSE";
        else if (code==CompressionMethod.JPEG.getCode()) method_name="JPEG";
        else if (code==CompressionMethod.WAVPACK.getCode()) method_name="WavPack";
        else if (code==CompressionMethod.LZMA.getCode()) method_name="LZMA";
        else if (code==CompressionMethod.PKWARE_IMPLODING.getCode()) method_name="IMPLODING";
        else if (code==CompressionMethod.IMPLOD.getCode()) method_name="IMPLODED";
        else if (code==CompressionMethod.PPMD.getCode()) method_name="PPMD";
        else if (code==CompressionMethod.SHRUNK.getCode()) method_name="SHRUNK";
        return method_name;
    }

    static public void writeEmptyZipHeader(SafFile3 of) throws Exception {
        OutputStream os=of.getOutputStream();
        writeEmptyZipHeader(os);
        os.flush();
        os.close();
    }

    static public void writeEmptyZipHeader(OutputStream os) throws IOException {
        byte[] null_header=new byte[]{
                (byte)0x50, (byte)0x4b, (byte)0x05, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, };
        os.write(null_header);
    }

}
