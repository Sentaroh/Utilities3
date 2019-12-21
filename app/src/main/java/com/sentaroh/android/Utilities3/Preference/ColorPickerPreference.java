package com.sentaroh.android.Utilities3.Preference;

/*
The MIT License (MIT)
Copyright (c) 2011-2013 Sentaroh

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
import android.content.Context;
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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sentaroh.android.Utilities3.Dialog.CommonDialog;
import com.sentaroh.android.Utilities3.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorPickerPreference extends DialogPreference {
    private static Logger log= LoggerFactory.getLogger(ColorPickerPreference.class);
	private final static String APPLICATION_TAG="ColorPickerPreference";
	private Context mContext=null;

	private static boolean mDebugEnabled=false;

    public ColorPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
        mContext=context;
        if (mDebugEnabled) log.debug("ColorPickerPreference");
	}
 
	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
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
        final MySavedState myState = new MySavedState(superState);
        myState.color_rgb=mColorRGB;
        return myState;
    };

    private static class MySavedState extends BaseSavedState {
        public String color_rgb;
        @SuppressWarnings("unchecked")
		public MySavedState(Parcel source) {
            super(source);
            color_rgb=source.readString();
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(color_rgb);
        }
        public MySavedState(Parcelable superState) {
            super(superState);
        }
        @SuppressWarnings("unused")
		public static final Creator<MySavedState> CREATOR =
                new Creator<MySavedState>() {
            public ColorPickerPreference.MySavedState createFromParcel(Parcel in) {
                return new ColorPickerPreference.MySavedState(in);
            }
            public ColorPickerPreference.MySavedState[] newArray(int size) {
                return new ColorPickerPreference.MySavedState[size];
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
        MySavedState myState = (MySavedState) state;
        
        super.onRestoreInstanceState(myState.getSuperState());
    };

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (mDebugEnabled) log.debug("onSetInitialValue");
		if (restorePersistedValue) {
			mColorRGB = getPersistedString(mColorRGB);
		} else {
			mColorRGB = (String) defaultValue;
			persistString(mColorRGB);
        }
	};


	@Override
	protected View onCreateDialogView() {
        if (mDebugEnabled) log.debug("onCreateDialogView");
		mColorPickerView =initViewWidget();
		return mColorPickerView;
	};
 
	@Override
	protected void onDialogClosed(boolean positiveResult) {
        if (mDebugEnabled) log.debug("onDialogClosed positiveResult="+positiveResult);
		if (positiveResult) {
		    String rgb="#"+buildRgbValue();
            persistString(rgb);
		}
		super.onDialogClosed(positiveResult);
	};

	@Override
    protected void showDialog(Bundle state) {
        if (mDebugEnabled) log.debug("showDialog");
		super.showDialog(state);
		CommonDialog.setDlgBoxSizeLimit(getDialog(), true);
	};

    private View mColorPickerView =null;
    private String mColorRGB="0";
    private boolean chagedByEditTextInput =false;
    private boolean changedBySeekbar =false;

    @SuppressLint("InflateParams")
	private View initViewWidget() {
        if (mDebugEnabled) log.debug("initViewWidget");
		final Context context=getContext();

		mColorRGB = getPersistedString(mColorRGB);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View file_select_view = inflater.inflate(R.layout.color_picker_preference, null);
        mColorPickerView =file_select_view;

        TextView current_color=(TextView) mColorPickerView.findViewById(R.id.color_picker_preference_color_current);
        current_color.setBackgroundColor((int)Long.parseLong(mColorRGB.substring(1),16));

        final EditText et_alpha_value=(EditText) mColorPickerView.findViewById(R.id.color_picker_preference_color_alpha_value);
        final EditText et_blue_value=(EditText) mColorPickerView.findViewById(R.id.color_picker_preference_color_blue_value);
        final EditText et_green_value=(EditText) mColorPickerView.findViewById(R.id.color_picker_preference_color_green_value);
        final EditText et_red_value=(EditText) mColorPickerView.findViewById(R.id.color_picker_preference_color_red_value);

        String color_alpha_hex=null;
        String color_red_hex=null;
        String color_green_hex=null;
        String color_blue_hex=null;
        try {
            color_alpha_hex=mColorRGB.substring(1,3);
            color_red_hex=mColorRGB.substring(3,5);
            color_green_hex=mColorRGB.substring(5,7);
            color_blue_hex=mColorRGB.substring(7,9);
        } catch(Exception e) {
            color_alpha_hex="ff";
            color_red_hex="00";
            color_green_hex="00";
            color_blue_hex="00";
        }
        et_alpha_value.setText(""+Integer.parseInt(color_alpha_hex, 16));
        et_red_value.setText(""+Integer.parseInt(color_red_hex, 16));
        et_green_value.setText(""+Integer.parseInt(color_green_hex, 16));
        et_blue_value.setText(""+Integer.parseInt(color_blue_hex, 16));

        final SeekBar sb_color_alpha=(SeekBar)mColorPickerView.findViewById(R.id.color_picker_preference_color_alpha_seekbar);
        final SeekBar sb_color_red=(SeekBar)mColorPickerView.findViewById(R.id.color_picker_preference_color_red_seekbar);
        final SeekBar sb_color_green=(SeekBar)mColorPickerView.findViewById(R.id.color_picker_preference_color_green_seekbar);
        final SeekBar sb_color_blue=(SeekBar)mColorPickerView.findViewById(R.id.color_picker_preference_color_blue_seekbar);

        sb_color_alpha.setMax(255);
        sb_color_alpha.setProgress(Integer.parseInt(color_alpha_hex, 16));
        sb_color_alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (changedBySeekbar) et_alpha_value.setText(""+i);
                setSampleBackgroundColot();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changedBySeekbar=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changedBySeekbar=false;
            }
        });

        sb_color_red.setMax(255);
        sb_color_red.setProgress(Integer.parseInt(color_red_hex, 16));
        sb_color_red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (changedBySeekbar) et_red_value.setText(""+i);
                setSampleBackgroundColot();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changedBySeekbar=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changedBySeekbar=false;
            }
        });

        sb_color_green.setMax(255);
        sb_color_green.setProgress(Integer.parseInt(color_green_hex, 16));
        sb_color_green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (changedBySeekbar) et_green_value.setText(""+i);
                setSampleBackgroundColot();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changedBySeekbar=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changedBySeekbar=false;
            }
        });

        sb_color_blue.setMax(255);
        sb_color_blue.setProgress(Integer.parseInt(color_blue_hex, 16));
        sb_color_blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (changedBySeekbar) et_blue_value.setText(""+i);
                setSampleBackgroundColot();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changedBySeekbar=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changedBySeekbar=false;
            }
        });

        et_alpha_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (!changedBySeekbar) {
                    chagedByEditTextInput =false;
                    if (editable.length()==0) {
                        sb_color_alpha.setProgress(0);
                        et_alpha_value.setText("0");
                    } else {
                        if (Integer.parseInt(editable.toString())<=255) {
                            sb_color_alpha.setProgress(Integer.parseInt(editable.toString()));
                            putErrorMessage("");
                        } else {
//                            putErrorMessage("Value must be 0 to 255");
                            et_alpha_value.setText("255");
                        }
                    }
                    et_alpha_value.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chagedByEditTextInput =true;
                        }
                    },0);
                }
            }
        });
        et_red_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (!changedBySeekbar) {
                    chagedByEditTextInput =false;
                    if (editable.length()==0) sb_color_red.setProgress(0);
                    else {
                        if (Integer.parseInt(editable.toString())<=255) {
                            sb_color_red.setProgress(Integer.parseInt(editable.toString()));
                            putErrorMessage("");
                        } else {
                            putErrorMessage("Value must be 0 to 255");
                            editable.clear();
                            editable.append("255");
                        }
                    }
                    et_red_value.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chagedByEditTextInput =true;
                        }
                    },0);
                }
            }
        });
        et_green_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (!changedBySeekbar) {
                    chagedByEditTextInput =false;
                    if (editable.length()==0) sb_color_green.setProgress(0);
                    else {
                        if (Integer.parseInt(editable.toString())<=255) {
                            sb_color_green.setProgress(Integer.parseInt(editable.toString()));
                            putErrorMessage("");
                        } else {
                            putErrorMessage("Value must be 0 to 255");
                            editable.clear();
                            editable.append("255");
                        }
                    }
                    et_green_value.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chagedByEditTextInput =true;
                        }
                    },0);
                }
            }
        });

        et_blue_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (!changedBySeekbar) {
                    chagedByEditTextInput =false;
                    if (editable.length()==0) sb_color_blue.setProgress(0);
                    else {
                        if (Integer.parseInt(editable.toString())<=255) {
                            sb_color_blue.setProgress(Integer.parseInt(editable.toString()));
                            putErrorMessage("");
                        } else {
                            putErrorMessage("Value must be 0 to 255");
                            editable.clear();
                            editable.append("255");
                        }
                    }
                    et_green_value.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chagedByEditTextInput =true;
                        }
                    },0);
                }
            }
        });


        setSampleBackgroundColot();
        return file_select_view;
    };

    private void putErrorMessage(String msg) {
        final TextView tv_msg=(TextView) mColorPickerView.findViewById(R.id.color_picker_preference_message);
        tv_msg.setText(msg);
    }

    private String buildRgbValue() {
//        final EditText et_alpha_value=(EditText) mColorPickerView.findViewById(R.id.color_picker_preference_color_alpha_value);
//        final EditText et_red_value=(EditText) mColorPickerView.findViewById(R.id.color_picker_preference_color_red_value);
//        final EditText et_green_value=(EditText) mColorPickerView.findViewById(R.id.color_picker_preference_color_green_value);
//        final EditText et_blue_value=(EditText) mColorPickerView.findViewById(R.id.color_picker_preference_color_blue_value);

        final SeekBar sb_color_alpha=(SeekBar)mColorPickerView.findViewById(R.id.color_picker_preference_color_alpha_seekbar);
        final SeekBar sb_color_red=(SeekBar)mColorPickerView.findViewById(R.id.color_picker_preference_color_red_seekbar);
        final SeekBar sb_color_green=(SeekBar)mColorPickerView.findViewById(R.id.color_picker_preference_color_green_seekbar);
        final SeekBar sb_color_blue=(SeekBar)mColorPickerView.findViewById(R.id.color_picker_preference_color_blue_seekbar);

        String color_hex_alpha=String.format("%2h", sb_color_alpha.getProgress()).replace(" ","0");
        String color_hex_red=String.format("%2h", sb_color_red.getProgress()).replace(" ","0");
        String color_hex_green=String.format("%2h", sb_color_green.getProgress()).replace(" ","0");
        String color_hex_blue=String.format("%2h", sb_color_blue.getProgress()).replace(" ","0");
        return color_hex_alpha+color_hex_red+color_hex_green+color_hex_blue;
    }

    private void setSampleBackgroundColot() {
        String argb=buildRgbValue();
        TextView sample_text=(TextView) mColorPickerView.findViewById(R.id.color_picker_preference_color_sample);
        TextView argb_text=(TextView) mColorPickerView.findViewById(R.id.color_picker_preference_color_argb_value);
        argb_text.setText("#"+argb);
//        log.info("color="+String.format("%8h",sample_text.getTextColors().getDefaultColor()));
//        if (ThemeUtil.isLightThemeUsed(mContext)) sample_text.setTextColor(0xff404040);
//        else sample_text.setTextColor(0xffc0c0c0);
//        sample_text.setText("Sample color");
//        log.info("argb="+argb);
        sample_text.setBackgroundColor((int)Long.parseLong(argb,16));
    }
    
}
