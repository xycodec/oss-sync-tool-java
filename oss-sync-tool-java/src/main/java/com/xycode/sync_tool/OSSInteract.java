package com.xycode.sync_tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.xycode.sync_tool.config.Config;
import com.xycode.sync_tool.internel.FileScan;
import com.xycode.sync_tool.utils.MyFileUtils;
import com.xycode.sync_tool.utils.MyOSSUtils;

public class OSSInteract {
	private static BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
	private static List<File> updateList;//待更新的文件列表
	private static List<String> include_suffix;//指定扫描的文件后缀
	private static List<String> include_suffix_bk;//指定扫描的文件后缀备份
	
	private static String input(String info) {
		System.out.printf(info);
		String in=null;
		try {
			in=reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return in;
	}
	
	private static String input() {
		String in=null;
		try {
			in=reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return in;
		
	}
	
	private static void ls() {//查看并存储待更新的文件列表
		List<File> cache_file_list=MyFileUtils.getCacheList(FileScan.source_list);//获得缓存文件list
		MyFileUtils.generate_cache(Config.CThread_num,FileScan.source_list,cache_file_list);//16线程生成缓存
		MyFileUtils.waitTaskFinished();
		List<String> cloud_file_list=MyOSSUtils.getCloudList(cache_file_list);//获得云端文件list

		MyOSSUtils.clearUpdateList();//先清除待更新列表，防止重复ls导致的重复添加
		MyOSSUtils.setIsUpdate(false);//不更新到云端,只list
		MyOSSUtils.update(Config.UThread_num, cache_file_list, cloud_file_list);//因为设置了标志,所以这里并不会更新
		MyOSSUtils.waitTaskFinished();

		updateList=MyOSSUtils.getUpdateList();
//		for(File f:updateList) {
//			System.out.println(f);
//		}
	}
	
	private static void ls_update() {//根据待更新文件列表进行更新
		//组合ls使用,之前已经更新过缓存,所以这里不再更新
//		MyFileUtils.generate_cache(Config.CThread_num,updateList,MyFileUtils.getCacheList(updateList));//16线程生成缓存
//		MyFileUtils.waitTaskFinished();
		MyOSSUtils.setIsUpdate(true);
		MyOSSUtils.update(Config.UThread_num, MyFileUtils.getCacheList(updateList), 
				MyOSSUtils.getCloudList(MyFileUtils.getCacheList(updateList)));
		MyOSSUtils.waitTaskFinished();
		MyOSSUtils.clearUpdateList();//清除缓存
	}
	
	private static void update() {//直接更新
		List<File> cache_file_list=MyFileUtils.getCacheList(FileScan.source_list);//获得缓存文件list
		MyFileUtils.generate_cache(Config.CThread_num,FileScan.source_list,cache_file_list);//16线程生成缓存
		MyFileUtils.waitTaskFinished();
		
		List<String> cloud_file_list=MyOSSUtils.getCloudList(cache_file_list);//获得云端文件list
		MyOSSUtils.setIsUpdate(true);
		MyOSSUtils.update(Config.UThread_num, cache_file_list, cloud_file_list);
		MyOSSUtils.waitTaskFinished();
		MyOSSUtils.clearUpdateList();//清除缓存
	}
	
	private static void cfg(String op) {
		if(op.equals("suffix")) {
			include_suffix_bk=new ArrayList<>(Config.included_suffix);
			include_suffix=Arrays.asList(input("please input suffix array:\n").split(" "));
			Config.included_suffix=include_suffix;
		}else if(op.equals("bucket")) {
			Config.bucket_name=input("please input bucket name:\n");
		}
	}
	
	private static void restoreSuffix() {
		include_suffix=new ArrayList<>(include_suffix_bk);
		Config.included_suffix=include_suffix;
	}
	
	private static void clearCache() {
		File temp_dir=new File(Config.temp_path);
		MyFileUtils.delete(temp_dir);
	}
	
	private static void printInfo() {
	    System.out.println("****************** oss_sync_tool_java *************************");
	    System.out.println("* input ls:  list the files that need to be updated.          *");
	    System.out.println("* input ls -u:  update list after execute ls.                 *");
	    System.out.println("* input update:  update all files.                            *");
	    System.out.println("* input cfg suffix:  configure include_suffix.                *");
	    System.out.println("* input cfg bucket:  configure oss2-bucket-name.              *");
	    System.out.println("* input restore:  restore (suffix) to the original state.     *");
	    System.out.println("* input clear:  clear all temp files.                         *");
	    System.out.println("* input show buckets:  show available bucket name.            *");
	    System.out.println("* input show info:  set up display help information or not.   *");
	    System.out.println("* input help:  show the help information.                     *");
	    System.out.println("* input q:  exit the program.                                 *");
	    System.out.println("********************************************** --xycode *******");
	}
	
	public static void interact() {
		if(Config.show_info) printInfo();
		String op=input("[oss-xycode]: ").toLowerCase();
		if(op.equals("ls")) {
			ls();
			interact();
		}else if(op.equals("ls -u")) {
			ls_update();
			interact();
		}else if(op.equals("update")) {
			update();
			interact();
		}else if(op.equals("cfg suffix")) {
			cfg("suffix");
			interact();
		}else if(op.equals("cfg bucket")) {
			cfg("bucket");
			interact();
		}else if(op.equals("restore")) {
			restoreSuffix();
			interact();
		}else if(op.equals("clear")) {
			clearCache();
			interact();
		}else if(op.equals("show info")) {
			String judge=input("show help info?(y/n)\n").toLowerCase();
			if(judge.equals("y")||judge.equals("yes")) {
				Config.show_info=true;
			}else if(judge.equals("n")||judge.equals("no")) {
				Config.show_info=false;
			}else {
				System.err.println("illegal arguments!");
			}
			interact();
		}else if(op.equals("help")) {
			printInfo();
			interact();
		}else if(op.equals("q")||op.equals("quit")||op.equals("exit")) {
			MyOSSUtils.shutdown();
			return;
		}else if(op.equals("show buckets")) {
			for(String item:Config.bucket_list) {
				System.out.println(item);
			}
			interact();
		}else if(op.length()==0) {
			interact();
		}else {
			System.err.println("illegal command!");
			interact();
		}
	}
	
	public static void interact2() {
		printInfo();
		Config.show_info=false;
		interact();
	}
	
	//单例模式...
	private OSSInteract() {
		
	}
	
	private static OSSInteract ossI=new OSSInteract();
	public static OSSInteract getInstance() {
		return ossI;
	}
	
	public static void main(String[] args) {
		OSSInteract o=OSSInteract.getInstance();
		o.interact();
	}
}
