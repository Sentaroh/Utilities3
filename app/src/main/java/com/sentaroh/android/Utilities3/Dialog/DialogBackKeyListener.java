package com.sentaroh.android.Utilities3.Dialog;

/*
The MIT License (MIT)
Copyright (c) 2013 Sentaroh

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
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.widget.Toast;

import com.sentaroh.android.Utilities3.R;

public class DialogBackKeyListener implements DialogInterface.OnKeyListener {
	private Context context;
	private Toast toast=null;
	private long last_show_time=0;
	public DialogBackKeyListener(Context c) {
		context=c;
		toast= Toast.makeText(context, c.getString(R.string.msgs_dlg_hardkey_back_button), Toast.LENGTH_SHORT);
		toast.setDuration(1500);
	}
	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode== KeyEvent.KEYCODE_BACK && event.getAction()== KeyEvent.ACTION_DOWN){
			 if ((last_show_time+1500)< System.currentTimeMillis()) {
				 toast.show();
				 last_show_time= System.currentTimeMillis();
			 }
			return true;
		} else {
			return false;
		}
	}
}
