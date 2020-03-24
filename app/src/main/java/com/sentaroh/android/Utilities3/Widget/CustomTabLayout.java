package com.sentaroh.android.Utilities3.Widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

public class CustomTabLayout extends TabLayout {
    public CustomTabLayout(@NonNull Context context) {
        super(context);
    }

    public CustomTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addTab(String tab_name) {
        addTab(this.newTab().setText(tab_name).setTag(tab_name));
    }

    public void setCurrentTabByPosition(int position) {
        getTabAt(position).select();
    }

    public void setCurrentTabByName(String tab_name) {
        for(int i=0;i<getTabCount();i++) {
            String tag=(String)getTabAt(i).getTag();
            if (tag!=null && tag.equals(tab_name)) {
                getTabAt(i).select();
                break;
            }
        }
    }

    public String getSelectedTabName() {
        String tab_name=(String)getTabAt(getSelectedTabPosition()).getTag();
        return tab_name;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        LinearLayout tabStrip = ((LinearLayout)getChildAt(0));
        if (enabled) {
            for(int i = 0; i < tabStrip.getChildCount(); i++) {
                tabStrip.getChildAt(i).setOnTouchListener(null);
            }
        } else {
            for(int i = 0; i < tabStrip.getChildCount(); i++) {
                tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }
        }

    }

}
