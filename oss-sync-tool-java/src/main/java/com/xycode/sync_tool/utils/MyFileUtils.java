package com.xycode.sync_tool.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystemNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.entity.DisallowIdentityContentLengthStrategy;

import com.xycode.sync_tool.config.Config;
import com.xycode.sync_tool.internel.FileScan;
import com.xycode.sync_tool.internel.GenerateCache;
import com.xycode.sync_tool.internel.OSSUpdater;

public class MyFileUtils {
	
	public static String format(String path) {
		return path
				.replace('\\', '/')//普通的字符替换
				.replaceAll("//", "/");//正则
	}
	
	public static boolean newer(File src,File dst) {
		if(!src.exists()) {
			MyLogger.logger.warning(src+" doesn't exsit!");
			try {
				throw new FileNotFoundException(src+" doesn't exsit!");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(!dst.exists()) {//目标文件不存在,默认源文件较新
			return true;
		}
		
		if(src.lastModified()>dst.lastModified()) {//源文件较新
			return true;
		}else return false;
	}
	
	public static String getSuffix(String filepath) {
		String filename = filepath.substring(filepath.lastIndexOf("/")+1);
		if(filename.lastIndexOf(".")==-1) {
			return "";//没有后缀,如MakeFile文件
		}
		return filename.substring(filename.lastIndexOf(".")+1);
	}
	
	public static void delete(File path) {
		if(path.exists()) {
			if(path.isDirectory()) {
				for(File f:path.listFiles()) {
					delete(f);
				}
			}
			path.delete();
		}
	}
	
	public static String concatPath(File srcfilepath,String sep,String dstdirpath) {
		if(!srcfilepath.isFile()) {
			MyLogger.logger.severe(srcfilepath+" is not a file!");
			try {
				throw new FileNotFoundException(srcfilepath+" is not a file!");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
//		if(!new File(dstdirpath).isDirectory()) {
//			MyLogger.logger.severe(dstdirpath+" is not a directory!");
//			try {
//				throw new FileNotFoundException(dstdirpath+" is not a directory!");
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
		
		String[] tokens=MyFileUtils.format(srcfilepath.toString()).split(sep.replaceAll("\\$", "\\[\\$\\]"));
		if(tokens.length!=2) {
			MyLogger.logger.severe(srcfilepath+" ,it's format is illegal!");
			try {
				throw new FileNotFoundException(srcfilepath+"'s format is illegal!");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return dstdirpath+tokens[1];
	}
	
	/*
	 * 考虑使用CountDownLatch来实现任务等待
	 */
	
	public static void waitTaskFinished() {
		try {
			GenerateCache.latch.await(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			GenerateCache.latch=new CountDownLatch(Config.CThread_num);
		}
	}
	
	private static ExecutorService es=null;
	//生成缓存
	public static void generate_cache(int task_num,List<File> src_file_list,List<File> cache_file_list) {
		assert task_num>0;
		assert src_file_list.size()==cache_file_list.size();
//		System.out.println(src_file_list.size());
//		for(int i=0;i<cache_file_list.size();++i) {
//			System.out.println(cache_file_list.get(i));
//		}
		es=new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>()) {
			@Override
			protected void terminated() {
				super.terminated();
				MyLogger.logger.info("generate cache finished!");
			}
		};
		List<List<File>> src_file_part_list=new ArrayList<>(),
				cache_file_part_list=new ArrayList<>();
		for(int i=0;i<task_num;++i) {
			src_file_part_list.add(new ArrayList<>());
			cache_file_part_list.add(new ArrayList<>());
		}
		
		//分配任务(热点分离)
		for(int i=0;i<src_file_list.size();++i) {
			src_file_part_list.get(i%task_num).add(src_file_list.get(i));
			cache_file_part_list.get(i%task_num).add(cache_file_list.get(i));
		}
		for(int i=0;i<task_num;++i) {
			es.execute(new GenerateCache(src_file_part_list.get(i), cache_file_part_list.get(i)));	
		}
		es.shutdown();
	}
	
	
	public static List<File> getCacheList(List<File> src_file_list){
		List<File> cache_file_list=new ArrayList<File>();
		for(int i=0;i<src_file_list.size();++i) {
			cache_file_list.add(new File(MyFileUtils
					.concatPath(src_file_list.get(i),Config.local_workspace_name,Config.temp_path)));
		}
		return cache_file_list;
	}
	
	public static void main(String[] args) {
//		System.out.println(System.getProperty("user.dir"));
//		System.out.println(Config.local_workspace_name);
//		System.out.println("$WorkSpace$".replaceAll("\\$", "\\[\\$\\]"));
		
		generate_cache(16,FileScan.source_list,getCacheList(FileScan.source_list));
	}

}
