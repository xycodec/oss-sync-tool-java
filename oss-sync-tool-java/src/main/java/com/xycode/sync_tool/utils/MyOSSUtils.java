package com.xycode.sync_tool.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.xycode.sync_tool.config.Config;
import com.xycode.sync_tool.internel.FileScan;
import com.xycode.sync_tool.internel.OSSUpdater;

public class MyOSSUtils {

	public static long dateToNum(String date_str) {
		String dateFormatStr="E, dd MMM yyyy HH:mm:ss z";//时间格式
		SimpleDateFormat sdf=new SimpleDateFormat(dateFormatStr,Locale.US);
		//System.out.println(sdf.format(new Date(System.currentTimeMillis())));
		long result=0;
		try {
			result=(sdf.parse(date_str).getTime())/1000;//以秒为精度,GMT -> CST
		} catch (ParseException e) {
			MyLogger.logger.warning(date_str+", illegal format!");
			e.printStackTrace();
		}
		return result;
	}
	
	//代理模式
	public static void setIsUpdate(boolean isUpdate) {
		OSSUpdater.setIsUpdate(isUpdate);
	}
	
	public static boolean getIsUpdate() {
		return OSSUpdater.getIsUpdate();
	}
	
	
	public static List<File> getUpdateList() {
		return OSSUpdater.getUpdateList();
	}
	
	public static void clearUpdateList() {
		OSSUpdater.clearUpdateList();
	}
	
	/*
	 * 使用CountDownLatch来实现任务等待
	 */
	public static void waitTaskFinished() {
		try {
			OSSUpdater.latch.await(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			OSSUpdater.latch=new CountDownLatch(Config.UThread_num);
		}
	}
	
	private static ExecutorService es=null;
	
	public static void update(int task_num,List<File> cache_file_list,List<String> cloud_file_list) {
		assert task_num>0;
		assert cache_file_list.size()==cloud_file_list.size();
		List<List<File>> src_file_part_list=new ArrayList<>();
		List<List<File>> cache_file_part_list=new ArrayList<>();
		List<List<String>> cloud_file_part_list=new ArrayList<>();
		for(int i=0;i<task_num;++i) {
			src_file_part_list.add(new ArrayList<>());
			cache_file_part_list.add(new ArrayList<>());
			cloud_file_part_list.add(new ArrayList<>());
		}
		//分配任务(热点分离)
		for(int i=0;i<cache_file_list.size();++i) {
			src_file_part_list.get(i%task_num).add(FileScan.source_list.get(i));
			cache_file_part_list.get(i%task_num).add(cache_file_list.get(i));
			cloud_file_part_list.get(i%task_num).add(cloud_file_list.get(i));
		}
		es=new ThreadPoolExecutor(0, Integer.MAX_VALUE,
	            60L, TimeUnit.SECONDS,
	            new SynchronousQueue<Runnable>()) {
			@Override
			protected void terminated() {
				super.terminated();
				if(OSSUpdater.getIsUpdate()) MyLogger.logger.info("update finished!");
				else MyLogger.logger.info("get updateList finished!");
				//OSSUpdater.shutdown();
			}
		};
		for(int i=0;i<task_num;++i) {
			es.execute(new OSSUpdater(src_file_part_list.get(i),
					cache_file_part_list.get(i), cloud_file_part_list.get(i)));
		}
		es.shutdown();
	}
	
	public static void shutdown() {
		OSSUpdater.shutdown();
	}
	
	public static List<String> getCloudList(List<File> cache_file_list){
		List<String> cloud_file_list=new ArrayList<>();
		for(int i=0;i<cache_file_list.size();++i) {
			cloud_file_list.add(MyFileUtils.format(
					MyFileUtils.concatPath(cache_file_list.get(i),Config.temp_cachespace_name,Config.cloud_path)));
			//System.out.println(cloud_file_list.get(i));
		}
		return cloud_file_list;
		
	}
	
	public static void main(String[] args) {
		List<File> cache_file_list=MyFileUtils.getCacheList(FileScan.source_list);
		List<String> cloud_file_list=getCloudList(cache_file_list);
		update(16, cache_file_list, cloud_file_list);
	}

}
