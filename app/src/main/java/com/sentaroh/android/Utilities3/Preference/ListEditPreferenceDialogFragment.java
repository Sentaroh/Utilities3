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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

import com.sentaroh.android.Utilities3.Dialog.CommonDialog;
import com.sentaroh.android.Utilities3.R;
import com.sentaroh.android.Utilities3.ThemeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.MissingResourceException;

public class ListEditPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    private static final boolean mDebugEnabled = true;
    private static final Logger log = LoggerFactory.getLogger(ListEditPreferenceDialogFragment.class);
    private final static String APPLICATION_TAG = "ListEditPreferenceDialogFragment";
    private Context mContext = null;
    private String mHint = "";
    private final String DIALOG_TITLE_DEFAULT_VALUE = "";

    public static ListEditPreferenceDialogFragment newInstance(String key) {
        final ListEditPreferenceDialogFragment fragment = new ListEditPreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    public static ListEditPreferenceDialogFragment newInstance(String key, String title) {
        final ListEditPreferenceDialogFragment fragment = new ListEditPreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        b.putString("AlertDialogTitle", title);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mDebugEnabled) log.debug(APPLICATION_TAG+" onCreate");

        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        if (mContext == null) mContext = getContext();
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        if (mDebugEnabled) log.debug(APPLICATION_TAG+" onBindDialogView");
        super.onBindDialogView(view);
    }

    private static final String STATE_ADAPTER_LIST = "STATE_ADAPTER_LIST";
    private static final String STATE_ADAPTER_LIST_CLONE = "STATE_ADAPTER_LIST_CLONE";
    private static final String STATE_OK_BUTTON_ENABLED = "STATE_OK_BUTTON_ENABLED";
    private static final String STATE_EDIT_DIALOG_VALUE = "STATE_EDIT_DIALOG_VALUE";
    private static final String STATE_EDIT_DIALOG_ITEM_POSITION = "STATE_EDIT_DIALOG_ITEM_POSITION";
    private static final String STATE_EDIT_DIALOG_OK_BUTTON_ENABLED = "STATE_EDIT_DIALOG_OK_BUTTON_ENABLED";

    @Override
    public @NonNull
    Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (mDebugEnabled) log.debug(APPLICATION_TAG+" onCreateDialog");
