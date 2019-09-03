package com.xycode.sync_tool.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.common.utils.IOUtils;


public class Config {
	public static int CThread_num;//生成缓存的线程数
	public static int UThread_num;//上传更新文件的线程数
	
	public static List<String> bucket_list;
	public static List<String> included_suffix;
	public static List<String> local_path_list;
	public static String endpoint,accessKeyId,accessKeySecret,bucket_name;
	public static String cloud_path,local_workspace_name,temp_cachespace_name,temp_path;
	public static String log_level;
	public static Boolean show_info;
	static {
		init();
	}
	
	private static void init() {
        InputStream inputStream = null;
        String config_str=null;
		try {
			inputStream = new FileInputStream("config.json");
			config_str = IOUtils.readStreamAsString(inputStream, "utf8");
		} catch (IOException e) {
			e.printStackTrace();
		}
        JSONObject config = JSON.parseObject(config_str);
        bucket_list=((JSONArray) config.get("bucket_list"))
        		.toJavaList(String.class);
        included_suffix=((JSONArray) config.get("include_suffix"))
        		.toJavaList(String.class);
        local_path_list=((JSONArray) config.get("local_path_list"))
        		.toJavaList(String.class);
        
        endpoint=config.getString("endpoint");
        accessKeyId=config.getString("accessKeyId");
        accessKeySecret=config.getString("accessKeySecret");
        bucket_name=config.getString("bucket_name");
        
        cloud_path=config.getString("cloud_path");
        temp_path=config.getString("temp_path");
        
        local_workspace_name=config.getString("local_workspace_name");
        temp_cachespace_name=config.getString("temp_cachespace_name");
        
        log_level=config.getString("log_level");
        
        show_info=config.getBoolean("show_info");
        
        CThread_num=config.getIntValue("CThread_num");
        UThread_num=config.getIntValue("UThread_num");
	}
	
	
	public static void main(String[] args) throws IOException {
                
	}
}
