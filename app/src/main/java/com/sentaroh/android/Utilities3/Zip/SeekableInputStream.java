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

import android.content.Context;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SeekableInputStream extends InputStream {
    private static Logger log= LoggerFactory.getLogger(SeekableInputStream.class);

//    public SeekableInputStream(File lf) throws FileNotFoundException {
//        super(lf,"r");
//    }





    private  InputStream mInputStrean=null;
    private Context mContext=null;
    private Uri mUri=null;
    private File mFile=null;
    private long mCurrentPosition=0L;
    private long mLength=0L;
//    public SeekableInputStream(Context c, Uri uri) throws IOException {
//        mInputStrean=c.getContentResolver().openInputStream(uri);
//        mLength=mInputStrean.available();
//        mContext=c;
//        mUri=uri;
//    }

    public SeekableInputStream(Context c, Uri uri, long file_length) throws IOException {
        mInputStrean=c.getContentResolver().openInputStream(uri);
        mLength=file_length;
        mContext=c;
        mUri=uri;
    }

    public SeekableInputStream(Context c, File lf) throws IOException {
        mInputStrean=new FileInputStream(lf);
        mLength=lf.length();
        mContext=c;
        mFile=lf;
    }

    public int read() throws IOException {
        int rd=mInputStrean.read();
        if (rd!=-1) mCurrentPosition++;
        return rd;
    }

    public long length() throws IOException {
        return mLength;
    }

    public long getPosition() {
        return mCurrentPosition;
    }

    public long getFilePointer() {
        return mCurrentPosition;
    }

    public void seek(long newPosition) throws IOException {
//        if (log.isTraceEnabled()) log.trace("seek pos="+newPosition+", current="+mCurrentPosition);
        if (newPosition>=0) {
            if (newPosition>mCurrentPosition) {
                mInputStrean.skip((newPosition-mCurrentPosition));
            } else if (newPosition<mCurrentPosition) {
                mInputStrean.close();
                if (mUri!=null) mInputStrean=mContext.getContentResolver().openInputStream(mUri);
                else mInputStrean=new FileInputStream(mFile);
                mInputStrean.skip(newPosition);
            }
            mCurrentPosition=newPosition;
        } else {
            throw new IOException("Negative value specified, value="+newPosition);
//            long new_pos=mCurrentPosition+newPosition;
//            mInputStrean.close();
//            if (mUri!=null) mInputStrean=mContext.getContentResolver().openInputStream(mUri);
//            else mInputStrean=new FileInputStream(mFile);
//            if (new_pos>=0) {
//                mInputStrean.skip(new_pos);
//            } else {
////                throw new IOException("Negative value specified, value="+new_pos);
////                if (log.isTraceEnabled()) log.trace("New position reset by 0."+" Calcurate new pos="+new_pos);
//            }
        }
    }

    public void skipBytes(long skipBytes) throws IOException {
        mInputStrean.skip(skipBytes);
        mCurrentPosition+=skipBytes;
    }

    public int read(byte[] buff) throws IOException {
        return read(buff, 0, buff.length);
    }

    public int read(byte[] buff, int offset, int readLength) throws IOException {
        int rc=mInputStrean.read(buff, offset, readLength);
        if (rc>0) mCurrentPosition+=rc;
        return rc;
    }

    public int readFully(byte[] buff) throws IOException {
        return read(buff, 0, buff.length);
    }
    public int readFully(byte[] buff, int offset, int readLength) throws IOException {
        return read(buff, offset, readLength);
    }

}
