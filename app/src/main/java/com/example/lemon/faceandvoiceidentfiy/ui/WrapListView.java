package com.example.lemon.faceandvoiceidentfiy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class WrapListView extends ListView {
	private int mWidth = 0;

	public WrapListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WrapListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// 重写onMeasure方法 解决默认横向占满屏幕问题
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int height = getMeasuredHeight();
		for (int i = 0; i < getChildCount(); i++) {
			int childWidth = getChildAt(i).getMeasuredWidth();
			mWidth = Math.max(mWidth, childWidth);
		}

		setMeasuredDimension(mWidth, height);
	}

	/**
	 * 设置宽度，如果不设置，则默认包裹内容
	 * 
	 * @param width
	 */
	protected void setListWidth(int width) {
		mWidth = width;
	}
}