//        CommonDialog.setDlgBoxSizeLimit(getDialog(), true);

        View listEditView;
        if (savedInstanceState == null) {
                listEditView = initViewWidget();
        } else {
            if (Build.VERSION.SDK_INT >= 33) {
                // Android T
                mValueList.value_item_list_array = savedInstanceState.getParcelableArrayList(STATE_ADAPTER_LIST, ValueItem.class);
                mOriginalValueList.value_item_list_array = savedInstanceState.getParcelableArrayList(STATE_ADAPTER_LIST_CLONE, ValueItem.class);
                mDialogOkButtonEnabled = savedInstanceState.getBoolean(STATE_OK_BUTTON_ENABLED);
            } else {
                mValueList.value_item_list_array = savedInstanceState.getParcelableArrayList(STATE_ADAPTER_LIST);
                mOriginalValueList.value_item_list_array = savedInstanceState.getParcelableArrayList(STATE_ADAPTER_LIST_CLONE);
                mDialogOkButtonEnabled = savedInstanceState.getBoolean(STATE_OK_BUTTON_ENABLED);
            }

            listEditView = reInitViewWidget();
        }

        String dialog_title = DIALOG_TITLE_DEFAULT_VALUE;
        Bundle currentFragmentBundle = this.getArguments();
        if (currentFragmentBundle != null) {
            dialog_title = currentFragmentBundle.getString("AlertDialogTitle", DIALOG_TITLE_DEFAULT_VALUE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(dialog_title);
        builder.setView(listEditView);

        builder.setPositiveButton(R.string.msgs_common_dialog_save,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDebugEnabled) log.debug(APPLICATION_TAG+" PositiveButton Click");
                DialogPreference preference = getPreference();
                if (preference instanceof ListEditPreference) {
                    savePreferences(preference.getKey(), buildSaveValue());
                }
            }
        });

        builder.setNegativeButton(R.string.msgs_common_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDebugEnabled) log.debug(APPLICATION_TAG+" NegativeButton Click");
                dialog.dismiss();
            }
        });

        AlertDialog ad = builder.create();

        final Bundle savedInstanceStateClone = savedInstanceState;
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mDialogOkButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                mDialogOkButton.setText(R.string.msgs_common_dialog_save);

                mDialogCancelButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                mDialogCancelButton.setText(R.string.msgs_common_dialog_cancel);

                setViewEnabled(mContext, mDialogOkButton, mDialogOkButtonEnabled);

                if (savedInstanceStateClone != null) {
                    try {
                        String edit_dialog_text = savedInstanceStateClone.getString(STATE_EDIT_DIALOG_VALUE);
                        int edit_dialog_item_position = savedInstanceStateClone.getInt(STATE_EDIT_DIALOG_ITEM_POSITION);
                        boolean edit_dialog_ok_button_enabled = savedInstanceStateClone.getBoolean(STATE_EDIT_DIALOG_OK_BUTTON_ENABLED);

                        if (mDebugEnabled) log.debug(APPLICATION_TAG + " Restore editListValue: edit_dialog_text=" + edit_dialog_text + " ,edit_dialog_item_position=" + edit_dialog_item_position);

                        if (edit_dialog_text != null) {
                            mEditItemPosition = edit_dialog_item_position;
                            editListValue(mValueList.value_item_list_array.get(edit_dialog_item_position), edit_dialog_ok_button_enabled, new SpannableStringBuilder(edit_dialog_text));
                        }
                    } catch (MissingResourceException e) {
                        // On configuration changed while no editListValue dialog is shown
                    }
                }
            }
        });

        return ad;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mDebugEnabled) log.debug(APPLICATION_TAG + " onSaveInstanceState");

        outState.putParcelableArrayList(STATE_ADAPTER_LIST, mValueList.value_item_list_array);
        outState.putParcelableArrayList(STATE_ADAPTER_LIST_CLONE, mOriginalValueList.value_item_list_array);
        outState.putBoolean(STATE_OK_BUTTON_ENABLED, mDialogOkButton.isEnabled());

        if (mEditItemDialog != null) {
            final Button btn_ok = mEditItemDialog.findViewById(R.id.list_edit_preference_item_edit_ok_btn);
            final EditText et_data = mEditItemDialog.findViewById(R.id.list_edit_preference_item_edit_new_value);
            outState.putString(STATE_EDIT_DIALOG_VALUE, et_data.getText().toString());
            outState.putInt(STATE_EDIT_DIALOG_ITEM_POSITION, mEditItemPosition);
            outState.putBoolean(STATE_EDIT_DIALOG_OK_BUTTON_ENABLED, btn_ok.isEnabled());

            // Dismiss the edit list item dialog on screen rotation to avoid memory leak when it is recreated after AlertDialog
            if (mEditItemDialog.isShowing()) mEditItemDialog.dismiss();
            else log.debug(APPLICATION_TAG + " ERROR: onSaveInstanceState mEditItemDialog could not be dismissed !");
        }

        super.onSaveInstanceState(outState);
    }

    private void savePreferences(String key, String value) {
        SharedPreferences myPreferences;
        //myPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        myPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString(key, value);
        myEditor.apply();
    }

    private String restorePreferences(String key) {
        SharedPreferences myPreferences;
        //myPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        myPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (myPreferences.contains(key))
            return myPreferences.getString(key, "");
        else return "";
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (mDebugEnabled) log.debug(APPLICATION_TAG+" onDialogClosed positiveResult="+positiveResult);
        //if (positiveResult) {
        //    savePreferences("settings_no_compress_file_type", buildSaveValue());
        //}
    }

    private Button mDialogOkButton = null;
    private boolean mDialogOkButtonEnabled = false;
    private Button mDialogCancelButton = null;

    private String mCurrentListData = "";
    private ValueItemList mValueList = new ValueItemList();
    private ValueItemList mOriginalValueList = new ValueItemList();
    private int mEditItemPosition = 0;
    private AdapterListEditor mListadapter = null;

    private View initViewWidget() {
        if (mDebugEnabled) log.debug(APPLICATION_TAG + " initViewWidget");

        DialogPreference preference = getPreference();
        if (preference instanceof ListEditPreference) {
            mCurrentListData = restorePreferences(preference.getKey());
            mHint = ((ListEditPreference) preference).getAddItemHint();
        }

        mValueList.value_item_list_array.clear();
        String[] list_array = mCurrentListData.split(";");
        for(String item : list_array) {
            if (item.length() > 0) {
                ValueItem mi = new ValueItem(item);
                mValueList.value_item_list_array.add(mi);
            }
        }
        Collections.sort(mValueList.value_item_list_array, new CustomComparator());

        //mOriginalValueList = mValueList.clone();
        mOriginalValueList = mValueList.cloneSerial();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate without parent because it is for AlertDialog !
        @SuppressLint("InflateParams") View listEditView = inflater.inflate(R.layout.list_edit_preference, null);

        final ListView lv = listEditView.findViewById(R.id.list_edit_preference_list_view);
        mListadapter = new AdapterListEditor(mContext, R.layout.list_edit_preference_entry_item, mValueList.value_item_list_array);
        lv.setAdapter(mListadapter);
        //mListadapter.sort(); //already sorted above by Collections

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!mValueList.value_item_list_array.get(i).isDeleted()) {
                    mEditItemPosition = i;
                    editListValue(mValueList.value_item_list_array.get(i), false, null);
                }
            }
        });

        final Button add_btn = listEditView.findViewById(R.id.list_edit_preference_add_btn);
        setViewEnabled(mContext, add_btn, false);
        final EditText et_list_value = listEditView.findViewById(R.id.list_edit_preference_add_item);
        et_list_value.setHint(mHint);

        et_list_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    for(ValueItem item : mValueList.value_item_list_array) {
                        setViewEnabled(mContext, add_btn, !item.getListValue().equals(editable.toString()));
                    }
                } else {
                    setViewEnabled(mContext, add_btn, false);
                }
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValueItem mi = new ValueItem(et_list_value.getText().toString());
                mValueList.value_item_list_array.add(mi);
                //mListadapter.sort();
                Collections.sort(mValueList.value_item_list_array, new CustomComparator());
                mListadapter.notifyDataSetChanged();
                et_list_value.setText("");
                setViewEnabled(mContext, mDialogOkButton, !mValueList.isSame(mOriginalValueList));
            }
        });


        return listEditView;
    }

    private View reInitViewWidget() {
        if (mDebugEnabled) log.debug(APPLICATION_TAG+" reInitViewWidget");

        DialogPreference preference = getPreference();
        if (preference instanceof ListEditPreference) {
            mCurrentListData = restorePreferences(preference.getKey());
            mHint = ((ListEditPreference) preference).getAddItemHint();
        }

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate without parent because it is for AlertDialog !
        @SuppressLint("InflateParams") View listEditView = inflater.inflate(R.layout.list_edit_preference, null);

        final ListView lv = listEditView.findViewById(R.id.list_edit_preference_list_view);
        mListadapter = new AdapterListEditor(mContext, R.layout.list_edit_preference_entry_item, mValueList.value_item_list_array);
        lv.setAdapter(mListadapter);
        //mListadapter.sort(); //No need as already sorted before configuration changed

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!mValueList.value_item_list_array.get(i).isDeleted()) {
                    mEditItemPosition = i;
                    editListValue(mValueList.value_item_list_array.get(i), false, null);
                }
            }
        });

        final Button add_btn = listEditView.findViewById(R.id.list_edit_preference_add_btn);
        setViewEnabled(mContext, add_btn, false);
        final EditText et_list_value = listEditView.findViewById(R.id.list_edit_preference_add_item);
        et_list_value.setHint(mHint);

        et_list_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    for(ValueItem item : mValueList.value_item_list_array) {
                        setViewEnabled(mContext, add_btn, !item.getListValue().equals(editable.toString()));
                    }
                } else {
                    setViewEnabled(mContext, add_btn, false);
                }
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValueItem mi = new ValueItem(et_list_value.getText().toString());
                mValueList.value_item_list_array.add(mi);
                //mListadapter.sort();
                Collections.sort(mValueList.value_item_list_array, new CustomComparator());
                mListadapter.notifyDataSetChanged();
                et_list_value.setText("");
                setViewEnabled(mContext, mDialogOkButton, !mValueList.isSame(mOriginalValueList));
            }
        });


        return listEditView;
    }

    private String buildSaveValue() {
        StringBuilder list_value = new StringBuilder();
//        log.info("size=" + mValueList.value_item_list_array.size());
        for(ValueItem item : mValueList.value_item_list_array) {
            if (!item.isDeleted()) list_value.append(item.getListValue()).append(";");
        }
        return list_value.toString();
    }

    // editListValue(): display the dialog to edit current list item
    private Dialog mEditItemDialog = null;
    private void editListValue(final ValueItem list_item, boolean ok_button_enabled, Editable init_value) {
        if (mDebugEnabled) log.debug(APPLICATION_TAG + " editListValue value=" + list_item.getListValue());
        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.list_edit_preference_item_edit);
        mEditItemDialog = dialog;

        final TextView title = dialog.findViewById(R.id.list_edit_preference_item_edit_title);
        title.setText(mContext.getString(R.string.msgs_list_edit_preference_item_edit_file_type_dialog_title));

        final TextView message = dialog.findViewById(R.id.list_edit_preference_item_edit_msg);

        final EditText et_data = dialog.findViewById(R.id.list_edit_preference_item_edit_new_value);
        if (init_value == null) et_data.setText(list_item.getListValue());
        else et_data.setText(init_value);

        final Button btn_ok = dialog.findViewById(R.id.list_edit_preference_item_edit_ok_btn);
        final Button btn_cancel = dialog.findViewById(R.id.list_edit_preference_item_edit_cancel_btn);

        if (!ok_button_enabled) {
            btn_ok.setEnabled(false);
            btn_ok.setAlpha(0.3f);
        }

        message.setText(mContext.getString(R.string.msgs_list_edit_preference_list_edit_list_specify_new_value));
        et_data.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    for(ValueItem item : mValueList.value_item_list_array) {
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
                mEditItemDialog = null;
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
                //mListadapter.sort();
                Collections.sort(mValueList.value_item_list_array, new CustomComparator());
                mListadapter.notifyDataSetChanged();
                dialog.dismiss();
                mEditItemDialog = null;
                setViewEnabled(mContext, mDialogOkButton, !mValueList.isSame(mOriginalValueList));
            }
        });

        dialog.show();
    }

    public class AdapterListEditor extends ArrayAdapter<ValueItem> {
        private final Context c;
        private final int id;
        private final ArrayList<ValueItem> items;

        public AdapterListEditor(Context context, int textViewResourceId, ArrayList<ValueItem> objects) {
            super(context, textViewResourceId, objects);
            c = context;
            id = textViewResourceId;
            items = objects;
        }

        public ValueItem getItem(int i) {
            return items.get(i);
        }

        public void remove(int i) {
            items.remove(i);
            notifyDataSetChanged();
        }

        public void replace(ValueItem fli, int i) {
            items.set(i, fli);
            notifyDataSetChanged();
        }

        public void sort() {
            this.sort(new Comparator<ValueItem>() {
                @Override
                public int compare(ValueItem lhs,
                                   ValueItem rhs) {
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
                holder.btn_row_delbtn = (ImageButton) v.findViewById(R.id.list_edit_preference_entry_item_delete_btn);
                holder.tv_row_filter = (TextView) v.findViewById(R.id.list_edit_preference_entry_item_mime_type);

                if (ThemeUtil.isLightThemeUsed(mContext)) holder.btn_row_delbtn.setBackgroundColor(Color.WHITE);

                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            final ValueItem o = getItem(position);
            if (o != null) {
                holder.tv_row_filter.setText(o.getListValue());
                holder.tv_row_filter.setVisibility(View.VISIBLE);
                holder.btn_row_delbtn.setVisibility(View.VISIBLE);

                setViewEnabled(mContext, holder.tv_row_filter, !o.isDeleted());//will set alpha
                if (o.isDeleted()) {
                    //holder.tv_row_filter.setEnabled(false);
                    //holder.tv_row_filter.setAlpha(0.3f);
                    //setViewEnabled(mContext, holder.tv_row_filter, false);//will set alpha

                    holder.btn_row_delbtn.setImageResource(R.drawable.context_button_trash_undo);
                    //holder.btn_row_delbtn.setEnabled(false);
                    //holder.btn_row_delbtn.setAlpha(0.3f);
                    //setViewEnabled(mContext, holder.btn_row_delbtn, false);
                } else {
                    //holder.tv_row_filter.setEnabled(true);
                    //holder.tv_row_filter.setAlpha(1.0f);
                    //setViewEnabled(mContext, holder.tv_row_filter, true);//will set alpha

                    holder.btn_row_delbtn.setImageResource(R.drawable.context_button_trash);
                    //holder.btn_row_delbtn.setEnabled(true);
                    //holder.btn_row_delbtn.setAlpha(1.0f);
                    //setViewEnabled(mContext, holder.btn_row_delbtn, true);
                }

                holder.btn_row_delbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        o.setDeleted(!o.isDeleted());
                        setViewEnabled(mContext, holder.tv_row_filter, !o.isDeleted());//will set alpha
                        //holder.tv_row_filter.setEnabled(!o.isDeleted());
                        //holder.btn_row_delbtn.setEnabled(!o.isDeleted());

                        if (o.isDeleted()) {
                            holder.btn_row_delbtn.setImageResource(R.drawable.context_button_trash_undo);
                        } else {
                            holder.btn_row_delbtn.setImageResource(R.drawable.context_button_trash);
                        }

                        notifyDataSetChanged();

                        //setViewEnabled(c, mDialogOkButton, true);
                        setViewEnabled(c, mDialogOkButton, !mValueList.isSame(mOriginalValueList));
//                        if (mNotifyDeleteListener != null)
//                            mNotifyDeleteListener.notifyToListener(true, new Object[]{o});
                    }

                });
            }

            return v;
        }

        private class ViewHolder {
            TextView tv_row_filter;
            ImageButton btn_row_delbtn;
        }
    }

    // Parcelable custom ArrayList so that it can be passed as Bundle argument
    // Needs to implement Serializable since we clone "parent class" ValueItemList using serialization
    private static class ValueItem implements Parcelable , Serializable, Cloneable {
        private String mListValue;
        private boolean mDelete = false;

        public ValueItem(String filter) {
            this.mListValue = filter;
        }

        public String getListValue() {
            return this.mListValue;
        }

        public void setListValue(String value) {
            this.mListValue = value;
        }

        public void setDeleted(boolean deleted) {
            mDelete = deleted;
        }

        public boolean isDeleted() {
            return mDelete;
        }

        public int describeContents() {
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        private ValueItem(Parcel in) {
            mListValue = in.readString();
            mDelete = in.readBoolean();
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(mListValue);
            out.writeBoolean(mDelete);
        }

        // Not being static will cause a crash when restoring after it was sent to background with kill activities enabled in system
        public static final Parcelable.Creator<ValueItem> CREATOR = new Parcelable.Creator<ValueItem>() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            public ValueItem createFromParcel(Parcel in) {
                return new ValueItem(in);
            }

            public ValueItem[] newArray(int size) {
                return new ValueItem[size];
            }
        };

        @NonNull
        @Override
        public ValueItem clone() {
            ValueItem ValueItemClone = null;
            try {
                ValueItemClone = (ValueItem) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert ValueItemClone != null;
            return ValueItemClone;
        }
    }

    // Serializable class to clone an ArrayList<ValueItem> using Serialization
    // Clonable to use custom super.clone()
    // Optional: implement Parcelable to pass teh whole class as bundle arg instead of only value_item_list_array
    private static class ValueItemList implements Serializable, Cloneable {
        public ArrayList<ValueItem> value_item_list_array = new ArrayList<ValueItem>();

        public ValueItemList(){}

        // Clone using java Cloneable clone()
        // not used
        @NonNull
        @Override
        public ValueItemList clone() {
            ValueItemList ValueItemListClone = null;
            try {
                ValueItemListClone = (ValueItemList) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            // override super.clone() implementation to make a deep copy of value_item_list_array
            assert ValueItemListClone != null;
            if (this.value_item_list_array != null) {
                ValueItemListClone.value_item_list_array = new ArrayList<ValueItem>();

                for (ValueItem valueItem : this.value_item_list_array) {
                    //Add the object clones
                    ValueItemListClone.value_item_list_array.add((ValueItem) valueItem.clone());
                }
            } else {
                ValueItemListClone.value_item_list_array = null;
            }

            //assert ValueItemListClone != null;
            return ValueItemListClone;
        }

        // Custom clone using Serialization/Deserialization
        public ValueItemList cloneSerial() {
            ValueItemList ValueItemListClone = null;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);

                oos.flush();
                oos.close();

                baos.flush();
                byte[] ba_buff = baos.toByteArray();
                baos.close();

                ByteArrayInputStream bais = new ByteArrayInputStream(ba_buff);
                ObjectInputStream ois = new ObjectInputStream(bais);

                ValueItemListClone = (ValueItemList) ois.readObject();
                ois.close();
                bais.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            assert ValueItemListClone != null;
            return ValueItemListClone;
        }

        public boolean isSame(ValueItemList comp) {
            if (mDebugEnabled) log.debug(APPLICATION_TAG + " this.value_item_list_array.size()=" + this.value_item_list_array.size());
            if (mDebugEnabled) log.debug(APPLICATION_TAG + " comp.value_item_list_array.size()=" + comp.value_item_list_array.size());

            boolean result = false;
            int i = 0;
            ArrayList<ValueItem> leftList = new ArrayList<ValueItem>();
            ArrayList<ValueItem> rightList = new ArrayList<ValueItem>();

            for(ValueItem item : this.value_item_list_array) {
                if (!item.isDeleted()) leftList.add(item);
                i++;

                if (mDebugEnabled) log.debug(APPLICATION_TAG + " leftList item" + i + "=" + item.getListValue());
            }

            i = 0;
            for(ValueItem item : comp.value_item_list_array) {
                if (!item.isDeleted()) rightList.add(item);
                i++;

                if (mDebugEnabled) log.debug(APPLICATION_TAG + " rightList item" + i + "=" + item.getListValue());
            }

            i = 0;
            if (leftList.size() == rightList.size()) {
                for(ValueItem left_item : leftList) {
                    ValueItem right_item = rightList.get(i);
                    boolean is_same = left_item.getListValue().toLowerCase().equals(right_item.getListValue().toLowerCase());

                    if (mDebugEnabled) log.debug(APPLICATION_TAG + " left_item.getListValue().toLowerCase()=" + left_item.getListValue().toLowerCase() +
                                                    " right_item.getListValue().toLowerCase()=" + right_item.getListValue().toLowerCase() + 
                                                    " is_same=" + is_same);
                    if (!is_same) break;
                    i++;
                }

                if (i == leftList.size()) result = true;
            }

            if (mDebugEnabled) log.debug(APPLICATION_TAG + " isSame=" + result + " i=" + i + " leftList.size()=" + leftList.size() + " rightList.size()=" + rightList.size());
            return result;
        }
    }

    public static class CustomComparator implements Comparator<ValueItem> {
        @Override
        public int compare(ValueItem o1, ValueItem o2) {
            return o1.getListValue().toLowerCase().compareTo(o2.getListValue().toLowerCase());
//            return this.filename.toLowerCase().compareTo(o.getName().toLowerCase()) * (-1);
        }
    }

    private static void setViewEnabled(Context c, View v, boolean enabled) {
        boolean isLight = ThemeUtil.isLightThemeUsed(c);
        CommonDialog.setViewEnabled(isLight, v, enabled);
    }
}
