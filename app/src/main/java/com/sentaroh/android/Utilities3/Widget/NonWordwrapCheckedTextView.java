package com.sentaroh.android.Utilities3.Widget;
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
import android.content.res.Resources;
import android.text.InputFilter;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.CheckedTextView;

import com.sentaroh.android.Utilities3.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class NonWordwrapCheckedTextView extends CheckedTextView {
    private static Logger log= LoggerFactory.getLogger(NonWordwrapCheckedTextView.class);

    private CharSequence mOrgText = "";
    private BufferType mOrgBufferType = BufferType.NORMAL;
    private boolean mWordWrapMode =true;
    private int mSplitTextLineCount=0;
    private SpannableStringBuilder mSpannableSplitText=null;

    private boolean mDebugEnabled=false;

    public void setDebugEnabled(boolean debug) {
        mDebugEnabled=debug;
    }

    public NonWordwrapCheckedTextView(Context context) {
        super(context);
        setDefaultWordwrapMode();
        if (mDebugEnabled) log.info("constructor 1");
    }

    private void setDefaultWordwrapMode() {
        if (Locale.getDefault().getLanguage().equals("ja") || Locale.getDefault().getLanguage().equals("zh")) setWordWrapEnabled(false);
    }

    public NonWordwrapCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaultWordwrapMode();
        if (mDebugEnabled) log.info("constructor 2");
    }

    public NonWordwrapCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDefaultWordwrapMode();
        if (mDebugEnabled) log.info("constructor 3");
    }

    public NonWordwrapCheckedTextView(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        setDefaultWordwrapMode();
        if (mDebugEnabled) log.info("constructor 4");
    }

    public void setWordWrapEnabled(boolean word_wrap_mode) {
        mWordWrapMode =word_wrap_mode;
    }

    public boolean isWordWrapEnabled() {
        return mWordWrapMode;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mDebugEnabled) log.info("onLayout changed="+changed+", left="+left+", top="+top+", right="+right+", bottom="+bottom);
        if (!isWordWrapEnabled() && (getMaxLines() > 1 || getMaxLines() < 0)) {
            super.setText(mSpannableSplitText, mOrgBufferType);
            if (mDebugEnabled) log.info("onLayout setText issued");
        }
    }

    @Override
    final protected void onMeasure(int w, int h) {
        if (mDebugEnabled) log.info("onMeasure w="+MeasureSpec.getSize(w)+", h="+MeasureSpec.getSize(h));
        if (!isWordWrapEnabled() && (getMaxLines() > 1 || getMaxLines() < 0)) {
            mSpannableSplitText=buildSplitText(MeasureSpec.getSize(w), MeasureSpec.getSize(h));
            super.setText(mSpannableSplitText, mOrgBufferType);
            super.onMeasure(w, h);
        } else {
            super.onMeasure(w, h);
        }
    }

    @Override
    final protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mDebugEnabled) log.info("onSizeChanged w="+w+", h="+h+", oldw="+oldw+", oldh="+oldh);
    };

    @Override
    public void setText(CharSequence text, BufferType type) {
        mOrgText = text;
        mOrgBufferType = type;
        super.setText(text, type);
        if (mDebugEnabled) log.info("setText length="+text.length()+", type="+type.toString()+", text="+text);
//        requestLayout();
    }

    public SpannableStringBuilder getModifiedText() {
        return mSpannableSplitText;
    }

    public CharSequence getOriginalText() {
        return mOrgText;
    }

    @Override
    public int length() {
        return mOrgText.length();
    }

    public SpannableStringBuilder buildSplitText(int w, int h) {
        TextPaint paint = getPaint();
        int wpl =getCompoundPaddingLeft();
        int wpr =getCompoundPaddingRight();
        int width = w - wpl - wpr;
        if (mDebugEnabled)  log.info("buildSplitText width="+width+", w="+w+", wpl="+wpl+", wpr="+wpr+", h="+h+", length="+mOrgText.length());

        SpannableStringBuilder output = null;
        int add_cr_cnt=0;
        if (width<=0) {
            output=new SpannableStringBuilder(mOrgText);
            mSplitTextLineCount=mOrgText.toString().split("\n").length;
        } else {
            output=new SpannableStringBuilder(mOrgText);
            int start=0;
            if (mDebugEnabled) log.info("input="+output.toString());
            if (output.length()>1) {
                while(start<output.length()) {
                    if (mDebugEnabled) log.info("start="+start);
                    String in_text=output.subSequence(start, output.length()).toString();
                    int cr_pos=in_text.indexOf("\n");
                    if (cr_pos>0) {
                        in_text = output.subSequence(start, start + cr_pos).toString();
                        int nc = paint.breakText(in_text, true, width-1, null);
                        if (output.charAt(start + nc) != '\n') output.insert(start + nc, "\n");
                        start = start + nc + 1;
                    } else if (cr_pos==0) {
                        start = start + 1;
                    } else {
                        int nc=paint.breakText(in_text, true, width-1, null);
//                        log.info("start="+start+", nc="+nc);
//                    log.info("in_text length="+in_text.length()+", text="+in_text);
                        if (nc<=(output.length()-start-1)) {
                            output.insert(start+nc, "\n");
//                        log.info("cr inserted2, pos="+(start + nc)+", output length="+output.length());
                            start=start+nc+1;
                        } else {
                            start=start+nc+1;
                        }
                    }
                }
            }
            mSplitTextLineCount=output.toString().split("\n").length;
        }

        if (mDebugEnabled) {
            log.info("buildSplitText Number of Lines="+mSplitTextLineCount+", added_cr/lf_count="+add_cr_cnt);
            log.info("buildSplitText input  char="+mOrgText.toString());
            log.info("buildSplitText input  hex ="+ StringUtil.getDumpFormatHexString(mOrgText.toString().getBytes(), 0, mOrgText.toString().getBytes().length));
            log.info("buildSplitText output char="+output.toString());
            log.info("buildSplitText output hex ="+ StringUtil.getDumpFormatHexString(output.toString().getBytes(), 0, output.toString().getBytes().length));
        }
        return output;
    }

    final static private float toPixel(Resources res, int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
        return px;
    }


}