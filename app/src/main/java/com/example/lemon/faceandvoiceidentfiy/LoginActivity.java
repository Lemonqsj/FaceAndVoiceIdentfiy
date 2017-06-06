package com.example.lemon.faceandvoiceidentfiy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lemon.faceandvoiceidentfiy.app.DemoApp;
import com.example.lemon.faceandvoiceidentfiy.entity.GroupHisList;
import com.example.lemon.faceandvoiceidentfiy.entity.User;
import com.example.lemon.faceandvoiceidentfiy.identify.FaceIdentifyActivity;
import com.example.lemon.faceandvoiceidentfiy.identify.VocalIdentifyActivity;
import com.example.lemon.faceandvoiceidentfiy.util.FontsUtil;
import com.example.lemon.faceandvoiceidentfiy.util.FuncUtil;
import com.example.lemon.faceandvoiceidentfiy.ui.DropEditText;

/**
 * 登录页面
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 * */
public class LoginActivity extends Activity implements OnClickListener {

	// 组ID下拉输入框
	private DropEditText mGroupIDDrop;
	
	private Toast mToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		intiUI();
		mToast = Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT);
	}

	private void intiUI() {
		TextView title = (TextView) findViewById(R.id.txt_login_title);
		TextView tip = (TextView) findViewById(R.id.txt_tip);

		findViewById(R.id.btn_confirm).setOnClickListener(LoginActivity.this);

		title.setTypeface(FontsUtil.font_yuehei);
		tip.setText(FontsUtil.ToDBC(getString(R.string.tip_auth_id)));
		
		//从本地读出历史记录
		DemoApp.setmHisList((GroupHisList)FuncUtil.readObject(this, DemoApp.HIS_FILE_NAME));
		
		mGroupIDDrop = (DropEditText) findViewById(R.id.drop_edit);

		((RelativeLayout) findViewById(R.id.btn_idf_speech)).setOnClickListener(this);
		((RelativeLayout) findViewById(R.id.btn_idf_face)).setOnClickListener(this);
		
	}

	@Override
	protected void onResume() {
		mGroupIDDrop.setStringList(LoginActivity.this,DemoApp.getmHisList().getGroupInfo_list(),
				DemoApp.getmHisList().getGroup_list());
		BaseAdapter adp = mGroupIDDrop.getAdapter();
		if(adp != null)
			adp.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		String uname = null;

		switch (v.getId()) {
		case R.id.btn_confirm:
			// 过滤掉不合法的用户名
			uname = ((EditText) findViewById(R.id.edt_username)).getText()
					.toString();
			if (TextUtils.isEmpty(uname)) {
				showTip("用户名不能为空");
				return;
			}

			// 设置全局的mAuth_id
			DemoApp.mAuth_id = uname;
			DemoApp.setHostUser( (User)FuncUtil.readObject(this, uname));
			DemoApp.getHostUser().setUsername(uname);
			FuncUtil.saveObject(this,DemoApp.getHostUser(), uname);
			
			// 跳转至主界面
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			break;
			
		case R.id.btn_idf_speech:
			String gourp_id_v = mGroupIDDrop.getText();
			if(TextUtils.isEmpty(gourp_id_v))
			{
				mGroupIDDrop.requestFocus();
				showTip(getString(R.string.groupid_null));
				return;
			}
			intent = new Intent(LoginActivity.this, VocalIdentifyActivity.class);
			intent.putExtra("group_id", gourp_id_v);
			startActivity(intent);
			break;

		case R.id.btn_idf_face:
			String gourp_id_f = mGroupIDDrop.getText();
			if(TextUtils.isEmpty(gourp_id_f))
			{
				mGroupIDDrop.requestFocus();
				showTip(getString(R.string.groupid_null));
				return;
			}
			intent = new Intent(LoginActivity.this, FaceIdentifyActivity.class);
			intent.putExtra("isIdentify", true);
			intent.putExtra("group_id", gourp_id_f);
			startActivity(intent);
			break;
			
		default:
			break;
		}
	}
	
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
}
