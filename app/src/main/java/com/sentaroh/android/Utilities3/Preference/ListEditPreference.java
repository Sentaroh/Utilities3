package com.sentaroh.android.Utilities3.Preference;

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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sentaroh.android.Utilities3.Dialog.CommonDialog;
import com.sentaroh.android.Utilities3.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;

public class ListEditPreference extends DialogPreference {
    private static Logger log= LoggerFactory.getLogger(ListEditPreference.class);
    private static boolean mDebugEnabled=false;
    private final static String APPLICATION_TAG="ColorPickerPreference";
    private Context mContext=null;

    public ListEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        if (mDebugEnabled) log.debug("ColorPickerPreference");
    }

    public ListEditPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext=context;
        if (mDebugEnabled) log.debug("ColorPickerPreference style");
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        if (mDebugEnabled) log.debug("onGetDefaultValue");
        return a.getString(index);
    }

    @Override
    protected void onBindDialogView(View view) {
        if (mDebugEnabled) log.debug("onBindDialogView");
        super.onBindDialogView(view);
    }

    @Override
    public void onActivityDestroy() {
        if (mDebugEnabled) log.debug("onActivityDestroy");
        super.onActivityDestroy();
    };

    @Override
    protected Parcelable onSaveInstanceState() {
        if (mDebugEnabled) log.debug("onSaveInstanceState");
        final Parcelable superState = super.onSaveInstanceState();
        final ListEditPreference.MySavedState myState = new ListEditPreference.MySavedState(superState);
        myState.list_item = mCurrentListData;
        return myState;
    };

    private static class MySavedState extends BaseSavedState {
        public String list_item;
        @SuppressWarnings("unchecked")
        public MySavedState(Parcel source) {
            super(source);
            list_item =source.readString();
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(list_item);
        }
        public MySavedState(Parcelable superState) {
            super(superState);
        }
        @SuppressWarnings("unused")
        public static final Creator<MySavedState> CREATOR =
                new Creator<MySavedState>() {
                    public ListEditPreference.MySavedState createFromParcel(Parcel in) {
                        return new ListEditPreference.MySavedState(in);
                    }
                    public ListEditPreference.MySavedState[] newArray(int size) {
                        return new ListEditPreference.MySavedState[size];
                    }
                };
    };

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (mDebugEnabled) log.debug("onRestoreInstanceState state="+state);
        if (state == null) {
            super.onRestoreInstanceState(state);
            return;
        }
        ListEditPreference.MySavedState myState = (ListEditPreference.MySavedState) state;

        super.onRestoreInstanceState(myState.getSuperState());
    };

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (mDebugEnabled) log.debug("onSetInitialValue");
        if (restorePersistedValue) {
            mCurrentListData = getPersistedString(mCurrentListData);
        } else {
            mCurrentListData = "text/*;test/*;";//(String) defaultValue;
            persistString(mCurrentListData);
        }
    };


    @Override
    protected View onCreateDialogView() {
        if (mDebugEnabled) log.debug("onCreateDialogView");
        mListEditView =initViewWidget();
        return mListEditView;
    };

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (mDebugEnabled) log.debug("onDialogClosed positiveResult="+positiveResult);
        if (positiveResult) {
            persistString(buildSaveValue());
        }
        super.onDialogClosed(positiveResult);
    };

    @Override
    protected void showDialog(Bundle state) {
        if (mDebugEnabled) log.debug("showDialog");
        super.showDialog(state);
        CommonDialog.setDlgBoxSizeLimit(getDialog(), true);
    };

    private View mListEditView =null;
    private String mCurrentListData ="";
    private ArrayList<ListValueItem> mValueList =new ArrayList<ListValueItem>();

    private View initViewWidget() {
        if (mDebugEnabled) log.debug("initViewWidget");
        final Context context=getContext();

        mCurrentListData = getPersistedString(mCurrentListData);

        mValueList.clear();
        String[] list_array= mCurrentListData.split(";");
        for(String item:list_array) {
            if (item.length()>0) {
                ListValueItem mi=new ListValueItem(item);
                mValueList.add(mi);
            }
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mListEditView = inflater.inflate(R.layout.list_edit_preference, null);

        final ListView lv=(ListView)mListEditView.findViewById(R.id.list_edit_ppreference_list_view);

        final AdapterListEditor adapter=new AdapterListEditor(mContext, R.layout.list_edit_preference_entry_item, mValueList);

        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!mValueList.get(i).isDeleted()) editListValue(mValueList.get(i));
            }
        });

        final Button add_btn=(Button) mListEditView.findViewById(R.id.list_edit_preference_add_btn);
        add_btn.setEnabled(false);
        final EditText et_list_value=(EditText) mListEditView.findViewById(R.id.list_edit_preference_add_mime_type);

        et_list_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length()>0) {
                    for(ListValueItem item: mValueList) {
                        if (item.getListValue().equals(editable.toString())) add_btn.setEnabled(false);
                        else add_btn.setEnabled(true);
                    }
                } else {
                    add_btn.setEnabled(false);
                }
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListValueItem mi=new ListValueItem(et_list_value.getText().toString());
                mValueList.add(mi);
                adapter.sort();
                et_list_value.setText("");
            }
        });


        return mListEditView;
    };

    private String buildSaveValue() {
        String list_value="";
//        log.info("size="+mValueList.size());
        for(ListValueItem item: mValueList) {
            if (!item.isDeleted()) list_value+=item.getListValue()+";";
        }
        return list_value;
    }

    private void editListValue(final ListValueItem list_item) {
        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.list_edit_preference_item_edit);


        final TextView title = (TextView) dialog.findViewById(R.id.list_edit_preference_item_edit_title);

        final TextView message = (TextView) dialog.findViewById(R.id.list_edit_preference_item_edit_msg);

        final EditText et_data = (EditText) dialog.findViewById(R.id.list_edit_preference_item_edit_new_value);
        et_data.setText(list_item.getListValue());

        final Button btn_ok = (Button) dialog.findViewById(R.id.list_edit_preference_item_edit_ok_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.list_edit_preference_item_edit_cancel_btn);

        btn_ok.setEnabled(false);
        btn_ok.setAlpha(0.3f);
        message.setText(mContext.getString(R.string.msgs_list_edit_preference_list_edit_list_specify_new_value));
        et_data.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length()>0) {
                    for(ListValueItem item: mValueList) {
                        if (item.getListValue().equals(editable.toString())) {
                            btn_ok.setEnabled(false);
                            btn_ok.setAlpha(0.3f);
                            message.setText(mContext.getString(R.string.msgs_list_edit_preference_list_edit_list_value_was_already_registerd));
                            break;
                        } else {
                            message.setText("");
                            btn_ok.setEnabled(true);
                            btn_ok.setAlpha(1.0f);
                        }
                    }
                } else {
                    message.setText(mContext.getString(R.string.msgs_list_edit_preference_list_edit_list_specify_list_value));
                    btn_ok.setEnabled(false);
                    btn_ok.setAlpha(0.3f);
                }
            }
        });


        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });
        // OKボタンの指定
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                list_item.setListValue(et_data.getText().toString());
                dialog.dismiss();
            }
        });
        dialog.show();

    }



    public class AdapterListEditor extends ArrayAdapter<ListValueItem> {
        private Context c;
        private int id;
        private ArrayList<ListValueItem> items;

        public AdapterListEditor(Context context, int textViewResourceId, ArrayList<ListValueItem> objects) {
            super(context, textViewResourceId, objects);
            c = context;
            id = textViewResourceId;
            items = objects;
        }

        public ListValueItem getItem(int i) {
            return items.get(i);
        }

        public void remove(int i) {
            items.remove(i);
            notifyDataSetChanged();
        }

        public void replace(ListValueItem fli, int i) {
            items.set(i, fli);
            notifyDataSetChanged();
        }

        public void sort() {
            this.sort(new Comparator<ListValueItem>() {
                @Override
                public int compare(ListValueItem lhs,
                                   ListValueItem rhs) {
                    return lhs.getListValue().compareToIgnoreCase(rhs.getListValue());
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(id, null);
                holder = new ViewHolder();
//            holder.ll_entry=(LinearLayout) v.findViewById(R.id.filter_list_item_entry);
                holder.btn_row_delbtn = (Button) v.findViewById(R.id.list_edit_preference_entry_item_delete_btn);
                holder.tv_row_filter = (TextView) v.findViewById(R.id.list_edit_preference_entry_item_mime_type);

                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            final ListValueItem o = getItem(position);

            if (o != null) {
                holder.tv_row_filter.setText(o.getListValue());
                holder.tv_row_filter.setVisibility(View.VISIBLE);
                holder.btn_row_delbtn.setVisibility(View.VISIBLE);

                holder.tv_row_filter.setEnabled(true);
                holder.btn_row_delbtn.setEnabled(true);

                if (o.isDeleted()) {
                    holder.tv_row_filter.setEnabled(false);
                    holder.tv_row_filter.setAlpha(0.3f);
                    holder.btn_row_delbtn.setEnabled(false);
                    holder.btn_row_delbtn.setAlpha(0.3f);
                    holder.btn_row_delbtn.setText(mContext.getString(R.string.msgs_list_edit_preference_list_was_deleted));
                } else {
                    holder.btn_row_delbtn.setText(mContext.getString(R.string.msgs_list_edit_preference_delete));
                }

                final int p = position;
                holder.btn_row_delbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.tv_row_filter.setEnabled(false);
                        holder.btn_row_delbtn.setEnabled(false);

                        o.setDeleted(true);
                        notifyDataSetChanged();

//                        if (mNotifyDeleteListener != null)
//                            mNotifyDeleteListener.notifyToListener(true, new Object[]{o});
                    }

                });
            }

            return v;
        }

        private class ViewHolder {
            TextView tv_row_filter;
            Button btn_row_delbtn;
        }
    }

    private class ListValueItem implements Comparable<ListValueItem> {

        private String mListValue ="";
        private boolean mDelete=false;

        public ListValueItem(String filter) {
            this.mListValue = filter;
        }

        public String getListValue() {
            return this.mListValue;
        }

        public void setListValue(String value) {
            this.mListValue = value;
        }

        public void setDeleted(boolean deleted) {
            mDelete=deleted;
        }

        public boolean isDeleted() {
            return mDelete;
        }

        @Override
        public int compareTo(ListValueItem o) {
            if (this.mListValue != null)
                return this.mListValue.toLowerCase().compareTo(o.getListValue().toLowerCase());
//				return this.filename.toLowerCase().compareTo(o.getName().toLowerCase()) * (-1);
            else
                throw new IllegalArgumentException();
        }
    }

}
