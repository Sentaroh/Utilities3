package com.sentaroh.android.Utilities3.ContextMenu;
/*
The MIT License (MIT)
Copyright (c) 2012 Sentaroh

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

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sentaroh.android.Utilities3.R;

import java.util.ArrayList;

public class CustomContextMenuAdapter extends BaseAdapter {

	private ArrayList<CustomContextMenuItem> mItems = new ArrayList<CustomContextMenuItem>();

	private Activity mActivity=null;

	public CustomContextMenuAdapter(Activity a) {
	    mActivity=a;
	}
//	public CustomContextMenuAdapter(Context c) {
//		context = c;
//	}

	public void addMenuItem(CustomContextMenuItem menuItem) {
		mItems.add(menuItem);
	}

	public void setMenuItemList(ArrayList<CustomContextMenuItem> mil) {mItems=mil;};


	@Override
	public int getCount() {
		return mItems.size();
	}

	public void clear() {
		mItems.clear();
		return ;
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CustomContextMenuItem item = (CustomContextMenuItem) getItem(position);
	 	final ViewHolder holder;

        View v = convertView;
        if (v == null) {
//            LayoutInflater vi = (LayoutInflater)mActivity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            v = vi.inflate(R.layout.custom_context_menu_item, null);
        	v = mActivity.getLayoutInflater().inflate(R.layout.custom_context_menu_item, null);
            holder=new ViewHolder();

        	holder.iv_icon=(ImageView)v.findViewById(R.id.custom_context_menu_icon);
           	holder.tv_menu=(TextView)v.findViewById(R.id.custom_context_menu_name);

            v.setTag(holder); 
        } else {
     	   holder= (ViewHolder)v.getTag();
        }
        if (item != null) {
        	holder.iv_icon.setImageDrawable(item.image);
        	holder.tv_menu.setText(item.text);
        	if(item.menu_enabled) {
        		holder.tv_menu.setEnabled(true);
        		holder.tv_menu.setTextColor(Color.BLACK);
        	} else {
        		holder.tv_menu.setEnabled(false);
        		holder.tv_menu.setTextColor(Color.GRAY);
        	}
        }
        return v;

	};
	static class ViewHolder {
		ImageView iv_icon;
		TextView tv_menu;
	}

};