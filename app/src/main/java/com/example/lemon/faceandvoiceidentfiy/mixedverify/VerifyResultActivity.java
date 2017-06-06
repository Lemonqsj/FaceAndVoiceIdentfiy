package com.example.lemon.faceandvoiceidentfiy.mixedverify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lemon.faceandvoiceidentfiy.MainActivity;
import com.example.lemon.faceandvoiceidentfiy.R;
import com.example.lemon.faceandvoiceidentfiy.entity.User;
import com.example.lemon.faceandvoiceidentfiy.util.CameraHelper;
import com.example.lemon.faceandvoiceidentfiy.util.FontsUtil;

import java.text.DecimalFormat;

/**
 * 验证结果显示页面
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 * */
public class VerifyResultActivity extends Activity implements OnClickListener {
	private final static int SUCCESS_SIMILARITY_COLOR = Color.parseColor("#8cd97c");
	private final static int LOSE_SIMILARITY_COLOR = Color.parseColor("#d9ac7c");
	
	private ImageView mPicShower;
	private TextView mLoginTitleText;
	private ImageView mChallengeResult;
	private TextView mFusionScore;
	private TextView mFaceScore;
	private TextView mVocalScore;
	private TextView mBonusHint;
	private ImageButton mAgainButton;
	private ImageButton mHomeButton;
	private User mUser;
	private CameraHelper mCameraHelper;
	
	private Toast mToast;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mixed_verify_result);
		
		mUser = (User)getIntent().getSerializableExtra("user");
		mCameraHelper = CameraHelper.createHelper(VerifyResultActivity.this);
		
		initUI();
		
		setResult();
	}

	@SuppressLint("ShowToast")
	private void initUI() {
		mLoginTitleText = (TextView) findViewById(R.id.txt_title);
		mLoginTitleText.setTypeface(FontsUtil.font_yuehei);
		
		mPicShower = (ImageView) findViewById(R.id.pic_show);
		mChallengeResult = (ImageView) findViewById(R.id.img_challenge_result);
		mFusionScore = (TextView) findViewById(R.id.txt_similarity);
		mFaceScore = (TextView) findViewById(R.id.txt_face_similarity);
		mVocalScore = (TextView) findViewById(R.id.txt_vocal_similarity);
		mBonusHint = (TextView) findViewById(R.id.txt_bonus_hint);
		mAgainButton = (ImageButton) findViewById(R.id.btn_again);
		mHomeButton = (ImageButton) findViewById(R.id.btn_home);
		
		mAgainButton.setOnClickListener(VerifyResultActivity.this);
		mHomeButton.setOnClickListener(VerifyResultActivity.this);
		
		
		
		mToast = Toast.makeText(VerifyResultActivity.this, "", Toast.LENGTH_SHORT);
		setFitPicShowerSize();
	}
	
	private void setFitPicShowerSize() {
		Point fitPicShowerSize = mCameraHelper.getFitSurfaceSize();
		// 判空，避免空指针引用错误
		if (null != fitPicShowerSize) {
			LayoutParams params = new LayoutParams(fitPicShowerSize.y, fitPicShowerSize.x);
	 		params.addRule(RelativeLayout.CENTER_IN_PARENT);
	 		mPicShower.setLayoutParams(params);
		}
	}
	
	// 显示挑战结果
	private void setResult() {
		Bitmap image = mCameraHelper.getImageBitmap();
		mPicShower.setImageBitmap(image);
		
		if (mUser.isLogined()) {
			// 成功
			mChallengeResult.setImageResource(R.drawable.success);
			mFusionScore.setTextColor(SUCCESS_SIMILARITY_COLOR);
			mFaceScore.setTextColor(SUCCESS_SIMILARITY_COLOR);
			mVocalScore.setTextColor(SUCCESS_SIMILARITY_COLOR);
			
			// 登录成功不要显示获奖提示
			mBonusHint.setVisibility(View.INVISIBLE);
		} else {
			// 失败
			mChallengeResult.setImageResource(R.drawable.lose);
			mFusionScore.setTextColor(LOSE_SIMILARITY_COLOR);
			mFaceScore.setTextColor(LOSE_SIMILARITY_COLOR);
			mVocalScore.setTextColor(LOSE_SIMILARITY_COLOR);
			mBonusHint.setVisibility(View.INVISIBLE);
		}
		
		DecimalFormat df = new DecimalFormat("#.##");
		mFusionScore.setText(df.format(mUser.getFusionScore()) + "%");
		mFaceScore.setText(df.format(mUser.getFaceScore()) + "%");
		mVocalScore.setText(df.format(mUser.getVoiceScore()) + "%");
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_again:
			VerifyResultActivity.this.finish();
			break;
		case R.id.btn_home:
			Intent intent = new Intent(VerifyResultActivity.this, MainActivity.class);
			// 将MainActivity以上的Activity从栈中全部clear掉
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (null != mToast) {
			mToast.cancel();
		}
	}
}
