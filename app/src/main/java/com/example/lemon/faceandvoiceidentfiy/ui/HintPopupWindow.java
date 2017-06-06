package com.example.lemon.faceandvoiceidentfiy.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.lemon.faceandvoiceidentfiy.R;

/**
 * 提示框
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 */
public class HintPopupWindow extends PopupWindow{
	private TextView mHintTextView;
	private String mHintText;

	public HintPopupWindow(Context context) {
		super(context);
		
		View contentView = LayoutInflater.from(context).inflate(R.layout.popup_window_content, null);
		mHintTextView = (TextView) contentView.findViewById(R.id.txt_popup_hint);
		setContentView(contentView);
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(null);
	}
	
	public void setHint(String hint) {
		if (TextUtils.isEmpty(mHintText) || !mHintText.equals(hint)) {
			// 只有与先前提示不一致时，才设置到textview
			mHintTextView.setText(hint);
			mHintText = hint;
		}
	}
	
}
