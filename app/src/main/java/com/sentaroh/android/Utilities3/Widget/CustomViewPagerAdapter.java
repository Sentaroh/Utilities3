package com.sentaroh.android.Utilities3.Widget;
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
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class CustomViewPagerAdapter extends PagerAdapter {
	 
    @SuppressWarnings("unused")
	private Context mContext;
    private View[] mViewList;
     
    public CustomViewPagerAdapter(Context context, View[] vl) {
        mContext = context;
        mViewList=vl;
    }
 
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // コンテナに追加
        ((ViewPager)container).addView(mViewList[position]);
        return mViewList[position];
    }
 
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // コンテナから View を削除
    	((ViewPager)container).removeView((View) object);
    }
 
    @Override
    public int getCount() {
        // リストのアイテム数を返す
        return mViewList.length;
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (View) object;
    }
 
}