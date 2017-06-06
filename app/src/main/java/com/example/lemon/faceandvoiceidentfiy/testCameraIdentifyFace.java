package com.example.lemon.faceandvoiceidentfiy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lemon.faceandvoiceidentfiy.app.DemoApp;
import com.example.lemon.faceandvoiceidentfiy.entity.User;
import com.example.lemon.faceandvoiceidentfiy.mixedverify.VerifyResultActivity;
import com.example.lemon.faceandvoiceidentfiy.ui.HintPopupWindow;
import com.example.lemon.faceandvoiceidentfiy.ui.RecordView;
import com.example.lemon.faceandvoiceidentfiy.util.CameraHelper;
import com.example.lemon.faceandvoiceidentfiy.util.FontsUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.record.PcmRecorder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Describe the function of the class
 *
 * @author lemon
 * @date 2017/6/5
 * @time 15:08
 * @Email lemonqsj@163.com
 * @description Describe the place where the class needs to pay attention.
 */

public class testCameraIdentifyFace extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = testCameraIdentifyFace.class.getSimpleName();
    // 用户信息
    private User mUser;
    // 是否登录成功
    private boolean mLoginSuccess = false;
    private Camera mCamera;
    private int mCameraId = CameraInfo.CAMERA_FACING_FRONT;
    private Camera.Size mPreviewSize;
    private boolean mIsPreviewing = false;
    private boolean mCanTakePic = true;
    private boolean mIsPause = false;
    private CameraHelper mCameraHelper;

    // 相机预览SurfaceView
    private SurfaceView     mPreviewSurface;
    private TextView        mHintTextView;
    private TextView        mPwdTextView;
    private ImageButton     mFlashSwitchButton;
    private ImageButton     mChangeCameraButton;
    private ProgressDialog  mProDialog;
   //
    private RecordView      mVolView;
    private ImageButton     mRecordButton;
    private boolean         mRecordButtonPressed;
    private HintPopupWindow mPopupHint;

    // 提示框显示位置的纵坐标
    private int mHintOffsetY;

    // 用户id，唯一标识
    private String           authid ;
    // 身份验证对象
    private IdentityVerifier mIdVerifier;
    // 验证数字密码
    private String           mVerifyNumPwd;
    // 验证密码类型，3：数字密码，其他暂不支持
    private static final String PWD_TYPE_NUM = "3";
    // 录音尾端点
    private static final String VAD_EOS = "2000";
    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;
    // 是否开始验证
    private boolean mVerifyStarted = false;
    // 操作是否被其他应用中断
    private boolean mInterruptedByOtherApp = false;
    // 按住麦克风为true开始写音频，松开为false停止写入
    private boolean mWriteAudio = false;
    private boolean mMoveOutofBound = false;
    private final int DELAY_TIME = 300;
    // 在松开麦克风之前是否已经出现错误
    private boolean mErrorOccurBeforeUp = false;
    // 上次有效点击快门的时间，用于防止用户频繁点击快门
    private long mLastValidShutterClickTime = 0;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video_verify);

        mUser = new User();

        authid = DemoApp.mAuth_id;

        mCameraHelper = CameraHelper.createHelper(testCameraIdentifyFace.this);

        initUI();

        mIdVerifier = IdentityVerifier.createVerifier(getApplicationContext(), new InitListener() {

            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode);
                }
            }


        });
    }

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
                double face_score = object.getDouble("face_score");

                mUser.setFaceScore(face_score);

                if ("accepted".equalsIgnoreCase(decision)) {
                    showTip("登陆成功");

                    mLoginSuccess = true;
                    mUser.setLogined(true);
                    mUser.setFacePic(mCameraHelper.getImageBitmap());

                    mPopupHint.dismiss();
                } else {
                   // showTip("验证失败");

                    mLoginSuccess = false;
                    mUser.setLogined(false);

                    showTip("登录失败");
                }

                Intent intent = new Intent(testCameraIdentifyFace.this, VerifyResultActivity.class);
                intent.putExtra("user", mUser);
                startActivity(intent);
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

            onResume();
        }

    };

    private static final int MSG_FACE_START = 1;
    private static final int MSG_TAKE_PICTURE = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FACE_START:
                    startFaceVerify();
                    break;
                case MSG_TAKE_PICTURE:
                    takePicture();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 开始人脸验证
     * */
    private void startFaceVerify() {
        Log.d(TAG, "startFaceVerify");

        byte[] imageData = mCameraHelper.getImageData();

        // 子业务执行参数，若无可以传空字符传
        StringBuffer params = new StringBuffer();
        // 向子业务写入数据，人脸数据可以一次写入
        mIdVerifier.writeData("ifr", params.toString(), imageData, 0, imageData.length);
        // 停止写入数据
        mIdVerifier.stopWrite("ifr");
    }

    private void takePicture() {
        // 拍照，发起人脸注册
        try {
            if(mCamera != null && mCanTakePic){
                Log.d(TAG, "takePicture");
                mCamera.takePicture(mShutterCallback, null, mPictureCallback);
                mCanTakePic = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {

        }
    };

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken");
            if (!mIsPause) {
                mCameraHelper.setCacheData(data, mCameraId, testCameraIdentifyFace.this);
                //发送消息 开始人脸识别
                mHandler.sendEmptyMessage(MSG_FACE_START);
            }
            mIsPreviewing = false;
            mCanTakePic = true;
        }
    };
    private void showTip(String s) {
        mToast.setText(s);
        mToast.show();
    }

    private void initUI() {
        mPreviewSurface = (SurfaceView) findViewById(R.id.sfv_preview);
        mPreviewSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置屏幕常亮
        mPreviewSurface.getHolder().setKeepScreenOn(true);
        // surfaceView增加回调句柄
        mPreviewSurface.getHolder().addCallback(this);

       // mHintTextView = (TextView) findViewById(R.id.txt_hint);
        //mPwdTextView = (TextView)findViewById(R.id.txt_num);
        mFlashSwitchButton = (ImageButton) findViewById(R.id.btn_flash_switch);
        mChangeCameraButton = (ImageButton) findViewById(R.id.btn_change_camera);
        mRecordButton = (ImageButton) findViewById(R.id.btn_record);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setCanceledOnTouchOutside(false);
        mProDialog.setTitle("请稍后");
        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if( null != mIdVerifier ){
                    mIdVerifier.cancel();
                }

                mVerifyStarted = false;
                mPopupHint.setHint(getString(R.string.vocal_register_press_hint));
            }
        });

        mChangeCameraButton.setOnClickListener(this);
        mFlashSwitchButton.setOnClickListener(this);
        mToast = Toast.makeText(testCameraIdentifyFace.this, "", Toast.LENGTH_SHORT);

        // 设置麦克风touch事件监听器
        mRecordButton.setOnTouchListener(mRecordButtonOnTouchListener);

        // 设置显示字体
        TextView title = (TextView) findViewById(R.id.txt_title);
        title.setTypeface(FontsUtil.font_yuehei);

        mPopupHint = new HintPopupWindow(testCameraIdentifyFace.this);
        mPopupHint.setHint(getString(R.string.vocal_register_press_hint));

        mVolView = new RecordView(testCameraIdentifyFace.this);
    }

    // 录音按钮Touch事件监听器
    private View.OnTouchListener mRecordButtonOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if( null == mIdVerifier ){
                // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mRecordButtonPressed = true;
                    // 按下事件
                    if (!mVerifyStarted) {
                        if (isFrequestlyClick()) {
                            // 频繁无效点击，则不处理
                            return false;
                        }
                        if (null != mCamera && !mIsPreviewing) {
                            mCamera.startPreview();
                            mIsPreviewing = true;
                        }
//                        if (!mVerifyStarted) {
//                            mWriteAudio = true;
//                            // 开启录音机
//                            mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
//                            try {
//                                mPcmRecorder.startRecording(mPcmRecordListener);
//                            } catch (SpeechError e) {
//                                e.printStackTrace();
//                            }
//                            // 开始验证
//                            startMFVVerify();
//                        }
                        startMFVVerify();
                        mPopupHint.setHint(getString(R.string.vocal_register_listening_hint));
                    } else {
                        showTip(getString(R.string.login_hint_verifying));
                        return false;
                    }
//                    mMoveOutofBound = false;
                    mErrorOccurBeforeUp = false;
//                    mVolView.startRecording();
                    break;
//                case MotionEvent.ACTION_MOVE:
//                    if (!mVerifyStarted) {
//                        return false;
//                    }
//
//                    int x = (int) event.getX();
//                    int y = (int) event.getY();
//                    Rect rect = new Rect();
//                    v.getLocalVisibleRect(rect);
//                    if (!rect.contains(x, y)) {
//                        // 按住后手指move出控件范围
//                        if (!mMoveOutofBound) {
//                            mMoveOutofBound = true;
//                            // 停止录音，开始拍照，随后开始人脸验证
//                            stopRecording();
//                            if (!mErrorOccurBeforeUp) {
//                                if (mPopupHint.isShowing()) {
//                                    mPopupHint.dismiss();
//                                }
//                                mHandler.sendEmptyMessageDelayed(MSG_TAKE_PICTURE, DELAY_TIME);
//                                showProDialog();
//                            }
//                        }
//                    }
                   // break;
                case MotionEvent.ACTION_UP:
                    mRecordButtonPressed = false;
                    v.performClick();

//                    // 停止录音，开始拍照，随后开始人脸验证
//                    stopRecording();

                    if (!mVerifyStarted) {
                        mPopupHint.setHint(getString(R.string.vocal_register_press_hint));
                        return false;
                    }

                    // 松开事件
                    if (!mMoveOutofBound) {

                        if (!mErrorOccurBeforeUp) {
                            if (mPopupHint.isShowing()) {
                                mPopupHint.dismiss();
                            }
                            mHandler.sendEmptyMessageDelayed(MSG_TAKE_PICTURE, DELAY_TIME);
//                            showProDialog();
                        }
                    }
                    break;

                default:
                    break;
            }

            return false;
        }
    };

    /**
     * 开始身份验证
     * */
    private void startMFVVerify() {
        Log.d(TAG, "startMFVVerify");

        mVerifyStarted = true;
        mLoginSuccess = false;

        // 设置融合验证参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
        // 验证模式，混合生物特征数据验证模式：mix
        mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mVerifyListener);
    }

    // 判断是否为频繁点击
    private boolean isFrequestlyClick() {
        long clickTime = System.currentTimeMillis();
        if (clickTime - mLastValidShutterClickTime < 200) {
            return true;
        }
        mLastValidShutterClickTime = clickTime;

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change_camera:
                int cameracount = Camera.getNumberOfCameras();
                if (cameracount <= 1) {
                    showTip(getString(R.string.hint_change_not_support));
                    return;
                }
                // 先关闭摄相头
                closeCamera();

                if (CameraInfo.CAMERA_FACING_BACK == mCameraId) {
                    if (CameraHelper.hasCamera(CameraInfo.CAMERA_FACING_FRONT))
                        mCameraId = CameraInfo.CAMERA_FACING_FRONT;
                } else if (CameraInfo.CAMERA_FACING_FRONT == mCameraId) {
                    if (CameraHelper.hasCamera(CameraInfo.CAMERA_FACING_BACK))
                        mCameraId = CameraInfo.CAMERA_FACING_BACK;
                } else {
                    showTip(getString(R.string.hint_change_not_support));
                    return;
                }

                openCamera();
                break;
            case R.id.btn_flash_switch:
                // 检查当前硬件设施是否支持闪光灯
                if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
                        || mCameraId == CameraInfo.CAMERA_FACING_FRONT){
                    showTip(getString(R.string.hint_flash_not_support));
                    return;
                }
                Camera.Parameters param = mCamera.getParameters();
                String flasemode = param.getFlashMode();
                if(TextUtils.isEmpty(flasemode))
                    return;
                if(flasemode.equals(Camera.Parameters.FLASH_MODE_TORCH)){
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mFlashSwitchButton.setImageResource(R.drawable.flash_close);
                    showTip(getString(R.string.hint_flash_closed));
                }else{
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mFlashSwitchButton.setImageResource(R.drawable.flash_open);
                    showTip(getString(R.string.hint_flash_opened));
                }
                // 防止参数设置部分手机failed
                try {
                    mCamera.setParameters(param);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    private void closeCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void openCamera() {
        if (null != mCamera) {
            return;
        }

        // 只有一个摄相头，打开后置
        if (Camera.getNumberOfCameras() == 1) {
            mCameraId = CameraInfo.CAMERA_FACING_BACK;
        }

        try {
            // 打开摄像头
            mCamera = Camera.open(mCameraId);
            mCamera.setDisplayOrientation(CameraHelper.getPreviewDegree(testCameraIdentifyFace.this, mCameraId));
            mCamera.setParameters(mCameraHelper.getCameraParam(testCameraIdentifyFace.this, mCamera, mCameraId));
            mPreviewSize = mCamera.getParameters().getPreviewSize();

            setSurfaceViewSize();

            // 设置用于显示拍照影像的SurfaceHolder对象
            mCamera.setPreviewDisplay(mPreviewSurface.getHolder());
            mCamera.startPreview(); // 开始预览
            mIsPreviewing = true;

            Log.d(TAG, "camera create");
        } catch (Exception e) {
            closeCamera();
            e.printStackTrace();
        }
    }
    private void setSurfaceViewSize() {
        Point                       fitSurfaceSize = mCameraHelper.getFitSurfaceSize(mPreviewSize);
        RelativeLayout.LayoutParams params         = new RelativeLayout.LayoutParams(fitSurfaceSize.y, fitSurfaceSize.x);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPreviewSurface.setLayoutParams(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            // 刷新屏幕横转参数变化，得放在catch里防止出现异常事件
            mCamera.setParameters(mCameraHelper.getCameraParam(testCameraIdentifyFace.this,
                    mCamera, mCameraId));// 设置相机的参数
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

    @Override
    protected void onPause() {
        mIsPause = true;

//		closeCamera();

        // 关闭录音机
        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }

        mHandler.removeMessages(MSG_FACE_START);
        mHandler.removeMessages(MSG_TAKE_PICTURE);

        // 若已经开始验证，然后执行了onPause就表明Activity被其他应用中断
        if (mVerifyStarted) {
            mInterruptedByOtherApp = true;
        }

        mVerifyStarted = false;

        if (null != mIdVerifier) {
            mIdVerifier.cancel();
        }

        // 防止跳转到其他Activity后还显示前面的toast
        if (null != mToast) {
            mToast.cancel();
        }
        // 取消提示框
        if (null != mPopupHint) {
            mPopupHint.dismiss();
        }
        // 取消进度框
        if (null != mProDialog) {
            mProDialog.cancel();
        }

        //setStopViewStatus();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获取和设置验证密码
//        mVerifyNumPwd = VerifierUtil.generateNumberPassword(8);
//        mPwdTextView.setText(getStyledPwdHint(mVerifyNumPwd));

        mIsPause = false;
        mVerifyStarted=false;
        mLastValidShutterClickTime = 0;

        // 显示操作提示
       // mHintTextView.setText(R.string.login_operation_hint);
        mPopupHint.setHint(getString(R.string.vocal_register_press_hint));

        mCanTakePic = true;
        if (mCamera != null) {
            mCamera.startPreview();
            mIsPreviewing = true;
        }

        if (mInterruptedByOtherApp) {
            showTip(getString(R.string.login_hint_interrupted));
            mInterruptedByOtherApp = false;
        }
    }
}
