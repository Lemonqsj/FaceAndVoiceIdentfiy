package com.example.lemon.faceandvoiceidentfiy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.example.lemon.faceandvoiceidentfiy.app.DemoApp;
import com.example.lemon.faceandvoiceidentfiy.util.FontsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 人脸验证示例demo
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 * */
public class FaceVerifyDemo extends Activity implements OnClickListener {
	private final static String TAG = FaceVerifyDemo.class.getSimpleName();
	
	// 模型操作
	private int mModelCmd;
	// 删除模型
	private final static int MODEL_DEL = 1;

	// 用户id，唯一标识
	private String authid ;
	// 身份验证对象
	private IdentityVerifier mIdVerifier;
	
	// 选择图片后返回 
	public static final int REQUEST_PICK_PICTURE = 1;
	// 拍照后返回
	private final static int REQUEST_CAMERA_IMAGE = 2;
    // 裁剪图片成功后返回
	public static final int REQUEST_INTENT_CROP = 3;
	
	private Bitmap mImageBitmap = null;
	private byte[] mImageData = null;
	private File mPictureFile;
	
	private ProgressDialog mProDialog;
	private Toast mToast;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_demo);
		// 初始化界面
		initUI();
		// 身份验证对象初始化
		mIdVerifier = IdentityVerifier.createVerifier(this, new InitListener() {
			
			@Override
			public void onInit(int errorCode) {
				if (ErrorCode.SUCCESS == errorCode) {
					showTip("引擎初始化成功");
				} else {
					showTip("引擎初始化失败，错误码：" + errorCode);
				}
			}
		});
		mToast = Toast.makeText(FaceVerifyDemo.this, "", Toast.LENGTH_SHORT);
	}

	private void initUI() {
		TextView title = (TextView) findViewById(R.id.txt_title);
		TextView username = (TextView) findViewById(R.id.txt_uname);
		ImageButton pick = (ImageButton) findViewById(R.id.btn_pick);
		ImageButton enroll = (ImageButton) findViewById(R.id.btn_face_enroll);
		ImageButton camera = (ImageButton) findViewById(R.id.btn_camera);
		ImageButton delete = (ImageButton) findViewById(R.id.btn_face_delete);
		ImageButton verify = (ImageButton) findViewById(R.id.btn_face_verify);
		// 显示用户名
	    authid = DemoApp.mAuth_id;
		if (!TextUtils.isEmpty(authid)) {
			username.setText(authid);
		}
		// 初始化点击监听事件
		pick.setOnClickListener(FaceVerifyDemo.this);
		enroll.setOnClickListener(FaceVerifyDemo.this);
		verify.setOnClickListener(FaceVerifyDemo.this);
		camera.setOnClickListener(FaceVerifyDemo.this);
		delete.setOnClickListener(FaceVerifyDemo.this);
		
		title.setTypeface(FontsUtil.font_yuehei);
		
		mProDialog = new ProgressDialog(FaceVerifyDemo.this);
		mProDialog.setCancelable(true);
		mProDialog.setTitle("请稍候");
		// cancel进度框时，取消正在进行的操作
		mProDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if (null != mIdVerifier) {
					mIdVerifier.cancel();
				}
			}
		});
	}

	/**
	 * 人脸注册监听器
	 */
	private IdentityListener mEnrollListener = new IdentityListener() {

		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, result.getResultString());
			
			if (null != mProDialog) {
				mProDialog.dismiss();
			}
			
			try {
				JSONObject object = new JSONObject(result.getResultString());
				int ret = object.getInt("ret");
				
				if (ErrorCode.SUCCESS == ret) {
					showTip("注册成功");
				}else {
					showTip(new SpeechError(ret).getPlainDescription(true));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
		
		@Override
		public void onError(SpeechError error) {
			if (null != mProDialog) {
				mProDialog.dismiss();
			}

			showTip(error.getPlainDescription(true));
		}

	};
	
	/**
	 * 人脸验证监听器
	 */
	private IdentityListener mVerifyListener = new IdentityListener() {
		
		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, result.getResultString());
			
			if (null != mProDialog) {
				mProDialog.dismiss();
			}
			
			try {
				JSONObject object = new JSONObject(result.getResultString());
				String decision = object.getString("decision");
				
				if ("accepted".equalsIgnoreCase(decision)) {
					showTip("通过验证");
				} else {
					showTip("验证失败");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
		
		@Override
		public void onError(SpeechError error) {
			if (null != mProDialog) {
				mProDialog.dismiss();
			}
			
			showTip(error.getPlainDescription(true));
		}

	};
	
	/**
	 * 人脸模型操作监听器
	 */
	private IdentityListener mModelListener = new IdentityListener() {
		
		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, result.getResultString());
			
			JSONObject jsonResult = null;
			int ret = ErrorCode.SUCCESS;
			try {
				jsonResult = new JSONObject(result.getResultString());
				ret = jsonResult.getInt("ret");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// 根据操作类型判断结果类型
			switch (mModelCmd) {
			case MODEL_DEL:
				if (ErrorCode.SUCCESS == ret) {
					showTip("删除成功");
				} else {
					showTip("删除失败");
				}
				break;
			default:
				break;
			}
		}
		
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
		
		@Override
		public void onError(SpeechError error) {
			// 弹出错误信息
			showTip(error.getPlainDescription(true));
		}

	};

	@Override
	public void onClick(View view) {
		if( null == mIdVerifier ){
			// 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
			showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
			return;
		}
		
		switch (view.getId()) {
		case R.id.btn_pick:
			// 调用系统相册，完成选图
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_PICK);
			startActivityForResult(intent, REQUEST_PICK_PICTURE);
			break;
			
		case R.id.btn_camera:
			// 设置相机拍照后照片保存路径
			mPictureFile = new File(Environment.getExternalStorageDirectory(), 
					"picture" + System.currentTimeMillis()/1000 + ".jpg");
			// 启动拍照,并保存到临时文件
			Intent mIntent = new Intent();
			mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPictureFile));
			mIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
			startActivityForResult(mIntent, REQUEST_CAMERA_IMAGE);
			break;
			
		case R.id.btn_face_enroll:
			// 人脸注册
			if (null != mImageData) {
				mProDialog.setMessage("注册中...");
				mProDialog.show();
				// 设置人脸注册参数
				// 清空参数
				mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
				// 设置会话场景
				mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
				// 设置会话类型
				mIdVerifier.setParameter(SpeechConstant.MFV_SST, "enroll");
				// 设置用户id
				mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);
				// 设置监听器，开始会话
				mIdVerifier.startWorking(mEnrollListener);
				
				// 子业务执行参数，若无可以传空字符传
				StringBuffer params = new StringBuffer();
				// 向子业务写入数据，人脸数据可以一次写入
				mIdVerifier.writeData("ifr", params.toString(), mImageData, 0, mImageData.length);
				// 停止写入
				mIdVerifier.stopWrite("ifr");
			} else {
				showTip("请选择图片后再注册");
			}
			break;
			
		case R.id.btn_face_verify:
			// 人脸验证
			if (null != mImageData) {
				mProDialog.setMessage("验证中...");
				mProDialog.show();
				// 设置人脸验证参数
				// 清空参数
				mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
				// 设置会话场景
				mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
				// 设置会话类型
				mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
				// 设置验证模式，单一验证模式：sin
				mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
				// 用户id
				mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);
				// 设置监听器，开始会话
				mIdVerifier.startWorking(mVerifyListener);
				
				// 子业务执行参数，若无可以传空字符传
				StringBuffer params = new StringBuffer();
				// 向子业务写入数据，人脸数据可以一次写入
				mIdVerifier.writeData("ifr", params.toString(), mImageData, 0, mImageData.length);
				// 停止写入
				mIdVerifier.stopWrite("ifr");
			} else {
				showTip("请选择图片后再验证");
			}
			break;
			
		case R.id.btn_face_delete:
			// 人脸模型删除
			mModelCmd = MODEL_DEL;
			executeModelCommand("delete");
			break;
		default:
			break;
		}
	}

	private void executeModelCommand(String cmd) {
		// 设置人脸模型操作参数
		// 清空参数
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		// 设置会话场景
		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
		// 用户id
		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);

		// 设置模型参数，若无可以传空字符传
		StringBuffer params = new StringBuffer();
		// 执行模型操作
		mIdVerifier.execute("ifr", cmd, params.toString(), mModelListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		
		String fileSrc = null;
		if (requestCode == REQUEST_PICK_PICTURE ) {
			if ("file".equals(data.getData().getScheme())) {
				// 有些低版本机型返回的Uri模式为file
				fileSrc = data.getData().getPath();
			} else {
				// Uri模型为content
				String[] proj = {MediaStore.Images.Media.DATA};
				Cursor cursor = getContentResolver().query(data.getData(), proj,
						null, null, null);
				cursor.moveToFirst();
				int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				fileSrc = cursor.getString(idx);
				cursor.close();
			}
			// 跳转到图片裁剪页面
			cropPicture(this,Uri.fromFile(new File(fileSrc)));
		} else if (requestCode == REQUEST_CAMERA_IMAGE) {
			if (null == mPictureFile) {
				showTip("拍照失败，请重试");
				return;
			}
			
			fileSrc = mPictureFile.getAbsolutePath();
			updateGallery(fileSrc);
			// 跳转到图片裁剪页面
			cropPicture(this,Uri.fromFile(new File(fileSrc)));
		} else if (requestCode == REQUEST_INTENT_CROP) {
			// 获取返回数据
			Bitmap bmp = data.getParcelableExtra("data");
			// 获取裁剪后图片保存路径
			fileSrc = getImagePath();
			
			// 若返回数据不为null，保存至本地，防止裁剪时未能正常保存
			if(null != bmp){
				saveBitmapToFile(bmp);
			}
			// 获取图片的宽和高
			Options options = new Options();
			options.inJustDecodeBounds = true;
			mImageBitmap = BitmapFactory.decodeFile(fileSrc, options);
			
			// 压缩图片
			options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(
					(double) options.outWidth / 1024f,
					(double) options.outHeight / 1024f)));
			options.inJustDecodeBounds = false;
			mImageBitmap = BitmapFactory.decodeFile(fileSrc, options);
			
			
			// 若mImageBitmap为空则图片信息不能正常获取
			if(null == mImageBitmap) {
				showTip("图片信息无法正常获取！");
				return;
			}
			
			// 部分手机会对图片做旋转，这里检测旋转角度
			int degree = readPictureDegree(fileSrc);
			if (degree != 0) {
				// 把图片旋转为正的方向
				mImageBitmap = rotateImage(degree, mImageBitmap);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			//可根据流量及网络状况对图片进行压缩
			mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
			mImageData = baos.toByteArray();
			
			((ImageView) findViewById(R.id.img_ifr)).setImageBitmap(mImageBitmap);
		}
	}

	@Override
	public void finish() {
		if (null != mProDialog) {
			mProDialog.dismiss();
		}
		setResult(RESULT_OK);
		super.finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (null != mIdVerifier) {
			mIdVerifier.destroy();
			mIdVerifier = null;
		}
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path 图片绝对路径
	 * @return degree 旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
	
	private void updateGallery(String filename) {
		MediaScannerConnection.scanFile(this, new String[] {filename}, null,
				new MediaScannerConnection.OnScanCompletedListener() {
				
					@Override
					public void onScanCompleted(String path, Uri uri) {

					}
				});
	}

	/**
	 * 旋转图片
	 * 
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotateImage(int angle, Bitmap bitmap) {
		// 图片旋转矩阵
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 得到旋转后的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}
	
	/**
	 * Toast弹出提示
	 * @param str
	 */
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	
	
	/***
	 * 裁剪图片
	 * @param activity Activity
	 * @param uri 图片的Uri
	 */
	public void cropPicture(Activity activity, Uri uri) {
		Intent innerIntent = new Intent("com.android.camera.action.CROP");
		innerIntent.setDataAndType(uri, "image/*");
		innerIntent.putExtra("crop", "true");// 才能出剪辑的小方框，不然没有剪辑功能，只能选取图片
		innerIntent.putExtra("aspectX", 1); // 放大缩小比例的X
		innerIntent.putExtra("aspectY", 1);// 放大缩小比例的X   这里的比例为：   1:1
		innerIntent.putExtra("outputX", 320);  //这个是限制输出图片大小
		innerIntent.putExtra("outputY", 320); 
		innerIntent.putExtra("return-data", true);
		// 切图大小不足输出，无黑框
		innerIntent.putExtra("scale", true);
		innerIntent.putExtra("scaleUpIfNeeded", true);
		innerIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getImagePath())));
		innerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		activity.startActivityForResult(innerIntent, REQUEST_INTENT_CROP);
	}
	
	/**
	 * 设置保存图片路径
	 * @return
	 */
	private String getImagePath(){
		String path;
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return null;
		}
		path =  Environment.getExternalStorageDirectory().getAbsolutePath() +"/MFVDemo/";
		File folder = new File(path);
		if (folder != null && !folder.exists()) {
			folder.mkdirs();
		}
		path += "mfvtest.jpg";
		return path;
	}
	
	/**
	 * 保存Bitmap至本地
	 * @param Bitmap
	 */
	private void saveBitmapToFile(Bitmap bmp){
		String file_path = getImagePath();
		File file = new File(file_path);
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
