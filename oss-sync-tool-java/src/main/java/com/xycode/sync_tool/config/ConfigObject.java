package com.xycode.sync_tool.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.common.utils.IOUtils;

public class ConfigObject {
	private String endpoint;
	private String accessKeyId;
	private String accessKeySecret;
	private String bucket_name;
	private List<String> bucket_list;
	private List<String> local_path_list;
	private String temp_path;
	private String cloud_path;
	private List<String> include_suffix;
	
	private int CThread_num;
	private int UThread_num;
	
	private String local_workspace_name;
	private String temp_cachespace_name;
	private String log_level;
	
	private boolean show_info;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getAccessKeyId() {
		return accessKeyId;
	}

	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	public String getAccessKeySecret() {
		return accessKeySecret;
	}

	public void setAccessKeySecret(String accessKeySecret) {
		this.accessKeySecret = accessKeySecret;
	}

	public String getBucket_name() {
		return bucket_name;
	}

	public void setBucket_name(String bucket_name) {
		this.bucket_name = bucket_name;
	}

	public List<String> getBucket_list() {
		return bucket_list;
	}

	public void setBucket_list(List<String> bucket_list) {
		this.bucket_list = bucket_list;
	}

	public List<String> getLocal_path_list() {
		return local_path_list;
	}

	public void setLocal_path_list(List<String> local_path_list) {
		this.local_path_list = local_path_list;
	}

	public String getTemp_path() {
		return temp_path;
	}

	public void setTemp_path(String temp_path) {
		this.temp_path = temp_path;
	}

	public String getCloud_path() {
		return cloud_path;
	}

	public void setCloud_path(String cloud_path) {
		this.cloud_path = cloud_path;
	}

	public List<String> getInclude_suffix() {
		return include_suffix;
	}

	public void setInclude_suffix(List<String> include_suffix) {
		this.include_suffix = include_suffix;
	}

	public int getCThread_num() {
		return CThread_num;
	}

	public void setCThread_num(int cThread_num) {
		CThread_num = cThread_num;
	}

	public int getUThread_num() {
		return UThread_num;
	}

	public void setUThread_num(int uThread_num) {
		UThread_num = uThread_num;
	}

	public String getLocal_workspace_name() {
		return local_workspace_name;
	}

	public void setLocal_workspace_name(String local_workspace_name) {
		this.local_workspace_name = local_workspace_name;
	}

	public String getTemp_cachespace_name() {
		return temp_cachespace_name;
	}

	public void setTemp_cachespace_name(String temp_cachespace_name) {
		this.temp_cachespace_name = temp_cachespace_name;
	}

	public String getLog_level() {
		return log_level;
	}

	public void setLog_level(String log_level) {
		this.log_level = log_level;
	}

	public boolean isShow_info() {
		return show_info;
	}

	public void setShow_info(boolean show_info) {
		this.show_info = show_info;
	}
	
	public static ConfigObject config=null;
	private static void init() {
        InputStream inputStream = null;
        String config_str=null;
		try {
			inputStream = new FileInputStream("config.json");
			config_str = IOUtils.readStreamAsString(inputStream, "utf8");
		} catch (IOException e) {
			e.printStackTrace();
		}
        config = JSON.parseObject(config_str,ConfigObject.class);
	}
	static {
		init();
	}
	public static void main(String[] args) {
		System.out.println(JSON.toJSONString(config));
	}
	
	
}
