package com.example.lemon.faceandvoiceidentfiy.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.lemon.faceandvoiceidentfiy.R;

/**
 * 水波纹绘制类
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 */
public class RecordView extends View {

	private Drawable drawable_vol = null;
	private Drawable drawable_outter = null;
//	private Drawable drawable_inner = null;
	private int      curVol = 0;
	private int    	 toVol = 0;
	private float    MIN_SCALE = 0.60f;
	private int DWIDTH_VOL = 0;
	private int DWIDTH_OUTTER = 0;
	// 圆圈中心在父控件中的坐标，默认在父控件的中心
	private int centerX;
	private int centerY;
	// 是否使用默认的中心位置
	private boolean useDefaultCenter = true;
	// 是否绘制外圈波纹动画
	private boolean drawOuterWave = false;
	
	public RecordView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RecordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public RecordView(Context context) {
		super(context);
		initView(context);
	}
	
	public void setCenterXY(int x, int y) {
		centerX = x;
		centerY = y;
		useDefaultCenter = false;
	}
	
	private void initView(Context context) {
		drawable_vol    = context.getResources().getDrawable(R.drawable.circle_vol);
		drawable_outter = context.getResources().getDrawable(R.drawable.circle_outter);
//		drawable_inner  = context.getResources().getDrawable(R.drawable.circle_inner);
		
		DWIDTH_VOL    = drawable_vol.getIntrinsicWidth();
		DWIDTH_OUTTER = drawable_outter.getIntrinsicWidth();
	}
	
	private float calScale(int volume) {
		return MIN_SCALE + ((float)volume)/30.0f;
	}
	Rect canrect = null;
	Rect drarect = new Rect();
	boolean fastStep = true;
	int stepCount = 0;
	private float scale_pre = 0.55f;
	
	@Override
	public void draw(Canvas canvas) {
		if(canrect == null)
			canrect = new Rect(0, 0, getWidth(), getHeight());
		canvas.save();
		
		if (useDefaultCenter) {
			canvas.translate(canrect.width()>>1, canrect.height()>>1);
		} else {
			canvas.translate(centerX, centerY);
		}
		 
		int step = 1;
		if(!fastStep && stepCount++%5!=1)
		{
			step = 0;
		}
		if(curVol > toVol)
			curVol -= step;
		else if(curVol < toVol)
			curVol += step;
		
		if(curVol == 0 && toVol == 0)
		{
			fastStep = false;
			toVol = 5;
		}
		
		float scale = calScale(curVol);
		Log.d("RecordView", "Record draw scale = " + curVol + ","+ toVol);
		int cr = (int) (DWIDTH_VOL*scale)>>1;
		drarect.set( -cr, -cr, cr, cr);
		drawable_vol.setBounds(drarect);
		drawable_vol.draw(canvas);
		
		//画内层 光晕
//		if(scale > 1.2)
//		{
//			drarect.set( -cr-1, -cr-1, cr+1, cr+1);
//			drawable_inner.setBounds(drarect);
//			drawable_inner.draw(canvas);
//		}		
		
		if (drawOuterWave) {
			//画最外层波浪
			scale_pre += 0.02; 
			scale_pre %= 2;
			cr = (int)(DWIDTH_OUTTER*scale_pre)>>1;
			drarect.set( -cr, -cr, cr, cr);
			drawable_outter.setBounds(drarect);		
			drawable_outter.draw(canvas);
		}
		
		canvas.restore();
		super.draw(canvas);
	}

	private static final int MSG_INVALIDATE = 0x01;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_INVALIDATE:
				invalidate();
				mHandler.removeMessages(MSG_INVALIDATE);
				mHandler.sendEmptyMessageDelayed(MSG_INVALIDATE,30);
				break;
			default:
				break;
			}
		}
	};

	public void setVolume(int volume) {
		Log.d("RecordView", "setVolume = " + volume + curVol + "," + toVol);
		if (volume > toVol) {
			toVol = volume;
			fastStep = true;
		} else if (curVol >= toVol) {
			toVol = volume;
		}
		mHandler.sendEmptyMessage(MSG_INVALIDATE);
	}

	public void startRecording() {
		curVol = 0;
		toVol  = 0;
		scale_pre = 0.55f;
		drawOuterWave = true;
		setVisibility(View.VISIBLE);
		mHandler.removeMessages(MSG_INVALIDATE);
		mHandler.sendEmptyMessage(MSG_INVALIDATE);
	}
	
	public void stopRecord() {
		drawOuterWave = false;
		mHandler.removeMessages(MSG_INVALIDATE);
		setVisibility(View.GONE);
	}
	
}
