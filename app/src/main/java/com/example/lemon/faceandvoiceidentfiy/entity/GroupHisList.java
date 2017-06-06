package com.example.lemon.faceandvoiceidentfiy.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupHisList implements Serializable{
	
	private static final long serialVersionUID = 2L;
//	private ArrayList<String> group_full_name_list ;  
	// 已加入组列表id-info
	private HashMap<String, String> group_map = null;

	// 已加入的组id列表
	private ArrayList<String> group_id_list = null;
	// 已加入的组列表
	private ArrayList<String> group_full_name_list = null;
	
	public GroupHisList(){
		 group_map = new HashMap<String, String>();
	}
	
	public void addHisItem(String groupId, String groupFullName){
		if(null == group_map) {
			 group_map = new HashMap<String, String>();
		}
		group_map.put(groupId, groupFullName);
	}
	
	public void removeHisItem(String groupId){
		if(null == group_map) {
			return;
		}
		group_map.remove(groupId);
	}
	
	public void setGroup_list(HashMap<String, String> group_list) {
		this.group_map = group_list;
	}

	public HashMap<String, String> getGroup_Hashlist() {
		if(null == group_map) {
			 group_map = new HashMap<String, String>();
		}
		return group_map;
	}
	
	public String getGroupInfo(String key) {
		return group_map.get(key);
	}
	
	public ArrayList<String> getGroup_list() {
		if(null == group_map) {
			 group_map = new HashMap<String, String>();
		}
		
		group_id_list = new ArrayList<String>();
		Iterator iterator = group_map.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String key = (String) entry.getKey();
			group_id_list.add(key);
		}
		return group_id_list;
	}

	public ArrayList<String> getGroupInfo_list() {
		if(null == group_map) {
			 group_map = new HashMap<String, String>();
		}

		group_full_name_list = new ArrayList<String>();
		Iterator iterator = group_map.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String val = (String) entry.getValue();
			group_full_name_list.add(val);
		}
		return group_full_name_list;
	}
}
