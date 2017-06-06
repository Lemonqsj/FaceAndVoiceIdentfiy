package com.example.lemon.faceandvoiceidentfiy.entity;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 实体类，保存用户信息
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 * */
public class User implements Serializable{
	private static final long serialVersionUID = 1L;
	// 用户名
	private String username = "default";
	// 脸部照片
	private transient Bitmap facePic;
	// 是否已注册
	private boolean isEnrolled;
	// 是否已登录
	private boolean isLogined;
	// 声纹标准分
	private double voice_score;
	// 人脸标准分
	private double face_score;
	// 融合标准分
	private double fusion_score;
	// 已加入的组列表id-info
	private HashMap<String, String> group_list = new HashMap<String, String>();  
	// 已加入的组信息列表
	private ArrayList<String> group_full_list = new ArrayList<String>();
	public User() {
		
	}
	
	public User(String username, Bitmap facePic) {
		this.username = username;
		this.facePic = facePic;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Bitmap getFacePic() {
		return facePic;
	}

	public void setFacePic(Bitmap facePic) {
		if (null != this.facePic) {
			this.facePic.recycle();
		}
		this.facePic = Bitmap.createBitmap(facePic);
	}
	
	public void setEnrolled(boolean enrolled) {
		this.isEnrolled = enrolled;
	}
	
	public boolean isEnrolled() {
		return this.isEnrolled;
	}
	
	public boolean isLogined() {
		return this.isLogined;
	}
	
	public void setLogined(boolean logined) {
		this.isLogined = logined;
	}

	public double getVoiceScore() {
		return voice_score;
	}

	public void setVoiceScore(double voice_score) {
		this.voice_score = voice_score;
	}

	public double getFaceScore() {
		return face_score;
	}

	public void setFaceScore(double face_score) {
		this.face_score = face_score;
	}

	public double getFusionScore() {
		return fusion_score;
	}

	public void setFusionScore(double fusion_score) {
		this.fusion_score = fusion_score;
	}

	public void setGroup_list(HashMap<String, String> group_list) {
		this.group_list = group_list;
	}

	public HashMap<String, String> getGroup_Hashlist() {
		if(group_list == null){
			group_list = new HashMap<String, String>();  
		}
		return group_list;
	}

	public ArrayList<String> getGroup_list() {
		if(null == group_list) {
			 group_list = new HashMap<String, String>();
		}
		
		group_full_list = new ArrayList<String>();
		Iterator iterator = group_list.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String val = (String) entry.getValue();
			group_full_list.add(val);
		}
		return group_full_list;
	}
}
