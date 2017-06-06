package com.example.lemon.faceandvoiceidentfiy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lemon.faceandvoiceidentfiy.identify.GroupManagerActivity;
import com.example.lemon.faceandvoiceidentfiy.util.FontsUtil;

/**
 * 身份验证demo主页面
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 */
public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initUI();
	}
	
	/**
	 * 初始化UI
	 */
	private void initUI() {
		TextView title = (TextView) findViewById(R.id.txt_title);
		TextView intro = (TextView) findViewById(R.id.txt_introduction);
		ImageView faceDemo = (ImageView) findViewById(R.id.btn_face_demo);
		ImageView vocalDemo = (ImageView) findViewById(R.id.btn_vocal_demo);
		ImageView mixedDemo = (ImageView) findViewById(R.id.btn_mixed_demo);
		ImageView groupManagerDemo = (ImageView) findViewById(R.id.btn_groupManager_demo);
		
		faceDemo.setOnClickListener(MainActivity.this);
		vocalDemo.setOnClickListener(MainActivity.this);
		mixedDemo.setOnClickListener(MainActivity.this);
		groupManagerDemo.setOnClickListener(MainActivity.this);
		
		title.setTypeface(FontsUtil.font_yuehei);
		intro.setText(FontsUtil.ToDBC(getString(R.string.introduction_demo)));
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btn_face_demo:
			// 跳转至人脸验证示例
			intent = new Intent(MainActivity.this, FaceVerifyDemo.class);
			intent.putExtra("scenes", "ifr");
			startActivity(intent);
			break;
		case R.id.btn_vocal_demo:
			// 跳转至声纹验证示例
			intent = new Intent(MainActivity.this, VocalVerifyDemo.class);
			intent.putExtra("scenes", "ivp");
			startActivity(intent);
			break;
		case R.id.btn_mixed_demo:
			// 跳转至融合验证示例
			intent = new Intent(MainActivity.this, testCameraIdentifyFace.class);
			intent.putExtra("scenes", "mix");
			startActivity(intent);
			break;
		case R.id.btn_groupManager_demo:
			// 跳转至融合验证示例
			intent = new Intent(MainActivity.this, GroupManagerActivity.class);
			startActivity(intent);
			break;
			
		default:
			break;
		}
	}
}
