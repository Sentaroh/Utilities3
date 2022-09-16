package com.sentaroh.android.Utilities3.Preference;

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
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import com.sentaroh.android.Utilities3.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListEditPreference extends DialogPreference {
    private static final Logger log = LoggerFactory.getLogger(ListEditPreference.class);
    private static final boolean mDebugEnabled = true;
    private final static String APPLICATION_TAG = "ListEditPreference";
    private final Context mContext;
    private String mHint = "";

    public ListEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (mDebugEnabled) log.debug(APPLICATION_TAG);
        initAttrs(context, attrs);
    }

    public ListEditPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        if (mDebugEnabled) log.debug(APPLICATION_TAG + " style");
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ListEditPreference);
        mHint = a.getString(R.styleable.ListEditPreference_hint);
        a.recycle();
    }

    public String getAddItemHint() {
        return mHint;
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        if (mDebugEnabled) log.debug(APPLICATION_TAG + " onGetDefaultValue");
        return a.getString(index);
    }
}
