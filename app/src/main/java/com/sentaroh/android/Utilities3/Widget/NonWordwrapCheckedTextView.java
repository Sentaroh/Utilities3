package com.sentaroh.android.Utilities3.Widget;

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

public class NonWordwrapCheckedTextView extends CheckedTextView {
    private static Logger log= LoggerFactory.getLogger(NonWordwrapCheckedTextView.class);

    private CharSequence mOrgText = "";
    private BufferType mOrgBufferType = BufferType.NORMAL;
    private boolean mWordWrapMode =false;
    private int mSplitTextLineCount=0;
    private SpannableStringBuilder mSpannableSplitText=null;

    private boolean mDebugEnabled=false;

    public NonWordwrapCheckedTextView(Context context) {
        super(context);
        setWrapFilter();
    }

    public NonWordwrapCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWrapFilter();
    }

    public NonWordwrapCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWrapFilter();
    }

    private void setWrapFilter() {
        if (isWordWrapByFilter()) {
            if (isWordWrapEnabled()) setFilters(new InputFilter[] {});
            else setFilters(new InputFilter[] { new CheckedTextViewFilter(this) });
        }
    }

    private boolean mWordwrapByFilter =false;
    public void setWordWrapByFilter(boolean wordwrap_by_filter) {
        mWordwrapByFilter =wordwrap_by_filter;
        setWrapFilter();
    }

    public boolean isWordWrapByFilter() {
        return mWordwrapByFilter;
    }

    public void setWordWrapEnabled(boolean word_wrap_mode) {
        mWordWrapMode =word_wrap_mode;
        setWrapFilter();
    }

    public boolean isWordWrapEnabled() {
        return mWordWrapMode;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        log.info("onLayout changed="+changed+", left="+left+", top="+top+", right="+right+", bottom="+bottom);
        if (isWordWrapByFilter()) {
            setText(mOrgText, mOrgBufferType);
        } else {
            if (!isWordWrapEnabled()) {
                super.setText(mSpannableSplitText, BufferType.SPANNABLE);//mOrgBufferType);
            }
        }
    }

    @Override
    final protected void onMeasure(int w, int h) {
        if (mDebugEnabled) log.info("onMeasure w="+MeasureSpec.getSize(w)+", h="+MeasureSpec.getSize(h));
//        TextPaint paint = getPaint();
        if (isWordWrapByFilter()) {
            super.onMeasure(w, h);
        } else {
            if (!isWordWrapEnabled()) {
                mSpannableSplitText=buildSplitText(MeasureSpec.getSize(w), MeasureSpec.getSize(h));
//                float sep_line1=0f;//toPixel(getResources(), 3);
//                int sep_line2=(int)toPixel(getResources(), 3);
//                TextPaint.FontMetrics fm=paint.getFontMetrics();
////            float ts_height=getTextSize();//Math.abs(fm.top)+Math.abs(fm.bottom);
//                float ts_height=Math.abs(fm.ascent)+Math.abs(fm.descent);
//                int new_h=((int)Math.ceil(ts_height+sep_line1))*mSplitTextLineCount+sep_line2;
//                if (mDebugEnabled) {
//                    log.info("onMeasure lineHeight="+ts_height+
//                            ", ascent="+fm.ascent+", bottom="+fm.bottom+", decent="+fm.descent+", leading="+fm.leading+", top="+fm.top);
//                    log.info("onMeasure textSize="+getTextSize()+", paint text size="+paint.getTextSize()+", no of lines="+mSplitTextLineCount+
//                            ", LineSpacing="+getLineSpacingExtra()+", LineSpcingMult="+getLineSpacingMultiplier());
//                    log.info("onMeasure w="+MeasureSpec.getSize(w)+", h="+MeasureSpec.getSize(h)+
//                            ", new w="+MeasureSpec.getSize(w)+", new h="+MeasureSpec.getSize(new_h));
//                }
//                setMeasuredDimension( MeasureSpec.getSize(w), MeasureSpec.getSize(new_h));
                super.setText(mSpannableSplitText, mOrgBufferType);
                super.onMeasure(w, h);
            } else {
                super.onMeasure(w, h);
            }
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
        requestLayout();
    }

    public SpannableStringBuilder getModifiedText() {
        return mSpannableSplitText;
    }

    @Override
    public CharSequence getText() {
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
//                    log.info("start="+start+", nc="+nc);
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

    private static class CheckedTextViewFilter implements InputFilter {
        private static Logger log= LoggerFactory.getLogger(CheckedTextViewFilter.class);

        private final NonWordwrapCheckedTextView view;

        public CheckedTextViewFilter(NonWordwrapCheckedTextView view) {
            this.view = view;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            TextPaint paint = view.getPaint();
            int w = view.getWidth();
            int wpl = view.getCompoundPaddingLeft();
            int wpr = view.getCompoundPaddingRight();
            int width = w - wpl - wpr;
//        log.info("source="+source);
//        log.info("start="+start+", end="+end+", width="+width+", w="+w+", wpl="+wpl+", wpr="+wpr);

            if (width<=0) return source;//Modified by F.Hoshino 2018/08/29
            SpannableStringBuilder result = new SpannableStringBuilder();
            for (int index = start; index < end; index++) {
                float rts= Layout.getDesiredWidth(source, start, index + 1, paint);
                if (rts > width) {
                    result.append(source.subSequence(start, index));
                    result.append("\n");
                    start = index;
//                Log.v("CustomTextView","Append cr/lf, result="+result);
                } else if (source.charAt(index) == '\n') {
                    result.append(source.subSequence(start, index));
                    start = index;
                }
//            log.info("start="+start+", end="+end+", index="+index+", rts="+rts);
            }
            if (start < end) {
                result.append(source.subSequence(start, end));
            }
//        log.info("result char="+result.toString());
//        log.info("source hex ="+ StringUtil.getDumpFormatHexString(source.toString().getBytes(), 0,source.toString().getBytes().length));
//        log.info("result hex ="+ StringUtil.getDumpFormatHexString(result.toString().getBytes(), 0,result.toString().getBytes().length));
            return result;
        }
    }

}