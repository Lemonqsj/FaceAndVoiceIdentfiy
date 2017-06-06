package com.example.lemon.faceandvoiceidentfiy.app;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;
import com.example.lemon.faceandvoiceidentfiy.R;
import com.example.lemon.faceandvoiceidentfiy.entity.GroupHisList;
import com.example.lemon.faceandvoiceidentfiy.entity.User;
import com.example.lemon.faceandvoiceidentfiy.util.FontsUtil;

/**
 * 应用入口类
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 * */
public class DemoApp extends Application {
	public static String mAuth_id;
	private static User mUser;
	public static final String HIS_FILE_NAME ="HistoryFile";
	private static GroupHisList mHisList ;
	


	@Override
	public void onCreate() {
		
		// 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用“,”分隔。
		// 设置你申请的应用appid
		StringBuffer param = new StringBuffer();
		param.append("appid="+getString(R.string.app_id));
		SpeechUtility.createUtility(getApplicationContext(), param.toString());
		
		// 创建字体
		FontsUtil.createFonts(getApplicationContext());
		
		super.onCreate();
	}
	
	
	
	public static User getHostUser() {
		if (null == mUser) {
			mUser = new User();
		}
		return mUser;
	}
	public static void setHostUser(User user) {
		mUser = user;
	}
	
	public static GroupHisList getmHisList() {
		if (null == mHisList) {
			mHisList = new GroupHisList();
		}
		return mHisList;
	}

	public static void setmHisList(GroupHisList mHisList) {
		DemoApp.mHisList = mHisList;
	}
	
}
